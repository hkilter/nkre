package util;

import simulation.Case;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.TreeSet;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.DocumentBuilder;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.w3c.dom.Node;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Element;
import org.w3c.dom.Attr;

import agent.Agent;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;

import landscape.InfluenceMatrix;

public class ConfigReader {
	/**
	 * Parse the input config xml file. Convert each case node into a case
	 * object and return a list of those case objects
	 * 
	 * @param xmlFileName
	 *            a string, which directs to the input config xml file
	 * @return a list of case objects
	 */
	public static ArrayList<Case> read(String xmlFileName) {
		ArrayList<Case> result = new ArrayList<Case>();
		try {
			File fXmlFile = new File(xmlFileName);
			DocumentBuilderFactory dbFactory = DocumentBuilderFactory
					.newInstance();
			DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
			Document doc = dBuilder.parse(fXmlFile);
			// reference -
			// http://www.mkyong.com/java/how-to-read-xml-file-in-java-dom-parser/
			// read this -
			// http://stackoverflow.com/questions/13786607/normalization-in-dom-parsing-with-java-how-does-it-work
			doc.getDocumentElement().normalize();
			NodeList caseList = doc.getElementsByTagName("case");

			for (int i = 0; i < caseList.getLength(); i++) {
				result.add(constructCase(caseList.item(i)));
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return result;
	}

	/**
	 * Convert a case node to a case object
	 * 
	 * @param caseNode
	 *            a node object, which represents a case node in the config xml
	 *            file
	 * @return a case object
	 */
	private static Case constructCase(Node caseNode) {
		int runs = -1;
		InfluenceMatrix inf = null;
		double bias = -1;
		double delta = -1;
		TreeSet<Integer> tauSet = new TreeSet<Integer>();
		ArrayList<Agent> agentList = new ArrayList<Agent>();

		NodeList settings = caseNode.getChildNodes();
		for (int i = 0; i < settings.getLength(); i++) {
			Node settingNode = settings.item(i);
			if (settingNode.getNodeType() == Node.ELEMENT_NODE) {
				Element setting = (Element) settingNode;
				if (setting.getTagName().equals("runs")) {
					runs = new Integer(setting.getTextContent().trim());
				} else if (setting.getTagName().equals("inf")) {
					inf = constructInf(setting.getTextContent().trim());
				} else if (setting.getTagName().equals("bias")) {
					bias = new Double(setting.getTextContent().trim());
				} else if (setting.getTagName().equals("delta")) {
					delta = new Double(setting.getTextContent().trim());
				} else if (setting.getTagName().equals("tau")) {
					tauSet.add(new Integer(setting.getTextContent().trim()));
				} else if (setting.getTagName().equals("agent")) {
					agentList.add(constructAgent(setting, inf));
				} else {
					System.out.println("WARNING : unknown case element "
							+ setting.getTagName());
				}
			}
		}
		return new Case(runs, inf, bias, delta, tauSet, agentList);
	}

	/**
	 * Return an influence matrix object according to the given file
	 * 
	 * @param infFileName
	 *            a string, which directs to the file of the influence matrix
	 * @return an influence matrix object according to the file indicated by the
	 *         given file name
	 */
	private static InfluenceMatrix constructInf(String infFileName) {
		// create inf matrix
		int matrix[][] = null;
		// start reading
		try {
			FileReader fRead = new FileReader(infFileName);
			BufferedReader bufRead = new BufferedReader(fRead);
			String line = bufRead.readLine();
			int lineCount = 0;
			while (line != null) {
				String tokens[] = line.split(",");
				if (matrix == null) { // first line
					matrix = new int[tokens.length][tokens.length];
				}
				for (int i = 0; i < tokens.length; i++) {
					matrix[lineCount][i] = (tokens[i].equals("x") ? 1 : 0);
				}
				line = bufRead.readLine();
				lineCount++;
			}
			fRead.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return new InfluenceMatrix(matrix);
	}

	/**
	 * Convert an agent node to an agent object
	 * 
	 * @param agentNode
	 *            a node object, which represents an agent node in the config
	 *            xml file
	 * @param inf
	 *            an influence matrix object
	 * @return a list of agent object
	 */
	private static Agent constructAgent(Node agentNode, InfluenceMatrix inf) {
		// parse the agent type
		String agentType = "";
		NamedNodeMap agentAttrs = agentNode.getAttributes();
		for (int i = 0; i < agentAttrs.getLength(); i++) {
			Node attrNode = agentAttrs.item(i);
			if (attrNode.getNodeType() == Node.ATTRIBUTE_NODE) {
				Attr attr = (Attr) attrNode;
				if (attr.getNodeName().equals("type")) {
					agentType = attr.getNodeValue();
				} else {
					System.out.println("WARNING : unknown agent attr "
							+ attr.getNodeName());
				}
			}
		}
		if (agentType.equals("")) {
			System.out.println("ERROR : agent type is missing");
			System.exit(1);
		}
		// parse the rest children nodes
		int num = -1;
		int power = -1;
		double constraint = -1;
		boolean isExhaustive = false;
		// boolean isRefactoringAll = false;
		ArrayList<HashSet<Integer>> plan = new ArrayList<HashSet<Integer>>();

		NodeList attrList = agentNode.getChildNodes();
		for (int i = 0; i < attrList.getLength(); i++) {
			Node attrNode = attrList.item(i);
			if (attrNode.getNodeType() == Node.ELEMENT_NODE) {
				Element attr = (Element) attrNode;
				if (attr.getTagName().equals("num")) {
					num = new Integer(attr.getTextContent().trim());
				} else if (attr.getTagName().equals("power")) {
					power = new Integer(attr.getTextContent().trim());
				} else if (attr.getTagName().equals("plan")) {
					plan = constructPlan(attr.getTextContent().trim());
				} else if (attr.getTagName().equals("constraint")) {
					constraint = new Double(attr.getTextContent().trim());
				} else if (attr.getTagName().equals("exhaustive")) {
					isExhaustive = attr.getTextContent().trim()
							.equalsIgnoreCase("true");
				}
				// else if (attr.getTagName().equals("refactoring")) {
				// isRefactoringAll = attr.getTextContent().trim()
				// .equalsIgnoreCase("true");
				// }
				else {
					System.out.println("WARNING : unknown agent attribute "
							+ attr.getTagName());
				}
			}
		}
		return new Agent(inf, plan, power, agentType, num, constraint,
				isExhaustive/*
							 * , isRefactoringAll
							 */);
	}

	/**
	 * Convert the given string to a list of sets of element indices.
	 * 
	 * @param plan
	 *            a string, which represents the iteration plan
	 * @return a list of sets of element indices
	 */
	private static ArrayList<HashSet<Integer>> constructPlan(String plan) {
		ArrayList<HashSet<Integer>> iterationPlan = new ArrayList<HashSet<Integer>>();
		String tokens[] = plan.split("\\)");
		for (int i = 0; i < tokens.length; i++) {
			HashSet<Integer> elements = new HashSet<Integer>();
			// separate elements in each iteration
			String tokens2[] = tokens[i].substring(1).split(",");
			for (int j = 0; j < tokens2.length; j++) {
				elements.add(new Integer(tokens2[j]));
				// if (!elements.add(new Integer(tokens2[j]))) {
				// System.out.println("WARNING: duplicated element "
				// + tokens2[j] + " in iteration " + i);
				// }
			}
			iterationPlan.add(elements);
		}
		return iterationPlan;
	}
}
