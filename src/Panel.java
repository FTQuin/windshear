package src;//checked!

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import javax.swing.JPanel;
import src.DrawingTask.Type;
import src.DrawingTask.Space;
import src.Game.GameMode;

/**
 * The desktop graphics surface on which all drawing will be done.
 */
class Panel extends JPanel {
	public enum FontMode {
		HUD,
		MENU,
		TITLE
	}

	private static final int S_OFFSET = 5;//offset for vector labels
	private static final Color BG_COLOR = Color.WHITE;
	private static final Color FONT_COLOR = Color.BLACK;
	private static final Color BOOM_COLOR = new Color(116, 116, 116);
	private static final Color LINE_COLOR = new Color(230, 48, 48);
	private static final Color SHAPE_COLOR = Color.BLACK;
	private static final Font HUD_FONT = new Font(Font.SANS_SERIF, Font.PLAIN, 14);
	private static final Font MENU_FONT = new Font(Font.SANS_SERIF, Font.BOLD, 48);
	private static final Font TITLE_FONT = new Font(Font.SANS_SERIF, Font.BOLD, 82);

	private Color lineColor;
  private Font font;
  private FontMode fontMode;
  private Queue tasks;

	/**
	* Create a new Panel.
	*/
	Panel() {
		fontMode = FontMode.HUD;
		setBackground(BG_COLOR);
		tasks = new Queue();

		if(Debug.verbose) {
			Debug.print("Panel.java:Panel(): Panel created");
		}
  }

	@Override
	public Dimension getPreferredSize() {
		return new Dimension(Game.SCREEN_WIDTH, Game.SCREEN_HEIGHT);
	}

	public void setFontMode(FontMode fm) {
		if(fm == FontMode.MENU)
			font = MENU_FONT;
		else if(fm == FontMode.TITLE)
			font = TITLE_FONT;
		else
			font = HUD_FONT;
	}

	public void setTasks(Queue q) {
		if(q != null)
			tasks = q;
		else
			Debug.print("Panel.java:setTasks(...): q cannot be null");
	}

	/**
	* Add a DrawingTask "d" to be drawn on the next frame.
	*/
  public void add(DrawingTask d){
		if(d != null)
			tasks.push(d);
		else
			Debug.print("Panel.java:add(...): d cannot be null");
  }

	public void setVectorColor(Color c) {
		if(c != null)
			lineColor = c;
		else
			Debug.print("Panel.java:setVectorColor(): c cannot be null");
	}

