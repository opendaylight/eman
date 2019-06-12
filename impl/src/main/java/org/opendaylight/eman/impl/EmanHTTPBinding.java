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
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.yang.types.rev130715.Timeticks;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.yang.types.rev130715.Timestamp;

import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.eman.rev171208.*;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.eman.rev171208.eodevices.*;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.eman.rev171208.eodevices.EoDevice.*;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.eman.rev171208.energygroup.*;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.eman.rev171208.energygroup.energyobject.*;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.eman.rev171208.energygroup.energyobject.relationships.*;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.eman.rev171208.energygroup.energyobject.relationships.Relationship.*;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.eman.rev171208.energygroup.energyobject.nameplate.*;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.eman.rev171208.energygroup.energyobject.powercontrols.*;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.eman.rev171208.Measurement.*;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.eman.rev171208.DemandMeasurement.*;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.eman.rev171208.demandmeasurement.*;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.eman.rev171208.demandmeasurement.timedmeasurements.*;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.eman.rev171208.demandmeasurement.timedmeasurements.timedmeasurement.*;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.eman.rev171208.TimeInterval.*;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.eman.rev171208.PowerStateSet.*;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.eman.rev171208.powerstateset.*;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.eman.rev171208.powerstateset.powerstates.*;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.eman.rev171208.powerstateset.powerstates.powerstate.*;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.eman.rev171208.eodevices.*;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.eman.rev171208.eodevices.eodevice.*;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.eman.rev171208.eodevices.eodevice.powerinterfaces.*;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.eman.rev171208.eodevices.eodevice.components.*;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.eman.rev171208.ACQuality.*;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.eman.rev171208.powermeasurement.*;

/*
*   EmanHTTPBinding queries energy device using HTTP protocol
*/
public class EmanHTTPBinding {
    private static final Logger LOG = LoggerFactory.getLogger(EmanProvider.class);

    private static final List<TimedMeasurement> timedMeasurementList = new ArrayList<>();
    private static final List<Relationship> relationshipList = new ArrayList<>();
    private static final List<PowerControl> powerControlList = new ArrayList<>();
    private static final List<PowerInterface> powerInterfaceList = new ArrayList<>();
    private static final List<Component> componentList = new ArrayList<>();

    /*
    *   parse Json and write to corresponding java object
    */
    private Relationships getRelationships(JsonObject eoDeviceJObj) {
        Relationships relationships = null;
        Relationship relationship = null;
    
       try {
            JsonObject relsJObj = eoDeviceJObj.getJsonObject("relationships");
        
             // create top-level object and add an array object. As we read data, add to array
            if (relationships == null) {
                 relationships = new RelationshipsBuilder()
                .setRelationship(relationshipList)
                .build();
            }

			JsonArray relArr = relsJObj.getJsonArray("relationship");

			for (int i=0; i< relArr.size(); i++ ) {
			    JsonObject relJObj = relArr.getJsonObject(i);
                relationship = new RelationshipBuilder()
                    .setRelationshipType(RelationshipType.valueOf(relJObj.getString("relationshipType")))
                    .setRelationshipObject((relJObj.getJsonNumber("relationshipObject")).longValue())
                    .build();
                relationshipList.add(relationship);
		    }
		} catch (Exception ex) {
			LOG.info(  "EmanHTTPBinding.getRelationships: catch Error: " + ex.getMessage(), ex);
		}        
        return (relationships);
    }
    
