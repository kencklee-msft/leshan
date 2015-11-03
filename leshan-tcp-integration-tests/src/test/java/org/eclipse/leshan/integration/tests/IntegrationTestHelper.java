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
 *     Zebra Technologies - initial API and implementation
 *******************************************************************************/

package org.eclipse.leshan.integration.tests;

import java.net.InetSocketAddress;

import org.eclipse.leshan.client.LwM2mClient;
import org.eclipse.leshan.client.californium.LeshanClientBuilder;
import org.eclipse.leshan.server.LwM2mServer;
import org.eclipse.leshan.server.californium.LeshanServerBuilder;
import org.eclipse.leshan.server.californium.LeshanServerBuilder.LeshanTcpServerBuilder;
import org.eclipse.leshan.server.client.Client;
import org.eclipse.leshan.server.impl.SecurityRegistryImpl;

/**
 * Helper for running a server and executing a client against it.
 * 
 */
public class IntegrationTestHelper {

    static final String ENDPOINT_IDENTIFIER = "kdfflwmtm";
    private static final String LOCALHOST = "localhost";
    private static final int PORT = 5683;
    private static final int SECURE_PORT = 443;
    private static final InetSocketAddress SERVER_ADDRESS = new InetSocketAddress(LOCALHOST, PORT);
    private static final InetSocketAddress SECURE_SERVER_ADDRESS = new InetSocketAddress(LOCALHOST, SECURE_PORT);

    LwM2mServer server;
    LwM2mClient client;

    public void createClient() {
        final LeshanClientBuilder builder = new LeshanClientBuilder();
        client = builder.setServerAddress(getServerAddress()).addBindingModeTCPClient().configure().build(2, 3);

    }

    public void createServer() {
        final LeshanTcpServerBuilder<?> builder = LeshanServerBuilder.getLeshanTCPServerBuilder();
        builder.setAddress(LOCALHOST);
        builder.setPort(PORT);
        builder.setSecurityRegistry(new SecurityRegistryImpl() {
            // TODO we should separate SecurityRegistryImpl in 2 registries :
            // InMemorySecurityRegistry and PersistentSecurityRegistry

            @Override
            protected void loadFromFile() {
                // do not load From File
            }

            @Override
            protected void saveToFile() {
                // do not save to file
            }
        });
        server = builder.build();
    }

    Client getClient() {
        return server.getClientRegistry().get(ENDPOINT_IDENTIFIER);
    }

    protected InetSocketAddress getServerAddress() {
        return SERVER_ADDRESS;
    }
    
    protected InetSocketAddress getServerSecureAddress() {
        return SECURE_SERVER_ADDRESS;
    }
}
