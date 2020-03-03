package exo1;

public class Bogus {
	private boolean stop;
	private final Object lock = new Object();

	public void runCounter() {
		var localCounter = 0;
		for (;;) {
			if (isStoped()) {
				break;
			}
			localCounter++;
		}
		System.out.println(localCounter);
	}

	public boolean isStoped() {
		synchronized(lock) {
			return stop;
		}
	}

	public void stop() {
		synchronized(lock) {
			stop = true;
		}
	}

	public static void main(String[] args) throws InterruptedException {
		var bogus = new Bogus();
		var thread = new Thread(bogus::runCounter);
		thread.start();
		Thread.sleep(100);
		bogus.stop();
		thread.join();
	}
}
