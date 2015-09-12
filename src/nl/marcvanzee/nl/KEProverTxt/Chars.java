package nl.marcvanzee.nl.KEProverTxt;

public class Chars {
	public static final char AND 		= '&';
	public static final char OR 		= '|';
	public static final char IMPLIES 	= '>';
	public static final char EQUIV  	= '<';
	public static final char NEG		= '-';
	public static final char LEFT_P		= '(';
	public static final char RIGHT_P	= ')';
	public static final char INPUT_STOP	= '.';
	
	public static boolean isConnective(char c) {
		return ((c == AND) || (c == OR) ||
				(c == IMPLIES) || (c == EQUIV));
	}
}
