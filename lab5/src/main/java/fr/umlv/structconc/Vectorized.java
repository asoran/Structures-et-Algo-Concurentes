package fr.umlv.structconc;

import jdk.incubator.vector.IntVector;
import jdk.incubator.vector.VectorOperators;
import jdk.incubator.vector.VectorSpecies;

public class Vectorized {

	private static final VectorSpecies<Integer> INT_VEC_SPEC = IntVector.SPECIES_PREFERRED;

	public static int sumLoop(int[] array) {
		var sum = 0;
		for (var value : array) {
			sum += value;
		}

		return sum;
	}

	public static int sumReduceLane(int[] array) {

		var sum = 0;

		var i = 0;
		var limit = array.length - (array.length % INT_VEC_SPEC.length()); // main loop

		for (; i < limit; i += INT_VEC_SPEC.length()) {
			var iv = IntVector.fromArray(INT_VEC_SPEC, array, i);
			sum += iv.reduceLanes(VectorOperators.ADD);
		}
		for (; i < array.length; i++) { // post loop
			sum += array[i];
		}

		return sum;
	}

	public static int sumLanewise(int[] array) {
		var i = 0;
		var limit = array.length - (array.length % INT_VEC_SPEC.length()); // main loop

		var sumIv = IntVector.zero(INT_VEC_SPEC);

		for (; i < limit; i += INT_VEC_SPEC.length()) {
			var iv = IntVector.fromArray(INT_VEC_SPEC, array, i);
			sumIv = sumIv.add(iv);
		}

		var sum = sumIv.reduceLanes(VectorOperators.ADD);

		for (; i < array.length; i++) { // post loop
			sum += array[i];
		}

		return sum;
	}

	public static int differenceLanewise(int[] array) {
		if (array.length == 0) {
			return 0;
		}

		var i = 0;
		var limit = array.length - (array.length % INT_VEC_SPEC.length()); // main loop

		var subIv = IntVector.zero(INT_VEC_SPEC);

		for (; i < limit; i += INT_VEC_SPEC.length()) {
			var iv = IntVector.fromArray(INT_VEC_SPEC, array, i);
			subIv = subIv.sub(iv);
		}

		var sub = 0;
		var arr = subIv.toArray();
		for (var a : arr) {
			sub += a; // et pas -= rip 10 min de ma vie
		}

		for (; i < array.length; i++) { // post loop
			sub -= array[i];
		}

		return sub;
	}

	public static int[] minmax(int[] array) {
		var maxIv = IntVector.broadcast(INT_VEC_SPEC, Integer.MIN_VALUE);
		var minIv = IntVector.broadcast(INT_VEC_SPEC, Integer.MAX_VALUE);

		var i = 0;
		var limit = array.length - (array.length % INT_VEC_SPEC.length()); // main loop
		for (; i < limit; i += INT_VEC_SPEC.length()) {
			var iv = IntVector.fromArray(INT_VEC_SPEC, array, i);
			maxIv = maxIv.max(iv);
			minIv = minIv.min(iv);
		}

		var max = maxIv.reduceLanes(VectorOperators.MAX);
		var min = minIv.reduceLanes(VectorOperators.MIN);

		for (; i < array.length; i++) { // post loop
			var a = array[i];
			if(a > max) {
				max = a;
			}
			if(a < min) {
				min = a;
			}
		}

		return new int[] { min, max };
	}
}
