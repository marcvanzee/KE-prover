package nl.marcvanzee.KEProver.prover;

import nl.marcvanzee.KEProver.GUI.Main;
import nl.marcvanzee.KEProver.formula.Formula;
import nl.marcvanzee.KEProver.formula.Subformula;
import nl.marcvanzee.KEProver.lib.Chars;
import nl.marcvanzee.KEProver.lib.FormulaComp;

/**
 * The Prover, the beast behind all the glitter and glamour.<br>
 * A SAKE implementation of a propositional logic tableau prover.<br>
 * 
 * @author marc
 */
public class SAKE {
	/** the formula that is being used for proving */
	private Formula formula;
	/** the current branchpoint, used for numbering in DC points */
	private int branchpoint;
	/** the proof tree */
	private ProofBranch proof;

	/**
	 * the gui that can be used for output.
	 * if there this object is not initialized, the output is sent to stdout
	 */
	private Main gui;
	
	//==========================================================
	//====================== CONSTRUCTORS ======================
	//==========================================================
	
	public SAKE() { }
	
	/** @param formula the formula to be used for proving. */
	public SAKE(Formula formula) {
		init(formula);
	}
	
	//==========================================================
	//====================== METHODS, OTHERS ===================
	//==========================================================
	
	/** @param formula the formula to be used for proving. */
	public void insert(Formula formula) {
		init(formula);
	}
	
	/**
	 * the actual proving of the formula.<br><br>
	 * Goes through the following steps:<br>
	 * 1. check for a contradiction and try to reduce the formula by calling the <code>reductions()</code> method<br>
	 * 2. if there is a contradiction, return true<br>
	 * 3. repeat (1,2) until there are no more reductions possible<br>
	 * 4. try dc by calling the <code>DC()</code> method<br>
	 * 5. if <code>DC()</code> does not succeed, stop and print a countermodel<br>
	 * 6. if <code>DC()</code> succeeds, create two branches, add the DC formula and call this method recursively<br>
	 * 7. if one of the branches does not close, return false<br>
	 * 8. if both the branches close, return true<br><br>
	 * 
	 * During this process, a ProofBranch object is used to keep track of the steps
	 * @return <code>true</code> if the formula is provable, <code>false</code> otherwise.
	 */
	public boolean isProvable() {
		// start with no reductions
		String reductions = null;
		
		do {
			// change the description of the previous prove if it exists, for the result of the prove
			updatePreviousProve(reductions);
			
			proof.addProofStep(formula.toProof(), reductions);
			
			GUImsg(formula.toString());
			
			// NOTE: the method hasContradiction() takes care of thinning when
			//       a contradiction occurs. therefore the proof object is given as an argument
			if (formula.hasContradiction(proof)) {
				proof.setLastDescription("axiom");
				GUImsg("X");
				return true;
			}
			
			// see if we can perform any form of reductions
			reductions = reduce();
		} while (reductions != null);

		// there were not reductions possible, and there is no contradiction yet,
		// so lets try DC!
		
		// first retrieve the subformula that we are going to perform DC on
		Subformula dc = DC();
		
		if (dc == null) {
			// apparently, dc was not possible, so we have a counter example
			// simply print the literals to show it
			GUImsg("\n\n*LIGHTNING*\ncountermodel: " + formula.literalsToStr());
			return false;
		} else {
			// print the branchpoint, to make sure that we are branching
			GUImsg(" |\n# BRANCHPOINT #" + this.branchpoint);
			GUImsg(" | DC on " + dc.print());
			
			proof.setLastDescription("dc on " + dc.print());
			
			// add a new SAKE object with the dc Subformula added to the current formula
			SAKE branch1 = new SAKE(formula.cloneAndAppend(dc));
			
			// update the branchpoint of the first branch
			branch1.setBranchPoint(branchpoint+1);
			
			// change the negation of the dc object and add it to the second branch
			dc.setNegs(dc.getNegs() + 1);
			SAKE branch2 = new SAKE(formula.cloneAndAppend(dc));
			
			// tell the branches where to print to
			branch1.setGui(gui);
			branch2.setGui(gui);
			
			if (!branch1.isProvable()) {
				// the formula is refutable, we can stop!
				return false;
			}
			// if we are here, the first branch closed, so lets try the second
			GUImsg("\n** continuing at BRANCHPOINT #" + branchpoint);
			
			// update the branchpoint of the second branch to the last of the first one
			branch2.setBranchPoint(branch1.getBranchPoint());
			
			if (!branch2.isProvable()) {
				// the formula is refutable, we can stop!
				return false;
			}
			
			// apparently, both of the branches closed
			// first update the branchpoint (so a possible parent can collect it)
			setBranchPoint(branch2.getBranchPoint());
			
			// save the branches to the prooftree
			proof.setBranches(branch1.getProofBranch(), branch2.getProofBranch());
			
			return true;
		}
	}
	
