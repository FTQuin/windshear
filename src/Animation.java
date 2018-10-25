package src;

import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;

/**
* Contains a sprite sheet and a keyframe map.
*/
class Animation {

	/* Forward - play normally, Backward - play in reverse */
	enum Direction {
		FORWARD,
		BACKWARD
	}

	boolean running;
	boolean repeat;
	int frame;
	int speed;
	int delay;
	int timer;
	double scale;
	BufferedImage sprite;
	BufferedImage frames[];
	Direction direction;
	Point[] keyframes;
	Point flip;
	Rectangle frameSize;

	public int mapPercentToKeyframe(double p) {
		if(p < 0.0) {
			p = 0.0;
			
			if(Debug.verbose)
				Debug.print("Animation.java:mapPercentToKeyframe(): p must be >= 0.0");
		} else if (p > 1.0) {
			p = 1.0;
			
			if(Debug.verbose)
				Debug.print("Animation.java:mapPercentToKeyframe(): p must be <= 1.0");
		}

		return (int) Math.floor(keyframes.length * p);
	}

	/* Make a copy of an Animation */
	public Animation(Animation a) {
		this(a.getSprite(), a.getFrameSize(), a.getKeyframes()
			, a.getFlip(), a.getSpeed(), a.getDelay());
	}

	Animation(BufferedImage b, Rectangle r, Point[] k, Point p, int s, int d) {
		this(b, r, k, 1.0, p, s, d);
	}

	private BufferedImage getSprite() {
		return sprite;
	}

	public Rectangle getFrameSize() {
		return frameSize;
	}

	private Point[] getKeyframes() {
		return keyframes;
	}

	public Point getFlip() {
		return flip;
	}

	private double getScale() {
		return scale;
	}

	/*private int getSpeed() {
		return speed;
	}*/

	private int getDelay() {
		return delay;
	}

	public int getFrameIndex() {
			return frame;
	}

	/*
	* @
	* @param d - delay start of animation by ms
	*/
	Animation(BufferedImage b, Rectangle r, Point[] k, double scale, Point p, int s, int d) {
		if(b != null) {
			sprite = b;
		} else {
			Debug.print("Animation.java:Animation(BufferedImage b...): b cannot be null");
			throw new IllegalArgumentException("Animation.java: invalid initializer, see log");
		}

		if(k.length > 0) {
			keyframes = k;
		} else {
			Debug.print("Animation.java:Animation(Rectangle[] k...): k must have 1 or more elements");
			throw new IllegalArgumentException("Animation.java: invalid initializer, see log");
		}

		if(s > 0) {
			speed = s;
		} else {
			Debug.print("Animation.java:Animation(int s...): s must be greater than 0");
			speed = 1;
		}

		if(d > 0) {
			delay = d;
		} else {
			Debug.print("Animation.java:Animation(int d...): d must be greater than 0");
			delay = 1;
		}

		direction = Direction.FORWARD;
		flip = p;
		frame = 0;
		frames = new BufferedImage[keyframes.length];
		frameSize = r;
		running = false;
		repeat = false;
		this.scale = scale;
		timer = 0;
		int count = 0;

		for(Point keyFrame : keyframes) {
			frames[count] = sprite.getSubimage((int) keyFrame.getX(), (int) keyFrame.getY()
			, (int) frameSize.getWidth(), (int) frameSize.getHeight());
			frames[count] = Game.toBufferedImage(frames[count].getScaledInstance((int) (frames[count].getWidth() * scale)
			, (int) (frames[count].getHeight() * scale), java.awt.Image.SCALE_SMOOTH));

			if(p.getX() < 0) {
				AffineTransform tx = AffineTransform.getScaleInstance(-1, 1);
				tx.translate(-frames[count].getWidth(), 0);
				AffineTransformOp op = new AffineTransformOp(tx, AffineTransformOp.TYPE_NEAREST_NEIGHBOR);
				frames[count] = op.filter(frames[count], null);
			}

			count++;
		}
	}

	/**
	* Get the current key frame.
	*/
	public BufferedImage getFrame() {
		return frames[frame];
	}

	/**
	* Get the frame that is at approximately "p" percent of the animation.
	*/
	public BufferedImage getFrame(double p) {
		int frame = mapPercentToKeyframe(p);

		if(frame < 0)
			frame = 0;
		else if(frame >= frames.length)
			frame = frames.length - 1;

		return frames[frame];
	}

	public void repeat() {
		repeat = repeat ? false : true;
	}

	public void forward() {
		direction = Direction.FORWARD;
	}

	public void backward() {
		direction = Direction.BACKWARD;
	}

	public void reverse() {
		if(direction == Direction.FORWARD)
			direction = Direction.BACKWARD;
		else
			direction = Direction.FORWARD;
	}

	public void tick(int f) {
		if(!running) {
			timer++;
		}

		if(timer % delay == 0) {
			running = true;
			timer = 0;
		}

		if(f % speed == 0) {
			if(running) {
				if(direction == Direction.FORWARD)
					frame++;
				else
					frame--;
			}

			if(frame < 0) {
				frame = 0;
				stop();
			} else if(frame > keyframes.length - 1) {
				if(repeat) {
					running = false;
					frame = 0;
				} else {
					frame = keyframes.length - 1;
					stop();
				}
			}
		}
	}

	public void start() {
		running = true;

		if(direction == Direction.FORWARD)
			frame = 0;
		else
			frame = keyframes.length - 1;
	}

	public void stop() {
		running = false;
	}

	public boolean isDone() {
		return !running;
	}

	public int getSpeed() {
		return speed;
	}
}
//EOF