    /*
    *   parse Json and write to corresponding java object
    */
    private PowerAttribute getACQuality(JsonObject jObj) {
        PowerAttribute powerAttribute = null;

        JsonObject acqJObj = jObj.getJsonObject("powerAttribute");

        if (acqJObj != null) {                      
    		LOG.info(  "EmanHTTPBinding.getACQuality: acqJObj " + acqJObj.toString());
            powerAttribute = new PowerAttributeBuilder()
                .setAcConfiguration(AcConfiguration.valueOf(acqJObj.getString("acConfiguration")))
                .setAvgVoltage((acqJObj.getJsonNumber("avgVoltage")).bigIntegerValue())
                .setAvgCurrent((acqJObj.getJsonNumber("avgCurrent")).bigIntegerValue())
                .setThdCurrent((acqJObj.getJsonNumber("thdCurrent")).bigIntegerValue())
                .setFrequency((acqJObj.getJsonNumber("frequency")).bigIntegerValue())
                .setUnitMultiplier((acqJObj.getJsonNumber("unitMultiplier")).longValue())
                .setAccuracy((acqJObj.getJsonNumber("accuracy")).longValue())
                .setTotalActivePower((acqJObj.getJsonNumber("totalActivePower")).bigIntegerValue())
                .setTotalReactivePower((acqJObj.getJsonNumber("totalReactivePower")).bigIntegerValue())
                .setTotalApparentPower((acqJObj.getJsonNumber("totalApparentPower")).bigIntegerValue())
                .setTotalPowerFactor((acqJObj.getJsonNumber("totalPowerFactor")).bigIntegerValue())
                .build();
		}
        else {
    		LOG.info(  "EmanHTTPBinding.getACQuality: acqJObj == null");
        }
		return (powerAttribute);
    }
    
    /*
    *   parse Json and write to corresponding java object
    */
    private Nameplate getNameplate(JsonObject eoDeviceJObj) {
        Nameplate nameplate = null;
        NominalPower nominalPower = null;
    
        JsonObject nameplateJObj = eoDeviceJObj.getJsonObject("nameplate");
        if (nameplateJObj == null) {
		    LOG.info(  "EmanHTTPBinding: getNameplate: nameplateJObj == null ");
            return (nameplate);
        }
         
         JsonObject nominalPowerJObj = nameplateJObj.getJsonObject("nominalPower");
         nominalPower = new NominalPowerBuilder()
            .setMultiplier(nominalPowerJObj.getInt("multiplier"))
            .setCaliber(Caliber.valueOf(nominalPowerJObj.getString("caliber")))
            .setAccuracy((nominalPowerJObj.getJsonNumber("accuracy")).longValue())
            .setValue((nominalPowerJObj.getJsonNumber("value")).longValue())
            .setUnits(nominalPowerJObj.getString("units"))
            .setPowerAttribute(getACQuality(nominalPowerJObj))
        .build();
       
         // create top-level object and add an array object. As we read data, add to array
        if (nameplate == null) {
             nameplate = new NameplateBuilder()
                .setNominalPower(nominalPower)
                .setDetails(nameplateJObj.getString("details"))
                .build();
        }
        return (nameplate);
    }
    
    /*
    *   parse Json and write to corresponding java object
    */
    private Power getPower(JsonObject jObj) {
        Power power = null;
    
        JsonObject pwrJObj = jObj.getJsonObject("power");
                        
		LOG.info(  "EmanHTTPBinding.getPower: pwrJObj " + pwrJObj.toString());

        if (pwrJObj != null) {
            power = new PowerBuilder()
                .setMultiplier(pwrJObj.getInt("multiplier"))
                .setCaliber(Caliber.valueOf(pwrJObj.getString("caliber")))
                .setAccuracy((pwrJObj.getJsonNumber("accuracy")).longValue())
                .setValue((pwrJObj.getJsonNumber("value")).longValue())
                .setUnits(pwrJObj.getString("units"))
                .setPowerAttribute(getACQuality(pwrJObj))
                .build();
		} 
        return (power);
    }
         
