package javachat.network.socket;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import javachat.JavaChat;
import javachat.network.message.Packet;
import javachat.network.message.PacketType;

/**
 * SocketController deals with reading/writing and cleaning up a socket when
 * the user wants to disconnect or when the server/client disconnects from us.
 * 
 * @author DrLabman
 */
public abstract class SocketController implements Runnable {
	private Socket socket;
	private boolean connected;
	private boolean disconnect;
	private ObjectOutputStream output;
	private ObjectInputStream input;
	//private PrintWriter output;
	//private BufferedReader input;
	
	public SocketController(Socket socket){
		try {
			this.socket = socket;
			connected = false;
			disconnect = false;
			
			output = new ObjectOutputStream(socket.getOutputStream());
			input = new ObjectInputStream(socket.getInputStream());
			//output = new PrintWriter(socket.getOutputStream(), true);
			//input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			
			new Thread(this).start();
		} catch (IOException ex) {
			cleanup();
		}
	}
	
	/**
	 * Cleanup code to shutdown a connection because the user wants to
	 * disconnect or because something went wrong.
	 */
	private void cleanup(){
//		if (!socket.isOutputShutdown()){
//			sendQuit();
//		}
		if (input != null){
			try { input.close(); } catch (IOException ex) {}
		}
		if (output != null)
			try { output.close(); } catch (IOException ex) {}
		if (socket != null && !socket.isClosed()){
			try { socket.close(); } catch (IOException ex) {
				JavaChat.println("Exception closing socket: " + ex.getMessage());
			}
		}
		setConnected(false);
		disconnected();
	}
	
	/**
	 * Main thread which reads data from the socket and passes it off to the
	 * handler.
	 */
	@Override
	public void run() {
		try {
			setConnected(true);
			
			while (!disconnect){
				if (!socket.isClosed()){// && input.available() > 0) {
					Packet msg = (Packet)input.readObject();
					receiveMsg(msg);
				} else {
					try { Thread.sleep(100); } 
					catch (InterruptedException ex) {}
				}
			}
		} catch (ClassNotFoundException ex) {
			JavaChat.println("Incorrect object from socket: " + ex.getMessage());
		} catch (java.net.ConnectException e){
			JavaChat.println("Connection exception: " + e.getMessage());
		} catch(java.net.SocketException e){
			JavaChat.println("Socket Exception: " + e.getMessage());
		} catch(java.io.IOException e){
			JavaChat.println("IO Exception: " + e.getMessage());
		} finally {
			cleanup();
		}
	}

	public void sendMsg(String msg){
		Packet cmd = new Packet(PacketType.MSG,new String[] {msg} );
		sendData(cmd);
	}
	
	public void sendMsg(Packet msg){
		sendData(msg);
	}
	
	private void sendQuit(){
		sendData(Packet.createQuitPacket());
	}
	
	private void sendData(Packet data){
		try {
			output.writeObject(data);
		} catch (IOException ex) {
			JavaChat.println("Error writing to socket: " + ex.getMessage());
		}
	}
	
	/**
	 * @return the connected
	 */
	public boolean isConnected() {
		return connected;
	}

	/**
	 * @param connected the connected to set
	 */
	public void setConnected(boolean connected) {
		this.connected = connected;
	}
	
	/**
	 * @return true if we are connected but in the process of disconnecting
	 *				or are actually disconnected
	 */
	public boolean isDisconnecting(){
		return disconnect;
	}

	/**
	 * @param disconnect the disconnect to set
	 */
	public void disconnect() {
		this.disconnect = true;
		sendQuit();
	}
	
	/**
	 * Each line that is received from the socket is sent to this method for
	 * handling
	 * 
	 * @param sktCtrl The Socket controller that received the message
	 * @param msg String received from the socket
	 */
	public abstract void receiveMsg(Packet msg);
	
	/**
	 * When a socket is closed the call back is used to alert the creating class.
	 * 
	 * @param sktCtrl The Socket controller that disconnected
	 */
	public abstract void disconnected();
}
