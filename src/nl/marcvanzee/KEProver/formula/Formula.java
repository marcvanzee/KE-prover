package nl.marcvanzee.KEProver.formula;

import java.util.Vector;

import nl.marcvanzee.KEProver.lib.FormulaComp;
import nl.marcvanzee.KEProver.prover.ProofBranch;

/**
 * this class contains a formula, which is a collection of SubFormula objects.<br>
 * it contains several methods for manipulations on the formula, or simply to get information out of it.<br>
 * it is used by the SAKE class to describe the current formula that is being refuted.
 * @author marc
 *
 */
public class Formula {
	private Vector <Subformula> subformulas = new Vector<Subformula>();
	
	//==========================================================
	//====================== CONSTRUCTORS ======================
	//==========================================================
	
	public Formula(Vector <Subformula> subFormulas) {
		for (int i=0; i<subFormulas.size(); i++) {
			add(subFormulas.get(i));
		}
	}
	
	public Formula() {
	}
	
	//==========================================================
	//========================= METHODS ========================
	//==========================================================
	
	/**
	 * verifies whether the Subformula exists in this formula
	 */
	public boolean exists(Subformula formula) {
		for (int i=0; i<size(); i++) {
			if (FormulaComp.equals(formula, get(i)))
				return true;
		}
		return false;
	}
	
	/**
	 * verifies whether this formula, or the negation of it already exists in this formula
	 * @param subformula
	 * @return whether the Subformula is free
	 */
	public boolean freeSubformula(Subformula subformula) {
		return (!exists(subformula) && !exists(new Subformula(subformula, false)));
	}
	
	public void add(Subformula subformula) {
		subformulas.add(subformula);
	}
	
	public void remove(int i) {
		subformulas.remove(i);
	}
	
	/**
	 * looks for a contradiction in this formula. If found, it will perform thinning, meaning that it will
	 * add another proof into the ProofBranch with only the contradicting elements.
	 * 
	 * @param proof
	 * @return whether a contradiction has been found
	 */
	public boolean hasContradiction(ProofBranch proof) {
		Subformula sub1, sub2;
		
		for (int i=0; i<size(); i++) {
			sub1 = get(i);
			for (int j=i; j<size(); j++) {
				sub2 = get(j);
				
				if (FormulaComp.isContradiction(sub1, sub2)) {
					// THINNING IS IMPLEMENTED HERE!
					// if there are more sequences, then add this sequence
					// to the prove and thin it. then add the thinned out one.
					if (size()>2) {
						proof.setLastDescription("thinning");
						Formula thinFormula = new Formula();
						thinFormula.add(sub1);
						thinFormula.add(sub2);
						proof.addProofStep(thinFormula.toProof(), "axiom");
					}
					return true;
				}
			}
		}
		
		return false;
	}

	/**
	 * try to add the subFormula (with the validation <code>val</code>) to this formula.
	 * @param subFormula the subFormula to add
	 * @param val the validation of the subFormula
	 * @return whether the adding has succeeded
	 */
	public boolean tryAdd(Subformula subFormula, boolean val) {
		Subformula newSeq = subFormula.clone();
		newSeq.setNegs((val ? subFormula.getNegs() : (subFormula.getNegs() + 1)));

		if (!exists(newSeq)) {
			add(newSeq);
			return true;
		} else {
			return false;
		}
	}
	
	/**
	 * put all the literals in this formula in one string, separated by a comma.
	 * @return the string that contains all the literals.
	 */
	public String literalsToStr() {
		String str = "";
		boolean first = true;
		
		for (int i=0; i<size(); i++) {
			Subformula cur = get(i);
			if (cur.isLiteral()) {
				// only add a comma when this is not the first literal
				str += first ? "" : ", ";
				
				// add a "-" when negated
				str += (cur.isNegated()?"-":"") + cur.getVar();
				first = false;
			}
		}
		
		return str;
	}

	/**
	 * clone the current formula, and append Subformula <code>formula</code> to it.
	 * @param formula
	 * @return the formula with the subformula appended to it
	 */
	public Formula cloneAndAppend(Subformula formula) {
		Formula retFormula;
		
		Vector <Subformula> seq = new Vector<Subformula>();
		for (int i=0; i<size(); i++) {
			seq.add(get(i).clone());
		}
		seq.add(formula.clone());
		retFormula = new Formula(seq);
		
		return retFormula;
	}
	
	//==========================================================
	//==================== METHODS, GETTERS/SETTERS ============
	//==========================================================
	
	public int size() {
		return subformulas.size();
	}
	
	public Subformula get(int i) {
		return subformulas.get(i);
	}
	
	/**
	 * return a string containing all subformulas separated by commas
	 */
	public String toString() {
		String str = "";
		for (int i=0; i<size(); i++) {
			// only a comma when this is not the first subformula
			str += (i>0) ? ", " : "";
			str += get(i).print();
		}
		
		return str;
	}
	
	/**
	 * turn this formula into a proof or the form:<br>
	 * (positive subformulas) => (negative subformulas)
	 * @return string with the proof
	 */
	public String toProof() {
		String LHS = "";
		String RHS = "";
		for (int i=0; i<size(); i++) {
			Subformula seq = get(i);
			if (!seq.isNegated()) {
				LHS += ((LHS == "") ? "" : ", ") + seq.print();
			} else {
				Subformula seqNeg = seq.clone();
				seqNeg.setNegs(seqNeg.getNegs()-1);
				RHS += ((RHS == "") ? "" : ", ") + seqNeg.print();
			}
		}
		
		return LHS + " => " + RHS;
	}
	
	public int length() {
		int length = 0;
		for (int i=0; i<subformulas.size(); i++) {
			length += subformulas.get(i).length();
		}
		return length;
	}
}
