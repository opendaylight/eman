/*
 * Copyright © 2015 2016, Comcast Corporation and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.eman.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Future;
import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.md.sal.binding.api.WriteTransaction;
import org.opendaylight.controller.md.sal.common.api.data.LogicalDatastoreType;
import org.opendaylight.controller.md.sal.common.api.data.TransactionCommitFailedException;
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

import org.opendaylight.yang.gen.v1.urn.opendaylight.eman.rev170105.PollEoDeviceInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.eman.rev170105.PollEoDeviceOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.eman.rev170105.PollEoDeviceOutputBuilder;


import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.common.RpcResult;
import org.opendaylight.yangtools.yang.common.RpcResultBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EmanProvider implements EmanService, AutoCloseable {

    private static final Logger LOG = LoggerFactory.getLogger(EmanProvider.class);
    private final EmanSNMPBinding snmpBinding = new EmanSNMPBinding();
    private final EmanHTTPBinding httpBinding = new EmanHTTPBinding();
    private final DataBroker dataBroker;
    private final List<EoDeviceEntry> eoDeviceEntryList = new ArrayList<>();
    private  EoDevices eoDevices = null;

    public EmanProvider(DataBroker dataBroker) {
        this.dataBroker = dataBroker;
    }

    public void init() {
        LOG.info("EmanProvider initialized");
    }

    @Override
    public void close() {
        LOG.info("EmanProvider Closed");
    }

    /* Implements getEoDevicePowerMeasures RPC from eman.yang.
        Queries device for powerMeasures and writes to MD_SAL.
        Is intended to query device capability and
        interface with device using appropriate protocol. Currently assumes SNMP
        To do: generalize to arbitrary device-level protocol
    */
    @Override
    public Future<RpcResult<PollEoDeviceOutput>> pollEoDevice(PollEoDeviceInput input) {
         EoDevice eoDevice = null;
         EoDeviceEntry eoDeviceEntry = null;
         
         String  msg = null;

         LOG.info("EmanProvider: pollEoDevice: ");

         // parse input
         String deviceIP = input.getDeviceIP();
         String protocol = input.getProtocol();
         int numSamples = input.getNumSamples();
         int period = input.getPeriod();
         
         LOG.info("EmanProvider: pollEoDevice: deviceIP " + deviceIP + "protocol "+ protocol + "numSamples %d period %d" ,numSamples, period);

         LOG.info("EmanProvider: pollEoDevice: call binding");
        if (protocol.equals("SNMP") ) {
         LOG.info("EmanProvider: pollEoDevice: snmp");
//            eoDeviceEntry = snmpBinding.getDevicePwrMsrSNMP(deviceIP, 0);
        }
        else {
         LOG.info("EmanProvider: pollEoDevice: http");
            eoDevice = httpBinding.getEoDevice(deviceIP, 0);            
        }
        
        if (eoDevices == null) {
             eoDevices = new EoDevicesBuilder()
            .setEoDeviceEntry(eoDeviceEntryList)
            .build();
        }

        if (eoDevice != null) {   
        
            eoDeviceEntry = new EoDeviceEntryBuilder()
            .setKey( new EoDeviceEntryKey(0) )
            .setName("Frank")
            .setEoDevice(eoDevice)
            .build();
                 
            eoDeviceEntryList.add(eoDeviceEntry);

            // Simple writes to MD-SAL of eoDevice and associated EoPowerMeasurement
             //   To do: extend to support flexible writes of entire model
            WriteTransaction tx = dataBroker.newWriteOnlyTransaction();
            InstanceIdentifier<EoDevices> iid = InstanceIdentifier.create(EoDevices.class);
            tx.put(LogicalDatastoreType.OPERATIONAL, iid, eoDevices);
            try {
                tx.submit().checkedGet();
            } catch (TransactionCommitFailedException e) {
                LOG.error("EmanProvider.pollEoDevice: Transaction failed: {}", e.toString());
            }
        }
        else {
            LOG.error("EmanProvider.pollEoDevice: eoDevice == null");
            msg = "EmanProvider.pollEoDevice: failed";
        }

        PollEoDeviceOutput output = new PollEoDeviceOutputBuilder()
            .setResponse("EmanProvider.pollEoDevice: success")
            .build();
        return RpcResultBuilder.success(output).buildFuture();
    }

}
