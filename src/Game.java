package src;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.util.*;
import javax.imageio.ImageIO;
import java.io.File;
import java.io.IOException;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsEnvironment;
import java.awt.Transparency;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.awt.image.AffineTransformOp;
import java.awt.geom.Rectangle2D;
import java.text.NumberFormat;
import java.text.DecimalFormat;
import src.Keyboard.KeyCode;
import src.Mouse.Code;
import src.DrawingTask.Space;
import src.DrawingTask.Type;
import src.Map.SegmentType;

/**
* Game driver.
*/
public class Game {

	public enum GameState {
		CRASHED_TERRAIN,
		CRASHED_GROUND,
		EXIT_BUTTON,
		MIDAIR_COLLISION
	}


	public static final String SFX_GEAR = "sfx/geardn.wav";
	public static final String SFX_MUSIC = "sfx/beetsalad3.wav";
	public static final String SFX_ENG = "sfx/eng.wav";
	public static final String DEFAULT_LEVEL = "levels/fuel_transfer.txt";
	public static final int SCREEN_WIDTH = 1500;
	public static final int SCREEN_HEIGHT = 700;
	public static final int TARGET_FPS = 60;
	public static final int NUM_PLAYERS = 2;
	public static final double FRAME_DELAY;//tick between frames
	public static final double MS_PER_S = 1000.0;
	public static final double NS_PER_S = 1000000000;//1e+9
	public static final int DEGREES_IN_HEMISPHERE = 180;
	public static final int PHYS_PER_FRAME = 2;
	public static final int NOZZLE_ATTRACT_DISTANCE = 3;
	public static final double C_NOZZLE = 100.0;
	public static final double PLAYER_SPRITE_SCALE = 0.9;

	static {
		FRAME_DELAY = MS_PER_S / TARGET_FPS;
	}

	private static final int GROUND_SCREEN_LIMIT = SCREEN_HEIGHT - 150;//# of px from bottom of screen
	private static final int NAV_AQUIRE_RANGE = 50000;
	private static final int TERRAIN_MODE_LIMIT = 200;

	/* Game constants */
	private static final double FG_SCALE = 18.0 * PLAYER_SPRITE_SCALE;
	private static final double BG_SCALE = 5.0;
	public static final double MOUNTAIN_SCALE = 9.0;
	public static final double MOUNTAIN_TILE_SCALE = 4.0;
	public static final int MOUNTAIN_SCREEN_OFFSET = -430;//hack
	private static final double BG_TILE_SCALE = 3.5;
	private static final float CLOUD_SCALE = 18.0f;
	private static final float CLOUD_IMAGE_SCALE = 7.0f;
	private static final double GROUND_SCALE = FG_SCALE;
	private static final float GROUND_TILE_SCALE = 4.0f;
	private static final float METERS_PER_NM = 1852.0f;
	private static final float METERS_PER_KM = 1000.0f;
	private static final double D_PLAYER_DIRECTION = 0.0;
	private static final double D_PLAYER_SPEED = 0.9;//0.9;
	private static final double D_AI_SPEED = 0.9;
	private static final int ENV_WIDTH = 5000000;
	private static final int ENV_HEIGHT = 100000;
	private static final int ENV_SCALE = 800;
	private static final int CEILING = 1000;
	public static final int REFUEL_ALTITUDE = 3000;//meters
	private static final int PLAYER1 = 0;
	private static final int PLAYER2 = 1;
	private static final double MAX_STEP = 0.015;

	/*element positioning*/
	public static final int NUM_SMOKE_PUFFS = 10;
	public static final int SFX_WIND_MAX_SPEED = 300;
	public static final int TANKER_ICON_Y = 12;
	public static final int TANKER_ICON_X = 10;
	public static final int HZ_ARROW_OFFSET_X = 80;
	public static final int VT_ARROW_OFFSET_X = 55;
	public static final int ARROW_OFFSET_Y = 50;
	private static final int RADAR_X = SCREEN_WIDTH - 200;
	private static final int RADAR_Y = SCREEN_HEIGHT - 80;
	private static final int WIND_VECTOR_X = 25;
	private static final int WIND_VECTOR_Y = 18;
	private static final int PLAYER_SCREEN_X = 600;
	private static int PLAYER_SCREEN_Y = 360;
	private static final int GROUND_OFFSET = 33;
	private static final int TURBINE_VECTOR_WIDTH = 8;
	private static final int AP_PRINTOUT_X = 300;
	private static final int AP_PRINTOUT_Y = SCREEN_HEIGHT - 130;
	private static final int AP_PRINTOUT_MARGIN = 20;
	private static final int FUEL_GAUGE_INDICATOR_Y = 8;
	private static final int FUEL_GAUGE_INDICATOR_X = 5;
	private static final int HUD_FUEL_GAUGE_X = 1100;
	private static final int HUD_FUEL_GAUGE_Y = 80;
	private static final int HUD_FLAP_GAUGE_X = 800;
	private static final int HUD_FLAP_GAUGE_Y = 80;
	private static final int HUD_FLAP_GAUGE_INDICATOR_Y = 10;
	private static final int SPEED_TAPE_X = 150;
	private static final int SPEED_TAPE_Y = 72;
	private static final int SPEED_TAPE_INDICATOR_Y = 54;
	private static final int THROTTLE_X = 87;
	private static final int THROTTLE_Y = 73;
	private static final int THROTTLE_INDICATOR_X = THROTTLE_X + 12;
	private static final int THROTTLE_CONSOLE_WIDTH = 58;
	private static final int GLIDESLOPE_INDICATOR_WIDTH = 75;
	private static final int GLIDESLOPE_INDICATOR_HEIGHT= 150;
	private static final int GLIDESLOPE_INDICATOR_X = SCREEN_WIDTH - GLIDESLOPE_INDICATOR_WIDTH;
	private static final int GLIDESLOPE_INDICATOR_Y = SCREEN_HEIGHT - GLIDESLOPE_INDICATOR_HEIGHT;
	private static final int ATTITUDE_X = 500;
	private static final int ATTITUDE_Y = 70;
	private static final int ATTITUDE_INDICATOR_X = 5;
	private static final int ATTITUDE_INDICATOR_y = 10;
	private static final int HUD_BACKING_OFFSET_X = -38;
	private static final int HUD_BACKING_OFFSET_Y = -10;
	private static final float ATTITUDE_ATH_SCALE = 2.2f;
	private static final int PITCH_X = 600;
	private static final int PITCH_Y = 70;
	private static final int GEAR_X = 900;
	private static final int GEAR_Y = 70;
	private static final int SPEED_BRAKE_WARN_X = 12;
	private static final int SPEED_BRAKE_WARN_Y = 12;
	private static final int NUM_RULES = 250;
	private static final int HORIZONTAL_STEP = (int) (METERS_PER_KM / 4);
	private static final int VERTICAL_STEP = 50;
	private static final int HORIZONTAL_RULE_Y = 11;
	private static final int VERTICAL_RULER_X = 3;
	private static final int QUAD_DIMENSION = 40;
	private static int GPWS_QUAD_SIZE;
	private static int GPWS_MAX;
	private static final int INITIAL_EXHAUST_SPEED = 1;
	private static final int HUD_START_X = 300;
	private static final int HUD_HEIGHT = 66;
	private static final int HUD_START_Y = SCREEN_HEIGHT - HUD_HEIGHT;
	private static final Color GROUND_WARNING_COLOR = new Color(196, 157, 129);
	private static final double GLIDESLOPE_INDICATOR_SCALE = 0.3;
	private static final double WHEEL_STICK_ZONE = 0.1;
	private static final double THROTTLE_INPUT_SCALE = 0.0005;
	public static final int N_CLOUD_IMAGES = 8;//4 original, plus 4 flipped
	public static final int TERRAIN_PATTERN_SIZE = 3;
	public static final int GROUND_PATTERN_SIZE = 4;
	public static final int ALTITUDE_LABEL_OFFSET = 45;
	public static final int POSITION_LABEL_OFFSET = 17;
	public static final int SHUTTLE_GROUND_OFFSET = -10;
	public static final int TANKER_GROUND_OFFSET = -7;
	private static final int NUM_ARROWS = 4;
	
	private static final int BG_SCREEN_OFFSET = 120;
	
	
	//most of this can be refactored to an associative data structure
	
	private static Point TERRAIN_ORIGIN;
	/*instance variables and components*/
	private Aircraft playerAircraft;
	private Aircraft computerAircraft;
	private Color terrainColor;
	private AffineTransform world, screen;
	private ArrayList<Pilot> players;
	private ArrayList<Waypoint> waypoints;
	private Window window;
	private Panel panel;
	private javax.swing.Timer timer;//full namespace because of wildcard import of awt and swing???
	private javax.swing.Timer eventTimer;
	private TimerListener timerListener;
	private TimerListener delayedEventListener;
	private Objective gameObjective;
	private Environment environment;
	private Terrain terrain;
	private Terrain terrain2;
	private Controller controller;
	private Random rand;
	private Background bg;
	private Point origin;
	private ArrayList<Point> trace;
	private Keyboard keyListener;
	private Mouse mouseListener;
	private Wind randWind;
	private Wind windEvent;
	private Aircraft.Type type;
	private Glideslope glideslope;
	//private Sound sound;
	private Animation mainGearAnimation;
	private Animation mainWheelAnimation;
	private Animation noseGearAnimation;
	private Animation noseWheelAnimation;
	private Animation beaconAnimation;
	private Animation smokeAnimation;
	private BufferedImage skyTile;
	private BufferedImage[] terrainTiles;
	//Refactor to an associatve data structure
	private BufferedImage[] groundTiles;
	private BufferedImage rj;
	private BufferedImage gear;
	private BufferedImage noseGearSprite;
	private BufferedImage mainGearSprite;
	private BufferedImage[] playerImages;
	private BufferedImage boomLink;
	private BufferedImage nearestPoint;
	private BufferedImage traceImage;
	private BufferedImage meshPoint;
	private BufferedImage smoke;
	private BufferedImage[] clouds;
	private BufferedImage windArrow;
	private BufferedImage windRadar;
	private BufferedImage arrow;
	private BufferedImage arrowFlipped;
	private BufferedImage arrows[];
	private BufferedImage beaconSprite;
	private BufferedImage impact;
	private BufferedImage elevator;
	private BufferedImage fuelGauge;
	private BufferedImage fuelGaugeIndicator;
	private BufferedImage flapGauge;
	private BufferedImage flapGaugeIndicator;
	private BufferedImage fuelGaugeDisconnected;
	private BufferedImage fuelGaugeConnected;
	private BufferedImage speedTape;
	private BufferedImage speedTapeIndicator;
	private BufferedImage speedTapeBacking;
	private BufferedImage throttle;
	private BufferedImage throttleIndicator;
	private BufferedImage nearestImage;
	private BufferedImage winMessage;
	private BufferedImage glideslopeGauge;
	private BufferedImage glideslopeIndicator;
	private BufferedImage attitudeBacking;
	private BufferedImage attitudeTop;
	private BufferedImage attitudeBottom;
	private BufferedImage attitudeIndicator;
	private BufferedImage pitchBacking;
	private BufferedImage pitchIndicator;
	private BufferedImage speedBrakeWarning;
	private BufferedImage tankerIcon;
	private BufferedImage waypointBacking;
	private BufferedImage gearBacking;
	private BufferedImage gearDown;
	private BufferedImage gearLocked;
	private BufferedImage gearTransit;
	private BufferedImage nozzle;
	private BufferedImage waypoint;
	private BufferedImage hudBacking;
	private double p2X;
	private double p2Y;
	private boolean ready;
	private boolean windEventActive;
	private boolean extraUI;
	private boolean gameRunning = true;
	private int windEventFrame;
	private int windEventDuration;
	/*used for tracking game run time*/
	private double deltaTime;
	private double targetDelta;
	private double currentTime;
	/*used for framerate calculations*/
	private int fps;
	private int tick;
	private int currentFrame;
	private int netFrameCounter;
	private int physCounter;
	private static final int HUD_VECTOR_WIDTH = 2;
	private Rectangle viewport;
	private Point worldTransform;
	private Point worldTransformP2;
	private Point computerScreenTransform;
	private Point screenTransform;
	private Point bgScreenTransform;
	private Point imageMidpoint;
	private Point bgTransform;
	private Point mountainTransform;
	private Point cloudTransform;
	private Point groundTransform;
	private Frame frame;
	private Map map;
	private Color forceVectorColor;
	private Color angularForceVectorColor;
	private Color directionVectorColor;
	private Color boomLinkColor;
	private int currentWaypoint;
	private GameMode gameMode;

