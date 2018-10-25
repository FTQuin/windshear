package src;

import java.io.File;
import java.io.IOException;
import java.io.FileNotFoundException;
import java.io.BufferedInputStream;
import java.lang.Class;
import java.util.Scanner;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.Control;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.FloatControl;
import javax.sound.sampled.Line;
import javax.sound.sampled.SourceDataLine;

public class Sound {

	public enum PlayMode {
		ONCE,
		LOOP
	}

	public enum EnvironmentState {
		WIND,
		ROLLING
	}

	public enum GpwsState {
		IDLE,
		GROUND,
		WINDSHEAR
	}

	public static final int P1_ENG_NUM_FRAGMENTS = 3;
	public static final int P1_ENV_NUM_FRAGMENTS = 2;
	public static final int P1_GPWS_NUM_FRAGMENTS = 1;
	public static final int SFX_ROLL_LINE = 1;
	public static final int GPWS_GROUND_LINE = 0;
	public static final int BUFFER_SIZE = 2000;//need to calc for each input file
	public static final String P1_ENG_PATH = "./crj_engine.txt";
	public static final String P1_ENV_PATH = "./crj_env.txt";
	public static final String P1_GPWS_PATH = "./crj_gpws.txt";
	private EnvironmentState p1EnvState;
	private GpwsState gpwsState;
	private MixedSound player1Eng;
	private MixedSound player1Env;
	private MixedSound gpws;


	public void enableSfxRolling() {
		p1EnvState = EnvironmentState.ROLLING;
	}

	public void disableSfxRolling() {
		p1EnvState = EnvironmentState.WIND;
	}

	public void enableSfxGroundWarning() {
		gpwsState = GpwsState.GROUND;
	}

	public void resetGpws() {
		gpwsState = GpwsState.IDLE;
	}

	public Sound() {
		Debug.print("creating sound");
		player1Eng = loadMixedSound(P1_ENG_PATH, P1_ENG_NUM_FRAGMENTS);
		player1Env = loadMixedSound(P1_ENV_PATH, P1_ENV_NUM_FRAGMENTS);
		gpws = loadMixedSound(P1_GPWS_PATH, P1_GPWS_NUM_FRAGMENTS);
		gpws.setSliderPosition(100);
		p1EnvState = EnvironmentState.WIND;
		gpwsState = GpwsState.IDLE;
	}

	/* p is turbine % and s is speed % */
	public void update(float p, float s) {
		player1Eng.update(p * 100);
		player1Env.update(s * 100);
		gpws.update(100);

		//if(player1Env == null)
		//	System.out.println("fuuuu");
		
		if(p1EnvState == EnvironmentState.ROLLING)
			player1Env.enableLine(SFX_ROLL_LINE);
		else
			player1Env.disableLine(SFX_ROLL_LINE);

		if(gpwsState == GpwsState.GROUND)
			gpws.enableLine(GPWS_GROUND_LINE);
		else
			gpws.disableLine(GPWS_GROUND_LINE);

		gpws.writeToBuffer();
		player1Env.writeToBuffer();
		player1Eng.writeToBuffer();
		
	}

	public class MixedSound {
		public static final int MIN = 0;
		public static final int MAX = 100;
		public static final int MAX_SIMULTANEOUS = 4;

		SoundFragment fragments[];
		SoundFragment active[];
		float sliderPosition;
		int numActive;

		public float getSliderPosition() {
			return sliderPosition;
		}

		public void setSliderPosition(float p) {
			sliderPosition = p;
		}

		public void disableLine(int i) {
			if(active[i] != null && active[i].isActive())
				active[i].activate();
		}

		public void enableLine(int i) {
			if(active[i] != null && !active[i].isActive())
				active[i].activate();
		}

		public void setVolume(int a, float v) {
			int frameSize = active[a].getFrameSize();
			int bufferSize = frameSize * BUFFER_SIZE;
			float logV = (float)Math.log(v);
			FloatControl co;
			SourceDataLine sdl = active[a].getSourceDataLine();

			if(logV < 0)
				logV = 0;

			if(!sdl.isOpen()) {
				try {
					sdl.open(active[a].getFormat(), bufferSize);
				} catch(Exception ex) {
					ex.printStackTrace();
				}

				sdl.start();
			}

			//not supported in linux, crash
			if(v >= 0 && v <= 100) {
				float cMin = (float)Math.log(1);
				float cMax = (float)Math.log(100);
				co = (FloatControl) sdl.getControl(FloatControl.Type.MASTER_GAIN);
				float minVol = co.getMinimum();
				float maxVol = co.getMaximum();
				float cVol = logV / (cMax - cMin);
				float vol = (float)(minVol + (maxVol - minVol) * cVol);
				//System.out.println("min max c vol " + minVol + " " + maxVol + " " + cVol + " " + vol);
				//System.out.println("cMin cMax " + cMin + " " + cMax);
				co.setValue(vol);
			}
		}

