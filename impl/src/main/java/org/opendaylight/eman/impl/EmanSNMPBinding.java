/*
 * Copyright © 2015 2016, Pajarito Technologies LLC and others.  All rights reserved.
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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.HashMap;
import javax.json.*;
import java.time.LocalDateTime;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.commons.codec.binary.Base64;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient; 
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;

/*
import org.opendaylight.yang.gen.v1.urn.opendaylight.eman.rev171208.EmanService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.eman.rev171208.EoDeviceObject;
import org.opendaylight.yang.gen.v1.urn.opendaylight.eman.rev171208.EoDevices;
import org.opendaylight.yang.gen.v1.urn.opendaylight.eman.rev171208.eodevices.EoDeviceEntry;
*/

public class EmanSNMPBinding {
    private static final Logger LOG = LoggerFactory.getLogger(EmanProvider.class);
	protected static final String CONTENTTYPE_JSON = "application/json; charset=utf-8";

    // Definitions used to invoke ODL SNMP API
	protected static final String USER = "admin";
	protected static final String PASSWORD = "admin";
    String authStr = USER + ":" + PASSWORD;
    String encodedAuthStr = Base64.encodeBase64String(authStr.getBytes());    
	
    /* To do: fully implement eoPowerMeasurement object
    */
    String[] eoPowerMeasurementAttrs = {
        "eoPower",
        "eoPowerNameplate",
        "eoPowerUnitMultiplier",
        "eoPowerAccuracy",
        "eoPowerMeasurementCaliber",
        "eoPowerCurrentType",
        "eoPowerMeasurementLocal",
        "eoPowerAdminState",
        "eoPowerOperState" /*,
        "eoPowerStateEnterReason" */ // snmp simulator does not currently support
    }; 

    HashMap<String, String> OIDMapper; 
    
    public EmanSNMPBinding() {
     
        OIDMapper = new HashMap<String, String>();
     
        OIDMapper.put(eoPowerMeasurementAttrs[0], ".1.3.6.1.2.1.229.1.2.1.1.2");
        OIDMapper.put(eoPowerMeasurementAttrs[1], ".1.3.6.1.2.1.229.1.2.1.2.2");
        OIDMapper.put(eoPowerMeasurementAttrs[2], ".1.3.6.1.2.1.229.1.2.1.3.2");
        OIDMapper.put(eoPowerMeasurementAttrs[3], ".1.3.6.1.2.1.229.1.2.1.4.2");
        OIDMapper.put(eoPowerMeasurementAttrs[4], ".1.3.6.1.2.1.229.1.2.1.5.2");
        OIDMapper.put(eoPowerMeasurementAttrs[5], ".1.3.6.1.2.1.229.1.2.1.6.2");
        OIDMapper.put(eoPowerMeasurementAttrs[6], ".1.3.6.1.2.1.229.1.2.1.7.2");
        OIDMapper.put(eoPowerMeasurementAttrs[7], ".1.3.6.1.2.1.229.1.2.1.8.2");
        OIDMapper.put(eoPowerMeasurementAttrs[8], ".1.3.6.1.2.1.229.1.2.1.9.2");
//        OIDMapper.put(eoPowerMeasurementAttrs[9], ".1.3.6.1.2.1.229.1.2.1.10.2");

    }


