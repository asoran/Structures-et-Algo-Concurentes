package exam.exo2;

import java.lang.invoke.MethodHandles;
import java.lang.invoke.VarHandle;
import java.util.Arrays;
import java.util.Objects;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class LockFreeCOWList<T> {
	private static final VarHandle SIZE_HANDLE;
	static {
		try {
			SIZE_HANDLE = MethodHandles.lookup().findVarHandle(LockFreeCOWList.class, "size", int.class);
		} catch (NoSuchFieldException | IllegalAccessException e) {
			throw new AssertionError("Should not happend", e);
		}
	}

	/**
	 * Comme il n'est censé y avoir que 1 seul thread qui "réussie" dans add, je
	 * pense que mettre entries seulement volatile est suffisant. J'ai choisi de
	 * faire un handle sur size car c'est plus rapide a comparer (CaS) que un array.
	 */
	private volatile T[] entries;
	@SuppressWarnings("unused")
	private int size;

	@SuppressWarnings("unchecked")
	public LockFreeCOWList() {
		this.entries = (T[]) new Object[0];
		this.size = 0;
	}

	public boolean add(T value) {
		Objects.requireNonNull(value);

		for (;;) {
			var s = (int) SIZE_HANDLE.getVolatile(this);
			var newSize = s + 1;

			var newArray = Arrays.copyOf(entries, newSize);
			newArray[s] = value;

			if (SIZE_HANDLE.compareAndSet(this, s, newSize)) {
				entries = newArray;
				return true;
			}
		}
	}

	public int size() {
		return (int) SIZE_HANDLE.getVolatile(this);
	}

	public static void main(String[] args) {
		int nbThreads = 4;

		var list = new LockFreeCOWList<Integer>();
		var r = new Random(0);

		var threads = IntStream.range(0, nbThreads).mapToObj(__ -> new Thread(() -> {
			for (var i = 0; i < 2_500; ++i) {
				list.add(r.nextInt(10_000));
			}
		})).collect(Collectors.toList());

		threads.forEach(Thread::start);
		for (var t : threads) {
			try {
				t.join();
			} catch (InterruptedException e) {
				throw new AssertionError("Should not happen");
			}
		}

		System.out.printf("Size should be %d\n", 2_500 * nbThreads);
		System.out.println("And actually is: " + list.size());
	}
}
