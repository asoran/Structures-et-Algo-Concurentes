package lab1.exo3;

import static java.util.Objects.requireNonNull;

import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicReference;

public class Linked<E> {
	private static class Entry<E> {
		@SuppressWarnings("unused") // mouais
		private final E element;
		private final Entry<E> next;

		private Entry(E element, Entry<E> next) {
			this.element = element;
			this.next = next;
		}
	}

	private AtomicReference<Entry<E>> head = new AtomicReference<Entry<E>>();

	public void addFirst(E element) {
		requireNonNull(element);

		while(true) {
			var current = head.get();
			if(head.compareAndSet(current, new Entry<E>(element, current))) {
				return;
			}
		}
	}

	public int size() {
		var size = 0;
		for (var link = head.get(); link != null; link = link.next) {
			size++;
		}
		return size;
	}


	public static void main(String[] args) throws InterruptedException {
		final int TEST_NUMBER = 100_000;
		final int NB_OF_THREADS = 2;
		var list = new Linked<String>();

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

		System.out.println("Size should be " + (TEST_NUMBER * NB_OF_THREADS) + ", but is : " + list.size());
	}
}
