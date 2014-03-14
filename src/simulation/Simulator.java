package simulation;

import landscape.*;
import agent.*;
import util.*;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.lang.Math;

public class Simulator {
	private InfluenceMatrix myInf;
	private double myBias;
	private double myDelta;
	private FitnessContributionTable myFit; // [0, 1]
	private Landscape myLandscapeTable[][]; // (totalShockNum + 1) x (N + 1)
	private ArrayList<Integer> myTauList;

	private ArrayList<Agent> myAgentList;
	private Agent myCurrentAgent;
	private int myCurrentTime;
	private int myCurrentShock;

	// private boolean myIsRefactoring;

	/**
	 * Firstly generate fitness contribution table based on the given delta and
	 * tau list. Then generate a 2-dimensional table of landscapes according the
	 * fitness contribution table. Dimension 1 represents shock numbers.
	 * Dimension 2 represents the number of implemented elements.
	 * 
	 * @param inf
	 *            an influence matrix object
	 * @param agents
	 *            a list of agent objects
	 * @param bias
	 *            a value between 0 and 1, which is the initial uncertainty
	 *            level, 0 for no uncertainty and 1 for full uncertainty
	 * @param delta
	 *            a value between 0 and 1, which determines the amount of
	 *            changes in/after each shock, 0 for no changes and 1 for
	 *            arbitrary changes
	 * @param tauList
	 *            a list of shock times
	 */
	public Simulator(InfluenceMatrix inf, ArrayList<Agent> agents, double bias,
			double delta, ArrayList<Integer> tauList) {
		this.myInf = inf;
		this.myAgentList = agents;
		this.myBias = bias;
		this.myDelta = delta;
		this.myTauList = tauList;
		// generate fitness contribution table
		this.myFit = new FitnessContributionTable(this.myInf, this.myDelta,
				this.getTotalShockNum());
		// generate landscapes
		this.myLandscapeTable = new Landscape[this.getTotalShockNum() + 1][this.myInf
				.getN() + 1];
		// for each shock
		for (int i = 0; i < this.myLandscapeTable.length; i++) {
			// for each step
			for (int j = 0; j < this.myLandscapeTable[0].length; j++) {
				this.myLandscapeTable[i][j] = new Landscape(this.myInf,
						this.myBias, i, j, this.myFit);
			}
		}
		// the same set of agents may run multiple times for different seeds
		// therefore, we need to reset the agents
		for (Agent agt : this.myAgentList) {
			agt.reset();
		}
	}

	/**
	 * Take every agent in the agent list to interact with the landscape. The
	 * simulation starts from the agent's initial location and starts
	 * implementation according to its iteration plan. After each iteration, the
	 * agent's uncertainty value reduces. If a shock occurs during one
	 * iteration, after that iteration, in addition to the change of uncertainty
	 * value, the final (after full implementation) landscape also changes.
	 * During each iteration, the agent firstly explores w.r.t. the elements in
	 * the current iteration with the rest elements fixed. Then the agent
	 * explores w.r.t. the elements before the current iteration with the rest
	 * elements (including the elements in the current iteration) fixed.
	 * 
	 * The fitness value after each exploration step is recorded in an excel
	 * sheet. One agent occupies one column.
	 */
	public void startSimulation() {
		for (Agent agt : this.myAgentList) {
			// the first agent in the case
			if (this.myCurrentAgent == null) {
				this.myCurrentAgent = agt;
				OutputWriter.setOutputFile(this.constructOutputFileName());
			}
			// the following agent
			else {
				OutputWriter.close();
				this.myCurrentAgent = agt;
				OutputWriter.setOutputFile(this.constructOutputFileName());
			}

			while (agt.hasNextAgent()) {
				this.myCurrentTime = 0;
				while (!this.myCurrentAgent.isDone()) {
					// detect new shock
					if (this.myCurrentTime > this.myTauList
							.get(this.myCurrentShock)) {
						this.myCurrentShock++;
					}
					// write log for initial config
					OutputWriter.writeLine(this.constructOutputFileLine());
					this.myCurrentTime++;
					// choose exploration strategy accordingly
					if (this.myCurrentAgent.isAveraging()) {
						if (this.myCurrentAgent.isExhaustive()) {
							// exhaustive explore
							this.exhaustiveExploreAveraging(this.myCurrentAgent
									.getImplementedElements(),
									this.myCurrentAgent.getCurrentElements(),
									this.myCurrentAgent
											.getUnimplementedElements());
						} else {
							// random explore
							this.randomExploreAveraging(this.myCurrentAgent
									.getImplementedElements(),
									this.myCurrentAgent.getCurrentElements(),
									this.myCurrentAgent
											.getUnimplementedElements());
						}
					} else {
						if (this.myCurrentAgent.isExhaustive()) {
							// exhaustive explore
							this.exhaustiveExploreNonAveraging(this.myCurrentAgent
									.getCurrentElements());
						} else {
							// random explore
							this.randomExploreNonAveraging(this.myCurrentAgent
									.getCurrentElements());
						}
					}
					// explore on current elements
					/*
					 * this.myIsRefactoring = false;
					 * this.randomExplore(this.myCurrentAgent
					 * .getCurrentElements());
					 */
					// explore on implemented elements
					/*
					 * this.myIsRefactoring = true;
					 * this.randomExplore(this.myCurrentAgent
					 * .getRefactoringElements());
					 */
					// next iteration
					this.myCurrentAgent.moveToNextIteration();
				}
				// next agent within the same type
				agt.nextAgent();
			}
		}
		OutputWriter.close();
	}

