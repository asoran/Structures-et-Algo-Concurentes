package exam.exo3;

import java.lang.invoke.MethodHandles;
import java.lang.invoke.VarHandle;

public class LockFreeOneRandomNumberGenerator {
	private static final VarHandle X_HANDLE;
	static {
		try {
			X_HANDLE = MethodHandles.lookup().findVarHandle(LockFreeOneRandomNumberGenerator.class, "x", long.class);
		} catch (NoSuchFieldException | IllegalAccessException e) {
			throw new AssertionError("Should not happend", e);
		}
	}

	@SuppressWarnings("unused")
	private long x;

	public LockFreeOneRandomNumberGenerator(long seed) {
		if (seed == 0) {
			throw new IllegalArgumentException("seed == 0");
		}
		x = seed;
	}

	public long next() { // Marsaglia's XorShift
		for (;;) {
			var oldX = (long) X_HANDLE.getVolatile(this);
			var _x = oldX;
			_x ^= _x >>> 12;
			_x ^= _x << 25;
			_x ^= _x >>> 27;
			if(X_HANDLE.compareAndSet(this, oldX, _x)) {
				return _x * 2685821657736338717L;
			}
		}
	}

	public static void main(String[] args) {
		var rng = new LockFreeOneRandomNumberGenerator(1);
		for (var i = 0; i < 5_000; i++) {
			System.out.println(rng.next());
		}
	}
}
