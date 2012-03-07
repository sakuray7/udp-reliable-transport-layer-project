package org.vt.networking.interfaces;

/**
 * This is the common interface  class which should be implement by all kinds of Layers
 * 
 * 
 * @author  Wei   Wang      - tskatom@vt.edu
 * 			Sunil Kamalakar - sunilk@vt.edu
 *
 */

public interface ILayer {
	
	public void sendTo(byte[] byteArray) throws Exception;

	public byte[] receiveFrom() throws Exception;

}
