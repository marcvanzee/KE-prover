package nl.marcvanzee.nl.KEProverTxt;

import java.util.Vector;

public class SAKE extends Refuter {
	
	public SAKE(Vector<Subformula> sequents) {
		super(sequents);
	}
	
	// try to refute the current set of sequences
	public boolean isRefutable() {
		String reductions    = null;
		
		do {
			if (proof.size() > 0) proof.setLastDescription(reductions);
			proof.addProofStep(seqToProof(sequences), reductions);
			Sys.debug(seqToStr(sequences));
			
			if (hasContradiction()) {
				proof.setLastDescription("axiom");
				Sys.print("X");
				return false;
			}
			reductions = reduce();
		} while (reductions != null);

		// DC!!!
		// this is based on recursion again.
		// first retrieve the subformula that we are going to perform DC on
		Subformula dc = DC();
		
		if (dc == null) {
			// counterexample
			System.out.print("*LIGHTNING*\ncountermodel: ");
			printLiterals();
			return true;
		} else {
			// now we are ready for DC.
			// - add dc to the sequence
			// - run this sequence in a new Sake object
			// - if    it is refutable: finished!
			// -       not: change the sequence dc to its negation
			// also not refutable? the formula is not refutable.
			// finished!
			
			Sys.debug("\n## BRANCHPOINT #" + this.branchpoint);
			Sys.debug("DC on " + dc.print());
			
			proof.setLastDescription("dc on " + dc.print());
			
			SAKE branch1 = cloneAndAppend(dc);
			branch1.setBranchPoint(branchpoint+1);
			dc.setNegs(dc.getNegs() + 1);
			SAKE branch2 = cloneAndAppend(dc);
			
			if (branch1.isRefutable()) {
				// the formula is refutable, we can stop!
				return true;
			}
			// change the branch
			Sys.debug("\n** continuing at BRANCHPOINT #" + branchpoint);
			branch2.setBranchPoint(branch1.getBranchPoint());
			
			// now perform DC wih this one
			if (branch2.isRefutable()) {
				// the formula is refutable, we can stop!
				return true;
			}
			setBranchPoint(branch2.getBranchPoint());
			proof.setBranches(branch1.getProofBranch(), branch2.getProofBranch());
			return false;
		}
	}
		
