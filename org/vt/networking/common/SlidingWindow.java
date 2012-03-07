package org.vt.networking.common;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

public class SlidingWindow {

	//The Maximum window size.
	public static final int MAX_ACK_RECEIVE_SIZE = 16;
	private static SlidingWindow slidingWindow;
	
	private  Map<Integer,DataSegment> window = getWinowInstance();
		
	public static synchronized SlidingWindow getInstance()
	{
		if(slidingWindow == null)
		{
			slidingWindow = new SlidingWindow();
		}
		return slidingWindow;
	}
	
	private  SlidingWindow()
	{
		super();
	}
	
	public  boolean checkWindowFull()
	{
		System.out.println("Current Window size: " + window.size());
		
		if(window.size() == MAX_ACK_RECEIVE_SIZE)
		{
			//System.out.println(window.size()+ ":");
			return true;
		}
		else {
			return false;
		}
	}
	
	public  synchronized Map<Integer,DataSegment> getWinowInstance()
	{
		if(window == null)
		{
			window = Collections.synchronizedMap(new LinkedHashMap<Integer,DataSegment>());
		}
		return window;
	}
	
	public  synchronized Map<Integer,DataSegment> getUnackSegments()
	{
		return window;
	}
	
	public  void windowWait()
	{
		synchronized(window)
		{
			try {
				window.wait();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	
	
	//REWRITING THE CODE
	public   void removeAckFromWindow(int ackSeqNum)
	{
		synchronized(window)
		{
			System.out.println("ACK Sequence number is:" + ackSeqNum);
			//TODO:Change, this not good code.
			for(int j=0; j < MAX_ACK_RECEIVE_SIZE; j++) {
				window.remove(ackSeqNum - j);
			}
			
			//Special case of sequence number being -1
			//In this scenario, if the previous message has been completely received, 
			//then the value is set to -1 and we can clear our cache.
			if(ackSeqNum == -1) {
				System.out.println("ACK Sequence number is -1");
				//Get hold of the last few packets sequence numbers.
				int maxSequenceNum = (int) (Math.ceil(DataConfig.MESSAGE_MAX_SIZE/DataConfig.UDP_MAX_SIZE));
				for(int j = 0; j < MAX_ACK_RECEIVE_SIZE; j++) {
					window.remove(maxSequenceNum - j);
				}
				
				if(!window.isEmpty()) {
					ArrayList<Number> unremovedKeys = new ArrayList<Number>();
					for (Number key : window.keySet()) {
						DataSegment ds = window.get(key);
						System.out.println("Key: " + key);
						if(ds.getIfLast() == DataConfig.IS_LAST) {
							unremovedKeys.add(key);
						}
					}
					for (Number key : unremovedKeys) {
						window.remove(key);
					}
				}
			}
			
			if(!checkWindowFull())
			{
				
					window.notify();
			}
		}
	
	}
	
	//put segment into window
	public  void putSegmentIntoMap(int seqNum, DataSegment ds)
	{
		synchronized(window)
		{
			window.put(seqNum, ds);
		}
	}
	
	public  void windowWakeup()
	{
		synchronized(window)
		{
			window.notify();
		}
	}
	
}
