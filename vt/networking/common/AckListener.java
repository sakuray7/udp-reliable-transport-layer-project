package org.vt.networking.common;

import java.net.DatagramPacket;


public class AckListener implements Runnable{
	private UDPWrapper udpWrapper;
	public AckListener(UDPWrapper udpWrapper)
	{
		this.udpWrapper = udpWrapper;
	}
	
	public DataSegment waitACKResponse() throws Exception
	{	
		DatagramPacket dp = udpWrapper.receiveFrom();
		byte[] receviedBytes = dp.getData();
		DataSegment receivedDS = new DataSegment(receviedBytes);
		return receivedDS;
	}
	
	public void processAck() throws Exception
	{
		while(true)
		{
			DataSegment ack = waitACKResponse();
			int seq = ack.getSeqnum();
			SlidingWindow.getInstance().removeAckFromWindow(seq);
//			char ifLast = ack.getIfLast();
//			if(ifLast == '1')
//			{
//				synchronized(ReliableTransportLayer.blockFlag)
//				{
//					ReliableTransportLayer.blockFlag.notify();
//				}
//			}
		}
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
