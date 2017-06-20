import java.awt.Desktop;
import java.awt.GraphicsEnvironment;
import java.io.File;
import java.net.URI;

import javax.swing.JOptionPane;
import javax.swing.UIManager;

import net.dv8tion.jda.core.utils.SimpleLog;
import net.dv8tion.jda.core.utils.SimpleLog.Level;

public class a {

	private static boolean gui;
	private static boolean debug;

	static boolean isGui() {
		return gui;
	}

	static boolean isDebug() {
		return debug;
	}

	public static void main(String[] args) {
		gui = containsArg(args, "--gui");
		debug = containsArg(args, "--debug") || Values.DEV;

		if(gui) {
			if(GraphicsEnvironment.isHeadless()) {
				// no gui supported
				gui = false; // disable gui mode
				errExit("GUI is not supported on your system.", Values.EXIT_CODE_GUI_NOT_SUPPORTED);
			}
			try {
				try {
					if(System.getProperty("os.name").toLowerCase().equals("linux") && System.getenv("XDG_CURRENT_DESKTOP").toLowerCase().equals("kde")) {
						// KDE theme fix
						UIManager.setLookAndFeel("com.sun.java.swing.plaf.gtk.GTKLookAndFeel");
					} else {
						// Use the systems theme
						UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
					}
				} catch (Exception e) {
					Log.debug("Failed to set look and feel.");
				}

				Log.debug("Launching GUI ...");

				// Launch GUI
				GUI frame = new GUI();
				frame.setVisible(true);

				// disable logging
				SimpleLog.LEVEL = Level.OFF;
			} catch (Exception e) {
				errExit("Failed to start GUI: " + e.getMessage());
			}
		} else {
			// find and set first error level (to only print errors of JDA)
			for(Level logLevel : Level.values()) {
				if(logLevel.isError()) {
					SimpleLog.LEVEL = logLevel;
					break;
				}
			}

			launch(args);
		}
	}

	static void launch(String[] args) {
		Log.print("Version: " + Values.BOT_VERSION);
		Log.print("Developer: " + Values.BOT_DEVELOPER);

		Log.print("Starting ...");

		// override log level if debug
		if(debug) {
			SimpleLog.LEVEL = Level.ALL;
		}

		// init config
		File configFile;
		if(Values.DEV) {
			configFile = new File(Values.DEV_CONFIG);
		} else {
			String configArg = getArg(args, "--config");
			if(configArg != null) {
				configFile = new File(configArg);
			} else {
				configFile = new File(Values.DEFAULT_CONFIG);
			}
		}

		Log.print("Config: " + configFile.getAbsolutePath());

		// load the config file
		if(!Config.init(configFile)) {
			errExit("Failed to load config.");
		}

		// Start the bot
		Bot.start();
	}

	static void errExit() {
		errExit(null);
	}

	static void errExit(String msg) {
		errExit(msg, 1);
	}


	static void errExit(String msg, int exitCode) {
		if(gui) {
			GUI.onErrExit(msg);
		} else {
			Log.print("Crash! Reason:");
			System.err.println(msg == null ? "Unknown" : msg);
			try {
				Thread.sleep(5000);
			} catch (InterruptedException e) {
				//e.printStackTrace();
			}
		}

		if(exitCode >= 1 && exitCode <= 127) {
			System.exit(exitCode);
		} else {
			System.exit(0);
		}
	}

	private static int getArgIndex(String[] args, String arg) {
		int ir = -1; // return -1 if args does not contain the argument
		for(int in = 0; in < args.length; in++) {
			if(args[in].equalsIgnoreCase(arg)) {
				ir = in;
				break;
			}
		}
		return ir;
	}

	private static boolean containsArg(String[] args, String arg) {
		return getArgIndex(args, arg) != -1;
	}

	// returns what is behind an argument
	private static String getArg(String[] args, String arg) {
		String result = null; // return null if argument is not given
		int i = getArgIndex(args, arg);
		if(i != -1) {
			result = args[i + 1];
		}
		return result;
	}

	//TODO: maybe find a better way (move to GUI class?)
	static void addToServerMessage(String link) {
		if(gui) {
			int r = JOptionPane.showConfirmDialog(null, "Do you want to add the bot to your server now?" + System.lineSeparator() + "This will open:" + System.lineSeparator() + link, Values.BOT_NAME, JOptionPane.YES_NO_OPTION);
			if(r == JOptionPane.YES_OPTION) {
				try {
					Desktop.getDesktop().browse(new URI(link));
				} catch (Exception e) {
					GUI.showErrMsgBox(e.getMessage());
				}
			}
			System.exit(1); // exit / restart after invite
		} else {
			errExit("To add the bot to your server visit: " + link);
		}
	}

}
