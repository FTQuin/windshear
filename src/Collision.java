package src;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.util.ArrayList;

/**
* Service class that contains various operations related to collision and physics.
*/
class Collision {
	public static final int NUM_COLLISION_VECTORS = 2;
	private static final int NUM_COLLISION_POINTS = 3;
	private static final int NUM_OBJECTS = 2;
	private static final int FIRST = 0;
	private static final int SECOND = 1;
	private static final double KNOTS_PER_MPS = 1.944;
	private static final double POINT_COLLISION_RADIUS = 0.5;
	private static final double C_BOUNDING_RADIUS = 1.1;//10% margin
	private static ArrayList<Point> nearestPoints;
	private static ArrayList<Vector> nearestVectors;
	private static ArrayList<Point> intersectPoints;
	public static boolean TERRAIN_MODE = false;
	
	static {
		intersectPoints = new ArrayList<Point>();
		nearestPoints = new ArrayList<Point>();
		nearestVectors = new ArrayList<Vector>();
	}

	/**
	* Convert meters per second to nautical miles per second.
	*/
	public static double mpsToKnots(double m) {
		return m * KNOTS_PER_MPS;
	}

	/**
	* Test for a collision between a point p and a plane v that begins
	* at vStart. Assumes p is descending towards vStart.
	*/
	public static boolean plane(Point p, Point vStart, Vector v) {
		//find y coordinate on plane given by v and vStart and check against position of p
		return p.getY() <= v.getY() / v.getX() * p.getX() + vStart.getY();
	}

	private static double findBoundingSphereRadius(ArrayList<Point> mesh) {
		double minX = 0, minY = 0, maxX = 0, maxY = 0;

		for(Point p : mesh) {
			if(p.getX() < minX)
				minX = p.getX();
			else if(p.getX() > maxX)
				maxX = p.getX();
			if(p.getX() < minY)
				minY = p.getY();
			else if(p.getX() > maxY)
				maxY = p.getY();
		}

		Vector maxDistance = Vector.create(maxX - minX, maxY - minY);
		maxDistance.scale(C_BOUNDING_RADIUS);
		return maxDistance.getMagnitude() / 2;
	}

	public static ArrayList<Vector> getNearestVectors() {
		return nearestVectors;
	}

	public static ArrayList<Point> getNearestPoints() {
		return nearestPoints;
	}

	public static ArrayList<Point> getIntersectPoints() {
		return intersectPoints;
	}

