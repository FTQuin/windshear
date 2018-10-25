package src;

import src.Aircraft.Type;

/**
* A service class for entry into the windshear application and testing of its functionality.
*/
public class Test {
	/**
	* Test environment entry point.
	* Usage: Test +"TEST_NAME"
	* Game engine: "ENG"
	*/
	public static void main(String[] args) {
		final char flag = '+';
		final char option = '-';
		final String testGameFlag = "ENG";
		final String p1Option = "P1";
		final String p2Option = "P2";
		boolean valid;
		Aircraft.Type type;
		String arg;

		if(args.length < 1) {
			Debug.print("Test.java usage: Test +TEST_NAME +TEST_NAME_2 ...");
			Debug.displayMessages();
			return;
		}

		type = null;

		for(int i = 0; i < args.length; i++) {//process options first
			if(args[i].charAt(0) == option && args[i].length() >= 2) {
				arg = args[i].substring(1);

				if(arg.equals(p1Option)) {
					type = Aircraft.Type.SHUTTLE;
					Debug.print("Test.java: starting game as player 1 (SHUTTLE)");
				} else if(arg.equals(p2Option)) {
					type = Aircraft.Type.TANKER;
					Debug.print("Test.java: starting game as player 2 (Tanker)");
				}
			}
		}

		for(int i = 0; i < args.length; i++) {//then execute tests
			if(args[i].charAt(0) == flag && args[i].length() >= 2) {
				arg = args[i].substring(1);
				if(arg.equals(testGameFlag)) {
					testGame(type);
					Debug.displayMessages();
				}
			}
		}
	}

	/*
	* Tests the game engine with the aircraft type "c".
	*/
	private static void testGame(Aircraft.Type c) {
		Debug.print("Test.java:testGame(...): launching new game");
		new Game(c);
	}
}
//End of file
