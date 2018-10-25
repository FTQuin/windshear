package src;

/**
* Simple 4-Point data structure for use in drawing.
*/
class Quad {
	public static final int NUM_VERTICES = 4;
	//private Point origin;
	//private Point one;
	//private Point two;
	//private Point three;
	private Point[] vertices;

	Quad(Point p0, Point p1, Point p2, Point p3) {
		vertices = new Point[]{new Point(p0), new Point(p1)
			, new Point(p2), new Point(p3)};
	}

	public void translate(double x, double y) {
		for(int i = 0; i < vertices.length; i++) {
			vertices[i].setLocation(Point.add(vertices[i], new Point(x, y)));
		}
	}
	
	public void setLocation(Point p) {
		setLocation(p.getX(), p.getY());
	}
		
	public void setLocation(double x, double y) {
		//vertices[0] = p;
		Point p = new Point(x, y);
		
		for(int i = 1; i < vertices.length; i++) {
			Point offset = Point.subtract(vertices[i], vertices[0]);//offset from origin point
			vertices[i].setLocation(Point.add(p, offset));
		}
		
		vertices[0] = p;
	}
	
	public Point getOrigin() {
		return vertices[0];
	}

	public void scale(double s) {
		for(int i = 1; i < vertices.length; i++) {
			vertices[i].setLocation(Point.add(vertices[0]
				, Point.subtract(vertices[i], vertices[0]).scale(s)));
		}
	}

	public Point[] getVertices() {
		return vertices;
	}

	public int[] getVerticesX() {
		int[] vX = new int[vertices.length];

		for(int i = 0; i < vX.length; i++) {
				vX[i] = (int) vertices[i].getX();
		}

		return vX;
	}

	public int[] getVerticesY() {
		int[] vY = new int[vertices.length];

		for(int i = 0; i < vY.length; i++) {
				vY[i] = (int) vertices[i].getY();
		}

		return vY;
	}
}
//EOF
