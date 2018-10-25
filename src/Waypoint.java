package src;//checked!

public class Waypoint {
	private Point location;
	private String label;

	Waypoint(Point p, String s) {
		location = p;
		label = s;
	}

	public String getLabel() {
		return label;
	}

	public Point getLocation() {
		return location;
	}

	public float getDistance(Point p) {
		return (float) Vector.create(p.getX() - location.getX()
			, p.getY() - location.getY()).getMagnitude();
	}
}
//EOF
