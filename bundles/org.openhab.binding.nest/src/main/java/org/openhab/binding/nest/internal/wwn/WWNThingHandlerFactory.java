/**
 * Copyright (c) 2010-2021 Contributors to the openHAB project
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
package org.openhab.binding.nest.internal.wwn;

import static org.openhab.binding.nest.internal.wwn.WWNBindingConstants.*;

import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.config.discovery.DiscoveryService;
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandlerFactory;
import org.eclipse.smarthome.core.thing.binding.ThingHandler;
import org.eclipse.smarthome.core.thing.binding.ThingHandlerFactory;
import org.openhab.binding.nest.internal.wwn.discovery.WWNDiscoveryService;
import org.openhab.binding.nest.internal.wwn.handler.WWNAccountHandler;
import org.openhab.binding.nest.internal.wwn.handler.WWNCameraHandler;
import org.openhab.binding.nest.internal.wwn.handler.WWNSmokeDetectorHandler;
import org.openhab.binding.nest.internal.wwn.handler.WWNStructureHandler;
import org.openhab.binding.nest.internal.wwn.handler.WWNThermostatHandler;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.component.annotations.Component;

/**
 * The {@link WWNThingHandlerFactory} is responsible for creating WWN thing handlers.
 *
 * @author David Bennett - Initial contribution
 */
@NonNullByDefault
@Component(service = ThingHandlerFactory.class, configurationPid = "binding.nest")
public class WWNThingHandlerFactory extends BaseThingHandlerFactory {

    private final Map<ThingUID, ServiceRegistration<?>> discoveryService = new HashMap<>();

    /**
     * The things this factory supports creating.
     */
    @Override
    public boolean supportsThingType(ThingTypeUID thingTypeUID) {
        return SUPPORTED_THING_TYPES_UIDS.contains(thingTypeUID);
    }

    /**
     * Creates a handler for the specific thing. This also creates the discovery service when the bridge is created.
     */
    @Override
    protected @Nullable ThingHandler createHandler(Thing thing) {
        ThingTypeUID thingTypeUID = thing.getThingTypeUID();

        if (THING_TYPE_ACCOUNT.equals(thingTypeUID)) {
            WWNAccountHandler handler = new WWNAccountHandler((Bridge) thing);
            WWNDiscoveryService service = new WWNDiscoveryService(handler);
            service.activate();
            discoveryService.put(handler.getThing().getUID(),
                    bundleContext.registerService(DiscoveryService.class.getName(), service, new Hashtable<>()));
            return handler;
        } else if (THING_TYPE_CAMERA.equals(thingTypeUID)) {
            return new WWNCameraHandler(thing);
        } else if (THING_TYPE_SMOKE_DETECTOR.equals(thingTypeUID)) {
            return new WWNSmokeDetectorHandler(thing);
        } else if (THING_TYPE_STRUCTURE.equals(thingTypeUID)) {
            return new WWNStructureHandler(thing);
        } else if (THING_TYPE_THERMOSTAT.equals(thingTypeUID)) {
            return new WWNThermostatHandler(thing);
        }

        return null;
    }

    @Override
    protected void removeHandler(ThingHandler thingHandler) {
        if (thingHandler instanceof WWNAccountHandler) {
            ServiceRegistration<?> reg = discoveryService.get(thingHandler.getThing().getUID());
            if (reg != null) {
                // Unregister the discovery service.
                WWNDiscoveryService service = (WWNDiscoveryService) bundleContext.getService(reg.getReference());
                service.deactivate();
                reg.unregister();
                discoveryService.remove(thingHandler.getThing().getUID());
            }
        }
        super.removeHandler(thingHandler);
    }
}