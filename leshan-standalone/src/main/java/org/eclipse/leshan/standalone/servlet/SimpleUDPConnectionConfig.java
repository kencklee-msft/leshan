package org.eclipse.leshan.standalone.servlet;

import org.eclipse.californium.elements.config.UDPConnectionConfig;

public class SimpleUDPConnectionConfig extends UDPConnectionConfig{
	
	private final String remoteAddress;
	private final int remotePort;

	public SimpleUDPConnectionConfig(final String remoteAddress, final int remotePort) {
		this.remoteAddress = remoteAddress;
		this.remotePort = remotePort;
	}

	@Override
	public String getRemoteAddress() {
		return remoteAddress;
	}

	@Override
	public int getRemotePort() {
		return remotePort;
	}

}
