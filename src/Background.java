package src;

import java.awt.Graphics2D;
import java.awt.Graphics;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.*;
import javax.imageio.ImageIO;
import src.DrawingTask.Space;
import src.Map.SegmentType;

/**
* Encapsulation of scrolling background animation.
*/
class Background {
	private static final int GROUND_PATTERN_SIZE = 3;
	private static final int VT_PATTERN_SIZE = 2;
	private static final int BG_TILE_ROWS = 4;
	private static final int HZ_PATTERN_SIZE = 2;
	private static final int MOUNTAIN_PATTERN_SIZE = 6;
	private static final int MAX_TRACE = 20;
	private static final int NORMAL = 0;
	private static final int HZ_FLIP = 1;
	private static final int VT_FLIP = 2;
	private static final int HZ_VT_FLIP = 3;
	private static final int NORMAL_A = 0;
	private static final int NORMAL_B = 1;
	private static final int NORMAL_C = 2;
	private static final int HZ_FLIP_A = 3;
	private static final int HZ_FLIP_B = 4;
	private static final int HZ_FLIP_C = 5;
	private static final int CLOUD_TYPES = Game.N_CLOUD_IMAGES;
	//will break the resetCloud function if set too high .. execution time increases exponentially as spawn area reaches saturation
	//2.0 is safe with other parameters as-is
	private static final double CLOUD_MULTIPLIER = 1.3;

	private static final int LIMIT = 2;
	private static final int CLOUD_LAYER_LIMIT = 1;
	private static final double CEILING = 800;
	private static final double C_SPAWN_AREA = 1.0;
	private static final double HZ_S_SPAWN_AREA = 6.0;
	private static final double VT_S_SPAWN_AREA = 4.0;
	private static final double FG_SCALE = 1.0;
	private static final double BG_SCALE = 3.0;
	private static final Point OFFSCREEN;
	private static int N_CLOUDS = 1;
	private static int CLOUD_RESET_ATTEMPT_LIMIT = 10;
	
	static {
		OFFSCREEN = new Point(-3000, -3000);
	}

	/*variables*/
	private boolean cloudSpawning;
	private int frameRate;
	private Vector bg;
	private Vector fg;
	private Rectangle scene;
	private Rectangle viewport;
	private double minGround;
	private Tile[] backgroundTiles;
	private Tile[] groundTiles;
	private Tile[] mountainTiles;
	private Vector direction;
	private Animation smokeAnimation;
	private ArrayList<Animation> smoke;
	private BufferedImage[] mountainImages;
	private BufferedImage[] skyImages;
	private BufferedImage[][] terrainImages;
	private BufferedImage[] groundTextures;
	private BufferedImage backgroundTexture;
	private ArrayList<BufferedImage> cloudTextures;
	private BufferedImage tracePoint;
	private ArrayList<Point> trace;
	//private Terrain terrain;
	private Point center;
	private double altitude;
	private double ceiling;
	//private double groundTexture;
	private int rows;
	private int cols;
	private int bgRows;
	private int mtnBgRows;
	private int bgCols;
	private int mtnBgCols;
	private int vtTileOffset;
	private int hzTileOffset;
	private Rectangle tileSize;
	private Rectangle mountainTileSize;
	private Rectangle cloudSize;
	private Point backgroundLayerCenter;
	private Point cloudLayerCenter;
	private Point groundLayerCenter;
	private Point mountainLayerCenter;
	private Map map;
	private Point[] clouds;
	private CloudType[] types;
	private double cloudScale;
	private double groundScale;
	private double backgroundScale;
	private double mountainScale;
	private Point[] groundTilePositions;
	private Point[] bgTilePositions;
	private Rectangle spawnArea;
	private Game parent;
	private Point cloudSpawnMin;

	private class Cloud {

		Point location;

		Cloud(Point p) {
			if(p != null) {
				location = p;
			} else {
				Debug.print("Background.java:Cloud:Cloud(): p cannot be null");
			}
		}

		public Point getLocation() {
			return location;
		}

		public void setLocation(double x, double y) {
			location.setLocation(x, y);
		}

	}

	private class Tile {
		int width;
		int height;

		BufferedImage image;

