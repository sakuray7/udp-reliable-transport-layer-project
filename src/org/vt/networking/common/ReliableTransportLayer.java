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
import java.net.SocketTimeoutException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Random;

import org.vt.networking.interfaces.ILayer;
import org.vt.networking.protocol.FileTransferProtocol;

public class ReliableTransportLayer implements ILayer {
	
	//The Maximum window size.
	public static final int MAX_ACK_RECEIVE_SIZE = 16;
	
	//The timeout value for retransmission.
	public static final int RETRANSMISSION_TIMEOUT = 50;
	
	public static final int MAX_RETRANSMISSION_ATTEMPTS = 100;
	
	private static long numOfSegments = 0;
	
	private static int nthRetransmission = 1;
	
	//The map to hold the unacknowledged messages.
	//This is a map of the sequence numbers and the data segment
	//associated with the sequence number
	private Map<Number , DataSegment> unAckSegments;
	
	public static final int DEFAULT_RESET_SEQ_NUM = -1;
	
	public static int currentSeqNum = DEFAULT_RESET_SEQ_NUM;

	//Keep count of the acknowledges to drop.
	private static int randCount = 0;
	
	//Inputs from the command line argument.
	private String name;
	private int port;
	
	//The current packets, address and port information.
	//TODO:THis design wont work for multiple clients.
	private InetAddress inetAddress;
	private int portNumber;
	
	private UDPWrapper udpWrapper;
	private Thread ackLisThread;
	private Timer timer;
	private ResentSegmentTimerTask rsst;
	
	private ServerSideWindow serverSideWindow; 
	private int ackSentFlagNum = 0;
	
	
	public ReliableTransportLayer(String remoteServername, int remoteServerPort) throws Exception {
		
		this.name = remoteServername;
		this.port = remoteServerPort;
		udpWrapper = new UDPWrapper();
		
		initDataStructures();
		ackLisThread = new Thread(new AckListener(this));
		ackLisThread.start();
		timer =  new Timer(this);
		
	}
	
	public ReliableTransportLayer(int localServerPort) throws Exception
	{
		this.port = localServerPort;
		udpWrapper = new UDPWrapper(port);
		
		initDataStructures();
	}
	
	private void initDataStructures() {
		
		unAckSegments = new LinkedHashMap<Number, DataSegment>();
		serverSideWindow = new ServerSideWindow();
	}
    
