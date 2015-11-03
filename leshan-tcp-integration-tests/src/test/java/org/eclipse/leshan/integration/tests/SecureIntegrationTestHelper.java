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

import javax.xml.bind.DatatypeConverter;

public class SecureIntegrationTestHelper extends IntegrationTestHelper {

    public final String pskIdentity = "Client_identity";
    public final byte[] pskKey = DatatypeConverter.parseHexBinary("73656372657450534b");
    public final PublicKey clientPublicKey;
    public final PrivateKey clientPrivateKey;
    public final PublicKey serverPublicKey;
    public final PrivateKey serverPrivateKey;

    /**
     * @TODO NEEDS to be rewritten using TLS
     */
    public SecureIntegrationTestHelper() {
//        // create client credentials
        try {
            // Get point values
            final byte[] publicX = DatatypeConverter
                    .parseHexBinary("89c048261979208666f2bfb188be1968fc9021c416ce12828c06f4e314c167b5");
            final byte[] publicY = DatatypeConverter
                    .parseHexBinary("cbf1eb7587f08e01688d9ada4be859137ca49f79394bad9179326b3090967b68");
            final byte[] privateS = DatatypeConverter
                    .parseHexBinary("e67b68d2aaeb6550f19d98cade3ad62b39532e02e6b422e1f7ea189dabaea5d2");

            // Get Elliptic Curve Parameter spec for secp256r1
            final AlgorithmParameters algoParameters = AlgorithmParameters.getInstance("EC");
            algoParameters.init(new ECGenParameterSpec("secp256r1"));
            final ECParameterSpec parameterSpec = algoParameters.getParameterSpec(ECParameterSpec.class);

            // Create key specs
            final KeySpec publicKeySpec = new ECPublicKeySpec(new ECPoint(new BigInteger(publicX), new BigInteger(
                    publicY)), parameterSpec);
            final KeySpec privateKeySpec = new ECPrivateKeySpec(new BigInteger(privateS), parameterSpec);

            // Get keys
            clientPublicKey = KeyFactory.getInstance("EC").generatePublic(publicKeySpec);
            clientPrivateKey = KeyFactory.getInstance("EC").generatePrivate(privateKeySpec);
        } catch (InvalidKeySpecException | NoSuchAlgorithmException | InvalidParameterSpecException e) {
            throw new RuntimeException(e);
        }

        // create server credentials
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
            final KeySpec publicKeySpec = new ECPublicKeySpec(new ECPoint(new BigInteger(publicX), new BigInteger(
                    publicY)), parameterSpec);
            final KeySpec privateKeySpec = new ECPrivateKeySpec(new BigInteger(privateS), parameterSpec);

            // Get keys
            serverPublicKey = KeyFactory.getInstance("EC").generatePublic(publicKeySpec);
            serverPrivateKey = KeyFactory.getInstance("EC").generatePrivate(privateKeySpec);
        } catch (InvalidKeySpecException | NoSuchAlgorithmException | InvalidParameterSpecException e) {
            throw new RuntimeException(e);
        }
//    }
//
//    public void createPSKClient() {
//        final LeshanClientBuilder builder = new LeshanClientBuilder();
//        client = builder.setServerAddress(getServerSecureAddress()).setPskSecurity(pskIdentity, pskKey)
//                .setBindingMode(BindingMode.T).build(2, 3);
//    }
//
//    public void createRPKClient() {
//        final LeshanClientBuilder builder = new LeshanClientBuilder();
//        client = builder.setServerAddress(getServerSecureAddress()).setRpkSecurity(clientPrivateKey, clientPublicKey)
//                .setBindingMode(BindingMode.T).build(2, 3);
//    }
//
//    public void createPSKandRPKClient() {
//        final LeshanClientBuilder builder = new LeshanClientBuilder();
//        client = builder.setServerAddress(getServerSecureAddress()).setPskSecurity(pskIdentity, pskKey)
//                .setRpkSecurity(clientPrivateKey, clientPublicKey).setBindingMode(BindingMode.T).build(2, 3);
//    }
//
//    public void createServerWithRPK() {
//        final LeshanServerBuilder builder = new LeshanServerBuilder();
//        builder.setLocalAddress(new InetSocketAddress(InetAddress.getLoopbackAddress(), 0));
//        builder.setLocalAddressSecure(new InetSocketAddress(InetAddress.getLoopbackAddress(), 0));
//        builder.setSecurityRegistry(new SecurityRegistryImpl(serverPrivateKey, serverPublicKey) {
//            // TODO we should separate SecurityRegistryImpl in 2 registries :
//            // InMemorySecurityRegistry and PersistentSecurityRegistry
//            @Override
//            protected void loadFromFile() {
//                // do not load From File
//            }
//
//            @Override
//            protected void saveToFile() {
//                // do not save to file
//            }
//        });
//
//        server = builder.build();
    }
}
