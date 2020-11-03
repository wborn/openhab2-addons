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
package org.openhab.binding.nestdeviceaccess.internal.doorbell;

import java.io.IOException;

import org.json.JSONArray;
import org.json.JSONObject;
import org.openhab.binding.nestdeviceaccess.internal.nesthelper.NestUtility;
import org.openhab.core.thing.Thing;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link NestDoorbell} is responsible for handling all attributes and features of a Nest Doorbell
 *
 * @author Brian Higginbotham - Initial contribution
 */
public class NestDoorbell {

    public NestDoorbell(Thing thing) {
        if (thing != null) {
            this.thing = thing;
            nestUtility = new NestUtility(this.thing);
        }
    }

    Thing thing;
    NestUtility nestUtility;

    private final Logger logger = LoggerFactory.getLogger(NestDoorbell.class);

    // Thermostat properties
    public String deviceName;
    public String deviceType;
    public String deviceCustomName;
    public String deviceStatus;
    public int[] deviceMaxImageResolution;
    public int[] deviceMaxVideoResolution;
    public String[] deviceVideoResolution;
    public String[] deviceParentRelations;
    public String[] deviceVideoCodecs;
    public String[] deviceAudioCodecs;
    public String[] deviceSupportedProtocols;

    public boolean parseDoorbellInfo(String jsonContent) {
        JSONObject jo = new JSONObject(jsonContent);

        deviceParentRelations = new String[2];
        deviceType = jo.getString("type");

        if (jo.getJSONObject("traits").getJSONObject("sdm.devices.traits.Info").has("customName")) {
            deviceCustomName = jo.getJSONObject("traits").getJSONObject("sdm.devices.traits.Info")
                    .getString("customName");
        }

        if (jo.getJSONObject("traits").getJSONObject("sdm.devices.traits.CameraLiveStream").has("maxVideoResolution")) {
            deviceMaxVideoResolution = new int[2];

            deviceMaxVideoResolution[0] = jo.getJSONObject("traits")
                    .getJSONObject("sdm.devices.traits.CameraLiveStream").getJSONObject("maxVideoResolution")
                    .getInt("width");
            deviceMaxVideoResolution[1] = jo.getJSONObject("traits")
                    .getJSONObject("sdm.devices.traits.CameraLiveStream").getJSONObject("maxVideoResolution")
                    .getInt("height");
        }

        if (jo.getJSONObject("traits").getJSONObject("sdm.devices.traits.CameraLiveStream").has("videoCodecs")) {
            JSONArray jaVideoCodecs = jo.getJSONObject("traits").getJSONObject("sdm.devices.traits.CameraLiveStream")
                    .getJSONArray("videoCodecs");
            deviceVideoCodecs = new String[jaVideoCodecs.length()];
            for (int nCount = 0; nCount < jaVideoCodecs.length(); nCount++) {
                deviceVideoCodecs[nCount] = jaVideoCodecs.getString(nCount);
            }
        }

        if (jo.getJSONObject("traits").getJSONObject("sdm.devices.traits.CameraLiveStream").has("audioCodecs")) {
            JSONArray jaAudioCodecs = jo.getJSONObject("traits").getJSONObject("sdm.devices.traits.CameraLiveStream")
                    .getJSONArray("audioCodecs");
            deviceAudioCodecs = new String[jaAudioCodecs.length()];
            for (int nCount = 0; nCount < jaAudioCodecs.length(); nCount++) {
                deviceVideoCodecs[nCount] = jaAudioCodecs.getString(nCount);
            }
        }

        if (jo.getJSONObject("traits").getJSONObject("sdm.devices.traits.CameraLiveStream").has("supportedProtocols")) {
            JSONArray jaSupportedProtocols = jo.getJSONObject("traits")
                    .getJSONObject("sdm.devices.traits.CameraLiveStream").getJSONArray("supportedProtocols");
            deviceSupportedProtocols = new String[jaSupportedProtocols.length()];
            for (int nCount = 0; nCount < jaSupportedProtocols.length(); nCount++) {
                deviceVideoCodecs[nCount] = jaSupportedProtocols.getString(nCount);
            }
        }

        if (jo.getJSONObject("traits").getJSONObject("sdm.devices.traits.CameraLiveStream").has("maxImageResolution")) {
            deviceMaxImageResolution = new int[2];
            deviceMaxImageResolution[0] = jo.getJSONObject("traits")
                    .getJSONObject("sdm.devices.traits.CameraLiveStream").getJSONObject("maxImageResolution")
                    .getInt("width");
            deviceMaxImageResolution[1] = jo.getJSONObject("traits")
                    .getJSONObject("sdm.devices.traits.CameraLiveStream").getJSONObject("maxImageResolution")
                    .getInt("height");
        }

        JSONArray jaParentRelations = jo.getJSONArray("parentRelations");

        for (int nCount = 0; nCount < jaParentRelations.length(); nCount++) {
            // get Available Modes
            deviceParentRelations[0] = jaParentRelations.getJSONObject(nCount).getString("parent");
            deviceParentRelations[1] = jaParentRelations.getJSONObject(nCount).getString("displayName");
            break;
        }
        deviceName = deviceParentRelations[1];

        return (true);
    }

    public boolean getDevices() throws IOException {
        try {
            String url = "https://smartdevicemanagement.googleapis.com/v1/enterprises/"
                    + thing.getProperties().get("projectId") + "/devices";
            nestUtility.getDeviceInfo(url);
            return (true);
        } catch (IOException e) {
            throw new IOException(e.getMessage());
        }
    }

    public boolean initializeDoorbell() throws IOException {
        return (getDevices());
    }

    public int[] getMaxImageResolution() {
        return (deviceMaxImageResolution);
    }

    public String[] getVideoCodecs() {
        return (deviceVideoCodecs);
    }

    public String[] getAudioCodecs() {
        return (deviceAudioCodecs);
    }

    public String[] getSupportedProtocols() {
        return (deviceSupportedProtocols);
    }

    public int[] getMaxVideoResolution() {
        return (deviceMaxVideoResolution);
    }

    public String getDeviceName() {
        return (deviceName);
    }

    public String getCustomName() {
        return (deviceCustomName);
    }

    public boolean getDoorbellInfo() throws IOException {
        try {
            String jsonContent;
            jsonContent = nestUtility.getDeviceInfo("https://smartdevicemanagement.googleapis.com/v1/enterprises/"
                    + thing.getProperties().get("projectId") + "/devices/" + thing.getProperties().get("deviceId"));

            return (parseDoorbellInfo(jsonContent));

        } catch (IOException e) {
            throw new IOException(e.getMessage());
        }
    }
}