    /*
    *   parse Json and write to corresponding java object
    */
    private Energy getEnergy(JsonObject eoDeviceJObj) {
        Energy energy = null;
    
        JsonObject energyJObj = eoDeviceJObj.getJsonObject("energy");
                        
		LOG.info(  "EmanHTTPBinding.getEnergy: energyJObj " + energyJObj.toString());

        try {
            energy = new EnergyBuilder()
                .setMultiplier(energyJObj.getInt("multiplier"))
                .setCaliber(Caliber.valueOf(energyJObj.getString("caliber")))
                .setAccuracy((energyJObj.getJsonNumber("accuracy")).longValue())
                .setStartTime((energyJObj.getJsonNumber("startTime")).longValue())
                .setUnits(energyJObj.getString("units"))
                .setProvided((energyJObj.getJsonNumber("provided")).longValue())
                .setUsed((energyJObj.getJsonNumber("used")).longValue())
                .setProduced((energyJObj.getJsonNumber("produced")).longValue())
                .setStored((energyJObj.getJsonNumber("stored")).longValue())
                .build();
		} catch (Exception ex) {
			LOG.info(  "EmanHTTPBinding.getEnergy: catch Error: " + ex.getMessage(), ex);
		}        
        return (energy);
    }
 
    /*
    *   parse Json and write to corresponding java object
    */
    private Demand getDemand(JsonObject eoDeviceJObj) {
        Demand demand = null;
        TimedMeasurements timedMeasurements = null;
        TimedMeasurement timedMeasurement = null;
        
        JsonObject demandJObj = eoDeviceJObj.getJsonObject("demand");

		LOG.info(  "EmanHTTPBinding.getDemand: demandJObj " + demandJObj.toString());

        try {                        
            JsonObject ilJobj = demandJObj.getJsonObject("intervalLength");
            IntervalLength intervalLength = new IntervalLengthBuilder()
                .setValue((ilJobj.getJsonNumber("value")).bigIntegerValue())
                .setUnits(Units.valueOf(ilJobj.getString("units")))
                .build();
                        
            JsonObject iwJobj = demandJObj.getJsonObject("intervalWindow");
            IntervalWindow intervalWindow = new IntervalWindowBuilder()
                .setValue((iwJobj.getJsonNumber("value")).bigIntegerValue())
                .setUnits(Units.valueOf(ilJobj.getString("units")))
                .build();
                        
            JsonObject sRobj = demandJObj.getJsonObject("sampleRate");
            SampleRate sampleRate = new SampleRateBuilder()
                .setValue((sRobj.getJsonNumber("value")).bigIntegerValue())
                .setUnits(Units.valueOf(ilJobj.getString("units")))
                .build();
        
             // create top-level object and add an array object. As we read data, add to array
            if (timedMeasurements == null) {
                 timedMeasurements = new TimedMeasurementsBuilder()
                .setTimedMeasurement(timedMeasurementList)
                .build();
            }

            JsonObject tmJObj = demandJObj.getJsonObject("timedMeasurements");
            JsonArray measurementJArr = tmJObj.getJsonArray("timedMeasurement");
            for (int i=0; i< measurementJArr.size(); i++ ) {
                JsonObject TMOJObj = measurementJArr.getJsonObject(i);
                JsonObject valueJObj = TMOJObj.getJsonObject("value");
                JsonObject maxJObj = TMOJObj.getJsonObject("maximum");
                            
                Value val = new ValueBuilder()
                    .setMultiplier(valueJObj.getInt("multiplier"))
                    .setCaliber(Caliber.valueOf(valueJObj.getString("caliber")))
                    .setAccuracy((valueJObj.getJsonNumber("accuracy")).longValue())
                    .setValue((valueJObj.getJsonNumber("value")).longValue())
                    .setUnits(valueJObj.getString("units"))
                    .build();
        
                Maximum max = new MaximumBuilder()
                    .setMultiplier(maxJObj.getInt("multiplier"))
                    .setCaliber(Caliber.valueOf(maxJObj.getString("caliber")))
                    .setAccuracy((maxJObj.getJsonNumber("accuracy")).longValue())
                    .setValue((maxJObj.getJsonNumber("value")).longValue())
                    .setUnits(maxJObj.getString("units"))
                    .build();
                
                TimedMeasurement m = new TimedMeasurementBuilder()
                    .setName(TMOJObj.getString("name"))
                    .setStartTime((TMOJObj.getJsonNumber("startTime")).longValue())
                    .setValue(val)
                    .setMaximum(max)
                    .build();
        
                timedMeasurementList.add(m);
            }
        
            demand = new DemandBuilder()
                .setMultiplier(demandJObj.getInt("multiplier"))
                .setCaliber(Caliber.valueOf(demandJObj.getString("caliber")))
                .setAccuracy((demandJObj.getJsonNumber("accuracy")).longValue())
                .setIntervalLength(intervalLength)
                .setIntervals((demandJObj.getJsonNumber("intervals")).bigIntegerValue())
                .setIntervalMode(IntervalMode.valueOf(demandJObj.getString("intervalMode")))
                .setIntervalWindow(intervalWindow)
                .setSampleRate(sampleRate)
                .setStatus(Status.valueOf(demandJObj.getString("status")))
                .setTimedMeasurements(timedMeasurements)
                .build();

		} catch (Exception ex) {
			LOG.info(  "EmanHTTPBinding.getEoDeviceEntry: catch Error: " + ex.getMessage(), ex);
		}
		
		return (demand);
    }