	/**
	 * Return the total number of shocks, i.e. the size of the set of tau list
	 * 
	 * @return the total number of shocks
	 */
	private int getTotalShockNum() {
		return this.myTauList.size();
	}

	/**
	 * Return a string which is the output file name.
	 * 
	 * @return a string which is the output file name
	 */
	private String constructOutputFileName() {
		return "o_n"
				+ this.myInf.getN()
				+ "k"
				+ this.myInf.getK()
				+ "_b"
				+ ("" + this.myBias)
				+ "d"
				+ ("" + this.myDelta)
				+ "c"
				+ ("" + this.myCurrentAgent.getConstraint())
				+ "_"
				+ (this.myCurrentAgent.isAveraging() ? "averaging"
						: "nonAveraging")
				+ "_"
				+ (this.myCurrentAgent.isExhaustive() ? "exhaustive" : "random")
				+ "_" + this.myCurrentAgent.getType() + ".txt";
	}

	/**
	 * An exploration step is to randomly select an unvisited neighbouring
	 * configuration/location. If the average fitness value of the new
	 * configuration/location is better than the current, the agent updates its
	 * configuration/location to the new one. This exploration step repeats
	 * until there is no more unvisited neighbouring configurations/locations.
	 * This method records each exploration step into a file.
	 * 
	 * @param implementedElements
	 *            a set of element indices, which indicates the positions of the
	 *            implemented elements
	 * @param implementingElements
	 *            a set of element indices, which indicates the positions of the
	 *            elements in the current iteration
	 * @param unimplementedElements
	 *            a set of element indices, which indicates the positions of the
	 *            unimplemented elements
	 */
	private void randomExploreAveraging(HashSet<Integer> implementedElements,
			HashSet<Integer> implementingElements,
			HashSet<Integer> unimplementedElements) {
		// get the set of implemented elements including the elements in the
		// current iteration, for exploration
		implementedElements.addAll(implementingElements);
		// get the set of unimplemented elements excluding the current elements
		// in the current iteration, for average
		unimplementedElements.removeAll(implementingElements);
		// create visited set
		HashSet<Integer> visitedLocIds = new HashSet<Integer>();
		// put agent's current location into the visited set
		visitedLocIds.add(this.myCurrentAgent.getLocId());
		// get current landscape
		Landscape ldscp = this.myLandscapeTable[this.myCurrentShock][this.myCurrentAgent
				.getImplementedElements().size()];
		// compute averaging score for agent's current location
		HashSet<Integer> unimplementedNeighbours = ldscp
				.getNeighboursInclusive(this.myCurrentAgent.getLocId(),
						unimplementedElements, unimplementedElements.size());
		double currentAverageScore = 0;
		for (Integer unimplementedNeighbour : unimplementedNeighbours) {
			currentAverageScore += ldscp
					.getScoreOfLocId(unimplementedNeighbour);
		}
		currentAverageScore /= unimplementedNeighbours.size();
		// get neighbour set of the agent's current location
		HashSet<Integer> implementingNeighbours = ldscp.getNeighboursInclusive(
				this.myCurrentAgent.getLocId(), implementingElements,
				this.myCurrentAgent.getProcessingPower());
		// from the neighbour set, remove all the visited locations, to end up
		// with unvisited neighbour set
		implementingNeighbours.removeAll(visitedLocIds);
		// deal with constraint
		int numToTry = (int) Math.ceil(this.myCurrentAgent.getConstraint()
				* implementingNeighbours.size());
		int numTried = 0;
		while (numTried < numToTry && !implementingNeighbours.isEmpty()) {
			// pick one candidate randomly
			int candidateNeighbour = -1;
			int candidateIdx = RandomGen.randomGen
					.nextInt(implementingNeighbours.size());
			Iterator<Integer> itr = implementingNeighbours.iterator();
			for (int i = 0; i <= candidateIdx; i++) {
				candidateNeighbour = itr.next();
			}
			// put the candidate in to visited set
			visitedLocIds.add(candidateNeighbour);
			numTried++;
			// compute averaging score for candidate location
			double candidateAverageScore = 0;
			for (Integer unimplementedNeighbour : unimplementedNeighbours) {
				candidateAverageScore += ldscp.getScoreOfLocId(ldscp
						.changeElements(unimplementedNeighbour,
								candidateNeighbour, implementedElements));
			}
			candidateAverageScore /= unimplementedNeighbours.size();
			// compare and pick the better one
			if (candidateAverageScore >= currentAverageScore) {
				this.myCurrentAgent.updateLocId(candidateNeighbour);
				currentAverageScore = candidateAverageScore;
				implementingNeighbours = ldscp.getNeighboursInclusive(
						this.myCurrentAgent.getLocId(), implementingElements,
						this.myCurrentAgent.getProcessingPower());
				implementingNeighbours.removeAll(visitedLocIds);
				numTried = 0;
			} else {
				implementingNeighbours.remove(candidateNeighbour);
			}
			OutputWriter.writeLine(this.constructOutputFileLine());
			this.myCurrentTime++;
		}
	}

