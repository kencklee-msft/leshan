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
package org.eclipse.leshan.server.californium;

import java.net.InetSocketAddress;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.Set;

import javax.net.ssl.SSLContext;

import org.eclipse.californium.core.network.CoAPEndpoint;
import org.eclipse.californium.core.network.Endpoint;
import org.eclipse.californium.core.network.config.NetworkConfig;
import org.eclipse.californium.core.network.tcp.TcpServerEndpoint;
import org.eclipse.californium.elements.tcp.server.TcpServerConnector;
import org.eclipse.californium.elements.tcp.server.TlsServerConnector;
import org.eclipse.californium.elements.tcp.server.TlsServerConnector.SSLClientCertReq;
import org.eclipse.californium.scandium.DTLSConnector;
import org.eclipse.californium.scandium.dtls.cipher.CipherSuite;
import org.eclipse.leshan.core.request.BindingMode;
import org.eclipse.leshan.server.LwM2mServer;
import org.eclipse.leshan.server.californium.impl.LeshanServer;
import org.eclipse.leshan.server.californium.impl.LwM2mPskStore;
import org.eclipse.leshan.server.californium.impl.SecureEndpoint;
import org.eclipse.leshan.server.client.ClientRegistry;
import org.eclipse.leshan.server.impl.ClientRegistryImpl;
import org.eclipse.leshan.server.impl.ObservationRegistryImpl;
import org.eclipse.leshan.server.impl.SecurityRegistryImpl;
import org.eclipse.leshan.server.model.LwM2mModelProvider;
import org.eclipse.leshan.server.model.StandardModelProvider;
import org.eclipse.leshan.server.observation.ObservationRegistry;
import org.eclipse.leshan.server.security.SecurityRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class helping you to build and configure a Californium based Leshan Lightweight M2M server. Usage: create it, call
 * the different setters for changing the configuration and then call the {@link #build()} method for creating the
 * {@link LwM2mServer} ready to operate.
 */
public class LeshanServerBuilder {

    private static final Logger LOG = LoggerFactory.getLogger(LeshanServer.class);

    /** default loopback address */
    private static final String LOCALHOST = "127.0.0.1";


    /** IANA assigned UDP port for CoAP (so for LWM2M) */
    public static final int PORT = 5683;

    /** IANA assigned UDP port for CoAP with DTLS (so for LWM2M) */
    public static final int PORT_DTLS = 5684;

    public static LeshanTcpServerBuilder<?> getLeshanTCPServerBuilder() {
        return new LeshanTcpServerBuilder();
    }

    public static LeshanTlsServerBuilder getLeshanTLSServerBuilder() {
        return new LeshanTlsServerBuilder();
    }

    public static LeshanUDPServerBuilder getLeshanUDPServerBuilder() {
        return new LeshanUDPServerBuilder();

    }

    public static abstract class BasicLeshanServerBuilder<E extends BasicLeshanServerBuilder<E>> {

        private final EnumSet<BindingMode> bindingModes;

        protected SecurityRegistry securityRegistry;
        protected ObservationRegistry observationRegistry;
        protected ClientRegistry clientRegistry;
        protected LwM2mModelProvider modelProvider;
        protected String address;
        protected int port;

        private BasicLeshanServerBuilder(final BindingMode bMode) {
            bindingModes = BindingMode.getBindingMode(bMode);
        }

        public E setClientRegistry(final ClientRegistry clientRegistry) {
            this.clientRegistry = clientRegistry;
            return (E) this;
        }

        public E setObservationRegistry(final ObservationRegistry observationRegistry) {
            this.observationRegistry = observationRegistry;
            return (E) this;
        }

        public E setSecurityRegistry(final SecurityRegistry securityRegistry) {
            this.securityRegistry = securityRegistry;
            return (E) this;
        }

        public E setObjectModelProvider(final LwM2mModelProvider objectModelProvider) {
            this.modelProvider = objectModelProvider;
            return (E) this;
        }

        public E setAddress(final String address) {
            this.address = address;
            return (E) this;
        }

        public E setPort(final int port) {
            this.port = port;
            return (E) this;
        }

        public LeshanServer build() {
            if (clientRegistry == null)
                clientRegistry = new ClientRegistryImpl();
            if (securityRegistry == null)
                securityRegistry = new SecurityRegistryImpl();
            if (observationRegistry == null)
                observationRegistry = new ObservationRegistryImpl();
            if (modelProvider == null)
                modelProvider = new StandardModelProvider();

            if (bindingModes.contains(BindingMode.Q) || bindingModes.contains(BindingMode.S))
                LOG.info("Binding Modes Q and S are not supported in this verison, they will be omitted");

            final Set<Endpoint> endpoints = buildEndpoints();

            return new LeshanServer(clientRegistry, securityRegistry, observationRegistry, modelProvider, endpoints);
        }

        protected abstract Set<Endpoint> buildEndpoints();
    }

    public static class LeshanTcpServerBuilder<T extends LeshanTcpServerBuilder<T>> extends BasicLeshanServerBuilder<T> {

        public LeshanTcpServerBuilder() {
            super(BindingMode.T);
        }

        // TODO add this in if needed
        // public <O> T addChannelOption(final ChannelOption<O> option, final O value) {
        // connectionConfig.addChannelOption(option, value);
        // return (T) this;
        // }

        @Override
        protected Set<Endpoint> buildEndpoints() {
            final TcpServerConnector serverConnector = new TcpServerConnector(this.address != null ? this.address : LOCALHOST, this.port);

            final Set<Endpoint> endpoints = new HashSet<>();
            endpoints.add(new TcpServerEndpoint(
                    serverConnector, NetworkConfig.getStandard()));

            return endpoints;
        }
    }

    public static class LeshanTlsServerBuilder extends LeshanTcpServerBuilder<LeshanTlsServerBuilder> {

        protected SSLContext sslContext;
        protected SSLClientCertReq req;
        protected String[] supportedTLSVersion;


        public LeshanTlsServerBuilder setSSLContext(final SSLContext sslContext) {
            this.sslContext = sslContext;
            return this;
        }

        public LeshanTlsServerBuilder setSSLClientCertReq(final SSLClientCertReq req) {
            this.req = req;
            return this;
        }

        public LeshanTlsServerBuilder setSupportedTLSVersion(final String... supportedTLSVersion) {
            this.supportedTLSVersion = supportedTLSVersion;
            return this;
        }

        @Override
        protected Set<Endpoint> buildEndpoints() {
            final TlsServerConnector serverConnector = new TlsServerConnector(this.address != null ? this.address : LOCALHOST, this.port, 
                    this.sslContext, this.req != null ? this.req : SSLClientCertReq.NONE, 
                    this.supportedTLSVersion != null ? this.supportedTLSVersion : new String[0]);

            final Set<Endpoint> endpoints = new HashSet<>();
            endpoints.add(new TcpServerEndpoint(serverConnector, NetworkConfig.getStandard()));

            return endpoints;
        }
    }

    public static class LeshanUDPServerBuilder extends BasicLeshanServerBuilder<LeshanUDPServerBuilder> {

        private InetSocketAddress localAddress;
        private InetSocketAddress localAddressSecure;

        private LeshanUDPServerBuilder() {
            super(BindingMode.U);
        }


        public LeshanUDPServerBuilder setLocalAddress(final String hostname, final int port) {
            this.localAddress = new InetSocketAddress(hostname, port);
            return this;
        }

        public LeshanUDPServerBuilder setLocalAddressSecure(final String hostname, final int port) {
            this.localAddressSecure = new InetSocketAddress(hostname, port);
            return this;
        }

        @Override
        protected Set<Endpoint> buildEndpoints() {
            final Set<Endpoint> endpoints = new HashSet<>();

            endpoints
                    .add(new CoAPEndpoint(this.localAddress != null ? this.localAddress : new InetSocketAddress(PORT)));

            // secure endpoint
            final DTLSConnector connector = new DTLSConnector(this.localAddressSecure != null ? this.localAddressSecure
                    : new InetSocketAddress(PORT_DTLS));
            connector.getConfig().setPskStore(new LwM2mPskStore(this.securityRegistry, this.clientRegistry));
            final PrivateKey privateKey = this.securityRegistry.getServerPrivateKey();
            final PublicKey publicKey = this.securityRegistry.getServerPublicKey();
            if (privateKey != null && publicKey != null) {
                connector.getConfig().setPrivateKey(privateKey, publicKey);
                // TODO this should be automatically done by scandium
                connector.getConfig().setPreferredCipherSuite(CipherSuite.TLS_ECDHE_ECDSA_WITH_AES_128_CCM_8);
            } else {
                // TODO this should be automatically done by scandium
                connector.getConfig().setPreferredCipherSuite(CipherSuite.TLS_PSK_WITH_AES_128_CCM_8);
            }
            final Endpoint secureEndpoint = new SecureEndpoint(connector);
            endpoints.add(secureEndpoint);

            return endpoints;
        }
    }
}