    /*
    *   parse Json and write to corresponding java object
    */
    private PowerControls getPowerControls(JsonObject eoDeviceJObj) {
        PowerControls powerControls = null;
        PowerStates powerStates = null;
        
        List<PowerState> powerStateList = null;
        
        // create top-level object and add an array object. As we read data, add to array
        if (powerControls == null) {
             powerControls = new PowerControlsBuilder()
            .setPowerControl(powerControlList)
            .build();
        }

        try {
            JsonObject pwrCntrlsJObj = eoDeviceJObj.getJsonObject("powerControls");
            JsonArray pwrCntrlJArr = pwrCntrlsJObj.getJsonArray("powerControl");
        
            LOG.info(  "EmanHTTPBinding.getPowerControls: pwrCntrlJArr " + pwrCntrlJArr.toString());

            for (int i = 0; i < pwrCntrlJArr.size(); i++) {
                 // create top-level object and add an array object. As we read data, add to array
                powerStateList = new ArrayList<>();
                powerStates = new PowerStatesBuilder()
                    .setPowerState(powerStateList)
                    .build();
 
                JsonObject pwrCntrlJObj = pwrCntrlJArr.getJsonObject(i);
                JsonObject pwrStatesJObj = pwrCntrlJObj.getJsonObject("powerStates");
                JsonArray pwrStateJArr = pwrStatesJObj.getJsonArray("powerState");
            
                for(int j = 0; j < pwrStateJArr.size(); j++) {
                    JsonObject pwrStateJObj = pwrStateJArr.getJsonObject(j);
                    JsonObject maxPwrJObj = pwrStateJObj.getJsonObject("maximumPower");
                
                    MaximumPower max = new MaximumPowerBuilder()
                        .setMultiplier(maxPwrJObj.getInt("multiplier"))
                        .setCaliber(Caliber.valueOf(maxPwrJObj.getString("caliber")))
                        .setAccuracy((maxPwrJObj.getJsonNumber("accuracy")).longValue())
                        .setValue((maxPwrJObj.getJsonNumber("value")).longValue())
                        .setUnits(maxPwrJObj.getString("units"))
                        .build();
                
                    PowerState pwrState = new PowerStateBuilder()
                        .setId(pwrStateJObj.getInt("id"))
                        .setPowerStateIdentifier((pwrStateJObj.getJsonNumber("powerStateIdentifier")).longValue())
                        .setName(pwrStateJObj.getString("name"))
                        .setCardinality((pwrStateJObj.getJsonNumber("cardinality")).longValue())
                        .setMaximumPower(max)
                        .setTotalTimeInState(new Timeticks ((pwrStateJObj.getJsonNumber("totalTimeInState")).longValue()))
                        .setEntryCount((pwrStateJObj.getJsonNumber("entryCount")).bigIntegerValue())
                        .build();
                 
                     powerStateList.add(pwrState);
                }

                PowerControl powerCntrl = new PowerControlBuilder()
                    .setId(pwrCntrlJObj.getInt("id"))
                    .setPowerStateIdentifier( (pwrCntrlJObj.getJsonNumber("powerStateIdentifier")).longValue() )
                    .setName(pwrCntrlJObj.getString("name"))
                    .setPowerStates(powerStates)
                    .setOperState( (pwrCntrlJObj.getJsonNumber("operState")).longValue() )
                    .setAdminState( (pwrCntrlJObj.getJsonNumber("adminState")).longValue() )
                    .setReason(pwrCntrlJObj.getString("reason"))
                    .setConfiguredTime(new Timestamp ((pwrCntrlJObj.getJsonNumber("configuredTime")).longValue()))
                    .build();
           
                powerControlList.add(powerCntrl);
            }
		} catch (Exception ex) {
			LOG.info(  "EmanHTTPBinding.getEoDeviceEntry: catch Error: " + ex.getMessage(), ex);
		} 
        return (powerControls);
    }