		public Tile(BufferedImage bi) {
			if(bi != null) {
				image = bi;
				width = image.getWidth();
				height = image.getHeight();
			}
			else {
				Debug.print("Background.java:Tile:Tile(): bi cannot be null");
				throw new IllegalArgumentException("Background.java");
			}
		}

		public void setImage(BufferedImage bi) {
			image = bi;
		}


		public int getWidth() {
			return width;
		}

		public int getHeight() {
			return height;
		}

		public BufferedImage getImage() {
			return image;
		}
	}

	//intended to be types like stratus, cumulus, cirrus, etc.
	//used simply as named constant for random cloud type selection
	enum CloudType {
		ONE,
		TWO,
		THREE,
		FOUR,
		ONE_FLIPPED,
		TWO_FLIPPED,
		THREE_FLIPPED,
		FOUR_FLIPPED
	}

	/**
	* Create a new Background
	* that scrolls in the direction and speed specified
	* by "v".
	* ASSUMES A SQUARE tSize
	*/
	Background(Game g, Vector v, Point p, Rectangle view
		, BufferedImage[] mountains, BufferedImage sky
		, BufferedImage[] groundTexture, Map m, Rectangle cloudSize
		, double backgroundScale, double groundScale, double cloudScale
		, double mountainScale, double terrainHeight, int fps) {
		this.cloudScale = cloudScale;
		this.groundScale = groundScale;
		this.mountainScale = mountainScale;
		this.backgroundScale = backgroundScale;
		this.backgroundTexture = backgroundTexture;
		this.groundTextures = groundTexture;
		parent = g;
		smoke = new ArrayList<Animation>();
		cloudTextures = new ArrayList<BufferedImage>();//...
		tracePoint = null;//...
		//groundTexture = g;
		//terrain = new Terrain(p, groundTexture);
		altitude = 0;
		ceiling = CEILING;
		map = m;
		viewport = view;
		trace = new ArrayList<Point>();

		frameRate = fps;
		direction = new Vector(1.0, 0.0);
		cloudSpawning = true;
		minGround = viewport.getHeight() - 50;//TEMP
		mountainTileSize = new Rectangle((int) mountains[0].getWidth(), (int) mountains[0].getHeight());
		//default size - updates on call to addCloudImg
		this.cloudSize = cloudSize;
		//int side = (int) Math.max(viewport.getWidth(), viewport.getHeight());
		spawnArea = new Rectangle(viewport.getWidth() * HZ_S_SPAWN_AREA
			, viewport.getHeight() * VT_S_SPAWN_AREA);
		N_CLOUDS = (int) (CLOUD_MULTIPLIER * (spawnArea.getWidth() / cloudSize.getWidth()));
		//System.out.println("N_CLOUDS " + N_CLOUDS);
		clouds = new Point[N_CLOUDS];
		types = new CloudType[N_CLOUDS];
		cloudSpawnMin = new Point(0, 0);
		double side = Math.max(cloudSize.getWidth(), cloudSize.getHeight());

		backgroundLayerCenter = new Point(p);
		cloudLayerCenter = new Point(p);
		groundLayerCenter = new Point(p.getX(), terrainHeight * groundScale);//this is OK
		mountainLayerCenter = new Point(p.getX(), terrainHeight * mountainScale
			+ Game.MOUNTAIN_SCREEN_OFFSET);//offset hack
		setCenter(p);
		setVector(v);
		vtTileOffset = 0;
		hzTileOffset = 0;
		AffineTransform tx;
		AffineTransformOp op;
		BufferedImage flipped;
		mountainImages = new BufferedImage[6];
		mountainImages[NORMAL_A] = mountains[0];
		mountainImages[NORMAL_B] = mountains[1];
		mountainImages[NORMAL_C] = mountains[2];

		tx = AffineTransform.getScaleInstance(-1, 1);
		tx.translate(-mountains[0].getWidth(null), 0);
		op = new AffineTransformOp(tx, AffineTransformOp.TYPE_NEAREST_NEIGHBOR);
		flipped = op.filter(mountains[0], null);
		mountainImages[HZ_FLIP_A] = flipped;

		tx = AffineTransform.getScaleInstance(-1, 1);
		tx.translate(-mountains[1].getWidth(null), 0);
		op = new AffineTransformOp(tx, AffineTransformOp.TYPE_NEAREST_NEIGHBOR);
		flipped = op.filter(mountains[1], null);
		mountainImages[HZ_FLIP_B] = flipped;

		tx = AffineTransform.getScaleInstance(-1, 1);
		tx.translate(-mountains[2].getWidth(null), 0);
		op = new AffineTransformOp(tx, AffineTransformOp.TYPE_NEAREST_NEIGHBOR);
		flipped = op.filter(mountains[1], null);
		mountainImages[HZ_FLIP_C] = flipped;

		//mountainImages[HZ_FLIP_A] = mountains[1];
		//mountainImages[HZ_FLIP_B] = mountains[1];

		//Initialize sky background textures
		skyImages = new BufferedImage[4];
		skyImages[NORMAL] = sky;

		tx = AffineTransform.getScaleInstance(1, -1);
		tx.translate(0, -sky.getHeight(null));
		op = new AffineTransformOp(tx, AffineTransformOp.TYPE_NEAREST_NEIGHBOR);
		flipped = op.filter(sky, null);
		skyImages[VT_FLIP] = flipped;

		tx = AffineTransform.getScaleInstance(-1, 1);
		tx.translate(-sky.getWidth(null), 0);
		op = new AffineTransformOp(tx, AffineTransformOp.TYPE_NEAREST_NEIGHBOR);
		flipped = op.filter(sky, null);
		skyImages[HZ_FLIP] = flipped;

		tx = AffineTransform.getScaleInstance(-1, -1);
		tx.translate(-sky.getWidth(null), -sky.getHeight(null));
		op = new AffineTransformOp(tx, AffineTransformOp.TYPE_NEAREST_NEIGHBOR);
		flipped = op.filter(sky, null);
		skyImages[HZ_VT_FLIP] = flipped;


		//Initialize background tiles
		tileSize = new Rectangle((int) sky.getWidth(), (int) sky.getHeight());
		double tempPatternOffset = 4;
		bgRows = (int) Math.ceil(viewport.getHeight() / tileSize.getHeight()) + BG_TILE_ROWS;
		bgCols = (int) Math.ceil(viewport.getWidth() / tileSize.getWidth()) * 2 + 2 * HZ_PATTERN_SIZE;
		backgroundTiles = new Tile[bgRows * bgCols];
		bgTilePositions = new Point[bgRows * bgCols];

		int row = 0;
		int col = 0;
		for(int i = 0; i < backgroundTiles.length; i++) {
			row = i / bgCols;
			col = i % bgCols;

			if(row % 2 == 1) {
				if(col % 2 == 0) {
					backgroundTiles[i] = new Tile(skyImages[NORMAL]);
				} else {
					backgroundTiles[i] = new Tile(skyImages[HZ_FLIP]);
				}
			} else {
				if(col % 2 == 0) {
					backgroundTiles[i] = new Tile(skyImages[VT_FLIP]);
				} else {
					backgroundTiles[i] = new Tile(skyImages[HZ_VT_FLIP]);
				}
			}

			row -= 1;//shift down
			col -= bgCols / 2;//shift left

			bgTilePositions[i] = new Point(col * backgroundTiles[i].getImage().getWidth()
				, row * backgroundTiles[i].getImage().getHeight());

		}

		mtnBgCols = (int) Math.ceil(viewport.getWidth() / mountainTileSize.getWidth()) + MOUNTAIN_PATTERN_SIZE;
		//bgRows = (int) Math.ceil(viewport.getHeight() / mountainTileSize.getHeight()) + MOUNTAIN_PATTERN_SIZE;
		mtnBgRows = 1;

		mountainTiles = new Tile[mtnBgRows * mtnBgCols];

		//System.out.println("NUM ROWS/COLS " + rows + "/" + cols);

		//Initialize background
		Random r = new Random();

		for(int i = 0; i < mountainTiles.length; i++) {
			mountainTiles[i] = new Tile(mountainImages[i % 6]);
		}

		Rectangle temptileSize = new Rectangle(groundTextures[0].getWidth(), groundTextures[0].getHeight());
		//bgRows = (int) Math.ceil(viewport.getHeight() / temptileSize.getHeight()) + GROUND_PATTERN_SIZE;
		bgRows = 1;
		bgCols = (int) Math.ceil(viewport.getWidth() / temptileSize.getWidth()) * 2 + GROUND_PATTERN_SIZE;
		groundTiles = new Tile[bgRows * bgCols];
		groundTilePositions = new Point[bgRows * bgCols];

		for(int i = 0; i < groundTiles.length; i++) {
			row = (i / bgRows);
			col = (i % bgCols) - 1 - bgCols / 2;
			//Debug.print("Init ground tile x to " + col * groundTexture[0].getWidth());
			groundTilePositions[i] = new Point(col * groundTexture[0].getWidth(), 0);
			groundTiles[i] = new Tile(groundTextures[0]);//initialize all as tundra
		}

		initializeClouds();
	}

