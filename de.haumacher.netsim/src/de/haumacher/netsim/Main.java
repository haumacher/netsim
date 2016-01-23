package de.haumacher.netsim;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Main entry point for the command line version.
 * 
 * @author <a href="mailto:haui@haumacher.de">Bernhard Haumacher</a>
 */
public class Main {

	/**
	 * Main method to start the proxy.
	 */
	public static void main(String[] args) throws IOException {
		Settings settings = new Settings();
		Server server = new Server(settings);
		new Thread(server, "dispatcher").start();
		
		
		BufferedReader console = new BufferedReader(new InputStreamReader(System.in));
		while (true) {
			List<String> arguments = readCommand(console);
			if (arguments.isEmpty()) {
				continue;
			}
			
			String command = arguments.get(0);
			if ("list".equals(command)) {
				if (arguments.size() != 1) {
					System.err.println("syntax: list");
				}
				server.list();
			} else if ("kill".equals(command)) {
				if (arguments.size() < 2) {
					System.err.println("syntax: kill <id>+");
				} else {
					for (int n = 1, cnt = arguments.size(); n < cnt; n++) {
						String id = arguments.get(n);
						boolean ok = server.kill(id);
						if (!ok) {
							System.err.println("error: not active: " + id);
						}
					}
				}
			} else if ("enable".equals(command)) {
				if (arguments.size() != 2) {
					System.err.println("syntax: enable <property>");
				} else {
					String property = arguments.get(1);
					setProperty(settings, property, true);
				}
			} else if ("disable".equals(command)) {
				if (arguments.size() != 2) {
					System.err.println("syntax: disable <property>");
				} else {
					String property = arguments.get(1);
					setProperty(settings, property, false);
				}
			} else if ("help".equals(command)) {
				if (arguments.size() > 1) {
					System.err.println("syntax: help");
				} else {
					System.out.println("commands: list, kill, enable, disable, help");
				}
			} else {
				System.err.println("syntax: unknown command");
			}
		}
	}

	private static void setProperty(Settings settings, String property, boolean state) {
		if ("symmetric-shutdown".equals(property)) {
			settings.setSymmetricShutdown(state);
		} else {
			System.err.println("syntax: unknown property");
		}
	}

	static Pattern PART_PATTERN = 
			Pattern.compile("\\s*" + "(?:" + "([^\\s'\"]+)" + "|" + "(?:" + "\"([^\"]*)\"" + ")" + "|"  + "(?:" + "'([^']*)'" + ")" + ")" + "\\s*");
	
	private static List<String> readCommand(BufferedReader console) throws IOException {
		String line = console.readLine();

		List<String> arguments = new ArrayList<>();
		Matcher matcher = PART_PATTERN.matcher(line);
		while (matcher.lookingAt()) {
			if (matcher.group(1) != null) {
				arguments.add(matcher.group(1));
			}
			else if (matcher.group(2) != null) {
				arguments.add(matcher.group(2));
			}
			else if (matcher.group(3) != null) {
				arguments.add(matcher.group(3));
			}
			matcher.region(matcher.end(), line.length());
		}
		assert matcher.regionStart() == matcher.regionEnd() || line.trim().isEmpty();
		return arguments;
	}
	
}