	public enum GameMode {
		MENU,
		GAME
	}

	private double oldDelta;
	
	public Point getBackgroundTransform() {
		return bgTransform;
	}
	
	public Point getScreenTransform() {
		return screenTransform;
	}

	public Point getMountainTransform() {
		return mountainTransform;
	}

	public Point getGroundTransform() {
		return groundTransform;
	}

	public Point getCloudTransform() {
		return cloudTransform;
	}

	public static BufferedImage toBufferedImage(Image img) {
		BufferedImage bi;
		Graphics2D g2d;
		if(img instanceof BufferedImage)
		{
			bi = (BufferedImage)img;
		} else {
			bi = new BufferedImage(img.getWidth(null), img.getHeight(null), BufferedImage.TYPE_INT_ARGB);
		}
		//null ImageObserver
		bi = new BufferedImage(img.getWidth(null), img.getHeight(null), BufferedImage.TYPE_INT_ARGB);
		g2d = bi.createGraphics();
		g2d.drawImage(img, 0, 0, null);
		g2d.dispose();
		return bi;

	}
	/**
	* An instance of the WINDSHEAR game.
	* Refuel your aircraft, don't spill too much fuel.
	*/
	Game(Aircraft.Type at) {
		//sound = new Sound();
		ArrayList<BufferedImage> allImages = new ArrayList<BufferedImage>();
		String path;
		fps = 0;
		tick = 0;
		currentFrame = 0;
		physCounter = 0;
		oldDelta = 0;
		ready = true;
		extraUI = false;
		frame = new Frame();
		worldTransform = new Point(0, 0);
		worldTransformP2 = new Point(0, 0);
		computerScreenTransform = new Point(0, 0);
		screenTransform = new Point(0, 0);
		bgTransform = new Point(0, 0);
		mountainTransform = new Point(0, 0);
		groundTransform = new Point(0, 0);
		cloudTransform = new Point(0, 0);
		imageMidpoint = new Point(0, 0);
		map = new Map(DEFAULT_LEVEL);
		TERRAIN_ORIGIN = new Point(Terrain.SEGMENT_SIZE, Map.GROUND_ALTITUDE);
		GPWS_QUAD_SIZE = (int) TERRAIN_ORIGIN.getY() - 50;
		GPWS_MAX = (int) TERRAIN_ORIGIN.getY() + GPWS_QUAD_SIZE;
		
		gameMode = GameMode.MENU;
		glideslope = new Glideslope();
		currentWaypoint = 0;
		clouds = new BufferedImage[N_CLOUD_IMAGES];
		terrainTiles = new BufferedImage[TERRAIN_PATTERN_SIZE];
		groundTiles = new BufferedImage[GROUND_PATTERN_SIZE];
	 	arrows = new BufferedImage[NUM_ARROWS];
		viewport = new Rectangle(SCREEN_WIDTH, SCREEN_HEIGHT);
		forceVectorColor = Color.RED;
		angularForceVectorColor = Color.BLACK;
		directionVectorColor = Color.MAGENTA;
		boomLinkColor = new Color(116, 116, 116);
		players = new ArrayList<Pilot>();
		playerImages = new BufferedImage[NUM_PLAYERS];
		targetDelta = MS_PER_S / TARGET_FPS / PHYS_PER_FRAME;
		//Debug.print("targetDelta " + targetDelta);

		if(at != null) {
			type = at;
		} else {
			type = Aircraft.Type.SHUTTLE;
			Debug.print("Game.java:Game(...): at cannot be null, using default aircraft");
		}
		
		//create aircraft
		at = (type == Aircraft.Type.SHUTTLE) ? Aircraft.Type.TANKER : Aircraft.Type.SHUTTLE;
		playerAircraft = new Aircraft(type, Map.PLAYER_1_VECTOR, Map.PLAYER_1_START, targetDelta);
		playerAircraft.trim();
		playerAircraft.toggleOutput();
		path = playerAircraft.getResourcePath().trim();//animation resource path for p1
		computerAircraft = new Aircraft(at, Map.PLAYER_2_VECTOR, Map.PLAYER_2_START, targetDelta);
		computerAircraft.trim();
		players.add(new Pilot(playerAircraft));
		players.add(new Pilot(computerAircraft));

		try {
			skyTile = getCompatibleImage(ImageIO.read(new File("png/sky.png")));
			terrainTiles[0] = ImageIO.read(new File("png/mountain1.png"));
			terrainTiles[1] = ImageIO.read(new File("png/mountain2.png"));
			terrainTiles[2] = ImageIO.read(new File("png/mountain3.png"));
			groundTiles[0] = ImageIO.read(new File("png/tundra.png"));
			groundTiles[1] = ImageIO.read(new File("png/runway_start.png"));
			groundTiles[2] = ImageIO.read(new File("png/runway.png"));
			groundTiles[3] = ImageIO.read(new File("png/runway_end.png"));
			clouds[0] = ImageIO.read(new File("png/cloud1.png"));
			clouds[1] = ImageIO.read(new File("png/cloud2.png"));
			clouds[2] = ImageIO.read(new File("png/cloud3.png"));
			clouds[3] = ImageIO.read(new File("png/cloud4.png"));
			mainGearSprite = getCompatibleImage(ImageIO.read(new File("png/"
				+ path + "/main_gear_sprite.png")));
			noseGearSprite = getCompatibleImage(ImageIO.read(new File("png/"
				+ path + "/nose_gear_sprite.png")));
			beaconSprite = getCompatibleImage(ImageIO.read(new File("png/beacon.png")));
			windRadar = getCompatibleImage(ImageIO.read(new File("png/wind_radar.png")));
			
			//path = playerAircraft.getResourcePath().trim()
			int count = 0;
			
			for(Pilot pi : players) {
				playerImages[count++] = getCompatibleImage(ImageIO.read(new File("png/"
					+ pi.isFlying().getResourcePath().trim() + "/base.png")));
			}
			
			//playerImages[PLAYER2] = getCompatibleImage(ImageIO.read(new File("png/"
				//+ computerAircraft.getResourcePath().trim() + "/base.png")));
			
			traceImage = getCompatibleImage(ImageIO.read(new File("png/trace_point.png")));
			meshPoint = getCompatibleImage(ImageIO.read(new File("png/mesh_point.png")));
			windArrow = getCompatibleImage(ImageIO.read(new File("png/wind_arrow.png")));
			smoke = getCompatibleImage(ImageIO.read(new File("png/smoke.png")));
			boomLink = getCompatibleImage(ImageIO.read(new File("png/boom_link.png")));
			nearestPoint = getCompatibleImage(ImageIO.read(new File("png/nearestPoint.png")));
			elevator = getCompatibleImage(ImageIO.read(new File("png/"
				+ path + "/elevator.png")));
			winMessage = getCompatibleImage(ImageIO.read(new File("png/win_message.png")));
			fuelGauge = getCompatibleImage(ImageIO.read(new File("png/fuel_gauge.png")));
			fuelGaugeIndicator = getCompatibleImage(ImageIO.read(new File("png/fuel_gauge_indicator.png")));
			fuelGaugeDisconnected = getCompatibleImage(ImageIO.read(new File("png/fuel_gauge_inactive.png")));
			fuelGaugeConnected = getCompatibleImage(ImageIO.read(new File("png/fuel_gauge_active.png")));
			flapGauge = getCompatibleImage(ImageIO.read(new File("png/flap_gauge.png")));
			flapGaugeIndicator = getCompatibleImage(ImageIO.read(new File("png/flap_gauge_indicator.png")));
			speedTape = getCompatibleImage(ImageIO.read(new File("png/speed_tape.png")));
			speedTapeBacking = getCompatibleImage(ImageIO.read(new File("png/speed_tape_backing.png")));
			speedTapeIndicator = getCompatibleImage(ImageIO.read(new File("png/speed_indicator.png")));
			throttle = getCompatibleImage(ImageIO.read(new File("png/throttle.png")));
			throttleIndicator = getCompatibleImage(ImageIO.read(new File("png/throttle_indicator.png")));
			glideslopeGauge = getCompatibleImage(ImageIO.read(new File("png/glideslope_backing.png")));
			glideslopeIndicator = getCompatibleImage(ImageIO.read(new File("png/glideslope_indicator.png")));
			attitudeBacking = getCompatibleImage(ImageIO.read(new File("png/attitude_backing.png")));
			attitudeTop = getCompatibleImage(ImageIO.read(new File("png/attitude_top.png")));
			attitudeBottom = getCompatibleImage(ImageIO.read(new File("png/attitude_bottom.png")));
			attitudeIndicator = getCompatibleImage(ImageIO.read(new File("png/attitude_indicator.png")));
			pitchBacking = getCompatibleImage(ImageIO.read(new File("png/pitch_backing.png")));
			pitchIndicator = getCompatibleImage(ImageIO.read(new File("png/pitch_indicator.png")));
			gearBacking = getCompatibleImage(ImageIO.read(new File("png/gear_backing.png")));
			gearDown = getCompatibleImage(ImageIO.read(new File("png/gear_down.png")));
			gearLocked = getCompatibleImage(ImageIO.read(new File("png/gear_locked.png")));
			gearTransit = getCompatibleImage(ImageIO.read(new File("png/gear_transit.png")));
			nozzle = getCompatibleImage(ImageIO.read(new File("png/nozzle.png")));
			waypoint = getCompatibleImage(ImageIO.read(new File("png/waypoint.png")));
			speedBrakeWarning = getCompatibleImage(ImageIO.read(new File("png/speedbrake.png")));
			waypointBacking = getCompatibleImage(ImageIO.read(new File("png/waypoint_backing.png")));
			tankerIcon = getCompatibleImage(ImageIO.read(new File("png/tanker_icon.png")));
			arrow = getCompatibleImage(ImageIO.read(new File("png/Arrow.png")));
			hudBacking = getCompatibleImage(ImageIO.read(new File("png/hud_backing.png")));
		} catch(IOException ex) {
			Debug.print("Game.java: Game(): Error loading a PNG file");
					ex.printStackTrace();
		}

		AffineTransform tx;
		AffineTransformOp op;
		BufferedImage flipped;


		//Transform and cache arrow images
		arrows[0] = arrow;
		tx = AffineTransform.getScaleInstance(-1, 1);
		tx.translate(-arrow.getWidth(null), 0);
		flipped = new AffineTransformOp(tx, AffineTransformOp.TYPE_NEAREST_NEIGHBOR)
			.filter(arrow, null);
		arrows[1] = flipped;

		tx = AffineTransform.getRotateInstance(-Math.PI / 2
			, getImageMidpoint(arrow).getX()
			, getImageMidpoint(arrow).getY());
		flipped = new AffineTransformOp(tx, AffineTransformOp.TYPE_NEAREST_NEIGHBOR)
			.filter(arrow, null);
		arrows[2] = flipped;

		tx = AffineTransform.getRotateInstance(Math.PI / 2
			, getImageMidpoint(arrow).getX()
			, getImageMidpoint(arrow).getY());
		flipped = new AffineTransformOp(tx, AffineTransformOp.TYPE_NEAREST_NEIGHBOR)
			.filter(arrow, null);
		arrows[3] = flipped;



		//scale images
		for(int i = 0; i < playerImages.length; i++) {
			playerImages[i] = toBufferedImage(playerImages[i].getScaledInstance((int)
				(playerImages[i].getWidth() * PLAYER_SPRITE_SCALE)
				, (int) (playerImages[i].getHeight() * PLAYER_SPRITE_SCALE)
				, java.awt.Image.SCALE_SMOOTH));
		}

		elevator = toBufferedImage(elevator.getScaledInstance((int) (elevator.getWidth() * PLAYER_SPRITE_SCALE)
			, (int) (elevator.getHeight() * PLAYER_SPRITE_SCALE), java.awt.Image.SCALE_SMOOTH));
		skyTile = toBufferedImage(skyTile.getScaledInstance((int) (skyTile.getWidth() * BG_TILE_SCALE)
			, (int)(skyTile.getHeight() * BG_TILE_SCALE), java.awt.Image.SCALE_SMOOTH));

		for(int i = 0; i < terrainTiles.length; i++) {
			terrainTiles[i] = toBufferedImage(terrainTiles[i].getScaledInstance((int) (terrainTiles[i].getWidth() * MOUNTAIN_TILE_SCALE)
				, (int) (terrainTiles[i].getHeight() * MOUNTAIN_TILE_SCALE), java.awt.Image.SCALE_SMOOTH));
		}

		for(int i = 0; i < groundTiles.length; i++) {
			groundTiles[i] = toBufferedImage(groundTiles[i].getScaledInstance((int) (groundTiles[i].getWidth() * GROUND_TILE_SCALE)
				, (int) (groundTiles[i].getHeight() * GROUND_TILE_SCALE), java.awt.Image.SCALE_SMOOTH));
		}


		delayedEventListener = new TimerListener(this, TimerListener.Type.DELAYED_EVENT);
		eventTimer = new javax.swing.Timer(Pilot.AUTOPILOT_DELAY, delayedEventListener);
		timerListener = new TimerListener(this, TimerListener.Type.GAME_CLOCK);
		timer = new javax.swing.Timer((int)FRAME_DELAY, timerListener);
		rand = new Random();
		keyListener = new Keyboard(this);
		mouseListener = new Mouse(this);
		randWind = new Wind(0.0d, 0);
		environment = new Environment(ENV_WIDTH, ENV_HEIGHT, ENV_SCALE);
		controller = new Controller(environment);
		origin = new Point(0, Map.GROUND_ALTITUDE);
		terrain = new Terrain(new Point(Map.MOUNTAIN_1_START, Map.GROUND_ALTITUDE));
		terrain2 = new Terrain(new Point(Map.MOUNTAIN_2_START, Map.GROUND_ALTITUDE));
		trace = new ArrayList<Point>();
		// Refactor this to a level file
		waypoints = new ArrayList<Waypoint>();
		
		for(int i = 0; i < Map.WAYPOINTS.length; i++) {
			waypoints.add(new Waypoint(Map.WAYPOINTS[i], Map.WAYPOINT_LABELS[i]));
		}
		
		terrainColor = Color.GRAY;
		gameObjective = new Objective();
		//Debug.print("VECTOR X " + playerAircraft.getVector().getX());
		Point p = players.get(PLAYER1).isFlying().getLocation();
		/*create animation for main gear*/
		int numFrames = 49;
		int frameWidth = 41;
		int frameHeight = 46;
		int animSpeed = 3;
		int animDelay = 1;
		int frameY = 0;

		// Refactor this to a loadable animation file
		if(playerAircraft.getType() == Aircraft.Type.TANKER) {
			numFrames = 59;
			frameWidth = 60;
			frameHeight = 69;
			frameY = frameHeight;
		} else {
			numFrames = 20;
			frameWidth = 24;
			frameHeight = 20;
			frameY = 0;
		}

		Point k[] = new Point[numFrames];
		Point flipTransform = new Point(1.0, 1.0);

		for(int i = 0; i < k.length; i++) {
			k[i] = new Point(i * frameWidth, frameY);
		}

		Rectangle r = new Rectangle(frameWidth, frameHeight);

		if(playerAircraft.getType() == Aircraft.Type.TANKER)
			flipTransform.setX(-1.0);
		else
			flipTransform.setX(1.0);

		mainGearAnimation = new Animation(mainGearSprite, r, k, PLAYER_SPRITE_SCALE, flipTransform, animSpeed, animDelay);

		// Refactor this to a loadable animation file
		if(playerAircraft.getType() == Aircraft.Type.TANKER) {
			numFrames = 12;
		} else {
			numFrames = 7;
		}


		k = new Point[numFrames];

		for(int i = 0; i < k.length; i++) {
			k[i] = new Point(i * frameWidth, 0);
		}

		r = new Rectangle(frameWidth, frameHeight);

		mainWheelAnimation = new Animation(mainGearSprite, r, k, PLAYER_SPRITE_SCALE, flipTransform, animSpeed, animDelay);


		// Create animation for nose gear
// Refactor this to a loadable animation file
		if(playerAircraft.getType() == Aircraft.Type.TANKER) {
			numFrames = 50;
			frameWidth = 33;
			frameHeight = 49;
		} else {
			numFrames = 22;
			frameWidth = 15;
			frameHeight = 21;
		}


	//	numFrames = 40;
		//frameWidth = 25;
		//frameHeight = 33;

		k = new Point[numFrames];

		for(int i = 0; i < k.length; i++) {
			k[i] = new Point(i * frameWidth, frameHeight);
		}

		r = new Rectangle(frameWidth, frameHeight);
		noseGearAnimation = new Animation(noseGearSprite, r, k, PLAYER_SPRITE_SCALE, flipTransform, animSpeed, animDelay);

// Refactor this to a loadable animation file
		if(playerAircraft.getType() == Aircraft.Type.TANKER) {
			numFrames = 9;
		} else {
			numFrames = 7;
		}

		//create animation for nose wheel

		k = new Point[numFrames];

		for(int i = 0; i < k.length; i++) {
			k[i] = new Point(i * frameWidth, 0);
		}

		noseWheelAnimation = new Animation(noseGearSprite, r, k, PLAYER_SPRITE_SCALE, flipTransform, animSpeed, animDelay);


		flipTransform.setX(1.0);

		k = new Point[5];
		for(int i = 0; i < k.length; i++) {
			k[i] = new Point(i * 8, 0);
		}

		r = new Rectangle(8, 8);
		beaconAnimation = new Animation(beaconSprite, r, k, flipTransform, 1, 28);
		beaconAnimation.forward();
		beaconAnimation.repeat();
		beaconAnimation.start();

		p = new Point(p.getX(), p.getY());

		Rectangle cloudBounds = new Rectangle(0.0, 0.0);
		double side = 0;
		if(clouds.length > 0) {
				side = CLOUD_IMAGE_SCALE * Math.max(clouds[0].getWidth()
					, clouds[0].getHeight());
				cloudBounds = new Rectangle(side, side);
		}

		//Debug.print("cloud bounds are " + side);
		bg = new Background(this, playerAircraft.getVector(), p, viewport, terrainTiles, skyTile, groundTiles
			, map, cloudBounds, BG_SCALE, GROUND_SCALE, CLOUD_SCALE, MOUNTAIN_SCALE, Map.GROUND_ALTITUDE, TARGET_FPS);

		if(traceImage != null) {
			bg.setTraceImage(traceImage);
		}


		Graphics g;
		BufferedImage scaled;
		//scale all cloud images
		for(BufferedImage cloud: clouds) {
			if(cloud != null) {
				int w = (int) (cloud.getWidth() * CLOUD_IMAGE_SCALE);
				int h = (int) (cloud.getHeight() * CLOUD_IMAGE_SCALE);
				scaled = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
				g = scaled.createGraphics();
				g.drawImage(cloud, 0, 0, w, h, null);
				g.dispose();
				scaled = getCompatibleImage(scaled);
				//allImages.add(scaled);
				bg.addCloudImg(scaled);
			}
		}

		//smokeAnimation = new Animation[NUM_SMOKE_PUFFS];
		r = new Rectangle(20, 20);
		k = new Point[6];
		for(int i = 0; i < k.length; i++) {
			k[i] = new Point(i * 20, 0);
		}

		animDelay = 1;
		animSpeed = 3;
		smokeAnimation = new Animation(smoke, r, k, PLAYER_SPRITE_SCALE, flipTransform, animSpeed, animDelay);


		if(smokeAnimation != null) {
			bg.setSmokeAnimation(smokeAnimation);
		}


		//initialize wind environment
		controller.generateWinds();

		panel = new Panel();
		panel.setFontMode(Panel.FontMode.MENU);
		window = new Window();
		window.setResizable(false);
		window.add(panel);
        window.pack();

		panel.addKeyListener(keyListener);
		panel.addMouseMotionListener(mouseListener);
		panel.addMouseListener(mouseListener);
		panel.addMouseWheelListener(mouseListener);
		panel.requestFocusInWindow();
        window.setLocationRelativeTo(null);
        window.setVisible(true);

		if(Debug.verbose) {
			Debug.print("Game.java:Game(...): window created and set to visible");
			Debug.print("Game.java:Game(...): game started!");
		}

		startGame(gameMode);
	}

