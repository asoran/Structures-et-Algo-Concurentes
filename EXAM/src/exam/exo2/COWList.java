package exam.exo2;

import java.util.Arrays;
import java.util.Objects;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class COWList<T> {

	private T[] entries;
	private int size;

	@SuppressWarnings("unchecked")
	public COWList() {
		this.entries = (T[]) new Object[0];
		this.size = 0;
	}

	public boolean add(T value) {
		Objects.requireNonNull(value);
		entries = Arrays.copyOf(entries, size + 1);
		entries[size++] = value;
		return true;
	}

	public int size() {
		return size;
	}

	/**
	 * 3) Y'a une datarace: plusieurs threads eessaient d'acceder a size et de
	 * modifier le contenue de entries
	 * 
	 */
	public static void main(String[] args) {
		int nbThreads = 4;

		var list = new COWList<Integer>();
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
		// Care, IndexOutOfBoundException will highly likely occur
	}
}