	/**
	 * An exploration step is to randomly select an unvisited neighbouring
	 * configuration/location. If the fitness value of the new
	 * configuration/location is better than the current, the agent updates its
	 * configuration/location to the new one. This exploration step repeats
	 * until there is no more unvisited neighbouring configurations/locations.
	 * This method records each exploration step into a file.
	 * 
	 * @param implementingElements
	 *            a set of element indices, which indicates the positions of the
	 *            changeable elements
	 */
	private void randomExploreNonAveraging(HashSet<Integer> implementingElements) {
		// create visited set
		HashSet<Integer> visitedLocIds = new HashSet<Integer>();
		// put agent's current location into the visited set
		visitedLocIds.add(this.myCurrentAgent.getLocId());
		// get neighbour set of the agent's current location
		Landscape ldscp = this.myLandscapeTable[this.myCurrentShock][this.myCurrentAgent
				.getImplementedElements().size()];
		HashSet<Integer> implementingNeighbours = ldscp.getNeighboursInclusive(
				this.myCurrentAgent.getLocId(), implementingElements,
				this.myCurrentAgent.getProcessingPower());
		// from the neighbour set, remove all the visited locations, to end up
		// with unvisited neighour set
		implementingNeighbours.removeAll(visitedLocIds);

		while (!implementingNeighbours.isEmpty()) {
			// pick one candidate randomly
			int candidateNeighbour = -1;
			int candidateIdx = RandomGen.randomGen
					.nextInt(implementingNeighbours.size());
			Iterator<Integer> itr = implementingNeighbours.iterator();
			for (int i = 0; i <= candidateIdx; i++) {
				candidateNeighbour = itr.next();
			}
			// put the candidate in to visited set
			visitedLocIds.add(candidateNeighbour);
			// compare and pick the better one
			if (ldscp.getScoreOfLocId(candidateNeighbour) >= ldscp
					.getScoreOfLocId(this.myCurrentAgent.getLocId())) {
				this.myCurrentAgent.updateLocId(candidateNeighbour);
				implementingNeighbours = ldscp.getNeighboursInclusive(
						this.myCurrentAgent.getLocId(), implementingElements,
						this.myCurrentAgent.getProcessingPower());
				implementingNeighbours.removeAll(visitedLocIds);
			} else {
				implementingNeighbours.remove(candidateNeighbour);
			}
			OutputWriter.writeLine(this.constructOutputFileLine());
			this.myCurrentTime++;
		}
	}

