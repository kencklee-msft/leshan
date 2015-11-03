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
package org.eclipse.leshan.core.request;

import java.net.InetAddress;
import java.util.Date;
import java.util.EnumSet;

import org.eclipse.leshan.LinkObject;
import org.eclipse.leshan.core.response.LwM2mResponse;

public class UpdateRequest implements UplinkRequest<LwM2mResponse> {

    private final InetAddress address;
    private final Integer port;
    private final Long lifeTimeInSec;
    private final String smsNumber;
    private final EnumSet<BindingMode> bindingModes;
    private final String registrationId;
    private final LinkObject[] objectLinks;

    public UpdateRequest(final String registrationId, final InetAddress address, final Integer port) {
        this(registrationId, address, port, null, null, null, null);
    }

    public UpdateRequest(final String registrationId, final InetAddress address, final Integer port, final Long lifetime, final String smsNumber,
    		final EnumSet<BindingMode> bindingModes, final LinkObject[] objectLinks) {
        this(registrationId, address, port, lifetime, smsNumber, bindingModes, objectLinks, null);
    }

    public UpdateRequest(final String registrationId, final Long lifetime, final String smsNumber, final EnumSet<BindingMode> bindingModes,
            final LinkObject[] objectLinks) {
        this(registrationId, null, null, lifetime, smsNumber, bindingModes, objectLinks, null);
    }

    /**
     * Sets all fields.
     * 
     * @param registrationId the ID under which the client is registered
     * @param address the client's host name or IP address
     * @param port the UDP port the client uses for communication
     * @param lifetime the number of seconds the client would like its registration to be valid
     * @param smsNumber the SMS number the client can receive messages under
     * @param bindingModes the binding mode(s) the client supports
     * @param objectLinks the objects and object instances the client hosts/supports
     * @param registrationDate the point in time the client registered with the server (?)
     * @throws NullPointerException if the registration ID is <code>null</code>
     */
    public UpdateRequest(final String registrationId, final InetAddress address, final Integer port, final Long lifetime, final String smsNumber,
    		final EnumSet<BindingMode> bindingModes, final LinkObject[] objectLinks, final Date registrationDate) {

        if (registrationId == null) {
            throw new NullPointerException("Registration ID must not be null");
        }
        this.registrationId = registrationId;
        this.address = address;
        this.port = port;
        this.objectLinks = objectLinks;
        this.lifeTimeInSec = lifetime;
        this.bindingModes = bindingModes;
        this.smsNumber = smsNumber;
    }

    public String getRegistrationId() {
        return registrationId;
    }

    public InetAddress getAddress() {
        return address;
    }

    public Integer getPort() {
        return port;
    }

    public LinkObject[] getObjectLinks() {
        return objectLinks;
    }

    public Long getLifeTimeInSec() {
        return lifeTimeInSec;
    }

    public String getSmsNumber() {
        return smsNumber;
    }

    public EnumSet<BindingMode> getBindingMode() {
        return bindingModes;
    }

    @Override
    public void accept(final UplinkRequestVisitor visitor) {
        visitor.visit(this);
    }
}