	//if any linear pair of objects collide this routine indicates a collision happened
	public static boolean objectCollision(Collidable[] objects) {
		boolean collision = false;
		boolean validMeshes = true;
		int vectorIndex = 0;//index of vector that should be flipped before construction of tree
		boolean[] collisions = new boolean[NUM_OBJECTS];
		double[] minCollisionRadius = new double[NUM_OBJECTS];
		double[] rotations = new double[NUM_OBJECTS];
		ArrayList[] candidatesPoints = new ArrayList[NUM_OBJECTS];
		ArrayList[] candidateVectors = new ArrayList[NUM_OBJECTS];
		ArrayList[] meshes = new ArrayList[NUM_OBJECTS];
		BST[] trees = new BST[NUM_OBJECTS];
		Point[] locations = new Point[NUM_OBJECTS];
		Point[][] points = new Point[NUM_OBJECTS][NUM_COLLISION_POINTS];
		Point[][] tempPoints = new Point[NUM_OBJECTS][NUM_COLLISION_POINTS];
		Vector[] betweenPlayers = new Vector[NUM_OBJECTS - 1];
		Vector[][] tempVectors = new Vector[NUM_OBJECTS][NUM_COLLISION_VECTORS];
		Vector tempVec;
		nearestPoints.clear();
		nearestVectors.clear();
		intersectPoints.clear();

		//initialize collision detection data arrays
		for(int i = 0; i < NUM_OBJECTS; i++) {
			collisions[i] = false;
			candidatesPoints[i] = new ArrayList<Point>();
			candidateVectors[i] = new ArrayList<Vector>();
			locations[i] = objects[i].getLocation();
			meshes[i] = objects[i].getMesh();
			
			if(!(meshes[i].size() > 0))
				validMeshes = false;
			//System.out.println("meshes " + i + " len " + meshes[i].size());
			//motion[i] = objects[i].getVector();
			rotations[i] = Math.toRadians(objects[i].getAngleToHorizon());
		}
		
		if(!validMeshes)
			return false;

		/* following block depends on initialized data for all objects
			 and is executed for pairs of objects only */
		for(int i = 0; i < NUM_OBJECTS - 1; i++) {
			//Debug.print("locations[i + 1] " + locations[i + 1]);
			//Debug.print("locations[i] " + locations[i]);
			betweenPlayers[i] = Vector.create(locations[i + 1].getX()
				- locations[i].getX() - Terrain.COLLISION_OFFSET, locations[i + 1].getY()
				- locations[i].getY());
			//Debug.print("betweenPlayers " + betweenPlayers[i]);
			minCollisionRadius[i] = findBoundingSphereRadius(meshes[i])
				+  findBoundingSphereRadius(meshes[i + 1]);

			if(betweenPlayers[i].getMagnitude() <= minCollisionRadius[i]) {
				collisions[i] = true;
				collisions[i + 1] = true;
			}
		}

		//construct trees for collision mesh
		for(int i = 0; i < NUM_OBJECTS; i++) {
			collisions[i] = true;//temp
			
			if(collisions[i]) { //if bounding sphere collision
				if((i + 1) % 2 == 0)
					betweenPlayers[vectorIndex].flip();
		
				if(i == 0)//hack
					trees[i] = constructBST(meshes[i], betweenPlayers[vectorIndex]
						, locations[i], rotations[i]);
				else
					trees[i] = constructTerrainBST(meshes[i], betweenPlayers[vectorIndex]
						, locations[0], rotations[i]);
						
				//Debug.print("" + trees[i]);
				//populate candidate point & vector arrays plus global nearest point list
				for(int j = 0; j < NUM_COLLISION_POINTS; j++) {
					points[i][j] = trees[i].removeMin();
					//Debug.print("nearest point " + points[i][j]);

					if(points[i][j] != null) {
						nearestPoints.add(points[i][j]);
						Point tempPoint = points[i][j].copy();
						
						if(i == 0)//hack
							tempPoint.translate(locations[i]);
						else
							tempPoint.translate(locations[0].getX(), locations[0].getY());
						
						//Debug.print("new tempP " + tempPoint);
						candidatesPoints[i].add(tempPoint);
						

						if(j > 0 && points[i][j - 1] != null) {
							//Debug.print("j " + points[i][j]);
							//Debug.print("j - 1 " + points[i][j - 1]);
							//if(i > 0)//hack
							double x = points[i][j].getX() - points[i][j - 1].getX();
							double y = points[i][j].getY() - points[i][j - 1].getY();
							//Debug.print("tempVec x y " + x + " " + y);
							
							tempVec = Vector.create(points[i][j].getX()
								- points[i][j - 1].getX(), points[i][j].getY()
								- points[i][j - 1] .getY());
							//else				
								//tempVec = Vector.create(points[i][j].getX()
									//- points[i][j - 1].getX(), points[i][j - 1] .getY()
									//- points[i][j].getY());
							//Debug.print("tempVec " + tempVec);
							candidateVectors[i].add(tempVec);
							nearestVectors.add(tempVec);
						}// else {
							//Debug.print("one vec point was null");
						//}
					}
				}
			}
		}

		/* following tests are for adjacent pairs of objects only.
			 this code block depends on candidate point arrays being fully populated */
		for(int i = 0; i < NUM_OBJECTS - 1; i++) {
			if(collisions[i]) {
				//populate points array with candidate points for each object
				for(int j = 0; j < NUM_COLLISION_POINTS; j++) {
					tempPoints[i][j] = (Point) candidatesPoints[i].get(j);
					tempPoints[i + 1][j] = (Point) candidatesPoints[i + 1].get(j);
				}

				//test for collision between any of the candidate points of two objects
				collisions[i] = testCandidatePoints(tempPoints[i]
					, tempPoints[i + 1]);

				/* test for line intersection with vector pairs - mesh point collisions
				have higher priority than vector intersections, don't perform test
				unless no previous collison detected */
				if(!collisions[i]) {
					//populate temp arrays with vector & point pairs from two objects
					outer:
					for(int j = 0; j < NUM_COLLISION_VECTORS; j++) {
						tempPoints[i][FIRST] = (Point) candidatesPoints[i].get(j);
						tempVectors[i][FIRST] = (Vector) candidateVectors[i].get(j);

						for(int k = 0; k < NUM_COLLISION_VECTORS; k++) {
							tempPoints[i][SECOND] = (Point) candidatesPoints[i + 1].get(k);
							tempVectors[i][SECOND] = (Vector) candidateVectors[i + 1].get(k);
							collisions[i] = testForLineIntersection(tempPoints[i]
								, tempVectors[i]);

							if(collisions[i])
								break outer;//no need to test further collisions, avoid overwrite of results
						}
					}
				}

				if(collisions[i])
					collision = true;
			}
		}

		return collision;
	}

