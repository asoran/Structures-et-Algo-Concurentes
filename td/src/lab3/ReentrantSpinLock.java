package lab3;

import java.lang.invoke.MethodHandles;
import java.lang.invoke.VarHandle;

public class ReentrantSpinLock {
	public static final VarHandle HANDLE;

	static {
		try {
			HANDLE = MethodHandles.lookup().findVarHandle(ReentrantSpinLock.class, "lock", int.class);
		} catch (NoSuchFieldException | IllegalAccessException e) {
			throw new AssertionError("Should not happen");
		}
	}

	private volatile int lock;
	private Thread currentThread;

	public void lock() {
		var t = Thread.currentThread();
		for (;;) {
			if (HANDLE.compareAndSet(this, 0, 1)) {
				currentThread = t; // ça marche
				return;
			} else {
				if (currentThread == t) {
					++lock;
					return;
				}
				Thread.onSpinWait();
			}
		}
	}

	public void unlock() {
		if (Thread.currentThread() != currentThread)
			throw new IllegalStateException();
		var l = lock;
		if (l == 1)
			currentThread = null;
		lock = l - 1;
	}

	public static void main(String[] args) throws InterruptedException {
		var runnable = new Runnable() {
			private int counter;
			private final ReentrantSpinLock spinLock = new ReentrantSpinLock();

			@Override
			public void run() {
				for (int i = 0; i < 1_000_000; i++) {
					spinLock.lock();
					try {
						counter++;
					} finally {
						spinLock.unlock();
					}
				}
			}
		};
		var t1 = new Thread(runnable);
		var t2 = new Thread(runnable);
		t1.start();
		t2.start();
		t1.join();
		t2.join();
		System.out.println("counter " + runnable.counter);
	}
}
