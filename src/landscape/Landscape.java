package landscape;

import util.RandomGen;

import java.util.Iterator;
import java.util.Arrays;
import java.util.HashSet;

public class Landscape {
	private InfluenceMatrix myInf;
	private double myBias; // [0, 1]
	private int myShockNum; // >= 0
	private int myStepNum; // [0, N]

	private double[] myMap; // the size is 2^N, N < 32

	private double myMax, myMin; // the local max and min

	/**
	 * Internally generate an array of fitness values for all possible
	 * configurations. Therefore, the size of the array is 2^N, where N is from
	 * the influence matrix. A configuration is also called as a location and
	 * the configuration's corresponding index in the array is called as its
	 * location id.
	 * 
	 * E.g., when N = 5, the fitness value of configuration 0,0,0,0,0 is the 0th
	 * entry in the array, the fitness value of configuration 0,0,0,1,0 is the
	 * 2nd entry in the array, the fitness value of configuration 1,0,0,1,0 is
	 * the 18th entry in the array.
	 * 
	 * In other word, the ith entry in the array represents the fitness value of
	 * the configuration that "looks" like the binary representation of integer
	 * i.
	 * 
	 * @param inf
	 *            an influence matrix object
	 * @param bias
	 *            a value between 0 and 1, which indicates the amount of
	 *            uncertainty at the beginning
	 * @param shockNum
	 *            a non-negative integer, which indicates the shock number
	 * @param stepNum
	 *            an integer between 0 and N (inclusive), which indicates the
	 *            number of implemented elements
	 * @param fit
	 *            a fitness contribution table object
	 */
	public Landscape(InfluenceMatrix inf, double bias, int shockNum,
			int stepNum, FitnessContributionTable fit) {
		// check for valid bias
		if (bias > 1 || bias < 0) {
			System.out
					.println("bias value should be a double in the range [0, 1]");
			System.exit(1);
		}
		// check for valid shockNum
		if (shockNum < 0) {
			System.out
					.println("shockNum value should be a non-negative integer");
			System.exit(1);
		}
		// check for valid stepNum
		if (stepNum > inf.getN() || stepNum < 0) {
			System.out
					.println("step value should be an integer in the range [0, N]");
			System.exit(1);
		}
		// assign private fields
		this.myInf = inf;
		this.myBias = bias;
		this.myShockNum = shockNum;
		this.myStepNum = stepNum;
		this.myMap = new double[1 << this.myInf.getN()];
		this.myMax = 0.0;
		this.myMin = 1.0;
		// compute fitness value for all location ids
		for (int i = 0; i < this.myMap.length; i++) {
			// convert location id to configuration
			int location[] = this.locIdToLocation(i);
			// compute fitness value based on the configuration using fitness
			// contribution table
			double value = 0.0;
			for (int j = 0; j < this.myInf.getN(); j++) {
				// indices in fitness contribution table
				int index1 = j;
				int index2 = location[j];
				int index3 = 0;
				int dependence[] = this.myInf.getDependentElementsOf(j);
				for (int k = 0; k < this.myInf.getK(); k++) {
					index3 <<= 1;
					index3 += location[dependence[k]];
				}
				double currentUncertainty = this.getCurrentUncertainty();
				value += ((1 - currentUncertainty)
						* fit.getValueOf(this.myShockNum, index1, index2,
								index3) + currentUncertainty
						* RandomGen.randomGen.nextDouble());
			}
			this.myMap[i] = value / this.myInf.getN();
			if (this.myMap[i] > this.myMax) {
				this.myMax = this.myMap[i];
			}
			if (this.myMap[i] < this.myMin) {
				this.myMin = this.myMap[i];
			}
		}
	}

	/**
	 * Return the fitness value of the given location id
	 * 
	 * @param locId
	 *            a non-negative integer, which represents a
	 *            configuration/location
	 * @return the fitness value of the given location id
	 */
	public double getScoreOfLocId(int locId) {
		return this.myMap[locId];
	}

	/**
	 * Return the local maximum value in the landscape.
	 * 
	 * @return the local maximum value in the landscape
	 */
	public double getMax() {
		return this.myMax;
	}

	/**
	 * Return the local minimum value in the landscape.
	 * 
	 * @return the local minimum value in the landscape
	 */
	public double getMin() {
		return this.myMin;
	}

