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
package org.openhab.binding.nestdeviceaccess.internal.nesthelper;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import org.openhab.core.thing.Thing;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.api.client.auth.oauth2.AuthorizationCodeTokenRequest;
import com.google.api.client.auth.oauth2.TokenResponse;
import com.google.api.client.auth.oauth2.TokenResponseException;
import com.google.api.client.googleapis.auth.oauth2.GoogleRefreshTokenRequest;
import com.google.api.client.http.BasicAuthentication;
import com.google.api.client.http.ByteArrayContent;
import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpResponse;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.auth.oauth2.AccessToken;
import com.google.common.net.HttpHeaders;

/**
 * The {@link NestUtility} is general utility class to help with all Nest SDM APIs
 *
 * @author Brian Higginbotham - Initial contribution
 */
public class NestUtility {

    public NestUtility(Thing thing) {
        if (thing != null) {
            this.thing = thing;
            deviceId = this.thing.getProperties().get("deviceId");
            clientId = this.thing.getProperties().get("clientId");
            clientSecret = this.thing.getProperties().get("clientSecret");
            projectId = this.thing.getProperties().get("projectId");
            refreshToken = this.thing.getProperties().get("refreshToken");
            accessToken = this.thing.getProperties().get("accessToken");
            accessTokenExpiration = this.thing.getProperties().get("accessTokenExpiration");

            SimpleDateFormat format = new SimpleDateFormat("E MMM dd HH:mm:ss zzz yyyy");
            try {
                if (accessTokenExpiration != null) {
                    Date date = format.parse(accessTokenExpiration);
                    googleAccessToken = new AccessToken(accessToken, date);
                } else {
                    googleAccessToken = refreshAccessToken(refreshToken, clientId, clientSecret);
                    thing.setProperty("accessTokenExpiration", googleAccessToken.getExpirationTime().toString());
                }
            } catch (ParseException e) {
                logger.debug("NestUtility constructor failed to parse date {}", e.getMessage());
            } catch (IOException e) {
                logger.debug("NestUtility constructor failed with exception {}", e.getMessage());
            }
        }
    }

    public NestUtility(String projectId, String clientId, String clientSecret, String refreshToken,
            AccessToken accessToken) {
        // secondary constructor for null thing and discovery service/non-basehandlers
        if (thing == null) {
            this.projectId = projectId;
            this.clientId = clientId;
            this.clientSecret = clientSecret;
            this.refreshToken = refreshToken;
            NestUtility.googleAccessToken = accessToken;
        }
    }

    Thing thing;

    private final static Logger logger = LoggerFactory.getLogger(NestUtility.class);

    private String deviceId;
    private String clientId;
    private String clientSecret;
    private String projectId;
    private String refreshToken;
    private String accessToken;
    private String accessTokenExpiration;
    private long accessTokenExpiresIn;
    private static AccessToken googleAccessToken;

    // helper local vars. Will set global properties file

    public AccessToken getAccessToken() throws IOException {
        try {
            if (isAccessTokenExpired()) {
                // we need to refresh the access token
                return (refreshAccessToken(refreshToken, clientId, clientSecret));
            } else {
                return (googleAccessToken);
            }
        } catch (IOException e) {
            throw new IOException(e.getMessage());
        }
    }