	/**
	 * all the reductions are called from here, in the following order:<br>
	 * - <code>removeNegations():</code> looks for double negations;<br>
	 * - <code>removeDoubles():</code> remove double appearances of subformulas;<br>
	 * - <code>betaSubsumption();</code><br>
	 * - <code>etaReductions();</code><br>
	 * - <code>betaReductions();</code><br>
	 * - <code>alphaReductions();</code><br><br>
	 * 
	 * note that when any of the methods returns a value, this value<br>
	 * is immediately returned and no other methods is called.<br>
	 * @return reduction description. if no reductions is possible, null is returned<br>
	 */
	private String reduce() {
		String ret = null;
		ret = removeNegations();  if (ret != null) return ret;
		ret = removeDoubles();    if (ret != null) return ret;
		ret = betaSubsumptions(); if (ret != null) return ret;
		ret = etaReductions();    if (ret != null) return ret;
		ret = betaReductions();   if (ret != null) return ret;
		ret = alphaReductions();  if (ret != null) return ret;
		return null;
	}
		

	/**
	 * looks for a possibility for DC, using the heuristic suggested by Jeremey Pitt<br>
	 * and Jim Cunningham, namely to select the least complex sub-formula of the<br>
	 * most complex formula.<br><br>
	 * 
	 * finds one of these formulas<br>
	 * <code>(0) -(P&Q), without P, Q</code><br>					
	 * <code>(1) P|Q, without -P, -Q</code><br>
	 * <code>(2) P>Q, without P, -Q</code><br>
	 * <code>(3) P=Q, without P, Q, -P, -Q</code><br><br>
	 *
	 * @return a <code>Subformula</code> object to spit on, or <code>null</code> when none found. 
	 */
	private Subformula DC() {		
		Subformula subShort;
		for (int i=0; i<formula.size(); i++) {
			Subformula sub1 = formula.get(i);
			
			// RULE (0)
			if ((sub1.getConnective() == Chars.AND) && (sub1.getNegs() == 1)) {
				if (!formula.exists(sub1.getSub1()) && !formula.exists(sub1.getSub2())) {
					subShort = FormulaComp.shortestFormula(sub1.getSub1(), sub1.getSub2());
					if (formula.freeSubformula(subShort))		return subShort.clone();
				}
			} 
			
			// RULE (1)
			if ((sub1.getConnective() == Chars.OR) && (sub1.getNegs() == 0)) {
				if (!formula.exists(new Subformula(sub1.getSub1(), false)) && !formula.exists(new Subformula(sub1.getSub2(), false))) {
					subShort = FormulaComp.shortestFormula(sub1.getSub1(), sub1.getSub2());
					if (formula.freeSubformula(subShort))		return new Subformula(subShort.clone(), false);
				}
			}
			
			// RULE (2)
			if ((sub1.getConnective() == Chars.IMPLIES) && (sub1.getNegs() == 0)) {
				if (!formula.exists(sub1.getSub1()) && !formula.exists(new Subformula(sub1.getSub2(), false))) {
					subShort = FormulaComp.shortestFormula(sub1.getSub1(), sub1.getSub2());
					if (formula.freeSubformula(subShort))		return subShort.clone();
				}
			}
			
			// RULE (3)
			if (sub1.getConnective() == Chars.EQUIV) {
				if (!formula.exists(sub1.getSub1()) && !formula.exists(new Subformula(sub1.getSub2(), false))) {
					subShort = FormulaComp.shortestFormula(sub1.getSub1(), sub1.getSub2());
					if (formula.freeSubformula(subShort))		return subShort.clone();
				}
			}
		}
		return null;
	}
	
	
	private void GUImsg(String str) {
		if (gui != null) {
			gui.msg(str);
		} else {
			System.out.println(str);
		}
	}

