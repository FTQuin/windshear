package src;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Scanner;
import java.io.File;
import java.io.FileNotFoundException;


/**
* A basic aircraft physics model using a Newtonian view of lift.
*/
class Aircraft implements Collidable {

	enum Type {
		SHUTTLE,
		TANKER
	}

	//physics simulation constants
	private static final int MAX_SPEED = 8000;
	private static final double G = 9.81;
	private static final double PEAK_DRAG_AOA = 50.0;
	private static final double C_ANGULAR_TORQUE = 0.025;
	private static final double C_ELEV_INPUT = 7.0;
	/* elevator deflection is proportional to inverse of air speed, set lower bound to stop
	   large deflection as air speed approaches 0 */
	private static final double MIN_ELEV_AIRFLOW = 30.0;
	private static final int BOOM_SIMS_PER_FRAME = 15;
	private static final int N_SPEED_RECORDS = 2;//must be even or calcAverageSpeed will throw null pointer exception
	/*aircraft physics coefficients*/
	private static final double C_KNOT_START = 0.1;
	private static final double C_BOOM_SPRING = 70;
	private static final double C_BOOM_DAMP = 50;
	private static final double C_BOOM_ANGULAR_DAMP = 0.001;
	private static final double C_ROOT_RESIST = 50;
	private static final double SPRING_BOTTOM_RANGE = 0.05;
	private static final double C_MAIN_SPRING_BOTTOM = 3.0;
	private static final double C_NOSE_SPRING_BOTTOM = 1.7;
	private static final double MIN_BRAKE_MOTION = 0.98;
	private static final double MAIN_SPRING_DAMP = 50000;
	private static final double MAIN_SPRING_STIFFNESS = 5.0;
	public static final int BOOM_THICKNESS = 3;
	public static final int BOOM_NUM_SECTIONS = 10;
	public static final double BOOM_SECTION_LENGTH = 0.5;
	public static final double BOOM_SECTION_MASS = 1;
	public static final double D_SPRING_STIFFNESS = 0;//unused
	public static final double NOZZLE_MASS = 5;

	public static final int FLIGHT_CEILING = 20000;
	public static final double C_CEILING_DENSITY = 0.17;
	public static final double C_SEALEVEL_DENSITY = 1.0;
	public static final double C_ANGULAR_DRAG = 5.0;

	private boolean initialized;
	private boolean outputToggled;

	public Point getLocation() {
		return new Point(location);
	}


	public double getClimbRate() {
		double climb = motion.getY();
		return climb;

	}

	private double getCDensity(int a) {
		double c;

		//Debug.print("aglMain " + a);

		if(a <= 0) {
			c = C_SEALEVEL_DENSITY;//100% density at 0m above MSL
		} else {
			//reduce sea level density by the fraction given by altitude divided by flight ceiling
			c = C_SEALEVEL_DENSITY - ((double) a / FLIGHT_CEILING) * C_SEALEVEL_DENSITY;

			if(c < C_CEILING_DENSITY) {
				c = C_CEILING_DENSITY;
			}

			//Debug.print("density " + c);
		}

		return c;
	}

	/* MOST OF THIS SHOULD BE REFACTORED TO AN ASSOCIATIVE DATA STRUCTURE */

	/*control limits and scales*/

	/*speed and direction in environment*/
	private Vector motion;
	/*properties*/
	private Type type;
	private boolean mainDown;
	private boolean noseDown;
	private boolean gearStowed;
	private boolean brakes;
	private boolean spdBrakes;
	private boolean gearInTransit;
	private double angleToHorizon;
	private double previousAngleToHorizon;
	private double throttleSetting;
	private double minThrottleSetting;
	private double maxThrottleSetting;
	private double maxThrust;
	private double minThrust;
	private double iThrust;
	private double mass;
	private double surfaceArea;
	private double wingArea;
	private double cArea;
	private double trim;
	private double lastSpd;
	private double mainSpring;
	private double noseSpring;
	private double aglMain;
	private double aglNose;
	private double aglSkid;
	private double cRotateDrag;
	private double averageSpeed;
	private double cLift;// = 0.5;
	private double maxCLift;// = 1.0;
	private double minCArea;// = 0.1;
	private double maxCArea;// = 1.0;
	private double cDrag;// = 0.005;
	private double cDragMax;// = 0.005;
	private double baseCDrag;// = 0.002;
    private double cGear;// = 0.001;
	private double cFlapsDrag;// = 0.0001;
	private double cFlapsLift;// = 0.008;
	private double cSpdBrkDrag;//= 0.003;
	private double cSpdBrkLift;// = 0.20;
	private double cRoll;// = 1.0;
	private double cBrakes;// = 0.2;
	private double skidTolerance;// = 0.001;
	public double mainGearHeight;// = 2.0;
	private double mainSpringTravel;// = 1.25;
	private double mainSpringMin;// = 0.001;
	public double noseGearHeight;// = 1.75;
	private double noseSpringTravel;// = 1.0;
	private double noseSpringMin;// = 0.001;//insig compared to base
	private double cSkidDamp;// = 0.1;
	private double cNoseDamp;// = 0.1;
	private double cNoseSpring;// = 0.1;
	private double cMainDamp;// = 0.1;
	private double cMainSpring;// = 0.1;
	private double cAngularResist;// = 0.1;
	private double mainSpringBase;// = 0.75;
	private double noseSpringBase;// = 0.1;
	public double cBoomSpringFriction;//  = 3.0;
	public double cBoomDrag;//  = 1.0;
	public double cBoomNozzleDrag;//  = 1.0;
	public double cBoomSpring;//  = 1.0;
	public double cBoomDamp;//  = 1.0;
	public double cBoomAngularDamp;//  = 1.0;
	private double aoi;// = 9.0;
	private double peakLiftAoa;// = 21.0;
	private double maxAoa;// = 60.0;
	private double maxFlaps;// = 30;
	private double flapIncrement;// = 10;
	private Vector nozzleForce;
	private double strutLimit;// = 15.0;
	private double cElev;// = 3.0;
	private double cSkid ;//= 100.0;
	public double maxElev ;//= 20.0;
	private int flaps;
	private Wind actingWind;
	private Point location;
	private Point previousLocation;
	private double previousStep;
	private double currentStep;
	private double targetStep;
	private double angularSpeed;
	private Point noseGear;
	private Point mainGear;
	private Point noseWheel;
	private Point mainWheel;
	private Point center;
	private Point tail;
	private Point nose;
	private Point bottom;
	private Point top;
	private Point origin;
	private Point fin;
	private Point exhaust;
	private Point skid;
	private Point frontSkid;
	private Point imageMidpoint;
	private Point nozzle;
	private Point topBeacon;
	private Point bottomBeacon;
	private Point stab;
	private Point frontStab;
	private Point topNose;
	private double apRotate;
	private double apElevator;
	private double apClimb;
	private double apAccel;
	private double apMaxPitch;
	private double apMaxElev;
	private double noseWeight;
	private double tailWeight;
	private double elevator;
	private String resourcePath;
	private Hose fuelBoom;
	private double[] speedRecords;
	private Queue locations;
	private double speedRecordTimeElapsed;
	private double airspeed;
	public static final String tankerPath = "1011_500.txt";
	public static final String crjPath = "crj_200.txt";
	public static final int N_PARAMETERS = 63;
	public static final int N_VALUE_PAIRS = 30;
	public static final int VALUE_PAIRS_START = N_PARAMETERS - N_VALUE_PAIRS;
	private double fuelTank;
	private double tankSize;
	private Vector lift;
	private Vector gravity;
	private Vector thrust;
	private Vector drag;
	private Vector normal;
	private double elevatorForce;
	private double noseForce;
	private double noseNormalForce;
	private double tailForce;
	private double skidForce;
	private double resistForce;
	private int imageIndex;

