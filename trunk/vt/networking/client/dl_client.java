package org.vt.networking.client;

import org.vt.networking.common.FileTransferLayer;
import org.vt.networking.interfaces.ILayer;

/**
 * This is the Client class which runs the client application.
 * It makes use of the underlying application layer.
 * 
 * @author  Wei   Wang      - tskatom@vt.edu
 * 			Sunil Kamalakar - sunilk@vt.edu
 *
 */
public class dl_client{


	public static void main(String[] argv) {
		
		if(argv.length != 3) {
			System.out.println(argv.length);
			System.out.print("Please provide the Input in the following format\n " +
									"dl_client hostname port filename");
			return;
		}
		
		//TODO:Check if file exists.
		
		//Convert the server-port to an integer.
		Integer serverPort = Integer.parseInt(argv[1]);
		
		ILayer appLayer;
		
		//Check is port is given to the server is a valid port
		if(serverPort < 0 && serverPort > 65535) {
			System.out.println("The server port is not valid. Give a valid input");
			return;
		}		
		
		try {
			appLayer = new FileTransferLayer(argv[0], argv[1], FileTransferLayer.CLIENT, argv[2]);
			
			//Send null to indicate that we just need to pick up the data from the filename which
			//is to read the file from the already passed file name.
			appLayer.sendTo(null);
			
		} catch (Exception e) {
			System.out.println(e.toString());
			e.printStackTrace();
		}		
	}
	
	
}
