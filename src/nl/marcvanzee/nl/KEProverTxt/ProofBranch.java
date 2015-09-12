package nl.marcvanzee.nl.KEProverTxt;

import java.util.Vector;

public class ProofBranch {	
	// where the premises can represent nested conclusions again, or a string
	// that represents a formula.
	
	private Vector <ProofStep> proofSteps = new Vector<ProofStep>();
	private boolean isDC = false;
	private ProofBranch branch1, branch2;
	private int line;
	
	public ProofBranch() {
		
	}

	public void addProofStep(String formula, String stepDescription) {
		ProofStep step = new ProofStep(formula, stepDescription);
		this.proofSteps.add(step);
	}

	public void setBranches(ProofBranch branch1, ProofBranch branch2) {
		this.branch1 = branch1;
		this.branch2 = branch2;
		this.isDC = true;
	}

	public void print() {
		// print the branch, start at the end
		if (isDC) {
			branch1.print();
			branch2.print();
		}
		for (int i=proofSteps.size()-1; i>=0; i--) {
			proofSteps.get(i).print();
		}
	}
	
	public int size() {
		return proofSteps.size();
	}
	
	public void setLastDescription(String str) {
		proofSteps.get(proofSteps.size()-1).setDescription(str);
	}
	
	public void lineSeed(int line) {
		if (isDC) {
			branch1.lineSeed(line);
			branch2.lineSeed(branch1.getLine());
			line = branch2.getLine();
			
			int dc1 = branch1.getLine()-1;
			int dc2 = branch2.getLine()-1;
			ProofStep lastStep = proofSteps.get(proofSteps.size()-1);
			lastStep.setDescription(lastStep.getDescription() + ", " + dc1 + "," + dc2);
		}
		int i, j;
		for (i=proofSteps.size()-1, j=line; i>=0; i--, j++) {
			proofSteps.get(i).setLine(j);
		}
		
		this.line = j;
	}
	
	public int getLine() {
		return line;
	}
}