	public double getMaxElevator() {
		return maxElev;
	}
	
	public String getResourcePath() {
		return resourcePath;
	}
	
	public int getImageIndex() {
		return imageIndex;
	}
	
	public Point getImageMidpoint() {
		return imageMidpoint;
	}

	public double[] getAutopilotSettings() {
		double settings[] = new double[6];

		settings[0] = apRotate;
		settings[1] = apElevator;
		settings[2] = apClimb;
		settings[3] = apAccel;
		settings[4] = apMaxPitch;
		settings[5] = apMaxElev;

		return settings;
	}

	private void loadAircraft(Type t) throws IllegalArgumentException {
		File f = null;
		String path = null;
		Scanner scan = null;
		String s = null;;
		String[] tokens = null;
		String[] parameters = null;
		double[][] pairs;
		int c = 0;

		if(t == Type.SHUTTLE) {
			path = crjPath;
		} else if(t == Type.TANKER) {
			path = tankerPath;
		}

		type = t;

		f = new File(path);
		parameters = new String[N_PARAMETERS];
		pairs = new double[N_VALUE_PAIRS][2];

		try {
			scan = new Scanner(f);
		} catch(FileNotFoundException e) {
			Debug.print("Aircraft.java:loadAircraft(): error loading file " + path);
			e.printStackTrace();
		}

		if(scan != null) {

			while(scan.hasNextLine()) {
				s = scan.nextLine();

				//System.out.println("line " + s);

				tokens = s.split("#");

				//System.out.println("tokens " + tokens[0] + " " + tokens[1]);


				parameters[c] = tokens[0];


				//System.out.println("c " + c + " " + parameters[c]);

				if(c >= VALUE_PAIRS_START) {
					tokens = parameters[c].split(",");
					pairs[c - VALUE_PAIRS_START] = new double[] {Double.parseDouble(tokens[0])
						, Double.parseDouble(tokens[1])};
				}

				c++;
			}

			/*if(Debug.verbose) {
				for(int i = 0; i < parameters.length; i++) {
					Debug.print(" " + parameters[i]);
				}
			}*/
			
			if(c == N_PARAMETERS) {
				c = 0;
				mass = Double.parseDouble(parameters[c++]);
				maxThrust = Double.parseDouble(parameters[c++]);
				surfaceArea = Double.parseDouble(parameters[c++]);
				wingArea = Double.parseDouble(parameters[c++]);
				aoi = Double.parseDouble(parameters[c++]);
				cElev = Double.parseDouble(parameters[c++]);
				cGear = Double.parseDouble(parameters[c++]);
				cBrakes = Double.parseDouble(parameters[c++]);
				cRoll = Double.parseDouble(parameters[c++]);
				cSkid = Double.parseDouble(parameters[c++]);
				cSkidDamp = Double.parseDouble(parameters[c++]);
				cNoseSpring = Double.parseDouble(parameters[c++]);
				cNoseDamp = Double.parseDouble(parameters[c++]);
				cMainSpring = Double.parseDouble(parameters[c++]);
				cMainDamp = Double.parseDouble(parameters[c++]);
				skidTolerance = Double.parseDouble(parameters[c++]);
				noseWeight = Double.parseDouble(parameters[c++]);
				tailWeight = Double.parseDouble(parameters[c++]);
				maxElev = Double.parseDouble(parameters[c++]);
				minThrottleSetting = Double.parseDouble(parameters[c++]);
				iThrust = maxThrust * Double.parseDouble(parameters[c++]);
				resourcePath = parameters[c++];
				imageIndex = Integer.parseInt(parameters[c++].trim());
				apRotate = Double.parseDouble(parameters[c++]);
				apElevator = Double.parseDouble(parameters[c++]);
				apClimb = Double.parseDouble(parameters[c++]);
				apAccel = Double.parseDouble(parameters[c++]);
				apMaxPitch = Double.parseDouble(parameters[c++]);
				apMaxElev = Double.parseDouble(parameters[c++]);
				cAngularResist = Double.parseDouble(parameters[c++]);
				cRotateDrag = C_ANGULAR_DRAG;
				cDragMax = Double.parseDouble(parameters[c++]);
				fuelTank = Double.parseDouble(parameters[c++]);
				tankSize = Double.parseDouble(parameters[c++]);
				c = 0;
				peakLiftAoa = pairs[c][0];
				maxAoa = pairs[c++][1];
				cLift = pairs[c][0];
				maxCLift = pairs[c++][1];
				cArea = pairs[c][0];
				maxCArea = pairs[c++][1];
				cDrag = pairs[c][0];
				baseCDrag = pairs[c++][1];
				flapIncrement = pairs[c][0];
				maxFlaps = pairs[c++][1];
				cFlapsLift = pairs[c][0];
				cFlapsDrag = pairs[c++][1];
				cSpdBrkLift = pairs[c][0];
				cSpdBrkDrag = pairs[c++][1];
				mainGearHeight = pairs[c][0];
				mainSpringTravel = pairs[c++][1];
				noseGearHeight = pairs[c][0];
				noseSpringTravel = pairs[c++][1];
				mainSpringMin = pairs[c][0];
				mainSpringBase = pairs[c++][1];
				noseSpringMin = pairs[c][0];
				noseSpringBase = pairs[c++][1];
				cBoomNozzleDrag = pairs[c][0];
				cBoomDrag = pairs[c++][1];
				cBoomSpring = C_BOOM_SPRING;
				cBoomDamp = C_BOOM_DAMP;
				cBoomAngularDamp = C_BOOM_ANGULAR_DAMP;
				tail = new Point(pairs[c][0], pairs[c++][1]);
				fin = new Point(pairs[c][0], pairs[c++][1]);
				skid = new Point(pairs[c][0], pairs[c++][1]);
				exhaust = new Point(pairs[c][0], pairs[c++][1]);
				top = new Point(pairs[c][0], pairs[c++][1]);
				bottom = new Point(pairs[c][0], pairs[c++][1]);
				center = new Point(pairs[c][0], pairs[c++][1]);
				mainGear = new Point(pairs[c][0], pairs[c++][1]);
				mainWheel = new Point(mainGear.getX(), mainGear.getY() - mainGearHeight);
				frontSkid = new Point(pairs[c][0], pairs[c++][1]);
				noseGear = new Point(pairs[c][0], pairs[c++][1]);
				noseWheel = new Point(noseGear.getX(), noseGear.getY() - noseGearHeight);
				nose = new Point(pairs[c][0], pairs[c++][1]);
				nozzle = new Point(pairs[c][0], pairs[c++][1]);
				topBeacon = new Point(pairs[c][0], pairs[c++][1]);
				bottomBeacon = new Point(pairs[c][0], pairs[c++][1]);
				stab = new Point(pairs[c][0], pairs[c++][1]);
				frontStab = new Point(pairs[c][0], pairs[c++][1]);
				topNose = new Point(pairs[c][0], pairs[c++][1]);
				imageMidpoint = new Point(pairs[c][0], pairs[c++][1]);
			} else {
				Debug.print("Aircraft.java:loadAircraft(): error, not all parameters were read");
			}
		}
	}

