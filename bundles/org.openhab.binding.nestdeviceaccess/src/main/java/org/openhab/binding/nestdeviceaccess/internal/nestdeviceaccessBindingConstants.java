/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.openhab.binding.nestdeviceaccess.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.thing.ThingTypeUID;

/**
 * The {@link nestdeviceaccessBindingConstants} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Brian Higginbotham - Initial contribution
 */
@NonNullByDefault
public class nestdeviceaccessBindingConstants {

    private static final String BINDING_ID = "nestdeviceaccess";

    // List of all Thing Type UIDs
    public static final ThingTypeUID THING_TYPE_GENERIC = new ThingTypeUID(BINDING_ID, "nest-device-access");
    public static final ThingTypeUID THING_TYPE_THERMOSTAT = new ThingTypeUID(BINDING_ID, "nest-device-thermostat");
    public static final ThingTypeUID THING_TYPE_DOORBELL = new ThingTypeUID(BINDING_ID, "nest-device-doorbell");
    // List of all Channel ids
    public static final String thermostatName = "thermostatName";
    public static final String thermostatCurrentMode = "thermostatCurrentMode";
    public static final String thermostatCurrentEcoMode = "thermostatCurrentEcoMode";
    public static final String thermostatTargetTemperature = "thermostatTargetTemperature";
    public static final String thermostatMinimumTemperature = "thermostatMinimumTemperature";
    public static final String thermostatMaximumTemperature = "thermostatMaximumTemperature";
    public static final String thermostatScaleSetting = "thermostatScaleSetting";
    public static final String doorbellName = "doorbellName";
}
