package org.vt.networking.common;

public class Timer implements Runnable{
	private long timeOut = 10;
	private long currentTimeStamp = 0;
	private long beginTimeStamp = 0;
	private boolean timeReset = false;
	private static long lastSegmentSendTimeStamp = 0;
	private ReliableTransportLayer rtl;
	
	
	public Timer(ReliableTransportLayer reliableTransprotLayer)
	{
		rtl =  reliableTransprotLayer;
	}
	
	
	public void resetTimer()
	{
		timeReset = true;
	}
	
	public void close()
	{
		this.close();
	}
	
	public void resendUnAckSegments()
	{
		
			System.out.println("Time Out Happen");
			rtl.resentTimeoutDatasegment();
		
	}
	
	public void process()
	{
		while(true)
		{
			beginTimeStamp = System.currentTimeMillis();
			int timeRemain = (int) (timeOut - (System.currentTimeMillis() - beginTimeStamp)); 
			timeReset = false;
			for(int i= timeRemain; i>=0; i--)
			{
				if(timeReset)
				{
					System.out.println("timer----Reset");
					break;
				}
				try {
						Thread.sleep(1);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			
			if(!timeReset)
			{
				resendUnAckSegments();
			}
		}
	}
	
	
	
	@Override
	public void run() {
		// TODO Auto-generated method stub
//		process();
	}

}
