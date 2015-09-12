package nl.marcvanzee.nl.KEProverTxt;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Vector;

public class Parser {	
	public Parser() {
	}
	
	public Vector<Subformula> readLines(char delimiter) {
		InputStreamReader inp = new InputStreamReader(System.in);
		BufferedReader br = new BufferedReader(inp);
		Vector <String> input = new Vector<String>();
		Vector <Subformula> sequences = new Vector<Subformula>();
		
		Sys.debug("--- PARSER OUTPUT");
		while (true) {
			String str;
			try {
				str = br.readLine();
				if (str.charAt(0) == Chars.INPUT_STOP) break;
				input.add(str.toLowerCase());
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		for (int i=0; i<input.size(); i++) {
			// get rid of additional spaces
			String noSpaces = input.get(i).replaceAll(" ", "");
			input.set(i, noSpaces);
			sequences.add(parse(input.get(i)));
			if (sequences.get(i) == null) {
				Sys.debug("* incorrect syntax in sequence: \"" + input.get(i) + "\", ignoring...");
				sequences.remove(i);
			}
		}
		
		Sys.debug("* parsing finished! resulting sequences:");
		printSequences(sequences);
		return sequences;
	}
	
	public Subformula parse(String str) {
		int location = -1;				// location of the connective
		int negs = 0;					// number of negations prior to the formula
		char c;
		int depth = 0;					// depth of the parenthesis
		int minDepth = -1;				// minimal number of parenthesis inside the sequence
		
		for (int i=0; i<str.length(); i++) {
			c = str.charAt(i);
			if (c == Chars.LEFT_P) {
				depth++;
			} else if (c == Chars.RIGHT_P) {
				depth--;
			} else {
				if (Chars.isConnective(c)) {
					// check whether this is the connective that we have to use in this formula!
					
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
		
		if (depth != 0) {
			// invalid number of parentheses
			return null;
		}
				
		
		if (location > -1) {
			// we have a connective
			
			if (minDepth > 0) {
				// we have <minDepth> surrounding parentheses
				// thus, remove these parentheses, but keep the negations in.
				// if there are negations, we leave the last parentheses in,
				// else the negation will not belong to the entire sequence anymore
				for (int i=1; i<minDepth; i++) {
					str = str.replaceFirst("\\(", "");
					str = str.replaceAll("\\)$", "");
				}
				negs = countNegations(str);
				
				if (str.charAt(0) != Chars.NEG) {
					str = str.substring(1, str.length()-1);
				}
			}
			
			// if we had negations, this means that we will have one extra pair
			// of parentheses, because we have a connective:
			// -----* (p>q)     we need the connective because of precedence
			// removeParentheses() makes sure that we have only 1 pair of parentheses
			
			// therefore we need to change the offsets for the subformulas
			int start = (negs > 0) ? (negs + 1) : 0;
			int end   = (negs > 0) ? str.length() - 1 : str.length();
			location  = (negs > 0) ? location - minDepth + 1 : location - minDepth;
			
			Subformula sub1 = parse(str.substring(start, location));
			Subformula sub2 = parse(str.substring(location+1,end));
			
			return new Subformula(sub1, str.charAt(location), sub2, negs);
		} else {
			// we have a proposition letter
			//str = removeParentheses(str, minDepth, true, false);
			str = str.replaceAll("(\\(|\\))", "");
			negs = countNegations(str);
			return new Subformula(str.substring(negs), negs);
		}
	}

	public void waitForEnter() {
		InputStreamReader inp = new InputStreamReader(System.in);
		BufferedReader br = new BufferedReader(inp);
		try {
			br.readLine();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private int countNegations(String str) {
			return (str.charAt(0) == Chars.NEG) ? 1 + countNegations(str.substring(1)) : 0;
	}

	private void printSequences(Vector <Subformula> sequences) {
		for (int i=0; i<sequences.size(); i++) {
			Sys.debug(sequences.get(i).print());
		}
	}
}