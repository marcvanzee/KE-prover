package nl.marcvanzee.KEProver.formula;

public class Subformula {
	/** whether this formula contains a literal. if not -> (formula) (connective) (formula) */
	private boolean literal;
	/** if the formula contains a literal: this is the literal */
	private String var;
	/** if the formula does not contain a literal, this is the connective */
	private char connective;
	/** number of negations before subformula: numNegations=3 => ---(SubFormula) */
	private int negs = 0;
	private Subformula sub1, sub2;
	
	//==========================================================
	//====================== CONSTRUCTORS ======================
	//==========================================================
	
	/**
	 * create a new instance of this class, by cloning the subformula in the argument
	 */
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
	
	public Subformula(String var, int negs) {
		literal = true;
		this.var = var;
		this.negs = negs;
	}

	public Subformula(Subformula sub1, char connective, Subformula sub2, int negs) {
		literal = false;
		this.sub1 = sub1;
		this.sub2 = sub2;
		this.connective = connective;
		this.negs = negs;
	}
	
	/**
	 * create a new instance of this class, by cloning the subformula in the argument<br>
	 * also, add one negation to the subformula is <code>negate</code> is false;
	 * @param sub the subformula to clone
	 * @param negate if this is <code>false</code>, a negation is added to the subformula
	 */
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
		this.negs = !negate ? sub.getNegs()+1 : sub.getNegs();
	}
	
	//==========================================================
	//=========================== METHODS ======================
	//==========================================================
	
	/**
	 * clone an instance of this class and return it
	 */
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
	
	/**
	 * put the subformula in a string and return the string
	 * @return string containing the subformula
	 */
	public String print() {
		String negs = "";
		for (int i=0; i<this.negs; i++)	negs += '-';
		if (literal) {
			return negs + var;
		} else {
			return negs + "(" + sub1.print() + connective + sub2.print() + ")";
		}
	}
	
	/**
	 * print this subformula with as few negations as possible.
	 * is used when printing the result of a DC()
	 * @return string with stripped subformula
	 */
	public String printStripped() {
		boolean negated = ((this.negs % 2) == 0) ? false : true;
		
		if (literal) {
			return (negated ? "-" : "" ) + var;
		} else {
			return (negated ? "=" : "" ) + "(" + sub1.print() + connective + sub2.print() + ")";
		}
	}
	
	//==========================================================
	//==================== METHODS, GETTERS/SETTERS ============
	//==========================================================
	
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
	
	public boolean isNegated() {
		return ((negs % 2) == 1);
	}
}