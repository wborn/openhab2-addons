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
package org.openhab.binding.nest.internal.sdm;

import static org.openhab.binding.nest.internal.sdm.SDMBindingConstants.*;

import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.config.discovery.DiscoveryService;
import org.eclipse.smarthome.core.auth.client.oauth2.OAuthFactory;
import org.eclipse.smarthome.core.i18n.TimeZoneProvider;
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandlerFactory;
import org.eclipse.smarthome.core.thing.binding.ThingHandler;
import org.eclipse.smarthome.core.thing.binding.ThingHandlerFactory;
import org.eclipse.smarthome.io.net.http.HttpClientFactory;
import org.openhab.binding.nest.internal.sdm.discovery.SDMDiscoveryService;
import org.openhab.binding.nest.internal.sdm.handler.SDMAccountHandler;
import org.openhab.binding.nest.internal.sdm.handler.SDMCameraHandler;
import org.openhab.binding.nest.internal.sdm.handler.SDMThermostatHandler;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * The {@link SDMThingHandlerFactory} is responsible for creating SDM thing handlers.
 *
 * @author Brian Higginbotham - Initial contribution
 * @author Wouter Born - Initial contribution
 */
@Component(service = ThingHandlerFactory.class, configurationPid = "binding.nest")
@NonNullByDefault
public class SDMThingHandlerFactory extends BaseThingHandlerFactory {

    private final Map<ThingUID, ServiceRegistration<?>> discoveryService = new HashMap<>();
    private HttpClientFactory httpClientFactory;
    private OAuthFactory oAuthFactory;
    private final TimeZoneProvider timeZoneProvider;

    @Activate
    public SDMThingHandlerFactory(final @Reference HttpClientFactory httpClientFactory,
            final @Reference OAuthFactory oAuthFactory, final @Reference TimeZoneProvider timeZoneProvider) {
        this.httpClientFactory = httpClientFactory;
        this.oAuthFactory = oAuthFactory;
        this.timeZoneProvider = timeZoneProvider;
    }

    @Override
    public boolean supportsThingType(ThingTypeUID thingTypeUID) {
        return SUPPORTED_THING_TYPES_UIDS.contains(thingTypeUID);
    }

    @Override
    protected @Nullable ThingHandler createHandler(Thing thing) {
        ThingTypeUID thingTypeUID = thing.getThingTypeUID();

        if (thingTypeUID.equals(THING_TYPE_ACCOUNT)) {
            SDMAccountHandler handler = new SDMAccountHandler((Bridge) thing, httpClientFactory, oAuthFactory);
            SDMDiscoveryService service = new SDMDiscoveryService(handler);
            discoveryService.put(handler.getThing().getUID(),
                    bundleContext.registerService(DiscoveryService.class.getName(), service, new Hashtable<>()));
            return handler;
        } else if (thingTypeUID.equals(THING_TYPE_CAMERA)) {
            return new SDMCameraHandler(thing, timeZoneProvider);
        } else if (thingTypeUID.equals(THING_TYPE_DISPLAY)) {
            return new SDMCameraHandler(thing, timeZoneProvider);
        } else if (thingTypeUID.equals(THING_TYPE_DOORBELL)) {
            return new SDMCameraHandler(thing, timeZoneProvider);
        } else if (thingTypeUID.equals(THING_TYPE_THERMOSTAT)) {
            return new SDMThermostatHandler(thing, timeZoneProvider);
        }

        return null;
    }

    @Override
    protected void removeHandler(ThingHandler thingHandler) {
        if (thingHandler instanceof SDMAccountHandler) {
            ServiceRegistration<?> reg = discoveryService.get(thingHandler.getThing().getUID());
            if (reg != null) {
                SDMDiscoveryService service = (SDMDiscoveryService) bundleContext.getService(reg.getReference());
                service.deactivate();
                reg.unregister();
                discoveryService.remove(thingHandler.getThing().getUID());
            }
        }
        super.removeHandler(thingHandler);
    }
}
