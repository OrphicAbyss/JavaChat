package javachat.network;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Queue;
import java.util.logging.Level;
import java.util.logging.Logger;
import javachat.JavaChat;
import javachat.network.message.Packet;
import javachat.network.socket.SocketController;
import javachat.network.util.IPUtil;
import javachat.network.util.UPnP;

/**
 * Chat server class. Creates a server sockets and accepts incoming connections
 * until disconnect is called. When a client connects a ClientSocket class is
 * created to handle incoming messages.
 * 
 * @author DrLabman
 */
public class Server implements Runnable {
	private int port;
	private boolean connected;
	private boolean disconnect;
	private ArrayList<ClientSocket> clients;
	private ServerSocket srvr;
	private KeepAlive keepAlive;
	
	public Server(int port){
		this.port = port;
		disconnect = false;
		connected = false;
		
		clients = new ArrayList<ClientSocket>();
		
		// Print useful information for user
		IPUtil.printExternalIP();
		IPUtil.printInternalIP();
		// Register UPnP port mapping
		UPnP.RegisterPort(port);
		
		keepAlive = new KeepAlive();		
		Thread t1 = new Thread(keepAlive);
		t1.start();
		
		Thread t = new Thread(this);
		t.start();
	}

	/**
	 * Handle incoming connections, create ClientSockets for them.
	 */
	@Override
	public void run() {
		try {
			srvr = new ServerSocket(port);
			connected = true;
			while (!disconnect){
				Socket skt = srvr.accept();
				ClientSocket client = new ClientSocket(skt);
				keepAlive.addToQueue(client);
				clients.add(client);
			}
		} catch (SocketException ex) {
			if (!ex.getMessage().equals("socket closed"))
				JavaChat.println("Socket Exception: " + ex.getMessage());
		} catch (IOException ex) {
			JavaChat.println("IO Exception: " + ex.getMessage());
		}
	}
	
	public boolean isConnected() {
		return connected;
	}
	
	public void sendMsg(SocketController sender, Packet msg) {
		for (ClientSocket client: clients){
			if (client != sender)
				client.sendMsg(msg);
		}
	}
	
	public void disconnect() {
		disconnect = true;
		for (ClientSocket client: clients){
			client.disconnect();
		}
		
		try {
			srvr.close();
		} catch (IOException ex) {
			JavaChat.println("Exception when closing socket: " + ex.getMessage());
		}
		JavaChat.println("No longer listening for connections.");
		
		UPnP.UnregisterPort();
	}
	
	public void printClientNames(){
		StringBuilder sb = new StringBuilder();
		for (ClientSocket client: clients){
			sb.append(client.getName());
			sb.append(" ");
		}
		System.out.println("Users: " + sb.toString());
	}
	
	private class KeepAlive implements Runnable {
		LinkedList<ClientSocket> queue = new LinkedList<ClientSocket>();
		LinkedList<ClientSocket> pinged = new LinkedList<ClientSocket>();

		@Override
		public void run() {
			while (!disconnect){
				long time = System.currentTimeMillis();

				if (pinged.size() > 0){
					if (pinged.peekFirst().getNextKeepAlive() < time + 15 * 1000){
						// Timed out on ping (more than 15 seconds to respond)
						ClientSocket client = pinged.removeFirst();
						client.disconnect();
					} else if (pinged.peekFirst().getNextKeepAlive() > time){
						ClientSocket client = pinged.removeFirst();
						queue.addLast(client);
					}
				}

				if (queue.size() > 0){	
					if (queue.peekFirst().getNextKeepAlive() < time){
						ClientSocket client = queue.removeFirst();
						client.sendMsg(Packet.createPingPacket());
						pinged.addLast(client);
					}
				}

				try {
					Thread.sleep(1000);
				} catch (InterruptedException ex) {

				}
			}
		}

		public void addToQueue(ClientSocket client){
			queue.addLast(client);
		}

		public void removeFromQueue(ClientSocket client){
			queue.remove(client);
			pinged.remove(client);
		}
	}
	
	/**
	 * Class for handling incoming messages and the disconnected callback.
	 */
	private class ClientSocket extends SocketController {
		private String name;
		private long nextKeepAlive;
		
		public ClientSocket(Socket socket){
			super(socket);
			nextKeepAlive = System.currentTimeMillis() + 1000;
		}
		
		@Override
		public void receiveMsg(Packet msg){
			if (msg != null){			
				switch (msg.getType()){
					case MSG:
						// Send message back to all other clients
						sendMsg(this, msg);
						break;
					case HELO:
						name = msg.getData()[0];
						String connectedMsg = name + " connected...";
						sendMsg(null, Packet.createMsgPacket(connectedMsg));
						printClientNames();
						break;
					case NAME:
						String names[] = msg.getData();
						String newNameMsg = names[0] + " changed name to " + names[1];
						name = names[1];
						sendMsg(null, Packet.createMsgPacket(newNameMsg));
						break;
					case PONG:
						nextKeepAlive = System.currentTimeMillis() + 60 * 1000;
						//JavaChat.println("Pong!");
						break;
					case QUIT:
						this.disconnect();
						JavaChat.println("Client disconnected.");
						break;
					default:
						JavaChat.println("Unknown packet type from connection: " + msg.getType());
						break;
				}
			}
		}

		@Override
		public void disconnected(){
			clients.remove(this);
		}
		
		public String getName(){
			return name;
		}
		
		public long getNextKeepAlive(){
			return nextKeepAlive;
		}
	}
}
