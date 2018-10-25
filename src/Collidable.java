package src;
import java.util.ArrayList;

/**
* An object that can be used with the collision routine.
*/
public interface Collidable {
  public double getAngleToHorizon();
  public ArrayList<Point> getMesh();
  public Point getLocation();
  //public Vector getVector();
}
//End of file
