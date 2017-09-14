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

import java.util.List;
import java.util.ArrayList;

import java.math.BigInteger;

import org.opendaylight.yang.gen.v1.urn.opendaylight.eman.rev170105.EmanService;

import org.opendaylight.yang.gen.v1.urn.opendaylight.eman.rev170105.MeasurementObject;

import org.opendaylight.yang.gen.v1.urn.opendaylight.eman.rev170105.PowerMeasurementObject;

import org.opendaylight.yang.gen.v1.urn.opendaylight.eman.rev170105.TimedMeasurementObject;
import org.opendaylight.yang.gen.v1.urn.opendaylight.eman.rev170105.timedmeasurementobject.Value;
import org.opendaylight.yang.gen.v1.urn.opendaylight.eman.rev170105.timedmeasurementobject.ValueBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.eman.rev170105.timedmeasurementobject.Maximum;
import org.opendaylight.yang.gen.v1.urn.opendaylight.eman.rev170105.timedmeasurementobject.MaximumBuilder;

import org.opendaylight.yang.gen.v1.urn.opendaylight.eman.rev170105.TimeIntervalObject;

import org.opendaylight.yang.gen.v1.urn.opendaylight.eman.rev170105.DemandMeasurementObject;
import org.opendaylight.yang.gen.v1.urn.opendaylight.eman.rev170105.demandmeasurementobject.IntervalLength;
import org.opendaylight.yang.gen.v1.urn.opendaylight.eman.rev170105.demandmeasurementobject.IntervalWindow;
import org.opendaylight.yang.gen.v1.urn.opendaylight.eman.rev170105.demandmeasurementobject.SampleRate;
import org.opendaylight.yang.gen.v1.urn.opendaylight.eman.rev170105.demandmeasurementobject.Measurements;
import org.opendaylight.yang.gen.v1.urn.opendaylight.eman.rev170105.demandmeasurementobject.measurements.TimedMeasurement;
import org.opendaylight.yang.gen.v1.urn.opendaylight.eman.rev170105.demandmeasurementobject.measurements.TimedMeasurementBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.eman.rev170105.demandmeasurementobject.IntervalLengthBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.eman.rev170105.demandmeasurementobject.IntervalWindowBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.eman.rev170105.demandmeasurementobject.SampleRateBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.eman.rev170105.demandmeasurementobject.MeasurementsBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.eman.rev170105.demandmeasurementobject.MeasurementsKey;

import org.opendaylight.yang.gen.v1.urn.opendaylight.eman.rev170105.PowerStateObject;
import org.opendaylight.yang.gen.v1.urn.opendaylight.eman.rev170105.powerstateobject.MaximumPower;
import org.opendaylight.yang.gen.v1.urn.opendaylight.eman.rev170105.powerstateobject.MaximumPowerBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.eman.rev170105.PowerStateSetObject;
import org.opendaylight.yang.gen.v1.urn.opendaylight.eman.rev170105.powerstatesetobject.PowerStates;
import org.opendaylight.yang.gen.v1.urn.opendaylight.eman.rev170105.powerstatesetobject.PowerStatesBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.eman.rev170105.powerstatesetobject.PowerStatesKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.eman.rev170105.powerstatesetobject.powerstates.PowerState;
import org.opendaylight.yang.gen.v1.urn.opendaylight.eman.rev170105.powerstatesetobject.powerstates.PowerStateBuilder;

import org.opendaylight.yang.gen.v1.urn.opendaylight.eman.rev170105.EnergyObject;
import org.opendaylight.yang.gen.v1.urn.opendaylight.eman.rev170105.energyobject.Power;
import org.opendaylight.yang.gen.v1.urn.opendaylight.eman.rev170105.energyobject.PowerBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.eman.rev170105.energyobject.Demand;
import org.opendaylight.yang.gen.v1.urn.opendaylight.eman.rev170105.energyobject.DemandBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.eman.rev170105.energyobject.PowerControls;
import org.opendaylight.yang.gen.v1.urn.opendaylight.eman.rev170105.energyobject.PowerControlsBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.eman.rev170105.energyobject.powercontrols.PowerControl;
import org.opendaylight.yang.gen.v1.urn.opendaylight.eman.rev170105.energyobject.powercontrols.PowerControlBuilder;

import org.opendaylight.yang.gen.v1.urn.opendaylight.eman.rev170105.EoDeviceObject;