	public Point getGroundLayerCenter() {
		return new Point(groundLayerCenter);
	}

	public void setDirection(Vector v) {
		if(v != null) {
			direction = v;
		} else {
			Debug.print("Background.java:setDirection(): v cannot be null");
			direction = new Vector(0.0, 0.0);
		}
	}

	//Turn cloud spawning on / generate cloud tasks for drawing
	public void cloudsOn() {
		cloudSpawning = true;
		resetAllClouds();
	}

	public void cloudsOff() {
		cloudSpawning = false;
	}

	public void setCenter(Point p) {
		//center = new Point(Math.ceil(p.getX()), Math.ceil(p.getY()));
		center = p;
		altitude = center.getY();
	}

	public LinkedList<DrawingTask> generateMountains() {
		Point p;
		LinkedList<DrawingTask> tasks = new LinkedList<DrawingTask>();

		for(int i = 0; i < mountainTiles.length; i++) {
			p = getMountainOffset(i);

			//if(mountainTiles[i].getImage() == null)
				//Debug.print("mtn tile is NULL");

			tasks.add(new DrawingTask(mountainTiles[i].getImage(), p
				, parent.getMountainTransform()
				, new Point(mountainTiles[i].getImage().getWidth() / 2
					, mountainTiles[i].getImage().getHeight() / 3)
				, 0));
		}

		return tasks;

	}

