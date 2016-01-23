package de.haumacher.netsim;

class Console {
	
	static long lastOut;
	static Thread daemon;

	public static synchronized void println(String line) {
		System.out.println(line);
		lastOut = System.currentTimeMillis();
		if (daemon == null) {
			daemon = new Thread("console") {
				@Override
				public void run() {
					synchronized (Console.class) {
						withConsoleLock();
					}
				}

				private void withConsoleLock() {
					try {
						tryRun();
					} catch (InterruptedException ex) {
						System.err.println("console: " + ex.getMessage());
					} finally {
						daemon = null;
					}
				}

				long lastAck = 0;
				
				private void tryRun() throws InterruptedException {
					while (true) {
						long out = waitForOutput();
						awaitTimeout(out);
						
						System.out.println();
					}
				}

				private void awaitTimeout(long out) throws InterruptedException {
					while (true) {
						long now = System.currentTimeMillis();
						long sleep = (out + 500) - now;
						if (sleep > 0) {
							Console.class.wait(sleep);
							out = lastOut;
						} else {
							lastAck = out;
							break;
						}
					}
				}

				private long waitForOutput() throws InterruptedException {
					long out;
					while (true) {
						out = lastOut;
						if (out != lastAck) {
							break;
						}
						Console.class.wait();
					}
					return out;
				}
			};
			daemon.setDaemon(true);
			daemon.start();
		} else {
			Console.class.notifyAll();
		}
	}
	
}