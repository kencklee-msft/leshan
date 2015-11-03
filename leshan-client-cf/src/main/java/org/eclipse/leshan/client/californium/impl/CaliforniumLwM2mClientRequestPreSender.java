package org.eclipse.leshan.client.californium.impl;

import java.util.Set;

import org.eclipse.leshan.client.californium.LeshanClient;
import org.eclipse.leshan.core.request.BootstrapRequest;
import org.eclipse.leshan.core.request.DeregisterRequest;
import org.eclipse.leshan.core.request.RegisterRequest;
import org.eclipse.leshan.core.request.UpdateRequest;
import org.eclipse.leshan.core.request.UplinkRequestVisitor;

public class CaliforniumLwM2mClientRequestPreSender implements UplinkRequestVisitor {

    private final Set<ObjectResource> clientObjects;
    private final LeshanClient client;

    public CaliforniumLwM2mClientRequestPreSender(final Set<ObjectResource> clientObjects, final LeshanClient client) {
        this.clientObjects = clientObjects;
        this.client = client;
    }

    @Override
    public void visit(final RegisterRequest request) {
    }

    @Override
    public void visit(final UpdateRequest request) {
    }

    @Override
    public void visit(final DeregisterRequest request) {
        for (final ObjectResource clientObject : clientObjects) {
            clientObject.removeObserverRelations();
        }
    }

    @Override
    public void visit(final BootstrapRequest request) {
    }

}
