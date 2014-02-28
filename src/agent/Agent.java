package agent;

import util.RandomGen;
import landscape.InfluenceMatrix;

import java.util.ArrayList;
import java.util.HashSet;

public class Agent {
	private String myType;
	private int myTotalNum;
	private int myNum;

	private InfluenceMatrix myInf;
	private ArrayList<HashSet<Integer>> myIterationPlan;
	private int myProcessingPower;
	private int myLocId;
	private int myCurrentIterationNum;
	private HashSet<Integer> myImplementedElements;
	private HashSet<Integer> myUnimplementedElements;

	private double myContraint;
	private boolean myIsExhaustive;

	// private boolean myIsRefactoringAll;

	/**
	 * Create a passive agent object. Here passive means that the agent does not
	 * interact with the landscape object directly. The agent only keeps its
	 * states, including its type name, number, iteration plan, processing
	 * power, current location id, current iteration number and set of
	 * implemented elements. The interaction between the agent and the landscape
	 * is done by the simulator.
	 * 
	 * @param inf
	 *            an influence matrix object
	 * @param iterationPlan
	 *            a list of sets of element indices, and the sets are a
	 *            partition of all element indices [0, N-1]
	 * @param processingPower
	 *            a non-negative integer, which indicates the maximum number of
	 *            changes to a configuration
	 * @param type
	 *            a string, which represents the agent's type name
	 * @param totalNum
	 *            a non-negative integer, which represents the number of agents
	 *            with the same type
	 * @param isRefactoringAll
	 *            a boolean value, which determines the scope in refactoring
	 *            process
	 */
	public Agent(InfluenceMatrix inf,
			ArrayList<HashSet<Integer>> iterationPlan, int processingPower,
			String type, int totalNum, double constraint, boolean isExhaustive
	/*
	 * , boolean isRefactoringAll
	 */) {
		// check for valid iteration plan
		boolean flagArray[] = new boolean[inf.getN()];
		for (HashSet<Integer> iteration : iterationPlan) {
			for (int element : iteration) {
				// if (!flagArray[element]) {
				flagArray[element] = true;
				// }
				// else {
				// System.out
				// .println("ERROR invalid iteration plan : duplicated element "
				// + element);
				// System.exit(1);
				// }
			}
		}
		ArrayList<Integer> missingSet = new ArrayList<Integer>();
		for (int i = 0; i < flagArray.length; i++) {
			if (!flagArray[i]) {
				missingSet.add(i);
			}
		}
		if (!missingSet.isEmpty()) {
			System.out
					.println("ERROR invalid iteration plan : missing element(s) "
							+ missingSet.toString());
			System.exit(1);
		}
		// check for valid processing power
		if (processingPower <= 0) {
			System.out
					.println("ERROR processing power should be positive, given "
							+ processingPower);
			System.exit(1);
		}
		// check for valid constraint
		if (constraint <= 0) {
			System.out
					.println("ERROR constraint value should be positive, given "
							+ constraint);
			System.exit(1);
		}
		if (constraint > 1) {
			System.out
					.println("ERROR constraint value should be less than 1, given "
							+ constraint);
			System.exit(1);
		}
		// assign private fields
		this.myType = type;
		this.myTotalNum = totalNum;
		this.myNum = 0;

		this.myInf = inf;
		this.myIterationPlan = iterationPlan;
		this.myProcessingPower = processingPower;
		// set initial location id randomly
		this.myLocId = -1;
		this.myCurrentIterationNum = 0;
		this.myImplementedElements = new HashSet<Integer>();
		this.myUnimplementedElements = new HashSet<Integer>();
		for (int i = 0; i < myInf.getN(); i++) {
			this.myUnimplementedElements.add(i);
		}
		this.myContraint = constraint;
		this.myIsExhaustive = isExhaustive;
		// this.myIsRefactoringAll = isRefactoringAll;
	}

	/**
	 * Return the agent's type name.
	 * 
	 * @return the agent's type name
	 */
	public String getType() {
		return this.myType;
	}

	/**
	 * Return the agent's number.
	 * 
	 * @return the agent's number
	 */
	public int getNum() {
		return this.myNum;
	}

	/**
	 * Return the agent's current iteration number.
	 * 
	 * @return the agent's current iteration number
	 */
	public int getIterationNum() {
		return this.myCurrentIterationNum;
	}

	/**
	 * Return the agent's processing power
	 * 
	 * @return the agent's processing power
	 */
	public int getProcessingPower() {
		return this.myProcessingPower;
	}

	/**
	 * Return the agent's current location id.
	 * 
	 * @return the agent's current location id
	 */
	public int getLocId() {
		return this.myLocId;
	}