import org.opendaylight.yang.gen.v1.urn.opendaylight.eman.rev170105.EoDevices;
import org.opendaylight.yang.gen.v1.urn.opendaylight.eman.rev170105.EoDevicesBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.eman.rev170105.eodevices.EoDeviceEntry;
import org.opendaylight.yang.gen.v1.urn.opendaylight.eman.rev170105.eodevices.EoDeviceEntryBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.eman.rev170105.eodevices.EoDeviceEntryKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.eman.rev170105.eodevices.eodeviceentry.EoDevice;
import org.opendaylight.yang.gen.v1.urn.opendaylight.eman.rev170105.eodevices.eodeviceentry.EoDeviceBuilder;

public class EmanHTTPBinding {
    private static final Logger LOG = LoggerFactory.getLogger(EmanProvider.class);
    private static final List<Measurements> measurementsList = new ArrayList<>();
    
    // parse a JSON EoDevice object to populate a Java Power object
    private Power getPower(JsonObject eoDeviceJObj) {
        Power power = null;
    
        JsonObject pwrJObj = eoDeviceJObj.getJsonObject("power");
                        
		LOG.info(  "EmanHTTPBinding.getPower: pwrJObj " + pwrJObj.toString());

        try {
            power = new PowerBuilder()
            .setMultiplier(pwrJObj.getInt("multiplier"))
    //		            .setCaliber(valueJObj.getString("caliber"))
            .setAccuracy((pwrJObj.getJsonNumber("accuracy")).longValue())
            .setValue((pwrJObj.getJsonNumber("value")).longValue())
            .setUnits(pwrJObj.getString("units"))
            .build();

		} catch (Exception ex) {
			LOG.info(  "EmanHTTPBinding.getEoDeviceEntry: catch Error: " + ex.getMessage(), ex);
		}        
        return (power);
    }
    
    // parse a JSON EoDevice object to populate a Java Demand object
    private Demand getDemand(JsonObject eoDeviceJObj) {
        Demand demand = null;
    
        JsonObject demandJObj = eoDeviceJObj.getJsonObject("demand");

		LOG.info(  "EmanHTTPBinding.getDemand: demandJObj " + demandJObj.toString());

        try {                        
            JsonObject ilJobj = demandJObj.getJsonObject("intervalLength");
            IntervalLength intervalLength = new IntervalLengthBuilder()
            .setValue((ilJobj.getJsonNumber("value")).bigIntegerValue())
    //        .setUnits(0)
            .build();
                        
            JsonObject iwJobj = demandJObj.getJsonObject("intervalWindow");
            IntervalWindow intervalWindow = new IntervalWindowBuilder()
            .setValue((iwJobj.getJsonNumber("value")).bigIntegerValue())
    //			    .setUnits()
            .build();
                        
            JsonObject sRobj = demandJObj.getJsonObject("sampleRate");
            SampleRate sampleRate = new SampleRateBuilder()
            .setValue((sRobj.getJsonNumber("value")).bigIntegerValue())
    //			    .setUnits()
            .build();

            JsonArray measurementJArr = demandJObj.getJsonArray("measurements");
            for (int j=0; j< measurementJArr.size(); j++ ) {
                JsonObject measurementJObj = measurementJArr.getJsonObject(j);
                JsonObject TMOJObj = measurementJObj.getJsonObject("timedMeasurement");
                JsonObject valueJObj = TMOJObj.getJsonObject("value");
                JsonObject maxJObj = TMOJObj.getJsonObject("maximum");
    //		            LOG.info(  "EmanHTTPBinding: getEoDeviceEntry: measurementJObj.name " + measurementJObj.getString("name"));
    //		            LOG.info(  "EmanHTTPBinding: getEoDeviceEntry: TMOJObj " + TMOJObj.toString());
    //		            LOG.info(  "EmanHTTPBinding: getEoDeviceEntry: valueJObj " + valueJObj.toString());
    //		            LOG.info(  "EmanHTTPBinding: getEoDeviceEntry: maxJObj " + maxJObj.toString());
                                
                Value val = new ValueBuilder()
                .setMultiplier(valueJObj.getInt("multiplier"))
    //		            .setCaliber(valueJObj.getString("caliber"))
                .setAccuracy((valueJObj.getJsonNumber("accuracy")).longValue())
                .setValue((valueJObj.getJsonNumber("value")).longValue())
                .setUnits(valueJObj.getString("units"))
                .build();
            
                Maximum max = new MaximumBuilder()
                .setMultiplier(maxJObj.getInt("multiplier"))
    //		            .setCaliber(maxJObj.getString("caliber"))
                .setAccuracy((maxJObj.getJsonNumber("accuracy")).longValue())
                .setValue((maxJObj.getJsonNumber("value")).longValue())
                .setUnits(maxJObj.getString("units"))
                .build();
            
                TimedMeasurement tm = new TimedMeasurementBuilder()
                .setStartTime((TMOJObj.getJsonNumber("startTime")).longValue())
                .setValue(val)
                .setMaximum(max)
                .build();
            
                Measurements m = new MeasurementsBuilder()
                .setName(measurementJObj.getString("name"))
                .setTimedMeasurement(tm)
                .build();
            
                measurementsList.add(m);
            }
        
            demand = new DemandBuilder()
            .setMultiplier(demandJObj.getInt("multiplier"))
    //                .setCaliber(demandJObj.getString("caliber"))
            .setAccuracy((demandJObj.getJsonNumber("accuracy")).longValue())
            .setIntervalLength(intervalLength)
            .setIntervals((demandJObj.getJsonNumber("intervals")).bigIntegerValue())
    //                .setIntervalMode(demandJObj.getString("intervalMode"))
            .setIntervalWindow(intervalWindow)
            .setSampleRate(sampleRate)
    //                .setStatus(demandJObj.getString("status"))
            .setMeasurements(measurementsList)
            .build();

		} catch (Exception ex) {
			LOG.info(  "EmanHTTPBinding.getEoDeviceEntry: catch Error: " + ex.getMessage(), ex);
		}
		
		return (demand);
    }
    
