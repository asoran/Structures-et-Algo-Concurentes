package lab2.exo3;

import static java.util.Objects.requireNonNull;

import java.lang.invoke.MethodHandles;
import java.lang.invoke.VarHandle;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class COWSet<E> {
	private final E[][] hashArray;

	private static final Object[] EMPTY = new Object[0];

	private final static VarHandle HASH_ARRAY_HANDLE;
	static {
		HASH_ARRAY_HANDLE = MethodHandles.arrayElementVarHandle(Object[][].class);
	}

	@SuppressWarnings("unchecked")
	public COWSet(int capacity) {
		var array = new Object[capacity][];
		Arrays.setAll(array, __ -> EMPTY);
		this.hashArray = (E[][]) array;
	}

	public boolean add(E element) {
		requireNonNull(element);
		var index = element.hashCode() % hashArray.length;
		for (;;) {
			var arr = (E[]) HASH_ARRAY_HANDLE.getVolatile(hashArray, index);
			for (var e : arr) {
				if (element.equals(e)) {
					return false;
				}
			}

			var oldArray = arr;
			var newArray = Arrays.copyOf(oldArray, oldArray.length + 1);
			newArray[oldArray.length] = element;

			if (HASH_ARRAY_HANDLE.compareAndSet(hashArray, index, arr, newArray))
				return true;
		}
	}

	public void forEach(Consumer<? super E> consumer) {
		for (var index = 0; index < hashArray.length; index++) {
			var oldArray = (E[]) HASH_ARRAY_HANDLE.getVolatile(hashArray, index);

			for (var element : oldArray) {
				consumer.accept(element);
			}
		}
	}

	public static void main(String[] args) throws InterruptedException {
		var nbThread = 2;
		var maxElem = 200_000;
		var MAGIKU_NAMBA = 4;

		var set = new COWSet<Integer>(maxElem / MAGIKU_NAMBA);

		var threads = IntStream.range(0, nbThread).mapToObj(__ -> {
			return new Thread(() -> {
				IntStream.range(0, maxElem).forEach(i -> {
					set.add(i);
				});
			});
		}).collect(Collectors.toList());
		threads.forEach(Thread::start);
		for (var t : threads)
			t.join();

		var list = new ArrayList<Integer>();
		set.forEach(list::add);

		System.out.printf("Should be %d ; is : %d\n", maxElem, list.size());

	}
}