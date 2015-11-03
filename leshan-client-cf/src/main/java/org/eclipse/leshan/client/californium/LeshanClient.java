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
package org.eclipse.leshan.client.californium;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicBoolean;

import org.eclipse.californium.core.CoapServer;
import org.eclipse.californium.core.network.CoAPEndpoint;
import org.eclipse.californium.core.network.Endpoint;
import org.eclipse.leshan.client.LwM2mClient;
import org.eclipse.leshan.client.californium.impl.CaliforniumLwM2mClientRequestPreSender;
import org.eclipse.leshan.client.californium.impl.CaliforniumLwM2mClientRequestSender;
import org.eclipse.leshan.client.californium.impl.ObjectResource;
import org.eclipse.leshan.client.resource.LwM2mObjectEnabler;
import org.eclipse.leshan.core.request.UplinkRequest;
import org.eclipse.leshan.core.response.ErrorCallback;
import org.eclipse.leshan.core.response.LwM2mResponse;
import org.eclipse.leshan.core.response.ResponseCallback;
import org.eclipse.leshan.util.Validate;

/**
 * A Lightweight M2M client.
 */
public class LeshanClient implements LwM2mClient {

    private final CoapServer clientSideServer;
    private final CaliforniumLwM2mClientRequestSender requestSender;
    private final List<LwM2mObjectEnabler> objectEnablers;
    private final CaliforniumLwM2mClientRequestPreSender requestPresender;
    private final AtomicBoolean isConnected;

    public LeshanClient(final InetSocketAddress serverAddress, final List<LwM2mObjectEnabler> objectEnablers) {
        this(new CoAPEndpoint(new InetSocketAddress("0", 0)), serverAddress, objectEnablers);
    }

    public LeshanClient(final InetSocketAddress clientAddress, final InetSocketAddress serverAddress,
            final List<LwM2mObjectEnabler> objectEnablers) {
        this(new CoAPEndpoint(clientAddress), serverAddress, objectEnablers);
    }

    public LeshanClient(final Endpoint endpoint, final InetSocketAddress serverAddress,
            final List<LwM2mObjectEnabler> objectEnablers) {

        Validate.notNull(endpoint);
        Validate.notNull(serverAddress);
        Validate.notNull(objectEnablers);
        Validate.notEmpty(objectEnablers);

        isConnected = new AtomicBoolean(false);

        clientSideServer = new CoapServer();
        clientSideServer.addEndpoint(endpoint);

        this.objectEnablers = new ArrayList<>(objectEnablers);
        final Set<ObjectResource> clientObjects = new HashSet<ObjectResource>();
        for (final LwM2mObjectEnabler enabler : objectEnablers) {
            if (clientSideServer.getRoot().getChild(Integer.toString(enabler.getId())) != null) {
                throw new IllegalArgumentException("Trying to load Client Object of name '" + enabler.getId()
                        + "' when one was already added.");
            }

            final ObjectResource clientObject = new ObjectResource(enabler);
            clientObjects.add(clientObject);
            clientSideServer.add(clientObject);
        }

        requestPresender = new CaliforniumLwM2mClientRequestPreSender(clientObjects, this);
        requestSender = new CaliforniumLwM2mClientRequestSender(endpoint, serverAddress, this);
    }

    @Override
    public void start() {
        if (isConnected.compareAndSet(false, true)) {
            try {
                final Map<InetSocketAddress, Future<?>> futures = clientSideServer.start();
                for (final Future<?> f : futures.values()) {
                    f.get();
                }
            } catch (ExecutionException | InterruptedException e) {
                isConnected.set(false);
                throw new RuntimeException("Execution exception on start", e);
            }
        }
    }

    @Override
    public void stop() {
        if (isConnected.compareAndSet(true, false)) {
            final Map<InetSocketAddress, Future<?>> futures = clientSideServer.stop();
            try {
                for (final Future<?> f : futures.values()) {
                    f.get();
                }
            } catch (ExecutionException | InterruptedException e) {
                throw new RuntimeException("Execution exception on start", e);
            }
        }
    }

    @Override
    public <T extends LwM2mResponse> T send(final UplinkRequest<T> request) {
        if (!isConnected.get()) {
            throw new RuntimeException("Internal CoapServer is not started.");
        }
        return requestSender.send(request, null);
    }

    @Override
    public <T extends LwM2mResponse> T send(final UplinkRequest<T> request, long timeout) {
        if (!isConnected.get()) {
            throw new RuntimeException("Internal CoapServer is not started.");
        }
        return requestSender.send(request, timeout);
    }

    @Override
    public <T extends LwM2mResponse> void send(final UplinkRequest<T> request,
            final ResponseCallback<T> responseCallback, final ErrorCallback errorCallback) {
        if (!isConnected.get()) {
            throw new RuntimeException("Internal CoapServer is not started.");
        }
        request.accept(requestPresender);
        requestSender.send(request, responseCallback, errorCallback);
    }

    @Override
    public List<LwM2mObjectEnabler> getObjectEnablers() {
        return objectEnablers;
    }
}
