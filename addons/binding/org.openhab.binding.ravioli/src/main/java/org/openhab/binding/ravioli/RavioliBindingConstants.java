/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.ravioli;

import org.eclipse.smarthome.core.thing.ThingTypeUID;

/**
 * The {@link RavioliBinding} class defines common constants, which are
 * used across the whole binding.
 *
 * @author RavioliTeam - Initial contribution
 */
public class RavioliBindingConstants {

    public static final String BINDING_ID = "ravioli";

    // List of all Thing Type UIDs
    public final static ThingTypeUID THING_TYPE_MEME = new ThingTypeUID(BINDING_ID, "meme");

    // List of all Channel ids
    public final static String CHANNEL_IMAGE = "image";
    public final static String CHANNEL_TEXT = "text";

}