	private void startGame(GameMode gm) {
			gameRunning = true;
			timer.start();
			gameMode = gm;

			if(gameMode == GameMode.GAME) {
				eventTimer.start();
			} else {
			}
	}

	/*
	* Service method for toggling the extra UI layer that contains additional flight data.
	*/
	private void extraUI() {
		extraUI = extraUI ? false : true;
	}

	/**
	* Callback function for the keyboard listener. Receives a code representing the type of key
	* event as well as its parameters then handles the input.
	*/
	public void keyInput(KeyCode c) {
		switch(c) {
			case MENU:
				gameMode = GameMode.MENU;
				break;
			case AUTOPILOT:
				players.get(PLAYER1).toggleAutopilot();

				break;
			case GEAR:
				int dir = players.get(PLAYER1).isFlying().toggleGear();//toggle landing gear
				
				if(dir > 0) {
					mainGearAnimation.forward();
					mainGearAnimation.start();
					noseGearAnimation.forward();
					noseGearAnimation.start();
				} else {
					mainGearAnimation.backward();
					mainGearAnimation.start();
					noseGearAnimation.backward();
					noseGearAnimation.start();
				}

				break;
			case FLAPS_UP:
				players.get(PLAYER1).adjustFlaps(-1);
				break;
			case PAUSE:
				pause();
				break;
			case FLAPS_DN:
				players.get(PLAYER1).adjustFlaps(1);
				break;
			case SPD_BRAKES:
				players.get(PLAYER1).spdBrakes();
				//gameObjective.connectNozzles();
				break;
			case XTRA_UI:
				extraUI();
				break;
			case UP:
				players.get(PLAYER1).noseUp(1);
				break;
			case DOWN:
				players.get(PLAYER1).noseDown(1);
				break;
			case LEFT:
				players.get(PLAYER1).retThrottle();
				break;
			case RIGHT:
				players.get(PLAYER1).advThrottle();
				break;
			case TRANSFER:
				gameObjective.transferFuel();
				break;
			case TARGET:
				if(players.size() > 1)
					if(gameObjective.navIsAcquired()) {
						gameObjective.navLost();
					} else if(NAV_AQUIRE_RANGE >= Vector.create(playerAircraft.getLocation()
						, computerAircraft.getLocation()).getMagnitude()
						&& currentWaypoint == 2 || currentWaypoint == 3) {
						gameObjective.navAcquired();
					}
				break;
			case BRAKES:
				players.get(PLAYER1).brakes();
				//players.get(PLAYER1).toggleAutopilot();
				break;
			case EXTEND_BOOM:
				players.get(PLAYER2).isFlying().extendBoom();
				break;
			default:
				break;
		}
	}

