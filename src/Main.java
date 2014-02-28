import simulation.Case;
import simulation.Simulator;
import util.ConfigReader;
import util.RandomGen;

import java.util.ArrayList;

public class Main {
	public static void main(String[] args) {
		if(args.length != 1){
			System.out.println("ERROR : invalid input, please input one xml config file");
			System.exit(1);
		}
		ArrayList<Case> cases = ConfigReader.read(args[0]);
		for (Case c : cases) {
			for (int i = 0; i < c.getRuns(); i++) {
				RandomGen.setSeed(i);
				Simulator s = new Simulator(c.getInf(), c.getAgentList(),
						c.getBias(), c.getDelta(), c.getTauList());
				s.startSimulation();
			}
		}
	}

}
