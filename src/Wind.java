package src;//checked!

/**
* A special type of vector that represents the motion of an air parcel.
* This abstraction allows for more granular control over the environment's winds.
*/
class Wind {
	private Vector vector;

	/**
	* Create a new wind with direction "d" and speed "s".
	*/
	Wind(double d, double s) {
		vector = new Vector(d, s);
	}

	/**
	* Create a new wind from an existing one.
	*/
	Wind(Wind w) {
		if(w == null)
			throw new IllegalArgumentException("Wind.java:Wind(): w cannot be null");
		else
			setVector(new Vector(w.getVector()));
	}

	/**
	* Create a new wind from an existing Vector "v".
	*/
	Wind(Vector v) {
		if(v == null)
			throw new IllegalArgumentException("Wind.java:Wind(Vector v): v cannot be null");
		else
			setVector(v);
	}

	/**
	* Returns the vector representing this wind.
	*/
	public Vector getVector() {
		return vector;
	}

	/**
	* Set the vector of this wind.
	*/
	public void setVector(Vector v) {
		if(v != null)
			vector = v;
		else
			Debug.print("Wind.java:setVector(...): v cannot be null");
	}
}
//EOF
