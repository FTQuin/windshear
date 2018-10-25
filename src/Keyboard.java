package src;//checked!

import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

/**
* Keyboard listener that relays commands to parent game.
*/
class Keyboard extends KeyAdapter {
	/**
	* An enumeration of all possible key events that are defined for this listener.
	*/
	public enum KeyCode {
		NONE,
		UP,
		DOWN,
		LEFT,
		RIGHT,
		TRANSFER,
		ENTER,
		GEAR,
		BRAKES,
		FLAPS_DN,
		FLAPS_UP,
		SPD_BRAKES,
		XTRA_UI,
		AUTOPILOT,
		EXTEND_BOOM,
		RETRACT_BOOM,
		TARGET,
		PAUSE,
		MENU
	}

	private Game parent;

	/**
	* Create a new key listener for the Game "g".
	*/
	Keyboard(Game g) {
		if(g != null) {
			parent = g;
		} else {
			Debug.print("Keyboard.java:Keyboard(...): g cannot be null");
			throw new IllegalArgumentException("Invalid intializer, see log.");
		}
	}

	/**
	* Called when user presses a key.
	*/
	@Override
	public void keyPressed(KeyEvent e) {
		process(e);
	}

	/*
	* Process a KeyEvent "e" and dispatch command.
	*/
	private void process(KeyEvent e) {
		int keyCode = e.getKeyCode();
		KeyCode code = KeyCode.NONE;

		switch(keyCode) {
			case KeyEvent.VK_A:
				code = KeyCode.LEFT;
				break;
			case KeyEvent.VK_D:
				code = KeyCode.RIGHT;
				break;
			case KeyEvent.VK_UP:
				code = KeyCode.UP;
				break;
			case KeyEvent.VK_DOWN:
				code = KeyCode.DOWN;
				break;
			case KeyEvent.VK_LEFT:
				code = KeyCode.LEFT;
				break;
			case KeyEvent.VK_RIGHT:
				code = KeyCode.RIGHT;
				break;
			case KeyEvent.VK_F:
				code = KeyCode.FLAPS_UP;
				break;
			case KeyEvent.VK_V:
				code = KeyCode.FLAPS_DN;
				break;
			case KeyEvent.VK_W:
				code = KeyCode.TRANSFER;
				break;
			case KeyEvent.VK_G:
				code = KeyCode.GEAR;
				break;
			case KeyEvent.VK_B:
				code = KeyCode.SPD_BRAKES;
				break;
			case KeyEvent.VK_N:
				code = KeyCode.AUTOPILOT;
				break;
			case KeyEvent.VK_M:
				code = KeyCode.RETRACT_BOOM;
				break;
			case KeyEvent.VK_S:
				code = KeyCode.TARGET;
				break;
			case KeyEvent.VK_ENTER:
				code = KeyCode.BRAKES;
				break;
			case KeyEvent.VK_U:
				code = KeyCode.XTRA_UI;
				break;
			case KeyEvent.VK_P:
				code = KeyCode.PAUSE;
				break;
			case KeyEvent.VK_ESCAPE:
				code = KeyCode.MENU;
				break;
		}

		parent.keyInput(code);
	}
}
//EOF