	/**
	* Create tasks for all background tiles.
	*/
	public LinkedList<DrawingTask> generateBackground() {
		LinkedList<DrawingTask> tasks = new LinkedList<DrawingTask>();
		Point p;

		for(int i = 0; i < backgroundTiles.length; i++) {
			p = getBackgroundOffset(i);
			tasks.add(new DrawingTask(backgroundTiles[i].getImage(), p
				, parent.getBackgroundTransform(), new Point(0, 0), 0));
		}

		return tasks;
	}

	/**
	* Create the drawing tasks for player trace points.
	*/
	public LinkedList<DrawingTask> generateTraces() {
		Animation a;
		LinkedList<DrawingTask> tasks = new LinkedList<DrawingTask>();
		Point p;

		for(int i = 0; i < trace.size(); i++) {
			p = getTraceOffset(i);
			a = smoke.get(i);
			tasks.add(new DrawingTask(a.getFrame(), p
				, parent.getScreenTransform()
				, new Point(a.getFrame().getWidth() / 2, a.getFrame().getHeight() / 2)
				, 0));
		}

		return tasks;
	}


	public void updateAnimations(int f) {
		for(int i = 0; i < smoke.size(); i++) {
			smoke.get(i).tick(f);
		}
	}


	private void setGroundImage(Point p, int i) {
		//p.scale(1.0 / FG_SCALE);
		p = new Point(p.getX() / groundScale, p.getY() / groundScale);
		//Debug.print("st at p x " + p.getX());
		SegmentType st = map.getSegmentType(p);
		//Debug.print("setting to st " + st.ordinal());
		groundTiles[i].setImage(groundTextures[st.ordinal()]);

	}


