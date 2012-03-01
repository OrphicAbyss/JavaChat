package javachat.network;

import java.io.IOException;
import java.net.ConnectException;
import java.net.Socket;
import java.net.UnknownHostException;
import javachat.JavaChat;
import javachat.network.message.Packet;
import javachat.network.socket.SocketController;

/**
 * This class acts as a pass through to the SocketController which handles the
 * actual reading and writing.
 * 
 * Here we pass through messages to send to the SocketController and handle
 * incoming messages.
 * 
 * @author DrLabman
 */
public class Client extends SocketController {
	private SocketController socketCtrl;
	private String name;
	
	public static Client createClient(String hostname, int port, String name) throws ConnectException{
		try {
			//Create a socket to connect with
			Socket skt = new Socket(hostname, port);
			// Create the client object using the socket
			Client client = new Client(skt);
			// Set the clients name and send the hello message to the server
			client.name = name;
			client.sendHello();
			return client;
		} catch (UnknownHostException ex) {
			JavaChat.println("Unknown Host: " + ex.getMessage());
		} catch (ConnectException ex) {
			JavaChat.println("Unable to connect to server: " + ex.getMessage());
			throw ex;
		} catch (IOException ex) {
			JavaChat.println("IO Exception: " + ex.getMessage());
		}
		// Something went wrong.
		return null;
	}
	
	private Client(Socket socket){
		super(socket);
	}

	@Override
	public void receiveMsg(Packet msg){
		if (msg != null){
						switch (msg.getType()){
				case MSG:
					// Send message back to all other clients
					JavaChat.println(msg.getData()[0]);
					break;
				case PING:
					sendMsg(Packet.createPongPacket());
					//JavaChat.println("Ping!");
					break;
				case QUIT:
					if (!socketCtrl.isDisconnecting())
						disconnect();
					JavaChat.println("Client disconnected.");
					break;
				case HELO:
				case NAME:
					// Not expected
					JavaChat.println("Received unexpected packet type: " + msg.getType().name());
					break;
				default:
					JavaChat.println("Unknown packet type from connection: " + msg.getType().name());
					break;
			}
		}
	}
	
	@Override
	public void disconnected(){
		JavaChat.disconnected();
	}
	
	/**
	 * Override sendMsg because we need to prepend our name to our messages
	 * and we want to print the message as it doesn't echo back to us.
	 * 
	 * @param msg 
	 */
	@Override
	public void sendMsg(String msg) {
		String fullMessage = "[" + name + "] " + msg;
		super.sendMsg(fullMessage);
		JavaChat.println(fullMessage);
	}
	
	private void sendHello(){
		sendMsg(Packet.createHeloPacket(name));
	}
	
	/**
	 * @param name the name to set
	 */
	public void setName(String name) {
		sendMsg(Packet.createNamePacket(this.name, name));
		this.name = name;
	}
}
