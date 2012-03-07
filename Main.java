
import java.util.Timer;
import java.util.concurrent.ArrayBlockingQueue;


public class Main {
	
	private static ArrayBlockingQueue<Character> sharedItems = new ArrayBlockingQueue<Character>(10);

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		
		Consumer consumer = new Consumer(sharedItems);
		
		TimerProducer timerProducer = new TimerProducer(sharedItems);
		Timer timer = new Timer();
		timer.schedule(timerProducer, 0, 40);
		
		Thread c1 = new Thread(consumer);
		c1.start();
		
		try {
			c1.join();
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		System.out.println("GOOOL");
		timer.cancel();
	}

}
