package src;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Scanner;

public class Map {

		public enum SegmentType {
			TUNDRA,
			RUNWAY_START,
			RUNWAY,
			RUNWAY_END
		}

		public class Segment {
			private Point start;
			private Point end;
			private SegmentType segmentType;

			public SegmentType getSegmentType() {
				return segmentType;
			}

			public Point getStart() {
				return start;
			}

			public Point getEnd() {
				return end;
			}

			public Segment(Point s, Point e, SegmentType st) {
				start = s;
				end = e;
				segmentType = st;
			}
		}

		public static final int MAP_START = 0;
		public static final int RUNWAY_LENGTH = 5000;
		public static final int RUNWAY_A_START = 300;
		public static final int RUNWAY_A_END = RUNWAY_A_START + RUNWAY_LENGTH;
		public static final int RUNWAY_B_START = 50000;
		public static final int RUNWAY_B_END = RUNWAY_B_START + RUNWAY_LENGTH;
		public static final int HEIGHT = 300;

		public static final int N_PARAMETERS = 33;
		public static final int N_PAIRS = 10;
		public static final int N_WAYPOINTS = 6;
		//public static final int VALUE_PAIRS_START = 28;

		public static int MAX_WIND;
		public static int CEILING;
		public static int ENVIRONMENT_WIDTH;
		public static int ENVIRONMENT_HEIGHT;
		public static int ENVIRONMENT_SCALE;
		public static int GROUND_ALTITUDE;
		public static int STEP_SIZE;
		public static int MOUNTAIN_WIDTH;
		public static int MOUNTAIN_HEIGHT;
		public static int MOUNTAIN_1_START;
		public static int MOUNTAIN_2_START;
		public static int RUNWAY_1_START;
		public static int RUNWAY_1_LENGTH;
		public static int RUNWAY_2_START;
		public static int RUNWAY_2_LENGTH;
		public static Point PLAYER_1_START;
		public static Point PLAYER_2_START;
		public static Point[] WAYPOINTS;
		public static String[] WAYPOINT_LABELS;
		public static String TITLE;
		public static String DESCRIPTION;
		public static Vector PLAYER_1_VECTOR;
		public static Vector PLAYER_2_VECTOR;		

		
		
		ArrayList<Segment> segments;
		
		int end;
		
		private void loadDefault() {
			segments.add(new Segment(new Point(MAP_START, HEIGHT)
				, new Point(RUNWAY_1_START, HEIGHT), SegmentType.TUNDRA));
			end = RUNWAY_1_START + RUNWAY_1_LENGTH;
			segments.add(new Segment(new Point(RUNWAY_1_START, HEIGHT)
				, new Point(end, HEIGHT), SegmentType.RUNWAY));
			segments.add(new Segment(new Point(end, HEIGHT)
				, new Point(RUNWAY_2_START, HEIGHT), SegmentType.TUNDRA));
			end = RUNWAY_2_START + RUNWAY_2_LENGTH;
			segments.add(new Segment(new Point(RUNWAY_2_START, HEIGHT)
				, new Point(end, HEIGHT), SegmentType.RUNWAY));
		}

		public SegmentType getSegmentType(Point p) {
			SegmentType st = SegmentType.TUNDRA;

			for(Segment s : segments) {
				if(p.getX() >= s.getStart().getX() && p.getX() <= s.getEnd().getX()) {
					st = s.getSegmentType();
					break;
				}
			}

			return st;
		}
		
		private void loadMap(String path) throws IllegalArgumentException {
			int c = 0;//counter
			int startPairs = N_PARAMETERS - N_PAIRS;
			double[][] pairs = new double[N_PAIRS][2];	
			File f = null;			
			Scanner scan = null;
			String line = null;
			String[] tokens = null;
			String[] parameters = new String[N_PARAMETERS];
			WAYPOINTS = new Point[N_WAYPOINTS];
			WAYPOINT_LABELS = new String[N_WAYPOINTS];
			
			if(path != null) {
				f = new File(path);
			} else {
				Debug.print("Map.java:loadMap(...): path cannot be null");
				throw new IllegalArgumentException("Invalid parameter, see log.");
			}

			try {
				scan = new Scanner(f);
			} catch(FileNotFoundException e) {
				Debug.print("Map.java:loadMap(...): error loading file " + path);
				e.printStackTrace();
			}

			if(scan != null) {
				while(scan.hasNextLine()) {
					line = scan.nextLine();
					//System.out.println("line " + line);
					tokens = line.split("#");
					
					for(int i = 0; i < tokens.length; i++) {
						tokens[i] = tokens[i].replaceAll("\\s+", "");
					}
					//System.out.println("tokens " + tokens[0] + " " + tokens[1]);
					
					parameters[c] = tokens[0];
					//System.out.println("c " + c + " " + parameters[c]);
					//parameters[c] = tokens[0].replaceAll("[^a-zA-Z]", "");
					if(c >= startPairs) {
						tokens = parameters[c].split(",");
						pairs[c - startPairs] = new double[] {Double.parseDouble(tokens[0])
							, Double.parseDouble(tokens[1])};
					}

					c++;
				}

				for(int i = 0; i < parameters.length; i++) {
					Debug.print(" " + parameters[i]);
				}

				
				if(c == N_PARAMETERS) {
					c = 0;
					TITLE = parameters[c++];
					DESCRIPTION = parameters[c++];
					MAX_WIND = Integer.parseInt(parameters[c++]);
					CEILING = Integer.parseInt(parameters[c++]);
					ENVIRONMENT_WIDTH = Integer.parseInt(parameters[c++]);
					ENVIRONMENT_HEIGHT = Integer.parseInt(parameters[c++]);
					ENVIRONMENT_SCALE = Integer.parseInt(parameters[c++]);
					GROUND_ALTITUDE = Integer.parseInt(parameters[c++]);
					STEP_SIZE = Integer.parseInt(parameters[c++]);
					MOUNTAIN_WIDTH = Integer.parseInt(parameters[c++]);
					MOUNTAIN_HEIGHT = Integer.parseInt(parameters[c++]);
					MOUNTAIN_1_START = Integer.parseInt(parameters[c++]);
					MOUNTAIN_2_START = Integer.parseInt(parameters[c++]);
					RUNWAY_1_START = Integer.parseInt(parameters[c++]);
					RUNWAY_1_LENGTH = Integer.parseInt(parameters[c++]);
					RUNWAY_2_START = Integer.parseInt(parameters[c++]);
					RUNWAY_2_LENGTH = Integer.parseInt(parameters[c++]);
					
					for(int i = 0; i < N_WAYPOINTS; i++) {
						WAYPOINT_LABELS[i] = parameters[c++];
					}
					
					c = 0;
					PLAYER_1_VECTOR = new Vector(pairs[c][0], pairs[c++][1]);
					PLAYER_2_VECTOR = new Vector(pairs[c][0], pairs[c++][1]);
				//	PLAYER_1_SPEED = Double.parseDouble(parameters[c++]);
					//PLAYER_2_SPEED = Double.parseDouble(parameters[c++]);
					
					PLAYER_1_START = new Point(pairs[c][0], pairs[c++][1]);
					PLAYER_2_START = new Point(pairs[c][0], pairs[c++][1]);
					
					for(int i = 0; i < N_WAYPOINTS; i++) {
						WAYPOINTS[i] = new Point(pairs[c][0], pairs[c++][1]);
					}
				} else {
					Debug.print("Map.java:loadMap(...): not all parameters were read");
				}
			}
		}
	
		public Map(String path) {
			segments = new ArrayList<Segment>();
			loadMap(path);
			loadDefault();
		}
}
//EOF
