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

		return forkJoinReduce(collection.spliterator(), threshold, initialValue, accumulator, combiner);
	}

	public static <T, V> V computeSeq(Spliterator<T> split, V initialValue, BiFunction<T, V, V> acc) {
		final var holder = new Object() {
			private V value = initialValue;
		};
		for (;;)
			if (!split.tryAdvance(t -> holder.value = acc.apply(t, holder.value)))
				break;
		return holder.value;
	}

	@SuppressWarnings("serial") // ?????????????
	private static class ReducingTask<T, V> extends RecursiveTask<V> {
		final Spliterator<T> split;
		final int threshold;
		final V initialValue;
		final BiFunction<T, V, V> acc;
		final BinaryOperator<V> comb;

		public ReducingTask(Spliterator<T> split, int threshold, V initialValue, BiFunction<T, V, V> acc,
				BinaryOperator<V> comb) {
			super();
			this.split = split;
			this.threshold = threshold;
			this.initialValue = initialValue;
			this.acc = acc;
			this.comb = comb;
		}

		@Override
		protected V compute() {
			var s = split.estimateSize();
			if (s == Long.MAX_VALUE) {
				throw new AssertionError("Je sais pas quoi faire");
			}

			if (s < threshold) {
				return computeSeq(split, initialValue, acc);
			}

			var s2 = split.trySplit();
			if (s2 == null)
				throw new IllegalArgumentException("Spliterator cannot split"); // Change this exception ?
			var f2 = new ReducingTask<T, V>(s2, threshold, initialValue, acc, comb);
			f2.fork();

			return comb.apply(computeSeq(split, initialValue, acc), f2.join());
		}
	}

	private static <V, T> V forkJoinReduce(Spliterator<T> spliterator, int threshold, V initialValue,
			BiFunction<T, V, V> accumulator, BinaryOperator<V> combiner) {

		var pool = ForkJoinPool.commonPool();
		return pool.invoke(new ReducingTask<T, V>(spliterator, threshold, initialValue, accumulator, combiner));
	}

	public static void main(String[] args) {
		// sequential
		System.out.println(IntStream.range(0, 10_000).sum());

		// fork/join
		var list = IntStream.range(0, 10_000).boxed().collect(Collectors.toList());
		var result = forkJoinReduce(list, 1_000, 0, (acc, value) -> acc + value, (acc1, acc2) -> acc1 + acc2);
		System.out.println(result);
	}
}
