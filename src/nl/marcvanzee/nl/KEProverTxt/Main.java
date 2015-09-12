package nl.marcvanzee.nl.KEProverTxt;

import java.util.Vector;

public class Main {
	private boolean start() {
		Vector <Subformula> sequents = new Vector<Subformula>();
		Parser parser = new Parser();
		
		System.out.println("Enter formulas, enter \".\" when finished : ");
		sequents = parser.readLines('.');
		
		if (sequents.size() < 1) {
			return false;
		}
		// load sequents into the refuter SAKE
		SAKE sake = new SAKE(sequents);
		Sys.setDebug(true); // print results to screen
		
		// TODO: sake.useHeuristic(0);

		if (!sake.isRefutable()) {
			sake.printProofTree();
		}
		
		return true;
	}
	
	public static void main(String[] args) {
		Main main = new Main();
		if (!main.start()) {
			Sys.debug("(EXITING) no valid sequences in input");
		}
	}
}