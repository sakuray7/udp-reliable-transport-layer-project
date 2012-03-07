package org.vt.networking.common;

import java.nio.ByteBuffer;

/**
 * This is the DataMessage class which define the Data format communicated in the application layer
 * in application Layer
 * 
 * @author  Wei   Wang      - tskatom@vt.edu
 * 			Sunil Kamalakar - sunilk@vt.edu
 *
 */
public class DataMessage {
	private byte[] messageType;
	private byte[] messageSize;
	private byte[] message;
	private int length;
	private byte[] dataMessage;
	
	public DataMessage(byte[] inMessage,int inMessageSize,char inMessageType)
	{
		messageType = ByteBuffer.allocate(2).putChar(inMessageType).array();
		messageSize = ByteBuffer.allocate(4).putInt(inMessageSize).array();
		message = ByteBuffer.allocate(inMessageSize).put(inMessage, 0, inMessageSize).array();
	}
	
	public DataMessage(byte[] inDataMessage)
	{
		messageType = ByteBuffer.allocate(2).put(inDataMessage, 0, 2).array();
		messageSize = ByteBuffer.allocate(4).put(inDataMessage,2,4).array();
		int messageLen = ByteBuffer.allocate(4).put(inDataMessage,2,4).getInt(0);
		
		if(messageLen < 0 || messageLen > DataConfig.MESSAGE_MAX_SIZE + 6) {
			return;
		}
		
//		System.out.println("messageLen=" + messageLen );
		length = messageLen + messageType.length + messageSize.length;
		//System.out.println("Message Length: " + messageLen + "\nSize inDataMessage: " + inDataMessage.length);
		message = ByteBuffer.allocate(messageLen).put(inDataMessage,6,messageLen).array();
	}
	
	public byte[] getBytes()
	{
		length = messageType.length + messageSize.length + message.length;
		dataMessage = new byte[length];
		System.arraycopy(messageType, 0, dataMessage, 0, messageType.length);
		System.arraycopy(messageSize, 0, dataMessage, messageType.length, messageSize.length);
		System.arraycopy(message, 0, dataMessage, messageType.length +  messageSize.length, message.length);
		return dataMessage;
	}
	
	public byte[] getMessage()
	{
		return this.message;
	}
	
	public byte[] getMessageType()
	{
		return this.messageType;
	}
}
