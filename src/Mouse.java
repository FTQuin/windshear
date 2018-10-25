package src;//checked!

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.Point;

/**
* Mouse listener that relays commands to parent game.
*/
class Mouse extends MouseAdapter {
	/**
	* An enumeration of possible mouse events that are defined for this listener.
	*/
	public enum Code {
		NONE,
		LEFT,
		RIGHT,
		LEFT_DRAG,
		RIGHT_DRAG,
		WHEEL
	}

	private Code code;
	private Game parent;
	private Point last;
	private Point click;
	private Point rightClick;
	private Point drag;
	private Point location;

	/**
	* Create a new mouse listener for the game "g".
	*/
	Mouse(Game g) {
		super();

		if(g != null) {
			parent = g;
		} else {
			Debug.print("Mouse.java:Mouse(...): g cannot be null");
			throw new IllegalArgumentException("Invalid intializer, see log.");
		}

		code = Code.NONE;
		click = null;
		drag = null;
		last = null;
		location = null;
	}

	/**
	* Called any time the cursor is moved.
	*/
	@Override
	public void mouseMoved(MouseEvent e) {
		location = e.getPoint();
	}

	/**
	* Called when user dragged the mouse while holding down a button.
	*/
	@Override
	public void mouseDragged(MouseEvent e) {
		int dX = 0, dY = 0;
		drag = e.getPoint();
		location = e.getPoint();

		if(drag != null && last != null) {
			dX = getDX();
			dY = getDY();
		}

		switch(code) {
			case LEFT:
				code = Code.LEFT_DRAG;
				break;
			case RIGHT:
				code = Code.RIGHT_DRAG;
				break;
		}

		parent.mouseInput(code, dX, dY);
		resetClick();
	}

	/**
	* Called when user pressed a mouse button.
	*/
	@Override
	public void mousePressed(MouseEvent e) {
		int b = e.getButton();
		click = e.getPoint();

		switch(b) {
			case MouseEvent.BUTTON1:
				code = Code.LEFT;
				break;
			case MouseEvent.BUTTON3:
				code = Code.RIGHT;
				break;
			default:
				code = Code.NONE;
				break;
		}

		parent.mouseInput(code, 0 , 0);

		if(Debug.verbose) {
			Debug.print("Debug.java:mousePressed(...): " + Code.values()[code.ordinal()]
				+ " button pressed");
		}
	}

	/**
	* Called when user released a mouse button.
	*/
	@Override
	public void mouseReleased(MouseEvent e) {
		code = Code.NONE;
		resetClick();
	}

	/**
	* Called when user scrolled the mouse wheel.
	*/
	@Override
	public void mouseWheelMoved(MouseWheelEvent e) {
		parent.mouseInput(Code.WHEEL, 0, e.getWheelRotation());
	}

	/*
	* Reset the stored click location.
	*/
	private void resetClick() {
		last = drag;
		drag = null;
	}

	/**
	*	Get a Point object representing screen-space location of cursor.
	*/
	public src.Point getLocation() {
		if(location != null)
			return new src.Point(location.getX(), location.getY());
		else
			return null;
	}

	/**
	*	Get a Point object representing screen-space location of a click.
	*/
	public src.Point getClick() {
		return new src.Point(click.getX(), click.getY());
	}

	/*
	* Returns the screen space horizontal distance the mouse was dragged.
	*/
	private int getDX() {
		return (int)(drag.getX() - last.getX());
	}

	/*
	* Returns the screen space vertical distance the mouse was dragged.
	*/
	private int getDY() {
		return (int)(drag.getY() - last.getY());
	}
}
//EOF
