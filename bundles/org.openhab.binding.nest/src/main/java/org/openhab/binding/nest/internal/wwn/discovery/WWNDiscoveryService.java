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
package org.openhab.binding.nest.internal.wwn.discovery;

import static org.eclipse.smarthome.core.thing.Thing.PROPERTY_FIRMWARE_VERSION;
import static org.openhab.binding.nest.internal.wwn.WWNBindingConstants.*;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.smarthome.config.discovery.AbstractDiscoveryService;
import org.eclipse.smarthome.config.discovery.DiscoveryResultBuilder;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.openhab.binding.nest.internal.wwn.config.WWNDeviceConfiguration;
import org.openhab.binding.nest.internal.wwn.config.WWNStructureConfiguration;
import org.openhab.binding.nest.internal.wwn.dto.BaseWWNDevice;
import org.openhab.binding.nest.internal.wwn.dto.WWNCamera;
import org.openhab.binding.nest.internal.wwn.dto.WWNSmokeDetector;
import org.openhab.binding.nest.internal.wwn.dto.WWNStructure;
import org.openhab.binding.nest.internal.wwn.dto.WWNThermostat;
import org.openhab.binding.nest.internal.wwn.handler.WWNAccountHandler;
import org.openhab.binding.nest.internal.wwn.listener.WWNThingDataListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This service connects to the Nest account and creates the correct discovery results for devices
 * as they are found through the WWN API.
 *
 * @author David Bennett - Initial contribution
 * @author Wouter Born - Add representation properties
 */
@NonNullByDefault
public class WWNDiscoveryService extends AbstractDiscoveryService {

    private static final Set<ThingTypeUID> SUPPORTED_THING_TYPES = Stream
            .of(THING_TYPE_CAMERA, THING_TYPE_THERMOSTAT, THING_TYPE_SMOKE_DETECTOR, THING_TYPE_STRUCTURE)
            .collect(Collectors.toSet());

    private final Logger logger = LoggerFactory.getLogger(WWNDiscoveryService.class);

    private final DiscoveryDataListener<WWNCamera> cameraDiscoveryDataListener = new DiscoveryDataListener<>(
            WWNCamera.class, THING_TYPE_CAMERA, this::addDeviceDiscoveryResult);
    private final DiscoveryDataListener<WWNSmokeDetector> smokeDetectorDiscoveryDataListener = new DiscoveryDataListener<>(
            WWNSmokeDetector.class, THING_TYPE_SMOKE_DETECTOR, this::addDeviceDiscoveryResult);
    private final DiscoveryDataListener<WWNStructure> structureDiscoveryDataListener = new DiscoveryDataListener<>(
            WWNStructure.class, THING_TYPE_STRUCTURE, this::addStructureDiscoveryResult);
    private final DiscoveryDataListener<WWNThermostat> thermostatDiscoveryDataListener = new DiscoveryDataListener<>(
            WWNThermostat.class, THING_TYPE_THERMOSTAT, this::addDeviceDiscoveryResult);

    @SuppressWarnings("rawtypes")
    private final List<DiscoveryDataListener> discoveryDataListeners = Arrays.asList(cameraDiscoveryDataListener,
            smokeDetectorDiscoveryDataListener, structureDiscoveryDataListener, thermostatDiscoveryDataListener);

    private final WWNAccountHandler accountHandler;

    private static class DiscoveryDataListener<T> implements WWNThingDataListener<T> {
        private Class<T> dataClass;
        private ThingTypeUID thingTypeUID;
        private BiConsumer<T, ThingTypeUID> onDiscovered;

        private DiscoveryDataListener(Class<T> dataClass, ThingTypeUID thingTypeUID,
                BiConsumer<T, ThingTypeUID> onDiscovered) {
            this.dataClass = dataClass;
            this.thingTypeUID = thingTypeUID;
            this.onDiscovered = onDiscovered;
        }

        @Override
        public void onNewData(T data) {
            onDiscovered.accept(data, thingTypeUID);
        }

        @Override
        public void onUpdatedData(T oldData, T data) {
        }

        @Override
        public void onMissingData(String nestId) {
        }
    }

    public WWNDiscoveryService(WWNAccountHandler accountHandler) {
        super(SUPPORTED_THING_TYPES, 60, true);
        this.accountHandler = accountHandler;
    }

    @SuppressWarnings("unchecked")
    public void activate() {
        discoveryDataListeners.forEach(listener -> accountHandler.addThingDataListener(listener.dataClass, listener));
        addDiscoveryResultsFromLastUpdates();
    }

    @Override
    @SuppressWarnings("unchecked")
    public void deactivate() {
        discoveryDataListeners
                .forEach(listener -> accountHandler.removeThingDataListener(listener.dataClass, listener));
    }

    @Override
    protected void startScan() {
        addDiscoveryResultsFromLastUpdates();
    }

    @SuppressWarnings("unchecked")
    private void addDiscoveryResultsFromLastUpdates() {
        discoveryDataListeners.forEach(listener -> addDiscoveryResultsFromLastUpdates(listener.dataClass,
                listener.thingTypeUID, listener.onDiscovered));
    }

    private <T> void addDiscoveryResultsFromLastUpdates(Class<T> dataClass, ThingTypeUID thingTypeUID,
            BiConsumer<T, ThingTypeUID> onDiscovered) {
        List<T> lastUpdates = accountHandler.getLastUpdates(dataClass);
        lastUpdates.forEach(lastUpdate -> onDiscovered.accept(lastUpdate, thingTypeUID));
    }

    private void addDeviceDiscoveryResult(BaseWWNDevice device, ThingTypeUID typeUID) {
        ThingUID bridgeUID = accountHandler.getThing().getUID();
        ThingUID thingUID = new ThingUID(typeUID, bridgeUID, device.getDeviceId());
        logger.debug("Discovered {}", thingUID);

        Map<String, Object> properties = new HashMap<>();
        properties.put(WWNDeviceConfiguration.DEVICE_ID, device.getDeviceId());
        properties.put(PROPERTY_FIRMWARE_VERSION, device.getSoftwareVersion());

        thingDiscovered(DiscoveryResultBuilder.create(thingUID) //
                .withThingType(typeUID) //
                .withLabel(device.getNameLong()) //
                .withBridge(bridgeUID) //
                .withProperties(properties) //
                .withRepresentationProperty(WWNDeviceConfiguration.DEVICE_ID) //
                .build() //
        );
    }

    public void addStructureDiscoveryResult(WWNStructure structure, ThingTypeUID typeUID) {
        ThingUID bridgeUID = accountHandler.getThing().getUID();
        ThingUID thingUID = new ThingUID(typeUID, bridgeUID, structure.getStructureId());
        logger.debug("Discovered {}", thingUID);
        Map<String, Object> properties = Collections.singletonMap(WWNStructureConfiguration.STRUCTURE_ID,
                structure.getStructureId());
        thingDiscovered(DiscoveryResultBuilder.create(thingUID) //
                .withThingType(THING_TYPE_STRUCTURE) //
                .withLabel(structure.getName()) //
                .withBridge(bridgeUID) //
                .withProperties(properties) //
                .withRepresentationProperty(WWNStructureConfiguration.STRUCTURE_ID) //
                .build() //
        );
    }
}
