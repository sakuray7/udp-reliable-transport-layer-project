package org.vt.networking.protocol;

import java.io.IOException;
import java.nio.ByteBuffer;

import org.vt.networking.common.DataConfig;
import org.vt.networking.common.DataMessage;

/**
 * This is the FileTransferProtocol class which define the Data format and action
 * in application Layer
 * 
 * @author  Wei   Wang      - tskatom@vt.edu
 * 			Sunil Kamalakar - sunilk@vt.edu
 *
 */

public class FileTransferProtocol {
	
	public static final String CONTROL_COMMAND_CREATE = "create";
	public static final String CONTROL_COMMAND_END = "end";
	
	//The action to be take for the processInput
	public static enum FTPAction {
		CREATE, WRITE, END, UNKNOWN; 
	}
		
	public DataMessage compose(byte[] inMessage,int inMessageSize,char inMessageType)
	{
		return new DataMessage(inMessage,inMessageSize,inMessageType);
	}
	
	public DataMessage compose(String command)
	{
		DataMessage commandData = new DataMessage(command.getBytes(),command.getBytes().length,DataConfig.COMMAND_FLAG);
		return commandData;
	}
	
	public DataMessage compose(byte[] body)
	{
		DataMessage bodyData = new DataMessage(body,body.length,DataConfig.DATA_FLAG);
		return bodyData;
	}
	
	public DataMessage decompose(byte[] inDataMessage)
	{
		return new DataMessage(inDataMessage);
	}	
	
	/**
	 * process the input bytes Received from Transport Layer
	 * @param inBytes
	 * @throws IOException
	 */
	public FTPAction processInput(byte[] inBytes)
	{
		DataMessage dm = new DataMessage(inBytes);
		if( ByteBuffer.wrap(dm.getMessageType()).getChar() == DataConfig.COMMAND_FLAG)
		{
			String command = new String(dm.getMessage());
			if(command.equals(CONTROL_COMMAND_CREATE)) {
				return FTPAction.CREATE;
			}
			else if(command.equals(CONTROL_COMMAND_END)) {
				return FTPAction.END;
			}
			else {
				return FTPAction.UNKNOWN;
			}
		} 
		else if( ByteBuffer.wrap(dm.getMessageType()).getChar() == DataConfig.DATA_FLAG)
		{
			return FTPAction.WRITE;
		}
		else {
			return FTPAction.UNKNOWN;
		}
	}
	
}