	private Vector nozzleForce;

	public Vector getNozzleForce() {
		calculateNozzleForce();
		return nozzleForce;
	}

	public void calculateNozzleForce() {
		if(players.size() > 1) {
			Point p2Nozzle = players.get(PLAYER2).isFlying().getNozzle();
			Point p1Nozzle = players.get(PLAYER1).isFlying().getNose();
			p1Nozzle = Collision.getRotatedPoint(p1Nozzle
				, Math.toRadians(players.get(PLAYER1).isFlying().getAngleToHorizon()));
			p1Nozzle.translate(worldTransform.getX(), worldTransform.getY());
			Vector force = Vector.create(new Point(p2Nozzle), new Point(p1Nozzle));
			nozzleForce = new Vector(0, 0);
			
			//Debug.print("force.getMagnitude() " + force.getMagnitude());
			if(force.getMagnitude() < NOZZLE_ATTRACT_DISTANCE) {
				gameObjective.connectNozzles();
				force.scale(C_NOZZLE * (1.0 / force.getMagnitude())).flip();
				
				nozzleForce = force;
				//Debug.print("nozzleForce " + nozzleForce);
			} else {
				gameObjective.disconnectNozzles();
			}
		} else {
			Debug.print("player size less than = to 1");
		}
	}

	private Point clickLocation;

	/**
	* Callback function for the mouse listener. Receives a code representing the type of mouse
	* event as well as its parameters then handles the input.
	*/
	public void mouseInput(Code mic, int x, int y) {
		switch(mic) {
			case LEFT:
				clickLocation = mouseListener.getClick();
				break;
			case RIGHT:
				break;
			case LEFT_DRAG:
				if(gameMode == GameMode.GAME) {
					playerAircraft.deflectElevator(-y);
					//playerAircraft.adjustThrottle(x * THROTTLE_INPUT_SCALE);
				}
				break;
			case RIGHT_DRAG:
				if(gameMode == GameMode.GAME)
					playerAircraft.adjustThrottle(x * THROTTLE_INPUT_SCALE);
				break;
		}
	}

	/**
	* Pause or unpause the game.
	*/
	public void pause() {
		if(timer != null) {
			if(timer.isRunning()) {
				timer.stop();
			} else {
				timer.start();
			}
		}
	}

	/**
	* End the game and close the application.
	*/
	public void end(GameState gs) {
		switch(gs) {
			case CRASHED_TERRAIN:
				Debug.print("Game.java: you crashed into the terrain!");
			break;
			case CRASHED_GROUND:
				Debug.print("Game.java: you crashed into the ground!");
			break;
		}
		//pause();
		Debug.displayMessages();
		gameRunning = false;
		System.exit(0);
	}

	public void newFrame() {
		ready = false;
		frame.render();
	}

	//convert image to color model that is optimized for graphics device
	public BufferedImage getCompatibleImage(BufferedImage b) {
		GraphicsConfiguration gc = GraphicsEnvironment.getLocalGraphicsEnvironment()
			.getDefaultScreenDevice().getDefaultConfiguration();
		BufferedImage compat = gc.createCompatibleImage(b.getWidth(), b.getHeight()
			, Transparency.TRANSLUCENT);
		Graphics2D g2d = compat.createGraphics();
		g2d.drawImage(b, 0, 0, null);
		g2d.dispose();
		return compat;
	}

	/*
	* Activates AI autopilot upon receiving call from a timed event handler.
	*/
	public void triggerEvent() {
		if(!players.get(PLAYER2).autopilotEngaged())
			players.get(PLAYER2).toggleAutopilot();
	}

	/* Transform point from model to world foreground space */
	private Point pointToWorldSpace(Point p, double d) {
		p = Collision.getRotatedPoint(p, d);
		p.translate(worldTransform);
		p.scale(FG_SCALE);
		return p;
	}
	
	/* Transform point from model to world foreground space */
	private Point pointToPlayer2WorldSpace(Point p, double d) {
		p = Collision.getRotatedPoint(p, d);
		p.translate(players.get(PLAYER2).isFlying().getLocation());
		p.scale(FG_SCALE);
		return p;
	}
	
	//temp
	private Point terrainPointToWorldSpace(Point p, double d) {
		p = Collision.getRotatedPoint(p, d);
		//p.translate(worldTransform);
		p.scale(FG_SCALE);
		return p;
	}
	
	public Point getImageMidpoint(BufferedImage b) {
		return new Point(b.getWidth() / 2, b.getHeight() / 2);
	}


	/**
	* One game loop iteration.
	*/
	public class Frame {
		private double time;
		private Queue tasks;
		private Vector ground;
		
		/**
		* Create a new frame that contains game loop logic.
		*/
		Frame() {
			deltaTime = 0;
			tick = 0;
		}
		
		/*
		* Set the true wind vector for a pilot's aircraft to the environmental
		* wind vector found at its current location in world space.
		*/
		private void applyWind(Pilot p) {
			p.isFlying().setActingWind(environment.getActingWind());
		}

		/*private Queue clip(Queue tasks) {
			AffineTransform screen = new AffineTransform();
			DrawingTask d;
			Iterator i;
			Queue t = new Queue();
			Debug.print("Game.java:clip(Queue tasks): # tasks before clipping " + tasks.length());

			//double x = players.get(PLAYER1).isFlying().getLocation().getX() - PLAYER_SCREEN_X;
			//double y = players.get(PLAYER1).isFlying().getLocation().getY() - PLAYER_SCREEN_Y;
			
			if(tasks != null) {
				while(!tasks.isEmpty()) {
					
					d = (DrawingTask) tasks.pull();
					
					if(d != null) {
						if (d.getSpace() == Space.SCREEN) {
							t.push(d);
						} else if(Collision.sphere(players.get(PLAYER1).isFlying().getLocation()
							, new Point(d.getTransform().getTranslateX()
							, d.getTransform().getTranslateY()), d.getSphere())) {
							t.push(d);
						} else {
							//t.push(d);//TEMP
							//Debug.print("clipped at " + d.getLocation().getX()
								//+ " " + d.getLocation().getY());
						}
					} else {
						Debug.print("Game.java:Frame.java: clip(...): null drawing task");
					}
				}
			}

			Debug.print("Game.java:clip(Queue tasks): # tasks after clipping " + t.length());
			return t;
		}*/

		private void crash() {
			Debug.print("Game.java:crash(): Player aircraft crashed into terrain!");
			Debug.displayMessages();
			//pause();
			//end();
		}

		public boolean detectGroundCollision(Point p) {
			boolean b = false;
			ground = Vector.create(environment.getWidth(), 0);

			/*if(Collision.plane(p, origin, ground)) {

				if(Debug.verbose) {
					Debug.print("Game.java: Aircraft collided with the ground.");
				}

				b = true;
			}*/

			return Collision.plane(p, origin, ground);
		}