	/**
	* Create the drawing tasks for all groundTexture backgroundTiles.
	*/
	public LinkedList<DrawingTask> generateGround() {
		Point p, mid;
		LinkedList<DrawingTask> tasks = new LinkedList<DrawingTask>();
		mid = new Point(0.0//groundTiles[0].getImage().getWidth() / 2
				//, groundTiles[0].getImage().getHeight());
				, 0.0);
				
		for(int i = 0; i < groundTiles.length; i++) {
			p = getGroundTileOffset(i);
			//Debug.print("ground tile world loc " + p);
			setGroundImage(p, i);
			tasks.add(new DrawingTask(groundTiles[i].getImage(), p
				, parent.getGroundTransform(), mid, 0));
		}

		return tasks;
	}

	/**
	* Create the drawing tasks for all clouds.
	*/
	public LinkedList<DrawingTask> generateClouds() {
		BufferedImage b = null;
		LinkedList<DrawingTask> tasks = new LinkedList<DrawingTask>();
		Point p;

		//Toggle cloud spawning as player crosses the ceiling
		if(altitude > ceiling) {
			if(!cloudSpawning)
				cloudsOn();
		} else if(cloudSpawning) {
			cloudsOff();
		}

		for(int i = 0; i < clouds.length; i++) {
			p = getCloudOffset(i);
			b = cloudTextures.get(types[i].ordinal());
			tasks.add(new DrawingTask(b, p, parent.getCloudTransform()
				, parent.getImageMidpoint(b), 0));
		}

		return tasks;
	}

	/**
	* Set the scrolling vector of the background.
	* @param v - the movement Vector in M/S of the player
	*/
	public void setVector(Vector v) {
		double dt = 1.0 / frameRate;
		bg = Vector.create(-BG_SCALE * v.getX() * dt, -BG_SCALE * v.getY() * dt);
		fg = Vector.create(-FG_SCALE * v.getX() * dt, -FG_SCALE * v.getY() * dt);
	}

	public void addCloudImg(BufferedImage b) {
		int xFlip, yFlip;
		AffineTransform tx;
		AffineTransformOp op;
		BufferedImage flipped;
		Random rand = new Random();

		if(b != null) {
			cloudTextures.add(b);
			//randomly flip texture then add to cloud texture list
			xFlip = rand.nextInt(3) - 1;
			yFlip = 1;
//yFlip = rand.nextInt(3) - 1;

			if(xFlip == 0)
				xFlip = 1;

			//if(yFlip == 0)
			//	yFlip = 1;

			//Debug.print("xF, yF " + xFlip + " " + yFlip);
			tx = AffineTransform.getScaleInstance(xFlip, yFlip);

			if(xFlip > 0)
				xFlip = 0;

			if(yFlip > 0)
				yFlip = 0;

			tx.translate(xFlip * b.getWidth(null), yFlip * b.getHeight(null));
			op = new AffineTransformOp(tx, AffineTransformOp.TYPE_NEAREST_NEIGHBOR);
			flipped = op.filter(b, null);
			cloudTextures.add(flipped);

		} else {
			Debug.print("Background.java:addCloudImg(): b cannot be null");
		}
	}

	/**
	* Set the cloud image
	* of the scrolling background.

	public void setCloudImage(BufferedImage cloudImg) {
		if(cloudImg != null) {
			cloud = cloudImg;
		}
	}	*/

	/**
	* Set the altitude (MSL) of the player.
	*/
	//public void setAltitude(double a) {
		//altitude = a;
	//}

	/**
	* Set the trace point image for RJ
	* on the scrolling background.
	*/
	public void setTraceImage(BufferedImage traceImg) {
		if(traceImg != null) {
			tracePoint = traceImg;
		}
	}

	public void setSmokeAnimation(Animation a) {
		if(a != null) {
			smokeAnimation = a;
		}
	}

	/**
	* Get the backgroundTextured background image for drawing.
	*/
	public BufferedImage getbackgroundTextureImage() {
		return backgroundTexture;
	}