	/**
	 * Define the distance between two configurations/locations to be the number
	 * of different element values.
	 * 
	 * E.g., distance between configurations 1,0,0,0 and 1,0,0,1 is 1, distance
	 * between configurations 1,0,0,0 and 0,1,1,0 is 3.
	 * 
	 * Define the distance between two configurations/locations w.r.t. a set of
	 * element indices to be the number of different element values in and ONLY
	 * in the given set of element indices.
	 * 
	 * E.g., the set of all configurations whose distances to configuration
	 * 1,0,0,0 are 1 w.r.t. element indices {0,1,2} is {[0,0,0,0], [1,1,0,0],
	 * [1,0,1,0]}
	 * 
	 * E.g., the set of all configurations whose distances to configuration
	 * 1,0,0,0 are 2 w.r.t. element indices {0,1,2} is {[1,1,1,0], [0,1,0,0],
	 * [0,0,1,0]}
	 * 
	 * This method returns the set of all location ids whose distances to the
	 * given location id w.r.t. the given elements are smaller or equal to the
	 * given processing power.
	 * 
	 * @param locId
	 *            a non-negative integer, which represents a
	 *            configuration/location
	 * @param elements
	 *            a set of element indices, which indicates the changeable
	 *            elements in a configuration
	 * @param processingPower
	 *            a non-negative integer, which indicates the maximum number of
	 *            changes to a configuration
	 * @return the set of all location ids whose distances to the given location
	 *         id w.r.t the given elements are smaller or equal to the given
	 *         processing power
	 */
	public HashSet<Integer> getNeighboursInclusive(int locId,
			HashSet<Integer> elements, int processingPower) {
		HashSet<Integer> result = new HashSet<Integer>();
		// base case
		if (processingPower == 0 || elements.isEmpty()) {
			result.add(locId);
			return result;
		}
		// recursion
		HashSet<Integer> reducedElements = new HashSet<Integer>(elements);
		Iterator<Integer> itr = reducedElements.iterator();
		int toggledLocId = this.toggleElementInLocId(locId, itr.next());
		itr.remove();

		result = this.getNeighboursInclusive(toggledLocId, reducedElements,
				processingPower - 1);
		result.addAll(this.getNeighboursInclusive(locId, reducedElements,
				processingPower));
		return result;
	}

	/**
	 * Returns a location id whose elements indicated by changableElements are
	 * copied from locIdMask whereas the rest elements are copied from locId
	 * 
	 * @param locId
	 *            location id to be changed
	 * @param locIdMask
	 *            location id to be used as mask
	 * @param changableElements
	 *            a set of element indices, which indicates the changeable
	 *            elements in a configuration
	 * @return a location id whose elements indicated by changableElements are
	 *         copied from locIdMask whereas the rest elements are copied from
	 *         locId
	 */
	public int changeElements(int locId, int locIdMask,
			HashSet<Integer> changableElements) {
		int location[] = locIdToLocation(locId);
		int locationMask[] = locIdToLocation(locIdMask);
		for (Integer element : changableElements) {
			location[element] = locationMask[element];
		}
		return locationToLocId(location);
	}

	/**
	 * Return a string representation of the landscape
	 * 
	 * @return a string representation of the landscape
	 */
	@Override
	public String toString() {
		String result = "";
		result += (this.myInf.toString() + "\n");
		result += ("bias\t: " + this.myBias + "\n");
		result += ("type\t: " + this.myShockNum + "\n");
		result += ("step\t: " + this.myStepNum + "\n");
		for (int i = 0; i < this.myMap.length; i++) {
			result += (i + "\t" + Arrays.toString(this.locIdToLocation(i))
					+ " -> " + this.myMap[i] + "\n");
		}
		return result;
	}

	/**
	 * Return an integer array that "looks" like the binary form of the given
	 * location id. E.g., when N = 4, location id = 13, then the array is
	 * [1,1,0,1]
	 * 
	 * @param locId
	 *            a non-negative integer, which represents a
	 *            configuration/location
	 * @return an integer array that "looks" like the binary form of the given
	 *         location id
	 */
	private int[] locIdToLocation(int locId) {
		int location[] = new int[this.myInf.getN()];
		for (int j = 0; j < this.myInf.getN(); j++) {
			location[j] = (locId >> (this.myInf.getN() - 1 - j)) % 2;
		}
		return location;
	}

	/**
	 * The reverse function of locIdToLocation.
	 * 
	 * @param location
	 *            an integer array, which represents a configuration/location
	 * @return a location id
	 */
	private int locationToLocId(int[] location) {
		int locId = 0;
		for (int i = 0; i < location.length; i++) {
			locId <<= 1;
			locId += location[i];
		}
		return locId;
	}

	/**
	 * Return a location id whose binary form is one bit different from the
	 * given location id, and the position of the different bit is determined by
	 * the given element index.
	 * 
	 * E.g., when N = 4, location id = 13, element index = 1, then the binary
	 * form is [1,1,0,1]. The new binary form is [1,0,0,1] and the new location
	 * id is 9.
	 * 
	 * @param locId
	 *            a non-negative integer, which represents a
	 *            configuration/location
	 * @param elementIdx
	 *            a non-negative integer, which indicates the position of the
	 *            bit to be toggled
	 * @return a location id whose binary form is one bit different from the
	 *         given location id, and the position of the different bit is
	 *         determined by the given element index.
	 */
	private int toggleElementInLocId(int locId, int elementIdx) {
		int shiftAmount = this.myInf.getN() - 1 - elementIdx;
		return locId + (1 << shiftAmount)
				* ((locId >> shiftAmount) % 2 == 0 ? 1 : -1);
	}

	/**
	 * The initial (before implementation) uncertainty value is defined to be
	 * the value of bias, and the final (after full implementation) uncertainty
	 * value is defined to be 0. The uncertainty value decreases linearly w.r.t.
	 * the number of implemented elements, i.e. step number. This method returns
	 * the uncertainty value determined by the current step number.
	 * 
	 * @return the uncertainty value determined by the current step number
	 */
	private double getCurrentUncertainty() {
		return (1.0 - (double) (this.myStepNum) / this.myInf.getN())
				* this.myBias;
	}

	/**
	 * Print the table contents, i.e., the fitness values of 2^N configurations
	 */
	public void printTableContents() {
		for (int i = 0; i < this.myMap.length; i++) {
			System.out.println(this.myMap[i]);
		}
	}

}
