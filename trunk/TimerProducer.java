
import java.util.TimerTask;
import java.util.concurrent.ArrayBlockingQueue;

public class TimerProducer extends TimerTask{

	private ArrayBlockingQueue<Character> sharedItems;
	private String testData = "Consumer-Producer problem!!!";
	private int i = 0;
	
	public TimerProducer(ArrayBlockingQueue<Character> sharedItems) {
		super();
		this.sharedItems = sharedItems;
	}
	
	public synchronized void produce()
	{
		Character ch = testData.charAt(i);
		sharedItems.clear();
		sharedItems.add(ch);
		System.out.println("Producer: Adding item - " + ch.toString());
		i++;
	}
	
	public boolean isEnd()
	{
		if(i == testData.length())
		{
			System.out.println("Producer completed!!");
			return true;
		}
		else 
		{
			return false;
		}
	}
	
	@Override
	public void run() 
	{
		// TODO Auto-generated method stub
		//Consider a string we want to produce.
		
		if(!isEnd())
		{
			synchronized (sharedItems) 
			{
				produce();
				sharedItems.notify();
			}
		} else
		{
			synchronized (sharedItems) 
			{
				sharedItems.notify();
			}
			cancel();
		}
	}

}
