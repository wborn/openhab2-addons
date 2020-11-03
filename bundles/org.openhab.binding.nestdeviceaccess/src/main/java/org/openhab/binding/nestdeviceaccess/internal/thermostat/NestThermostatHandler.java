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

package org.openhab.binding.nestdeviceaccess.internal.thermostat;

import static org.openhab.binding.nestdeviceaccess.internal.nestdeviceaccessBindingConstants.*;

import java.io.IOException;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.nestdeviceaccess.internal.nestdeviceaccessConfiguration;
import org.openhab.binding.nestdeviceaccess.internal.nesthelper.NestUtility;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.binding.BaseThingHandler;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.openhab.core.types.State;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link NestThermostatHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Brian Higginbotham - Initial contribution
 */
@NonNullByDefault
public class NestThermostatHandler extends BaseThingHandler {

    NestUtility nestUtility = new NestUtility(thing);
    private final Logger logger = LoggerFactory.getLogger(NestThermostatHandler.class);

    private @NonNullByDefault({}) ScheduledFuture<?> refreshJob;
    private @Nullable nestdeviceaccessConfiguration config;
    NestThermostat nestThermostat;

    public NestThermostatHandler(Thing thing) {
        super(thing);
        nestThermostat = new NestThermostat(thing); // initialize thermostat with base properties
    }

    @Override
    public void dispose() {
        if (refreshJob != null) {
            refreshJob.cancel(true);
        }
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        logger.debug("handleCommand reporting {},{}", channelUID.getId(), command.toString());

        try {

            if (thermostatName.equals(channelUID.getId())) {

                if (command instanceof RefreshType) {
                    // TODO: handle data refresh

                    logger.debug("handleCommand reporting {}", command.toString());
                }

                // TODO: handle command

                // Note: if communication with thing fails for some reason,
                // indicate that by setting the status with detail information:
                // updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                // "Could not control device at IP address x.x.x.x");
            } else if (thermostatCurrentMode.equals(channelUID.getId())) {
                if (command.toString() != "REFRESH") {
                    if (nestThermostat.setThermostatMode(command.toString())) {
                        logger.info("Thermostat: {} set command {} to {}", thing.getProperties().get("deviceName"),
                                channelUID.toString(), command.toString());
                        Thread.sleep(3000); // set up to handle delay with changing mode and current settings being
                                            // updated
                        refreshChannels();
                    }
                }
            } else if (thermostatCurrentEcoMode.equals(channelUID.getId())) {
                if (command.toString() != "REFRESH") {
                    if (nestThermostat.setThermostatEcoMode(command.toString())) {
                        logger.info("Thermostat: {} set command {} to {}", thing.getProperties().get("deviceName"),
                                channelUID.toString(), command.toString());
                        Thread.sleep(3000); // set up to handle delay with changing mode and current settings being
                                            // updated
                        refreshChannels();
                    }
                }
            } else if (thermostatTargetTemperature.equals(channelUID.getId())) {
                if (command.toString() != "REFRESH") {
                    if (nestThermostat.setThermostatTargetTemperature(Double.parseDouble(command.toString()), 0, 0,
                            false)) {
                        logger.info("Thermostat: {} set command {} to {}", thing.getProperties().get("deviceName"),
                                channelUID.toString(), command.toString());
                        refreshChannels();
                    }
                }
            } else if (thermostatMinimumTemperature.equals(channelUID.getId())) {
                if (command.toString() != "REFRESH") {
                    if (nestThermostat.setThermostatTargetTemperature(0, Double.parseDouble(command.toString()),
                            nestThermostat.getMinMaxTemperature()[1], true)) {
                        logger.info("Thermostat: {} set command {} to {}", thing.getProperties().get("deviceName"),
                                channelUID.toString(), command.toString());
                        refreshChannels();
                    }
                }
            } else if (thermostatMaximumTemperature.equals(channelUID.getId())) {
                if (command.toString() != "REFRESH") {
                    if (nestThermostat.setThermostatTargetTemperature(0, nestThermostat.getMinMaxTemperature()[0],
                            Double.parseDouble(command.toString()), true)) {
                        logger.info("Thermostat: {} set command {} to {}", thing.getProperties().get("deviceName"),
                                channelUID.toString(), command.toString());
                        refreshChannels();
                    }
                }
            }
        } catch (IOException e) {
            logger.debug("handleMessage reporting exception {}", e.getMessage());
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            logger.debug("handleMessage reporting exception {}", e.getMessage());
        }
    }