	/**
	* Test for a collision between two vectors.
	* Vector vectors[n] starts at Point points[n].
	*/
	public static boolean testForLineIntersection(Point[] points
		, Vector[] vectors) {
		boolean collision = false;
		double intersectX, intersectY;
		double[] intercepts = new double[NUM_OBJECTS];
		double[] slopes = new double[NUM_OBJECTS];
		double[][] domains = new double[NUM_OBJECTS][NUM_COLLISION_VECTORS];

		//find slope-intercept form of equation for both vectors
		for(int i = 0; i < NUM_COLLISION_VECTORS; i++) {
			slopes[i] = vectors[i].getY() / vectors[i].getX();
			intercepts[i] = points[i].getY() - slopes[i] * points[i].getX();
		}

		//find intersect point for pair of vectors
		for(int i = 1; i < NUM_COLLISION_VECTORS; i++) {
			intersectX = (intercepts[i] - intercepts[i - 1])
				/ (slopes[i - 1] - slopes[i]);
			intersectY = slopes[i - 1] * intersectX + intercepts[i - 1];
			intersectPoints.add(new Point(intersectX, intersectY));//for visual cue

			//check if intersect point within domain for both vectors
			for(int j = 0; j < NUM_COLLISION_VECTORS; j++) {
				domains[j][FIRST] = Math.min(points[j].getX()
					, points[j].getX() + vectors[j].getX());
				domains[j][SECOND] = Math.max(points[j].getX()
					, points[j].getX() + vectors[j].getX());

				if(intersectX >= domains[j][FIRST]
					&& intersectX <= domains[j][SECOND]) {
					if(j == 0)//should be generalized for more than 2 vectors
				 		collision = true;
				} else {
					collision = false;
				}
			}
		}

		return collision;
	}

	/*
	* Test for a collision between each point of A and B.
	*/
	private static boolean testCandidatePoints(Point[] pA, Point[] pB) {
		boolean collision = false;

		outer:
		for(int i = 0; i < pA.length; i++) {
			for(int j = 0; j < pB.length; j++) {
				if(Vector.create(pA[i].getX() - pB[j].getX()
					, pA[i].getY() - pB[j].getY()).getMagnitude()
					< POINT_COLLISION_RADIUS) {
					collision = true;
					break outer;
				}
			}
		}

		return collision;
	}

	/**
	* Construct a weighted BST with a given collision mesh, a vector pointing
	* to intended operand in collision test, and a rotation value.
	* Weight is the distance between that mesh point and the midway point of
	* the vector.
	*/
	public static BST constructBST(ArrayList<Point> mesh, Vector between
		, Point loc, double r) {
		BST bst = new BST();
		Point midway = new Point(between.getX(), between.getY());
		//Debug.print("midway " + midway);
		//midway = Point.add(loc, midway);
		//Debug.print("midway " + midway);
		//int j;
		double weight;
		//Point p;
		Point[] meshPoints = mesh.toArray(new Point[1]);
		
		
		/*for(int i = 0; i < meshPoints.length; i++) {
			j = meshPoints.length - 1 - i;
			p = getRotatedPoint(meshPoints[i], r);
			weight = Vector.create(midway, p).getMagnitude();
			bst.add(p, weight);
			p = getRotatedPoint(meshPoints[j], r);
			weight = Vector.create(midway, p).getMagnitude();
			bst.add(p, weight);
			j--;
		}*/
			
		for(Point p: mesh) {
			//Debug.print("mesh point " + p);
			//Debug.print("midway " + midway);
			p = getRotatedPoint(p, r);
			weight = Vector.create(midway, p).getMagnitude();
			//Debug.print("weight " + weight);
			//Debug.print("mesh point " + p);
			bst.add(p, weight);//point, weight
		}

		return bst;
	}
	
	public static BST constructTerrainBST(ArrayList<Point> mesh, Vector between
		, Point loc, double r) {
		BST bst = new BST();
		Point midway = new Point(between.getX(), between.getY());
		//Debug.print("midway " + midway);
		//midway = Point.add(loc, midway);
		//Debug.print("midway " + midway);
		//int j;
		double weight;
		//Point p;
		Point[] meshPoints = mesh.toArray(new Point[1]);

			
		for(Point p: mesh) {
			//Debug.print("mesh point " + p);
			//Debug.print("midway " + midway);
			
			p = p.copy().translate(-loc.getX(), -loc.getY());
			p = getRotatedPoint(p, r);
			weight = Vector.create(midway, p).getMagnitude();
			//Debug.print("weight " + weight);
			//Debug.print("mesh point " + p);
			bst.add(p, weight);//point, weight
		}

		return bst;
	}
	
	
	/* Find which quadrant contains a given point */
	private static int findQuadrant(Point p) {
		int quadrant = 0;

		if(p.getX() < 0)
			quadrant = (p.getY() < 0) ? 4 : 1;
		else
			quadrant = (p.getY() < 0) ? 3 : 2;

		return quadrant;
	}

	/**
	* Test for bounding sphere collision between two points given sum of
	* their spheres' radiuses.
	*/
	public static boolean sphere(Point p1, Point p2, double r) {
		return Math.sqrt(Math.pow(p2.getX() - p1.getX(), 2)
			+ Math.pow(p2.getY() - p1.getY(), 2)) <= r;
	}

	/**
	* Rotate a given point around the origin by amount r and return a new point.
	* Uses AffineTransform class to perform operation.
	*/
	public static Point getRotatedPoint(Point p, double r) {
		AffineTransform transform = new AffineTransform();
		Point2D rotated = new Point2D.Double();
		transform.rotate(r);
		transform.transform(new Point2D.Double(p.getX(), p.getY()), rotated);
		return new Point(rotated.getX(), rotated.getY());
	}
}
//End of file
