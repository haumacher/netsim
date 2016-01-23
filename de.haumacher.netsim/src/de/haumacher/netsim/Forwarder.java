package de.haumacher.netsim;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.SocketException;


final class Forwarder extends Thread {
		private final OutputStream out;

		private final String id;

		private final Socket input;

		private Forwarder _other;

		private Server master;

		public Forwarder(String id, Socket input, OutputStream out) {
			super(id);
			
			this.out = out;
			this.id = id;
			this.input = input;
		}
		
		public String getKey() {
			return id;
		}

		@Override
		public void run() {
			Console.println(id + ": started");
			master.started(this);
			try {
				InputStream in = input.getInputStream();
				byte[] buffer = new byte[4096];
				int direct;
				while ((direct = in.read(buffer)) >= 0) {
					if (direct > 0) {
						out.write(buffer, 0, direct);
					}
				}
			} catch (SocketException ex) {
				if ("socket closed".equals(ex.getMessage())) {
					Console.println(id + ": " + ex.getMessage());
				} else {
					error(ex);
				}
			} catch (Exception ex) {
				error(ex);
			} finally {
				Console.println(id + ": stopping");
				try {
					input.close();
				} catch (IOException ex) {
					error(ex);
				}
				if (_other != null) {
					// Forward connection shutdown to the other side. Otherwise, a half-open connection remains.
					_other.close();
				}
				master.terminated(this);
				Console.println(id + ": stopped");
			}
		}

		private void error(Exception ex) {
			System.err.println(id + ": error: " + ex.getMessage());
		}

		public void close() {
			try {
				input.close();
				out.close();
			} catch (IOException ex) {
				error(ex);
			}
		}

		public void setOther(Forwarder other) {
			_other = other;
		}

		public void setMaster(Server server) {
			this.master = server;
		}
	}