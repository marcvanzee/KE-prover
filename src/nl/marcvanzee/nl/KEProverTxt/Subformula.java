package nl.marcvanzee.nl.KEProverTxt;

public class Subformula {
	private boolean literal; 		// whether this formula contains a literal. if not -> (formula) (connective) (formula)
	private String var;				// if the formula contains a literal: this is the literal
	private char connective;		// the connective when not
	private int negs = 0;			// number of negations before subformula: numNegations=3 => ---(SubFormula)
	private Subformula sub1, sub2;	// the two subformula 
	
	// use these flags to speed things up a little
	//private boolean A_REDUCED = false;
	//private boolean B_REDUCED = false;
	//private boolean E_REDUCED = false;
	
	public Subformula(String var) {
		literal = true;
		this.var = var;
	}
	
	public Subformula(String var, int negs) {
		literal = true;
		this.var = var;
		this.negs = negs;
	}
	
	public Subformula(Subformula sub1, char connective, Subformula sub2) {
		literal = false;
		this.sub1 = sub1;
		this.sub2 = sub2;
		this.connective = connective;
	}

	public Subformula(Subformula sub1, char connective, Subformula sub2, int negs) {
		literal = false;
		this.sub1 = sub1;
		this.sub2 = sub2;
		this.connective = connective;
		this.negs = negs;
	}
	
	public Subformula(Subformula sub) {
		if (!sub.isLiteral()) {
			literal = false;
			sub1 = sub.getSub1().clone();
			sub2 = sub.getSub2().clone();
			connective = sub.connective;
		} else {
			literal = true;
			var = sub.getVar();
		}
		negs = sub.getNegs();
	}
	
	public Subformula(Subformula sub, int negs) {
		if (!sub.isLiteral()) {
			literal = false;
			sub1 = sub.getSub1().clone();
			sub2 = sub.getSub2().clone();
			connective = sub.connective;
		} else {
			literal = true;
			var = sub.getVar();
		}
		this.negs = negs;
	}
	
	public Subformula(Subformula sub, boolean negate) {
		if (!sub.isLiteral()) {
			literal = false;
			sub1 = sub.getSub1().clone();
			sub2 = sub.getSub2().clone();
			connective = sub.connective;
		} else {
			literal = true;
			var = sub.getVar();
		}
		this.negs = sub.getNegs()+1;
	}
	
	public String print() {
		String negs = "";
		for (int i=0; i<this.negs; i++)	negs += '-';
		if (literal) {
			return negs + var;
		} else {
			return negs + "(" + sub1.print() + connective + sub2.print() + ")";
		}
	}
	
	public String printStripped() {
		boolean negated = ((this.negs % 2) == 0) ? false : true;
		
		if (literal) {
			return (negated ? "-" : "" ) + var;
		} else {
			return (negated ? "=" : "" ) + "(" + sub1.print() + connective + sub2.print() + ")";
		}
	}
		
	public int getNegs() {
		return negs;
	}
	
	public char getConnective() {
		return connective;
	}
	
	public Subformula getSub1() {
		if (!literal) {
			return sub1;
		} else {
			return null;
		}
	}
	
	public Subformula getSub2() {
		if (!literal) {
			return sub2;
		} else {
			return null;
		}
	}
	
	public String getVar() {
		return var;
	}
	
	public boolean isLiteral() {
		return literal;
	}
	
	public void setNegs(int negs) {
		this.negs = negs;
	}
	
	public Subformula clone() {
		Subformula temp;
		if (literal) {
			temp = new Subformula(var, negs);
		} else {
			temp = new Subformula(sub1.clone(), connective, sub2.clone(), negs);
		}
		
		return temp;
	}

	public int length() {
		if (literal) {
			// add one for the var
			return negs + 1;
		} else {
			// add one for the connective
			return negs + 1 + sub1.length() + sub2.length();
		}
	}
	
	public boolean isNegated() {
		return ((negs % 2) == 1);
	}
}