	private void addLocationRecord(Point p) {
		locations.push(p);
		if(locations.isFull())
			airspeed = calcAverageSpeed();//no longer used
	}

	public double getAirspeed() {
		double a;
		//air resist vector of ambient air
		Vector r = new Vector(0, 0);
		
		
		Vector windResist = getWindResistance();
		Vector chordline = new Vector(getChordline()).normalize();
		chordline.setDirection(chordline.getDirection() + Math.toRadians(aoi));
		double dp = windResist.getX() * chordline.getX()
			+ windResist.getY() * chordline.getY();
		//double aoa = Math.acos(Math.abs(dp / (windResist.getMagnitude()
		//	* chordline.getMagnitude())));
		//return aoa;
		//r.setDirection(chordline.getDirection());
		//r.setMagnitude(Math.abs(dp));
		//a = getWindResistance().getMagnitude();
		//Debug.print("wind resist mag is " + a);
		return dp;
	}

	public double calcAverageSpeed() {
		double s = 0;
		double l;
		double x = 0, y = 0;
		Point p, p1;
		Vector v = new Vector(0, 0);

		Point original;

		//System.out.println("getting avg");

		if(locations.isFull()) {
			//System.out.println("calcing avg");
			l = locations.length();
			while(!locations.isEmpty()) {
				p = (Point) locations.pull();
				p1 = (Point) locations.pull();

				x += p1.getX() - p.getX();
				y += p1.getY() - p.getY();
				//s += ((Double)locations.pull());
				//System.out.println("looping");
			}

			//System.out.println("x and y " + x + " " + y);

			v = Vector.create(x, y);
			//System.out.println("v mag " + v.getMagnitude());

			v.scale(1 / speedRecordTimeElapsed);
			//System.out.println("v mag after scale " + v.getMagnitude());
			speedRecordTimeElapsed = 0;
		}

		//System.out.println("locations length " + locations.length());
		//System.out.println("returning s as " + s);
		return v.getMagnitude();
	}

	public void toggleOutput() {
		outputToggled = (outputToggled) ? false : true;
	}

	/**
	* Create a new aircraft. If type argument is valid, will
	* load aircraft file and initialize its physics state.
	*/
	public Aircraft(Type t, Vector v, Point p, double step) throws IllegalArgumentException {
		initialized = false;
		outputToggled = false;
		brakes = false;
		gearInTransit = false;
		gearStowed = false;
		mainDown = true;
		noseDown = false;
		gearDirection = 0;
		previousStep = targetStep;
		targetStep = step;
		maxThrottleSetting = 1.0;
		aglMain = 0.0;
		flaps = 0;
		trim = 0.0;
		angularSpeed = 0.0;
		elevator = 0.0;
		lastSpd = 0.0;
		mainSpring = 0.0;
		noseSpring = 0.0;
		airspeed = 0;
		averageSpeed = 0.0;
		actingWind = new Wind(0.0, 0.0);

		if(t == null) {
			Debug.print("Aircraft.java:Aircraft(): t cannot be null");
			throw new IllegalArgumentException();
		} else {
			loadAircraft(t);
		}

		System.out.println("creating queue");
		speedRecordTimeElapsed = 0;
		locations = new Queue(N_SPEED_RECORDS);
		locations.setDropout();
		System.out.println("queue created");

		/*speedRecords = new double[N_SPEED_RECORDS];
		for(int i = 0; i < speedRecords.length; i++) {
			speedRecords[i] = -1;
		}*/


		if(p == null) {
			Debug.print("Aircraft.java:Aircraft(): p cannot not be null, using default");
			setLocation(new Point(0.0, 0.0));
		} else {
			setLocation(p);
			//previousLocation = p;
		}

		if(v == null) {
			Debug.print("Aircraft.java:Aircraft(): v cannot not be null, using default");
			motion = new Vector(0.0, 0.0);
		} else {
			motion = v;
			airspeed = v.getMagnitude();
		}

		Debug.print("hose is init to " + motion.getMagnitude());

		if(type == Type.TANKER) {
			fuelBoom = new Hose(getTail(), motion, BOOM_NUM_SECTIONS, BOOM_SECTION_MASS, BOOM_SECTION_LENGTH);
		} else {
			fuelBoom = new Hose(getNose(), motion, 1, BOOM_SECTION_MASS, BOOM_SECTION_LENGTH);
		}
	}

	public double getAngularSpeed() {
		return angularSpeed;
	}

	public Type getType() {
		return type;
	}

	public double getMass() {
		return mass;
	}

	public Vector getThrust() {
		return new Vector(thrust);
	}

	public Vector getLift() {
		return new Vector(lift);
	}

	public Vector getDrag() {
		return new Vector(drag);
	}

	public Vector getGravity() {
		return new Vector(gravity);
	}

	public Vector getNormal() {
		return new Vector(normal);
	}
/*	public Vector[] getLinearForces() {
		return new Vector[]{mass, thrust,};
	}*/

	public double getMainGearHeight() {
		//return mainWheel.getY() - center.getY() + mainSpringTravel;
		return mainSpringTravel;
	}

	/*static {
		tail = new Point(24.02, 12.30);
		nose = new Point(-24.94, -0.92);
		top = new Point(-5.505, 3.003);
		bottom = new Point(-3.336, -4.504);
		fin = new Point(16.76, .167);
		noseGear = new Point(-15.685, -2.50);
		noseWheel = new Point(noseGear.getX(), noseGear.getY() - noseGearHeight);
		mainGear = new Point(1.913, -1.5);
		mainWheel = new Point(mainGear.getX(), mainGear.getY() - mainGearHeight);
		center = new Point(0.0, 0.0);
		exhaust = new Point(24.771, 0.917);
		skid = new Point(12.844, -2.502);
		frontSkid = new Point(-14.262, -3.169);
	}*/
	
	/**
	* 
	*/
	public ArrayList<Point> getMesh() {
		/* the first point added to this mesh is excluded by the collision routine.
		   points added to this list determine how well the collision detection works:
		   ideally a minimum number of points to create a rough bounding polygon of the aircraft
		   is all that is needed */
		ArrayList<Point> mesh = new ArrayList<Point>();
		mesh.add(center);
		mesh.add(tail);
		mesh.add(nose);
		mesh.add(top);
		//mesh.add(nozzle);
		
		mesh.add(topBeacon);
		mesh.add(bottomBeacon);
		mesh.add(stab);
		mesh.add(frontStab);
		mesh.add(bottom);
		//mesh.add(fin);
		//mesh.add(noseGear);
		//mesh.add(noseWheel);
		//mesh.add(mainGear);
		//mesh.add(mainWheel);
		
		//mesh.add(exhaust);
		mesh.add(skid);
		//mesh.add(frontSkid);
		mesh.add(topNose);

		return mesh;
	}

	/*public Point simFramesAhead(double f) {
		double newX = p.getX() + playerAircraft.getVector().getX() * (f / Game.TARGET_FPS);
		double newY = p.getY() + playerAircraft.getVector().getY() * (f / Game.TARGET_FPS);
		p = new Point(newX, newY);
	}*/

