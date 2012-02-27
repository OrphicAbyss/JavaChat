package javachat;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

/**
 *
 * @author DrLabman
 */
public class SocketController implements Runnable {
	private Socket socket;
	private SocketHandler handler;
	private boolean connected;
	private boolean disconnect;
	private PrintWriter output;
	private BufferedReader input;
	
	public SocketController(SocketHandler handler, Socket socket){
		this.socket = socket;
		this.handler = handler;
		connected = false;
		disconnect = false;
		
		new Thread(this).start();
	}
	
	@Override
	public void run() {
		try {
			setConnected(true);
			JavaChat.println("Connected!");
			
			output = new PrintWriter(socket.getOutputStream(), true);
			input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			
			while (!disconnect){
				if (!socket.isClosed() && input.ready()) {
					String msg = input.readLine();
					handler.receiveMsg(this, msg);
				} else {
					try { Thread.sleep(100); } 
					catch (InterruptedException ex) {}
				}
			}
		} catch (java.net.ConnectException e){
			JavaChat.println("Connection exception: " + e.getMessage());
		} catch(java.net.SocketException e){
			JavaChat.println("Socket Exception: " + e.getMessage());
		} catch(java.io.IOException e){
			JavaChat.println("IO Exception: " + e.getMessage());
		} finally {
			if (!socket.isOutputShutdown()){
				sendCmd("QUIT");
			}
			if (input != null){
				try { input.close(); } catch (IOException ex) {}
			}
			if (output != null)
				output.close();
			if (socket != null && !socket.isClosed()){
				try { socket.close(); } catch (IOException ex) {
					JavaChat.println("Exception closing socket: " + ex.getMessage());
				}
			}
			setConnected(false);
			handler.disconnected(this);
		}
	}

	public void sendMsg(String msg){
		sendData("MSG " + msg);
	}
	
	public void sendCmd(String cmd){
		sendData("CMD " + cmd);
	}
	
	private void sendData(String data){
		output.println(data);
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
	 * @param disconnect the disconnect to set
	 */
	public void disconnect() {
		this.disconnect = true;
	}
}
