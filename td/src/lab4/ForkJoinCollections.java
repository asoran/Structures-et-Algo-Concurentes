package lab4;

import java.util.Collection;
import java.util.Spliterator;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveTask;
import java.util.function.BiFunction;
import java.util.function.BinaryOperator;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class ForkJoinCollections {
	public static <T, V> V forkJoinReduce(Collection<T> collection, int threshold, V initialValue,
			BiFunction<T, V, V> accumulator, BinaryOperator<V> combiner) {

		return forkJoinReduce(collection.spliterator(), collection.size(), threshold, initialValue, accumulator,
				combiner);
	}

	private static <V, T> V forkJoinReduce(Spliterator<T> spliterator, int size, int threshold, V initialValue,
			BiFunction<T, V, V> accumulator, BinaryOperator<V> combiner) {

		var pool = ForkJoinPool.commonPool();
		return pool.invoke(new ReducingTask<>(spliterator, size, threshold, initialValue, accumulator, combiner));
	}

	@SuppressWarnings("serial")
	private static class ReducingTask<T, V> extends RecursiveTask<V> {
		private final Spliterator<T> split;
		private final int threshold;
		private final V initialValue;
		private final BiFunction<T, V, V> acc;
		private final BinaryOperator<V> comb;
		private final long size; // Approximative size, not exact

		public ReducingTask(Spliterator<T> split, long size, int threshold, V initialValue, BiFunction<T, V, V> acc,
				BinaryOperator<V> comb) {
			super();
			this.split = split;
			this.threshold = threshold;
			this.initialValue = initialValue;
			this.acc = acc;
			this.comb = comb;
			this.size = size;
		}

		@Override
		protected V compute() {
			var s = split.estimateSize();
			if (s == Long.MAX_VALUE) {
				s = size;
			}

			if (s < threshold) {
				return computeSeq(split, initialValue, acc);
			}

			var s2 = split.trySplit();
			if (s2 == null) {
				// If it can't split, do it normally
				return computeSeq(s2, initialValue, acc);
			}

			var f2 = new ReducingTask<>(s2, s / 2, threshold, initialValue, acc, comb);
			f2.fork();

			return comb.apply(computeSeq(split, initialValue, acc), f2.join());
		}
	}

	private static <T, V> V computeSeq(Spliterator<T> split, V initialValue, BiFunction<T, V, V> acc) {
		var holder = new Object() {
			private V value = initialValue;
		};
		for (;;) {
			if (!split.tryAdvance(t -> holder.value = acc.apply(t, holder.value))) {
				break;
			}
		}
		return holder.value;
	}

	public static void main(String[] args) {
		// sequential ; takes about 90ms on my pc
		System.out.println(IntStream.range(0, 10_000).sum());

		// fork/join ; takes about 120ms on my pc
		var list = IntStream.range(0, 10_000).boxed().collect(Collectors.toList());
		var result = forkJoinReduce(list, 1_000, 0, (acc, value) -> acc + value, (acc1, acc2) -> acc1 + acc2);
		System.out.println(result);
	}
}
