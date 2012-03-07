package org.vt.networking.server;

import org.vt.networking.common.FileTransferLayer;
import org.vt.networking.interfaces.ILayer;

/**
 * This is the server class which runs the server application.
 * It makes use of the underlying application layer.
 * 
 * @author  Wei   Wang      - tskatom@vt.edu
 * 			Sunil Kamalakar - sunilk@vt.edu
 *
 */
public class dl_server {
	
	//The name of the output file that needs to be created on the server.
	public static String OUTPUT_FILE_NAME = "output.dat";
	
	public static void main(String[] argv)
	{
		if(argv.length != 1) {
			System.out.print("Please provide the Input in the following format\n " +
									"dl_server  port");
			return;
		}
		
		//Convert the server-port to an integer.
		Integer serverPort = Integer.parseInt(argv[0]);
		ILayer appLayer;
		
		//Check is port is given to the server is a valid port
		if(serverPort < 0 && serverPort > 65535) {
			System.out.println("The server port is not valid. Give a valid input");
			return;
		}
		
		//TODO: Support IPv4 and IPv6
		try {
			appLayer = new FileTransferLayer("localhost", argv[0], FileTransferLayer.SERVER, OUTPUT_FILE_NAME);
			
			//Always keep listening for incoming messages
			while(true)
			{
				//This is a pull of the message form the below layer.
				appLayer.receiveFrom();
			}
		} catch (Exception e) {
			System.out.println(e.toString());
			e.printStackTrace();
		}
	}
}
