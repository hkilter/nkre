package util;

import random.*;

public class RandomGen {
	private final static long myMagicSeed = 900111;
	private static int myCurrentSeed = -1;

	/**
	 * Global random generator object
	 */
	public static MersenneTwisterFast randomGen = null;

	/**
	 * To avoid the correlation among continuous seeds, this method uses a fixed
	 * seed to generate a sequence of random integers and takes the ith random
	 * integer to be the actual seed, where i is the given seed.
	 * 
	 * @param seed
	 *            an non-negative integer, which is actually the run number
	 */
	public static void setSeed(int seed) {
		myCurrentSeed = seed;
		randomGen = new MersenneTwisterFast(myMagicSeed);
		long newSeed = 0;
		for (int i = 0; i < seed; i++) {
			newSeed = randomGen.nextInt();
		}
		randomGen = new MersenneTwisterFast(newSeed);
	}

	/**
	 * Return the run number instead of the actual seed.
	 * 
	 * @return the run number instead of the actual seed
	 */
	public static int getSeed() {
		return myCurrentSeed;
	}
}