		public void render() {
			int x, y;
			double delta = System.nanoTime() - time;
			double playerATH = Math.toRadians(playerAircraft.getAngleToHorizon());
			double step = delta / NS_PER_S;
			Aircraft a, tempAircraft;
			DrawingTask currentTask;
			Point origin = new Point(0, Map.GROUND_ALTITUDE);
			Point p;
			tasks = new Queue();
			
			if(step > MAX_STEP)//for simulation stability when frametime exceeds fixed step size
				step = MAX_STEP;

			time = System.nanoTime();
			tick += delta;
			currentFrame++;
			
			
			if(tick >= NS_PER_S) {
				Debug.print("Game.java: frames per second " + currentFrame);
				tick = currentFrame = 0;
			}

			if(gameMode == GameMode.GAME) {
				Point player1Location = players.get(PLAYER1).isFlying().getLocation();
				DrawingTask d;
				Vector v;
				environment.updateWindEngine(delta, player1Location);
				mainGearAnimation.tick(currentFrame);
				noseGearAnimation.tick(currentFrame);
				beaconAnimation.tick(currentFrame);
				bg.updateAnimations(currentFrame);
				step /= PHYS_PER_FRAME;

				//Update physics
				for(int i = 0; i < players.size(); i++) {
					if(players.get(i).autopilotEngaged())
						players.get(i).doAI();

					for(int c = 0; c < PHYS_PER_FRAME; c++) {
						applyWind(players.get(i));
						players.get(i).isFlying().setNozzleForce(getNozzleForce());
						players.get(i).isFlying().updateModel(step);
					}

					players.get(i).setAGL(players.get(i).isFlying().getLocation().getY()
						- Map.GROUND_ALTITUDE);
				}
				
				
				boolean gLocked = playerAircraft.gearLocked();
				boolean playGPWS = players.get(PLAYER1).getGPWS()
					.testForGroundCollision((float) playerAircraft.getVector().getY()
					, (float) playerAircraft.getAGL(), gLocked);
			
				/*if(playGPWS)
					sound.enableSfxGroundWarning();
				else
					sound.resetGpws();

				//update sound engine
				if(players.get(PLAYER1).isFlying().isLanded())
					sound.enableSfxRolling();
				else
					sound.disableSfxRolling();*/

				/*sound.update((float) players.get(PLAYER1).isFlying().getTurbineSpeed()
					, Math.min((float) (players.get(PLAYER1).isFlying().getAirspeed()
					/ SFX_WIND_MAX_SPEED), 1.0f));*/

				
				Terrain currentTerrain;
				
				//if(playerAircraft.getLocation().getX() <= Map.MOUNTAIN_1_START + Map.MOUNTAIN_WIDTH)
					currentTerrain = terrain;
				//else {
				//	currentTerrain = terrain2;
					//Debug.print("is terrain2");
				//}
				
				//Debug.print(Vector.create(playerAircraft.getLocation(), computerAircraft.getLocation())
					//.getMagnitude() + "");
				if(Vector.create(playerAircraft.getLocation(), computerAircraft.getLocation())
					.getMagnitude() < TERRAIN_MODE_LIMIT) {
					Collision.TERRAIN_MODE = false;
					
					//Test for aircraft collision
					if(Collision.objectCollision(new Collidable[]{playerAircraft
						, computerAircraft})) {
						Debug.print("Game.java: Midair collision!");
						pause();
						//end(GameState.MIDAIR_COLLISION);
					}
				} else {//Test for collision with terrain
					Collision.TERRAIN_MODE = true;

					if(Collision.objectCollision(new Collidable[]{playerAircraft, currentTerrain})) {
						Debug.print("Game.java: Player aircraft collided with terrain!");
						pause();
						//end(GameState.CRASHED_TERRAIN);
					}
				}
				
				//display win message and pause game if condition met
				if(gameObjective.hasPlayerWon()) {
					//imageMidpoint = new Point(winMessage.getWidth() / 2
					//	, winMessage.getHeight() / 2);
					//tasks.push(new DrawingTask(winMessage, Space.SCREEN, 
					//	, , 0));
					Debug.print("Game.java: You win!");
					panel.setTasks(tasks);
					panel.repaint();
					pause();
				}

				//update screen transforms
				worldTransform = players.get(PLAYER1).isFlying().getLocation();
				screenTransform = new Point(worldTransform).scale(-FG_SCALE);
				double playerGroundOffset = playerAircraft.getMainGearHeight() * FG_SCALE;
				//double playerGroundOffset = 0;
				double playerScreenY = PLAYER_SCREEN_Y;
				bg.setCenter(worldTransform);
				bg.setDirection(playerAircraft.getVector().flip().normalize());
				terrain.setCenter(worldTransform);//refactor
				terrain2.setCenter(worldTransform);
				Point ground = bg.getGroundLayerCenter().translate(screenTransform);
				ground.setY(SCREEN_HEIGHT - playerScreenY - ground.getY());

				if(ground.getY() < GROUND_SCREEN_LIMIT)		
					//move player position by difference of ground position and ground limit
					playerScreenY -= (GROUND_SCREEN_LIMIT - ground.getY());

				screenTransform.translate(PLAYER_SCREEN_X, playerScreenY);
				computerScreenTransform = new Point(worldTransform).scale(-FG_SCALE);
				computerScreenTransform.translate(PLAYER_SCREEN_X, playerScreenY);
				bgTransform = new Point(worldTransform).scale(-BG_SCALE);
				bgTransform.translate(PLAYER_SCREEN_X, playerScreenY
					/ (FG_SCALE / BG_SCALE) - BG_SCREEN_OFFSET);//temp
				cloudTransform = new Point(worldTransform).scale(-CLOUD_SCALE);
				cloudTransform.translate(PLAYER_SCREEN_X, playerScreenY
					/ (FG_SCALE / CLOUD_SCALE));
				groundTransform = new Point(worldTransform).scale(-FG_SCALE);
				groundTransform.translate(PLAYER_SCREEN_X, playerScreenY
					/ (FG_SCALE / GROUND_SCALE) + playerGroundOffset);
				mountainTransform = new Point(worldTransform).scale(-MOUNTAIN_SCALE);
				mountainTransform.translate(PLAYER_SCREEN_X, playerScreenY
					/ (FG_SCALE / MOUNTAIN_SCALE));

				if(mainGearAnimation.isDone() && noseGearAnimation.isDone()) {
					playerAircraft.lockGear();
				}
				
				/* CREATE DRAWING TASKS */
				
				int count = 0;
				Iterator<DrawingTask> it;
				
				for(DrawingTask bgTask : bg.generateBackground()) {
					tasks.push(bgTask);
				}
				
				for(DrawingTask mountainTask : bg.generateMountains()) {
					tasks.push(mountainTask);
				}
					
				//Create tasks for terrain quads and vertices
				for(Quad q : currentTerrain.getQuads()) {
					q.scale(FG_SCALE);
					q.setLocation(Point.subtract(q.getOrigin(), worldTransform).scale(FG_SCALE));
					q.translate(worldTransform.getX() * FG_SCALE, worldTransform.getY() * FG_SCALE);
					tasks.push(new DrawingTask(q, screenTransform, terrainColor));
				}

				ArrayList<Point> vertices = currentTerrain.getVertices();
				p = new Point(0, 0);
				imageMidpoint = new Point(nearestPoint.getWidth() / 2
					, nearestPoint.getHeight() / 2);
				
				for(DrawingTask groundTask : bg.generateGround()) {
					tasks.push(groundTask);
				}

				for(Point terrainVertex : currentTerrain.getMesh()) {
					p = terrainVertex.copy();
					p.setLocation((p.getX() - worldTransform.getX()) * FG_SCALE
						, (p.getY()- worldTransform.getY()) * FG_SCALE);
					p.translate(worldTransform.getX() * FG_SCALE, worldTransform.getY() * FG_SCALE);
					tasks.push(new DrawingTask(meshPoint, p, screenTransform, imageMidpoint, 0));
				}
				
				//Create task for visual GPWS aid
				int endX = VERTICAL_RULER_X + QUAD_DIMENSION;
				int endY = SCREEN_HEIGHT + (int) worldTransform.getY();//rename
				Point start = new Point(VERTICAL_RULER_X, endY - GPWS_MAX);
				Point second = new Point(endX, endY	- GPWS_MAX);
				Point third = new Point(endX, endY);
				Point fourth = new Point(VERTICAL_RULER_X, endY);
				tasks.push(new DrawingTask(new Quad(start, second, third, fourth)
					, new Point(0, 0), Space.SCREEN, GROUND_WARNING_COLOR));

				//Create tasks for horizontal ruler marks
				Point p2Location = players.get(PLAYER2).isFlying().getLocation();
				int tankerIconX = (int) (waypoints.get(currentWaypoint).getLocation().getX()
					- worldTransform.getX() + PLAYER_SCREEN_X);
				int hzStep;
				String hzMarker;
				String s;
				BufferedImage indicator;
				Point indicatorLocation;

				if(gameObjective.navIsAcquired())
					tankerIconX = (int) (computerAircraft.getLocation().getX()
						- worldTransform.getX() + PLAYER_SCREEN_X);
						
				//Create visual task for horizontal position indicator
				if(tankerIconX > SCREEN_WIDTH) {
					indicator = arrows[0];
					indicatorLocation = new Point(SCREEN_WIDTH - HZ_ARROW_OFFSET_X
					, SCREEN_HEIGHT - ARROW_OFFSET_Y);
				}	else if(tankerIconX < 0) {
					indicator = arrows[1];
					indicatorLocation = new Point(HZ_ARROW_OFFSET_X, SCREEN_HEIGHT
						- ARROW_OFFSET_Y);
				} else {
					if(gameObjective.navIsAcquired()) {
						indicator = tankerIcon;
					} else {
						indicator = waypoint;
					}
					
					indicatorLocation = new Point(tankerIconX
						, SCREEN_HEIGHT - ARROW_OFFSET_Y);
				}

				tasks.push(new DrawingTask(indicator, indicatorLocation
					, new Point(0, 0), getImageMidpoint(indicator), 0));

				//create horizontal navigation marks
				for(int j = 0; j < NUM_RULES; j++) {
					hzMarker = "'";
					hzStep = j * HORIZONTAL_STEP;
					p = new Point(hzStep, HORIZONTAL_RULE_Y);
					p.translate(-worldTransform.getX() + PLAYER_SCREEN_X, 0);

					if(j % 2 == 0) {
						hzMarker = "|";
						s = "" + hzStep / METERS_PER_KM;
						tasks.push(new DrawingTask(s, p.getX() - POSITION_LABEL_OFFSET
							, p.getY() + POSITION_LABEL_OFFSET, Type.STRING));
					}

					currentTask = new DrawingTask(hzMarker, p.getX(), p.getY()
						, Type.STRING);
					tasks.push(currentTask);
				}
				
				int tankerIconY = (int) (waypoints.get(currentWaypoint).getLocation().getY()
					- worldTransform.getY() + PLAYER_SCREEN_Y);
					
				if(gameObjective.navIsAcquired())
					tankerIconY = (int) (computerAircraft.getLocation().getY()
					- worldTransform.getY() + PLAYER_SCREEN_Y);
				
				int vMarkY;

				//Create visual task for vertical position indicator
				if(tankerIconY > SCREEN_HEIGHT) {
					indicator = arrows[2];
					indicatorLocation = new Point(VT_ARROW_OFFSET_X, SCREEN_HEIGHT
						- ARROW_OFFSET_Y);
				}	else if(tankerIconY < 0) {
					indicator = arrows[3];
					indicatorLocation = new Point(VT_ARROW_OFFSET_X, ARROW_OFFSET_Y);
				} else {
					if(gameObjective.navIsAcquired()) {
						indicator = tankerIcon;
					} else {
						indicator = waypoint;
					}
					indicatorLocation = new Point(VT_ARROW_OFFSET_X, tankerIconY);
				}

				tasks.push(new DrawingTask(indicator, indicatorLocation
					, new Point(0, 0), getImageMidpoint(indicator), 0));

				//Create tasks for vertical ruler marks
				for(int j = 0; j < NUM_RULES; j++) {
					s = "-";
					vMarkY = (int) (j * VERTICAL_STEP);

					if(j % 2 == 0)
						s += vMarkY;

					p = new Point(0, vMarkY - worldTransform.getY());
					tasks.push(new DrawingTask(s, p.getX(), SCREEN_HEIGHT
						 - PLAYER_SCREEN_Y - p.getY(), Type.STRING));
				}

				//Create tasks for textual position indicators
				NumberFormat n = NumberFormat.getInstance();
				n.setMaximumFractionDigits(0);
				tasks.push(new DrawingTask("- -", HORIZONTAL_RULE_Y, SCREEN_HEIGHT
					- PLAYER_SCREEN_Y, DrawingTask.Type.STRING));
				tasks.push(new DrawingTask(n.format(playerAircraft.getAGL() + TERRAIN_ORIGIN.getY()) + "m"
					, ALTITUDE_LABEL_OFFSET, SCREEN_HEIGHT - PLAYER_SCREEN_Y, DrawingTask.Type.STRING));
					
				tasks.push(new DrawingTask("|", PLAYER_SCREEN_X, HORIZONTAL_RULE_Y
					, DrawingTask.Type.STRING));
				n.setMaximumFractionDigits(2);
				tasks.push(new DrawingTask(n.format(playerAircraft.getLocation().getX()
					/ METERS_PER_KM) + "km", PLAYER_SCREEN_X - POSITION_LABEL_OFFSET
					, HORIZONTAL_RULE_Y + POSITION_LABEL_OFFSET, null));
				
				/* Create tasks for player aircraft */
				count = 0;
				imageMidpoint = new Point(playerAircraft.getImageMidpoint())
					.scale(PLAYER_SPRITE_SCALE);
				tasks.push(new DrawingTask(playerImages[PLAYER1]
					, new Point(playerAircraft.getLocation()).scale(FG_SCALE)
					, screenTransform, imageMidpoint
					, -Math.toRadians(playerAircraft.getAngleToHorizon())));

				//Elevator animation
				double elevatorAnimationScale = 0.7;
				
				if(playerAircraft.getType() == Aircraft.Type.TANKER)
					elevatorAnimationScale = 0.4;
				
				double elevatorAnimationRotate = -playerATH + Math.PI
					+ Math.toRadians(playerAircraft.getElevator()) * elevatorAnimationScale;
				p = pointToWorldSpace(playerAircraft.getFin(), playerATH);
				imageMidpoint = new Point(elevator.getWidth() / 2
					, elevator.getHeight() / 2);
				tasks.push(new DrawingTask(elevator, p, screenTransform, imageMidpoint
					, elevatorAnimationRotate));
				
				//Gear animation
				if(!playerAircraft.gearStowed()) {
					boolean gearLocked = playerAircraft.gearLocked();
					double[] springPercents = new double[2];
					Animation[] gearAnimations = new Animation[2];
					BufferedImage frame;
					Point[] gearPoints = new Point[2];
					springPercents[0] = playerAircraft.getSpringPercent();
					springPercents[1] = playerAircraft.getNoseSpringPercent();
					gearPoints[0] = playerAircraft.getMainGear();
					gearPoints[1] = playerAircraft.getNoseGear();

					if(!gearLocked) {
						gearAnimations[0] = mainGearAnimation;
						gearAnimations[1] = noseGearAnimation;
					} else {
						gearAnimations[0] = mainWheelAnimation;
						gearAnimations[1] = noseWheelAnimation;
					}

					for(int i = 0; i < gearAnimations.length; i++) {
						p = pointToWorldSpace(gearPoints[i], playerATH);
						imageMidpoint = new Point(gearAnimations[i].getFrame().
							getWidth() / 2, 0);

						if(!gearLocked)
							frame = gearAnimations[i].getFrame();
						else
							frame = gearAnimations[i].getFrame(springPercents[i]);

						tasks.push(new DrawingTask(frame, p, screenTransform, imageMidpoint
							, -playerATH));
					}
				}
				
				/* Create tasks for computer aircraft */
				imageMidpoint = new Point(computerAircraft.getImageMidpoint())
					.scale(PLAYER_SPRITE_SCALE);
				tasks.push(new DrawingTask(playerImages[PLAYER2]
					, new Point(computerAircraft.getLocation()).scale(FG_SCALE)
					, screenTransform, imageMidpoint
					, -Math.toRadians(computerAircraft.getAngleToHorizon())));
					
				//Elevator animation
				/*elevatorAnimationScale = 0.4;			
				elevatorAnimationRotate = -players.get(PLAYER2).isFlying().getAngleToHorizon() + Math.PI
					+ Math.toRadians(computerAircraft.getElevator()) * elevatorAnimationScale;
				p = pointToPlayer2WorldSpace(computerAircraft.getFin()
					, -players.get(PLAYER2).isFlying().getAngleToHorizon());
				imageMidpoint = new Point(elevator.getWidth() / 2, elevator.getHeight() / 2);
				tasks.push(new DrawingTask(elevator, p, screenTransform, imageMidpoint
					, elevatorAnimationRotate));*/
					
				//Fuel boom links
				a = players.get(PLAYER2).isFlying();
				Point l1 = null, l2 = null;
				Point[] boomLinks = a.getBoomLinks();
				imageMidpoint = new Point(nozzle.getWidth() / 2, nozzle.getHeight() / 2);
				
				for(int i = 0; i < boomLinks.length; i++) {
					//draw a line between every pair of boom links
					if(i > 0) {
						l2 = boomLinks[i];
						tasks.push(new DrawingTask(Vector.create(l1, l2).scale(FG_SCALE), ""
							, new Point(l2).scale(FG_SCALE), screenTransform, boomLinkColor
							, Aircraft.BOOM_THICKNESS));
					}
					
					if(i == boomLinks.length - 1) {
						double dir = -Vector.create(l1, l2).getDirection();
						
						if(gameObjective.nozzlesConnected())
							dir = -Math.toRadians(playerAircraft.getAngleToHorizon());
						
						tasks.push(new DrawingTask(nozzle, new Point(l2).scale(FG_SCALE)
							, screenTransform, getImageMidpoint(nozzle), dir));
					}
					
					l1 = boomLinks[i];
				}

				/* Create tasks for collision points and connecting vectors */
				int pointCount = 0;
				Point tempPoint;
				imageMidpoint = new Point(nearestPoint.getWidth() / 2
					, nearestPoint.getHeight() / 2);
					
				if(extraUI) {
					count = 0;
					
					if(!Collision.TERRAIN_MODE) {
						for(Point collisionPoint : Collision.getNearestPoints()) {
							tempPoint = collisionPoint.copy();
							
							if(count > 2)//hack - player 1 & 2 points are in same list but use different world transforms
								tempPoint = pointToPlayer2WorldSpace(tempPoint, 0);
							else
								tempPoint = pointToWorldSpace(tempPoint, 0);

							tasks.push(new DrawingTask(nearestPoint, tempPoint, screenTransform
								, imageMidpoint, 0));
							count++;
						}
					} else {
						for(Point collisionPoint : Collision.getNearestPoints()) {
							//Debug.print("" + collisionPoint);
							tempPoint = pointToWorldSpace(collisionPoint.copy(), 0);
							tasks.push(new DrawingTask(nearestPoint, tempPoint, screenTransform
								, imageMidpoint, 0));
							count++;
						}
					}
				}


				count = 0;//vector starting points are offset by 1 from point list

				if(extraUI) {
					if(!Collision.TERRAIN_MODE) {
						for(Vector collisionVector : Collision.getNearestVectors()) {
							collisionVector.scale(FG_SCALE);
							
							if(count > 1) {//hack - 3rd vector starts from 4th point in global list
								tempPoint = pointToPlayer2WorldSpace(Collision.getNearestPoints()
									.get(count + 1).copy(), 0);
								tasks.push(new DrawingTask(collisionVector, "" + count, tempPoint
									, screenTransform, Color.GREEN, HUD_VECTOR_WIDTH));
							} else {
								tempPoint = pointToWorldSpace(Collision.getNearestPoints()
									.get(count).copy(), 0);
								tasks.push(new DrawingTask(collisionVector, "" + count, tempPoint
									, screenTransform, Color.GREEN, HUD_VECTOR_WIDTH));
							}
							
							count++;
						}
					} else {
						for(Vector collisionVector : Collision.getNearestVectors()) {
							collisionVector.scale(FG_SCALE);
							if(count > 1) {//hack - 3rd vector starts from 4th point in global list
								tempPoint = pointToWorldSpace(Collision.getNearestPoints()
									.get(count + 1).copy(), 0);
								tasks.push(new DrawingTask(collisionVector, "" + count, tempPoint
									, screenTransform, Color.GREEN, HUD_VECTOR_WIDTH));
							} else {
								tempPoint = pointToWorldSpace(Collision.getNearestPoints()
									.get(count).copy(), 0);
								tasks.push(new DrawingTask(collisionVector, "" + count, tempPoint
									, screenTransform, Color.GREEN, HUD_VECTOR_WIDTH));
							}
							count++;
						}
					}
				}

				count = 0;

				//Create tasks for aircraft mesh points
				if(extraUI) {
					imageMidpoint = new Point(meshPoint.getWidth() / 2, meshPoint.getHeight() / 2);

					for(int i = 0; i < players.size(); i++) {
						tempAircraft = players.get(i).isFlying();
						ArrayList<Point> meshPoints = tempAircraft.getMesh();

						for(Point mP : meshPoints) {		
							mP = Collision.getRotatedPoint(mP
								, Math.toRadians(tempAircraft.getAngleToHorizon())).scale(FG_SCALE);
							mP.translate(tempAircraft.getLocation().getX() * FG_SCALE
								, tempAircraft.getLocation().getY() * FG_SCALE);
							currentTask = new DrawingTask(meshPoint, mP, screenTransform
								, imageMidpoint, 0);
							tasks.push(currentTask);
						}
					}
				}

				//Create task for beacon animation
				p = pointToWorldSpace(playerAircraft.getTop(), playerATH);
				imageMidpoint = new Point(beaconAnimation.getFrame().getWidth() / 2
					, beaconAnimation.getFrame().getHeight() / 2);
				tasks.push(new DrawingTask(beaconAnimation.getFrame(), p
					, screenTransform, imageMidpoint, playerATH));

				//Get tasks for smoke trail animation
				for(DrawingTask traceTask : bg.generateTraces()) {
					tasks.push(traceTask);
				}
					
				//Get tasks for clouds
				it = bg.generateClouds().iterator();

				for(DrawingTask cloudTask : bg.generateClouds()) {
					tasks.push(cloudTask);
				}

				/* Create HUD tasks */
				AffineTransform at2;
				Point clipLoc;
				Queue subTasks;
				Rectangle clipBounds;
				Rectangle2D clip;
				int currentX = HUD_START_X;
				int currentY = HUD_START_Y;
				//Create the landing gear indicator
				BufferedImage gi;

				at2 = new AffineTransform();
				at2.translate(currentX + HUD_BACKING_OFFSET_X, HUD_START_Y + HUD_BACKING_OFFSET_Y);
				subTasks = new Queue();
				//subTasks.push(new DrawingTask(, Space.SCREEN, 0, 0, 0));
				clip = new Rectangle2D.Float(0, 0, hudBacking.getWidth(), hudBacking.getHeight());
				tasks.push(new DrawingTask(hudBacking, subTasks, at2, clip));
				
				
				if(!playerAircraft.gearStowed() && playerAircraft.gearLocked())
					gi = gearDown;
				else if(playerAircraft.gearLocked())
					gi = gearLocked;
				else
					gi = gearTransit;

				at2 = new AffineTransform();
				at2.translate(currentX, HUD_START_Y);
				subTasks = new Queue();
				subTasks.push(new DrawingTask(gearBacking, Space.SCREEN, 0, 0, 0));
				clip = new Rectangle2D.Float(0, 0, gi.getWidth(), gi.getHeight());
				tasks.push(new DrawingTask(gi, subTasks, at2, clip));
				currentX += gearBacking.getWidth();	
				//create task for the fuel gauge
				subTasks = new Queue();
				clip = new Rectangle2D.Float(0, 0, fuelGauge.getWidth(), fuelGauge.getHeight());
				at2 = new AffineTransform();
				at2.translate(currentX, HUD_START_Y);
				float fuelGuageIndicatorScale = 0.81f;
				int fuelGaugeIndicatorY = fuelGauge.getHeight() - FUEL_GAUGE_INDICATOR_Y
					- (int)(fuelGauge.getHeight() * fuelGuageIndicatorScale * (playerAircraft.getFuel() / playerAircraft.getMaxFuel()));
				
				if(gameObjective.nozzlesConnected()) {
					imageMidpoint = new Point(0, -fuelGaugeConnected.getHeight() / 2);
					int activeOffsetY = (int)imageMidpoint.getY();
					
					if(gameObjective.transferringFuel())
						activeOffsetY = 0;
					
					subTasks.push(new DrawingTask(fuelGaugeConnected, Space.SCREEN, 0, activeOffsetY, 0));
				}
				
				subTasks.push(new DrawingTask(fuelGaugeIndicator, Space.SCREEN
					, FUEL_GAUGE_INDICATOR_X, fuelGaugeIndicatorY, 0));
				subTasks.push(new DrawingTask(fuelGauge, Space.SCREEN, 0, 0, 0));
				tasks.push(new DrawingTask(fuelGaugeDisconnected, subTasks, at2, clip));
				currentX += fuelGaugeDisconnected.getWidth();
				//create task for the flaps gauge
				int flapIndicatorOffsetX = -3;
				double flapsIndicatorScale = 4.3;
				subTasks = new Queue();
				clip = new Rectangle2D.Float(0, 0, flapGauge.getWidth(), flapGauge.getHeight());
				at2 = new AffineTransform();
				at2.translate(currentX, currentY);
				subTasks.push(new DrawingTask(flapGaugeIndicator, Space.SCREEN
					, flapGauge.getWidth() / 2 + flapIndicatorOffsetX
					, HUD_FLAP_GAUGE_INDICATOR_Y
					, Math.toRadians(playerAircraft.flapSetting()) * flapsIndicatorScale
					, flapGaugeIndicator.getWidth() + flapIndicatorOffsetX
					, flapGaugeIndicator.getHeight()));
					
				if(playerAircraft.speedBrakesDeployed())
					subTasks.push(new DrawingTask(speedBrakeWarning, Space.SCREEN
					, SPEED_BRAKE_WARN_X, SPEED_BRAKE_WARN_Y, 0.0
					, flapGaugeIndicator.getWidth() + flapIndicatorOffsetX
					, flapGaugeIndicator.getHeight()));
				
				tasks.push(new DrawingTask(flapGauge, subTasks, at2, clip));
				currentX += flapGauge.getWidth();
				
				//create the weather radar
				NumberFormat nf = NumberFormat.getInstance();
				nf.setMaximumFractionDigits(0);
				at2 = new AffineTransform();
				at2.setToTranslation(currentX, currentY);
				clip = new Rectangle2D.Float(0, 0, windRadar.getWidth(), windRadar.getHeight());
				subTasks = new Queue();
				subTasks.push(new DrawingTask(windArrow, Space.SCREEN, WIND_VECTOR_X, WIND_VECTOR_Y
					, -playerAircraft.getActingWind().getDirection() - Math.PI / 2 //hack
					, windArrow.getWidth() / 2, windArrow.getHeight() / 2));
				double windSpeed = (playerAircraft.getActingWind().getMagnitude() / Map.MAX_WIND) * 100;
				subTasks.push(new DrawingTask(nf.format(windSpeed) + "%"
					, 5, 15, null));
				//subTasks.push(new DrawingTask("" + n.format(ath), 5, 15, null));
				tasks.push(new DrawingTask(windRadar, subTasks, at2, clip));
				currentX += windRadar.getWidth();
				
				//create tasks for the speed tape
				final int speedIndicatorOffset = speedTapeIndicator.getWidth() / 2;
				int speedIndicatorMax = SPEED_TAPE_X + speedTape.getWidth() - speedTapeBacking.getWidth() / 2;
				int speedIndicatorX = SPEED_TAPE_X + (int)(speedTapeBacking.getWidth() / 2 - speedIndicatorOffset);//- speedTapeBacking.getWidth() / 2);
				int speedIndicatorY = SCREEN_HEIGHT - SPEED_TAPE_Y - speedTapeIndicator.getHeight();
				subTasks = new Queue();
				at2 = new AffineTransform();
				at2.translate(currentX, HUD_START_Y);
				clipLoc = new Point(SPEED_TAPE_X, SCREEN_HEIGHT - SPEED_TAPE_Y);
				clipBounds = new Rectangle(speedTape.getWidth(), speedTape.getHeight());
				float speedTapeScale = -3.88f;
				int speedTapeOffset = 100;
				int speedTapeX = (int) (-playerAircraft.getAirspeed() * speedTapeScale) + speedTapeOffset;
				int speedTapeY = 4;
				subTasks.push(new DrawingTask(speedTape, Space.SCREEN, speedTapeX
					, speedTapeY, 0));
				int speedTapePaddingX = 3;
				int speedTapePaddingY = 3;
				int speedTapeClipWidth = speedTapeBacking.getWidth() - 2 * speedTapePaddingX;
				int speedTapeClipHeight = speedTapeBacking.getHeight() - 2 * speedTapePaddingY;
				clip = new Rectangle2D.Float(speedTapePaddingX, 0
					, speedTapeClipWidth, speedTapeClipHeight);
				final int speedIndicatorOffsetY = 1;
				final int speedIndicatorOffsetX = speedTapeBacking.getWidth() / 2 - speedIndicatorOffset;
				subTasks.push(new DrawingTask(speedTapeIndicator, Space.SCREEN, speedIndicatorOffsetX
					, speedIndicatorOffsetY, 0));
				NumberFormat airspeedFormat = NumberFormat.getInstance();
				airspeedFormat.setMaximumFractionDigits(0);
				//subTasks.push(new DrawingTask("" + airspeedFormat.format(-Collision.mpsToKnots(playerAircraft.getAirspeed())), 8
				//	, 15, null));
				speedIndicatorY = SCREEN_HEIGHT - SPEED_TAPE_Y;
				final int speedLabelPaddingY = 15;
				final int speedLabelPaddingX = 5;
				/*subTasks.push(new DrawingTask("" + n.format(
					Collision.mpsToKnots(a.getAirspeed()))
					, speedIndicatorOffsetX + speedLabelPaddingX
					, speedLabelPaddingY, null));*/
				tasks.push(new DrawingTask(speedTapeBacking, subTasks, at2, clip));
				currentX += speedTapeBacking.getWidth();
				
				//create tasks for the throttle
				at2 = new AffineTransform();
				at2.setToTranslation(currentX, currentY);
				clip = new Rectangle2D.Float(0, 0, throttle.getWidth(), throttle.getHeight());
				subTasks = new Queue();
				final int throttlePadding = 6;
				final double throttleRangeScale = 0.77;
				int throttleX = throttle.getWidth() / 2 - throttleIndicator.getWidth() / 2;
				double throttleY = throttlePadding + throttleRangeScale * (throttle.getHeight() - throttlePadding) * (1 - playerAircraft.getThrottleSetting() / 100.0);
				subTasks.push(new DrawingTask(throttleIndicator, Space.SCREEN, throttleX, throttleY, 0));
				Point turbineVectorOffset = new Point(4, 0);
				Point turbineVectorStart = new Point(turbineVectorOffset.getX(), throttle.getHeight() - throttlePadding);
				final int TURBINE_SPEED_INDICATOR_STROKE = 4;
				int turbineVectorLength =  throttle.getHeight() - 2 * throttlePadding;
				Vector turbineVector = new Vector(Math.PI * 0.5, turbineVectorLength * playerAircraft.getTurbineSpeed());
				subTasks.push(new DrawingTask(turbineVector, "", turbineVectorStart.getX()
					, turbineVectorStart.getY(), Space.SCREEN, Color.GREEN, TURBINE_SPEED_INDICATOR_STROKE));
				turbineVectorOffset = new Point(-6, 0);
				turbineVectorStart.setX(turbineVectorStart.getX() + THROTTLE_CONSOLE_WIDTH + turbineVectorOffset.getX());
				subTasks.push(new DrawingTask(turbineVector, "", turbineVectorStart.getX()
					, turbineVectorStart.getY(), Space.SCREEN, Color.GREEN, TURBINE_SPEED_INDICATOR_STROKE));
				tasks.push(new DrawingTask(throttle, subTasks, at2, clip));
				currentX += throttle.getWidth();
				
				//create attitude indicator
				//ath winds past 360, modulus to get desired value
				double ath = playerAircraft.getAngleToHorizon() % (2 * DEGREES_IN_HEMISPHERE);
				
				if(ath > DEGREES_IN_HEMISPHERE)
					ath = -DEGREES_IN_HEMISPHERE + (ath % DEGREES_IN_HEMISPHERE);
				else if(ath < -DEGREES_IN_HEMISPHERE)
					ath = DEGREES_IN_HEMISPHERE + (ath % DEGREES_IN_HEMISPHERE);

				int attitudeY = (int) (attitudeBacking.getHeight() / 2
					+ Math.signum(ath) * Math.min(Math.abs(ath), DEGREES_IN_HEMISPHERE / 2) * ATTITUDE_ATH_SCALE);
				subTasks = new Queue();
				subTasks.push(new DrawingTask(attitudeBottom, Space.SCREEN, 0, attitudeY, 0));
				attitudeY -= attitudeTop.getHeight();
				subTasks.push(new DrawingTask(attitudeTop, Space.SCREEN, 0, attitudeY, 0));
				attitudeY = attitudeBacking.getHeight() / 2 - attitudeIndicator.getHeight() / 2;
				int attitudeX = attitudeBacking.getWidth() / 2 - attitudeIndicator.getWidth() / 2;
				subTasks.push(new DrawingTask(attitudeIndicator, Space.SCREEN, attitudeX, attitudeY, 0));
				subTasks.push(new DrawingTask(attitudeBacking, Space.SCREEN, 0, 0, 0));
				//subTasks.push(new DrawingTask("" + n.format(ath), 5, 15, null));
				at2 = new AffineTransform();
				at2.translate(currentX, currentY);
				clip = new Rectangle2D.Float(0, 0, attitudeBacking.getWidth(), attitudeBacking.getHeight());
				tasks.push(new DrawingTask(attitudeBacking, subTasks, at2, clip));
				currentX += attitudeBacking.getWidth();
				
				//create pitch input indicator
				double elevAngle = playerAircraft.getElevator();
				int pitchX = pitchBacking.getWidth() / 2
					- pitchIndicator.getWidth() / 2;
				int pitchY = pitchBacking.getHeight() / 2
					- pitchIndicator.getHeight() / 2 + (int) elevAngle;
				subTasks = new Queue();
				subTasks.push(new DrawingTask(pitchIndicator, Space.SCREEN, pitchX, pitchY, 0));
				at2 = new AffineTransform();
				at2.translate(currentX, currentY);
				clip = new Rectangle2D.Float(0, 0
					, pitchBacking.getWidth(), pitchBacking.getHeight());
				tasks.push(new DrawingTask(pitchBacking, subTasks, at2, clip));
				currentX += pitchBacking.getWidth();			
				
				//create the glideslope indicator
				final int paddX = 0;
				final int paddY = 4;
				at2 = new AffineTransform();
				at2.setToTranslation(currentX, currentY);
				clip = new Rectangle2D.Float(paddX, paddY, glideslopeGauge.getWidth() - 2 * paddX
					, glideslopeGauge.getHeight() - 2 * paddY);
				subTasks = new Queue();
				imageMidpoint = new Point(glideslopeIndicator.getWidth() / 2, glideslopeIndicator.getHeight() / 2);
				double targetX = glideslopeGauge.getWidth() / 2 - imageMidpoint.getX();
				double targetY = glideslopeGauge.getHeight() / 2 - imageMidpoint.getY();// - Math.min(0, gsDev * GLIDESLOPE_INDICATOR_SCALE);
				double gsDev = glideslope.getDeviation(player1Location);
				final int indicatorMaxY = (int) targetY;
				int indicatorY = (int)(gsDev * GLIDESLOPE_INDICATOR_SCALE);
				indicatorY = (int)(Math.signum(indicatorY) * Math.min(indicatorMaxY, Math.abs(indicatorY)));
				subTasks.push(new DrawingTask(glideslopeIndicator, Space.SCREEN,(int) targetX, (int) targetY - indicatorY, 0));
				tasks.push(new DrawingTask(glideslopeGauge, subTasks, at2, clip));
				currentX += glideslopeGauge.getWidth();

				//update and draw waypoint indicators
				double playerDistance;
				double wpDist = 0.0;
				Waypoint w = null;
				v = null;

				if(currentWaypoint < waypoints.size())
					w = waypoints.get(currentWaypoint);

				//Debug.print("currentWaypoint " + currentWaypoint);
				

				if(gameObjective.navIsAcquired()) {
					//gameObjective.navAcquired();
					w = new Waypoint(new Point(players.get(PLAYER2).isFlying()
						.getLocation()), "Computer Aircraft");
					imageMidpoint = new Point(arrow.getWidth() / 2, arrow.getHeight());
					tasks.push(new DrawingTask(arrow, pointToWorldSpace(player1Location, 0)
						, screenTransform, imageMidpoint, Vector.create(playerAircraft.getLocation()
						, computerAircraft.getLocation()).getDirection()));
				} else {
					gameObjective.navLost();
				}

				if(w != null) {
					//advance current waypoint if crossing boundary
					if(!gameObjective.navIsAcquired()
						&& playerAircraft.getLocation().getX() > w.getLocation().getX()
						&& currentWaypoint < waypoints.size() - 1)
						currentWaypoint++;

					wpDist = w.getDistance(playerAircraft.getLocation()) / METERS_PER_NM;
					n = NumberFormat.getInstance();
					n.setMaximumFractionDigits(1);
					String wpDistLabel = n.format(wpDist);
					int wLabelY = 35;
					int wLabelX = 10;
					at2 = new AffineTransform();
					at2.setToTranslation(currentX, currentY);
					clip = new Rectangle2D.Float(0, 0, waypointBacking.getWidth()
						, waypointBacking.getHeight());
					subTasks = new Queue();
					subTasks.push(new DrawingTask(w.getLabel(), wLabelX, wLabelY, DrawingTask.Type.STRING));
					wLabelY = 55;
					wLabelX = 75;
					subTasks.push(new DrawingTask(wpDistLabel, wLabelX, wLabelY, DrawingTask.Type.STRING));
					tasks.push(new DrawingTask(waypointBacking, subTasks, at2, clip));
					//currentX += waypointBacking.getWidth();
				}
				
				/* Draw extra UI elements*/
				if(extraUI) {
					double scale = 0.001;
					double[] apStatus = players.get(PLAYER1).getAutopilotStatus();
					Point player1ScreenSpaceLocation =
						new Point(playerAircraft.getLocation()).scale(FG_SCALE);
					n = NumberFormat.getInstance();
					n.setMaximumFractionDigits(3);
					//Create tasks for autopilot parameter printouts
					count = 0;
					tasks.push(new DrawingTask("climb, tClimb: " + n.format(apStatus[0])
						+ ", " + n.format(apStatus[1]), AP_PRINTOUT_X
						, AP_PRINTOUT_Y + AP_PRINTOUT_MARGIN * count++, null));
					tasks.push(new DrawingTask("accel, tAccel: " + n.format(apStatus[2])
						+ ", " + n.format(apStatus[3]), AP_PRINTOUT_X
						, AP_PRINTOUT_Y + AP_PRINTOUT_MARGIN * count++, null));
					tasks.push(new DrawingTask("rotate, tRotate: " + n.format(apStatus[4])
						+ ", " + n.format(apStatus[5]), AP_PRINTOUT_X
						, AP_PRINTOUT_Y + AP_PRINTOUT_MARGIN * count++, null));

					if(players.get(PLAYER1).isFlying().getType() == Aircraft.Type.TANKER)
						scale = 0.0001;

					Vector[] linearForces = new Vector[]{playerAircraft.getThrust()
						, playerAircraft.getDrag(), playerAircraft.getLift()
						, playerAircraft.getGravity(), playerAircraft.getNormal()};
					String[] linearLabels = new String[]{"T"
						, "D", "L", "G", "N"};

					for(int i = 0; i < linearForces.length; i++) {
						linearForces[i].scale(scale);
					 	tasks.push(new DrawingTask(linearForces[i]
							, linearLabels[i], player1ScreenSpaceLocation
							, screenTransform, forceVectorColor));
					}

					scale = 0.0001;
					Vector[] angularForces = new Vector[]{
							playerAircraft.getAngularResistance()
						, playerAircraft.getElevatorForce(), playerAircraft.getTailForce()
						, playerAircraft.getNoseForce(), playerAircraft.getNoseNormal()
						, playerAircraft.getSkidNormal()};
					Point[] angularLocations = new Point[]{playerAircraft.getFin()
						,	playerAircraft.getFin(), playerAircraft.getTail()
						, playerAircraft.getNose(), playerAircraft.getNoseGear()
						, playerAircraft.getSkid()};
					String[] angularLabels = new String[]{"rR", "rE", "rT"
						, "rN", "rG", "rS"};

					for(int i = 0; i < angularForces.length; i++) {
						p = pointToWorldSpace(angularLocations[i], playerATH);
						tasks.push(new DrawingTask(angularForces[i].scale(scale)
							, angularLabels[i], p, screenTransform, Color.BLACK));
					}

					scale = 100.0;
					tasks.push(new DrawingTask(playerAircraft.getChordline().scale(scale)
						, "CHORD", player1ScreenSpaceLocation, screenTransform
						, directionVectorColor));
					scale = 2.0;
					tasks.push(new DrawingTask(playerAircraft.getVector().scale(scale)
						, "MOTION", player1ScreenSpaceLocation, screenTransform
						, directionVectorColor));
					imageMidpoint = getImageMidpoint(boomLink);

					for(int i = 0; i < players.size(); i++) {
						p = players.get(i).isFlying().getLocation();
						tasks.push(new DrawingTask(boomLink, p, screenTransform
							, imageMidpoint, 0));
					}
				}

				//Add trace smoke point to background
				p = pointToWorldSpace(playerAircraft.getExhaust(), playerATH);
				v = playerAircraft.getChordline().flip();
				v.setMagnitude(INITIAL_EXHAUST_SPEED);
				bg.addTrace(p, v);
				//Set motion vector and update background
				bg.setVector(playerAircraft.getVector());
				bg.update();
			} else { //render menu
				tasks.push(new DrawingTask((Shape) new java.awt.Rectangle(SCREEN_WIDTH
					, SCREEN_HEIGHT), new Color(130, 185, 220), Space.SCREEN, 0, 0));

				final int N_BUTTONS = 2;
				final int MENU_WIDTH = 400;
				final int MENU_HEIGHT = 240;
				final int TITLE_SIZE = 100;
				final int MENU_X = SCREEN_WIDTH / 2 - MENU_WIDTH / 2;
				final int MENU_Y = SCREEN_HEIGHT / 2 - MENU_HEIGHT / 2 + TITLE_SIZE;

				final int MENU_BORDER = 8;
				final int BUTTON_MARGIN = 8;
				final int BUTTON_WIDTH = MENU_WIDTH - 2 * MENU_BORDER - 2 * BUTTON_MARGIN;
				final int BUTTON_HEIGHT = 100;
				final int BUTTON_BORDER = 5;
				final int BUTTON_TEXT_X = 50;
				final int BUTTON_TEXT_Y = 65;
				final int TITLE_Y = 300;
				final int TITLE_X = 500;
				final Color BUTTON_COLOR = new Color(209, 212, 190);

				Color buttonHoverColor = new Color(244, 247, 225);
				Color buttonColor = BUTTON_COLOR;
				Color borderColor = new Color(100, 100, 100);
				Point pointerLocation = mouseListener.getLocation();

				final String BUTTON_1 = "PLAY";
				final String BUTTON_2 = "EXIT";
				final String TITLE = "WINDSHEAR";
				tasks.push(new DrawingTask(TITLE, TITLE_X, TITLE_Y, Type.STRING, Panel.FontMode.TITLE));

				int buttonX = MENU_X + MENU_BORDER + BUTTON_MARGIN;
				int buttonY = MENU_Y + MENU_BORDER + BUTTON_MARGIN;

				tasks.push(new DrawingTask((Shape)new java.awt.Rectangle(MENU_WIDTH, MENU_HEIGHT), borderColor, Space.SCREEN, MENU_X, MENU_Y));
				tasks.push(new DrawingTask((Shape)new java.awt.Rectangle(MENU_WIDTH - 2 * MENU_BORDER, MENU_HEIGHT - 2 * MENU_BORDER)
					, new Color(130, 185, 220), Space.SCREEN, MENU_X + MENU_BORDER, MENU_Y + MENU_BORDER));

				if(pointerLocation != null) {
					//Debug.print("pointer loc " + pointerLocation);
					if(pointerLocation.getX() > buttonX + BUTTON_BORDER && pointerLocation.getX() < buttonX + BUTTON_BORDER + BUTTON_WIDTH
						&& pointerLocation.getY() > buttonY + BUTTON_BORDER && pointerLocation.getY() < buttonY + BUTTON_BORDER + BUTTON_HEIGHT) {
						buttonColor = buttonHoverColor;
					}
				}

				if(clickLocation != null) {
					if(clickLocation.getX() > buttonX + BUTTON_BORDER && clickLocation.getX() < buttonX + BUTTON_BORDER + BUTTON_WIDTH
						&& clickLocation.getY() > buttonY + BUTTON_BORDER && clickLocation.getY() < buttonY + BUTTON_BORDER + BUTTON_HEIGHT) {
						clickLocation = null;
						startGame(GameMode.GAME);
					}
				}

				tasks.push(new DrawingTask((Shape)new java.awt.Rectangle(BUTTON_WIDTH, BUTTON_HEIGHT), borderColor, Space.SCREEN, buttonX, buttonY));
				tasks.push(new DrawingTask((Shape)new java.awt.Rectangle(BUTTON_WIDTH - 2 * BUTTON_BORDER, BUTTON_HEIGHT - 2 * BUTTON_BORDER)
					, buttonColor, Space.SCREEN, buttonX + BUTTON_BORDER, buttonY + BUTTON_BORDER));
				tasks.push(new DrawingTask(BUTTON_1, buttonX + BUTTON_TEXT_X, buttonY + BUTTON_TEXT_Y, Type.STRING, Panel.FontMode.MENU));

				buttonColor = BUTTON_COLOR;
				buttonY += BUTTON_MARGIN + BUTTON_HEIGHT;

				if(pointerLocation != null) {
					//Debug.print("pointer loc " + pointerLocation);

					if(pointerLocation.getX() > buttonX + BUTTON_BORDER
						&& pointerLocation.getX() < buttonX + BUTTON_BORDER + BUTTON_WIDTH
						&& pointerLocation.getY() > buttonY + BUTTON_BORDER
						&& pointerLocation.getY() < buttonY + BUTTON_BORDER + BUTTON_HEIGHT) {
						buttonColor = buttonHoverColor;
					}

					if(clickLocation != null) {
						if(clickLocation.getX() > buttonX + BUTTON_BORDER
							&& clickLocation.getX() < buttonX + BUTTON_BORDER + BUTTON_WIDTH
							&& clickLocation.getY() > buttonY + BUTTON_BORDER
							&& clickLocation.getY() < buttonY + BUTTON_BORDER
								+ BUTTON_HEIGHT) {
								clickLocation = null;
							end(GameState.EXIT_BUTTON);
						}
					}
				}

				tasks.push(new DrawingTask((Shape)new java.awt.Rectangle(BUTTON_WIDTH, BUTTON_HEIGHT), borderColor, Space.SCREEN, buttonX, buttonY));
				tasks.push(new DrawingTask((Shape)new java.awt.Rectangle(BUTTON_WIDTH - 2 * BUTTON_BORDER, BUTTON_HEIGHT - 2 * BUTTON_BORDER), buttonColor, Space.SCREEN, buttonX + BUTTON_BORDER, buttonY + BUTTON_BORDER));
				tasks.push(new DrawingTask(BUTTON_2, buttonX + BUTTON_TEXT_X, buttonY + BUTTON_TEXT_Y, Type.STRING, Panel.FontMode.MENU));
			}
			oldDelta = delta;
			//tasks = clip(tasks);
			panel.setTasks(tasks);
			panel.repaint();
			Debug.displayMessages();
			ready = true;
		}

