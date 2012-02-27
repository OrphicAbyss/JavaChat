package javachat;

import java.net.Socket;

/**
 * Handles sending and receiving of messages from a socket for a client.
 * 
 * @author DrLabman
 */
public class ClientSocket implements ChatInterface, SocketHandler {
	private SocketController socketCtrl;

	public ClientSocket(ChatWindow gui, Socket socket){
		socketCtrl = new SocketController(this, socket);
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
					ChatWindow.instance.println("Unknown command from connection: " + msg);
				}
			} else if (msg.startsWith("MSG")) {
				ChatWindow.instance.println(msg.substring(4)); // Output message
			}
		}
	}
	
	@Override
	public void sendMsg(String msg) {
		sendMsg(msg,true);
	}

	public void sendMsg(String msg, boolean echo) {
		socketCtrl.sendMsg(msg);
		if (echo)
			ChatWindow.instance.println(msg);
	}

	public void sendCmd(String cmd){
		socketCtrl.sendCmd(cmd);
	}
	
	@Override
	public boolean isConnected() {
		return socketCtrl.isConnected();
	}
	
	@Override
	public void disconnect() {
		socketCtrl.disconnect();
	}
}
