package javachat.network;

import java.io.IOException;
import java.net.ConnectException;
import java.net.Socket;
import java.net.UnknownHostException;
import javachat.JavaChat;
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
			sendCmd("HELO " + name);
			//JavaChat.println("Connected!");
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
	public void receiveMsg(SocketController sktCtrl, String msg){
		if (msg.length() != 0){
			if (msg.endsWith("\n")){
				msg = msg.substring(0,msg.length()-1);
			}
			
			if (msg.startsWith("CMD")){
				if (msg.endsWith("QUIT")){
					disconnect();
				} else {
					JavaChat.println("Unknown command from connection: " + msg);
				}
			} else if (msg.startsWith("MSG")) {
				JavaChat.println(msg.substring(4)); // Output message
			}
		}
	}
	
	@Override
	public void disconnected(SocketController sktCtrl){
		JavaChat.disconnected();
	}
	
	public void sendMsg(String msg) {
		sendMsg(msg,true);
	}

	public void sendMsg(String msg, boolean echo) {
		String fullMessage = "[" + name + "] " + msg;
		socketCtrl.sendMsg(fullMessage);
		if (echo)
			JavaChat.println(fullMessage);
	}

	public void sendCmd(String cmd){
		socketCtrl.sendCmd(cmd);
	}
	
	public boolean isConnected() {
		return (socketCtrl == null) ? false : socketCtrl.isConnected();
	}
	
	public void disconnect() {
		socketCtrl.disconnect();
	}

	/**
	 * @param name the name to set
	 */
	public void setName(String name) {
		sendCmd("NAME " + this.name + " " + name);
		this.name = name;
	}
}
