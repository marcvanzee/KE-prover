package nl.marcvanzee.nl.KEProverTxt;

// a proofline represents a simple deductionstep from one formula

public class ProofStep {
	private String formula;
	private String stepDescription;
	private int line;
	
	public ProofStep(String formula, String stepDescription) {
		this.formula = formula;
		this.stepDescription = stepDescription;
	}
	
	public void print() {
		//((stepDescription == null) ? "" : " (" +
		String append = ((stepDescription == "axiom") || stepDescription.substring(0, 2).equals("dc"))  ? "" : ", " + (line-1);
		Sys.print(line + ". " + formula + "\t\t\t(" + stepDescription + append + ").");
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
