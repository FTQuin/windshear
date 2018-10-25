package src;
import java.awt.Color;
import java.awt.Shape;
import java.awt.image.BufferedImage;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import src.Panel.FontMode;
import java.util.ArrayList;

/**
 * Represents a symbol (image, string, or vector) and a world space location at which to draw.
 */
class DrawingTask {
	private static final double DEFAULT_SCALE = 1.0;
	private static final double DEFAULT_STROKE = 2.0;
	private static final Color DEFAULT_COLOR = Color.BLACK;

	/**
	* Enumeration of all possible types.
	*/
	enum Type {
		IMAGE,
		STRING,
		VECTOR,
		POLYGON,
		SHAPE
	}

	enum Space {
		SCREEN,
		WORLD
	}

	public FontMode getFontMode() {
		return fontMode;
	}

	private AffineTransform transform;
  private BufferedImage image;
	private Color color;
	private String text;
	private Space space;
	private Type type;
	private Quad quad;
	private Shape shape;
	private Vector vector;
	private FontMode fontMode;
	private Point location;//fix redundancy with x, y

	private Point clipLoc;
	private Rectangle2D clipBounds;

	private Queue subTasks;


	private boolean fixed;
    private double x, y;
	private double radius;
	private double scale;

	private int stroke;


	public Queue getSubTasks() {
		return subTasks;
	}



	public double getScale() {
		return scale;
	}

	/**
	* Get bounding sphere radius.
	*/
	public double getSphere() {
		//bsr from task plus screen bsr
		return radius + Math.max(Game.SCREEN_WIDTH, Game.SCREEN_HEIGHT);
	}

	public Space getSpace() {
		return space;
	}

	public Shape getShape() {
		return shape;
	}

	/**
	* Returns the stored affine transformation.
	*/
	public AffineTransform getTransform() {
		return transform;
	}

	/**
	* Returns the stored image.
	*/
	public BufferedImage getImage() {
		return image;
	}

	/**
	* Returns the stored string.
	*/
	public String getString() {
		return text;
	}

	/**
	* Returns the type of this task.
	*/
	public Type getType() {
		return type;
	}

	public Color getColor() {
		return color;
	}

	/**
	* Returns the stored vector.
	*/
	public Vector getVector() {
		return vector;
	}

	public void setLocation(Point p) {
		if(p != null) {
			location = p;
		} else {
			Debug.print("DrawingTask.java:setLocation(Point p): \"p\" cannot be null");
		}
	}

	public Point getLocation() {
		return location;
	}

	/**
	* Returns the world space X axis location where this task should be drawn.
	*/
	public double getX() {
		return x;
	}

	/**
	* Returns the world space Y axis location where this task should be drawn.
	*/
	public double getY() {
		return y;
	}

	public Quad getQuad() {
		return quad;
	}

	/*DrawingTask(Quad q, Space p, Color c){
		quad = q;
		space = p;
		color = c;
		type = Type.POLYGON;
		location = q.getOrigin();
    transform = new AffineTransform();
		scale = DEFAULT_SCALE;
		this.x = location.getX();
    this.y = location.getY();
    transform.translate(0, 0);
		//transform.rotate(d, aX, aY);
	}*/
	DrawingTask(Quad q, Point translate, Color c) {
		this(q, translate, Space.WORLD, c);
	}

	DrawingTask(Quad q, Point translate, Space s, Color c) {
		color = c;
		location = q.getOrigin();
		quad = q;
		scale = DEFAULT_SCALE;
		type = Type.POLYGON;
		space = s;
		transform = new AffineTransform();
		//transform.translate(location.getX(), location.getY());
		transform.translate(translate.getX(), translate.getY());
		//transform.setToTranslation(transform.getTranslateX()
		//	, Game.SCREEN_HEIGHT - transform.getTranslateY());
		//transform.rotate(r, mid.getX(), mid.getY());
		//transform.rotate(d, aX, aY);
	}

	/**
	* Create a task from a BufferedImage "b".
	* Image will be drawn at world space "x, y" with rotation "d" around "aX, aY".
	*/
    DrawingTask(BufferedImage b, Space p, double x, double y, double d, double aX, double aY){
		clipLoc = null;
		clipBounds = null;

		subTasks = null;
		space = p;
		type = Type.IMAGE;
        transform = new AffineTransform();
		setImage(b);
		location = new Point(x, y);
		scale = DEFAULT_SCALE;
		this.x = x;
        this.y = y;
		
		if(b.getWidth() > b.getHeight()) {
			radius = b.getWidth();
		} else {
			radius =  b.getHeight();
		}
		
		transform.translate(x, y);
		transform.rotate(d, aX, aY);
    }

