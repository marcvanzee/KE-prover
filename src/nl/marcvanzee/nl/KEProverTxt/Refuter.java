package nl.marcvanzee.nl.KEProverTxt;

import java.util.Vector;
// the main theorem prover using the SAKE heuristic
// it assumes a vectorlist of subfomulas and returns an altered one

public class Refuter {
	protected Vector <Subformula> sequences;
	protected ProofBranch proof;
	protected int heuristic = 0;
	protected boolean debug = false;
	protected int branchpoint = 0;
	
	public Refuter(Vector <Subformula> sequents) {
		this.sequences = new Vector<Subformula>();
	
		for (int i=0; i<sequents.size(); i++) {
			this.sequences.add(sequents.get(i).clone());
		}
		proof = new ProofBranch();
	}
	
	protected boolean freeSequence(Subformula formula) {
		return (!exists(formula) && !exists(new Subformula(formula, false)));
	}
	
	protected boolean equals(Subformula sub1, Subformula sub2) {
		if ((sub1.isLiteral()) && (sub2.isLiteral())) {
			if ((sub1.isNegated() == sub2.isNegated()) &&
				(sub1.getVar().equals(sub2.getVar()))) {
				return true;
			} else {
				return false;
			}
		} else {
			// we use isNegated() because we want to ignore the amount
			// of negations here
			if ((sub1.getConnective() == sub2.getConnective()) &&
				(sub1.isNegated() == sub2.isNegated()) &&
				(equals(sub1.getSub1(), sub2.getSub1())) &&
				(equals(sub1.getSub2(), sub2.getSub2()))) {
				return true;
			} else {
				return false;
			}
		}
	}
	
	// look for contradictions
	public boolean hasContradiction() {
		Subformula sub1, sub2;
		
		for (int i=0; i<sequences.size(); i++) {
			sub1 = sequences.get(i);
			for (int j=i; j<sequences.size(); j++) {
				sub2 = sequences.get(j);
				
				if (isContradiction(sub1, sub2)) {
					// THINNING IS IMPLEMENTED HERE!
					// if there are more sequences, then add this sequence
					// to the prove and thin it. then add the thinned out one.
					if (sequences.size()>2) {
						proof.setLastDescription("thinning");
						Vector <Subformula> thinSeq = new Vector<Subformula>();
						thinSeq.add(sub1);
						thinSeq.add(sub2);
						proof.addProofStep(seqToProof(thinSeq), "axiom");
					}
					return true;
				}
			}
		}
		
		return false;
	}
	
	private boolean isContradiction(Subformula sub1, Subformula sub2) {
		if ( ((sub1.getNegs() == 1) && (sub2.getNegs() == 0)) ||
				((sub1.getNegs() == 0) && (sub2.getNegs() == 1))) {
			
			if (sub1.isLiteral() && sub2.isLiteral()) {
				if (sub1.getVar().equals(sub2.getVar())) {
					return true;
				}
			} else if (!sub1.isLiteral() && !sub2.isLiteral()) {
				if ((sub1.getConnective() == sub2.getConnective()) &&
						(equals(sub1.getSub1(), sub2.getSub1())) &&
						(equals(sub1.getSub2(), sub2.getSub2()))) {
					Sys.debug("contradiction between " + sub1.print() + " and " + sub2.print());
					return true;
				}
			}
		}
		return false;
	}
	
	public String seqToStr(Vector <Subformula> sequences) {
		String str = "";
		for (int i=0; i<sequences.size(); i++) {
			str += (i>0) ? ", " : "";
			str += sequences.get(i).print();
		}
		
		return str;
	}
	
	public String seqToProof(Vector <Subformula> sequences) {
		String LHS = "";
		String RHS = "";
		for (int i=0; i<sequences.size(); i++) {
			Subformula seq = sequences.get(i);
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
	
	protected boolean isUnique(Subformula seq) {
		for (int i=0; i<sequences.size(); i++) {
			if (equals(seq, sequences.get(i)))
				return false;
		}
		
		return true;
	}
	
	protected boolean tryAdd(Subformula seq, boolean val) {
		Subformula newSeq = seq.clone();
		newSeq.setNegs((val ? seq.getNegs() : (seq.getNegs() + 1)));

		if (isUnique(newSeq)) {
			sequences.add(newSeq);
			return true;
		} else {
			return false;
		}
	}
	
	// TODO: code heuristics
	
	// three different heuristics possible:
	// (0) default 				-- use first formula found
	// (1) pitt & cunningham 	-- use least complex subformula of the most complex formula
	// (2) d'agostine			-- use subformula that appears most often in the branch
	// (3) van zee				-- my own heuristic, pick 
	public void useHeuristic(int heuristic) {
		this.heuristic = heuristic;
	}

	protected boolean exists(Subformula formula) {
		for (int i=0; i<sequences.size(); i++) {
			if (equals(formula, sequences.get(i)))
				return true;
		}
		return false;
	}

	protected String removeDoubles() {
		for (int i=0; i<sequences.size(); i++) {
			for (int j=i+1; j<sequences.size(); j++) {
				if (equals(sequences.get(i), sequences.get(j))) {
					// copy this sequences because it will be deleted afterwards
					String ret = sequences.get(i).print();
					sequences.remove(i);
					return ret;
				}
			}
		}
		return null;
	}
	
	protected boolean removeNegations() {
		for (int i=0; i<sequences.size(); i++) {
			if (sequences.get(i).getNegs() > 1) {
				sequences.get(i).setNegs(sequences.get(i).getNegs()-2);
				return true;
			}
		}
		return false;
	}
	
	protected Subformula shortestFormula(Subformula sub1, Subformula sub2) {
		return (sub1.length() <= sub2.length()) ? sub1 : sub2;
	}
	
	protected void setBranchPoint(int branch) {
		this.branchpoint = branch;
	}
	
	protected int getBranchPoint() {
		return branchpoint;
	}

	protected void printLiterals() {
		String str = "";
		
		boolean first = true;
		for (int i=0; i<sequences.size(); i++) {
			Subformula cur = sequences.get(i);
			if (cur.isLiteral()) {
				str += first ? "" : ", ";
				str += (cur.isNegated()?"-":"") + cur.getVar();
				first = false;
			}
		}
		
		Sys.print(str);
	}
}