	public Point getMainWheel() {
		return mainWheel;
	}

	public Point getNoseWheel() {
		return noseWheel;
	}

	public Point getExhaust() {
		return exhaust;
	}

	public Point getSkid() {
		return skid;
	}

	public Point getTop() {
		return top;
	}

	public Point getBottom() {
		return bottom;
	}

	public double getElevator() {
		return elevator;
	}

	public Point getCenter() {
		return center;
	}

	public Point getNose() {
		return nose;
	}

	public Point getNoseGear() {
		return noseGear;
	}

	public Point getMainGear() {
		return mainGear;
	}

	public Point getTail() {
		return tail;
	}

	public Point getFin() {
		return fin;
	}

	public Point getStab() {
		return stab;
	}


	public double calculateSkidNormal() {
		double f = 0, v = 0;

		//Debug.print("aglskid " + aglSkid);
		//Debug.print("skidTolerance " + skidTolerance);
		
		if(aglSkid < skidTolerance && angularSpeed > 0)
			v = angularSpeed * tailWeight * Math.pow(skid.getX() - center.getX(), 2);//angular momentum

		//return -tailForce + cSkid * -v;
		return cSkid * -v - cSkidDamp * angularSpeed;
	}

	private void angularAcceleration(double step) {
		double a, i, t; //angular acceleration, rotational inertia, torque
	//	i = noseWeight * Math.abs(nose.getX() - center.getX());
		//i += tailWeight * Math.abs(fin.getX() - center.getX());
		i = mass;
		elevatorForce = calculateElevatorForce();
		noseForce = calculateNoseForce();
		noseNormalForce = calculateNoseNormal();
		resistForce = calculateAngularResistance();
		//if(type == Type.SHUTTLE)
		//System.out.println("resistForce " + resistForce);
		skidForce = calculateSkidNormal();
		tailForce = calculateTailForce();
		t = elevatorForce + noseForce + noseNormalForce + resistForce
			+ skidForce + tailForce;
		a = t / i;
		angularSpeed += a * step;
		angleToHorizon += angularSpeed * step;
	}

//	public Vector getElevatorAirflow() {
	//	Vector v;

	//public double getAngularSpeed(double step) {
	//	return (angleToHorizon - previousAngleToHorizon) / step;
	//}


	public double calculateElevatorForce() {
		double flow = Math.pow(getAirflow().getMagnitude(), 2);
		return cElev * flow * Math.toRadians(elevator)
			* Math.pow(fin.getX() - center.getX(), 2);
	}

	private double calculateAngularResistance() {
		int sign = -1;
		double aoa = getAngleOfAttack();
		double correction = Math.PI;
		//double flow = Math.pow(getAirflow().getMagnitude(), 2);
		//double flow = Math.log(getAirflow().getMagnitude());
		double flow = Math.log(getWindResistance().getMagnitude());
		double r = Math.pow(fin.getX() - center.getX(), 2);
		//Vector chordline = getChordline();
		//aoi is not factored into rotation resistance calculation
		//as it's based on airframe and tail
		Vector chordline = getChordline();
		Vector windResist = getWindResistance().flip();

		//correct angles if they might fall on opposite sides of the circle's wrap point
		//if(chordline.getDirection() >= Math.PI * 1.5)//upper bound enforced by vector class
		//	correction = -correction;
		//else if (chordline.getDirection() >= correction)
		//	correction = 0;

		chordline.setDirection(chordline.getDirection() + correction);
		windResist.setDirection(windResist.getDirection() + correction);

		if(chordline.getDirection() < windResist.getDirection())
			sign = 1;
		
		if(type == Type.SHUTTLE) {
//System.out.println("chordline.getDirection() " + chordline.getDirection());
//System.out.println("windResist.getDirection() " + windResist.getDirection());
		//System.out.println("aoa " + aoa);
		}
		
		aoa = Math.pow(Math.toDegrees(aoa), 2);
		double f = sign * cAngularResist * aoa * flow * r;
		//double f = cAngularResist * aoa * flow * r;
		double d = Math.signum(angularSpeed) * cRotateDrag * surfaceArea * Math.pow(angularSpeed, 2);
		//return f - d;
		return f;
	}


	/**
	* Get normal force imparted by front gear suspension.
	*/
	public double calculateNoseNormal() {
		double f = 0, p = 0;
		
		if(noseDown) {
			p = noseForce + angularSpeed * noseWeight
				* Math.pow(noseWheel.getX()- center.getX(), 2);
		    f = -cNoseSpring * noseSpring * p;
			
			if(noseSpring > noseSpringTravel - SPRING_BOTTOM_RANGE)
				f *= C_NOSE_SPRING_BOTTOM;//scale up normal force if spring is bottomed
		
			//f -= cNoseDamp * angularSpeed;
		}

		return f;
	}

	public double calculateNoseForce() {
		return -G * noseWeight * Math.pow(nose.getX() - center.getX(), 2);
	}

	public double calculateTailForce() {
		return G * tailWeight * Math.pow(tail.getX() - center.getX(), 2);
	}

	/**
	* Set the physics properties
	* of the aircraft (mass, surface area,
	* wing area, maximum throttleSetting).
	*/
/*	public void setProperties(int m, int sA, int wA, int mT, Type t) {
		cArea = minCArea;

		if(m > 0) {
			mass = m;
		}
		if(sA > 0) {
			surfaceArea = sA;
		}
		if(wA > 0) {
			wingArea = wA;
		}
		if(mT > D_MIN_THRUST) {
			maxThrust = mT;
		}

		minThrust = D_MIN_THRUST;

		if(throttleSetting > maxThrust) {
			throttleSetting = maxThrust;
		}

		type = t;

		if(t == Type.TANKER) {
			//Aircraft.fin = new Point(19.0, 4.0);
		}
	}*/



	public void setNozzleForce(Vector v) {
		nozzleForce = v;
	}



	/**
	*
	*/
	public void setActingWind(Wind w) {
		if(w != null) {
			actingWind = w;
		}
	}

	public Point[] getBoomLinks() {
		return fuelBoom.getLinks();
	}

	public void extendBoom() {
		fuelBoom.addKnot();
	}

	public class Hose {

		private Point attachment;
		private int numNodes;

		private Knot[] knots;
		private Spring[] springs;

		public void addKnot() {
			Knot k = new Knot(knots[0].getMass()
				, new Point(knots[knots.length - 1].getLocation().getX() - springs[springs.length - 1].getLength()
					, knots[knots.length - 1].getLocation().getY() - springs[springs.length - 1].getLength())
				, new Vector(knots[knots.length - 1].getMotion().getDirection()
					, knots[knots.length - 1].getMotion().getMagnitude()));

			Spring s = new Spring(springs[0].getLength(), springs[0].getStiffness());

			Knot[] temp = new Knot[knots.length + 1];
			for(int i = 0; i < knots.length; i++) {
				temp[i] = knots[i];
			}
			knots = new Knot[temp.length];
			for(int i = 0; i < knots.length; i++) {
				knots[i] = temp[i];
			}
			knots[knots.length - 1] = knots[knots.length - 2];//move nozzle
			knots[knots.length - 2] = k;//insert new knot

			//System.out.println("last 2 knots xpos " + knots[knots.length - 2].getLocation().getX() + " " + knots[knots.length - 3].getLocation().getX());


			Spring[] tempSprings = new Spring[springs.length + 1];
			for(int i = 0; i < springs.length; i++) {
				tempSprings[i] = springs[i];
			}
			springs = new Spring[tempSprings.length];
			for(int i = 0; i < springs.length; i++) {
				springs[i] = tempSprings[i];
			}
			springs[springs.length - 1] = s;//insert new knot

		}

