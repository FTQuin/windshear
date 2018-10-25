package src;

/**
*  Represents a location in a given coordinate space.
*/
class Point {

	public static Point add(Point p1, Point p2) {
		return new Point(p1.getX() + p2.getX(), p1.getY() + p2.getY());
	}

	public static Point subtract(Point p1, Point p2) {
		return new Point(p1.getX() - p2.getX(), p1.getY() - p2.getY());
	}

	double x, y;

	/**
	* Create a new point from an existing point.
	*/
	Point(Point p) {
		if(p != null) {
			x = p.getX();
			y = p.getY();
		} else {
			Debug.print("Point.java:Point(...): p cannot be null");
		}
	}

	Point(Vector v) {
		if(v != null) {
			x = v.getX();
			y = v.getY();
		} else {
			Debug.print("Point.java:Point(...): v cannot be null");
		}
	}

	public String toString() {
		return "Point X: " + x + ", Y: " + y;
	}

	/**
	* Create a new point represented by "x" and "y".
	*/
	Point(double x, double y) {
		this.x = x;
		this.y = y;
	}

	public Point setX(double x) {
		this.x = x;
		return this;
	}

	public Point setY(double y) {
		this.y = y;
		return this;
	}

	/**
	* Create a copy of this point.
	*/
	public Point copy() {
		return new Point(x, y);
	}

	/**
	* Returns the location of this point on the X axis.
	*/
	public double getX() {
		return x;
	}

	/**
	* Returns the location of this point on the Y axis.
	*/
	public double getY() {
		return y;
	}

	/**
	* Set the location of this point to "x" and "y".
	*/
	public void setLocation(double x, double y) {
		this.x = x;
		this.y = y;
	}
	
	public void setLocation(Point p) {
		setLocation(p.getX(), p.getY());
	}
	
	/**
	* Move the point by offsets "x" and "y".
	*/
	public Point translate(double x, double y) {
		this.x += x;
		this.y += y;
		return this;
	}

	public Point translate(Point p) {
		translate(p.getX(), p.getY());
		return this;
	}

	/**
	* Scale the translation of this point by "s".
	*/
	public Point scale(double s) {
		x *= s;
		y *= s;
		return this;
	}
}
//End of file
