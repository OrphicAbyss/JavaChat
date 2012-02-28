package javachat.network;

import java.io.Serializable;

/**
 * Represents a message sent between the server and client in either direction.
 * 
 * A packet can either be a command packet or a message packet.
 * 
 * @author DrLabman
 */
public class Packet implements Serializable {
	private PacketType type;
	private String data[];
	
	public Packet(PacketType type, String data[]){
		this.type = type;
		this.data = data;
	}
	
	public PacketType getType(){
		return type;
	}
	
	public String[] getData(){
		return data;
	}
}
