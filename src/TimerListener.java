package src;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
* Game clock listener. Receives events from the game clock and synchronizes with parent
* before calling the method to start a new frame.
*/
class TimerListener implements ActionListener {
	private Game game;
	private Type type;
	private double delay;
	
	public enum Type {
		GAME_CLOCK,
		DELAYED_EVENT
	}
	
	/**
	* Create a new clock listener for parent Game "g".
	*/
	TimerListener(Game g, Type t) {
		if(g != null) {
			game = g;
		} else {
			Debug.print("TimerListener.java:TimerListener(...): must be associated with a Game, g cannot be null");
			throw new IllegalArgumentException("TimerListener.java:TimerListener(...): " +	"g is null");
		}
		if(t != null) {
			type = t;
		} else {
			Debug.print("TimerListener.java:TimerListener(...): must have a Type, t cannot be null");
			throw new IllegalArgumentException("TimerListener.java:TimerListener(...): " +	"t is null");
		}
	}

	/**
	* Receives tick events from associated clock.
	*/
	@Override
	public void actionPerformed(ActionEvent e) {
		if(type == Type.GAME_CLOCK) {
			if(game.ready())
				game.newFrame();
			else
				if(Debug.verbose)
					Debug.print("TimerListener.java: Previous frame not completed");
		} else {
			game.triggerEvent();
		}
	}
}
//end of file
