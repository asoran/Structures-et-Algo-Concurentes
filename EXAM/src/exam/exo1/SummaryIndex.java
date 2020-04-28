package exam.exo1;

import java.util.Arrays;
import java.util.Random;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveTask;

public class SummaryIndex {
	private static class Entry {
		private double average;
		private int cursor;
		private final double[] data;

		private Entry(int dataLength) {
			this.average = Double.NaN;
			double[] data = new double[dataLength];
			Arrays.fill(data, Double.NaN);
			this.data = data;
		}
	}

	private final Entry[] entries;

	public SummaryIndex(int entryLength, int dataLength) {
		var entries = new Entry[entryLength];
		for (var i = 0; i < entries.length; i++) {
			entries[i] = new Entry(dataLength);
		}
		this.entries = entries;
	}

	public void add(int entryIndex, double value) {
		var entry = entries[entryIndex];
		var cursor = entry.cursor;
		entry.data[cursor] = value;
		entry.cursor = (cursor + 1) % entry.data.length;
	}

	public double average(int entryIndex) { // pas utilisée dans l'exercice
		return entries[entryIndex].average;
	}

	public double sumSummary() {
		var sum = 0.0;
		for (var i = 0; i < entries.length; i++) {
			var entry = entries[i];
			var stats = Arrays.stream(entry.data).filter(v -> !Double.isNaN(v)).summaryStatistics();
			var average = stats.getAverage();
			;
			entry.average = average;
			if (!Double.isNaN(average)) {
				sum += stats.getSum();
			}
		}
		return sum;
	}

	public double sequentialSumSummary(int from, int to) {
		var sum = 0.0;
		for (var i = from; i < to; i++) {
			var entry = entries[i];
			var stats = Arrays.stream(entry.data).filter(v -> !Double.isNaN(v)).summaryStatistics();
			var average = stats.getAverage();
			entry.average = average;
			if (!Double.isNaN(average)) {
				sum += stats.getSum();
			}
		}
		return sum;
	}

	public double parallelSumSummary() {
		var pool = ForkJoinPool.commonPool();
		return pool.invoke(new Task(this, 0, entries.length));
	}

	@SuppressWarnings("serial")
	private static class Task extends RecursiveTask<Double> {
		private final static int FLOOR = 100;
		private final SummaryIndex index;
		private final int from, to;

		public Task(SummaryIndex index, int from, int to) {
			this.index = index;
			this.from = from;
			this.to = to;
		}

		@Override
		protected Double compute() {
			if (to - from <= FLOOR) {
				return index.sequentialSumSummary(from, to);
			} else {
				var middle = (from + to) / 2;

				var t1 = new Task(index, from, middle);
				var t2 = new Task(index, middle, to);
				t2.fork();
				return t1.compute() + t2.join();
			}
		}
	}

	public double averageSummary() {
		var sum = 0.0;
		var count = 0L;
		for (var i = 0; i < entries.length; i++) {
			var entry = entries[i];
			var stats = Arrays.stream(entry.data).filter(v -> !Double.isNaN(v)).summaryStatistics();
			var average = stats.getAverage();
			entry.average = average;
			if (!Double.isNaN(average)) {
				sum += stats.getSum();
				count += stats.getCount();
			}
		}
		return sum / count;
	}

	public double parallelAverageSummary() {
		var pool = ForkJoinPool.commonPool();
		return pool.invoke(new TaskAverage(this, 0, entries.length));
	}

	@SuppressWarnings("serial")
	private static class TaskAverage extends RecursiveTask<Double> {
		private final static int FLOOR = 100;
		private final SummaryIndex index;
		private final int from, to;
		private final boolean initialCall; // Know if its initial call or not

		private long c; // Count the number of elements

		private TaskAverage(SummaryIndex index, int from, int to, boolean call) {
			// assert(from < to); // ?
			this.index = index;
			this.from = from;
			this.to = to;
			this.initialCall = call;
			this.c = 0L;
		}

		public TaskAverage(SummaryIndex index, int from, int to) {
			this(index, from, to, true);
		}

		@Override
		protected Double compute() {
			if (to - from <= FLOOR) {
				var sum = 0.0;
				var count = 0L;
				for (var i = from; i < to; i++) {
					var entry = index.entries[i];
					var stats = Arrays.stream(entry.data).filter(v -> !Double.isNaN(v)).summaryStatistics();
					var average = stats.getAverage();
					entry.average = average;
					if (!Double.isNaN(average)) {
						sum += stats.getSum();
						count += stats.getCount();
					}
				}

				this.c = count;
				if (initialCall) {
					return sum / count;
				} else {
					return sum;
				}
			}

			var middle = (from + to) / 2;

			var t1 = new TaskAverage(index, from, middle, false);
			var t2 = new TaskAverage(index, middle, to, false);
			t2.fork();
			var v1 = t1.compute();
			var v2 = t2.join();

			this.c = t2.c + t1.c;
			if (initialCall) {
				return (v1 + v2) / this.c;
			} else {
				return (v1 + v2);
			}
		}
	}

	public static void main(String[] args) {
		var entryLength = 20_000;
		var summaryIndex = new SummaryIndex(entryLength, 200);

		var random = new Random(0);
		for (var i = 0; i < 10_000_000; i++) {
			summaryIndex.add(i % entryLength, random.nextInt(100));
		}

		System.out.println(summaryIndex.sumSummary());
		System.out.println(summaryIndex.sequentialSumSummary(0, entryLength));
		System.out.println(summaryIndex.sequentialSumSummary(0, entryLength / 2)
				+ summaryIndex.sequentialSumSummary(entryLength / 2, entryLength));
		System.out.println(summaryIndex.parallelSumSummary());

		System.out.println(summaryIndex.averageSummary());
		System.out.println(summaryIndex.parallelAverageSummary());
	}
}