	@Override
	public void sendTo(byte[] byteArray) throws Exception {
				
		int segmentNums = (int)Math.ceil((double)byteArray.length/DataConfig.UDP_MAX_SIZE);
		int identityId = DataConfig.getNextIdentityId();
		
		
		//If it is a control message, then they require an acknowledgement.
		DataMessage dm = new DataMessage(byteArray);
		if(ByteBuffer.wrap(dm.getMessageType()).getChar() == DataConfig.COMMAND_FLAG)
		{
			String command = new String(dm.getMessage());
			
			if(command.equals(FileTransferProtocol.CONTROL_COMMAND_CREATE)) {
				DataSegment tempSegment = new DataSegment(identityId, DataConfig.getNextSeqnum(), DataConfig.IS_LAST, DataConfig.SENT_DATA , 
																byteArray.length, byteArray);
				
				SlidingWindow.getInstance().putSegmentIntoMap(0, tempSegment);
				sendFreshSegment(tempSegment);
			}
			else if(command.equals(FileTransferProtocol.CONTROL_COMMAND_END)) {
				DataSegment tempSegment = new DataSegment(identityId, DataConfig.getNextSeqnum(), DataConfig.IS_LAST, DataConfig.SENT_DATA , 
															byteArray.length, byteArray);
				SlidingWindow.getInstance().putSegmentIntoMap(0, tempSegment);
				sendFreshSegment(tempSegment);
			}
		}
		else {
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
				DataSegment tempSegment = new DataSegment(identityId,DataConfig.getNextSeqnum(),ifFlag,segmentType,temDataSize,temData);
				
				sendFreshSegment(tempSegment);			
			}
		}
		System.out.println("Segments sent:" + numOfSegments);
	}

	
		
	public byte[] receiveFrom() throws Exception {	
		 
		    Map<Number, DataSegment> messageMap = new LinkedHashMap<Number, DataSegment>();
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
//				System.out.println("-----" + "\tSeqNum: " + ds.getSeqnum() + "\tDS Length: " + ds.getData().length + "-----");			
								
				boolean isHasMessage = processCurrentSegment(ds);
				
				if(isHasMessage)
				{
					return serverSideWindow.getMessageBytebuffer().array();
				}
				

			} while(true);
		 
	}


	private boolean processCurrentSegment(DataSegment ds) throws Exception {
				
		boolean isOutOfOrder = false;
		boolean isHasMessage = false;
		int receivedSeq = ds.getSeqnum();
		ackSentFlagNum ++;
		if(ackSentFlagNum>=100000)
		{
			ackSentFlagNum = 0;
		}
		
		//TODO:Put it only if it is in order.
		//Handle out of order message.
		if(receivedSeq!=serverSideWindow.getBaseSeq())
		{
			isOutOfOrder = true;
		}
		serverSideWindow.addReceivedSegment(receivedSeq, ds);
		
		if(ackSentFlagNum%10==0 || (ds.getIfLast()==DataConfig.IS_LAST || isOutOfOrder))
		{
			int identityId = ds.getIdentityId();
			int ackSeqNum = serverSideWindow.getAckSeqNum();
			char ifLast = ds.getIfLast();
			sendACKResponse(identityId, ackSeqNum, ifLast, inetAddress, portNumber);
			if(isOutOfOrder)
			{
				System.out.println("Packet received out of order!!! Expected Sequence Number is: " + serverSideWindow.getBaseSeq() + "Received Seq is:" + receivedSeq);
			}
		}
		
		isHasMessage = serverSideWindow.getIfHasMessage();
		return isHasMessage;
		
	}

	private void sendFreshSegment(DataSegment segment) throws Exception
	{
		SlidingWindow.getInstance().addSegment(segment.getSeqnum(), segment);
		sendSegment(segment);
	}
	
	private void sendSegment(DataSegment segment) throws Exception
	{
		byte[] sendData = segment.getBytes();
		udpWrapper.sendTo(sendData, sendData.length, name, port);
		rsst =  new ResentSegmentTimerTask(this);
		timer.execute(DataConfig.TIMEOUT);
		numOfSegments++;
	}
	
	private boolean sendACkSegment(DataSegment segment, InetAddress ia, int port) throws Exception
	{
		byte[] sentData = segment.getBytes();
		udpWrapper.sendTo(sentData, sentData.length, ia, port);
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
	
	public DataSegment receiveSegment() throws Exception
	{
		DatagramPacket receivedPacket =  udpWrapper.receiveFrom();
		
		byte[] receviedBytes = receivedPacket.getData();
		
		DataSegment receivedDS = new DataSegment(receviedBytes);
		
		//Set the value of address and port for later use.
		inetAddress = receivedPacket.getAddress();
		portNumber = receivedPacket.getPort();
			
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
	
	@SuppressWarnings("deprecation")
	public void close()
	{
		if(this.ackLisThread.isAlive())
		{
			this.ackLisThread.stop();
		}
		
		timer.cancel();
	}
	
	public  void resentTimeoutDatasegment()
	{
		
			Map<Integer,DataSegment> unAckSegments = SlidingWindow.getInstance().getUnackSegments();
			 
			int currentBaseNum = SlidingWindow.getInstance().getBaseNum();
			int currentNextSeq = SlidingWindow.getInstance().getNextSeqNum();
			int currentUnAckNum = (currentNextSeq+DataConfig.MAX_SEQ_SIZE-currentBaseNum)%DataConfig.MAX_SEQ_SIZE;
			for(int i=0;i<currentUnAckNum;i++)
			{
				try {
						DataSegment ds = unAckSegments.get((currentBaseNum+i)%DataConfig.MAX_SEQ_SIZE);
						if(ds != null)
						{
							sendSegment(ds);
							System.out.println("ResentSEQ= " +  (currentBaseNum+i)%DataConfig.MAX_SEQ_SIZE);
						}
						
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}

	}
	
	
}
