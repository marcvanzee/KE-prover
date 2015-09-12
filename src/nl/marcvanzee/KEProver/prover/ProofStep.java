package nl.marcvanzee.KEProver.prover;

/**
 * a ProofStep represents a simple deductionstep
 * @author marc
 *
 */

public class ProofStep {
	/** the formula that is derived */
	private String formula;
	/** the description of the previous deduction step */
	private String stepDescription;
	/** the line number we are on, this is used when printing the final proof */
	private int line;
	
	public ProofStep(String formula, String stepDescription) {
		this.formula = formula;
		this.stepDescription = stepDescription;
	}
	
	/**
	 * turn this step into a string and return it
	 */
	public String toString() {
		String append = ((stepDescription == "axiom") || stepDescription.substring(0, 2).equals("dc"))  ? "" : ", " + (line-1);
		return line + ". " + formula + "\t\t\t(" + stepDescription + append + ").";
	}
	
	public void setDescription(String description) {
		stepDescription = description;
	}
	
	public int getLine() {
		return line;
	}
	
	public void setLine(int line) {
		this.line = line;
	}
	
	public String getDescription() {
		return stepDescription;
	}
}