    /*
    *   parse Json and write to corresponding java object
    */
    private EnergyObject getEnergyObject(JsonObject eoJObj) {
        EnergyObject energyObject = null;

        try {
    		energyObject = new EnergyObjectBuilder()
                .setIndex((eoJObj.getJsonNumber("index")).longValue())
                .setName(eoJObj.getString("name"))
                .setIdentifier((eoJObj.getJsonNumber("identifier")).longValue())
                .setAlternateKey(eoJObj.getString("alternateKey"))
                .setDomainName(eoJObj.getString("domainName"))
                .setRole(eoJObj.getString("role"))
                .setKeywords(eoJObj.getString("keyWords"))
                .setImportance((eoJObj.getJsonNumber("importance")).longValue())
                .setRelationships(getRelationships(eoJObj))
                .setNameplate(getNameplate(eoJObj))
                .setPower(getPower(eoJObj))
                .setEnergy(getEnergy(eoJObj))
                .setDemand(getDemand(eoJObj))
                .setPowerControls(getPowerControls(eoJObj))
                .build();
		} catch (Exception ex) {
			LOG.info(  "EmanHTTPBinding.getEnergyObject: catch Error: " + ex.getMessage(), ex);
		} 
        return (energyObject);
    }

    /*
    *   parse Json and write to corresponding java object
    */
    private PowerInterfaces getPowerInterfaces(JsonObject eoDeviceJObj) {
        PowerInterfaces powerInterfaces = null;
        PowerInterface powerInterface = null;

        try {        
             // create top-level object and add an array object. As we read data, add to array
            if (powerInterfaces == null) {
                 powerInterfaces = new PowerInterfacesBuilder()
                .setPowerInterface(powerInterfaceList)
                .build();
            }

			JsonObject pisJObj = eoDeviceJObj.getJsonObject("powerInterfaces");
			JsonArray pisJArr = pisJObj.getJsonArray("powerInterface");
			
			for (int i=0; i< pisJArr.size(); i++ ) {
			    JsonObject piJObj = pisJArr.getJsonObject(i);
                JsonObject eoJObj = piJObj.getJsonObject("energyObject");

			    powerInterface = new PowerInterfaceBuilder()
                    .setId(eoDeviceJObj.getInt("id"))
                    .setEnergyObject(getEnergyObject(eoJObj))
                    .build();
                powerInterfaceList.add(powerInterface);
			}
		} catch (Exception ex) {
			LOG.info(  "EmanHTTPBinding.getPowerInterfaces: catch Error: " + ex.getMessage(), ex);
		} 

        return (powerInterfaces);
    }

