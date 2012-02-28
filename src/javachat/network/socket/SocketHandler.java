package javachat.network.socket;

import javachat.network.message.Packet;

/**
 * Classes that implement this interface handle the data which is read out
 * of a socket by the SocketController class.
 * 
 * @author DrLabman
 */
public interface SocketHandler {
	/**
	 * Each line that is received from the socket is sent to this method for
	 * handling
	 * 
	 * @param sktCtrl The Socket controller that received the message
	 * @param msg String received from the socket
	 */
	public void receiveMsg(SocketController sktCtrl, Packet msg);
	
	/**
	 * When a socket is closed the call back is used to alert the creating class.
	 * 
	 * @param sktCtrl The Socket controller that disconnected
	 */
	public void disconnected(SocketController sktCtrl);
}