	/**
	 * An exploration step is to greedily select the best unvisited neighbouring
	 * configuration/location. Then the agent updates its configuration/location
	 * to the new one. This exploration step repeats until the current
	 * configuration/location is the local best. This method records each
	 * exploration step into a file.
	 * 
	 * @param implementedElements
	 *            a set of element indices, which indicates the positions of the
	 *            implemented elements
	 * @param implementingElements
	 *            a set of element indices, which indicates the positions of the
	 *            elements in the current iteration
	 * @param unimplementedElements
	 *            a set of element indices, which indicates the positions of the
	 *            unimplemented elements
	 */
	private void exhaustiveExploreAveraging(
			HashSet<Integer> implementedElements,
			HashSet<Integer> implementingElements,
			HashSet<Integer> unimplementedElements) {
		// get the set of implemented elements including the elements in the
		// current iteration, for exploration
		implementedElements.addAll(implementingElements);
		// get the set of unimplemented elements excluding the current elements
		// in the current iteration, for average
		unimplementedElements.removeAll(implementingElements);
		// create visited set
		HashSet<Integer> visitedLocIds = new HashSet<Integer>();
		// put agent's current location into the visited set
		visitedLocIds.add(this.myCurrentAgent.getLocId());
		// get current landscape
		Landscape ldscp = this.myLandscapeTable[this.myCurrentShock][this.myCurrentAgent
				.getImplementedElements().size()];
		// compute averaging score for agent's current location
		HashSet<Integer> unimplementedNeighbours = ldscp
				.getNeighboursInclusive(this.myCurrentAgent.getLocId(),
						unimplementedElements, unimplementedElements.size());
		double currentAverageScore = 0;
		for (Integer unimplementedNeighbour : unimplementedNeighbours) {
			currentAverageScore += ldscp
					.getScoreOfLocId(unimplementedNeighbour);
		}
		currentAverageScore /= unimplementedNeighbours.size();
		// get neighbour set of the agent's current location
		HashSet<Integer> implementingNeighbours = ldscp.getNeighboursInclusive(
				this.myCurrentAgent.getLocId(), implementingElements,
				this.myCurrentAgent.getProcessingPower());
		// from the neighbour set, remove all the visited locations, to end up
		// with unvisited neighbour set
		implementingNeighbours.removeAll(visitedLocIds);
		// deal with constraint
		int numToTry = (int) Math.ceil(this.myCurrentAgent.getConstraint()
				* implementingNeighbours.size());
		int numTried = 0;
		boolean foundBetter = false;
		do {
			// if found better config before, then update the neighouring set
			if (foundBetter) {
				implementingNeighbours = ldscp.getNeighboursInclusive(
						this.myCurrentAgent.getLocId(), implementingElements,
						this.myCurrentAgent.getProcessingPower());
				implementingNeighbours.removeAll(visitedLocIds);
				numTried = 0;
				OutputWriter.writeLine(this.constructOutputFileLine());
				this.myCurrentTime++;
			}
			// reset foundBetter flag
			foundBetter = false;
			// loop to find best config among current neighbours
			while (numTried < numToTry && !implementingNeighbours.isEmpty()) {
				// pick one candidate randomly
				int candidateNeighbour = -1;
				int candidateIdx = RandomGen.randomGen
						.nextInt(implementingNeighbours.size());
				Iterator<Integer> itr = implementingNeighbours.iterator();
				for (int i = 0; i <= candidateIdx; i++) {
					candidateNeighbour = itr.next();
				}
				// put the candidate in to visited set and remove it from
				// neighbouring set
				visitedLocIds.add(candidateNeighbour);
				implementingNeighbours.remove(candidateNeighbour);
				numTried++;
				// compute averaging score for candidate location
				double candidateAverageScore = 0;
				for (Integer unimplementedNeighbour : unimplementedNeighbours) {
					candidateAverageScore += ldscp.getScoreOfLocId(ldscp
							.changeElements(unimplementedNeighbour,
									candidateNeighbour, implementedElements));
				}
				candidateAverageScore /= unimplementedNeighbours.size();
				// compare and pick the better one, but not update the
				// neighbouring set
				if (candidateAverageScore >= currentAverageScore) {
					this.myCurrentAgent.updateLocId(candidateNeighbour);
					currentAverageScore = candidateAverageScore;
					foundBetter = true;
				}
			}
		} while (foundBetter);
	}

