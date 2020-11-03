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

import static org.openhab.binding.nestdeviceaccess.internal.nestdeviceaccessBindingConstants.*;

import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.nestdeviceaccess.internal.doorbell.NestDoorbellHandler;
import org.openhab.binding.nestdeviceaccess.internal.thermostat.NestThermostatHandler;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.binding.BaseThingHandlerFactory;
import org.openhab.core.thing.binding.ThingHandler;
import org.openhab.core.thing.binding.ThingHandlerFactory;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link nestdeviceaccessHandlerFactory} is responsible for creating things and thing
 * handlers.
 *
 * @author Brian Higginbotham - Initial contribution
 */
@NonNullByDefault
@Component(configurationPid = "binding.nestdeviceaccess", service = ThingHandlerFactory.class)
public class nestdeviceaccessHandlerFactory extends BaseThingHandlerFactory {
    private final Logger logger = LoggerFactory.getLogger(nestdeviceaccessHandlerFactory.class);

    private static final Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = Collections.unmodifiableSet(
            Stream.of(THING_TYPE_GENERIC, THING_TYPE_THERMOSTAT, THING_TYPE_DOORBELL).collect(Collectors.toSet()));

    @Override
    public boolean supportsThingType(ThingTypeUID thingTypeUID) {
        logger.info("supportsThingType reporting {}", thingTypeUID.toString());
        return SUPPORTED_THING_TYPES_UIDS.contains(thingTypeUID);
    }

    @Override
    protected @Nullable ThingHandler createHandler(Thing thing) {
        ThingTypeUID thingTypeUID = thing.getThingTypeUID();
        logger.info("createHandler reporting {}", thingTypeUID.toString());

        if (thingTypeUID.equals(THING_TYPE_GENERIC)) {
            logger.debug("createHandler reporting Generic..");
            return new nestdeviceaccessHandler(thing);
        }
        if (thingTypeUID.equals(THING_TYPE_THERMOSTAT)) {
            logger.debug("createHandler reporting Thermostat..");
            return new NestThermostatHandler(thing);
        }
        if (thingTypeUID.equals(THING_TYPE_DOORBELL)) {
            logger.debug("createHandler reporting Doorbell..");
            return new NestDoorbellHandler(thing);
        }
        logger.info("createHandler never should have come here.. Returning null");
        return null;
    }
}
