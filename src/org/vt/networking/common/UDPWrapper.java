package org.vt.networking.common;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;

/**
 * The class which acts as a wrapper around UDP communication.
 *  @author Sunil Kamalakar - sunilk@vt.edu
 */
public class UDPWrapper{
		
	private DatagramSocket socket;

	public UDPWrapper() throws Exception {
		createSocket();
	}
	
	public UDPWrapper(Integer port) throws Exception {
		createSocket(port);
	}	

	private void createSocket() throws Exception {
		socket =  new DatagramSocket();
	}

	public void createSocket(Integer port) throws Exception {
		socket = new DatagramSocket(port);
	}

	public DatagramPacket receiveFrom() throws SocketTimeoutException, Exception {
		// this buffersize should be set as received_packet_size but not 
		// UDP_Max_size, because after add TransfortLaery header,
		// its' size bigger than UDP_Max_size
		byte[] buf = new byte[DataConfig.RECEIVED_PACKET_SIZE];
		DatagramPacket packet = new DatagramPacket(buf, buf.length);
		
		socket.receive(packet);
		
		return packet;
	}

	public void sendTo(byte[] sendData, int length, String name, int port) throws Exception {
		
		InetAddress address = getInetAddress(name);
		DatagramPacket packet = new DatagramPacket(sendData, sendData.length, address, port);
		socket.send(packet);
	}
	
	public void sendTo(byte[] sendData, int length, InetAddress ia, int port) throws Exception {
		
		DatagramPacket packet = new DatagramPacket(sendData, sendData.length, ia, port);
		socket.send(packet);
	}
	
	public  InetAddress getInetAddress(String hostName) throws Exception
    {
    	InetAddress iNetAddress = null;
    	try {
				for(InetAddress ia : InetAddress.getAllByName(hostName))
				{
//					System.out.println(ia.getHostAddress());
					if(ia instanceof Inet6Address)
					{
//						System.out.println("IPV6");
						iNetAddress = ia;
						return iNetAddress;
					}
					else if(ia instanceof Inet4Address)
					{
//						System.out.println("IPV4");
						iNetAddress = ia;
					}
				}
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			System.out.println("---");
			throw new Exception();
		}
    	return iNetAddress;
    } 
	
	public void setSoTimeOut(int timeout) throws SocketException {
		//Set a timeout for the socket, receive.
		//socket.setSoTimeout(ReliableTransportLayer.RETRANSMISSION_TIMEOUT);
	}
	
}
