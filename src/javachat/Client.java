package javachat;

import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;

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
	
	public Client(String hostname, int port){	
		try {
			Socket skt = new Socket(hostname, port);
			socketCtrl = new SocketController(this, skt);
			JavaChat.println("Connected!");
		} catch (UnknownHostException ex) {
			JavaChat.println("Unknown Host: " + ex.getMessage());
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
		
	}
	
	public void sendMsg(String msg) {
		sendMsg(msg,true);
	}

	public void sendMsg(String msg, boolean echo) {
		socketCtrl.sendMsg(msg);
		if (echo)
			JavaChat.println(msg);
	}

	public void sendCmd(String cmd){
		socketCtrl.sendCmd(cmd);
	}
	
	public boolean isConnected() {
		return socketCtrl.isConnected();
	}
	
	public void disconnect() {
		socketCtrl.disconnect();
	}
}
