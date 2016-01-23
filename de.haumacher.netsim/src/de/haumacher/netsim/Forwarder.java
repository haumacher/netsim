package de.haumacher.netsim;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.SocketException;


final class Forwarder extends Thread {
		private final OutputStream _out;

		private final String _id;

		private final Socket _input;

		private Forwarder _other;

		private Server master;

		private final Settings _settings;

		public Forwarder(Settings settings, String id, Socket input, OutputStream out) {
			super(id);
			
			_settings = settings;
			_id = id;
			_input = input;
			_out = out;
		}
		
		public String getKey() {
			return _id;
		}

		@Override
		public void run() {
			Console.println(_id + ": started");
			master.started(this);
			try {
				InputStream in = _input.getInputStream();
				byte[] buffer = new byte[4096];
				int direct;
				while ((direct = in.read(buffer)) >= 0) {
					if (direct > 0) {
						_out.write(buffer, 0, direct);
					}
				}
			} catch (SocketException ex) {
				if ("socket closed".equals(ex.getMessage())) {
					Console.println(_id + ": " + ex.getMessage());
				} else {
					error(ex);
				}
			} catch (Exception ex) {
				error(ex);
			} finally {
				Console.println(_id + ": stopping");
				try {
					_input.close();
				} catch (IOException ex) {
					error(ex);
				}
				if (_settings.symmetricShutdown()) {
					if (_other != null) {
						_other.close();
					}
				}
				master.terminated(this);
				Console.println(_id + ": stopped");
			}
		}

		private void error(Exception ex) {
			System.err.println(_id + ": error: " + ex.getMessage());
		}

		public void close() {
			try {
				_input.close();
				_out.close();
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