		//by default hose is laid out horizontally extending along negative x axis from attachment
		public Hose(Point p, Vector v, int numSections, double mass, double springLength) {
			if(p != null) {
				attachment = p;
			} else {
				Debug.print("Aircraft.java:Hose:Hose(): p cannot be null");
				attachment = new Point(0.0, 0.0);
			}

			numNodes = 0;

			if(numSections > 0) {
				knots = new Knot[numSections + 1];
				springs = new Spring[numSections];
			} else {
				Debug.print("Aircraft.java:Hose:Hose(): numSections must be > 0");
				numSections = 1;
				//this is a bug
			}

			Point current = Collision.getRotatedPoint(attachment, -angleToHorizon);
			current = Point.add(current, location);

			for(int i = 0; i < knots.length; i++) {
				current = new Point(current.getX() + C_KNOT_START * i
					, current.getY() + C_KNOT_START * i);
					
				if(i == knots.length - 1)
					mass = NOZZLE_MASS;
				
				knots[i] = new Knot(mass, current, v);
			}

			for(int i = 0; i < springs.length; i++) {
				springs[i] = new Spring(springLength, cBoomSpring);
			}
		}

		public void setLocation(Point p) {
			if(p != null) {
				attachment = p;
			} else {
				Debug.print("Aircraft.java:Hose.java:setLocation(): p cannot be null");
			}
		}

		public void applyForce(Knot k, Vector f, double step) {
			double mass = 0;
			Vector ac;
			Vector g = Vector.create(0, -G * mass);
			
			if(k != null)
				mass = k.getMass();
			else
				Debug.print("Aircraft.java:Hose.java:applyForce(): k cannot be null");
			
			
			ac = Vector.add(f, g).scale(1.0 / mass);
			//Point ac = Point.add(new Point(f), new Point(g));
			//ac.scale(1.0 / mass);
			//ac.scale(step);
			//double fX = f.getX() + g.getX();
			//double fY = f.getY() + g.getY();
			//double aX = fX / mass;
			//double aY = fY / mass;
			//p = ;
			//Vector v = Vector.add(k.getMotion(), a.scale(step));
			//double vX = k.getMotion().getX() + aX * step;
			//double vY = k.getMotion().getY() + aY * step;
			Vector tempMotion = Vector.add(k.getMotion(), new Vector(ac).scale(step));
			
			//Debug.print("tempMotion " + tempMotion);
			if(k.getPreviousLocation() != null) {
				//vX = k.getLocation().getX() - k.getPreviousLocation().getX();
				//vY = k.getLocation().getY() - k.getPreviousLocation().getY();
				tempMotion = Vector.create(k.getLocation(), k.getPreviousLocation());
			}
			//Debug.print("tempMotion 2 " + tempMotion);
			//tempMotion = Vector.create(vX, vY);
			Vector newLoc = Vector.add(new Vector(k.getLocation()), tempMotion);
			newLoc = Vector.add(newLoc, ac.scale(Math.pow(step, 2)));
			//a = Vector.add(Vector.add(new Vector(k.getLocation()), tempMotion), a.scale(Math.pow(step, 2)));
			//double newX = k.getLocation().getX() + tempMotion.getX() + aX * Math.pow(step, 2);
			//double newY = k.getLocation().getY() + tempMotion.getY() + aY * Math.pow(step, 2);
			
			k.setPreviousLocation(k.getLocation());
			//k.setLocation(new Point(a));
			k.setLocation(new Point(newLoc));
			//k.setLocation(new Point(newX, newY));
		}

		public void update(double step) {
			double d, f, x, y, dRoot;
			Knot k1, k2;
			Spring s;
			Vector springForce;
			Point firstKnot, att;
			Vector force, drag;
			att = Collision.getRotatedPoint(attachment, Math.toRadians(getAngleToHorizon()));
			att.translate(location);
			knots[0].setLocation(new Point(att));
			
			Vector rootResist = new Vector(Math.toRadians(getAngleToHorizon()), 1).flip();
			Vector between;
			
			Vector toRoot = Vector.create(knots[0].getLocation(), knots[1].getLocation());
			//correct direction to be relative to level aircraft
			toRoot.setDirection(toRoot.getDirection() + Math.toRadians(getAngleToHorizon()));
			
			//update acting force on all hose links
			for(int i = 0; i < knots.length - 1; i++) {
				k1 = knots[i];
				k2 = knots[i + 1];
				s = springs[i];
				between = Vector.create(k1.getLocation(), k2.getLocation());
				springForce = Vector.create(k1.getLocation(), k2.getLocation());//refactor
				d = s.getLength() - springForce.getMagnitude();
				springForce.normalize().flip();
				f = s.getStiffness() * d;//magnitude of spring force that resists hose deformation between k1 and k2
				
				if(f < 0) {
					f = Math.abs(f);
					springForce.flip();//force repelling instead of attracting
				}

				//set magnitude of spring force for first link
				springForce.setMagnitude(f);
				//add damp to force or hose will never settle
				springForce = Vector.add(springForce
					, Vector.subtract(k1.getMotion(), k2.getMotion()).scale(cBoomDamp));
					//, Vector.subtract(k2.getMotion(), k1.getMotion()).scale(cBoomDamp));

				if(i == knots.length - 2)//nozzle
					cBoomDrag = cBoomNozzleDrag;
				
				//aerodynamic drag on link given by some constant of its motion 
				drag = new Vector(k2.getMotion()).flip().scale(cBoomDrag);
				//drag = new Vector(0, 0);
				force = Vector.add(springForce, drag);

				if(i == knots.length - 2 && nozzleForce != null && type == Type.TANKER)
					force = Vector.add(force, nozzleForce);
				
				if(i == 0) {
					//if(type == Type.TANKER)
						//System.out.println("\n this");
					double rootResistMagnitude; 
					//Debug.print("d " + d);
					//System.out.println("toRoot.getX() " + toRoot.getX());
					if(toRoot.getX() > 0)
						rootResistMagnitude = C_ROOT_RESIST * (1.0 / toRoot.getMagnitude());						
					else
						rootResistMagnitude = C_ROOT_RESIST * toRoot.getMagnitude();
					
					rootResist.setMagnitude(rootResistMagnitude);
					//System.out.println("rootResist " + rootResist);
					force = Vector.add(force, rootResist);
				}
				
				applyForce(k2, force, step);
				drag = new Vector(k1.getMotion()).flip().scale(cBoomDrag);
				force = Vector.add(springForce.flip(), drag);

				//force = Vector.add(v.flip(), drag);
				applyForce(k1, force, step);
			}
		}

		public Point getNozzle() {
			return knots[knots.length - 1].getLocation();
			//return knots[0].getLocation();
		}

