 package org.vt.networking.common;

/**
 * This is the transport layer class which runs under application layer
 * It implement how to transfer the information from host to host.
 * 
 * @author  Wei   Wang      - tskatom@vt.edu
 * 			Sunil Kamalakar - sunilk@vt.edu
 *
 */
import java.net.DatagramPacket;
import java.net.InetAddress;

import org.vt.networking.interfaces.ILayer;

public class UnreliableTransportLayer implements ILayer {
	

	//Inputs from the command line argument.
	private String name;
	private int port;
	
	private UDPWrapper udpWrapper;
	
	public UnreliableTransportLayer(String remoteServername, int remoteServerPort) throws Exception {
		
		this.name = remoteServername;
		this.port = remoteServerPort;
		udpWrapper = new UDPWrapper();
	}
	
	public UnreliableTransportLayer(int localServerPort) throws Exception
	{
		this.port = localServerPort;
		udpWrapper = new UDPWrapper(port);
	}
    
	@Override
	public void sendTo(byte[] byteArray) throws Exception {
				
		int segmentNums = (int)Math.ceil((double)byteArray.length/DataConfig.UDP_MAX_SIZE);
		int identityId = DataConfig.getNextIdentityId();
		for(int i=0; i<segmentNums; i++)
		{
			int temDataSize = DataConfig.UDP_MAX_SIZE <= (byteArray.length-i*DataConfig.UDP_MAX_SIZE)?
										DataConfig.UDP_MAX_SIZE:byteArray.length-i*DataConfig.UDP_MAX_SIZE;
			
			byte[] temData = new byte[temDataSize];
			System.arraycopy(byteArray, i * DataConfig.UDP_MAX_SIZE, temData, 0, temDataSize);
			char ifFlag;
			if(i == segmentNums-1)
			{
				ifFlag = DataConfig.IS_LAST;
			}
			else
			{
				ifFlag = DataConfig.NOT_LAST;
			}
			char segmentType = DataConfig.SENT_DATA;
			DataSegment tempSegment = new DataSegment(identityId,i,ifFlag,segmentType,temDataSize,temData);

			sendSegment(tempSegment);
			waitACKResponse();
		}
		
	}
	
	public DataSegment waitACKResponse() throws Exception
	{
		return receiveSegment();
	}
	
	public byte[] receiveFrom() throws Exception {	
		 
		 byte[] message = new byte[DataConfig.MESSAGE_MAX_SIZE + 6];
			int dataSize = 0;
			int identityId = -1;
			int currentIndex = 0;
			@SuppressWarnings("unused")
			int sequenceNum = 0;
			char lastFlag;
			do
			{
				DataSegment ds = receiveSegment();
				identityId = ds.getIdentityId();
				sequenceNum = ds.getSeqnum();
				dataSize = ds.getDataSize();
				System.arraycopy(ds.getData(), 0, message, currentIndex, dataSize);
				currentIndex += dataSize;
				lastFlag = ds.getIfLast();
				if(lastFlag == DataConfig.IS_LAST)
				{
					return message;
				}
			} while(true);
		 
	}

	private void sendSegment(DataSegment segment) throws Exception
	{
		byte[] sendData = segment.getBytes();
		udpWrapper.sendTo(sendData, sendData.length, name, port);

	}
	
	private boolean sendACkSegment(DataSegment segment, InetAddress ia, int port) throws Exception
	{
		byte[] sentData = segment.getBytes();
		udpWrapper.sendTo(sentData, sentData.length, ia.getHostName(), port);
		return true;
	}
	
	public void sendACKResponse(int identityId,int sequenceNum,char ifLast, InetAddress ia, int port) throws Exception
	{
		char segmentType = DataConfig.RESPONSE_DATA;
		int dataSize = 1;
		byte[] data = new byte[1];
		DataSegment responseSegment = new DataSegment(identityId,sequenceNum,ifLast,segmentType,dataSize,data);
		sendACkSegment(responseSegment,ia,port);
	}
	
	private DataSegment receiveSegment() throws Exception
	{
		DatagramPacket receivedPacket =  udpWrapper.receiveFrom();
		
		byte[] receviedBytes = receivedPacket.getData();
		
		DataSegment receivedDS = new DataSegment(receviedBytes);
		
		char segmentType = receivedDS.getSegmentType();
		if(segmentType==DataConfig.SENT_DATA)
		{
			int identityId = receivedDS.getIdentityId();
			int sequenceNum = receivedDS.getSeqnum();
			char ifLast = receivedDS.getIfLast();
			InetAddress ia = receivedPacket.getAddress();
			int port = receivedPacket.getPort();
			sendACKResponse(identityId,sequenceNum,ifLast,ia,port);
		}
		else if(segmentType == DataConfig.RESPONSE_DATA)
		{
			// do nothing
		}
		
		return receivedDS;
	}
	
	public String[] getClientInfo()
	{
		//TODO we should implement in project2 
		String[] clientInfo = new String[2];
		clientInfo[0] = "address";
		clientInfo[1] = "port";
		return clientInfo;
	}
	
}
