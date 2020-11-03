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

/**
 * The {@link nestdeviceaccessConfiguration} class contains fields mapping thing configuration parameters.
 *
 * @author Brian Higginbotham - Initial contribution
 */
public class nestdeviceaccessConfiguration {

    /**
     * Sample configuration parameter. Replace with your own.
     */
    public String projectId;
    public String clientId;
    public String clientSecret;
    public String authorizationToken;
    public String accessToken;
    public String refreshToken;
    public String accessTokenExpiresIn;
    public String deviceId;
    public String deviceName;
    public int refreshInterval;
}