	/**
	* Scroll background and update offsets for drawing.
	*/
	public void update() {
		Point p = center;
	
		//For all layers of background, if center has exceeded bounds -> reset
		if(Math.abs(center.getX() * cloudScale - cloudLayerCenter.getX())
			>= CLOUD_LAYER_LIMIT * viewport.getWidth()) {
			cloudLayerCenter.setX(center.getX() * cloudScale);
			resetOffscreenClouds();
		}
		
		if(Math.abs(center.getY() * cloudScale - cloudLayerCenter.getY())
			>= CLOUD_LAYER_LIMIT * viewport.getHeight()) {
			cloudLayerCenter.setY(center.getY() * cloudScale);
			resetOffscreenClouds();
		}

		if(Math.abs(center.getX() * mountainScale - mountainLayerCenter.getX())
			>= MOUNTAIN_PATTERN_SIZE * mountainTileSize.getWidth())
			mountainLayerCenter.setX(center.getX() * mountainScale);

		if(Math.abs(center.getX() * backgroundScale - backgroundLayerCenter.getX())
			> HZ_PATTERN_SIZE * tileSize.getWidth())
			backgroundLayerCenter.setX(center.getX() * backgroundScale);

		if(Math.abs(center.getY() * backgroundScale - backgroundLayerCenter.getY())
			> VT_PATTERN_SIZE * tileSize.getHeight())
			backgroundLayerCenter.setY(center.getY() * backgroundScale);

		if(Math.abs(center.getX() * groundScale - groundLayerCenter.getX())
			>= LIMIT * groundTextures[0].getWidth())
			groundLayerCenter.setX(center.getX() * groundScale);

		//Remove trace points that have exceeded the left bound of the screen
		for(int i = 0; i < trace.size(); i++) {
			p = trace.get(i);
			//System.out.println("trace x " + p.getX());
			//System.out.println("center x " + center.getX());
			//System.out.println("bound " + (center.getX() - LIMIT * viewport.getWidth()))

			if(trace.size() > MAX_TRACE) {
				trace.remove(i);//if number of traces exceeded, remove current ... might be removing from wrong end!
				smoke.remove(i);
			} else if(p.getX() < center.getX() - viewport.getWidth() / 2) {//if trace center (S) exceeds left bound of screen
				trace.remove(i);
				smoke.remove(i);
				//Why does not removing them or having p.getX() < 0 result in index out of range error?
			}
		}
	}

	/*
	* Reset all cloud positions.
	*/
	private void resetAllClouds() {
		for(int i = 0; i < clouds.length; i++) {
			resetCloud(i);
		}
	}
	
	/*
	* Reset clouds that appear offscreen.
	*/
	private void resetOffscreenClouds() {
		double cloudRightX = 0, cloudBottomY = 0;
		//camera space
		double screenLeftX = center.getX() * cloudScale - viewport.getWidth();
		double screenTopY = center.getY() * cloudScale + viewport.getHeight() / 2;
		double screenBottomY = center.getY() * cloudScale - viewport.getHeight() / 2;

		for(int i = 0; i < clouds.length; i++) {
			cloudBottomY = getCloudOffset(i).getY() + cloudSize.getHeight();
			cloudRightX = getCloudOffset(i).getX() + cloudSize.getWidth();

			if(cloudRightX < screenLeftX) //|| cloudBottomY < screenTopY
				//|| getCloudOffset(i).getY() < screenBottomY)
				resetCloud(i);
		}
	}

	/*
	* Reset the position of the cloud given by index.
	* Cloud spawning works with player motion in right direction only.
	* This can get stuck in an infinite loop if the number of clouds is too high.
	* *** opportunity for improvement
	*/
	private void resetCloud(int index) {
		Random rand = new Random();

		if(index >= 0 && index < clouds.length) {
			boolean valid;
			int min, count = 0;
			double dist;
			Point newLoc;
			Vector offset;
			
			do {
				valid = true;
				//set new cloud position randomly within spawnarea
				//correct Y so that player resides in middle of the vertical spawn area
				newLoc = new Point(rand.nextFloat() * spawnArea.getWidth()
					, rand.nextFloat() * spawnArea.getHeight() - spawnArea.getHeight() / 2);
				min = (int) Math.sqrt(Math.pow(viewport.getWidth(), 2)
					+ Math.pow(viewport.getHeight(), 2))
					+ ((int) Math.sqrt(Math.pow(spawnArea.getWidth(), 2)
					+ Math.pow(spawnArea.getHeight(), 2))) / 2;
				min *= C_SPAWN_AREA;

				offset = new Vector(bg).flip();
				offset.setMagnitude(min);
				//System.out.println(offset);
				//System.out.println(newLoc);
				newLoc = Point.add(Point.add(newLoc, new Point(offset)), cloudLayerCenter);

				/* very inefficient! this might run 50 or more times in one frame.
				   opportunity for improvement: random cloud spawning with overlap prevention */
				for(int i = 0; i < clouds.length; i++) {
					dist = new Vector(Point.subtract(clouds[i], newLoc)).getMagnitude();
					
					if(dist < cloudSize.getWidth()) {
						valid = false;
						break;
					}
				}
				
				count++;				
			} while (!valid);

			if(Debug.verbose)
				Debug.print("Background.java: resetCloud(...): attempts = "
					+ count);
			
			if(!cloudSpawning)
				newLoc = OFFSCREEN;

			clouds[index].setLocation(newLoc);
		} else {
			Debug.print("Background.java:resetCloud(): index out of range");
		}
	}

