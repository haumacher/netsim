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
		Server server = new Server();
		new Thread(server, "dispatcher").start();
		
		Pattern partPattern = Pattern.compile("\\s*" + "(?:" + "([^\\s'\"]+)" + "|" + "(?:" + "\"([^\"]*)\"" + ")" + "|"  + "(?:" + "'([^']*)'" + ")" + ")" + "\\s*");
		
		BufferedReader console = new BufferedReader(new InputStreamReader(System.in));
		while (true) {
			String command = console.readLine();

			List<String> arguments = new ArrayList<>();
			Matcher matcher = partPattern.matcher(command);
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
				matcher.region(matcher.end(), command.length());
			}
			assert matcher.regionStart() == matcher.regionEnd() || command.trim().isEmpty();
			
			if (arguments.isEmpty()) {
				continue;
			}
			
			if ("list".equals(arguments.get(0))) {
				if (arguments.size() != 1) {
					System.err.println("syntax: list");
				}
				server.list();
			}
			else if ("kill".equals(arguments.get(0))) {
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
			} else {
				System.err.println("syntax: unknown command");
			}
		}
	}
	
}
