package org.vt.networking.common;

import java.nio.ByteBuffer;
/**
 * This is the DataSegment class which define the Data format communicating in the transport layer
 * 
 * @author  Wei   Wang      - tskatom@vt.edu
 * 			Sunil Kamalakar - sunilk@vt.edu
 *
 */

public class DataSegment {
	private byte[] identityId; //the DataSegment derived from the same message will have the same identityId
	private byte[] sequenceNum; //used to record the sequence of the DataSegment derived from the same message
	private byte[] ifLast; //used to record the DataSegment whether the last one or not of the message
	private byte[] segmentType; //used to record whether the DataSegment used to sent or confirm
	private byte[] dataSize; // used to record the size of data exclude header
	private byte[] data; //data body
	
	public int 	   length; //length of whole DataSegment including header and body
	private byte[] dataSegmentBytes; //
	
	public DataSegment(int identityId, int sequenceNum, char ifLast, char segmentType,int inDataSize, byte[] inData)
	{
		this.identityId = ByteBuffer.allocate(4).putInt(identityId).array();
		this.sequenceNum = ByteBuffer.allocate(4).putInt(sequenceNum).array();
		this.ifLast = ByteBuffer.allocate(2).putChar(ifLast).array();
		this.segmentType = ByteBuffer.allocate(2).putChar(segmentType).array();
		this.dataSize = ByteBuffer.allocate(4).putInt(inDataSize).array();
		this.data = ByteBuffer.allocate(inDataSize).put(inData, 0, inDataSize).array();
		this.length = this.identityId.length + this.sequenceNum.length + this.ifLast.length + this.dataSize.length + this.segmentType.length + data.length;
	}
	
	public DataSegment(byte[] inBytes)
	{
		this.identityId = ByteBuffer.allocate(4).put(inBytes,0,4).array();
		this.sequenceNum = ByteBuffer.allocate(4).put(inBytes,4,4).array();
		this.ifLast = ByteBuffer.allocate(2).put(inBytes,8,2).array();
		this.segmentType = ByteBuffer.allocate(2).put(inBytes,10,2).array();
		this.dataSize = ByteBuffer.allocate(4).put(inBytes,12,4).array();
		int dSize = ByteBuffer.allocate(4).put(inBytes,12,4).getInt(0);
		this.data = ByteBuffer.allocate(dSize).put(inBytes,16,dSize).array();
		this.length = identityId.length + sequenceNum.length + ifLast.length + dataSize.length + segmentType.length + data.length;
	}
	
	public byte[] getBytes()
	{
		
		dataSegmentBytes = new byte[length];
		//copy the identityId into dataSegment
		System.arraycopy(identityId, 0, dataSegmentBytes, 0, identityId.length);
		// copy the sequenceNum into dataSegment
		System.arraycopy(sequenceNum, 0, dataSegmentBytes, identityId.length, sequenceNum.length);
		// copy ifLast flag into dataSegment
		System.arraycopy(ifLast, 0, dataSegmentBytes,identityId.length + sequenceNum.length, ifLast.length);
		//copy segmentType into dataSegment
		System.arraycopy(segmentType, 0, dataSegmentBytes,identityId.length + sequenceNum.length + ifLast.length, segmentType.length);
		// copy datasize into dataSegment
		System.arraycopy(dataSize, 0, dataSegmentBytes, identityId.length + sequenceNum.length + ifLast.length + segmentType.length, dataSize.length);
		// copy data into dataSegment
		System.arraycopy(data, 0, dataSegmentBytes, identityId.length + sequenceNum.length + ifLast.length + segmentType.length + dataSize.length, data.length);
		
		return dataSegmentBytes;
	}
	
	public int getSeqnum()
	{
		return ByteBuffer.allocate(4).put(sequenceNum).getInt(0);
	}
	
	public int getIdentityId()
	{
		return ByteBuffer.allocate(4).put(identityId).getInt(0);
	}
	
	public char getIfLast()
	{
		return ByteBuffer.allocate(2).put(ifLast).getChar(0);
	}
	
	public char getSegmentType()
	{
		return ByteBuffer.allocate(2).put(segmentType).getChar(0);
	}
	
	public int getDataSize()
	{
		return ByteBuffer.allocate(4).put(dataSize).getInt(0);
	}
	
	public byte[] getData()
	{
		return this.data;
	}
	
	public int getLength()
	{
		return length;
	}
}
