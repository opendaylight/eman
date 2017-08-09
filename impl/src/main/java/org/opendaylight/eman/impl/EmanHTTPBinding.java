/*
 * Copyright © 2015 2016, Comcast Corporation and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.eman.impl;

import javax.json.*;
import java.io.IOException;
import java.io.InputStream;
import java.util.Scanner;
import java.io.StringReader;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;

import org.opendaylight.yang.gen.v1.urn.opendaylight.eman.rev170105.eomeasurementgroup.EoPowerMeasurementBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.eman.rev170105.eomeasurementgroup.EoPowerMeasurement;
import org.opendaylight.yang.gen.v1.urn.opendaylight.eman.rev170105.eomeasurementgroup.EoPowerMeasurementKey;


public class EmanHTTPBinding {
    private static final Logger LOG = LoggerFactory.getLogger(EmanProvider.class);
	
    // stub to provide symmetry with SNMPBinding
    public String getEoPowerMeasurementAttribute(String deviceIP, String attribute) {
		LOG.info(  "EmanHTTPBinding: getEoPowerMeasurementAttribute ");
            
        return null;
    }
	
    // stub to provide symmetry with SNMPBinding
    public String setEoPowerMeasurementAttribute(String deviceIP, String attribute, String value) {
		LOG.info(  "EmanHTTPBinding: setEoPowerMeasurementAttribute ");
            
        return null;
    }

    public EoPowerMeasurement getEoPowerMeasurement(String deviceIP, int key) {
		String targetUrl = "http://"+deviceIP;
		EoPowerMeasurement pwrM = null;
		JsonReader rdr = null;
		String str = null;

		LOG.info(  "EmanHTTPBinding: getEoPowerMeasurement ");

		CloseableHttpClient httpClient = HttpClients.createDefault();

		try {
			HttpGet httpGet = new HttpGet(targetUrl);
			CloseableHttpResponse httpResponse = httpClient.execute(httpGet);
			HttpEntity responseEntity = httpResponse.getEntity();

//			LOG.info(  "EmanHTTPBinding: Response Status: " + httpResponse.getStatusLine().toString());

			InputStream in = responseEntity.getContent();			
			java.util.Scanner s = new java.util.Scanner(in).useDelimiter("\\A");
            str = s.hasNext() ? s.next() : "";
//		    LOG.info(  "EmanHTTPBinding: getEoPowerMeasurement: str " + str);

			// Doens't like InputStream ??? so converted to string
//			rdr = Json.createReader(in);
			rdr = Json.createReader(new StringReader(str));
			JsonObject obj = rdr.readObject();			

			JsonArray components = obj.getJsonArray("components");
			JsonObject arObj = components.getJsonObject(0);
			JsonObject eoObj = arObj.getJsonObject("eoPowerMeasurement");

		
            // Instantiate EoPowerMeasurement object
            pwrM = new EoPowerMeasurementBuilder()
                .setKey( new EoPowerMeasurementKey(key) )
                .setEoPower( Integer.parseInt( eoObj.getString("eoPower") ) )
                .setEoPowerNameplate( Integer.parseInt( eoObj.getString("eoPowerNameplate") ) )
                .setEoPowerUnitMultiplier( Integer.parseInt( eoObj.getString("eoPowerUnitMultiplier") ) )
                .setEoPowerAccuracy( Integer.parseInt( eoObj.getString("eoPowerAccuracy") ) )
                .setEoPowerMeasurementCaliber( Integer.parseInt( eoObj.getString("eoPowerMeasurementCaliber") ) )
                .setEoPowerCurrentType( Integer.parseInt( eoObj.getString("eoPowerCurrentType") ) )
                .setEoPowerMeasurementLocal( Integer.parseInt( eoObj.getString("eoPowerMeasurementLocal") ) )
                .setEoPowerAdminState( Integer.parseInt( eoObj.getString("eoPowerAdminState") ) )
                .setEoPowerOperState( Integer.parseInt( eoObj.getString("eoPowerOperState") ) )
                .build();


		} catch (Exception ex) {
			LOG.info(  "EmanHTTPBinding: catch Error: " + ex.getMessage(), ex);
		} finally {
			try {
			    rdr.close();
				httpClient.close();
			} catch (IOException ex) {
				LOG.info(  "EmanHTTPBinding: finally Error: " + ex.getMessage(), ex);
			}
		}
            
        return pwrM;
	}
	
}