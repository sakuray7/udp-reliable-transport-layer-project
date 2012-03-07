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
	private Thread timerThread;
	private Timer timer;
	
	public static String blockFlag = "1";
	
	
	public ReliableTransportLayer(String remoteServername, int remoteServerPort) throws Exception {
		
		this.name = remoteServername;
		this.port = remoteServerPort;
		udpWrapper = new UDPWrapper();
		
		initDataStructures();
		ackLisThread = new Thread(new AckListener(udpWrapper));
		ackLisThread.start();
		
		timer = new Timer(this);
		timerThread = new Thread(timer);
		timerThread.start();
		
		
	}
	
	public ReliableTransportLayer(int localServerPort) throws Exception
	{
		this.port = localServerPort;
		udpWrapper = new UDPWrapper(port);
		
		initDataStructures();
	}
	
	private void initDataStructures() {
		
		unAckSegments = new LinkedHashMap<Number, DataSegment>();
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
				DataSegment tempSegment = new DataSegment(identityId, 0, DataConfig.IS_LAST, DataConfig.SENT_DATA , 
																byteArray.length, byteArray);
				
				SlidingWindow.getInstance().putSegmentIntoMap(0, tempSegment);
				sendSegment(tempSegment);
//				synchronized(blockFlag)
//				{
//					blockFlag.wait();
//				}
//				sendSegmentAndWaitForACK(tempSegment);
			}
			else if(command.equals(FileTransferProtocol.CONTROL_COMMAND_END)) {
				DataSegment tempSegment = new DataSegment(identityId, 0, DataConfig.IS_LAST, DataConfig.SENT_DATA , 
															byteArray.length, byteArray);
//				unAckSegments.put(0, tempSegment);
//				sendSegmentAndWaitForACK(tempSegment);
				SlidingWindow.getInstance().putSegmentIntoMap(0, tempSegment);
				sendSegment(tempSegment);
//				synchronized(blockFlag)
//				{
//					blockFlag.wait();
//				}
			}
		}
		else {
			for(int i=0; i<segmentNums; i++)
			{
//				Thread.sleep(50);
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
//				int seqNum = DataConfig.getNextSeqnum();
				DataSegment tempSegment = new DataSegment(identityId,i,ifFlag,segmentType,temDataSize,temData);
				
//				//Add the segment to the map.
//				unAckSegments.put(seqNum, tempSegment);
				
//				boolean waitForACK = false;
//				
//				//TODO: Make sure that we do not handle Application control messages here.
//				if(i % MAX_ACK_RECEIVE_SIZE != (MAX_ACK_RECEIVE_SIZE - 1)) {
//					waitForACK = false;
//					
//					//TODO: What if the packet is the last one and not with sequence num of MAX_WINDOW_SIZE
//					//TODO: Remove this, it is just a simulation for packet drop.
//					Random randNum = new Random();
//					int randNumber = randNum.nextInt() % 25;
//					//Randomly drop packet
//					if(randNumber == 0) {
//						continue;
//					}
//					
//					sendSegment(tempSegment);				
//				}
//				else {
//					waitForACK = true;
//				}
//				if(waitForACK) {				
//					//Wait for the ACK and remove it from the map.
//					sendSegmentAndWaitForACK(tempSegment);
//				}
				
				//TODO: add for SlidingWindow.getInstance()
				System.out.println("i:" + i);
				System.out.println("Sent-Seq:" + tempSegment.getSeqnum());
				if(SlidingWindow.getInstance().checkWindowFull()) 
					{
						//if the window is full,wait for available window space
						SlidingWindow.getInstance().windowWait();
						sendSegment(tempSegment);
						SlidingWindow.getInstance().putSegmentIntoMap(i, tempSegment);
					}
					else
					{
						sendSegment(tempSegment);	
						SlidingWindow.getInstance().putSegmentIntoMap(i, tempSegment);
					}
				}
				timer.resetTimer();
			
//			synchronized(blockFlag)
//			{
//				System.out.println("I'm Waiting");
//				blockFlag.wait();
//				System.out.println("I'm awaking");
//			}
		}
		
		System.out.println("Segments sent:" + numOfSegments);
	}
	
	private void sendSegmentAndWaitForACK(DataSegment segment) throws Exception {
		sendSegment(segment);
		DataSegment ack = waitACKResponse();
		if(ack != null) {			
			int ackSeqNum = ack.getSeqnum();
			removeOnACKReceive(ackSeqNum);
		}
		
		if(unAckSegments.size() > 0) {
			sendSegmentAndWaitForACK(unAckSegments);
		}
	}
	
	private void sendSegmentAndWaitForACK(Map<Number, DataSegment> map) throws Exception {
		
		for (Number key : map.keySet()) {
			sendSegment(map.get(key));			
		}
		
		DataSegment ack = waitACKResponse();
		if(ack != null) {			
			int ackSeqNum = ack.getSeqnum();
			removeOnACKReceive(ackSeqNum);
		}
		
		if (!map.isEmpty()){
			//TODO: Change this, how many times can we do this?
			//Ideally terminate this after a few attempts like say 10.
			//Thread.sleep(RETRANSMISSION_TIMEOUT/5);
			sendSegmentAndWaitForACK(map);
		}
		
		//This could cause an infinite loop here, kill it when we have a threaded model.
		//if(!map.isEmpty()) {
		//	sendSegmentAndWaitForACK(map);
		//}
	}
	

	private void removeOnACKReceive(int ackSeqNum) {
		
		//TODO:Change, this not good code.
		for(int j=0; j < MAX_ACK_RECEIVE_SIZE; j++) {
			unAckSegments.remove(ackSeqNum - j);
		}
		
		//Special case of sequence number being -1
		//In this scenario, if the previous message has been completely received, 
		//then the value is set to -1 and we can clear our cache.
		if(ackSeqNum == -1) {
			System.out.println("ACK Sequence number is -1");
			//Get hold of the last few packets sequence numbers.
			int maxSequenceNum = (int) (Math.ceil(DataConfig.MESSAGE_MAX_SIZE/DataConfig.UDP_MAX_SIZE));
			for(int j = 0; j < MAX_ACK_RECEIVE_SIZE; j++) {
				unAckSegments.remove(maxSequenceNum - j);
			}
			
			if(!unAckSegments.isEmpty()) {
				ArrayList<Number> unremovedKeys = new ArrayList<Number>();
				for (Number key : unAckSegments.keySet()) {
					DataSegment ds = unAckSegments.get(key);
					System.out.println("Key: " + key);
					if(ds.getIfLast() == DataConfig.IS_LAST) {
						unremovedKeys.add(key);
					}
				}
				for (Number key : unremovedKeys) {
					unAckSegments.remove(key);
				}
			}
		}
	}

	public DataSegment waitACKResponse() throws Exception
	{
		DataSegment ds = null;
		try {
			udpWrapper.setSoTimeOut(nthRetransmission * RETRANSMISSION_TIMEOUT);
			ds = receiveSegment();
			if(ds != null) 
				nthRetransmission = 1;
			udpWrapper.setSoTimeOut(0);
		}
		catch (SocketTimeoutException e) {
			nthRetransmission++;
			udpWrapper.setSoTimeOut(0);
			System.out.println("Timeout occured, restransmitting packets. Number of retransmission:" + nthRetransmission);
			
			if(nthRetransmission >= MAX_RETRANSMISSION_ATTEMPTS) {
				System.out.println("Too many retransmissions attempted. Closing the client. Sorry!!!");
				System.exit(-1);
			}
			
			sendSegmentAndWaitForACK(unAckSegments);
		}
		
		return ds;
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
				System.out.println("-----" + "\tSeqNum: " + ds.getSeqnum() + "\tDS Length: " + ds.getData().length + "-----");		
				//System.arraycopy(ds.getData(), 0, message, currentIndex, dataSize);
				//currentIndex += dataSize;
				Random randNum = new Random();
				int randNumber = randNum.nextInt() % 25;
				
				//Randomly drop packet
				if(randNumber == 0) {
					System.out.println("randNumber=" + randNumber + "Drop segment: " + ds.getIdentityId() + "-" + ds.getSeqnum());
//					continue;
				}
				
								
				boolean isOutOfOrder = processCurrentSegment(messageMap, ds);
				
				
				lastFlag = ds.getIfLast();
				
				//For a command that is sent, we need to make sure that the ack is sent back here.
				if(lastFlag == DataConfig.IS_LAST && sequenceNum == 0) {
					isOutOfOrder = false;
				}
				
				if(lastFlag == DataConfig.IS_LAST && !isOutOfOrder)
				{
					byte[] message = convertMapToMessage(messageMap);
					System.out.println("IS_LAST => Seq Num: " + currentSeqNum);
					
					//If we set this to -1 and the last ack packet is lost, it could lead to an infinite loop.
					currentSeqNum = DEFAULT_RESET_SEQ_NUM;
					
					sendACKResponse(ds.getIdentityId(), currentSeqNum, lastFlag, inetAddress, portNumber);
					
					return message;
				}
			} while(true);
		 
	}

	private byte[] convertMapToMessage(Map<Number, DataSegment> map) {

		byte[] message = new byte[DataConfig.MESSAGE_MAX_SIZE + 6];
		ByteBuffer byteBuffer = ByteBuffer.wrap(message);
		
		for ( Number seqNum : map.keySet()) {
			DataSegment ds = map.get(seqNum);
			byteBuffer.put(ds.getData());
		}
		
		map = null;
		//System.out.println("Message to app layer: " + byteBuffer.array().length);
		return byteBuffer.array();
	}

	private boolean processCurrentSegment(Map<Number, DataSegment> map, DataSegment ds) throws Exception {
				
		boolean isOutOfOrder = false;
		
		//TODO:Put it only if it is in order.
		//Handle out of order message.
		if((currentSeqNum + 1)%DataConfig.MAX_SEQ_SIZE == ds.getSeqnum()) {
			isOutOfOrder = false;
			currentSeqNum = ds.getSeqnum();
			map.put(ds.getSeqnum(), ds);
		}
		else {
			//Packet received out of order, we need to drop it and
			//ask for an retransmission.
			isOutOfOrder = true;
			System.out.println("Packet received out of order!!! Expected Sequence Number is: " + (currentSeqNum + 1));
		}
		
		char segmentType = ds.getSegmentType();
		if(segmentType==DataConfig.SENT_DATA)
		{
			int identityId = ds.getIdentityId();
			int sequenceNum = ds.getSeqnum();
			char ifLast = ds.getIfLast();
//			if( (sequenceNum % MAX_ACK_RECEIVE_SIZE == (MAX_ACK_RECEIVE_SIZE - 1))) {
			if( (sequenceNum % 15 == 0) || ds.getIfLast() =='1') {
					sendACKResponse(identityId, currentSeqNum, ifLast, inetAddress, portNumber);
				//sendACKResponse(identityId, currentSeqNum, ifLast, inetAddress, portNumber);
			}
		}
		
		return isOutOfOrder;
		
	}

	private void sendSegment(DataSegment segment) throws Exception
	{
		byte[] sendData = segment.getBytes();
		udpWrapper.sendTo(sendData, sendData.length, name, port);
		
		numOfSegments++;

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
		if(this.timerThread.isAlive())
		{
			this.timerThread.stop();
		}
	}
	
	public  void resentTimeoutDatasegment()
	{
		
			Map<Integer,DataSegment> unAckSegments = SlidingWindow.getInstance().getUnackSegments();
			synchronized(SlidingWindow.getInstance().getWinowInstance()) {
			for (Integer key : unAckSegments.keySet()) {
				try {
						sendSegment(unAckSegments.get(key));
						System.out.println("Resent Seq = " + unAckSegments.get(key).getSeqnum());
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}			
			}
			}

	}
	
	
}
