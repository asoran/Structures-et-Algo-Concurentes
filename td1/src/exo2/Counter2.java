package exo2;

import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicInteger;

public class Counter2 {
	private final AtomicInteger counter = new AtomicInteger(0);

	public int nextInt() {
		return counter.getAndIncrement();
	}

	public static void main(String[] args) throws InterruptedException {
		final int TEST_NUMBER = 100_000;
		final int NB_OF_THREADS = 2;
		Counter2 counter = new Counter2();

		var threads = new ArrayList<Thread>();
		for(var i = 0; i < NB_OF_THREADS; ++i) {
			threads.add(new Thread(() -> {
				for(var j = 0; j < TEST_NUMBER; ++j) {
					counter.nextInt();
				}
			}));
		}

		threads.forEach(Thread::start);
		for(var thread : threads)
			thread.join();

		System.out.println("Value should be " + (TEST_NUMBER * NB_OF_THREADS) + ", but is : " + counter.nextInt());
	}
}