		public Point[] getLinks() {
			Point[] links = new Point[knots.length];

			for(int i = 0; i < knots.length; i++) {
				links[i] = new Point(knots[i].getLocation());
			}

			return links;
		}
	}

	public Point getNozzle() {
		return fuelBoom.getNozzle();
	}

	/**
	* Represents a section within a hose that behaves like a mainSpring.
	*/
	public class Spring {
		public double length;
		public double stiffness;

		public Spring(double l, double s) {
			length = l;
			stiffness = s;
		}

		public double getLength() {
			return length;
		}

		public double getStiffness() {
			return stiffness;
		}
	}

	/**
	* Represents a node that joins two hose sections.
	*/
	public class Knot {
		private double mass;
		private Point location;
		private Point previousLocation;
		private Vector motion;

		public Knot(double m, Point p, Vector v) {
			mass = m;
			setLocation(p);
			setMotion(v);
		}

		public double getMass() {
			return mass;
		}

		public Point getLocation() {
			return location;
		}

		public Point getPreviousLocation() {
			return previousLocation;
		}

		public void setLocation(Point p) {
			if(p != null) {
				location = p;
			} else {
				Debug.print("Aircraft.java:Knot:setLocation(): p cannot be null");
				location = new Point(0.0, 0.0);
			}
		}

		public void setPreviousLocation(Point p) {
			if(p != null) {
				previousLocation = p;
			} else {
				Debug.print("Aircraft.java:Knot:setPreviousLocation(): p cannot be null");
				previousLocation = new Point(0.0, 0.0);
			}
		}

		public Vector getMotion() {
			Vector m = motion;
			if(previousLocation != null)
				m = Vector.create(location.getX() - previousLocation.getX(),
					location.getY() - previousLocation.getY());
			return m;
		}

		public void setMotion(Vector v) {
			if(v != null) {
				motion = v;
			} else {
				Debug.print("Aircraft.java:Knot:setMotion(): v cannot be null");
				motion = new Vector (0.0, 0.0);
			}
		}
	}

	public void setLocation(Point p) {
		if(p != null) {
			location = p;
		} else {
			location = new Point(0.0, 0.0);
			Debug.print("Aircraft.java:Aircraft(): p cannot be null");
			Debug.print("Aircraft.java:Aircraft(): resetting to world origin");
		}
	}

	public boolean gearStowed() {
		return gearStowed;
	}

	public boolean gearLocked() {
		return !gearInTransit;
	}

	public void lockGear() {
		gearInTransit = false;

		if(gearDirection > 0) {
			gearStowed = true;
			gearDirection = 0;
		}
	}

	public int toggleGear() {
		if(gearStowed()) {
			gearDown();
		} else if(gearLocked()) {
			gearUp();
		}
		return gearDirection;
	}

	private int gearDirection;

	public void gearUp() {
		gearDirection = 1;
		gearInTransit = true;
	}

	public void gearDown() {
		gearDirection = -1;
		gearInTransit = true;
		gearStowed = false;
	}

	public void adjustFlaps(int i) {
		if(i < 0 && flaps > 0) {
			flaps -= flapIncrement;
		} else if (i > 0 && flaps < maxFlaps) {
			flaps += flapIncrement;
		}
	}

	public double getSpringPercent() {
		//Debug.print("mainSpring , mainSpring travel " + mainSpring + " " + mainSpringTravel);
	//	Debug.print("mainSpring percent  " + (mainSpring / mainSpringTravel));

		return mainSpring / mainSpringTravel;
	}

	public double getNoseSpringPercent() {
		return noseSpring / noseSpringTravel;
	}

	public double getSpringDeflection() {
		return mainSpringTravel - mainSpring;
	}

	public double getNoseSpringDeflection() {
		return noseSpringTravel - noseSpring;
		//return noseSpring;
	}

	public boolean speedBrakesDeployed() {
		return spdBrakes;
	}
	/**
	* Toggle speed brakes.
	*/
	public void spdBrakes() {
		spdBrakes = spdBrakes ? false : true;
	}

	/**
	* Toggle the brakes.
	*/
	public void brakes() {
		brakes = brakes ? false : true;
	}

	/**
	* Deflect the elevator by i, moderated by wind resistance.
	*/
	public void deflectElevator(double i) {
		elevator += C_ELEV_INPUT * (1.0 / Math.max(getAirflow().getMagnitude(), MIN_ELEV_AIRFLOW))
			* i;
		
		//enforce elevator motion range
		if(elevator > maxElev)
			elevator = maxElev;
		else if(elevator < -maxElev)
			elevator = -maxElev;

		trim();
	}

	public void setElevator(double d) {
		elevator = d;
		//enforce elevator range of motion
		if(elevator > maxElev)
			elevator = maxElev;
		else if(elevator < -maxElev)
			elevator = -maxElev;

		trim();
	}

	/**
	* Adjust the throttle by a linear amount.
	*/
	public void adjustThrottle(double i) {
		throttleSetting += i;

		if(throttleSetting > maxThrottleSetting)
			throttleSetting = maxThrottleSetting;
		else if(throttleSetting < minThrottleSetting)
			throttleSetting = minThrottleSetting;
	}

	/**
	* Returns current throttle setting in percent.
	*/
	public double getThrottleSetting() {
		return throttleSetting * 100;
	}

	/**
	* Update the physics model of the Aircraft by calculating and applying a net force.
	*/
	public void updateModel(double step) {
		currentStep = step;

		if(step < targetStep && previousStep < targetStep
			&& step > 0.0 && previousStep > 0.0)
			initialized = true;

		Vector force;
		lift = calculateLift();
		thrust = calculateThrust();
		drag = calculateDrag();
		gravity = calculateGravity();
		normal = calculateNormal();
		force = Vector.add(lift, thrust);
		force = Vector.add(force, drag);
		force = Vector.add(force, gravity);

		if(mainDown) {
			//fY += calculateNormal().getY();
			force = Vector.add(force, normal);
		}

		if(initialized) {
			applyForce(force, step);
			angularAcceleration(step);

			if(fuelBoom != null)
				fuelBoom.update(step);
		}

		previousStep = step;
		addLocationRecord(location);
		speedRecordTimeElapsed += step;
	}

	public void update(double step) {
		for(int i = 0; i < BOOM_SIMS_PER_FRAME; i++) {
			fuelBoom.update(step / BOOM_SIMS_PER_FRAME);
		}
	}

	private Vector getMotion() {
		Vector m = motion;
		if(previousLocation != null) {
			m = Vector.create(location.getX() - previousLocation.getX(), location.getY() - previousLocation.getY());
			m = new Vector(m.getDirection(), m.getMagnitude() / previousStep);
		}
		//Debug.print("**** m mag " + m.getMagnitude());
		return m;
	}

	/*
	* Service method - accelerate the aircraft,
	* print out the new and previous velocities.
	*/
	private void applyForce(Vector f, double step) {
		Vector previousMotion;
		Vector accel = new Vector(f).scale(1.0 / mass);
		accel.scale(step);

		//Debug.print("accel " + accel.getMagnitude() + " " + accel.getDirection());


		motion = Vector.add(motion, accel);
		previousMotion = new Vector(motion);

		if(previousLocation != null) {
			previousMotion = new Vector(Point.subtract(location, previousLocation));
			motion = previousMotion;
		}

		previousLocation = location;
		accel.scale(step);//original accel should be scaled by step squared, however it was already scaled by step previously in function
		Vector loc = new Vector(location);
		loc = Vector.add(loc, previousMotion);
		loc = Vector.add(loc, accel);
		location = new Point(loc);

		if(outputToggled) {
		//	Debug.print("motion dir " + previousMotion.getDirection() + " " + motion.getDirection());
		//	Debug.print("type is " + type);
		}

		if(motion.getMagnitude() > MAX_SPEED)
			motion.setMagnitude(MAX_SPEED);//global speed limit
	}


