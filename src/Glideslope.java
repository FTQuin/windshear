package src;//checked!

public class Glideslope {
	public static final double ANGLE = 3.0;//DEGREES
	public static final double DEPTH = 1.4;//DEGREES
	public static final double RANGE = 3000;//meters
	private double depth;
	private double angle;
	private Point start;
	private Point outerMarker;
	private Point middleMarker;
	private Point innerMarker;
	private Point end;
	private Vector center;
	private Vector upper;
	private Vector lower;

	Glideslope() {
		center = new Vector(Math.PI - Math.toRadians(ANGLE), RANGE);
		upper = new Vector(center.getDirection() - Math.toRadians(DEPTH) / 2, RANGE);
		lower = new Vector(center.getDirection() + Math.toRadians(DEPTH) / 2, RANGE);
		loadDefaults();
	}

	private void loadDefaults() {
		Vector temp = new Vector(center);
		start = new Point(Map.RUNWAY_B_START + 50, Game.TERRAIN_HEIGHT);
		end = new Point(start.getX() + center.getX()
			, start.getY() + center.getY());
		temp.scale(0.75);
		outerMarker = new Point(start.getX() + temp.getX()
			, start.getY() + temp.getY());
		temp = new Vector(center);
		temp.scale(0.50);
		middleMarker = new Point(start.getX() + temp.getX()
			, start.getY() + temp.getY());
		temp = new Vector(center);
		temp.scale(0.25);
		innerMarker = new Point(start.getX() + temp.getX()
			, start.getY() + temp.getY());
	}

	public Point getStart() {
		return start;
	}

	public Point getEnd() {
		return end;
	}

	public Vector getCenter() {
		return center;
	}

	public double getDeviation(Point p) {
		Point e = new Point(end);
		Point s = new Point(e.getX() + center.getX(), e.getY() + center.getY());
		double slope = (e.getY() - s.getY()) / (e.getX() - s.getX());
		double b = -slope * s.getX() + s.getY();
		Point p2 = new Point(p.getX(), slope * p.getX() + b);
		Vector dev = Vector.create(p2.getX() - p.getX(), p2.getY() - p.getY());
		return dev.getY();
	}
}
//EOF