	public void printProofTree() {
		gui.msg("== Proof: =============================================");
		proof.lineSeed(0);
		gui.msg(proof.toString());
		gui.msg("================================================= QED =");
	}
	
	private void init(Formula formula) {
		this.formula = formula;
		proof = new ProofBranch();
		branchpoint = 0;
	}	
	
	private void updatePreviousProve(String reductions) {
		if (proof.size() > 0) {
			proof.setLastDescription(reductions);
			GUImsg("| " + reductions);
		}
	}
	
	//==========================================================
	//====================== METHODS, REDUCTIONS ===============
	//==========================================================
	
	/**
	 * search formula for double occurrences and remove if found.
	 * @return a description of the double occurrence reduction, or null when none is possible
	 */
	protected String removeDoubles() {
		// go through formula
		for (int i=0; i<formula.size(); i++) {
			// from there, search remaining formulas
			for (int j=i+1; j<formula.size(); j++) {
				if (FormulaComp.equals(formula.get(i), formula.get(j))) {
					
					// copy this formula because it will be removed
					String ret = formula.get(i).print();
					
					// remove formula
					formula.remove(i);
					return "cloning " + ret;
				}
			}
		}
		return null;
	}
	
	/**
	 * search formula for double negations and remove if found.
	 * @return a description of the negation reduction, or null when none is possible
	 */
	protected String removeNegations() {
		for (int i=0; i<formula.size(); i++) {
			
			// check for negations
			if (formula.get(i).getNegs() > 1) {
				formula.get(i).setNegs(formula.get(i).getNegs()-2);
				return "double negation";
			}
		}
		return null;
	}
	
