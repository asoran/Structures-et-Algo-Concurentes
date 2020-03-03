package exo1;

public class BogusVolatile {
	private volatile boolean stop;

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
		return stop;
	}

	public void stop() {
		stop = true;
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
