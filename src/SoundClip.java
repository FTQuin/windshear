package src;
import javax.sound.sampled.Clip;
import java.util.ArrayList;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.Line;
//import javax.sound.sampled.Line.Info;
import javax.sound.sampled.Control;
//import javax.sound.sampled.Control.Type;
import java.lang.Class;
import javax.sound.sampled.DataLine;
//import javax.sound.sampled.FloatControl.Type;
import javax.sound.sampled.FloatControl;
import java.io.File;

public class SoundClip {
	private ArrayList<Clip> clips;
	private Game parent;
	private Clip clip;
	private static final float SAMPLE_RATE = 44000.0f;
	private static final int SAMPLE_SIZE = 16;
	private static final int CHANNELS = 2;
	private static final int FRAME_SIZE = 2;
	private static final float FRAME_RATE = 2;
	
	public enum PLAY_MODE {
		ONCE,
		LOOP
	}
	
	SoundClip(Game g) {

		parent = g;
	}
	
	
	
	public void playSoundOnLoop(String path) {
		if(path != null) {
			AudioInputStream inStream = null;
			Clip c = null;
			try {
				c = AudioSystem.getClip();
				inStream = AudioSystem.getAudioInputStream(new java.io.File(path));
				//clips.add(c);
				c.open(inStream);
				//System.out.println("c.getFrameLength() " + c.getFrameLength());
				c.setLoopPoints(0, -1);//-1 end of clip
				c.loop(Clip.LOOP_CONTINUOUSLY);
			} catch(Exception e) {
				e.printStackTrace();
			}
			
			if(c != null)
				c.start();
		}
	}
	
	public void playSound(String path) {
		if(path != null) {
			AudioFormat af;
			File f;
			Line.Info lineInfo;
			FloatControl co;
			
			
			//float sampleRate;
			//float frameRate;
			//int frameSize;
			//int channels;
			//int sampleSizeInBits;
			//boolean bigEndian;
			
			AudioInputStream inStream = null;
			Clip c = null;
			try {
				f = new File(path);
				inStream = AudioSystem.getAudioInputStream(f);
				af = inStream.getFormat();
				lineInfo = new DataLine.Info(Class.forName("javax.sound.sampled.Clip"),  af);
				c = (Clip) AudioSystem.getLine(lineInfo);
				c.open(inStream);
				co = (FloatControl) c.getControl(FloatControl.Type.MASTER_GAIN);
				
				System.out.println("volume min and max " + co.getMinimum() + " " + co.getMaximum());
				co.setValue(-20.0f);
				
				
				//c = AudioSystem.getClip();
				
				//clips.add(c);
				
				c.setLoopPoints(0, -1);//-1 end of clip
				c.loop(Clip.LOOP_CONTINUOUSLY);
			} catch(Exception e) {
				e.printStackTrace();
				System.out.println("EXCEPTION");
			}
			
			if(c != null)
				c.start();
		}
	}
	
}