	/**
	 * tries to find possible alpha reductions in the current formula.<br>
	 * Uses the method <code>alphaReduce(Subformula subformula, int type)</code> for the hard work.<br><br>
	 * 
	 * The following formulas are applicable to alpha reductions:<br>
	 * <code>(0) -(p>q) <=>  p,-q			right-></code><br>
	 * <code>(1) -(p|q) <=> -p,-q			right-|</code><br>
	 * <code>(2)   p&q  <=>  p,q			left-&</code><br>
	 * 
	 * @return a description of the alpha reduction, or null when none is possible
	 */
	private String alphaReductions() {
		// go through our formula
		for (int i=0; i<formula.size(); i++) {
			Subformula sub = formula.get(i);
			
			if (!sub.isLiteral()) {	
				// alpha reduction are only possible on formulas with at least one connective
				
				if (sub.getNegs() == 1) {
					// only negated subformulas
					
					// RULE (0)
					// now check the connective
					if (sub.getConnective() == Chars.IMPLIES) {
						if (alphaReduce(sub, 0)) return "right->";
					} 
					// RULE (1)
					if (sub.getConnective() == Chars.OR) {
						if (alphaReduce(sub, 1)) return "right-|";
					}
				} else {
					// only positive subformulas
					
					// RULE (2)					
					if (sub.getConnective() == Chars.AND) {
						if (alphaReduce(sub, 2)) return "left-&";
					}
				}
			}
		}
		// no alpha reductions performed
		return null;
	}
	
	
	/**
	 * used by <code>alphaReductions()</code>.
	 * @param subformula the subformula to perform alpha reduction on
	 * @param type the kind of reduction, see alphReductions() description for a list
	 * @return true if the alpha reductions has succeeded, else false.
	 */
	private boolean alphaReduce(Subformula subformula, int type) {
		boolean ret;
		boolean sub1 = false, sub2 = false;

		// we have to add the new subformulas to the formula, therefore we have to adjust the negations
		switch (type) {
			case 0:	sub1 = true;	sub2 = false;		break;
			case 1:	sub1 = false;	sub2 = false;		break;
			case 2: sub1 = true;	sub2 = true;
		}	
		
		// try to add the subformulas
		ret = formula.tryAdd(subformula.getSub1(), sub1) | formula.tryAdd(subformula.getSub2(), sub2);
		
		// return true if we have added a formula, else return false
		if (ret) {
			return true;
		} else {
			return false;
		}
	}
	

	
	/**
	 * tries to find possible beta reductions in the current formula.<br><br>
	 * 
	 * The following formulas are applicable to alpha reductions:<br>
	 * <code>(0) -(p&q) &  p |- -q		right1-&</code><br>
	 * <code>(1) -(p&q) &  q |- -p		right2-&</code><br>
	 * <code>(2)   p|q  & -p |-  q		left1-|</code><br>
	 * <code>(3)   p|q  & -q |-  q		left2-|</code><br>
     * <code>(4)   p>q  &  p |-  q		left1-></code><br>
	 * <code>(5)   p>q  & -q |- -p		left2-></code><br>
	 * 
	 * @return a description of the beta reduction, or null when none is possible
	 */
	private String betaReductions() {
		// go through the formula
		for (int i=0; i<formula.size(); i++) {
			Subformula sub1 = formula.get(i);
			
			// beta reductions for the '&' connective (0 and 1)
			if ((sub1.getConnective() == Chars.AND) && (sub1.getNegs() == 1)) {
				// RULE (0)
				if (formula.exists(sub1.getSub1())) {
					if (formula.tryAdd(sub1.getSub2().clone(), false)) {
						return "right1-& on " + sub1.print() + " and " + sub1.getSub1().printStripped();
					}
				}
				
				// RULE (1)
				if (formula.exists(sub1.getSub2())) {
					if (formula.tryAdd(sub1.getSub1().clone(), false)) {
						return "right2-& on " + sub1.print() + " and " + sub1.getSub2().printStripped();
					}
				} 
			}
			
			// beta reductions for the '|' connective (2 and 3)
			if ((sub1.getConnective() == Chars.OR) && (sub1.getNegs() == 0)) {
				// RULE (2)
				if (formula.exists(new Subformula(sub1.getSub1(), false))) {
					if (formula.tryAdd(sub1.getSub2().clone(), true)) {
						return "left1-| on " + sub1.print() + " and -" + sub1.getSub1().printStripped();

					}
				}
				
				// RULE (3)
				if (formula.exists(new Subformula(sub1.getSub2(), false))) {
					if (formula.tryAdd(sub1.getSub1().clone(), true)) {
						return "left2-| on " + sub1.print() + " and -" + sub1.getSub2().printStripped();
					}
				}
			}
			
			// beta reductions for the '>' connective
			if ((sub1.getConnective() == Chars.IMPLIES) && (sub1.getNegs() == 0)) {
				// RULE (4)
				if (formula.exists(sub1.getSub1())) {
					if (formula.tryAdd(sub1.getSub2().clone(), true)) {
						return "left1-> on " + sub1.print() + " and " + sub1.getSub1().printStripped();
					}
				}
				
				// RULE (5)
				if (formula.exists(new Subformula(sub1.getSub2(), false))) {
					if (formula.tryAdd(sub1.getSub1().clone(), false)) {
						return "left2-> on " + sub1.print() + " and -" + sub1.getSub2().printStripped();
					}
				}
			} 
		}
		return null;
	}
	
	
	