    // parse a JSON EoDevice object to populate a Java PowerControls object
    private List<PowerControls> getPowerControls(JsonObject eoDeviceJObj) {
        List<PowerControls> pwrControlsList = new ArrayList<>();
        List<PowerStates> pwrStatesList = new ArrayList<>();

        try {
            JsonArray pwrCntrlJArr = eoDeviceJObj.getJsonArray("powerControls");
        
            LOG.info(  "EmanHTTPBinding.getPowerControls: pwrCntrlJArr " + pwrCntrlJArr.toString());

            for(int i = 0; i < pwrCntrlJArr.size(); i++) {
                JsonObject pwrContrlsEntryJObj = pwrCntrlJArr.getJsonObject(i);
                JsonArray pwrStatesJArr = pwrContrlsEntryJObj.getJsonArray("powerStates");
            
                for(int j = 0; j < pwrStatesJArr.size(); j++) {
                    JsonObject pwrStateEntrylJObj = pwrStatesJArr.getJsonObject(j);
                    JsonObject maxPwrlJObj = pwrStateEntrylJObj.getJsonObject("maximumPower");
                
                    MaximumPower max = new MaximumPowerBuilder()
                    .setMultiplier(maxPwrlJObj.getInt("multiplier"))
        //		            .setCaliber(maxPwrlJObj.getString("caliber"))
                    .setAccuracy((maxPwrlJObj.getJsonNumber("accuracy")).longValue())
                    .setValue((maxPwrlJObj.getJsonNumber("value")).longValue())
                    .setUnits(maxPwrlJObj.getString("units"))
                    .build();
                
                    PowerState pwrState = new PowerStateBuilder()
                    .setPowerStateIdentifier((pwrStateEntrylJObj.getJsonNumber("powerStateIdentifier")).longValue())
                    .setName(pwrStateEntrylJObj.getString("name"))
                    .setCardinality((pwrStateEntrylJObj.getJsonNumber("cardinality")).longValue())
                    .setMaximumPower(max)
//                    .setTotalImeInState(pwrStateEntrylJObj.getString("totalTimeInState"))
                    .setEntryCount((pwrStateEntrylJObj.getJsonNumber("entryCount")).bigIntegerValue())
                    .build();
                
                    PowerStates pwrStateEntry = new PowerStatesBuilder()
                    .setName(pwrStateEntrylJObj.getString("name"))
                    .setPowerState(pwrState)
                    .build();
                 
                     pwrStatesList.add(pwrStateEntry);
                }

                JsonObject pwrContrlJObj = pwrContrlsEntryJObj.getJsonObject("powerControl");
                PowerControl powerCntrl = new PowerControlBuilder()
                .setPowerStateIdentifier( (pwrContrlJObj.getJsonNumber("powerStateIdentifier")).longValue() )
                .setName(pwrContrlJObj.getString("name"))
                .setPowerStates(pwrStatesList)
                .setOperState( (pwrContrlJObj.getJsonNumber("operState")).longValue() )
                .setAdminState( (pwrContrlJObj.getJsonNumber("adminState")).longValue() )
                .setReason(pwrContrlJObj.getString("reason"))
//                .setConfigurationTime(pwrContrlEntryJObj.getInt("configuredTime"))
                .build();

                PowerControls powerCntrlEntry = new PowerControlsBuilder()
                .setName(pwrContrlJObj.getString("name"))
                .setPowerControl(powerCntrl)
                .build();
           
                pwrControlsList.add(powerCntrlEntry);
            }
		} catch (Exception ex) {
			LOG.info(  "EmanHTTPBinding.getEoDeviceEntry: catch Error: " + ex.getMessage(), ex);
		} 
		       
        return (pwrControlsList);
    }
    