	/**
	 * An exploration step is to greedily select the best unvisited neighbouring
	 * configuration/location. Then the agent updates its configuration/location
	 * to the new one. This exploration step repeats until the current
	 * configuration/location is the local best. This method records each
	 * exploration step into a file.
	 * 
	 * @param implementingElements
	 *            a set of element indices, which indicates the positions of the
	 *            elements in the current iteration
	 */
	private void exhaustiveExploreNonAveraging(
			HashSet<Integer> implementingElements) {
		// create visited set
		HashSet<Integer> visitedLocIds = new HashSet<Integer>();
		// put agent's current location into the visited set
		visitedLocIds.add(this.myCurrentAgent.getLocId());
		// get current landscape
		Landscape ldscp = this.myLandscapeTable[this.myCurrentShock][this.myCurrentAgent
				.getImplementedElements().size()];
		// get neighbour set of the agent's current location
		HashSet<Integer> implementingNeighbours = ldscp.getNeighboursInclusive(
				this.myCurrentAgent.getLocId(), implementingElements,
				this.myCurrentAgent.getProcessingPower());
		// from the neighbour set, remove all the visited locations, to end up
		// with unvisited neighbour set
		implementingNeighbours.removeAll(visitedLocIds);
		// deal with constraint
		int numToTry = (int) Math.ceil(this.myCurrentAgent.getConstraint()
				* implementingNeighbours.size());
		int numTried = 0;
		boolean foundBetter = false;
		do {
			// if found better config before, then update the neighouring set
			if (foundBetter) {
				implementingNeighbours = ldscp.getNeighboursInclusive(
						this.myCurrentAgent.getLocId(), implementingElements,
						this.myCurrentAgent.getProcessingPower());
				implementingNeighbours.removeAll(visitedLocIds);
				numTried = 0;
				OutputWriter.writeLine(this.constructOutputFileLine());
				this.myCurrentTime++;
			}
			// reset foundBetter flag
			foundBetter = false;
			// loop to find best config among current neighbours
			while (numTried < numToTry && !implementingNeighbours.isEmpty()) {
				// pick one candidate randomly
				int candidateNeighbour = -1;
				int candidateIdx = RandomGen.randomGen
						.nextInt(implementingNeighbours.size());
				Iterator<Integer> itr = implementingNeighbours.iterator();
				for (int i = 0; i <= candidateIdx; i++) {
					candidateNeighbour = itr.next();
				}
				// put the candidate in to visited set and remove it from
				// neighbouring set
				visitedLocIds.add(candidateNeighbour);
				implementingNeighbours.remove(candidateNeighbour);
				numTried++;
				// compare and pick the better one, but not update the
				// neighbouring set
				if (ldscp.getScoreOfLocId(candidateNeighbour) >= ldscp
						.getScoreOfLocId(this.myCurrentAgent.getLocId())) {
					this.myCurrentAgent.updateLocId(candidateNeighbour);
					foundBetter = true;
				}
			}
		} while (foundBetter);
	}

	/**
	 * Return a string which is one record in the exploration step.
	 * 
	 * @return a string which is one record in the exploration step
	 */
	private String constructOutputFileLine() {
		return RandomGen.getSeed()
				+ "\t"
				+ this.myCurrentAgent.getNum()
				+ "\t"
				+ this.myCurrentTime
				+ "\t"
				+ this.myCurrentShock
				+ "\t"
				+ this.myCurrentAgent.getIterationNum()
				+ "\t"
				/*
				 * + this.myIsRefactoring + "\t"
				 */
				// score
				+ this.myLandscapeTable[this.myCurrentShock][this.myInf.getN()]
						.getScoreOfLocId(this.myCurrentAgent.getLocId())
				+ "\t"
				// global maximum in the current shock
				+ this.myLandscapeTable[this.myCurrentShock][this.myInf.getN()]
						.getMax()
				+ "\t"
				// global minimum in the current shock
				+ this.myLandscapeTable[this.myCurrentShock][this.myInf.getN()]
						.getMin();
	}

	public static void main(String args[]) {
		if (args.length != 3) {
			System.out
					.println("ERROR : invalid input, please input as follows");
			System.out.println("config.xml shockNum stepNum");
			System.exit(1);
		}
		String xmlFileName = args[0];
		int shockNum = new Integer(args[1]);
		int stepNum = new Integer(args[2]);
		ArrayList<Case> cases = ConfigReader.read(xmlFileName);
		Case c = cases.get(0);
		RandomGen.setSeed(c.getRuns());
		Simulator s = new Simulator(c.getInf(), c.getAgentList(), c.getBias(),
				c.getDelta(), c.getTauList());
		if (shockNum >= s.getTotalShockNum() || shockNum < 0) {
			System.out
					.println("ERROR : invalid shock number, please input value within [0, "
							+ (s.getTotalShockNum() - 1) + "] inclusive");
			System.exit(1);
		}
		if (stepNum >= s.myInf.getN() || stepNum < 0) {
			System.out
					.println("ERROR : invalid step number, please input value within [0, "
							+ (s.myInf.getN() - 1) + "] inclusive");
			System.exit(1);
		}
		s.myLandscapeTable[shockNum][stepNum].printTableContents();
	}
}