    /*
    *   parse Json and write to corresponding java object
    */
    private Components getComponents(JsonObject eoDeviceJObj) {
        Components components = null;
        Component component = null;

        try {        
             // create top-level object and add an array object. As we read data, add to array
            if (components == null) {
                 components = new ComponentsBuilder()
                .setComponent(componentList)
                .build();
            }

			JsonObject JObj = eoDeviceJObj.getJsonObject("components");
			JsonArray JArr = JObj.getJsonArray("component");
			
			for (int i=0; i< JArr.size(); i++ ) {
			    JsonObject piJObj = JArr.getJsonObject(i);
                JsonObject eoJObj = piJObj.getJsonObject("energyObject");

			    component = new ComponentBuilder()
                    .setId(eoDeviceJObj.getInt("id"))
                    .setEnergyObject(getEnergyObject(eoJObj))
                    .build();
                componentList.add(component);
			}
		} catch (Exception ex) {
			LOG.info(  "EmanHTTPBinding.getComponents: catch Error: " + ex.getMessage(), ex);
		} 

        return (components);
    }

    /*
    *   parse Json and write to corresponding java object
    */
    public EoDevice getEoDevice(String deviceIP, int key) {
		String targetUrl = "http://"+deviceIP;
		JsonReader rdr = null;
		String str = null;	
		EoDevice eoDevice = null;	

		LOG.info(  "EmanHTTPBinding.getEoDevice: targetUrl " + targetUrl);

		CloseableHttpClient httpClient = HttpClients.createDefault();

		try {
			HttpGet httpGet = new HttpGet(targetUrl);

			CloseableHttpResponse httpResponse = httpClient.execute(httpGet);
			HttpEntity responseEntity = httpResponse.getEntity();

			InputStream in = responseEntity.getContent();			
			java.util.Scanner s = new java.util.Scanner(in).useDelimiter("\\A");
            str = s.hasNext() ? s.next() : "";
//		    LOG.info(  "EmanHTTPBinding: getEoDevice: str " + str);

			rdr = Json.createReader(new StringReader(str));
			JsonObject obj = rdr.readObject();			
			JsonObject eoDevicesObj = obj.getJsonObject("eoDevices");
		    LOG.info(  "EmanHTTPBinding: getEoDevice: eoDevicesObj " + eoDevicesObj.toString());
			JsonArray eoDeviceArr = eoDevicesObj.getJsonArray("eoDevice");
		    LOG.info(  "EmanHTTPBinding: getEoDevice: eoDevice " + eoDeviceArr.toString());
		    LOG.info(  "EmanHTTPBinding: getEoDevice: eoDevice.size() " + eoDeviceArr.size());
			
			for (int i=0; i< eoDeviceArr.size(); i++ ) {
			    JsonObject eoDeviceJObj = eoDeviceArr.getJsonObject(i);
                JsonObject eoJObj = eoDeviceJObj.getJsonObject("energyObject");

			    eoDevice = new EoDeviceBuilder()
                    .setId(eoDeviceJObj.getInt("id"))
                    .setEocategory(Eocategory.valueOf(eoDeviceJObj.getString("eocategory")))
                    .setEnergyObject(getEnergyObject(eoJObj))
                    .setPowerInterfaces(getPowerInterfaces(eoDeviceJObj))
                    .setComponents(getComponents(eoDeviceJObj))
                    .build();
			}
		} catch (Exception ex) {
			LOG.info(  "EmanHTTPBinding.getEoDevice: catch Error: " + ex.getMessage(), ex);
		} finally {
			try {
			    rdr.close();
				httpClient.close();
			} catch (IOException ex) {
				LOG.info(  "EmanHTTPBinding.getEoDevice: finally Error: " + ex.getMessage(), ex);
			}
		}
            
        return eoDevice;
	}
}