	/*
	* Returns a Vector that represents the apparent wind.
	*/
	public Vector getWindResistance() {
		//Vector t = actingWind.getVector();
		Vector r = new Vector(getMotion());
		r.flip();

		//if(type == Type.TANKER) //hack
			//System.out.println("actingWind dir " + Math.toDegrees(actingWind.getVector().getDirection())
			//	+ " actingWind mag " + actingWind.getVector().getMagnitude());
			
		r = Vector.add(r, actingWind.getVector());

		//double x = -getMotion().getX() + -t.getX();
		//double y = -getMotion().getY() + -t.getY();
		return r;
	}


	private static final int turbineMass = 100;
	private static final int maxRPM = 3000;
	private static final int engineTorque = 30000;
	private static final double cTurbineFriction = 0.1;
	private static final double turbineRadius = 1.0;
	private double turbineSpeed;

	public double getTurbineSpeed() {
		return turbineSpeed / (float)maxRPM;
	}

	public Vector getAngularResistance() {
		return Vector.create(0.0, resistForce);
	}

	public Vector getElevatorForce() {
		return Vector.create(0.0, elevatorForce);
	}

	public Vector getNoseForce() {
		return Vector.create(0.0, noseForce);
	}

	public Vector getNoseNormal() {
		return Vector.create(0.0, noseNormalForce);
	}

	public Vector getSkidNormal() {
		return Vector.create(0.0, skidForce);
	}

	public Vector getTailForce() {
		return Vector.create(0.0, -tailForce);
	}




	/*
	* Calculate the throttleSetting that is produced by the engines.
	* returns a force vector.
	*
	*/
	public Vector calculateThrust() {
		double f= 0;
		double accel = 0;

		double i = 0;
		double t = 0;

		i = 2 * turbineRadius * turbineMass;
		t = throttleSetting * engineTorque;
		t -= turbineMass * turbineSpeed * cTurbineFriction;


		accel = t / i;

		turbineSpeed += accel;

		if(turbineSpeed < 0)
			turbineSpeed = 0;
		if(turbineSpeed > maxRPM)
			turbineSpeed = maxRPM;

		//Debug.print("turbine speed " + turbineSpeed);

		f = turbineSpeed / maxRPM * maxThrust * getCDensity((int) aglMain);

		//Debug.print("cDensity " + getCDensity((int) aglMain));

		return new Vector(getChordline().getDirection(), f);
	}

	/*
	* Returns a force Vector representing the aerodynamic drag on the aircraft.
	*/
	public Vector calculateDrag() {
		Vector chordline = getChordline();
		Vector windResist = getWindResistance();

		double aoa;
		double wX = windResist.getX();
		double wY = windResist.getY();
		double cX = chordline.getX();
		double cY = chordline.getY();
		double dp = (cX * wX) + (cY * wY);
		double area;
		double dragConstant = cDrag;

		//aoa = Math.acos( Math.abs(dp / (windResist.getMagnitude() * chordline.getMagnitude()) ) );
		aoa = getAngleOfAttack();

		//System.out.println("dragConstant " + dragConstant);

		double peak_aoa = Math.toRadians(PEAK_DRAG_AOA);

		area = surfaceArea; //* ((aoa / peak_aoa) * (maxCArea - minCArea)) + minCArea;

		//if(aoa <= peak_aoa) {
		dragConstant = baseCDrag + dragConstant * (aoa / peak_aoa);
		//} else {
			//cDrag = cDrag * ((90.0d - aoa) / peak_aoa);
		//}

		dragConstant += flaps * cFlapsDrag;

		if(!gearStowed) {
			dragConstant += cGear;
		}

		if(spdBrakes) {
			dragConstant += cSpdBrkDrag;
		}

		double aeroMag = Math.pow(windResist.getMagnitude(), 2) * area * dragConstant * getCDensity((int) getAGL());
		//Debug.print("aeroMag " + aeroMag);
		Vector dAero = new Vector(windResist.getDirection(), aeroMag);
		//Debug.print("dAero");


		Vector rollingFriction = new Vector(0.0, 0.0);

		if(mainDown) {
			double vX = getMotion().getX();
			double x = cRoll * vX;

			if(brakes)
				x += cBrakes *  Math.signum(vX) * (1.0 / Math.max(Math.log(Math.abs(vX))
					, MIN_BRAKE_MOTION));

			rollingFriction = Vector.create(-x, 1.0);
		}

		//Debug.print("rollingFriction x y " + rollingFriction.getX() + " " + rollingFriction.getY());
		//return Vector.add(dAero, rollingFriction);
		return Vector.create(dAero.getX() + rollingFriction.getX(), dAero.getY() + rollingFriction.getY());
	}

	/*
	*	Get the angle of attack - the incidence angle between motion and wing chord
	*/
	public double getAngleOfAttack() {
		Vector chordline = getChordline();
		Vector windResist = getWindResistance();
		//chordline.setDirection(chordline.getDirection() + Math.toRadians(aoi));
		double dp = chordline.getX() * windResist.getX()
			+ chordline.getY() * windResist.getY();
		double aoa = Math.acos(Math.abs(dp / (windResist.getMagnitude()
			* chordline.getMagnitude())));
		return aoa;
	}

	/*
	* Calculate the lift on the aircraft and return a force.
	*/
	public Vector calculateLift() {
		Vector chordline = getChordline();
		Vector windResist = getWindResistance();
		double aoa = getAngleOfAttack();


		//System.out.println("chrd dir " + chordline.getDirection());
		//System.out.println("mtion dir " + motion.getDirection());



		Vector airflow = getAirflow();

		double peak = Math.toRadians(peakLiftAoa);

		//Debug.print("peak " + peak);

	//if(aoa <= peak && c > 1.5 * Math.PI) {

		//lift coeff scales linearly with aoa up to peak lift, then reverses at peak lift * 2
		if(aoa <= peak)
			cLift = maxCLift * (aoa / peak);
		else if(aoa <= 2 * peak)
			cLift = maxCLift * ((2 * peak - aoa) / peak);
		else
			cLift = 0;

		Vector tempMotion = new Vector(motion);


		//Debug.print("aoa " + aoa);
		//Debug.print("cLift " + cLift);
		//tempMotion.flipX();//motion uses clockwise, chord counter-clockwise
		double correction = Math.PI / 2;
		double chordDir = chordline.getDirection();
	//	tempMotion.setDirection(Math.PI - tempMotion.getDirection());
		double motionDir = tempMotion.getDirection();
		//double motionDir = tempMotion.getDirection();//motion uses clockwise, chord counter-clockwise



		//correct angles if they might fall on opposite sides of the circle's wrap point
		if(motionDir >= Math.PI * 1.5) {//upper bound enforced by vector class
			correction = -correction;
		} else if (motionDir >= correction) {
			correction = 0;
		}

	/*	if(outputToggled) {

			Debug.print("aoa " + Math.toDegrees(aoa));
			Debug.print("type is " + type);
			Debug.print("correction " + correction);
			Debug.print("chordDir " + Math.toDegrees(chordline.getDirection()));
			Debug.print("motionDir " + Math.toDegrees(tempMotion.getDirection()));
		}*/
//correction = 0;
		chordline.setDirection(chordDir + correction);
		tempMotion.setDirection(motionDir + correction);

		chordDir = chordline.getDirection();
		motionDir = tempMotion.getDirection();//motion uses clockwise, chord counter-clockwise

	/*	if(outputToggled) {
			Debug.print("corr chordDir " + Math.toDegrees(chordline.getDirection()));
			Debug.print("corr motionDir " + Math.toDegrees(tempMotion.getDirection()));
		}*/


		//no lift if negative angle of attack
		if(chordline.getDirection() < tempMotion.getDirection())
			cLift = 0;


		/*} else {
			cLift = Math.max(0, maxCLift - (aoa - pL) / pL * maxCLift);
		}*/

		if(spdBrakes) {
			cLift -= cSpdBrkLift;
		}

		cLift += flaps * cFlapsLift;

		cLift = Math.max(0, cLift);
		//Debug.print("direction " + (airflow.getDirection() - Math.PI / 2));
		Vector lift = new Vector(airflow.getDirection() - Math.PI / 2
			, Math.pow(airflow.getMagnitude(), 2) * getCDensity((int) getAGL()) * cLift * aoa * wingArea);

		return lift;
	}