	/**
	* Create a task from a BufferedImage "b".
	* Image will be drawn at world space "x, y" with rotation "d" and scale "s".
	*/
    DrawingTask(BufferedImage b, Space p, double x, double y, double d, double s){
		clipLoc = null;
		clipBounds = null;

		space = p;
		type = Type.IMAGE;
        transform = new AffineTransform();
		setImage(b);
		location = new Point(x, y);
		this.x = x;
        this.y = y;
		scale = s;
		if(b.getWidth() > b.getHeight()) {
			radius = b.getWidth();
		} else {
			radius =  b.getHeight();
		}
		subTasks = null;
        transform.translate(x, y);
		transform.rotate(d, image.getWidth() / 2, image.getHeight() / 2);
    }

	/**
	* Create a task from a BufferedImage "b".
	* Image will be drawn at world space "x, y" with rotation "d"
	*/
    DrawingTask(BufferedImage b, Space p, double x, double y, double d){

		clipLoc = null;
		clipBounds = null;

		space = p;
		type = Type.IMAGE;
        transform = new AffineTransform();
		setImage(b);
		location = new Point(x, y);
		scale = DEFAULT_SCALE;
		this.x = x;
        this.y = y;
		if(b.getWidth() > b.getHeight()) {
			radius = b.getWidth();
		} else {
			radius =  b.getHeight();
		}
		subTasks = null;
        transform.translate(x, y);
		transform.rotate(d, image.getWidth() / 2, image.getHeight() / 2);
		//transform.rotate(d);
    }



	  DrawingTask(BufferedImage b, Point loc, Point translate, Point mid, double r) {
			transform = new AffineTransform();
			location = loc;
			//scale = DEFAULT_SCALE;
			clipLoc = null;
			clipBounds = null;
			subTasks = null;
			setImage(b);
			radius = Math.max(b.getWidth(), b.getHeight());

			transform.translate(location.getX(), location.getY());
			transform.translate(translate.getX(), translate.getY());
			transform.setToTranslation(transform.getTranslateX() - mid.getX()
				, Game.SCREEN_HEIGHT - transform.getTranslateY() - mid.getY());
			transform.rotate(r, mid.getX(), mid.getY());
		//	transform.translate(-mid.getX(), -mid.getY());
	  }

	/**
	* Create a task for a vector "v".
	* A line representing this vector will be drawn with label "s".
	*/
	DrawingTask(Vector v, String s, Point loc, Point translate, Color c, double st) {
		space = Space.WORLD;
		type = Type.VECTOR;
		scale = DEFAULT_SCALE;
		location = loc;
		stroke = (int) st;

		if(v != null) {
			image = null;
			vector = v;
		} else {
			Debug.print("DrawingTask.java:DrawingTask(...): v cannot be null");
			throw new IllegalArgumentException("Invalid initializer, see log");
		}

		if(s != null)
			text = s;
		else
			s = "";

		if(c != null)
			color = c;
		else
			color = DEFAULT_COLOR;

		transform = new AffineTransform();
		transform.translate(location.getX(), location.getY());
		transform.translate(translate.getX(), translate.getY());
		transform.setToTranslation(transform.getTranslateX()
			, Game.SCREEN_HEIGHT - transform.getTranslateY());
	}

	DrawingTask(Vector v, String s, Point loc, Point translate
		, Color c) {
		this(v, s, loc, translate, c, DEFAULT_STROKE);
	}

	/**
	* Create a task for a Shape "s".
	*/
	DrawingTask(Shape s, Point translate, Color c) {
		color = c;
		scale = DEFAULT_SCALE;
		location = new Point(0, 0);
		type = Type.SHAPE;

		if(s != null) {
			image = null;
			shape = s;
			text = "";
			vector = null;
		} else {
			Debug.print("DrawingTask.java:DrawingTask(...): s cannot be null");
			throw new IllegalArgumentException("Invalid initializer, see log");
		}

		transform = new AffineTransform();
		transform.translate(location.getX(), location.getY());
		transform.translate(translate.getX(), translate.getY());
		transform.setToTranslation(transform.getTranslateX()
			, Game.SCREEN_HEIGHT - transform.getTranslateY());
	}

