package javachat;

/**
 * Main class for Java Chat
 * 
 * Unless the -server argument is given on the command line a GUI window is
 * opened.
 * 
 * @author DrLabman
 */
public class JavaChat {
	/** The singleton instance of the chat window */
	public static ChatWindow instance = null;
	private static Server server;
	private static Client client;
	
	public static void println(String text){
		instance.println(text);
	}
	
	public static void startServer(String port){
		println("Starting server on port " + port);
		try {
			int portVal = Integer.parseInt(port);
			// Start up the server
			server = new Server(portVal);
			// Make sure the server is up
			while (server.isConnected() != true){
				try {
					Thread.sleep(10);
				} catch (InterruptedException ex) {}
			}
			// Set up a client connecting to our own server for us to send and receive on
			client = new Client("localhost",portVal);
		} catch (NumberFormatException e){
			println("Port is not a number.");
		}
	}
	
	public static void startClient(String hostname, String port){
		println("Connecting to server " + hostname + ":" + port);
		try {
			int portVal = Integer.parseInt(port);
			client = new Client(hostname, portVal);
		} catch (NumberFormatException e){
			println("Port is not a number.");
		}
	}
	
	/**
	 * Close the server if it is open as well as closing all connected clients
	 * and/or close the client.
	 */
	public static void disconnect(){
		boolean disconnected = false;
		
		if (server != null && server.isConnected()){
			server.disconnect();
			disconnected = true;
		}
		
		if (client != null && client.isConnected()){
			client.disconnect();
			disconnected = true;
		}
		
		// Error if we weren't able to disconnect
		if (!disconnected){
			println("Not connected: Unable to disconnect.");
		}
	}
	
	/**
	 * Allow the GUI to get the client so that it can send messages over the
	 * network.
	 * 
	 * @return The client object
	 */
	public static Client getClient(){
		return client;
	}
	
	/**
	 * @param args the command line arguments
	 */
	public static void main(String args[]) {
		/*
		 * Set the Nimbus look and feel
		 */
		//<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /*
		 * If Nimbus (introduced in Java SE 6) is not available, stay with the
		 * default look and feel. For details see
		 * http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html
		 */
		try {
			for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
				if ("Nimbus".equals(info.getName())) {
					javax.swing.UIManager.setLookAndFeel(info.getClassName());
					break;
				}
			}
		} catch (ClassNotFoundException ex) {
			java.util.logging.Logger.getLogger(ChatWindow.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
		} catch (InstantiationException ex) {
			java.util.logging.Logger.getLogger(ChatWindow.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
		} catch (IllegalAccessException ex) {
			java.util.logging.Logger.getLogger(ChatWindow.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
		} catch (javax.swing.UnsupportedLookAndFeelException ex) {
			java.util.logging.Logger.getLogger(ChatWindow.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
		}
		//</editor-fold>

		/**
		 * Disconnect from server/clients when shutting down. Needed by the
		 * server to remove UPnP mapping.
		 */
		Runtime.getRuntime().addShutdownHook(new Thread() {
			@Override
			public void run() {
				if (instance != null){
					if (server != null && server.isConnected()){
						server.disconnect();
					}
					if (client != null && client.isConnected()){
						client.disconnect();
					}
				}
				
			}
		});
		
		/*
		 * Create and display the form
		 */
		java.awt.EventQueue.invokeLater(new Runnable() {
			@Override
			public void run() {
				instance = new ChatWindow();
				instance.setVisible(true);
			}
		});
	}
}
