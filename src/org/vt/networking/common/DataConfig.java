package org.vt.networking.common;

/**
 * This is the DataConfig class which used to store some global data and common util methods
 * in application Layer
 * 
 * @author  Wei   Wang      - tskatom@vt.edu
 * 			Sunil Kamalakar - sunilk@vt.edu
 *
 */
public class DataConfig {
	public static final long TIMEOUT = 100;
	public static final double LOSTPROBALITY = 0.001;
	public static final int WINDOW_SIZE = 15;
	public static int UDP_MAX_SIZE = 450; // maximum of the size can be sent in one segment
	public static int RECEIVED_PACKET_SIZE = 1024;
	public static int MESSAGE_MAX_SIZE = 256 * 1000 - 6;
	public static int identityId = -1;
	public static char COMMAND_FLAG = '0'; //used for message Flag
	public static char DATA_FLAG = '1'; //used for message Flag
	public static char IS_LAST = '1'; // used for dataSegment ifLast flag
	public static char NOT_LAST = '0';  // used for dataSegment ifLast flag
	public static char SENT_DATA = '0'; //used to identify message as sent
	public static char RESPONSE_DATA = '1'; //used to identify message as response
	public static int seqnum = -1;
	public static int MAX_SEQ_SIZE = 50000;
	
	public static int getNextIdentityId()
	{
		return ++identityId;
	}
	
	public static int getNextSeqnum()
	{
		return (++ seqnum)%MAX_SEQ_SIZE;
	}
	
	
}
