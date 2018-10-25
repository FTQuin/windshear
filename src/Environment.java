package src;//checked!

/**
* A 2-dimensional atmosphere composed of an array of Wind vectors,
* which are varied according to the game's difficulty settings.
*/
class Environment {
	/*default constants*/
	//transition duration and cell size are dependent
	private static final double TRANSITION_DURATION = 3.0 * Game.NS_PER_S;
	private static final double WIND_DEFAULT_SPEED = 3.0;//mps
	private static final double WIND_DEFAULT_DIRECTION = Math.PI;
	private static final double WIND_ENGINE_FRICTION = 0.1;
	private static final int WIND_ENGINE_MASS = 1000;
	/*limits*/
	private static final int MIN_SCALE = 500;
	private static final int MIN_WIDTH = 100000;
	private static final int MIN_HEIGHT = 20000;
	/*instance variables, one-time initialization in cstr*/
	private final int envWidth;//number of wind vectors
	private final int envHeight;//number of wind vectors
	private final int scale;//the number of meters covered per wind vector
	private boolean transitionPlaying;
	private double time;
	private double windEngineRadius;
	private double windEngineSpeed;
	private Wind[][] env;	/*an n-by-n array of wind vectors represents the 2-dimensional environment*/
	private Wind actingWind;
	private Wind currentWind;
	private Wind previousWind;
	private Queue actingWinds;

	public Wind getActingWind() {
		return actingWind;
	}

	public void updateWindEngine(double step, Point p) {
		Wind tempWind1 = null, tempWind2 = null;
		Wind w = getWindOnAircraft(p);
		//Debug.print("step " + step);
		
		if(transitionPlaying) {
			time += step;
			//Debug.print("tranny playing!");

			if(time > TRANSITION_DURATION) {
				time = 0;
				transitionPlaying = false;
				actingWinds.pull();
			}
		}
		
		//System.out.println("actingWinds.size() " + actingWinds.size());
		
		if(actingWinds.size() > 1)
			transitionPlaying = true;	

			
		if(w != null && !actingWinds.contains(w)) {
			actingWinds.push(w);
			
			//previousWind = currentWind;
			//currentWind = w;
			//transitionPlaying = true;
		}
		
		//if(actingWinds.size() > 1)
		//	currentWind = (Wind) actingWinds.get(1);
		//else
		//	currentWind = (Wind) actingWinds.get(0);
		
		//if(previousWind != null)
			//tempWind1 = new Wind(previousWind);
		if(actingWinds.size() > 0)
			tempWind1 = new Wind((Wind) actingWinds.get(0));
		//tempWind2 = new Wind(currentWind);
		if(actingWinds.size() > 1) {
			tempWind2 = new Wind((Wind) actingWinds.get(1));
			
		}
		
		if(transitionPlaying && tempWind1 != null && tempWind2 != null) {
			double windScale = time / TRANSITION_DURATION;
			tempWind1.getVector().scale(1 - windScale);
			tempWind2.getVector().scale(windScale);
			actingWind = new Wind(Vector.add(tempWind1.getVector(), tempWind2.getVector()));
			//Debug.print("acting wind mag " + actingWind.getVector());
		} else {
			actingWind = tempWind1;
		}
	}

	/**
	* Create a standard Environment.
	* w - width in meters
	* h - height in meters
	* s - scale (meters:cell)
	*/
	Environment(double w, double h, double s) {
		/*check validity of arguments before assignment*/
		if(s >= MIN_SCALE) {
			scale = (int)s;
		} else {
			if(Debug.verbose)
				Debug.print("Environment.java: scale must be at least " + MIN_SCALE);

			scale = MIN_SCALE;
		}

		transitionPlaying = false;
		time = 0;
		currentWind = null;
		previousWind = null;
		actingWinds = new Queue();
		windEngineRadius = scale / 2;
		windEngineSpeed = 0;

		if(w > MIN_WIDTH) {
			envWidth = (int)(w / scale);
		} else {
			if(Debug.verbose)
				Debug.print("Environment.java: width must be at least " + MIN_WIDTH);

			envWidth = (int)(MIN_WIDTH / scale);
		}

		if(h > MIN_HEIGHT) {
			envHeight = (int)(h / scale);
		} else {
			if(Debug.verbose)
				Debug.print("Environment.java: height must be at least " + MIN_HEIGHT);

			envHeight = (int)(MIN_HEIGHT / scale);
		}

		env = new Wind[envHeight][envWidth];//create the environment 2d array
		initEnvWithDefaults();//populate the array with defaults
	}

	/**
	* Returns true if a given coordinate "x, y" is contained within this environment's bounds.
	*/
	public boolean inBounds(double x, double y) {
		boolean b = false;

		if(x >= 0 && x < envWidth * scale) {
			if(y >= 0 && y < envHeight * scale)
				b = true;
		}

		return b;
	}

	/**
	* ...
	*/
	public int getRow(double y) {
		return (int) (y / scale);
	}

	/**
	* ...
	*/
	public int getCol(double x) {
		return (int) (x / scale);
	}

	/**
	* Populate cell at row "r" and column "c" with a wind "v".
	*/
	public void setEnvWind(Wind v, int r, int c) {
		/*make sure r and c are within range*/
		if(r < 0 || r >= envHeight) {
			if(Debug.verbose)
				Debug.print("Environment.java: cell specified is outside bounds of environment");

			r = 0;//default cell 0
		}

		if(c < 0 || c >= envWidth) {
			if(Debug.verbose)
				Debug.print("Environment.java: cell specified is outside bounds of environment");

			c = 0;//default cell 0
		}

		if(v != null) {
			env[r][c] = v;
		} else {
			if(Debug.verbose)
				Debug.print("Environment.java: wind vector cannot be null");
		}
	}

	/**
	* Given a position "x,y" in the environment, returns the wind acting on that point.
	*/
	public Wind getWindOnAircraft(Point p) {
		return getEnvWind((int) (p.getX() / scale), (int) (p.getY() / scale));
	}

	/**
	* Returns the wind vector at row "r" and column "c" in the environment.
	*/
	public Wind getEnvWind(int r, int c) {
		boolean inputValid = true;
		Wind wind;

		/*make sure r and c are within range*/
		if((r < 0 || r >= envHeight) || (c < 0 || c >= envWidth)) {
			if(Debug.verbose)
				Debug.print("Environment.java: cell (" + r + ", " + c + ") is out of bounds");

			inputValid = false;
		}

		if(inputValid)
			wind = env[r][c];
		else
			wind = new Wind(0, 0);

		return wind;
	}

	/**
	* Return the scale of the environment meters:cell
	*/
	public double getScale() {
		return scale;
	}

	/**
	* Return the width of the environment.
	*/
	public int getWidth() {
		return (int)(envWidth * scale);
	}

	/**
	* Returns the height of the environment.
	*/
	public int getHeight() {
		return (int)(envHeight * scale);
	}

	/*
	* Service method: initialize the environment's wind vectors with defaults.
	*/
	private void initEnvWithDefaults() {
		Wind w = new Wind(WIND_DEFAULT_DIRECTION, WIND_DEFAULT_SPEED);

		for(int r=0; r<envHeight; r++) {
			for(int c=0; c<envWidth; c++) {
				setEnvWind(w, r, c);
			}
		}
	}
}
//EOF
