package src;//checked!

class Rectangle {
	double width;
	double height;

	Rectangle(double w, double h) {
		if(w > 0)
			width = w;
		else
			Debug.print("Rectangle.java:Rectangle(...): w must be > 0");

		if(h > 0)
			height = h;
		else
			Debug.print("Rectangle.java:Rectangle(...): h must be > 0");
	}

	double getWidth() {
		return width;
	}

	double getHeight() {
		return height;
	}

	/**
	* Scale the rectangle by constant "s".
	*/
	void scale(double s) {
		width *= s;
		height *= s;
	}
}
//EOF
