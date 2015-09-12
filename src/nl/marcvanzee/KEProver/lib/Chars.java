package nl.marcvanzee.KEProver.lib;

/**
 * this class contains all constants: the connectives, the parentheses and the negation symbol.<br>
 * furthermore, it contains a method for connective verification.
 * 
 * @author marc
 *
 */
public class Chars {
	public static final char AND 		= '&';
	public static final char OR 		= '|';
	public static final char IMPLIES 	= '>';
	public static final char EQUIV  	= '<';
	public static final char NEG		= '-';
	public static final char LEFT_P		= '(';
	public static final char RIGHT_P	= ')';
	
	public static boolean isConnective(char c) {
		return ((c == AND) || (c == OR) ||
				(c == IMPLIES) || (c == EQUIV));
	}
}