	public Vector getAirflow() {
		Vector windResist = getWindResistance();
		Vector chordline = new Vector(getChordline()).normalize();
		chordline.setDirection(chordline.getDirection() + Math.toRadians(aoi));
		double dp = windResist.getX() * chordline.getX()
			+ windResist.getY() * chordline.getY();
		//if(type == Type.SHUTTLE)
		//Debug.print("dp " + dp);
		windResist.setMagnitude(Math.abs(dp));
		//windResist.setDirection(chordline.getDirection());
		/*
		double lX = windResist.getX();
		double lY = windResist.getY();

		Vector chordline = getChordline();

		double a = chordline.getDirection() - Math.toRadians(aoi);

		if(a < 0) {
			a = 2 * Math.PI - Math.abs(a);
		}

		chordline = new Vector(a, chordline.getMagnitude());

		double cX = chordline.getX();
		double cY = chordline.getY();
		double tX = (lX * cX / Math.pow(chordline.getMagnitude(), 2)) * cX;
		double tY = (lY * cY / Math.pow(chordline.getMagnitude(), 2)) * cY;

		return Vector.create(tX, tY);*/
		return windResist;
	}

	public Vector getChordline() {
		Vector chordline;
		double ath = angleToHorizon;
		//System.out.println("angle to horizon " + ath);

		if(ath < 0) {
			ath = Math.PI * 2 - Math.toRadians(Math.abs(ath));
		} else {
			ath = Math.toRadians(ath);
		}

		return new Vector(ath, 1.0);
	}

	public void trim() {
		lastSpd = getMotion().getMagnitude();
	}

	/**
	* Calculate the force of gravity on the aircraft.
	* @return a vector representing the force of gravity
	*/
	public Vector calculateGravity() {
		return Vector.create(0.0, -G * (mass + fuelTank));
	}

	public double getFuel() {
		return fuelTank;
	}

	public double getMaxFuel() {
		return tankSize;
	}

	public void transferFuel(double amount) {
		fuelTank += amount * currentStep;
		if(fuelTank < 0)
			fuelTank = 0;
		else if(fuelTank > tankSize)
			fuelTank = tankSize;
	}

	public boolean fuelTankFull() {
		return (int) fuelTank == (int) tankSize;
	}

	public boolean fuelTankEmpty() {
		return (int) fuelTank == 0;
	}

	/*
	*/
	private double dampen(double s) {
		return 1.0f / Math.pow(s, 2);
	}

	/**
	* Calculate the normal force on the aircraft.
	*/
	public Vector calculateNormal() {
		double f = 0;
		double p = motion.getY() * mass;
		Vector v = new Vector(0, 0);
		f = -gravity.getY() + p;
		
		if(mainDown) {
			v = new Vector(gravity).flip();
			f = cMainSpring * mainSpring * f - cMainDamp * getMotion().getY();
			
			if(mainSpring > mainSpringTravel - SPRING_BOTTOM_RANGE)
				f *= C_MAIN_SPRING_BOTTOM;//scale up normal force if spring is bottomed
			
			v.setMagnitude(Math.max(0, f));
		}
		
		return v;
	}



	public double getAGL() {
		//Debug.print("AGL is " + aglMain);
		return aglMain;
	}

	/**
	* Set above ground level of aircraft center.
	*/
	public void setAGL(double a) {
		double theta = Math.toRadians(angleToHorizon);
		//find precise agl of contact points in world space
		aglMain = a + (mainWheel.getX() - center.getX()) * Math.sin(theta)
			- mainGearHeight * Math.cos(theta) 
			+ (mainGear.getY() - center.getY()) * Math.cos(theta);
		aglNose = a + (noseWheel.getX() - center.getX()) * Math.sin(theta)
			- noseGearHeight * Math.cos(theta)
			+ (noseGear.getY() - center.getY()) * Math.cos(theta);
		aglSkid = a + (skid.getX() - center.getX()) * Math.sin(theta)
			+ (skid.getY() - center.getY()) * Math.cos(theta);
		
		if(aglMain < mainSpringTravel) {
			mainDown = true;
			mainSpring = Math.max(mainSpringTravel - aglMain, mainSpringMin);
		} else {
			mainDown = false;
			mainSpring = 0;
		}

		if(aglNose < noseSpringTravel) {
			noseDown = true;
			noseSpring = Math.max(noseSpringTravel - aglNose, noseSpringMin);
		} else {
			noseDown = false;
			noseSpring = 0;
		}	
	}

	/**
	* Get the aircraft's vector in the environment.
	*/
	public Vector getVector() {
		return new Vector(getMotion());
	}

	/**
	*
	*/
	public void setAircraftVector(Vector v) {
		if(v != null) {
			motion = v;
		} else {
			Debug.print("Aircraft.java: vector cannot be null");
		}
	}

	public boolean noseIsDown() {
		return noseDown;
	}

	public void noseUp() {
		noseDown = false;
	}

	public void noseDown() {
		noseDown = true;
		//System.out.println("Setting nose down");
	}


	public void land() {
		mainDown = true;
	}

	public void liftoff() {
		mainDown = false;
	}


	/**
	* Returns when airplane is on ground.
	*/
	public boolean isLanded() {
		return mainDown;
	}

	/**
	* Returns the strength limit of the landing gear expressed
	* as a descent rate in meters per second.
	*/
	public double getStrutLimit() {
		return strutLimit;
	}

	/**
	* Return angle of aircraft's chord line
	* (angle of nose relative horizon).
	*/
	public double getAngleToHorizon() {
		return angleToHorizon;
	}


	public int flapSetting() {
		return flaps;
	}

	/**
	*
	*/
	public Vector getActingWind() {
		return new Vector(actingWind.getVector());
	}
}
//End of file
