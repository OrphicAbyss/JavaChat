package javachat.network.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.URL;
import java.net.UnknownHostException;
import javachat.JavaChat;

/**
 *
 * @author DrLabman
 */
public class IPUtil {
	public static String getInternalIPAddress(){
		try {
			InetAddress addr = InetAddress.getLocalHost();
			// Get IP Address
			return addr.getHostAddress();
			//String hostname = addr.getHostName();
		} catch (UnknownHostException ex) {
		}
		return null;
	}
	
	public static void printInternalIP(){
		// Get IP Address
		String ipAddr = IPUtil.getInternalIPAddress();
		//String hostname = addr.getHostName();
		JavaChat.println("Internal (local) clients should use the address: " + ipAddr);
	}
	
	public static String getExternalIPAddress(){
		BufferedReader in = null;
		try {
			URL whatismyip = new URL("http://automation.whatismyip.com/n09230945.asp");
			in = new BufferedReader(new InputStreamReader(whatismyip.openStream()));
			return in.readLine(); //you get the IP as a String
		} catch (IOException ex) {
		} finally {
			try { in.close(); } catch (IOException ex) {}
		}
		return null;
	}
	
	public static void printExternalIP(){
		// Get IP Address
		String ipAddr = getExternalIPAddress();
		if (ipAddr != null){
			JavaChat.println("External (internet) clients should use the address: " + ipAddr);
		} else {
			JavaChat.println("Exception while attempting to get external ip address.");
		}
	}
}
