package javachat.network;

import java.io.IOException;
import java.net.ConnectException;
import java.net.Socket;
import java.net.UnknownHostException;
import javachat.JavaChat;
import javachat.network.message.Packet;
import javachat.network.socket.SocketController;
import javachat.network.socket.SocketHandler;

/**
 * This class acts as a pass through to the SocketController which handles the
 * actual reading and writing.
 * 
 * Here we pass through messages to send to the SocketController and handle
 * incoming messages.
 * 
 * @author DrLabman
 */
public class Client implements SocketHandler {
	private SocketController socketCtrl;
	private String name;
	
	public Client(String hostname, int port, String name) throws ConnectException{	
		this.name = name;
		try {
			Socket skt = new Socket(hostname, port);
			socketCtrl = new SocketController(this, skt);
			sendHello();
		} catch (UnknownHostException ex) {
			JavaChat.println("Unknown Host: " + ex.getMessage());
		} catch (ConnectException ex) {
			JavaChat.println("Unable to connect to server: " + ex.getMessage());
			throw ex;
		} catch (IOException ex) {
			JavaChat.println("IO Exception: " + ex.getMessage());
		}
	}

	@Override
	public void receiveMsg(SocketController sktCtrl, Packet msg){
		if (msg != null){
						switch (msg.getType()){
				case MSG:
					// Send message back to all other clients
					JavaChat.println(msg.getData()[0]);
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
	public void disconnected(SocketController sktCtrl){
		JavaChat.disconnected();
	}
	
	public void sendMsg(String msg) {
		String fullMessage = "[" + name + "] " + msg;
		socketCtrl.sendMsg(fullMessage);
		JavaChat.println(fullMessage);
	}

	public void sendCmd(Packet cmd){
		socketCtrl.sendCmd(cmd);
	}
	
	public boolean isConnected() {
		return (socketCtrl == null) ? false : socketCtrl.isConnected();
	}
	
	public void disconnect() {
		socketCtrl.disconnect();
	}

	private void sendHello(){
		sendCmd(Packet.createHeloPacket(name));
	}
	
	/**
	 * @param name the name to set
	 */
	public void setName(String name) {
		sendCmd(Packet.createNamePacket(this.name, name));
		this.name = name;
	}
}
