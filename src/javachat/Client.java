package javachat;

import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;

/**
 *
 * @author DrLabman
 */
public class Client implements ChatInterface {
	private ClientSocket chat;
	
	public Client(ChatWindow gui, String hostname, int port){	
		try {
			Socket skt = new Socket(hostname, port);
			chat = new ClientSocket(gui,skt);
		} catch (UnknownHostException ex) {
			gui.println("Unknown Host: " + ex.getMessage());
		} catch (IOException ex) {
			gui.println("IO Exception: " + ex.getMessage());
		}
	}

	@Override
	public boolean isConnected() {
		if (chat != null)
			return chat.isConnected();
		return false;
	}

	@Override
	public void sendMsg(String msg) {
		if (chat != null)
			chat.sendMsg(msg);
	}

	@Override
	public void disconnect() {
		if (chat != null)
			chat.disconnect();
	}
}
