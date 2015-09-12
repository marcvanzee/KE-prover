package nl.marcvanzee.KEProver.prover;

import java.util.Vector;

/**
 * this is the class that represents the proof tree, which can be printed after
 * a closed tree has just been proven, meaning that every end of the branch has
 * ended in a contradiction.
 * 
 * @author marc
 *
 */
public class ProofBranch {	
	/** the set of ProofSteps that this branch consists out of */
	private Vector <ProofStep> proofSteps = new Vector<ProofStep>();
	/** whether this branch ends with a DC */
	private boolean hasDC = false;
	/** if this branch does end with DC, these two branches represent the branches after the DC */
	private ProofBranch branch1, branch2;
	/** the line of proof, needed when printing the proof */
	private int line;
	
	//==========================================================
	//====================== CONSTRUCTOR =======================
	//==========================================================
	
	public ProofBranch() {
	}

	//==========================================================
	//========================= METHODS ========================
	//==========================================================
	
	public void addProofStep(String formula, String stepDescription) {
		ProofStep step = new ProofStep(formula, stepDescription);
		this.proofSteps.add(step);
	}

	public void setBranches(ProofBranch branch1, ProofBranch branch2) {
		this.branch1 = branch1;
		this.branch2 = branch2;
		this.hasDC = true;
	}

	
	public void setLastDescription(String str) {
		proofSteps.get(proofSteps.size()-1).setDescription(str);
	}
	
	public void lineSeed(int line) {
		if (hasDC) {
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
	
	//==========================================================
	//==================== METHODS, GETTERS ====================
	//==========================================================
	
	public int getLine() {
		return line;
	}
	
	public boolean getDC() {
		return hasDC;		
	}
	
	public Vector <ProofStep> getProofSteps() {
		return proofSteps;
	}
	
	public String toString() {
		String str = "";
		// print the branch, start at the end
		if (hasDC) {
			str += branch1.toString() + "\n";
			str += branch2.toString() + "\n";
		}
		for (int i=proofSteps.size()-1; i>=0; i--) {
			str += proofSteps.get(i).toString() + "\n";
		}
		
		return str;
	}
	
	public int size() {
		return proofSteps.size();
	}
}