	/*
	* Initialize cloud types and positions.
	*/
	private void initializeClouds() {
		int type;
		Random rand = new Random();

		for(int i = 0; i < clouds.length; i++) {
			type = rand.nextInt(CLOUD_TYPES);
			types[i] = CloudType.values()[type];
			clouds[i] = new Point(OFFSCREEN);//initialize to offscreen
		}
	}

	/**
	* Add a trace point to the background.
	* @param p the point in screen space (pixels)
	*/
	public void addTrace(Point p, Vector v) {
		if(p != null && v != null) {
			trace.add(p);
			//traceVelocity.add(v);
			if(smokeAnimation != null) {
				Animation a = new Animation(smokeAnimation);
				a.forward();
				a.start();
				smoke.add(a);
			}
		} else {
			Debug.print("Background.java:addTrace(...): p & v cannot be null");
		}
	}

	/*
	* 
	*/
	private Point getCloudOffset(int i) {
		Point p = new Point(0, 0);

		if(i >= 0 && i < clouds.length)
			p = clouds[i];
		else if(Debug.verbose)
			Debug.print("Background.java:getCloudOffset(): cloud index out of range");
			
		return p;
	}

	/*
	* Get a point representing the world space coordinate of a groundTexture tile.
	*/
	public Point getGroundTileOffset(int i) {
		Point p;

		if(i >= 0 && i < groundTilePositions.length) {
			//Debug.print("ground tile world loc " + groundTilePositions[i]);
			//Debug.print("ground tile x y " + groundTiles[i].getX() + " " + groundTiles[i].getY() );
			p = Point.add(groundTilePositions[i], groundLayerCenter);

			//Debug.print("final x y " + p.getX() + " " + p.getY() );

		} else {
			p = new Point(0, 0);

			if(Debug.verbose) {
				Debug.print("Background.java:getGroundTileOffset(): index out of range");
			}
		}

		return p;
	}

	/**
	* Get a point representing the world space coordinate of a mountain tile.
	*/
	public Point getMountainOffset(int i) {
		int col = i % mtnBgCols;
		int row = 0;
		Point p = new Point(col * mountainTileSize.getWidth(), mountainImages[0].getHeight());

		if(i >= 0 && i < mountainTiles.length)
			p = Point.add(mountainLayerCenter, p);
		else
			Debug.print("Background.java:getMountainOffset(): tile index out of range");
			
		return p;
	}


	/**
	* Get a point representing the world space coordinate of a background tile.
	*/
	public Point getBackgroundOffset(int i) {
		Point p = bgTilePositions[i];
		
		if(i >= 0 && i < backgroundTiles.length) {
			p = Point.add(backgroundLayerCenter, p);
		} else {
			Debug.print("Background:getBackgroundOffset(): index out of range");
		}

		return p;
	}

	/**
	* Get a point representing the world space coordinate of a trace point.
	*/
	public Point getTraceOffset(int i) {
		Point p;
		
		if(i >= 0 && i < trace.size()) {
			p = trace.get(i);
		} else {
			p = new Point(0, 0);
			
			if(Debug.verbose)
				Debug.print("Background.java:getTraceOffset(...): trace index out of range " + i);
		}

		return p;
	}
}
//End of file
