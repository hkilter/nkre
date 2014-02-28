package simulation;

import java.util.ArrayList;
import java.util.TreeSet;

import agent.Agent;
import landscape.InfluenceMatrix;

public class Case {
	private int myRuns;
	private InfluenceMatrix myInf;
	private double myBias;
	private double myDelta;
	private ArrayList<Integer> myTauList;
	private ArrayList<Agent> myAgentList;

	/**
	 * Create a new Case object with the given parameters.
	 * 
	 * @param runs
	 *            an integer, which indicates the total number of runs, each run
	 *            takes a different seed
	 * @param inf
	 *            an influence matrix object
	 * @param bias
	 *            a value between 0 and 1, which is the initial uncertainty
	 *            level, 0 for no uncertainty and 1 for full uncertainty
	 * @param delta
	 *            a value between 0 and 1, which determines the amount of
	 *            changes in/after each shock, 0 for no changes and 1 for
	 *            arbitrary changes
	 * @param tauList
	 *            a list of shock times
	 * @param agentList
	 *            a list of agent objects
	 */
	public Case(int runs, InfluenceMatrix inf, double bias, double delta,
			TreeSet<Integer> tauList, ArrayList<Agent> agentList) {
		this.myRuns = runs;
		this.myInf = inf;
		this.myBias = bias;
		this.myDelta = delta;
		this.myTauList = new ArrayList<Integer>(tauList);
		this.myAgentList = agentList;
	}

	/**
	 * Return the total number of runs.
	 * 
	 * @return the total number of runs
	 */
	public int getRuns() {
		return this.myRuns;
	}

	/**
	 * Return a copy of the influence matrix object.
	 * 
	 * @return a copy of the influence matrix object
	 */
	public InfluenceMatrix getInf() {
		return new InfluenceMatrix(this.myInf);
	}

	/**
	 * Return the bias value.
	 * 
	 * @return the bias value
	 */
	public double getBias() {
		return this.myBias;
	}

	/**
	 * Return the delta value.
	 * 
	 * @return the delta value
	 */
	public double getDelta() {
		return this.myDelta;
	}

	/**
	 * Return the list of shock times.
	 * 
	 * @return the list of shock times
	 */
	public ArrayList<Integer> getTauList() {
		return new ArrayList<Integer>(this.myTauList);
	}

	/**
	 * Return the list of agent objects.
	 * 
	 * @return the list of agent objects
	 */
	public ArrayList<Agent> getAgentList() {
		return new ArrayList<Agent>(this.myAgentList);
	}

	/**
	 * Return a string representation of the Case object.
	 */
	@Override
	public String toString() {
		String result = "runs = " + this.myRuns + "\ninf = \n" + this.myInf
				+ "\nbias = " + this.myBias + "\ndelta = " + this.myDelta
				+ "\nTau = " + this.myTauList + "\nAgents = ";
		for (Agent agt : this.myAgentList) {
			result += ("\n" + agt);
		}
		return result;
	}
}
