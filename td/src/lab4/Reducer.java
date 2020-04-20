package lab4;

import java.util.Arrays;
import java.util.Random;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveTask;
import java.util.function.IntBinaryOperator;

public class Reducer {
	public static int sum(int[] array) {
		return parallelReduceWithForkJoin(array, 0, Math::addExact);
	}

	public static int max(int[] array) {
		return parallelReduceWithForkJoin(array, Integer.MIN_VALUE, Math::max);
	}

	public static int reduce(int[] arr, int initial, IntBinaryOperator op) {
		var acc = initial;
		for (var val : arr) {
			acc = op.applyAsInt(acc, val);
		}
		return acc;
	}

	public static int reduceWithStream(int[] arr, int from, int toEx, int initial, IntBinaryOperator op) {
		return Arrays.stream(arr, from, toEx).reduce(initial, op);
	}

	public static int reduceWithStream(int[] arr, int initial, IntBinaryOperator op) {
		return Arrays.stream(arr).reduce(initial, op);
	}

	public static int parallelReduceWithStream(int[] arr, int initial, IntBinaryOperator op) {
		return Arrays.stream(arr).parallel().reduce(initial, op);
	}

	private static class ReducingTask extends RecursiveTask<Integer> {
		final int[] arr;
		final int initial, from, toEx;
		final IntBinaryOperator op;

		public ReducingTask(int[] arr, int from, int toEx, int initial, IntBinaryOperator op) {
			this.arr = arr;
			this.initial = initial;
			this.op = op;
			this.from = from;
			this.toEx = toEx;
		}

		@Override
		protected Integer compute() {
			var s = (toEx - from);

			if (s < 100_000) {
				return reduceWithStream(arr, from, toEx, initial, op);
			}

			int middle = Math.floorDiv(from + toEx, 2);
			var f1 = new ReducingTask(arr, from, middle, initial, op);
			f1.fork();
			var f2 = new ReducingTask(arr, middle, toEx, initial, op);
			return op.applyAsInt(f2.compute(), f1.join());
		}
	}

	public static int parallelReduceWithForkJoin(int[] arr, int initial, IntBinaryOperator op) {
		var pool = ForkJoinPool.commonPool();

		return pool.invoke(new ReducingTask(arr, 0, arr.length, initial, op));
	}

	public static void main(String[] args) {
		var r = new Random(0);
		var str = r.ints(1_000_000, 0, 1_000);
		var arr = str.toArray();
		// Max: 999 ; Sum: 499293065

		System.out.println(max(arr));
		System.out.println(sum(arr));
		// 499293065
	}
}
