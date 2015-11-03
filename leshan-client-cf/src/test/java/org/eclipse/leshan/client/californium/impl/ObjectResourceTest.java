/*******************************************************************************
 * Copyright (c) 2015 Sierra Wireless and others.
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
package org.eclipse.leshan.client.californium.impl;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.eclipse.leshan.core.node.LwM2mPath;
import org.junit.Test;

public class ObjectResourceTest {

    @Test
    public void objectShouldNotifyItself() {
        shouldNotify("/3", "/3");
    }

    @Test
    public void objectShouldNotNotifyDifferentObject() {
        shouldNotNotify("/3", "/4");
    }

    @Test
    public void objectInstanceShouldNotifyItself() {
        shouldNotify("/3/0", "/3/0");
    }

    @Test
    public void objectShouldNotNotifyObjectInstace() {
        shouldNotNotify("/3/0", "/3");
    }

    @Test
    public void objectInstanceShouldNotNotifyDifferentObjectInstance() {
        shouldNotNotify("/3/0", "/3/1");
    }

    @Test
    public void objectInstanceShouldNotifyObject() {
        shouldNotify("/3", "/3/1");
    }

    @Test
    public void resourceShouldNotifyItself() {
        shouldNotify("/3/1/0", "/3/1/0");
    }

    @Test
    public void resourceShouldNotNotifyDifferentResource() {
        shouldNotNotify("/3/1/0", "/3/1/1");
    }

    @Test
    public void resourceShouldNotifyObjectInstace() {
        shouldNotify("/3/1", "/3/1/1");
    }

    @Test
    public void resourceShouldNotifyObject() {
        shouldNotify("/3", "/3/1/1");
    }

    private void shouldNotNotify(String observing, String notifying) {
        assertFalse(ObjectResource.shouldNotify(new LwM2mPath(observing), new LwM2mPath(notifying)));
    }

    private void shouldNotify(String observing, String notifying) {
        assertTrue(ObjectResource.shouldNotify(new LwM2mPath(observing), new LwM2mPath(notifying)));
    }

}
