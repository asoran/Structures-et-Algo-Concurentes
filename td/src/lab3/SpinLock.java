package lab3;

import java.lang.invoke.MethodHandles;
import java.lang.invoke.VarHandle;

public class SpinLock {
	public static final VarHandle HANDLE;
	static {
		try {
			HANDLE = MethodHandles.lookup().findVarHandle(SpinLock.class, "isLocked", boolean.class);
		} catch (NoSuchFieldException | IllegalAccessException e) {
			throw new AssertionError();
		}
	}

	@SuppressWarnings("unused")
	private volatile boolean isLocked = false;

	public void lock() {
		while (!HANDLE.compareAndSet(this, false, true))
			Thread.onSpinWait();
	}

	public void unlock() {
		isLocked = false;
	}

	public boolean tryLock() {
		return HANDLE.compareAndSet(this, false, true);
	}

	public static void main(String[] args) throws InterruptedException {
		var runnable = new Runnable() {
			private int counter;
			private final SpinLock spinLock = new SpinLock();

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
