package src;
import java.util.Random;

/**
* The controller of an aircraft. Uses basic decision making to keep the aircraft level
* in changing winds. This acts like a flight computer that can be controlled manually
* or with the autopilot.
*/
class Pilot {
	private static final int D_X = 0;
	private static final int D_Y = 0;

	/*AI parameters*/
	private static final double AOA_LIMIT_NORMAL = 21.0d;
	private static final double AOA_LIMIT_PANIC = 35.0d;
	private static final int AP_STALL_SPEED = 180;
	private static final int AP_MIN_SPEED = 200;
	private static final int AP_MAX_SPEED = 250;
	private static final int AP_VEL_LIMIT = 10;
	private static final int AP_PANIC_LIMIT = 50;
	private static final int THROTTLE_INERTIA = 4;
	private static final int AP_THROTTLE_FORCE = 8;
	private static final int PANIC_CONTROL_SCALE = 2;
	private static final int STALL_RECOVERY_SCALE = 7;
	private static final int STALL_RECOVERY_SPEED_MARGIN = 10;

	private static final double MARGIN = 0.0;

	/*pilot flies this aircraft*/
	private Aircraft flying;
	private int throttleForce;
	private boolean inStall;
	private boolean autopilot;

	private GPWS gpws;

	public class GPWS {
		public boolean groundWarning;
		public boolean windshear;
		public boolean traffic;

		public static final float GROUND_WARNING_TIME = 3.0f;
		public static final float GROUND_WARNING_VSPD = 15.0f;

		public GPWS() {
			groundWarning = false;
		}

		public boolean testForGroundCollision(float verticalSpd
			, float distFromObstacle, boolean gearLocked) {
			verticalSpd = Math.abs(verticalSpd);

			if(verticalSpd < 0	&& distFromObstacle / verticalSpd
				< GROUND_WARNING_TIME) {

				if(!gearLocked || verticalSpd > GROUND_WARNING_VSPD)
					groundWarning = true;

			} else {
				groundWarning = false;
			}

			return groundWarning;
		}

		public boolean groundWarningActive() {
			return false;
		}
	}

	public GPWS getGPWS() {
		return gpws;
	}

	/**
	* Create a new pilot flying aircraft "a". Starting position will be offset "oX" and "oY"
	* from the default player start position.
	*/
	Pilot(Aircraft a) {
		if(a != null) {
			flying = a;
			throttleForce = 0;
		} else {
			Debug.print("Player.java: pilot cannot fly a null aircraft!");
			throw new IllegalArgumentException("Player.java: invalid initializer, see log");
		}

		gpws = new GPWS();
		inStall = false;
		autopilot = false;
	}

	public void setAGL(double a) {
		flying.setAGL(a);
	}

	/**
	* Toggle the spd brakes.
	*/
	public void spdBrakes() {
		flying.spdBrakes();
	}
	/**
	* Toggle the brakes.
	*/
	public void brakes() {
		flying.brakes();
	}

	/**
	* Toggle the landing gear.
	*/
	public void gear() {
		flying.toggleGear();
	}


	private static final double MAX_ATH = 20.0;


	public boolean autopilotEngaged() {
		return autopilot;
	}

	public void toggleAutopilot() {
		autopilot = autopilot ? false : true;
		setAutopilotMode(FlightMode.HOLD_ALT);
	}

	public enum FlightMode {
		HOLD_ALT,
		SEEK_TARGET
	}

	private FlightMode autopilotMode;
	private double holdAltitude;
	private double holdSpeed;

	public void apHoldAltitude(double a, double s) {
		autopilotMode = FlightMode.HOLD_ALT;
		holdAltitude = a;
		holdSpeed = s;
	}

	public void setAutopilotMode(FlightMode fm) {
		autopilotMode = fm;
		if(autopilotMode == FlightMode.HOLD_ALT)
			apHoldAltitude(flying.getAGL(), flying.getVector().getMagnitude());
	}



	public static final int AUTOPILOT_DELAY = 1000;

	private static final int AP_ALT_TOLERANCE = 1;
	private static final int AP_SPEED_TOLERANCE = 2;
	private static final double I_SPD_SCALE = 0.1;
	private static final double I_ELEV_SCALE = 8.0;

	private double C_AP_ROTATE = 1.0;
	private double C_AP_ELEV = 0.00001;
	private double C_CLIMB = 0.001;
	private double C_CLIMB_ACCEL = 1.0;
	private double AP_MAX_PITCH = 21.0;
	private double AP_MAX_ELEV = 4.0;

	//private static final double AP_CORRECTION_SCALE = 0.2;

	public void loadAircraft	()  {

	}

	private Point previousLocation;
	private Point currentLocation;
	private Vector previousVector;
	private double previousRotation;
	private double accel, targetAccel;
	private double climb, targetClimb;
	private double rotation, targetRotation;
	private double spd;

