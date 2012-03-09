package org.vt.networking.common;

import java.net.DatagramPacket;


public class AckListener implements Runnable{
	private ReliableTransportLayer reliableTransportLayer;
	private int currentCheckSeq=0;
	private int currentCheckTimes=0;   
	
	public AckListener(ReliableTransportLayer reliableTransportLayer)
	{
		this.reliableTransportLayer = reliableTransportLayer;
	}
	
	
	public void fastRetransmit()
	{
		//if acklistener has received the lastHasAckSeq triple times,
		//then retrasnmit current unack segments at once before timeout
		System.out.println("Tripple ACK Happen");
		reliableTransportLayer.resentTimeoutDatasegment();
	}
	
	public void processAck() throws Exception
	{
		while(true)
		{
			DataSegment ack = reliableTransportLayer.receiveSegment();
			int seq = ack.getSeqnum();
			SlidingWindow.getInstance().removeSegment(seq);
			if(checkIfTripleAck(seq))
			{
				fastRetransmit();
			}
		}
	}
	
	public boolean checkIfTripleAck(int seq)
	{
		currentCheckSeq = SlidingWindow.getInstance().getLastAckSeq();
		if(seq==currentCheckSeq)
		{
			currentCheckTimes++;
		}
		else 
		{
			currentCheckTimes=0;
		}
		if(currentCheckTimes==3)
		{
			return true;
		}
		return false;
	}
	
	
	@Override
	public void run() {
		// TODO Auto-generated method stub
		while(true)
		{
			try {
					processAck();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

}