		private Point getImageMidpoint(BufferedImage b) {
			return new Point(b.getWidth() / 2, b.getHeight() / 2);
		}
	}

	/**
	* Returns true if previous render loop is complete.
	*/
	public boolean ready() {
		return ready;
	}

	private class Objective {
		private static final int FUEL_RATE = 5000;
		private static final double WIN_AIRSPEED_LIMIT = 10;
		private boolean connected;
		private boolean transferring;
		private boolean navAcquired;
		
		Objective() {
			connected = false;
			transferring = false;
		}

		public boolean hasPlayerWon() {
			boolean b = false;
			Aircraft a1 = players.get(PLAYER1).isFlying();
			//Debug.print("a1.getAirspeed() " + a1.getAirspeed());
			if(a1.isLanded() && a1.fuelTankFull()
				&& Math.abs(a1.getAirspeed()) < WIN_AIRSPEED_LIMIT)
				b = true;
			
			return b;
		}


		public boolean transferringFuel() { return transferring;	}
		public boolean nozzlesConnected() { return connected; }
		public boolean navIsAcquired() { return navAcquired; }
		public void connectNozzles() { connected = true; }
		public void disconnectNozzles() { connected = false; }
		public void navAcquired() { navAcquired = true; }
		public void navLost() { navAcquired = false; }

		private void transferFuel() {
			Aircraft a1 = players.get(PLAYER1).isFlying();
			Aircraft a2 = players.get(PLAYER2).isFlying();
			if(connected) {
				if(!a1.fuelTankFull() && !a2.fuelTankEmpty()) {
					a1.transferFuel(FUEL_RATE);
					a2.transferFuel(-FUEL_RATE);
				}
				transferring = true;
			} else
				transferring = false;
		}

	}
}
