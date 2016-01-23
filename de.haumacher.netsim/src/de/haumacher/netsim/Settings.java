package de.haumacher.netsim;

class Settings {

	private boolean _symmetricShutdown = true;

	/**
	 * Whether to forward a connection shutdown from one side of a proxied connection ot the other
	 * side.
	 * 
	 * <p>
	 * When set to <code>false</code>, the socket to the client stays open if the server closes its
	 * connection and vice versa. This results in a failure when the next communication occurs on
	 * the side of the connection that was left open.
	 * </p>
	 */
	public boolean symmetricShutdown() {
		return _symmetricShutdown;
	}

	public void setSymmetricShutdown(boolean symmetricShutdown) {
		_symmetricShutdown = symmetricShutdown;
	}

}
