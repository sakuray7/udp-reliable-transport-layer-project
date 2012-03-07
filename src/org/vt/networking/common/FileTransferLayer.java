package org.vt.networking.common;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

import org.vt.networking.interfaces.ILayer;
import org.vt.networking.protocol.FileTransferProtocol;
import org.vt.networking.protocol.FileTransferProtocol.FTPAction;
import org.vt.networking.server.dl_server;

/**
 * This is the Application Layer class which how to communication in the level of application,
 * which is directly interactive with user
 * THe FileTransferLayer implement file create, file Write, command executing, transfer speed calculation 
 * and so on
 * @author  Wei   Wang      - tskatom@vt.edu
 * 			Sunil Kamalakar - sunilk@vt.edu
 *
 */

public class FileTransferLayer implements ILayer {
	
	private static long numOfMessages = 0;
	
	//Identifier for the server or the client.
	public static final String SERVER = "Server";
	public static final String CLIENT = "Client";
	
	//private String name;
	//private String port;
	private String isServer;
	
	private String filename;
	
	private File file;
	private FileOutputStream fileOutputStream;
	private long fileTransferStartTime;
	private long fileTransferEndTime;
	private long fileSize;
	
	//Keep a reference to the underlying layer.
	ReliableTransportLayer transportLayer;
	
	//The protocol to use.
	FileTransferProtocol fileTransferProtocol;
		
	public FileTransferLayer(String servername, String serverPort, 
								String isServer, String filename) throws Exception {
		
		super();
		//this.name = servername;
		//this.port = serverPort;
		this.isServer = isServer;
		this.filename = filename;
		
		if(isServer.equals(SERVER)) {
			transportLayer = new ReliableTransportLayer(Integer.parseInt(serverPort));			
		}
		else { //is client
			//Check if the file exists.
			if(!ifFileExists(filename)) {
				throw new Exception("The file does not exist!!!");
			}
			transportLayer = new ReliableTransportLayer(servername, Integer.parseInt(serverPort));			 
		}
	
		fileTransferProtocol = new FileTransferProtocol();
	}
	
	public void setFilename(String filename) {
		this.filename = filename;
	}

	@Override
	public void sendTo(byte[] obj) throws Exception {
		
		if(isServer.equals(SERVER)) {			
		
		}
		else {
			//Take client action.
			
			File file = new File(filename);
			FileInputStream fi = new FileInputStream(file);
			
			DataMessage command = fileTransferProtocol.compose(FileTransferProtocol.CONTROL_COMMAND_CREATE);
			transportLayer.sendTo(command.getBytes());
			
			System.out.println("Start File Transfer!");
			
			@SuppressWarnings("unused")
			long j = 0;
			byte[] fileBytes = new byte[DataConfig.MESSAGE_MAX_SIZE];
			int byteLengthPerRead = 0;
			
			while((byteLengthPerRead=fi.read(fileBytes))!=-1)
			{
				byte[] actualBytes = ByteBuffer.allocate(byteLengthPerRead).put(fileBytes,0,byteLengthPerRead).array();
				DataMessage fileBody = fileTransferProtocol.compose(actualBytes);
				transportLayer.sendTo(fileBody.getBytes());
				fileBytes = new byte[DataConfig.MESSAGE_MAX_SIZE];
				j++;
				//Thread.sleep(5);
			}
			
			command = fileTransferProtocol.compose(FileTransferProtocol.CONTROL_COMMAND_END);
			transportLayer.sendTo(command.getBytes());
			System.out.println("End File Transfer!");
			
			//close connection
			transportLayer.close();
		}
	}

	@Override
	public byte[] receiveFrom() throws Exception {
		
		byte[] retVal;
		
		if(isServer.equals(SERVER)) {			
			byte[] byteArray = transportLayer.receiveFrom();
			takeAction(fileTransferProtocol.processInput(byteArray), fileTransferProtocol.decompose(byteArray));
			retVal = byteArray;
		}
		else {
			//Take client action.
			retVal = null;
		}
		
		return retVal;
	}
		
	public void takeAction(FTPAction action, DataMessage dataMessage) throws IOException
	{
		
		switch (action) {
		
			case CREATE: {
				createFile(dl_server.OUTPUT_FILE_NAME);
				fileTransferStartTime = System.currentTimeMillis();
				ReliableTransportLayer.currentSeqNum = -1;
				break;
			}	
			
			case END: {
				System.out.println("\n\nFile Transfer Over!");
				fileOutputStream.flush();
				fileOutputStream.close();
				fileTransferEndTime = System.currentTimeMillis();
				setFileSize();
				System.out.println("Throughput = " + getTransferSPeed()/1000 + "Kbps");
				ReliableTransportLayer.currentSeqNum = -1;
				
				System.exit(1);
			}
			
			case WRITE: {
				
				if(fileOutputStream == null) {
					System.out.println("The file has not yet been created. create it now again.");
					createFile(dl_server.OUTPUT_FILE_NAME);
				}
				
				System.out.println("Message received: " + ++numOfMessages + "\tMessage length: " + dataMessage.getMessage().length);
				fileOutputStream.write(dataMessage.getMessage());
				break;
			}
			
			default: {
				System.out.println("Sorry, Unknown type of message in FileTransferLayer!!!");
				break;
			}
		}
	}
	
	private void createFile(String fileName) throws IOException
	{
		file = new File(fileName);
		fileOutputStream = new FileOutputStream(file);
	}
	
	private boolean ifFileExists(String filename) {
		 File file=new File(filename);
		 return file.exists() && !file.isDirectory();
	}
	
	public void setFileSize()
	{
		fileSize = file.length()*8;
	}
	
	public double getTransferSPeed()
	{
		float timeElapsed = fileTransferEndTime-fileTransferStartTime;
		System.out.println("timeElapsed=" +timeElapsed + " ms");
		System.out.println("fileSize=" + fileSize + " bits");
		return fileSize/(timeElapsed/(1000));
	}
	
}