    void refreshChannels() {
        logger.info("refreshChannels process a timer for Updating thing Channels for:{}", thing.getUID());

        try {
            // refresh status of the device

            if (nestThermostat.getThermostatInfo()) {
                State statusState = new StringType(nestThermostat.getDeviceStatus());
                updateState("thermostatStatus", statusState);
                State nameState = new StringType(nestThermostat.getDeviceName());
                updateState("thermostatName", nameState);
                State humidityState = new DecimalType(nestThermostat.getCurrentHumidity());
                updateState("thermostatHumidityPercent", humidityState);
                State ambientState = new DecimalType(nestThermostat.getAmbientTemperatureSetting());
                updateState("thermostatAmbientTemperature", ambientState);
                State setTempHeatState = new DecimalType(nestThermostat.getCurrentTemperatureHeat());
                updateState("thermostatTemperatureHeat", setTempHeatState);
                State setTempCoolState = new DecimalType(nestThermostat.getCurrentTemperatureCool());
                updateState("thermostatTemperatureCool", setTempCoolState);
                State setTargetTempState = new DecimalType(nestThermostat.getTargetTemperature());
                updateState("thermostatTargetTemperature", setTargetTempState);
                State setMinTempState = new DecimalType(nestThermostat.getMinMaxTemperature()[0]);
                updateState("thermostatMinimumTemperature", setMinTempState);
                State setMaxTempState = new DecimalType(nestThermostat.getMinMaxTemperature()[1]);
                updateState("thermostatMaximumTemperature", setMaxTempState);
                State modeState = new StringType(nestThermostat.getThermostatMode());
                updateState("thermostatCurrentMode", modeState);
                State ecoModeState = new StringType(nestThermostat.getThermostatEcoMode());
                updateState("thermostatCurrentEcoMode", ecoModeState);
                State scaleSettingState = new StringType(nestThermostat.getTemperatureScaleSetting());
                updateState("thermostatScaleSetting", scaleSettingState);

            }
        } catch (IOException e) {
            logger.debug("refreshChannels() reporting exception {}", e.getMessage());
        }
    }

    @Override
    public void initialize() {
        logger.debug("Start initializing!");
        config = getConfigAs(nestdeviceaccessConfiguration.class);
        config.projectId = thing.getProperties().get("projectId");
        config.clientId = thing.getProperties().get("clientId");
        config.clientSecret = thing.getProperties().get("clientSecret");
        config.accessToken = thing.getProperties().get("accessToken");
        config.refreshToken = thing.getProperties().get("refreshToken");
        config.deviceId = thing.getProperties().get("deviceId");
        config.deviceName = thing.getProperties().get("deviceName");
        config.refreshInterval = Integer.parseInt(thing.getConfiguration().get("refreshInterval").toString());

        // TODO: Initialize the handler.
        // The framework requires you to return from this method quickly. Also, before leaving this method a thing
        // status from one of ONLINE, OFFLINE or UNKNOWN must be set. This might already be the real thing status in
        // case you can decide it directly.
        // In case you can not decide the thing status directly (e.g. for long running connection handshake using WAN
        // access or similar) you should set status UNKNOWN here and then decide the real status asynchronously in the
        // background.

        // set the thing status to UNKNOWN temporarily and let the background task decide for the real status.
        // the framework is then able to reuse the resources from the thing handler initialization.
        // we set this upfront to reliably check status updates in unit tests.

        updateStatus(ThingStatus.UNKNOWN);

        // Example for background initialization:
        scheduler.execute(() -> {
            boolean thingReachable = true; // <background task with long running initialization here>
            try {

                nestThermostat.initializeThermostat();
                // NestUtility.pubSubEventHandler("openhab-nest-int-1601138253554", "sdm_pull_events");
                // when done do:
                thingReachable = nestThermostat.getDeviceStatus().equalsIgnoreCase("ONLINE");
                if (thingReachable) {
                    updateStatus(ThingStatus.ONLINE);
                } else {
                    updateStatus(ThingStatus.OFFLINE);
                }

            } catch (Exception e) {
                logger.debug("Initialize() caught an exception {}", e.getMessage());
            }
        });

        if (refreshJob == null || refreshJob.isCancelled()) {
            refreshJob = scheduler.scheduleWithFixedDelay(this::refreshChannels, 0, config.refreshInterval,
                    TimeUnit.SECONDS);
        }
        // Note: When initialization can NOT be done set the status with more details for further
        // analysis. See also class ThingStatusDetail for all available status details.
        // Add a description to give user information to understand why thing does not work as expected. E.g.
        // updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
        // "Can not access device as username and/or password are invalid");
    }
}
