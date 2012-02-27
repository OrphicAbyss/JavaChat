package javachat;

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
	 * @param msg String received from the socket
	 */
	public void receiveMsg(SocketController sktCtrl, String msg);
}
