package org.vt.networking.common;

import java.util.TimerTask;

/**
 * 
 * @author Wei Wang, Sunil
 * Timer class used to schedule and reschedule the timerTask
 * to resent segments
 */

public class Timer  {
	private ResentSegmentTimerTask timerTask;
	private java.util.Timer timer = new java.util.Timer();
	private int i = 0;
	private ReliableTransportLayer reliableTransportLayer;
	
	public Timer(ReliableTransportLayer rtl)
	{
		this.reliableTransportLayer = rtl;
	}
	public void start(long timeOut)
	{
		timerTask = new ResentSegmentTimerTask(reliableTransportLayer);
		timer.schedule(timerTask, timeOut,timeOut);
	}
	
	public void reStart(long timeOut)
	{
		timerTask.cancel();
		start(timeOut);
	}
	
	public synchronized void execute(long timeOut)
	{
		if(timerTask!=null)
		{
			timerTask.cancel();
		}
		timerTask = new ResentSegmentTimerTask(reliableTransportLayer);
		timer.schedule(timerTask,timeOut,timeOut);
	}
	
	public void cancel()
	{
		timer.cancel();
	}
	
}
