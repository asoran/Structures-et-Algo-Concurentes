package exam.exo3;

public class RandomNumberGenerator {
	private long x;

	public RandomNumberGenerator(long seed) {
		if (seed == 0) {
			throw new IllegalArgumentException("seed == 0");
		}
		x = seed;
	}

	public long next() { // Marsaglia's XorShift
		x ^= x >>> 12;
		x ^= x << 25;
		x ^= x >>> 27;
		return x * 2685821657736338717L;
	}

	/**
	 * Data race dans next sur 'x': la méthode peut être interrompue a nimporte quel
	 * moment et les threads vont s'entre mélanger leur 'x' ...
	 * 
	 */
	public static void main(String[] args) {
		var rng = new RandomNumberGenerator(1);
		for (var i = 0; i < 5_000; i++) {
			System.out.println(rng.next());
		}
	}
}