	/**
	* Iterates through each DrawingTask in the queue and paints it on the screen
	* represented by the Graphics object "g".
	*/
	@Override
  public void paintComponent(Graphics g) {
		super.paintComponent(g);
		int x1, y1, x2, y2;
		DrawingTask d;
		DrawingTask.Type t;
		Graphics2D g2d = (Graphics2D)g;
		String s;
		g2d.setFont(font);

		while(!tasks.isEmpty()) {
			d = (DrawingTask)tasks.pull();
			t = d.getType();

			switch(t) {
				case POLYGON:
					int[] vX = d.getQuad().getVerticesX();
					int[] vY = d.getQuad().getVerticesY();

					for(int i = 0; i < vX.length; i++) {
						vX[i] += d.getTransform().getTranslateX();
						vY[i] += d.getTransform().getTranslateY();

						if(d.getSpace() == DrawingTask.Space.WORLD)
							vY[i] = Game.SCREEN_HEIGHT - vY[i];
					}

					g2d.setColor(d.getColor());
					g2d.fillPolygon(vX, vY, Quad.NUM_VERTICES);
					break;
				case IMAGE:
					BufferedImage temp = d.getImage();
					Queue subTasks = d.getSubTasks();
					g2d.setClip(0, 0, (int) getPreferredSize().getWidth()
						, (int) getPreferredSize().getHeight());

					if(subTasks != null && !subTasks.isEmpty()) {
						temp = new BufferedImage(d.getImage().getWidth()
							, d.getImage().getHeight(), BufferedImage.TYPE_INT_ARGB);
						Graphics2D g2dSub = (Graphics2D) temp.getGraphics();
						AffineTransform a = new AffineTransform();
						g2dSub.setClip(0, 0, (int) getPreferredSize().getWidth()
							, (int) getPreferredSize().getHeight());
						g2dSub.drawImage(d.getImage(), a, null);
						g2dSub = (Graphics2D) temp.getGraphics();

						while(!subTasks.isEmpty()) {
							DrawingTask sub = (DrawingTask) subTasks.pull();
							g2dSub.clip(d.getClipBounds());
							t = sub.getType();

							if(t == Type.IMAGE) {
								g2dSub.drawImage(sub.getImage(), sub.getTransform(), null);
							} else if(t == Type.STRING && sub.getString() != null) {
								setFontMode(FontMode.HUD);
								g2dSub.setFont(font);
								g2dSub.setColor(Color.BLACK);
								g2dSub.drawString(sub.getString(), (int) sub.getX(), (int) sub.getY());
							} else if(t == Type.VECTOR) {
								g2dSub.setColor(sub.getColor());
								Vector v = sub.getVector();
								x1 = (int) sub.getX();
								y1 = (int) sub.getY();
								x2 = x1 + (int) v.getX();
								y2 = y1 - (int) v.getY();
								g2dSub.setStroke(new BasicStroke(sub.getStroke()));
								g2dSub.drawLine(x1, y1, x2, y2);
								g2dSub.drawString(sub.getString(), x2 + S_OFFSET, y2);
							}
						}

						subTasks.clear();
					}

					//move this to a proper clipping function
					int yPosition = (int) d.getTransform().getTranslateY();
					int xPosition = (int) d.getTransform().getTranslateX();

					if(yPosition < 2 * Game.SCREEN_HEIGHT
						&& yPosition > -3 * Game.SCREEN_HEIGHT
						&& xPosition < 2 * Game.SCREEN_WIDTH
						&& xPosition > -3 * Game.SCREEN_WIDTH)
						g2d.drawImage(temp, d.getTransform(), null);

					break;
				case STRING:
					setFontMode(d.getFontMode());
					g2d.setFont(font);
					s = d.getString();
					g2d.setColor(FONT_COLOR);
					g2d.drawString(s, (int)d.getX(), (int)d.getY());
					break;
				case VECTOR:
					Vector v = d.getVector();
					String label = d.getString();
					g2d.setColor(d.getColor());
					g2d.setStroke(new BasicStroke(d.getStroke()));
					//temp variables for readability
					x1 = (int) d.getTransform().getTranslateX();
					y1 = (int) d.getTransform().getTranslateY();
					x2 = x1 + (int) v.getX();
					y2 = y1 - (int) v.getY();
					//Debug.print("drawing vector " + label + " at " + x1 + " " + y1);
					g2d.drawLine(x1, y1, x2, y2);

					if(label != null) {
						g2d.drawString(label, x2 + S_OFFSET, y2);
					}
					/*if(d.getSpace() == Space.WORLD) {


						g2d.setStroke(new BasicStroke(Aircraft.BOOM_THICKNESS));
						g2d.drawLine(x1, y1, x2, y2);
					} else {
						g2d.setColor(d.getColor());
						Vector v = d.getVector();
						x1 = (int) d.getX();
						y1 = (int) d.getY();
						x2 = x1 + (int) v.getX();
						y2 = y1 - (int) v.getY();
						g2d.setStroke(new BasicStroke(d.getStroke()));
						g2d.drawLine(x1, y1, x2, y2);
						g2d.drawString(d.getString(), x2 + S_OFFSET, y2);
					}*/

					break;
				case SHAPE:
					AffineTransform saveAT = g2d.getTransform();//save previous transform
					g2d.setColor(d.getColor());
					g2d.translate(d.getLocation().getX(), d.getLocation().getY());
					g2d.fill(d.getShape());
					g2d.setTransform(saveAT);//restore transform after drawing shape
					break;
			}
		}
  }//end paintComponent
}
//EOF
