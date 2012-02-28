package javachat.network.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javachat.JavaChat;
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
			JavaChat.println("Warning: UPnP service already started, will shutdown and restart.");
			UnregisterPort();
		}
				
		String ipAddr = IPUtil.getInternalIPAddress();
		if (ipAddr != null){
			// Port Mapping
			PortMapping desiredMapping = new PortMapping(port, ipAddr, PortMapping.Protocol.TCP, "JavaChat Port Mapping");
			upnpService = new UpnpServiceImpl( new PortMappingListener(desiredMapping));//, CreateListenerToPrintUPnPDeviceData());
			upnpService.getControlPoint().search();
		} else {
			JavaChat.println("Error getting internal IP address.");
			JavaChat.println("Unable to setup UPnP NAT port mapping without IP address.");
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
					JavaChat.println("Found WANIPConnection service.");
				}

				JavaChat.println("Added device: " + device.getDisplayString());
				for (Service s: device.findServices()){
					JavaChat.println("   Has Service: " + s.toString());
					for (Action a: s.getActions()){
						JavaChat.println("      Has Action: " + a.getName());
						for (ActionArgument aArgs: a.getArguments()){
							JavaChat.println("         Has Action Argument: " + aArgs.getName());
						}
					}
				}
			}
		};
		return Listener;
	}
}
