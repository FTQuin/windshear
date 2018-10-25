package src;//checked!

/**
* A Vector represents a direction expressed as Radians in one circle
* and a magnitude expressed as an arbitrary unit of length.
*/
class Vector {
	private static final double D_DIR = 0.0;
	private static final double D_MAG = 0.0;
	private double direction;
	private double magnitude;
	
	public String toString() {
		return "Vector direction : " + direction
			+ ", magnitude: " + magnitude;
	}
	
	static Vector average(Vector v1, Vector v2) {
		return create((v1.getX() + v2.getX()) / 2
			, (v1.getY() + v2.getY()) / 2);
	}

	static Vector add(Vector v1, Vector v2) {
		return create(v1.getX() + v2.getX(), v1.getY() + v2.getY());
	}

	static Vector subtract(Vector v1, Vector v2) {
		return create(v1.getX() - v2.getX()
			, v1.getY() - v2.getY());
	}

	//?
	static Vector copy(Vector v) {
		Vector copy;

		if(v != null)
			copy = new Vector(v.getDirection(), v.getMagnitude());
		else
			throw new IllegalArgumentException("Vector.java: copy():  v cannot be null");

		return copy;
	}

	static Vector create(Point p1, Point p2) {
		return create(p1.getX() - p2.getX()
			, p1.getY() - p2.getY());
	}

	/**
	* Service method to create a new vector from its X and Y components.
	*/
	static Vector create(double x, double y) {
		int q;
		double d, m;
		double theta = 0;

		if(x >= 0)
			q = (y >= 0) ? 1 : 4;
		else
			q = (y >= 0) ? 2 : 3;

		if((q == 1 || q == 3) && x != 0)
			theta = Math.atan(Math.abs(y / x));
		else if((q == 2 || q == 4) && y != 0)
			theta = Math.atan(Math.abs(x / y));

		/* hack for above case where x == 0 and theta isn't calculated...
	  		- happened when x == 0 and y > 0 
			- also happens for y == 0 and x < 0 */
		if(x == 0 & y > 0)
			d = 0.5 * Math.PI;
		else if(y == 0 & x < 0)
			d = Math.PI;
		else
			d = theta + (q - 1) * (Math.PI / 2);

		m = Math.sqrt(Math.pow(x, 2) + Math.pow(y, 2));
		return new Vector(d, m);
	}

	Vector() {
		setDirection(0.0);
		setMagnitude(0.0);
	}

	Vector(Vector v) {
		direction = v.getDirection();
		magnitude = v.getMagnitude();
	}

	Vector(Point p) {
		this(create(p.getX(), p.getY()));
	}

	/**
	* Create a new Vector from a direction "d" and magnitude "m".
	*/
	Vector(double d, double m) {
		setDirection(d);
		setMagnitude(m);
	}

	/**
	* Returns the component parallel with the X axis.
	*/
	public double getX() {
		int q = 1;
		double theta = direction;
		double x = 0.0d;

		while(theta > Math.PI/2) {
			theta -= Math.PI/2;
			q++;
		}

		if(q == 1 || q == 3)
			x = Math.cos(theta) * magnitude;
		else if(q == 2 || q == 4)
			x = Math.sin(theta) * magnitude;

		if(q == 2 || q == 3)
			x = -x;

		return x;
	}

	/**
	* Returns the component parallel with the X axis.
	*/
	public double getY() {
		int q = 1;
		double theta = direction;
		double y = 0.0d;

		while(theta > (Math.PI/2)) {
			theta -= Math.PI/2;
			q++;
		}

		if(q == 1 || q == 3)
			y = Math.sin(theta) * magnitude;
		else if(q == 2 || q == 4)
			y = Math.cos(theta) * magnitude;

		if(q == 3 || q == 4)
			y = -y;

		return y;
	}
	
	/**
	* Change this vector to a unit vector.
	*/
	public Vector normalize() {
		magnitude = 1.0;
		return this;
	}

	/**
	* Flip this vector about both axes.
	*/
	public Vector flip() {
		Vector n = create(-getX(), -getY());
		direction = n.getDirection();
		magnitude = n.getMagnitude();
		return this;
	}

	public Vector flipX() {
		Vector n = create(-getX(), getY());
		direction = n.getDirection();
		magnitude = n.getMagnitude();
		return this;
	}
	
	/*public Vector flipY() {
		Vector n = create(getX(), -getY());
		direction = n.getDirection();
		magnitude = n.getMagnitude();
		return this;
	}*/
	
	/**
	* Scale the magnitude of this vector by a constant "c".
	*/
	public Vector scale(double c) {
		magnitude *= Math.abs(c);

		if(c < 0)
			flip();

		return this;
	}

	/**
	* Returns the direction of this vector.
	*/
	public double getDirection() {
		return direction;
	}

	/**
	* Returns the magnitude of this vector.
	*/
	public double getMagnitude() {
		return magnitude;
	}

	/**
	* Set the direction of this vector to "d".
	*/
	public void setDirection(double d) {
		if(Math.abs(d) > 2 * Math.PI)
			d = d % (2 * Math.PI);

		if(d < 0) {
			d = 2 * Math.PI - Math.abs(d);

			//if(Debug.verbose)
				//Debug.print("Vector.java:setDirection(): d cannot be negative");
		}

		direction = d;
	}

	/**
	* Set the magnitude of this vector to "m".
	*/
	public void setMagnitude(double m) {
		if(m < 0) {
			m = D_MAG;
			Debug.print("Vector.java:setMagnitude(...): m cannot be negative");
		}

		magnitude = m;
	}
}
//EOF
