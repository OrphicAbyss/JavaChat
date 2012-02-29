package javachat.network;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;
import javachat.JavaChat;
import javachat.network.message.Packet;
import javachat.network.socket.SocketController;
import javachat.network.util.IPUtil;
import javachat.network.util.UPnP;

/**
 * Server class, waits for connection on given port
 * 
 * @author DrLabman
 */
public class Server implements Runnable {
	private int port;
	private boolean connected;
	private boolean disconnect;
	private ArrayList<SocketController> clients;
	private ServerSocket srvr;
	
	public Server(int port){
		this.port = port;
		disconnect = false;
		connected = false;
		
		clients = new ArrayList<SocketController>();
		
		// Print useful information for user
		IPUtil.printExternalIP();
		IPUtil.printInternalIP();
		// Register UPnP port mapping
		UPnP.RegisterPort(port);

		Thread t = new Thread(this);
		t.start();
	}

	@Override
	public void run() {
		try {
			srvr = new ServerSocket(port);
			connected = true;
			while (!disconnect){
				Socket skt = srvr.accept();
				clients.add(new ClientSocket(skt));
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
		for (SocketController client: clients){
			if (client != sender)
				client.sendMsg(msg);
		}
	}
	
	public void disconnect() {
		disconnect = true;
		for (SocketController client: clients){
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
	
	/**
	 * Class for handling incoming messages and the disconnected callback.
	 */
	private class ClientSocket extends SocketController {
		public ClientSocket(Socket socket){
			super(socket);
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
						String connectedMsg = msg.getData()[0] + " connected...";
						sendMsg(null, Packet.createMsgPacket(connectedMsg));
						break;
					case NAME:
						String names[] = msg.getData();
						String newNameMsg = names[0] + " changed name to " + names[1];
						sendMsg(null, Packet.createMsgPacket(newNameMsg));
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
	}
}