	/**
	 * Return the agent's constraint
	 * 
	 * @return the agent's constraint
	 */
	public double getConstraint() {
		return this.myContraint;
	}

	/**
	 * Return if the agent uses exhaustive search
	 * 
	 * @return if the agent uses exhaustive search
	 */
	public boolean IsExhaustive() {
		return this.myIsExhaustive;
	}

	/**
	 * Return true if all iterations are implemented already.
	 * 
	 * @return true if all iterations are implemented already
	 */
	public boolean isDone() {
		return this.myCurrentIterationNum == this.myIterationPlan.size();
	}

	/**
	 * Return the set of elements in the current iteration.
	 * 
	 * @return the set of elements in the current iteration
	 */
	public HashSet<Integer> getCurrentElements() {
		return new HashSet<Integer>(
				this.myIterationPlan.get(this.myCurrentIterationNum));
	}

	/**
	 * Return the set of all elements before the current iteration.
	 * 
	 * @return the set of all elements before the current iteration
	 */
	public HashSet<Integer> getImplementedElements() {
		return new HashSet<Integer>(this.myImplementedElements);
	}

	/**
	 * Return the complimentary set of myImplementedElements
	 * 
	 * @return the complimentary set of myImplementedElements
	 */
	public HashSet<Integer> getUnimplementedElements() {
		return new HashSet<Integer>(this.myUnimplementedElements);
	}

	// /**
	// * Return the set of all elements in the refactoring process
	// *
	// * @return the set of all elements in the refactoring process
	// */
	// public HashSet<Integer> getRefactoringElements() {
	// if (this.myIsRefactoringAll) {
	// // in the first iteration, there is no need to do refactoring on the
	// // same set again
	// if (this.myCurrentIterationNum == 0) {
	// return new HashSet<Integer>();
	// }
	// // otherwise, return all elements before and in the current
	// // iteration
	// HashSet<Integer> result = new HashSet<Integer>(
	// this.myImplementedElements);
	// result.addAll(this.myIterationPlan.get(this.myCurrentIterationNum));
	// return result;
	// }
	// return new HashSet<Integer>(this.myImplementedElements);
	// }

	/**
	 * Return true if the current number is smaller than the total number
	 * 
	 * @return true if the current number is smaller than the total number
	 */
	public boolean hasNextAgent() {
		return this.myNum < this.myTotalNum;
	}

	/**
	 * Increment the current number, initialize agent's location with a random
	 * position, set current iteration to 0, clear implemented elements set,
	 * restore unimplemented elements set
	 */
	public void nextAgent() {
		this.myNum++;
		this.myLocId = RandomGen.randomGen.nextInt(1 << this.myInf.getN());
		this.myCurrentIterationNum = 0;
		this.myImplementedElements.clear();
		this.myUnimplementedElements.clear();
		for (int i = 0; i < myInf.getN(); i++) {
			this.myUnimplementedElements.add(i);
		}
	}

	/**
	 * Reset the current number to be 0, initialize agent's location with a
	 * random position, set current iteration to 0, clear implemented elements
	 * set, restore unimplemented elements set
	 */
	public void reset() {
		this.myNum = 0;
		this.myLocId = RandomGen.randomGen.nextInt(1 << this.myInf.getN());
		this.myCurrentIterationNum = 0;
		this.myImplementedElements.clear();
		this.myUnimplementedElements.clear();
		for (int i = 0; i < myInf.getN(); i++) {
			this.myUnimplementedElements.add(i);
		}
	}

	/**
	 * Set the agent's current location id to a new location id
	 * 
	 * @param newLocId
	 *            a new location id
	 */
	public void updateLocId(int newLocId) {
		this.myLocId = newLocId;
	}

	/**
	 * Add the elements of the current iteration into the set of implemented
	 * elements, remove the elements of the current iteration from the set of
	 * unimplemented elements, then increase the current iteration number by 1
	 */
	public void moveToNextIteration() {
		this.myImplementedElements.addAll(this.myIterationPlan
				.get(this.myCurrentIterationNum));
		this.myUnimplementedElements.removeAll(this.myIterationPlan
				.get(this.myCurrentIterationNum));
		myCurrentIterationNum++;
	}

	/**
	 * Return a string representation of the agent
	 */
	@Override
	public String toString() {
		return "Type = " + this.myType + "\tNum = " + this.myTotalNum
				+ "\tNum = " + this.myNum + "\tpower = "
				+ this.myProcessingPower + "\tconstraint" + this.myContraint
				/*
				 * + "\trefactoring = " + this.myIsRefactoringAll
				 */
				+ "\tplan = " + this.myIterationPlan.toString();
	}
}