    /* Build:
    *   edDevices
    *       EoDeviceEntry[]
    *           EoDevice - container
    *               EoDeviceObject - group
    *                   energyObject - group
    *                       power - container
    *                           PowerMeasurementObject - group
    *                       demand - container
    *                           DemandMeasurementObject - group
    *                               MeasurementObject - group
    *                               intervalLength - container
    *                                   TimeIntervalObject - group
    *                               intervalWindow - container
    *                                   TimeIntervalObject - group
    *                               samplerate - container
    *                                   TimeIntervalObject - group
    *                               measurements[]
    *                                   TimedMeasurementObject - container
    *                                       TimedMeasurementObject - group
    *                                           value - container
    *                                               PowerMeasurementObject - group
    *                                           Maximum - container
    *                                                PowerMeasurementObject - group
    *                       powerControls - container
    *                           powerControl - container
    *                               PowerStateSetObject - group
    * 
    * Note: containers have Builder classes, groups (object) do not. Lists have Key classes                  
    */
    public EoDevice getEoDevice(String deviceIP, int key) {
		String targetUrl = "http://"+deviceIP;
		JsonReader rdr = null;
		String str = null;	
		
		EoDevice eoDevice = null;	

		LOG.info(  "EmanHTTPBinding.getEoDeviceEntry: targetUrl " + targetUrl);

		CloseableHttpClient httpClient = HttpClients.createDefault();

		try {
			HttpGet httpGet = new HttpGet(targetUrl);

			CloseableHttpResponse httpResponse = httpClient.execute(httpGet);
			HttpEntity responseEntity = httpResponse.getEntity();

			InputStream in = responseEntity.getContent();			
			java.util.Scanner s = new java.util.Scanner(in).useDelimiter("\\A");
            str = s.hasNext() ? s.next() : "";
		    LOG.info(  "EmanHTTPBinding: getEoDeviceEntry: str " + str);

			rdr = Json.createReader(new StringReader(str));
			JsonObject obj = rdr.readObject();			
			JsonObject eoDevicesObj = obj.getJsonObject("eoDevices");
			JsonArray eoEntries = eoDevicesObj.getJsonArray("eoDeviceEntries");
		    LOG.info(  "EmanHTTPBinding: getEoDeviceEntry: eoDevicesObj " + eoDevicesObj.toString());
		    LOG.info(  "EmanHTTPBinding: getEoDeviceEntry: eoDeviceEntries " + eoEntries.toString());
			
			for (int i=0; i< eoEntries.size(); i++ ) {
			    JsonObject eoDeviceEntryJObj = eoEntries.getJsonObject(i);
			    String entryName = eoDeviceEntryJObj.getString("name");
			    JsonObject eoDeviceJObj = eoDeviceEntryJObj.getJsonObject("EoDevice");
			    
			    Demand demand = getDemand(eoDeviceJObj);

			    eoDevice = new EoDeviceBuilder()
// how to do emums?                .setEocategory(eoDeviceJObj.getString("eoCategory"))
// hw to set long??                .setIndex(eoDeviceJObj.getInt("index"))
                .setName(eoDeviceJObj.getString("name"))
                .setIdentifier((eoDeviceJObj.getJsonNumber("identifier")).longValue())
                .setAlternateKey(eoDeviceJObj.getString("alternateKey"))
                .setDomainName(eoDeviceJObj.getString("domainName"))
                .setRole(eoDeviceJObj.getString("role"))
                .setKeywords(eoDeviceJObj.getString("keyWords"))
                .setImportance((eoDeviceJObj.getJsonNumber("importance")).longValue())
//                .setPower(getPower(eoDeviceJObj))
                .setDemand(demand)
//                .setPowerControls(getPowerControls(eoDeviceJObj))
			    .build();
			}

		} catch (Exception ex) {
			LOG.info(  "EmanHTTPBinding.getEoDeviceEntry: catch Error: " + ex.getMessage(), ex);
		} finally {
			try {
			    rdr.close();
				httpClient.close();
			} catch (IOException ex) {
				LOG.info(  "EmanHTTPBinding.getEoDeviceEntry: finally Error: " + ex.getMessage(), ex);
			}
		}
            
        return eoDevice;
	}
	
}