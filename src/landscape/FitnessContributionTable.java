package landscape;

import util.RandomGen;
import java.util.Arrays;
import java.util.ArrayList;

public class FitnessContributionTable {
	private InfluenceMatrix myInf;
	private double myDelta; // [0, 1]
	private int myTotalShockNum; // >= 0
	private int myDim1, myDim2, myDim3;
	/**
	 * A 4-dimensional table, the size is
	 * 
	 * (myTotalShockNum + 1) x N rows x 2 choices x 2^K choices, K < N < 32
	 */
	private ArrayList<double[][][]> myTable;

	/**
	 * Internally create a 4-dimensional table, (total shock number + 1) by N by
	 * 2 by 2^K, where N and K are from the influence matrix. Dimension 1
	 * represents shock numbers. Dimension 2 represents N elements. Dimension 3
	 * represents the 2 choices (i.e. 0 or 1) for one element. Dimension 4
	 * represents all the possible combination of one element's dependent
	 * elements. The value is a randomly generated fitness contribution value
	 * between 0 to 1.
	 * 
	 * E.g., when N = 6 and K = 5, the value in [1][0][1][5] gives the fitness
	 * contribution value "between the 1st and the 2nd shocks",
	 * "the 0th element" is "1" given
	 * "its 5 dependent elements are 0,0,1,0,1 respectively".
	 * 
	 * E.g., when N = 4 and K = 2, the value in [0][1][0][1] gives the fitness
	 * contribution value "before the 1st shock", "the 1st element" is "0" given
	 * "its 2 dependent elements are 0,1 respectively".
	 * 
	 * @param inf
	 *            an influence matrix object
	 * @param delta
	 *            a value between 0 and 1, which indicates the amount of changes
	 *            in each shock, 0 for no changes and 1 for arbitrary changes
	 * @param totalShockNum
	 *            a non-negative integer, which indicates the total number of
	 *            shocks
	 * @see InfluenceMatrix
	 */
	public FitnessContributionTable(InfluenceMatrix inf, double delta,
			int totalShockNum) {
		// check for valid delta
		if (delta > 1 || delta < 0) {
			System.out
					.println("delta value should be a double in the range [0, 1]");
			System.exit(1);
		}
		// check for valid totalShockNum
		if (totalShockNum < 0) {
			System.out
					.println("totalShockNum value should be a non-negative integer");
			System.exit(1);
		}
		// assign private field
		this.myDelta = delta;
		this.myTotalShockNum = totalShockNum;
		this.myInf = inf;

		this.myDim1 = this.myInf.getN();
		this.myDim2 = 2;
		this.myDim3 = (1 << this.myInf.getK());
		this.myTable = new ArrayList<double[][][]>();
		// loop for (total shock number + 1) times, here the +1 is for no shock
		for (int i = 0; i < this.myTotalShockNum + 1; i++) {
			this.myTable.add(new double[this.myDim1][this.myDim2][this.myDim3]);
		}

		// fill up the 4-dimensional table
		for (int i = 0; i < this.myDim1; i++) {
			for (int j = 0; j < this.myDim2; j++) {
				for (int k = 0; k < this.myDim3; k++) {
					// firstly generate the value for the time when no shocks
					// have occurred
					this.myTable.get(0)[i][j][k] = RandomGen.randomGen
							.nextDouble();
					// then generate the values for the following shocks
					// recursively
					// i.e. the value after lth shock depends on the value of
					// after (l-1)th shock
					for (int l = 1; l < this.myTotalShockNum + 1; l++) {
						this.myTable.get(l)[i][j][k] = (1 - this.myDelta)
								* this.myTable.get(l - 1)[i][j][k]
								+ this.myDelta
								* RandomGen.randomGen.nextDouble();
					}
				}
			}
		}
	}

	/**
	 * Return the fitness contribution value in the internal 4-dimensional table
	 * with the given indices.
	 * 
	 * @return the fitness contribution value in the internal 4-dimensional
	 *         table with the given indices
	 * @see FitnessContributionTable
	 */
	public double getValueOf(int shockIndex, int index1, int index2, int index3) {
		return this.myTable.get(shockIndex)[index1][index2][index3];
	}

	/**
	 * Return a string representation of the fitness table.
	 * 
	 * @return a string representation of the fitness table
	 */
	@Override
	public String toString() {
		String result = "delta\t: " + this.myDelta + "\n";
		for (int i = 0; i < this.myDim1; i++) {
			for (int j = 0; j < this.myDim2; j++) {
				for (int k = 0; k < this.myDim3; k++) {
					Integer kInBinary[] = new Integer[this.myInf.getK()];
					for (int l = 0; l < this.myInf.getK(); l++) {
						kInBinary[l] = ((k >> (this.myInf.getK() - 1 - l)) % 2);
					}
					result += ("d("
							+ i
							+ ") = "
							+ j
							+ " | "
							+ "d"
							+ Arrays.toString(this.myInf
									.getDependentElementsOf(i)) + " = "
							+ Arrays.toString(kInBinary) + " ->");
					for (double[][][] subTable : this.myTable) {
						result += ("\t" + subTable[i][j][k]);
					}
					result += "\n";
				}
			}
		}
		return result;
	}
}
