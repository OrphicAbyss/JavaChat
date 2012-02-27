package javachat;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.teleal.cling.UpnpService;
import org.teleal.cling.UpnpServiceImpl;
import org.teleal.cling.model.meta.Action;
import org.teleal.cling.model.meta.ActionArgument;
import org.teleal.cling.model.meta.Device;
import org.teleal.cling.model.meta.Service;
import org.teleal.cling.model.types.UDAServiceId;
import org.teleal.cling.registry.DefaultRegistryListener;
import org.teleal.cling.registry.Registry;
import org.teleal.cling.registry.RegistryListener;
import org.teleal.cling.support.igd.PortMappingListener;
import org.teleal.cling.support.model.PortMapping;

/**
 * Handles UPnP calls needed for the server to automatically open a port on a
 * router.
 * 
 * @author DrLabman
 */
public class UPnP {
	private static UpnpService upnpService = null;
	
	/**
	 * Start up the upnpService and register the port
	 * 
	 * @param port 
	 */
	public static void RegisterPort(int port){
		if (upnpService != null){
			ChatWindow.instance.println("UPnP service already started, will shutdown and restart.");
			UnregisterPort();
		}
		
		BufferedReader in = null;
		try {
			URL whatismyip = new URL("http://automation.whatismyip.com/n09230945.asp");
			in = new BufferedReader(new InputStreamReader(whatismyip.openStream()));
			String ip = in.readLine(); //you get the IP as a String
			ChatWindow.instance.println("External ip address is: " + ip);
		} catch (MalformedURLException ex) {
			ChatWindow.instance.println("Error getting the external ip address: " + ex.getMessage());
		} catch (IOException ex) {
			ChatWindow.instance.println("Error getting the external ip address: " + ex.getMessage());
		} finally {
			try {
				in.close();
			} catch (IOException ex) {
				Logger.getLogger(UPnP.class.getName()).log(Level.SEVERE, null, ex);
			}
		}
		
		try {
			InetAddress addr = InetAddress.getLocalHost();
			// Get IP Address
			String ipAddr = addr.getHostAddress();
			// Get hostname
			String hostname = addr.getHostName();
			
			ChatWindow.instance.println("Creating server on: " + hostname + " on ip: " + ipAddr);
			PortMapping desiredMapping = new PortMapping(port, ipAddr, PortMapping.Protocol.TCP, "JavaChat Port Mapping");
			
			upnpService = new UpnpServiceImpl( new PortMappingListener(desiredMapping));//, CreateListenerToPrintUPnPDeviceData());
			upnpService.getControlPoint().search();
		} catch (UnknownHostException e) {
			ChatWindow.instance.println("Error getting the computers hostname: " + e.getMessage());
			ChatWindow.instance.println("UPnP NAT port mapping may not be in place.");
		}
	}
	
	/**
	 * Shutdown the upnpService to unregister the port mapping.
	 */
	public static void UnregisterPort(){
		if (upnpService != null)
			upnpService.shutdown();
		
		upnpService = null;
	}
	
	/**
	 * Lists devices, services, actions, and action argumensts.
	 * 
	 * @return A listener to print out debut information.
	 */
	public static RegistryListener CreateListenerToPrintUPnPDeviceData(){
		RegistryListener Listener = new DefaultRegistryListener(){				
			@Override
			public void deviceAdded(Registry registry, Device device) {
				Service service = device.findService(new UDAServiceId("WANIPConnection"));
				if (service != null){
					ChatWindow.instance.println("Found WANIPConnection service.");
				}

				ChatWindow.instance.println("Added device: " + device.getDisplayString());
				for (Service s: device.findServices()){
					ChatWindow.instance.println("   Has Service: " + s.toString());
					for (Action a: s.getActions()){
						ChatWindow.instance.println("      Has Action: " + a.getName());
						for (ActionArgument aArgs: a.getArguments()){
							ChatWindow.instance.println("         Has Action Argument: " + aArgs.getName());
						}
					}
				}
			}
		};
		return Listener;
	}
}