	// Build body of POST message to ODL SNMP API
	private String buildSNMPGetBody(String deviceIP, String attribute) {
		LOG.info( "EmanSNMPBinding.buildSNMPGetBody: deviceIP and attribute: " + deviceIP+" "+attribute);

		final String pre = "{\"input\": {\"ip-address\":";
		final String mid = ",\"oid\": ";
		final String post = ",\"get-type\": \"GET\",\"community\":\"energy-object\"}}";
        String oid = OIDMapper.get(attribute);
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
	public String getEoAttrSNMP(String deviceIP, String attribute) {
		LOG.info( "EmanSNMPBinding.getEoAttrSNMP: ");

		/* To do: generalize targetURL
		    research using Java binding to make reference to 'local' ODL API
		 */
		String targetUrl = "http://localhost:8181/restconf/operations/snmp:snmp-get";
		String bodyString = buildSNMPGetBody(deviceIP, attribute);
		CloseableHttpClient httpClient = HttpClients.createDefault();
		String result = null;

		try {
		    /* invoke ODL SNMP API */
			HttpPost httpPost = new HttpPost(targetUrl);
			httpPost.addHeader("Authorization", "Basic " + encodedAuthStr);

			StringEntity inputBody = new StringEntity(bodyString);
			inputBody.setContentType(CONTENTTYPE_JSON);
			httpPost.setEntity(inputBody);

			CloseableHttpResponse httpResponse = httpClient.execute(httpPost);
			HttpEntity responseEntity = httpResponse.getEntity();
			LOG.info( "EmanSNMPBinding.getEoAttrSNMP: Response Status: " + httpResponse.getStatusLine().toString());
			InputStream in = responseEntity.getContent();

			/* Parse response from ODL SNMP API */
			JsonReader rdr = Json.createReader(in);
			JsonObject obj = rdr.readObject();
			JsonObject output = obj.getJsonObject("output");
			JsonArray results = output.getJsonArray("results");
			JsonObject pwr = results.getJsonObject(0);
			String oid = pwr.getString("oid");
			result = pwr.getString("value");
			rdr.close();

			LOG.info( "EmanSNMPBinding.getEoAttrSNMP: oid: " + oid + " value " + result);
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
	private String buildSNMPSetBody(String deviceIP, String attribute, String value) {
		final String ipStr = "{\"input\": {\"ip-address\":"+deviceIP;
        String oid = OIDMapper.get(attribute);
		final String oidStr = ",\"oid\": "+oid;
		final String commStr = ",\"community\":\"energy-object\"";
		final String valStr = ",\"value\": "+value;
		String bodyString = ipStr+oidStr+commStr+valStr+"}}";
		return bodyString;
	}

    /* Set Energy Object attribute via ODL SNMP API
    */
	public String setEoAttrSNMP(String deviceIP, String attribute, String value) {
		/* To do: generalize targetURL */
		String targetUrl = "http://localhost:8181/restconf/operations/snmp:snmp-set";
		String bodyString = buildSNMPSetBody(deviceIP, attribute, value);
		CloseableHttpClient httpClient = HttpClients.createDefault();
		String result = null;

		LOG.info( "EmanSNMPBinding.setEoAttrSNMP: HTTP SET SNMP call to: " + targetUrl);

		try {
			HttpPost httpPost = new HttpPost(targetUrl);
			httpPost.addHeader("Authorization", "Basic " + encodedAuthStr);

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
 
	/* Reads eoPowerMeasurement attributes vis ODL SNMP API and writes to MD-SAL
	    Currently rather inefficient as it loops through list of resources, reading sequentially
	*/
/*                
	public EoDeviceEntry getDevicePwrMsrSNMP(String deviceIP, int key) {
 		LOG.info( "EmanSNMPBinding.getDevicePwrMsrSNMP: ");
 		
        // Instantiate EoPowerMeasurement object
        EoPowerMeasurement pwrM = new EoPowerMeasurementBuilder()
            .setKey( new EoPowerMeasurementKey(key) )
//            .setTimeStamp( now.toString() )
            .setEoPower( Integer.parseInt( getEoAttrSNMP(deviceIP, eoPowerMeasurementAttrs[0]) ) )
            .setEoPowerNameplate( Integer.parseInt( getEoAttrSNMP(deviceIP, eoPowerMeasurementAttrs[1]) ) )
            .setEoPowerUnitMultiplier( Integer.parseInt( getEoAttrSNMP(deviceIP, eoPowerMeasurementAttrs[2]) ) )
            .setEoPowerAccuracy( Integer.parseInt( getEoAttrSNMP(deviceIP, eoPowerMeasurementAttrs[3]) ) )
            .setEoPowerMeasurementCaliber( Integer.parseInt( getEoAttrSNMP(deviceIP, eoPowerMeasurementAttrs[4]) ) )
            .setEoPowerCurrentType( Integer.parseInt( getEoAttrSNMP(deviceIP, eoPowerMeasurementAttrs[5]) ) )
            .setEoPowerMeasurementLocal( Integer.parseInt( getEoAttrSNMP(deviceIP, eoPowerMeasurementAttrs[6]) ) )
            .setEoPowerAdminState( Integer.parseInt( getEoAttrSNMP(deviceIP, eoPowerMeasurementAttrs[7]) ) )
            .setEoPowerOperState( Integer.parseInt( getEoAttrSNMP(deviceIP, eoPowerMeasurementAttrs[8]) ) )
//            .setEoPowerStateEnterReason( Integer.parseInt( getEoAttrSNMP(deviceIP, eoPowerMeasurementAttrs[9]) ) )
            .build();
        return null;
 
    }	    
*/            

}