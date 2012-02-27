package javachat;

/**
 * A chat server or client implements this interface allowing the GUI to send
 * messages to the connected client.
 *
 * @author DrLabman
 */
public interface ChatInterface {
	public boolean isConnected();
	public void sendMsg(String msg);
	public void disconnect();
}
