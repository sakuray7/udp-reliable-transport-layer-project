package org.vt.networking.common;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

public class SlidingWindow {

	
	//The Maximum window size.
	public static final int WINDOW_SIZE = DataConfig.WINDOW_SIZE;
	private static SlidingWindow slidingWindow;
	private int baseNum = 0;
	private int nextSeqNum = 0;
	private int maxSeqNum = DataConfig.MAX_SEQ_SIZE;
	private int lastAckSeq = -1;
	private Object lockObject; 
	
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
		lockObject = new Object();
	}
	
	
	public  synchronized Map<Integer,DataSegment> getWinowInstance()
	{
		if(window == null)
		{
			window = Collections.synchronizedMap(new LinkedHashMap<Integer,DataSegment>());
		}
		return window;
	}
	
	public synchronized Map<Integer,DataSegment> getUnackSegments()
	{
		return window;
	}
		

	
	//put segment into window
	public  void putSegmentIntoMap(int seqNum, DataSegment ds)
	{
		synchronized(window)
		{
			window.put(seqNum, ds);
		}
	}
	
	
	//modified RemoveElement from Window
	public void removeSegment(int seqNum)
	{
		int currentUnAckNum = (this.nextSeqNum+this.maxSeqNum-this.baseNum)%this.maxSeqNum;
		if(this.baseNum+currentUnAckNum>this.maxSeqNum)
		{
			if(seqNum<this.baseNum&&seqNum>=(this.baseNum+currentUnAckNum)%this.maxSeqNum)
			{
				//the seqNum is not in current unAck scope just discard it
				//do nothing
			}
			else
			{
				for(int i=0; i<currentUnAckNum;i++)
				{
					window.remove(this.baseNum+i);
					if((this.baseNum+i)%this.maxSeqNum==seqNum)
					{
						break;
					}
				}
				this.baseNum = (seqNum+1)%this.maxSeqNum;
				this.lastAckSeq = (this.baseNum+this.maxSeqNum-1)%this.maxSeqNum;
				windowWakeup();
			}
			
		}
		else
		{
			if(seqNum>=this.baseNum&&seqNum<=(this.baseNum+currentUnAckNum-1))
			{
				for(int i=this.baseNum;i<=seqNum;i++)
				{
					window.remove(i);
				}
				this.baseNum = (seqNum+1)%this.maxSeqNum;
				this.lastAckSeq = (this.baseNum+this.maxSeqNum-1)%this.maxSeqNum;
				windowWakeup();
			}
			else
			{
				//the seqNum is not in current unAck scope just discard it
				//do nothing
			}
		}
		
	}

	//modified addElement into window
	public void addSegment(int seqNum, DataSegment inDataSegment)
	{
		if(checkWindowFull())
		{
			windowWait();
		}
		window.put(seqNum, inDataSegment);
		this.nextSeqNum = (seqNum + 1)%this.maxSeqNum;
	}
	
	//lock the lockObject to wait
	public void windowWait()
	{
		synchronized(lockObject)
		{
			try {
					System.out.println("Go into Waiting!");
					this.lockObject.wait();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	//wake up the locked object
	public  void windowWakeup()
	{
		synchronized(lockObject)
		{
			lockObject.notify();
		}
	}
	//check window is full
	public  boolean checkWindowFull()
	{
		int currentUnAckNum = (this.nextSeqNum+this.maxSeqNum-this.baseNum)%this.maxSeqNum;
		if(currentUnAckNum>=this.WINDOW_SIZE)
		{
			return true;
		}
		else
		{
			return false;
		}
	}
	
	public int getBaseNum() {
		return baseNum;
	}

	public void setBaseNum(int baseNum) {
		this.baseNum = baseNum;
	}

	public int getNextSeqNum() {
		return nextSeqNum;
	}

	public int getLastAckSeq() {
		return lastAckSeq;
	}
	
}
