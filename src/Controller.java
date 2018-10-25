package src;
import java.util.Random;

/**
* A utility class that manages an Environment object according to parameters
* defined by the difficulty setting and other constants. Used to populate the environment
* with wind vectors and control simulated wind shear events.
*/
class Controller {
	/**
	* Difficulty scale is set according to how many tries it should take an average
	* player to successfully complete an objective. This is chosen to avoid arbitrary definitions
	* of difficulty that don't suggest a design philosophy, like "easy, medium, hard".
	*/
	public static enum Difficulty {
		EASY,
		MEDIUM,
		HARD
	}

	/*class constants and wind generator parameters*/
	private static final double upAngleRange = 2.75d;
	private static final double shearAngleRange = 0.78d;
	private static final double downAngleRange = 2.75d;
	//private static final int D_MAX_WIND_SPEED = Map.MAX_WIND;
	//private static final int D_MAX_WIND_SPEED_5T = 50;
	//private static final int D_MAX_WIND_SPEED_10T = 70;
	private static final int D_MAJOR_WIND_EVENT_SEED = 66;
	private static final int D_WIND_EVENT_DURATION = 33;
	private static final int WIND_VARIATION_SEED = 15;
	private static final Difficulty D_DIFFICULTY = Difficulty.EASY;

	/*instance variables*/
	private Environment environment;
	private Difficulty difficulty;
	private Random randomGenerator;
	private int maxWindSpeed;
	private int windEventSeed;
	private int windEventDuration;

	/**
	* Create a new environment controller.
	* @param e - the Environment object to manipulate
	*/
	Controller(Environment e){
		if(e != null) {
			environment = e;
		} else {
			Debug.print("Controller.java: Environment e cannot be null");
			throw new IllegalArgumentException("Controller.java: invalid initializer, see log");
		}
		randomGenerator = new Random();
		setDifficulty(D_DIFFICULTY);
	}

	/**
	* Set the difficulty of the environment controller AI.
	* See enum Difficulty for possible values.
	*/
	public void setDifficulty(Difficulty d) {
		difficulty = d;

		switch(difficulty) {
			case EASY:
				windEventSeed = D_MAJOR_WIND_EVENT_SEED;
				windEventDuration = D_WIND_EVENT_DURATION;
				maxWindSpeed = Map.MAX_WIND;
				break;
			case MEDIUM:
				windEventSeed = D_MAJOR_WIND_EVENT_SEED / 2;
				windEventDuration = D_WIND_EVENT_DURATION * 2;
				maxWindSpeed = Map.MAX_WIND * 2;
				break;
			case HARD:
				windEventSeed = D_MAJOR_WIND_EVENT_SEED / 3;
				windEventDuration = D_WIND_EVENT_DURATION * 3;
				maxWindSpeed = Map.MAX_WIND * 3;
				break;
			default:
				break;
		}
	}

	/**
	* Generate a major wind event with a random direction and speed.
	*/
	public Wind majorWindEvent(double x, double y) {
		Wind w;
		double direction;
		int speed;
		int random;//temp
		random = randomGenerator.nextInt(windEventSeed)+1;
		if(random % windEventSeed == 0) {
			direction = randomGenerator.nextDouble() * (2*Math.PI);
			speed = maxWindSpeed;
			w = new Wind(direction, speed);
		} else {
			w = null;
		}
		return w;
	}

	/**
	* ...
	*/
	public int getWindEventDuration() {
		return windEventDuration;
	}

	/**
	* Populate the environment with random winds, with ratio of winds in 
	* the horizontal direction (shear) to vertical (up or down drafts) determined
	* by WIND_VARIATION_SEED.
	*/
	public void generateWinds() {
		int random;
		int numWinds = 0;
		Wind w;
		
		for(int y = 0; y < environment.getHeight() / environment.getScale(); y++) {
			for(int x = 0; x < environment.getWidth() / environment.getScale(); x++) {
				random = randomGenerator.nextInt(WIND_VARIATION_SEED) + 1;//increment by 1 to handle 0 case for mod operation
				
				if(x % random == 0)
					w = generateUp();
				else if(x % random == 1)
					w = generateDown();
				else
					w = generateShear();
				
				environment.setEnvWind(w, y, x);
				numWinds++;
			}
		}
		
		if(Debug.verbose) {
			Debug.print("Controller.java: generated " + numWinds + " winds");
		}
	}

	/*Generate a Wind with a direction "up"*/
	private Wind generateUp() {
		double direction;
		double speed;
		direction = randomGenerator.nextDouble() * upAngleRange;
		speed = randomGenerator.nextDouble()*maxWindSpeed;
		if(Debug.verbose) {
			//Debug.print("Controller.java: generating \"up\" Wind, direction " + direction);
		}
		return new Wind(direction, speed);
	}

	/*Generate a Wind with a direction "down"*/
	private Wind generateDown() {
		double direction;
		double speed;
		direction = upAngleRange + shearAngleRange
			+ randomGenerator.nextDouble() * downAngleRange;
		speed = randomGenerator.nextDouble()*maxWindSpeed;
		if(Debug.verbose) {
			//Debug.print("Controller.java: generating \"down\" Wind, direction " + direction);
		}
		return new Wind(direction, speed);
	}

	/*Generate a Wind with a direction considered "shear"*/
	private Wind generateShear() {
		double direction;
		double speed;
		direction = upAngleRange + randomGenerator.nextDouble() * shearAngleRange;
		speed = randomGenerator.nextDouble()*maxWindSpeed;
		if(Debug.verbose) {
			//Debug.print("Controller.java: generating \"shear\" Wind, direction " + direction);
		}
		return new Wind(direction, speed);
	}
}
//End of file
