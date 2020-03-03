package exo3;

import static java.util.Objects.requireNonNull;

import java.lang.invoke.MethodHandles;
import java.lang.invoke.VarHandle;
import java.util.ArrayList;

public class Linked2<E> {
	private static class Entry<E> {
		@SuppressWarnings("unused") // mouais
		private final E element;
		private final Entry<E> next;

		private Entry(E element, Entry<E> next) {
			this.element = element;
			this.next = next;
		}
	}

	@SuppressWarnings("unused")
	private volatile Entry<E> entry;
	private final static VarHandle handler;

	static {
		try {
			handler = MethodHandles.lookup()
				.findVarHandle(Linked2.class, "entry", Entry.class);

		} catch (NoSuchFieldException | IllegalAccessException e) {
			throw new AssertionError("=(");
		}
	}

	public void addFirst(E element) {
		requireNonNull(element);

		while(true) {
			var current = (Entry<E>) handler.getVolatile(this);
			if(handler.compareAndSet(this, current, new Entry<E>(element, current))) {
				return;
			}
		}
	}

	public int size() {
		var size = 0;
		for (var link = (Entry<E>) handler.getVolatile(this); link != null; link = link.next) {
			size++;
		}
		return size;
	}


	public static void main(String[] args) throws InterruptedException {
		final int TEST_NUMBER = 100_000;
		final int NB_OF_THREADS = 2;
		var list = new Linked2<String>();

		var threads = new ArrayList<Thread>();
		for(var i = 0; i < NB_OF_THREADS; ++i) {
			threads.add(new Thread(() -> {
				for(var j = 0; j < TEST_NUMBER; ++j) {
					list.addFirst("Hello World");
				}
			}));
		}

		threads.forEach(Thread::start);
		for(var thread : threads)
			thread.join();

		System.out.println("[Size should be " + (TEST_NUMBER * NB_OF_THREADS) + ", and it is : " + list.size());
	}	
}
