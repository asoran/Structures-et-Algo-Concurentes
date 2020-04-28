package exam.exo3;

import java.lang.invoke.MethodHandles;
import java.lang.invoke.VarHandle;

public class LockFreeTwoRandomNumberGenerator {
	private static final VarHandle X_HANDLE;
	static {
		try {
			X_HANDLE = MethodHandles.lookup().findVarHandle(LockFreeTwoRandomNumberGenerator.class, "x", long.class);
		} catch (NoSuchFieldException | IllegalAccessException e) {
			throw new AssertionError("Should not happend", e);
		}
	}

	@SuppressWarnings("unused")
	private long x;

	public LockFreeTwoRandomNumberGenerator(long seed) {
		if (seed == 0) {
			throw new IllegalArgumentException("seed == 0");
		}
		x = seed;
	}

	private static long marsagliaXorShift(long _x) {
		return ((_x ^ (_x >>> 12)) ^ ((_x ^ (_x >>> 12)) << 25))
				^ (((_x ^ (_x >>> 12)) ^ ((_x ^ (_x >>> 12)) << 25)) >>> 27);
		// C'est moche :')
	}

	/**
	 * Je préfère l'implémentation avec CaS car je trouve cette façon très vodoo et
	 * je sais même pas si j'ai bien fait :(
	 */
	public long next() { // Marsaglia's XorShift
		var oldX = (long) X_HANDLE.getAndSet(this, marsagliaXorShift(x));
		return marsagliaXorShift(oldX) * 2685821657736338717L;
	}

	public static void main(String[] args) {
		var rng = new LockFreeTwoRandomNumberGenerator(1);
		for (var i = 0; i < 5_000; i++) {
			System.out.println(rng.next());
		}
	}
}