	/**
	 * tries to find possible beta subsumptions in the current formula.<br><br>
	 * 
	 * the following formulas are applicable to beta subsumptions:<br>
	 * <code>(0) p|q & p => remove p|q</code><br>
	 * <code>(1) p|q & q => remove p|q</code><br>
	 * <code>(2) p&p & p => remove p&p</code><br>
	 * <code>(3) p>p & p => remove p>p</code><br>
	 * <code>(4) p<p & p => remove p<p</code><br>
	 * 
	 * @return a description of the beta subsumptions, or null when none is possible
	 */
	private String betaSubsumptions() {
		// go through the formula
		for (int i=0; i<formula.size(); i++) {
			Subformula sub1 = formula.get(i);
			
			if ((sub1.getConnective() == Chars.OR) && (sub1.getNegs() == 0)) {
				// RULE (0) & RULE (1)
				if (formula.exists(sub1.getSub1()) || formula.exists(sub1.getSub2())) {
					String ret = sub1.print();
					formula.remove(i);
					return "beta subsumption on " + ret;
				}
			}
			if ( ((sub1.getConnective() == Chars.AND)     ||
			      (sub1.getConnective() == Chars.IMPLIES) ||
				  (sub1.getConnective() == Chars.EQUIV)    ) && (sub1.getNegs() == 0)) {
				// RULE (2,3,4)
				if (formula.exists(sub1.getSub1()) && FormulaComp.equals(sub1.getSub1(), sub1.getSub2())) {
					String ret = sub1.print();
					formula.remove(i);
					return "beta subsumption on " + ret;
				}
			}
		}
		
		return null;
	}
	
	
	/**
	 * tries to find possible eta reductions in the current formula.<br>
	 * Uses the method <code>etaReduce(Subformula subformula, int type)</code> for the hard work.<br><br>
	 * 
	 * The following formulas are applicable to eta reductions:<br>
	 * <code>(0)  p & -(p<q) |- -q</code><br>
	 * <code>(1)  q & -(p<q) |- -p</code><br>
	 * <code>(2) -p & -(p<q) |-  q</code><br>
	 * <code>(3) -q & -(p<q) |-  p</code><br>
	 * <code>(4)  p &   p<q  |-  q</code><br>
	 * <code>(5)  q &   p<q  |-  p</code><br>
	 * <code>(6) -p &   p<q  |- -q</code><br>
	 * <code>(7) -q &   p<q  |- -p</code><br>
	 * 
	 * @return a description of the eta reduction, or null when none is possible
	 */
	private String etaReductions() {
		// go through the formula
		for (int i=0; i<formula.size(); i++) {
			Subformula sub1 = formula.get(i);
			
			// only on an equivalence relationship
			if (sub1.getConnective() == Chars.EQUIV) {
				
				// now go through all the formulas again
				for (int j=0; j<formula.size(); j++) {
					
					// except the one in the outer loop
					if (i != j) {
						Subformula sub2 = formula.get(j);
						
						// the eta reductions can be divided in two groups, 
						// we use this technique to quickly scan for all reductions.
						for (int k=0; k<4; k++) {
							if (etaReduce(sub1, sub2, k)) {
								if (sub1.getNegs() == 0) {
									return "right" + k + "-< on " + sub1.print() + " and " + sub2.print();
								} else {
									return "left" + k + "-< on " + sub1.print() + " and " + sub2.print();
								}
							}
						}
					}
				}
			}
		}
		return null;
	}
	
	
	/**
	 * used by <code>etaReductions()</code> for subformula analysis
	 * 
	 * @param sub1 the first subformula
	 * @param sub2 the second subformula
	 * @param type the sort of reductions
	 * @return <code>true</code> if succesful, <code>false</code> otherwise.
	 */
	private boolean etaReduce(Subformula sub1, Subformula sub2, int type) {
		Subformula s1 = null, newSeq = null;
		boolean newSeqValue = true;

		// initialize the subformulas for comparing
		switch (type) {
		case 0:	s1 = sub1.getSub1();						newSeq = sub1.getSub2();	newSeqValue = false;	break;
		case 1: s1 = sub1.getSub2();						newSeq = sub1.getSub1(); 	newSeqValue = false;	break;
		case 2: s1 = new Subformula(sub1.getSub1(), false);	newSeq = sub1.getSub2(); 							break;
		case 3: s1 = new Subformula(sub1.getSub2(), false);	newSeq = sub1.getSub1(); 							break;
		}
		
		// turn the truth value around, because rules 0-3 are identical to 4-7
		// except that the resulting truth value gets turned around
		if (sub1.getNegs() == 0) newSeqValue = !newSeqValue;
		
		if (FormulaComp.equals(s1, sub2)) {
			if (formula.tryAdd(newSeq.clone(), newSeqValue)) {
				return true;
			}
		} 		
		
		return false;
	}

	
	//==========================================================
	//=============== METHODS, GETTERS/SETTERS =================
	//==========================================================
	
	public void setGui(Main gui) {
		this.gui = gui;
	}
	
	protected void setBranchPoint(int branch) {
		this.branchpoint = branch;
	}
	
	public int getBranchPoint() {
		return branchpoint;
	}
	
	public ProofBranch getProofBranch() {
		return proof;
	}
}

