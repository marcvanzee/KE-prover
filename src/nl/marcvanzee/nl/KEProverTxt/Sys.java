package nl.marcvanzee.nl.KEProverTxt;

public class Sys {
	private static boolean debug = false;
	
	public static void debug(String str) {
		if (debug) System.out.println(str);
	}
	
	public static void setDebug(boolean debug) {
		Sys.debug = debug;
	}
	
	public static void print(String str) {
		System.out.println(str);
	}
}