		public void writeToBuffer() {
			for(int i = 0; i < numActive; i++) {
				if(active[i].isActive())
					writeBufferToDataLine(i);
			}
		}

		public void update(float p) {
			int a = 0;
			setSliderPosition(p);
			numActive = 0;

			for(int i = 0; i < fragments.length; i++) {

				if(fragments[i].inRange(sliderPosition)) {

					if(numActive < active.length) {
						active[numActive++] = fragments[i];
					} else {
						Debug.print("Sound: No room in active queue");
						break;
					}

				} else if(fragments[i].isActive()) {
					fragments[i].activate();
				}

			}

			//update volume levels of active fragments
			for(int i = 0; i < numActive; i++) {
				//System.out.println("numactive " + numActive);
				
				if(!active[i].isActive())
					active[i].activate();

				setVolume(i, active[i].getVolume(sliderPosition));
			}
		}

		public void writeBufferToDataLine(int i) {
			try {
				int frameSize = active[i].getFrameSize();
				//Debug.print("Frame size is " + frameSize);
				int bufferSize = frameSize * BUFFER_SIZE;
				//Debug.print("buffer size is " + bufferSize);
				BufferedInputStream bis = active[i].getBufferedInputStream();
				int inputAvail = bis.available();

				if(inputAvail <= 0)
					bis.reset();

				SourceDataLine sdl = active[i].getSourceDataLine();
				int outputAvail = sdl.available();
				byte[] buffer = new byte[outputAvail];
				int bytesRead = bis.read(buffer, 0, buffer.length);
				sdl.write(buffer, 0, buffer.length);
			} catch(IOException ex) {
				ex.printStackTrace();
			} catch(Exception ex) {
				ex.printStackTrace();
			}
		}

		/*public void stop(int f) {
			Clip c = fragments[f].getClip();
			c.stop();
		}

		public void play(int a) {
			Clip c;
			try {
				c = active[a].getClip();
				if(!c.isOpen()) {
					c.open(active[a].getAudioInputStream());
					c.setLoopPoints(0, -1);
					c.loop(Clip.LOOP_CONTINUOUSLY);
				}

				setVolume(a, active[a].getVolume(sliderPosition));
				c.flush();
				c.start();
			} catch(Exception ex) {
				ex.printStackTrace();
				System.exit(1);
			}
		}*/

		public MixedSound(SoundFragment[] sf, float p) {

			if(p >= MIN && p <= MAX)
				sliderPosition = p;
			else {
				Debug.print("Sound:MixedSound:MixedSound(): p out of range");
				sliderPosition = MIN;
			}

			active = new SoundFragment[MAX_SIMULTANEOUS];
			fragments = sf;
			numActive = 0;
		}
	}

	public static final int N_PARAMETERS = 4;
	public static final int VALUE_PAIRS_START = 1;
	public static final int N_VALUE_PAIRS = 3;
	public static final int ENTRY_SIZE = 4;//number of records per entry

	public MixedSound loadMixedSound(String path, int numFragments) {
		File f = null;
		Scanner scan = null;
		String s = null;;
		String[] tokens = null;
		String[] parameters = null;
		float[][] pairs;
		SoundFragment[] fragments;

		f = new File(path);
		parameters = new String[numFragments * ENTRY_SIZE];
		pairs = new float[N_VALUE_PAIRS][2];

		try {
			scan = new Scanner(f);
		} catch(FileNotFoundException e) {
			Debug.print("Sound:loadMixedSound(): error loading file " + path);
			e.printStackTrace();
		}

		/*read from sound description file*/
		int c = 0;
		if(scan != null) {
			while(scan.hasNextLine()) {
				s = scan.nextLine();
				tokens = s.split("#");
				parameters[c++] = tokens[0].trim();//discard comment part of line
			}
		}

		//numFragments = c / ENTRY_SIZE;
		//Debug.print("numfragments " + numFragments);
		fragments = new SoundFragment[numFragments];

		for(int i = 0; i < numFragments; i++) {
			int current = 0;
			int p = i * ENTRY_SIZE;

			for(int j = p; j < p + ENTRY_SIZE; j++) {
				if(j >= p + VALUE_PAIRS_START) {
					tokens = parameters[j].split(",");
					pairs[current++] = new float[] {Float.parseFloat(tokens[0]), Float.parseFloat(tokens[1])};
				}
			}

			fragments[i] = loadFragment(parameters[p], pairs[0][0], pairs[0][1], pairs[1][0], pairs[1][1], pairs[2][0], pairs[2][1]);
		}

		return new MixedSound(fragments, 0);
	}