    public AccessToken setAccessToken(String accessToken, int accessTokenExpiresIn) {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.SECOND, accessTokenExpiresIn);
        googleAccessToken = new AccessToken(accessToken, calendar.getTime());
        return (googleAccessToken);
    }

    public boolean isAccessTokenExpired() {

        if (googleAccessToken.getExpirationTime().compareTo(Calendar.getInstance().getTime()) < 0) {
            return (true);
        } else {
            return (false);
        }
    }

    public String deviceExecuteCommand(String deviceId, String projectId, String accessToken, String requestBody)
            throws IOException {

        try {
            HttpTransport transport = new NetHttpTransport();
            HttpRequest request = transport.createRequestFactory().buildPostRequest(
                    new GenericUrl("https://smartdevicemanagement.googleapis.com/v1/enterprises/" + projectId
                            + "/devices/" + deviceId + ":executeCommand"),
                    ByteArrayContent.fromString("application/json", requestBody));
            request.getHeaders().set(HttpHeaders.CONTENT_TYPE, "application/json");
            request.getHeaders().set(HttpHeaders.AUTHORIZATION, "Bearer " + getAccessToken().getTokenValue());

            HttpResponse response = request.execute();
            return (convertStreamtoString(response.getContent()));
        } catch (IOException e) {
            // int statusCode = Integer.parseInt(e.getMessage().substring(0, 3));
            /*
             * if (statusCode == 401) {
             * // get access token refreshed
             * logger.debug("deviceExecuteCommand reporting access token is expired..");
             * refreshAccessToken(refreshToken, clientId, clientSecret);
             * logger.debug("deviceExecuteCommand reporting access token refresh successful..");
             * return (deviceExecuteCommand(deviceId, projectId, getAccessToken().getTokenValue(), requestBody)); //
             * returns
             */
            // error
            throw new IOException(e.getMessage());
        }
    }

    public String getDeviceInfo(String url) throws IOException {
        try {
            HttpTransport transport = new NetHttpTransport();
            HttpRequest request = transport.createRequestFactory().buildGetRequest(new GenericUrl(url));

            request.getHeaders().set(HttpHeaders.CONTENT_TYPE, "application/json");
            request.getHeaders().set(HttpHeaders.AUTHORIZATION, "Bearer " + getAccessToken().getTokenValue());
            HttpResponse response = request.execute();

            return (convertStreamtoString(response.getContent()));
        } catch (IOException e) {
            /*
             * int statusCode = Integer.parseInt(e.getMessage().substring(0, 3));
             *
             * if (statusCode == 401) {
             * // get access token refreshed
             * logger.debug("deviceGetInfo reporting access token is expired..");
             * accessToken = refreshAccessToken(refreshToken, clientId, clientSecret);
             * logger.debug("deviceGetInfo reporting access token refresh successful..");
             *
             * return (getDeviceInfo(accessToken, url)); // returns error code in string
             * // format. Did this to
             * // commoditize the
             * // return value for successful JSON response
             */
            throw new IOException(e.getMessage()); // never should get here unless there is a problem
        }
    }

    public AccessToken refreshAccessToken(String refreshToken, String clientId, String clientSecret)
            throws IOException {
        try {
            String accessToken;
            TokenResponse response = new GoogleRefreshTokenRequest(new NetHttpTransport(), new JacksonFactory(),
                    refreshToken, clientId, clientSecret).execute();
            // logger.info("Access Token: {}", response.getAccessToken());
            // logger.info("Refresh Token Lifespan: {}", response.getExpiresInSeconds());
            accessToken = response.getAccessToken();
            accessTokenExpiresIn = response.getExpiresInSeconds();
            googleAccessToken = setAccessToken(accessToken, (int) accessTokenExpiresIn);
            if (thing != null) {
                thing.setProperty("accessTokenExpiresIn", response.getExpiresInSeconds().toString());
                thing.setProperty("accessToken", googleAccessToken.getTokenValue());
            }
            return (googleAccessToken);
        } catch (TokenResponseException e) {
            logger.debug("refreshAccessToken threw an exception {}", e.getDetails().getError());
            if (e.getDetails().getErrorDescription() != null) {
                logger.debug("refreshAccessToken threw further description {}", e.getDetails().getErrorDescription());
            }
            throw new IOException(e.getMessage());
        }
    }

    public String[] requestAccessToken(String clientId, String clientSecret, String authorizationToken)
            throws IOException {
        try {
            String accessToken;
            String[] tokens = new String[2];
            TokenResponse response = new AuthorizationCodeTokenRequest(new NetHttpTransport(), new JacksonFactory(),
                    new GenericUrl("https://www.googleapis.com/oauth2/v4/token"), authorizationToken)
                            .setRedirectUri("https://www.google.com")
                            .setClientAuthentication(new BasicAuthentication(clientId, clientSecret)).execute();
            // logger.info("Access Token: {}", response.getAccessToken());
            // logger.info("Refresh Token: {}", response.getRefreshToken());
            // logger.info("Refresh Token Lifespan: {}", response.getExpiresInSeconds());
            accessToken = response.getAccessToken();
            refreshToken = response.getRefreshToken();

            accessTokenExpiresIn = response.getExpiresInSeconds();
            Calendar calendar = Calendar.getInstance();
            calendar.add(Calendar.SECOND, (int) accessTokenExpiresIn);

            // Construct a proper AccessToken
            googleAccessToken = new AccessToken(accessToken, calendar.getTime());

            tokens[0] = accessToken;
            tokens[1] = refreshToken;
            if (thing != null) {
                thing.setProperty("accessToken", response.getAccessToken());
                thing.setProperty("refreshToken", response.getRefreshToken());
                thing.setProperty("accessTokenExpiresIn", response.getExpiresInSeconds().toString());
            }
            return (tokens);
        } catch (TokenResponseException e) {
            throw new IOException(e.getMessage());
        }
    }

    /* Took function from online stackoverflow article... credit: Zapnologica */
    private String convertStreamtoString(InputStream is) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        StringBuilder sb = new StringBuilder();

        String line = "";
        try {
            while ((line = reader.readLine()) != null) {
                sb.append(line + "\n");
            }
            is.close();
            return sb.toString();
        } catch (IOException e) {
            throw new IOException(e.getMessage());
        }
    }

    public String getDevices(String projectId, AccessToken accessToken) throws IOException {

        try {
            HttpTransport transport = new NetHttpTransport();
            HttpRequest request = transport.createRequestFactory().buildGetRequest(new GenericUrl(
                    "https://smartdevicemanagement.googleapis.com/v1/enterprises/" + projectId + "/devices"));

            request.getHeaders().set(HttpHeaders.CONTENT_TYPE, "application/json");
            request.getHeaders().set(HttpHeaders.AUTHORIZATION, "Bearer " + getAccessToken().getTokenValue());
            HttpResponse response = request.execute();

            return (convertStreamtoString(response.getContent()));

        } catch (IOException e) {
            logger.debug("getDevices returning exception {}", e.getMessage());
            return ((e.getMessage().substring(0, 3)));
        }
    }

    /*
     * public int getStructures(String projectId, String accessToken) throws IOException {
     *
     * try {
     * HttpTransport transport = new NetHttpTransport();
     * HttpRequest request = transport.createRequestFactory().buildGetRequest(new GenericUrl(
     * "https://smartdevicemanagement.googleapis.com/v1/enterprises/" + projectId + "/structures"));
     *
     * request.getHeaders().set(HttpHeaders.CONTENT_TYPE, "application/json");
     * request.getHeaders().set(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken);
     * HttpResponse response = request.execute();
     *
     * JSONObject jo = new JSONObject(convertStreamtoString(response.getContent()));
     * JSONArray ja = jo.getJSONArray("structures");
     * // allocate structures array
     * structureId = new String[ja.length()];
     * structureName = new String[ja.length()];
     *
     * for (int i = 0; i < ja.length(); i++) {
     * structureName[i] = ja.getJSONObject(i).getJSONObject("traits")
     * .getJSONObject("sdm.structures.traits.Info").getString("customName");
     * String temp = ja.getJSONObject(i).getString("name");
     * structureId[i] = temp.substring(temp.lastIndexOf("/") + 1, temp.length());
     * logger.debug("getStructures reporting id {} and name {}", structureId[i], structureName[i]);
     * }
     *
     * response.getContent().close();
     * return (response.getStatusCode());
     * } catch (IOException e) {
     * logger.debug("getStructures returning exception {}", e.getMessage());
     * return (Integer.parseInt((e.getMessage().substring(0, 3))));
     * }
     * }
     */
}
