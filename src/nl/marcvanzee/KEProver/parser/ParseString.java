package nl.marcvanzee.KEProver.parser;

import nl.marcvanzee.KEProver.lib.Chars;

/**
 * String processing class used by parser.Parser<br>
 * contains a collection of methods that make parsing more easy
 * 
 * @author marc
 */
public class ParseString {
	private String str;
	private int location = -1;				// location of the connective
	private char c;							// character of the connective
	private int depth = 0;					// depth of the parenthesis
	private int minDepth = -1;				// minimal depth in the formula
	
	//==========================================================
	//====================== CONSTRUCTOR =======================
	//==========================================================
	
	/**
	 * Processes the input string by calling the analyse() method
	 * @param str input string in propositional logic
	 */
	public ParseString(String str) {
		this.str = str;
		analyse();
	}
	
	//==========================================================
	//========================= METHODS ========================
	//==========================================================
	
	/**
	 * analyze the string by walking through it:<br>
	 * - checks for connectives;<br>
	 * - keep track of the number of parentheses, to find out the minimal depth, and the overall depth;<br>
	 * - locates the top connective and saves its position
	 */
	private void analyse() {
		// walk through the string
		for (int i=0; i<str.length(); i++) {
			// analyse character i
			c = str.charAt(i);
			if (c == Chars.LEFT_P) {
				depth++;
			} else if (c == Chars.RIGHT_P) {
				depth--;
			} else {
				if (Chars.isConnective(c)) {
					// check whether this is the top connective.
					
					// if the depth of the current parentheses is smaller than the current minimal
					// depth of our connective, it means that we have a connective that is more
					// relevant to this sequence. in this way we will end up with the connective
					// of our sequence.
					
					// minDepth will be the overall depth of our sequence:
					// (((p))) 		-> minDepth = 3
					// (p)>(((q))) 	-> minDepth = 0
					if (depth < minDepth || minDepth < 0) {
						minDepth = depth;
						location = i;
					}
				}
			}
		}
	}
	
	/**
	 * remove all outer parentheses of this formula.<br>
	 * the number of parentheses to remove are saved in minDepth<br>
	 */
	public void removeOuterParentheses() {
		for (int i=1; i<minDepth(); i++) {
			str = str.replaceFirst("\\(", "");
			str = str.replaceAll("\\)$", "");
			location--;
		}
	}
	
	/**
	 * simply remove all parentheses in the string.<br>
	 * used for literal.
	 */
	public void removeAllParentheses() {
		str = str.replaceAll("(\\(|\\))", "");
	}
	
	public void addSingleParentheses() {
		this.str = "(" + this.str + ")";
		location++;
	}

	//==========================================================
	//====================== GETTERS ===========================
	//==========================================================
	
	/**
	 * recursive function that returns the number of negations preceding the input string<br>
	 * if a negation is found, it is stripped off. then the method is called recursively using the input string without the first character.
	 * @param countStr the string to analyze
	 * @return the number of negations
	 */
	public int getNegations(String countStr) {
		return ((countStr.length() > 1) && (countStr.charAt(0) == Chars.NEG)) ? 1 + getNegations(countStr.substring(1)) : 0;
	}
	
	// the remaining methods are very trivial and therefore not documented
	
	private int getBeginIndex() {
		int negs = getNegations(str);
		return (negs > 0) ? (negs + 1) : 1;
	}

	private int getConnectiveIndex() {
		return location;
	}

	public String getSubformula1() {
		return str.substring(getBeginIndex(), getConnectiveIndex());
	}
	
	public String getSubformula2() {
		return str.substring(getConnectiveIndex()+1, str.length()-1);
	}
	
	public String getString() {
		return str;
	}
	
	public char getConnective() {
		return str.charAt(getConnectiveIndex());
	}
	
	public int getDepth() {
		return depth;
	}

	public int getConnectiveLocation() {
		return location;
	}

	public int minDepth() {
		return minDepth;
	}
}
