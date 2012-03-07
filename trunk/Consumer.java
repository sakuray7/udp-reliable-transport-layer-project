
import java.util.concurrent.ArrayBlockingQueue;


public class Consumer implements Runnable {
	
	private ArrayBlockingQueue<Character> sharedItems;
	private boolean hasCharacter =  true;
	
	public Consumer(ArrayBlockingQueue<Character> sharedItems) {
		super();
		this.sharedItems = sharedItems;
	}

	public boolean consume() throws InterruptedException
	{
		
		if(sharedItems.isEmpty())
		{
			System.out.println("All the Characters have been comsumed!");
			return false;
		}
		else
		{
			Character character = sharedItems.poll();
			sharedItems.clear();
			System.out.println("\tConsumer: Receiveing item - " + character);
			System.out.println("\tConsumer: sharedItems.isEmpty() - " + sharedItems.isEmpty());
			return true;
		}
	}
	

	@Override
	public void run() 
	{
		try {
				do
				{
					synchronized (sharedItems) 
					{
						hasCharacter = consume();
						if(!hasCharacter)
						{
							break;
						}
						sharedItems.wait();
					}
				}
				while(true);
			}
		catch (Exception e) {
			//Thread.interrupted();
			e.printStackTrace();
		}
		System.out.println("Consumer finished consuming items");
	}
			
		
}