	private String alphaReductions() {
		// alpha reductions
		for (int i=0; i<sequences.size(); i++) {
			// get a formula
			Subformula sub = sequences.get(i);
			
			// alpha reduction on formulas with at least one connective
			if (!sub.isLiteral()) {
				// alpha reduction for negations
				
				// the following formulas are applicable to alpha reductions:
				// (0) -(p>q) <=> p,-q			right->
				// (1) -(p|q) <=> -p,-q			right-|
				// (2) p&q    <=> p,q			left-&
				
				// first ensure the sequence is negated
				if (sub.getNegs() == 1) {
					// RULE (0)
					// now check the connective
					if (sub.getConnective() == Chars.IMPLIES) {
						// try to reduce this sequences.
						// if it succeeds (meaning that it hasn't been done before), 
						// return true TODO: and flag the sequence as used for alpha reduction
						if (alphaReduce(sub, 0)) return "right->";
					} 
					// RULE (1)
					if (sub.getConnective() == Chars.OR) {
						if (alphaReduce(sub, 1)) return "right-|";
					}
				} else {
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
	
	private boolean alphaReduce(Subformula formula, int type) {
		boolean ret;
		boolean sub1 = false, sub2 = false;

		// (0) -(p>q) <=> 	p,-q
		// (1) -(p|q) <=> 	-p,-q
		// (2) p&q    <=> 	p,q
		
		// we now have to add the sequences on the RHS of the
		// formulas above. therefore we have to adjust the negations
		switch (type) {
			case 0:	sub1 = true;	sub2 = false;		break;
			case 1:	sub1 = false;	sub2 = false;		break;
			case 2: sub1 = true;	sub2 = true;
		}	
		
		// now try to add the subformulas
		ret = tryAdd(formula.getSub1(), sub1) | tryAdd(formula.getSub2(), sub2);
		
		// return true if we have added a formula, else return false
		if (ret) {
			Sys.debug("alpha reduction on " + formula.print());
			return true;
		} else {
			return false;
		}
	}

	private String betaReductions() {
		for (int i=0; i<sequences.size(); i++) {
			Subformula sub1 = sequences.get(i);
			
			// (0) -(p&q) &  p |- -q		right1-&
			// (1) -(p&q) &  q |- -p		right2-&
			// (2)   p|q  & -p |-  q		left1-|
			// (3)   p|q  & -q |-  q		left2-|
			// (4)   p>q  &  p |-  q		left1->
			// (5)   p>q  & -q |- -p		left2->
			
			// beta reductions for the '&' connective (0 and 1)
			if ((sub1.getConnective() == Chars.AND) && (sub1.getNegs() == 1)) {
				// RULE (0)
				if (exists(sub1.getSub1())) {
					if (tryAdd(sub1.getSub2().clone(), false)) {
						return "right1-& on " + sub1.print() + " and " + sub1.getSub1().printStripped();
					}
				}
				
				// RULE (1)
				if (exists(sub1.getSub2())) {
					if (tryAdd(sub1.getSub1().clone(), false)) {
						return "right2-& on " + sub1.print() + " and " + sub1.getSub2().printStripped();
					}
				} 
			}
			
			// beta reductions for the '|' connective (2 and 3)
			if ((sub1.getConnective() == Chars.OR) && (sub1.getNegs() == 0)) {
				// RULE (2)
				if (exists(new Subformula(sub1.getSub1(), false))) {
					if (tryAdd(sub1.getSub2().clone(), true)) {
						return "left1-| on " + sub1.print() + " and -" + sub1.getSub1().printStripped();

					}
				}
				
				// RULE (3)
				if (exists(new Subformula(sub1.getSub2(), false))) {
					if (tryAdd(sub1.getSub1().clone(), true)) {
						return "left2-| on " + sub1.print() + " and -" + sub1.getSub2().printStripped();
					}
				}
			}
			
			// beta reductions for the '>' connective
			if ((sub1.getConnective() == Chars.IMPLIES) && (sub1.getNegs() == 0)) {
				// RULE (4)
				if (exists(sub1.getSub1())) {
					if (tryAdd(sub1.getSub2().clone(), true)) {
						return "left1-> on " + sub1.print() + " and " + sub1.getSub1().printStripped();
					}
				}
				
				// RULE (5)
				if (exists(new Subformula(sub1.getSub2(), false))) {
					if (tryAdd(sub1.getSub1().clone(), false)) {
						return "left2-> on " + sub1.print() + " and -" + sub1.getSub2().printStripped();
					}
				}
			} 
		}
		return null;
	}
	
	private String etaReductions() {
		for (int i=0; i<sequences.size(); i++) {
			Subformula sub1 = sequences.get(i);
			
			// (0)  p & -(p<q) |- -q
			// (1)  q & -(p<q) |- -p
			// (2) -p & -(p<q) |-  q
			// (3) -q & -(p<q) |-  p
			// (4)  p &   p<q  |-  q
			// (5)  q &   p<q  |-  p
			// (6) -p &   p<q  |- -q
			// (7) -q &   p<q  |- -p
			
			if (sub1.getConnective() == Chars.EQUIV) {
				for (int j=0; j<sequences.size(); j++) {
					if (i != j) {
						Subformula sub2 = sequences.get(j);
						
						for (int k=0; k<4; k++) {
							if (etaReduce(sub1, sub2, k, sub1.getNegs())) {
								if (sub1.getNegs() == 0) {
									return "right" + k + "-< on " + sub1.print() + "and " + sub2.print();
								} else {
									return "left" + k + "-< on " + sub1.print() + "and " + sub2.print();
								}
							}
						}
					}
				}
			}
		} 
		// no eta reductions performed
		return null;
	}
	
	private boolean etaReduce(Subformula sub1, Subformula sub2, int type, int negs) {
		// s1 = the sequence containing "<"
		// s2 = the other sequence
		Subformula s1 = null, newSeq = null;
		boolean newSeqValue = true;

		switch (type) {
		case 0:	s1 = sub1.getSub1();						newSeq = sub1.getSub2();	newSeqValue = false;	break;
		case 1: s1 = sub1.getSub2();						newSeq = sub1.getSub1(); 	newSeqValue = false;	break;
		case 2: s1 = new Subformula(sub1.getSub1(), false);	newSeq = sub1.getSub2(); 							break;
		case 3: s1 = new Subformula(sub1.getSub2(), false);	newSeq = sub1.getSub1(); 							break;
		}
		
		// turn the truth value around, because rules 0-3 are identical to 4-7
		// except that the resulting truth value gets turned around
		
		if (negs == 0) newSeqValue = !newSeqValue;
		
		if (equals(s1, sub2)) {
			if (tryAdd(newSeq.clone(), newSeqValue)) {
				return true;
			}
		} 		
		
		return false;
	}
	
	private Subformula DC() {
		Subformula ret = null;
		
		switch (heuristic) {
		//default
		case 0:
			ret = DCdefault();
			break;
		}
		
		return (ret == null) ? ret : ret.clone();
	}
	
	private Subformula DCdefault() {
		// find one of these formulas		and make sure these literals aren't clauses
		// (0) -(P&Q)						P, Q							
		// (1) P|Q							-P, -Q
		// (2) P>Q							P, -Q
		// (3) P=Q							P, Q, -P, -Q
				
		// the default DC simply splits on the first possibility it encounters
		
		Subformula subShort;
		for (int i=0; i<sequences.size(); i++) {
			Subformula sub1 = sequences.get(i);
			
			// RULE (0)
			if ((sub1.getConnective() == Chars.AND) && (sub1.getNegs() == 1)) {
				if (!exists(sub1.getSub1()) && !exists(sub1.getSub2())) {
					subShort = shortestFormula(sub1.getSub1(), sub1.getSub2());
					if (freeSequence(subShort))		return subShort;
				}
			} 
			
			// RULE (1)
			if ((sub1.getConnective() == Chars.OR) && (sub1.getNegs() == 0)) {
				if (!exists(new Subformula(sub1.getSub1(), false)) && !exists(new Subformula(sub1.getSub2(), false))) {
					subShort = shortestFormula(sub1.getSub1(), sub1.getSub2());
					if (freeSequence(subShort))		return new Subformula(subShort, false);
				}
			}
			
			// RULE (2)
			if ((sub1.getConnective() == Chars.IMPLIES) && (sub1.getNegs() == 0)) {
				if (!exists(sub1.getSub1()) && !exists(new Subformula(sub1.getSub2(), false))) {
					subShort = shortestFormula(sub1.getSub1(), sub1.getSub2());
					if (freeSequence(subShort))		return subShort;
				}
			}
			
			// RULE (3)
			if (sub1.getConnective() == Chars.EQUIV) {
				if (!exists(sub1.getSub1()) && !exists(new Subformula(sub1.getSub2(), false))) {
					subShort = shortestFormula(sub1.getSub1(), sub1.getSub2());
					if (freeSequence(subShort))		return subShort;
				}
			}
		}
		return null;
	}

	private String reduce() {
		String ret = null;
		if (removeNegations()) 	return "double negation";
		ret = removeDoubles();   if (ret != null) return "cloning " + ret;
		ret = etaReductions();   if (ret != null) return ret;
		ret = betaReductions();  if (ret != null) return ret;
		ret = alphaReductions(); if (ret != null) return ret;
	// 	if (sake.subsumption()) return true;		// TODO
		return null;
	}
	
	private SAKE cloneAndAppend(Subformula formula) {
		SAKE branch;

		Vector <Subformula> seq = new Vector<Subformula>();
		for (int i=0; i<sequences.size(); i++) {
			seq.add(sequences.get(i).clone());
		}	
		seq.add(formula);
		branch = new SAKE(seq);
		return branch;
	}

	public ProofBranch getProofBranch() {
		return proof;
	}
	
	public void printProofTree() {
		Sys.print("\n\n== Proof: ==================================================");
		proof.lineSeed(0);
		proof.print();
		Sys.print("====================================================== QED =");
	}
}