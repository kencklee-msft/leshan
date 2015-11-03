/*******************************************************************************
 * Copyright (c) 2013-2015 Sierra Wireless and others.
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * and Eclipse Distribution License v1.0 which accompany this distribution.
 * 
 * The Eclipse Public License is available at
 *    http://www.eclipse.org/legal/epl-v10.html
 * and the Eclipse Distribution License is available at
 *    http://www.eclipse.org/org/documents/edl-v10.html.
 * 
 * Contributors:
 *     Sierra Wireless - initial API and implementation
 *******************************************************************************/
package org.eclipse.leshan.standalone;

import java.math.BigInteger;
import java.security.AlgorithmParameters;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.ECGenParameterSpec;
import java.security.spec.ECParameterSpec;
import java.security.spec.ECPoint;
import java.security.spec.ECPrivateKeySpec;
import java.security.spec.ECPublicKeySpec;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.InvalidParameterSpecException;
import java.security.spec.KeySpec;
import java.util.EnumSet;

import javax.net.ssl.SSLException;
import javax.xml.bind.DatatypeConverter;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.webapp.WebAppContext;
import org.eclipse.leshan.core.request.BindingMode;
import org.eclipse.leshan.server.californium.LeshanServerBuilder;
import org.eclipse.leshan.server.californium.LeshanServerBuilder.BasicLeshanServerBuilder;
import org.eclipse.leshan.server.californium.LeshanServerBuilder.LeshanTcpServerBuilder;
import org.eclipse.leshan.server.californium.LeshanServerBuilder.LeshanUDPServerBuilder;
import org.eclipse.leshan.server.californium.impl.LeshanServer;
import org.eclipse.leshan.server.impl.SecurityRegistryImpl;
import org.eclipse.leshan.standalone.servlet.ClientServlet;
import org.eclipse.leshan.standalone.servlet.EventServlet;
import org.eclipse.leshan.standalone.servlet.ObjectSpecServlet;
import org.eclipse.leshan.standalone.servlet.SecurityServlet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LeshanStandalone {

	private static final Logger LOG = LoggerFactory.getLogger(LeshanStandalone.class);

	private Server server;
	private LeshanServer lwServer;

	public void start() {
		// Use those ENV variables for specifying the interface to be bound for coap and coaps
		final String iface = System.getenv("COAPIFACE");
		final String ifaces = System.getenv("COAPSIFACE");
		final String mainBindingMode = System.getenv("BINDING");

		final EnumSet<BindingMode> bindingModes = BindingMode.parseFromString(mainBindingMode);

		if(bindingModes.contains(BindingMode.T)) {
			buildTCPStandaloneServer(iface);
		} else if(bindingModes.contains(BindingMode.U)) {
			buildUDPStandaloneServer(iface, ifaces);
		} else {
			LOG.error("NO Valide binding mode was specified, please pass environement variable BINDING as either U or T, Q and S are not supported for now ");
		}
	}

	private void buildUDPStandaloneServer(final String iface, final String ifaces) {
		// Build LWM2M server
		final LeshanUDPServerBuilder builder = LeshanServerBuilder.getLeshanUDPServerBuilder();
		if (iface != null && !iface.isEmpty()) {
			final String[] add = iface.split(":");
			builder.setLocalAddress(add[0], Integer.parseInt(add[1]));
		}
		if (ifaces != null && !ifaces.isEmpty()) {
			final String[] adds = ifaces.split(":");
			builder.setLocalAddressSecure(adds[0], Integer.parseInt(adds[1]));
		}

		// Get public and private server key
		PrivateKey privateKey = null;
		PublicKey publicKey = null;
		try {
			// Get point values
			final byte[] publicX = DatatypeConverter
					.parseHexBinary("fcc28728c123b155be410fc1c0651da374fc6ebe7f96606e90d927d188894a73");
			final byte[] publicY = DatatypeConverter
					.parseHexBinary("d2ffaa73957d76984633fc1cc54d0b763ca0559a9dff9706e9f4557dacc3f52a");
			final byte[] privateS = DatatypeConverter
					.parseHexBinary("1dae121ba406802ef07c193c1ee4df91115aabd79c1ed7f4c0ef7ef6a5449400");

			// Get Elliptic Curve Parameter spec for secp256r1
			final AlgorithmParameters algoParameters = AlgorithmParameters.getInstance("EC");
			algoParameters.init(new ECGenParameterSpec("secp256r1"));
			final ECParameterSpec parameterSpec = algoParameters.getParameterSpec(ECParameterSpec.class);

			// Create key specs
			final KeySpec publicKeySpec = new ECPublicKeySpec(new ECPoint(new BigInteger(publicX), new BigInteger(publicY)),
					parameterSpec);
			final KeySpec privateKeySpec = new ECPrivateKeySpec(new BigInteger(privateS), parameterSpec);

			// Get keys
			publicKey = KeyFactory.getInstance("EC").generatePublic(publicKeySpec);
			privateKey = KeyFactory.getInstance("EC").generatePrivate(privateKeySpec);

			builder.setSecurityRegistry(new SecurityRegistryImpl(privateKey, publicKey));
		} catch (InvalidKeySpecException | NoSuchAlgorithmException | InvalidParameterSpecException e) {
			LOG.warn("Unable to load RPK.", e);
		}

		startServer(builder);
	}

	//offer a secure connection as well
	private void buildTCPStandaloneServer(final String iface) {
		// Build LWM2M server
		final LeshanTcpServerBuilder<?> builder = LeshanServerBuilder.getLeshanTCPServerBuilder();
		if (iface != null && !iface.isEmpty()) {
			final String[] add = iface.split(":");
			builder.setAddress(add[0]).setPort(Integer.parseInt(add[1]));
		} else {
			LOG.error("No address as been specified, please enter arguement HOSTNAME, PORT for TCP");
		}

		final TLSServerConnectionConfig config = new TLSServerConnectionConfig("localhost", 5684);
		final String keystore = "/Users/simonlemoy/Workspace_github/tls_tmp/zatar-server-1.ks";
		try {
			config.secure("TLS", "password", new String[]{keystore}, "TLSv1.1", "TLSv1.2");
		} catch (SSLException | NoSuchAlgorithmException e1) {
			LOG.error("could not setup a secure connection: ", e1);
		}
		startServer(builder);

	}

	private void startServer(final BasicLeshanServerBuilder<?> builder) {
		lwServer = builder.build();
		lwServer.start();

		// Now prepare and start jetty
		String webPort = System.getenv("PORT");
		if (webPort == null || webPort.isEmpty()) {
			webPort = System.getProperty("PORT");
		}
		if (webPort == null || webPort.isEmpty()) {
			webPort = "8080";
		}
		server = new Server(Integer.valueOf(webPort));
		final WebAppContext root = new WebAppContext();
		root.setContextPath("/");
		root.setResourceBase(this.getClass().getClassLoader().getResource("webapp").toExternalForm());
		root.setParentLoaderPriority(true);
		server.setHandler(root);

		// Create Servlet
		final EventServlet eventServlet = new EventServlet(lwServer);
		final ServletHolder eventServletHolder = new ServletHolder(eventServlet);
		root.addServlet(eventServletHolder, "/event/*");

		final ServletHolder clientServletHolder = new ServletHolder(new ClientServlet(lwServer));
		root.addServlet(clientServletHolder, "/api/clients/*");

		final ServletHolder securityServletHolder = new ServletHolder(new SecurityServlet(lwServer.getSecurityRegistry()));
		root.addServlet(securityServletHolder, "/api/security/*");

		final ServletHolder objectSpecServletHolder = new ServletHolder(new ObjectSpecServlet());
		root.addServlet(objectSpecServletHolder, "/api/objectspecs/*");

		// Start jetty
		try {
			server.start();
		} catch (final Exception e) {
			LOG.error("jetty error", e);
		}
	}


	public void stop() {
		try {
			lwServer.destroy();
			server.stop();
		} catch (final Exception e) {
			throw new RuntimeException(e);
		}
	}

	public static void main(final String[] args) {
		new LeshanStandalone().start();
	}
}