package src;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Calendar;

/**
* Debug is a utility class used for printing messages to the console and/or a log file.
* To print a message in this application call the print(...) method.
*/
class Debug {
	public static boolean verbose = false;
	private static final String LOG_FILE = "debug.txt";
	private static boolean toConsole;
	private static boolean toFile;
	private static BufferedWriter writer;
	private static Queue q;

	static {
		init();
	};

	/**
	* Toggle the printing of messages to system console.
	*/
	public static void toConsole() {
		toConsole = toConsole ? false : true;
	}

	/**
	* Toggle the redirection of messages to log file.
	*/
	public static void toFile() {
		if(toFile) {
			closeLog();
		} else {
			initLog();
		}
		toFile = toFile ? false : true;
	}

	/**
	* Display all messages in the queue using each output method set.
	*/
	public static void displayMessages() {
		String msg;
		while(!q.isEmpty()) {
			msg = (String)q.pull();
			if(toConsole) {
				System.out.println(msg);
			}
			if(toFile) {
				if(writer != null) {
					try {
						writer.write(msg, 0, msg.length());
						writer.newLine();
					} catch(IOException e) {
						Debug.print("Debug.java:displayMessages(): write to log file failed");
						System.out.println(e.getStackTrace());
					}
				} else {
					print("Debug.java:displayMessages(): writer was not properly initialized");
				}
			}
		}
		if(toFile) {
			try {
				writer.flush();
			} catch(IOException e) {
				print("Debug.java:displayMessages(): error flushing write buffer");
				System.out.println(e.getStackTrace());
			}
		}
	}

	/**
	* Print a message to Debug's specified output device(s): console or file.
	*/
	public static void print(String s) {
		if(s != null) {
			q.push(s);
		} else {
			throw new NullPointerException("Debug.java:print(String s): \"s\" cannot be null");
		}
	}

	/*
	* Flush the file writer and close the log file.
	*/
	private static void closeLog() {
		if(writer != null) {
			try {
				writer.flush();
				writer.close();
			} catch (IOException e) {
				System.out.println(e.getStackTrace());
			}
		}
	}

	/*
	* Initialize the log file and buffered file I/O.
	*/
	private static void initLog() {
		Calendar calendar;
		Path path;
		String heading;
		/*create the log heading using the date and time*/
		calendar = Calendar.getInstance();
		heading = "Debug.txt LOG FILE " + calendar.get(Calendar.HOUR);
		heading = heading + ":" + calendar.get(Calendar.MINUTE);
		heading = heading + ":" + calendar.get(Calendar.SECOND);
		heading = heading + " " + calendar.get(Calendar.DATE);
		heading = heading + "/" + calendar.get(Calendar.MONTH);
		heading = heading + "/" + calendar.get(Calendar.YEAR);
		path = null;
		writer = null;
		try {
			path = FileSystems.getDefault().getPath(LOG_FILE);
		} catch (Exception e) {
			print("Debug.java:initLog(): error creating path for log file");
			System.out.println(e.getStackTrace());
		}
		try {
			Charset charset = Charset.forName("US-ASCII");
			StandardOpenOption o1 = StandardOpenOption.APPEND;
			StandardOpenOption o2 = StandardOpenOption.CREATE;
			StandardOpenOption o3 = StandardOpenOption.WRITE;
			writer = Files.newBufferedWriter(path, charset, o1, o2, o3);
		} catch (IOException e) {
			print("Debug.java:initLog(): error creating the file writer");
			System.out.println(e.getStackTrace());
		} catch (Exception e) {
			System.out.println("Debug.java:initLog(): unhandled exception");
			System.out.println(e.getStackTrace());
		}
		if(writer != null) {
			try {
				writer.write(heading, 0, heading.length());
				writer.newLine();
			} catch (IOException e) {
				System.out.println("Debug.java:initLog(): error writing log file heading");
				System.out.println(e.getStackTrace());
			}
		}
	}

	/*
	* Initialize the Debug utility class.
	*/
	private static void init() {
		q = new Queue();
		toConsole = true;
		toFile = false;
		initLog();
	}
}
//End of file