	public Point getClipLocation() {
		return clipLoc;
	}

	public Rectangle2D getClipBounds() {
		return clipBounds;
	}

	/**
	* Create a task from a BufferedImage "b".
	* Image will be drawn at world space "x, y" with rotation "d"
	*/
    DrawingTask(BufferedImage b, Queue sub, AffineTransform a, Rectangle2D clip){
		setImage(b);
		space = Space.SCREEN;
		type = Type.IMAGE;
        transform = a;
		scale = DEFAULT_SCALE;
		radius = Math.max(b.getWidth(), b.getHeight());//find screen space bounding radius of task
		clipLoc = null;
		//clipBounds = null;
		clipBounds = clip;
		location = new Point(a.getTranslateX(), a.getTranslateY());
		subTasks = sub;

    }

	DrawingTask(String s, double x, double y) {//why the type param?
		this(s, x, y, Type.STRING, FontMode.HUD);
	}
	
	/**
	* Create a task from a String "s". String will be drawn at screen space "x, y".
	*/
	DrawingTask(String s, double x, double y, Type t) {//why the type param?
		this(s, x, y, t, FontMode.HUD);
	}




	/**
	* Create a task from a String "s". String will be drawn at screen space "x, y".
	*/
	DrawingTask(String s, double x, double y, Type t, FontMode fm) {//why the type param?
		if(t != null) {
			type = t;
		} else {
			type = Type.STRING;
		}
		fontMode = fm;
		if(s != null) {
			text = s;
			image = null;
			vector = null;
		} else {
			Debug.print("DrawingTask.java:DrawingTask(String s,): \"s\" cannot be null");
			throw new IllegalArgumentException("DrawingTask.java:DrawingTask(String s,): "
				+ " \"s\" cannot be null");
		}
		space = Space.SCREEN;
		transform = new AffineTransform();
		transform.translate(x, y);
		location = new Point(x, y);
		scale = DEFAULT_SCALE;
		this.x = x;
		this.y = y;
	}

	/**
	* Create a task for a Shape "s".
	*/
	DrawingTask(Shape s, Color c, Space space, double x, double y) {
		transform = new AffineTransform();
		type = Type.SHAPE;
		scale = DEFAULT_SCALE;
		color = c;
		if(s != null) {
			image = null;
			text = "";
			vector = null;
			shape = s;
		} else {
			Debug.print("DrawingTask.java:DrawingTask(Vector v, String s,): "
				+ " \"v\" or \"s\" cannot be null");
			throw new IllegalArgumentException("DrawingTask.java:DrawingTask(Vector v, String s,): "
				+ " \"v\" or \"s\" cannot be null");
		}
		this.space = space;
		this.x = x;
		this.y = y;
		location = new Point(x, y);
	}

	DrawingTask(Vector v, String s, double x, double y, Space p, Color c, int st) {
		this(v, s, x, y, p, c);
		stroke = st;
	}

	public int getStroke() {
		return stroke;
	}

	/**
	* Create a task for a Vector "v".
	* A line representing this Vector will be drawn at screen space "x, y" with label "s".
	*/
	DrawingTask(Vector v, String s, double x, double y, Space p, Color c) {
		type = Type.VECTOR;
		scale = DEFAULT_SCALE;

		if(c != null) {
			color = c;
		} else {
			Debug.print("DrawingTask.java:DrawingTask(): " + " c cannot be null");
		}

		if(v != null && s != null) {
			space = p;
			image = null;
			text = s;
			vector = v;
		} else {
			Debug.print("DrawingTask.java:DrawingTask(Vector v, String s,): "
				+ " \"v\" or \"s\" cannot be null");
			throw new IllegalArgumentException("DrawingTask.java:DrawingTask(Vector v, String s,): "
				+ " \"v\" or \"s\" cannot be null");
		}
		//space = Space.SCREEN;
		transform = new AffineTransform();
		location = new Point(x, y);
		transform.translate(x, y);
		this.x = x;
		this.y = y;
	}

	/**
	* Set the image stored by this task to "b".
	*/
    public void setImage(BufferedImage b) {
			if(b != null) {
				image = b;
				type = Type.IMAGE;
				text = null;
				vector = null;
			} else {
				Debug.print("DrawingTask.java:setImage(BufferedImage b): b cannot be null");
				throw new IllegalArgumentException("Invalid initializer, see log");
			}
    }
}
//End of file