	public SoundFragment loadFragment(String path, float v, float tv, float s, float e, float fi, float fo) {
		SoundFragment sf = null;
		if(path != null) {
			AudioInputStream inStream;
			AudioFormat af;
			Clip c;
			SourceDataLine sdl;
			File f;
			FloatControl co;
			Line.Info lineInfo;


			try {
				f = new File(path);
				inStream = AudioSystem.getAudioInputStream(f);
				af = inStream.getFormat();
				lineInfo = new DataLine.Info(Class.forName("javax.sound.sampled.SourceDataLine"),  af);
				sdl = (SourceDataLine) AudioSystem.getLine(lineInfo);
				sf = new SoundFragment(sdl, inStream, v, tv, s, e, fi, fo);
			} catch(Exception ex) {
				ex.printStackTrace();
			}
		}
		return sf;
	}

	public class SoundFragment {
		BufferedInputStream inBufferStream;
		AudioInputStream inStream;
		SourceDataLine sourceDataLine;
		float start;
		float end;
		float fadeIn;
		float fadeOut;
		float targetVolume;
		float minVolume;
		private boolean active;
		private int frameSize;
		private AudioFormat af;

		public AudioFormat getFormat() {
				return af;
		}

		public int getFrameSize() {
			return frameSize;
		}

		public SoundFragment(SourceDataLine sdl, AudioInputStream ais, float v, float tv, float s, float e, float fi, float fo) {
			sourceDataLine = sdl;

			inStream = ais;
			frameSize = 0;


			try {
				af = inStream.getFormat();
				frameSize = af.getFrameSize();
				inBufferStream = new BufferedInputStream(inStream, ais.available());
				inBufferStream.mark(inBufferStream.available());
			} catch(Exception ex) {
				ex.printStackTrace();
			}
			start = s;
			end = e;
			fadeIn = fi;
			fadeOut = fo;
			targetVolume = tv;
			minVolume = v;
			active = false;
		}

		public boolean isActive() {
			return active;
		}

		public void activate() {
			active = (active) ? false: true;
		}

		/*assume p is in valid range*/
		public float getVolume(float p) {
			if(start + fadeIn > end - fadeOut) {
				Debug.print("Sound:SoundFragment:getVolume(): Overlap in fade-in and fade-out");
			}

			float baseVolume = targetVolume;

			//Debug.print("start and p " + start + " " + p);

			float dFromStart = p - start;
			float dFromEnd = end - p;
			float v = targetVolume;

			//Debug.print("targetVolume is " + targetVolume);
			//Debug.print("dFromStart is " + dFromStart);
			//Debug.print("dFromEnd is " + dFromEnd);
			//Debug.print("fadeIn is " + fadeIn);
			//Debug.print("fadeOut is " + fadeOut);
			//Debug.print("p is " + p);
			//Debug.print("start is " + start);
			//Debug.print("end is " + end);

			if(p < start || p > end)
				v = 0;
			 else if(dFromStart < fadeIn)
				v = (dFromStart / fadeIn) * v;
			 else if(dFromEnd < fadeOut) {
				v = (dFromEnd / fadeOut) * v;
				//Debug.print("FADING OUT");
			 } else
				v = Math.max(targetVolume, minVolume);//currently unneeded
				//v = Math.max(targetVolume * (dFromStart / (end - start)), minVolume);

			v = Math.max(v, minVolume);
			//Debug.print("v is " + v);

			return v;
		}

		public boolean inRange(float p) {
			//Debug.print("testing range p start end " + p + " " + start + " " + end);

			return p >= start && p <= end;
		}



		public AudioInputStream getAudioInputStream() {
			return inStream;
		}

		public BufferedInputStream getBufferedInputStream() {
			return inBufferStream;
		}

		public SourceDataLine getSourceDataLine() {
			return sourceDataLine;
		}

		public float getStart() {
			return start;
		}

		public float getEnd() {
			return end;
		}
	}
}
