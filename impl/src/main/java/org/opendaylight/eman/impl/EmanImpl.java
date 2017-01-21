/*
 * Copyright © 2015 Copyright (c) Pajarito Technologies LLC and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.eman.impl;

import java.io.IOException;
import java.io.InputStream;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.nio.file.*;
import java.util.Scanner;
import java.util.concurrent.Future;

import org.opendaylight.yang.gen.v1.urn.opendaylight.eman.rev170105.EmanService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.eman.rev170105.GetEoAttributeInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.eman.rev170105.GetEoAttributeOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.eman.rev170105.GetEoAttributeOutputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.eman.rev170105.SetEoAttributeInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.eman.rev170105.SetEoAttributeOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.eman.rev170105.SetEoAttributeOutputBuilder;
import org.opendaylight.yangtools.yang.common.RpcResult;
import org.opendaylight.yangtools.yang.common.RpcResultBuilder;

import org.apache.commons.codec.binary.Base64;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;

import javax.json.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class EmanImpl implements EmanService {
    private static final Logger LOG = LoggerFactory.getLogger(EmanImpl.class);
    protected static final String CONTENTTYPE_JSON = "application/json; charset=utf-8";

    protected static final String USER = "admin";
    protected static final String PASSWORD = "admin";

    // Create authentication string and encode it to Base64
    String authStr = USER + ":" + PASSWORD;
    String encodedAuthStr = Base64.encodeBase64String(authStr.getBytes());

    // Get EnergyObject Attribute
    // Implements getEoAttribute RPC. Maps attribute name to SNMP OID to query device
    @Override
    public Future<RpcResult<GetEoAttributeOutput>> getEoAttribute(GetEoAttributeInput input) {
        LOG.info("EmanImpl: getEoAttribute: ");

        // parse input
        String deviceIP = input.getDeviceIP();
        String attribute = input.getAttribute();
        String msg = null;

        // Hardcoded to query device via SNMP
        msg = getEoAttrSNMP(encodedAuthStr, deviceIP, attribute);

        GetEoAttributeOutput output = new GetEoAttributeOutputBuilder()
                .setResponse("Power attribute " + msg)
                .build();
        return RpcResultBuilder.success(output).buildFuture();
    }

    // Set EnergyObject Attribute
    @Override
    public Future<RpcResult<SetEoAttributeOutput>> setEoAttribute(SetEoAttributeInput input) {
        LOG.info("EmanImpl: setEoAttribute: ");

        // parse input
        String deviceIP = input.getDeviceIP();
        String attribute = input.getAttribute();
        String value = input.getValue();
        String msg = null;

        // Hardcoded to query device via SNMP
        msg = setEoAttrSNMP(encodedAuthStr, deviceIP, attribute, value);

        SetEoAttributeOutput output = new SetEoAttributeOutputBuilder()
                .setResponse("Power attribute " + msg)
                .build();
        return RpcResultBuilder.success(output).buildFuture();
    }

    private String res2OID(String resourceName) {

        OIDMap om = new OIDMap();
        String oid = om.getOID(resourceName);
        return (oid);
    }

    // Build body of POST message to ODL SNMP API
    private String buildSNMPGetBody(String resourceName, String deviceIP) {
        final String pre = "{\"input\": {\"ip-address\":";
        final String mid = ",\"oid\": ";
        final String post = ",\"get-type\": \"GET\",\"community\":\"energy-object\"}}";
        String oid = res2OID(resourceName);
        String bodyString = pre+deviceIP+mid+oid+post;
        return bodyString;
    }

    /* Query ODL SNMP API for a given eman OID.  Example response:
    {
      "output": {
        "results": [
          {
            "value": "222",
            "oid": "1.3.6.1.2.1.229.0.1.0"
          }
        ]
      }
    }
    */
    private String getEoAttrSNMP(String authString, String deviceIP, String attribute) {
        String targetUrl = "http://localhost:8181/restconf/operations/snmp:snmp-get";
        String bodyString = buildSNMPGetBody(attribute, deviceIP);
        CloseableHttpClient httpClient = HttpClients.createDefault();
        String result = null;

        LOG.info( "HTTP GET SNMP call to: " + targetUrl);

        try {
            HttpPost httpPost = new HttpPost(targetUrl);
            httpPost.addHeader("Authorization", "Basic " + authString);

            StringEntity inputBody = new StringEntity(bodyString);
            inputBody.setContentType(CONTENTTYPE_JSON);
            httpPost.setEntity(inputBody);

            CloseableHttpResponse httpResponse = httpClient.execute(httpPost);
            HttpEntity responseEntity = httpResponse.getEntity();
            LOG.info( "Response Status: " + httpResponse.getStatusLine().toString());
            InputStream in = responseEntity.getContent();

            JsonReader rdr = Json.createReader(in);
            JsonObject obj = rdr.readObject();
            JsonObject output = obj.getJsonObject("output");
            JsonArray results = output.getJsonArray("results");
            JsonObject pwr = results.getJsonObject(0);
            String oid = pwr.getString("oid");
            result = pwr.getString("value");
            rdr.close();

            LOG.info( "oid: " + oid);
            LOG.info( "value: " + result);

        } catch (Exception ex) {
            LOG.info( "Error: " + ex.getMessage(), ex);
        } finally {
            try {
                httpClient.close();
            } catch (IOException ex) {
                LOG.info( "Error: " + ex.getMessage(), ex);
            }
        }
        return (result);
    }


    // Build body of POST message to ODL SNMP API
    private String buildSNMPSetBody(String resourceName, String deviceIP, String value) {
        final String ipStr = "{\"input\": {\"ip-address\":"+deviceIP;
        String oid = res2OID(resourceName);
        final String oidStr = ",\"oid\": "+oid;
        final String commStr = ",\"community\":\"energy-object\"";
        final String valStr = ",\"value\": "+value;
        String bodyString = ipStr+oidStr+commStr+valStr+"}}";
        return bodyString;
    }

    private String setEoAttrSNMP(String authString, String deviceIP, String attribute, String value) {
        String targetUrl = "http://localhost:8181/restconf/operations/snmp:snmp-set";
        String bodyString = buildSNMPSetBody(attribute, deviceIP, value);
        CloseableHttpClient httpClient = HttpClients.createDefault();
        String result = null;

        LOG.info( "HTTP SET SNMP call to: " + targetUrl);
        LOG.info( "HTTP SET SNMP body: " + bodyString);

        try {
            HttpPost httpPost = new HttpPost(targetUrl);
            httpPost.addHeader("Authorization", "Basic " + authString);

            StringEntity inputBody = new StringEntity(bodyString);
            inputBody.setContentType(CONTENTTYPE_JSON);
            httpPost.setEntity(inputBody);

            CloseableHttpResponse httpResponse = httpClient.execute(httpPost);
            HttpEntity responseEntity = httpResponse.getEntity();
            LOG.info( "Response Status: " + httpResponse.getStatusLine().toString());
            InputStream in = responseEntity.getContent();

        } catch (Exception ex) {
            LOG.info( "Error: " + ex.getMessage(), ex);
        } finally {
            try {
                httpClient.close();
            } catch (IOException ex) {
                LOG.info( "Error: " + ex.getMessage(), ex);
            }
        }
        return (result);
    }


}