	private void resetElevator() {
		flying.setElevator(0.0);
	}


	public double[] getAutopilotStatus() {
		final int numStats = 7;
		int count = 0;
		double[] status = new double[numStats];

		status[count++] = climb;
		status[count++] = targetClimb;
		status[count++] = accel;
		status[count++] = targetAccel;
		status[count++] = rotation;
		status[count++] = targetRotation;
		status[count++] = flying.getType().ordinal();

		return status;
	}

	/**
	* Command the auto-pilot to
	* fly the plane.
	*/
	public void doAI() {
		int maxElev = 5;
		double alt = flying.getAGL();
		double dAlt = 0;
		double dAcc = 0;
		double dClimb = 0;
		double dRotate = 0;
		double dSpd = 0;
		double scale = 1.0;

		Vector currentVector = flying.getVector();
		currentLocation = flying.getLocation();

		accel = 0;
		targetAccel = 0;
		climb = 0;
		targetClimb = 0;
		rotation = 0;
		targetRotation = 0;
		spd = currentVector.getMagnitude();

		double[] settings = flying.getAutopilotSettings();

		C_AP_ROTATE = settings[0];
		C_AP_ELEV = settings[1];
		C_CLIMB = settings[2];
		C_CLIMB_ACCEL = settings[3];
		AP_MAX_PITCH = settings[4];
		AP_MAX_ELEV = settings[5];

		//System.out.println("agl " + alt);

		if(autopilotMode == FlightMode.HOLD_ALT) {

		//	System.out.println("flying.getAngularSpeed() " + flying.getAngularSpeed());

			dSpd = holdSpeed - spd;

			rotation = flying.getAngularSpeed();

			if(previousLocation != null) {
				climb = currentLocation.getY() - previousLocation.getY();
			}


			if(previousVector != null) {
				accel = previousVector.getY() - currentVector.getY();
			}


			//Debug.print("accel " + accel);
			//Debug.print("holdAltitude " + holdAltitude);
			//Debug.print("alt " + alt);

			//climb was always 0

			dAlt = holdAltitude - alt;

			//Debug.print("dalt " + dAlt);

			if(Math.abs(dAlt) > AP_ALT_TOLERANCE) {

				targetClimb = C_CLIMB * dAlt;
			//	Debug.print("target climb " + targetClimb);

				//Debug.print("current climb " + climb);

				dClimb = targetClimb - climb;

				targetAccel = C_CLIMB_ACCEL * dClimb;
				dAcc = targetAccel - accel;

				//Debug.print("dacc " + dAcc);

				//targetRotation = Math.signum(dAcc) * C_AP_ROTATE * Math.log(Math.abs(dAcc));
				targetRotation = C_AP_ROTATE * dAcc;

				if(targetRotation > 0) {
					if(flying.getAngleToHorizon() > AP_MAX_PITCH) {
						targetRotation = 0;
					}
				} else {
					if(flying.getAngleToHorizon() < -AP_MAX_PITCH) {
						targetRotation = 0;
					}
				}

				dRotate = targetRotation - rotation;


				//Debug.print("target rotation " + targetRotation);
				//Debug.print("dRotate " + dRotate);
				//Debug.print("rotation " + rotation);

				scale = C_AP_ELEV * dRotate;


			//	Debug.print("scale, setElev to " + scale + ", " + (AP_MAX_ELEV * scale));

				flying.setElevator(AP_MAX_ELEV * scale);

			}

			if(Math.abs(dSpd) > AP_SPEED_TOLERANCE) {
				scale = Math.signum(dSpd) * I_SPD_SCALE;
				flying.adjustThrottle(scale);
			}


		}

		previousRotation = flying.getAngularSpeed();
		previousLocation = new Point(currentLocation);
		previousVector = new Vector(currentVector);
	}

	/**
	* Get Aircraft this pilot is flying.
	*/
	public Aircraft isFlying() {
		return flying;
	}

	/**
	* Apply the controls to nose the airplane up.
	*/
	public void noseUp(double s) {
		//Debug.print("PUSHING NOSE UP");
		flying.deflectElevator(-1.0 * s);
	}

	/**
	* Apply the controls to nose the airplane down.
	*/
	public void noseDown(double s) {
		//Debug.print("PUSHING NOSE DOWN");
		flying.deflectElevator(1.0 * s);
	}

	/**
	* Advance main propulsion throttle.
	*/
	public void advThrottle() {
		flying.adjustThrottle(0.02);
	}

	/**
	* Retract main propulsion throttle.
	*/
	public void retThrottle() {
		flying.adjustThrottle(-0.02);
	}

	public void adjustFlaps(int i) {
		flying.adjustFlaps(i);
	}
}
//End of file
