package de.haumacher.netsim;

import java.io.IOException;
import java.net.Inet4Address;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

class Server implements Runnable {
	private int _nextId = 1;

	private final ServerSocket _server;

	private final ConcurrentHashMap<String, Forwarder> _forwarders = new ConcurrentHashMap<>();

	private final Settings _settings;

	public Server(Settings settings) throws IOException {
		_settings = settings;
		_server = new ServerSocket(8077);
	}

	@Override
	public void run() {
		Console.println("Proxy started.");
		try {
			while (true) {
				accept();
			}
		} finally {
			Console.println("Proxy stopped.");
		}
	}

	public void started(Forwarder forwarder) {
		_forwarders.put(forwarder.getKey(), forwarder);
	}

	public void terminated(Forwarder forwarder) {
		_forwarders.remove(forwarder.getKey());
	}

	private void accept() {
		try {
			tryAccept();
		} catch (IOException ex) {
			System.err.println("error: " + ex.getMessage());
		}
	}

	private void tryAccept() throws IOException, UnknownHostException {
		final Socket clientSide = _server.accept();
		final int id = _nextId++;
		Socket serverSide = new Socket();
		serverSide.connect(new InetSocketAddress(Inet4Address.getLocalHost(), 8080));
		Forwarder requestForwarder = new Forwarder(_settings, "c" + id, clientSide, serverSide.getOutputStream());
		Forwarder responseForwarder = new Forwarder(_settings, "s" + id, serverSide, clientSide.getOutputStream());

		requestForwarder.setOther(responseForwarder);
		requestForwarder.setMaster(this);
		responseForwarder.setOther(requestForwarder);
		responseForwarder.setMaster(this);

		requestForwarder.start();
		responseForwarder.start();
	}

	public void list() {
		Set<String> keySet = _forwarders.keySet();
		if (keySet.isEmpty()) {
			Console.println("No open connections.");
			return;
		}
		ArrayList<String> keys = new ArrayList<>(keySet);
		Collections.sort(keys);
		for (String key : keys) {
			Console.println(key);
		}
	}

	public boolean kill(String id) {
		Forwarder forwarder = _forwarders.get(id);
		if (forwarder == null) {
			return false;
		}

		forwarder.close();
		return true;
	}

}