package nl.marcvanzee.KEProver.parser;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import nl.marcvanzee.KEProver.formula.Subformula;
import nl.marcvanzee.KEProver.lib.Chars;

/**
 * Parses an incoming string that represents a formula in propositional logic, and
 * returns the formula as a SubFormula object, which contains the structure of the formula.
 * 
 * @author marc
 *
 */
public class Parser {	
	public Parser() {
	}

	/**
	 * Read a string and send it back in a Subformula object if it is valid..
	 * 
	 * First, the method strips all spaces and makes sure the string contains only valid characters.
	 * If not, it will be sent to the parse method. Else, null is returned.
	 * 
	 * @param str A string in propositional logic. Allowed characters: a-z,A-Z,0-9,_,(,) and the connectives, as defined in the lib.Chars class.
	 * @return a Subformula object that contains the input formula. returns null if the input was invalid.
	 */
	public Subformula readLine(String str) {
		// get rid of all spaces for parsing
		str = str.replaceAll(" ", "");
		
		// now check if the string does not contain illegal characters.
		// if so, leave immediately. legal characters are:
		// &, |, >, <, [a-z], [A-Z], -, (, ), 0-9, _
		
		Pattern p = Pattern.compile("^[a-zA-Z_0-9" + Chars.AND + Chars.OR + Chars.IMPLIES + Chars.EQUIV + "\\-\\(\\)]+$");
		Matcher m = p.matcher(str);

		return m.find() ? parse(str.toLowerCase()) : null;
	}
	
	/**
	 * This method does the actual parsing, by using the ParseString object.
	 * If the input string contains a connective, the method is called twice recursively with the subformulas as argument.
	 * If not, the input string must be a literal, thus the literal is returned (in a Subformula object).
	 * @param str a validated string in propositional logic.
	 * @return a Subformula object containing a hierarchal structure of the input string
	 */
	private Subformula parse(String str) {
		// instantiate a parseString, which automatically analyzes the input string
		ParseString parseStr = new ParseString(str);
		
		// parseStr.getDepth should be zero, since the depth at the end of the string is zero.
		// if not, we have an invalid number of parentheses
		if (parseStr.getDepth() != 0) {
			return null;
		}
		
		// now see if the string contains a connective
		if (parseStr.getConnectiveLocation() > -1) {
			// check for surrounding parentheses.
			if (parseStr.minDepth() > 0) {
				// we have parseStr.minDepth() surrounding parentheses
				// thus, remove these parentheses, except the outer ones.
				parseStr.removeOuterParentheses();
			} else {
				// add outer parentheses, for parsing purposes
				parseStr.addSingleParentheses();
			}
			
			// parseStr contains now: -*(FORMULA)   (zero or more preceding negations)
			
			// collect both the subformulas and collect them in a Subformula object
			Subformula sub1 = parse(parseStr.getSubformula1());
			Subformula sub2 = parse(parseStr.getSubformula2());
			
			// collect negations preceding this string
			int negations = parseStr.getNegations(parseStr.getString());
			

			return new Subformula(sub1, parseStr.getConnective(), sub2, negations);
			
		} else {
			// we have a proposition letter
			// remove all parentheses
			parseStr.removeAllParentheses();
			
			int negs = parseStr.getNegations(parseStr.getString());
			return new Subformula(parseStr.getString().substring(negs), negs);
		}
	}
}