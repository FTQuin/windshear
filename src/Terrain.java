package src;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Random;
import java.util.Iterator;

class Terrain implements Collidable {
	private static final int N_VERTICES = 1000;
	private static final int N_VERTICES_PER_SEGMENT = 30;//if this is even the terrain quads only appear every second segment... why?
	private static final double MAX_SLOPE = 0.4;
	/* reducing D_STEP improves collision detection by adding more points to the mesh -
	   a commensurate decrease in the collision radius of the aircraft's mesh
	   might be necessary to avoid false collisions */
	private static final int D_STEP = 25;
	public static final int SEGMENT_SIZE = N_VERTICES_PER_SEGMENT * D_STEP;
	private static final int COLLISION_RADIUS = 5;
	private static final int MOUNTAIN_WIDTH = 8000;
	/* offsets center of terrain, used in bst construction to find nearest points
	   - setting this will cause nearest points to appear at an offset from player location */
	//private static final int COLLISION_OFFSET = -275;
	public static final int COLLISION_OFFSET = 0;
	private static final int STEEPNESS = 4;
//	private ArrayList<Point> vertices;
	private LinkedHashMap<Integer, ArrayList<Point>> vertices;
	private Point center;
	private Point ground;

	public double getAngleToHorizon() {
		return 0.0;//terrain has fixed rotation
	}
	
	//return list of points near center of mesh
	public ArrayList<Point> getMesh() {
		ArrayList<Point> mesh = new ArrayList<Point>();
		
		int segmentSize = N_VERTICES_PER_SEGMENT * D_STEP - D_STEP;
		int key = (int) Math.floor(center.getX() / segmentSize);
		key *= segmentSize;
		key += 2 * D_STEP;
		//Debug.print("Terrain.java: key " + key);

		if(vertices.containsKey(key))
			mesh = vertices.get(key);
		//else
		//	Debug.print("Terrain.java: key doesnt exist " + key);

		return mesh;
	}

	/**
	* Return center of collision mesh, defined by location of player.
	*/
	public Point getLocation() {
		return center;
	}

	/*public void setGroundPosition(Point p) {
		if(p != null)
			ground.setLocation(ground.getX(), p.getY());
		else
			Debug.print("Terrain.java: setGroundPosition(...): p cannot be null");
	}*/

	public void setCenter(Point p) {
		if(p != null)
			center.setLocation(p.getX() + COLLISION_OFFSET, p.getY());
		else
			Debug.print("Terrain.java: setCenter(...): p cannot be null");
	}

	/**
	* Get a list of points that comprise the terrain mesh.
	* Should call getMesh where possible to reduce size of return list.
	*/
	public ArrayList<Point> getVertices() {
		ArrayList<Point> allVertices = new ArrayList<Point>();

		for(Integer i : vertices.keySet()) {
			allVertices.addAll(vertices.get(i));
		}

		return allVertices;
	}

	/**
	* Create a new 2D terrain mesh. Points are randomly generated to 
	* mimic a mountainous terrain with peaks and valleys.
	*/
	Terrain(Point g) {
		int c = 0, sig = 0;
		int numSegments = N_VERTICES / N_VERTICES_PER_SEGMENT;
		double x = 0, y = 0;
		ArrayList<Point> segmentVertices;
		Random rand = new Random();
		ground = g;
		x = ground.getX();
		y = ground.getY() - D_STEP;
		vertices = new LinkedHashMap<Integer
			, ArrayList<Point>>(N_VERTICES_PER_SEGMENT);
		center = new Point(0, 0);
		
		for(int i = 0; i < numSegments; i++) {
			segmentVertices = new ArrayList<Point>();
			int segmentSize = N_VERTICES_PER_SEGMENT * D_STEP - D_STEP;
			//key is mapped from start of segment
			int key = (int) Math.floor(x / segmentSize);
			key *= segmentSize;
			key += 2 * D_STEP;
			
			for(int j = 1; j < N_VERTICES_PER_SEGMENT; j++) {
				x += D_STEP;
				c += D_STEP;
				sig = (int) Math.signum(MOUNTAIN_WIDTH / 2 - c);//???
				
				if(rand.nextInt(STEEPNESS) == 1)
					sig = -sig;
				
				y += sig * MAX_SLOPE * D_STEP;

				if(c > MOUNTAIN_WIDTH)
					c = 0;
				
				//System.out.println("creating point " + new Point(x, y));
				segmentVertices.add(new Point(x, y));
			}
			
			vertices.put(key, segmentVertices);
		}
	}

	public double getCollisionRadius() {
		return COLLISION_RADIUS;
	}

	/**
	* Get a list of quads that comprise this terrain mesh. Used for drawing.
	*/
	public ArrayList<Quad> getQuads() {
		ArrayList<Quad> quads = new ArrayList<Quad>();
		Iterator it = getVertices().iterator();
		Point p0, p1;
		p0 = null;
		
		while(it.hasNext()) {
			if(p0 == null)
				p0 = (Point) it.next();
			
			if(it.hasNext()) {
				p1 = (Point) it.next();
				quads.add(new Quad(p0, p1
					, new Point(p1.getX(), ground.getY())
					, new Point(p0.getX(), ground.getY())));
				p0 = p1;
			}
		}

		return quads;
	}
}
//EOF
