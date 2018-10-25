package src;//checked!

import javax.swing.JFrame;

/**
 * A basic desktop window that houses the game screen.
 */
class Window extends JFrame {
	private static final boolean resizable = false;
	private static final boolean undecorated = false;

	/**
	* Create a new Window.
	*/
	Window() {
		super();

		if(Debug.verbose)
			Debug.print("Window.java:Window(): Window created");

		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setResizable(resizable);
		setUndecorated(undecorated);
	}
}
//EOF
