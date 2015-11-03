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

import java.util.EnumSet;

/**
 * Transport binding and Queue Mode
 */
public enum BindingMode {

	/** UDP */
	U,

	/** SMS */
	S,

	/** Queue Mode */
	Q,

	/** TCP Server */
	T,

	/** TCP Client */
	C;

	public static EnumSet<BindingMode> getBindingMode(final BindingMode... bindingModeList) {
		final EnumSet<BindingMode> bindingModes = EnumSet.noneOf(BindingMode.class);
		for(final BindingMode bMode : bindingModeList) {
			bindingModes.add(bMode);
		}
		return bindingModes;
	}

	public static String setToString(final EnumSet<BindingMode> bindingModes) {
		final StringBuilder sb = new StringBuilder(); 
		for(final BindingMode bindingMode : bindingModes) {
			sb.append(bindingMode);
		}
		return sb.toString();
	}

	public static EnumSet<BindingMode> parseFromString(final String bindingModeString) {
		final EnumSet<BindingMode> bindingModes = EnumSet.noneOf(BindingMode.class);
		if(bindingModeString != null && !bindingModeString.isEmpty()) {
			final String[] bindingComponent = bindingModeString.split("");
			for(final String bm : bindingComponent) {
				if(!bm.isEmpty()) {
					bindingModes.add(BindingMode.valueOf(bm));
				}
			}
		}
		return bindingModes;
	}
}
