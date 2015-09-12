package nl.marcvanzee.KEProver.lib;

import nl.marcvanzee.KEProver.formula.Subformula;

/**
 * A collection of static methods that are used for the comparison of two objects
 * of the Subformula class.
 * 
 * @author marc
 *
 */
public class FormulaComp {
	/**
	 * verifies whether two Subformulas are equal, meaning that they have equal<br>
	 * negations, and equal connectives and subformulas. This is verified recursively<br> 
	 * until the literals are compared, which of course also should be equal.
	 * 
	 * @param sub1 the first Subformula
	 * @param sub2 the second Subformula
	 * @return <code>true</code> if <code>sub1</code> is equal to <code>sub2</code>, else <code>false</code>
	 */
	public static boolean equals(Subformula sub1, Subformula sub2) {
		
		// see whether the subformulas are literals
		if ((sub1.isLiteral()) && (sub2.isLiteral())) {
			// both negated/not negated and same variables
			// we use isNegated() because we want to ignore the amount of negations here
			// meaning that p and --p are equal.
			if ((sub1.isNegated() == sub2.isNegated()) && (sub1.getVar().equals(sub2.getVar()))) {
				return true;
			} else {
				return false;
			}
		} else {
			// the subformulas are not literals
			// do a recursive call
			if ((sub1.getConnective() == sub2.getConnective()) && (sub1.isNegated() == sub2.isNegated()) &&
				(equals(sub1.getSub1(), sub2.getSub1())) && (equals(sub1.getSub2(), sub2.getSub2()))) {
				return true;
			} else {
				return false;
			}
		}
	}

	/**
	 * verify whether two subformulas contradict. this is the case when one of<br>
	 * them has 1 negation, and the other one 0, but they are for the rest the same.
	 * 
	 * @param sub1 first subformula
	 * @param sub2 second subformula
	 * @return <code>true</code> if formulas contradicts, <code>false</code> otherwise.
	 */
	public static boolean isContradiction(Subformula sub1, Subformula sub2) {
		if ( ((sub1.getNegs() == 1) && (sub2.getNegs() == 0)) || ((sub1.getNegs() == 0) && (sub2.getNegs() == 1))) {
			
			if (sub1.isLiteral() && sub2.isLiteral()) {
				if (sub1.getVar().equals(sub2.getVar())) {
					return true;
				}
			} else if (!sub1.isLiteral() && !sub2.isLiteral()) {
				if ((sub1.getConnective() == sub2.getConnective()) &&
						(equals(sub1.getSub1(), sub2.getSub1())) &&
						(equals(sub1.getSub2(), sub2.getSub2()))) {
					return true;
				}
			}
		}
		return false;
	}
	
	/**
	 * @param sub1
	 * @param sub2
	 * @return returns shortest formula, without parentheses
	 */
	public static Subformula shortestFormula(Subformula sub1, Subformula sub2) {
		return (sub1.length() <= sub2.length()) ? sub1 : sub2;
	}
}