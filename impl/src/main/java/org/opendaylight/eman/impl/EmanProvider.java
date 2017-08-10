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
import org.opendaylight.yang.gen.v1.urn.opendaylight.eman.rev170105.EoDevices;
import org.opendaylight.yang.gen.v1.urn.opendaylight.eman.rev170105.GetEoAttributeInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.eman.rev170105.GetEoAttributeOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.eman.rev170105.GetEoAttributeOutputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.eman.rev170105.GetEoDevicePowerMeasuresInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.eman.rev170105.GetEoDevicePowerMeasuresOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.eman.rev170105.GetEoDevicePowerMeasuresOutputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.eman.rev170105.SetEoAttributeInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.eman.rev170105.SetEoAttributeOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.eman.rev170105.SetEoAttributeOutputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.eman.rev170105.eodevices.EoDevice;
import org.opendaylight.yang.gen.v1.urn.opendaylight.eman.rev170105.eodevices.EoDeviceBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.eman.rev170105.eodevices.EoDeviceKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.eman.rev170105.eomeasurementgroup.EoPowerMeasurement;
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
    private final List<EoDevice> eoDeviceList = new ArrayList<>();

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

    /* Implements getEoAttribute RPC from eman.yang. Is intended to query device capability and
        interface with device using appropriate protocol. Currently assumes SNMP
        To do: generalize to arbitrary device-level protocol
    */
    @Override
    public Future<RpcResult<GetEoAttributeOutput>> getEoAttribute(GetEoAttributeInput input) {
         LOG.info("EmanProvider: getEoAttribute: ");

         if (input == null) {
             LOG.info("EmanProvider: getEoAttribute: input equals null");
         }

         // parse input
         String deviceIP = input.getDeviceIP();
         String attribute = input.getAttribute();
         String msg = null;

         /* Hardcoded to query device via SNMP.
             To do: generalize to support other device level protocols
         */
 		if (false /* device capabilities == SNMP */) {
            msg = snmpBinding.getEoAttrSNMP(deviceIP, attribute);
        }
        else {
            msg = httpBinding.getEoPowerMeasurementAttribute(deviceIP, attribute);
        }

        GetEoAttributeOutput output = new GetEoAttributeOutputBuilder()
            .setResponse("Get attribute " + attribute + " " +msg)
            .build();
        return RpcResultBuilder.success(output).buildFuture();

     }

    /* Implements setEoAttribute RPC from eman.yang. Is intended to query device capability and
        interface with device using appropriate protocol. Currently assumes SNMP
        To do: generalize to arbitrary device-level protocol
    */
    @Override
    public Future<RpcResult<SetEoAttributeOutput>> setEoAttribute(SetEoAttributeInput input) {
         LOG.info("EmanProvider: setEoAttribute: ");

         // parse input
         String deviceIP = input.getDeviceIP();
         String attribute = input.getAttribute();
         String value = input.getValue();
         String msg = null;

         // Hardcoded to interface w device via SNMP
 		if (false /* device capabilities == SNMP */) {
            msg = snmpBinding.setEoPowerMeasurementAttribute(deviceIP, attribute, value);
        }
        else {
            msg = httpBinding.setEoPowerMeasurementAttribute(deviceIP, attribute, value);
        }

        SetEoAttributeOutput output = new SetEoAttributeOutputBuilder()
            .setResponse("Power attribute " + msg)
            .build();
        return RpcResultBuilder.success(output).buildFuture();
    }


    /* Implements getEoDevicePowerMeasures RPC from eman.yang.
        Queries device for powerMeasures and writes to MD_SAL.
        Is intended to query device capability and
        interface with device using appropriate protocol. Currently assumes SNMP
        To do: generalize to arbitrary device-level protocol
    */
    @Override
    public Future<RpcResult<GetEoDevicePowerMeasuresOutput>> getEoDevicePowerMeasures(GetEoDevicePowerMeasuresInput input) {
         List<EoPowerMeasurement> pwrMList;
         EoDevice eoDevice;
 		 String msg;

         LOG.info("EmanProvider: getDevicePowerMeasures: ");

         // parse input
         String deviceIP = input.getDeviceIP();
         String protocol = input.getProtocol().getName();
         EoPowerMeasurement pwrM = null;

         // eoDeviceList is class collection of eoDevices
         if (eoDeviceList.isEmpty()) {
            eoDevice = new EoDeviceBuilder()
               .setKey( new EoDeviceKey(0) )
               .setIPAddress(deviceIP)
               .setEoPowerMeasurement(new ArrayList<EoPowerMeasurement>())
               .build();

            eoDeviceList.add(eoDevice);
         }
         else {
            // To Do: search for existing device w this IP, or create
             eoDevice = eoDeviceList.get(0);
         }

			
        pwrMList = eoDevice.getEoPowerMeasurement();
        int key = pwrMList.size();
 		// TBD: add logic to determine southbound protocol based on device capabilities
 		if (protocol.equals("snmp")) {
            pwrM = snmpBinding.getEoPowerMeasurement(deviceIP, key);            
        } 
        else {
            pwrM = httpBinding.getEoPowerMeasurement(deviceIP, key);            
            LOG.info("EmanProvider: using httpBinding: ");
        }
        
        if (pwrM != null) {
        
            pwrMList.add(pwrM);

            /* Simple writes to MD-SAL of eoDevice and associated EoPowerMeasurement
                To do: extend to support flexible writes of entire model
            */
            WriteTransaction tx = dataBroker.newWriteOnlyTransaction();
            InstanceIdentifier<EoDevice> iid = InstanceIdentifier.create(EoDevices.class).child(EoDevice.class, eoDevice.getKey());
            tx.put(LogicalDatastoreType.OPERATIONAL, iid, eoDevice);
            try {
                tx.submit().checkedGet();
            } catch (TransactionCommitFailedException e) {
                LOG.error("Transaction failed: {}", e.toString());
            }

            InstanceIdentifier<EoPowerMeasurement> pid = iid.builder().child(EoPowerMeasurement.class, pwrM.getKey()).build();
            WriteTransaction tx2 = dataBroker.newWriteOnlyTransaction();
            tx2.put(LogicalDatastoreType.OPERATIONAL, pid, pwrM);
            try {
                tx2.submit().checkedGet();
            } catch (TransactionCommitFailedException e) {
                LOG.error("Transaction failed: {}", e.toString());
            }
            msg = "getEoDevicePowerMeasures: success";
        }
        else {
            LOG.error("getEoDevicePowerMeasures: pwrM == null");
            msg = "getEoDevicePowerMeasures: failed";
        }

        GetEoDevicePowerMeasuresOutput output = new GetEoDevicePowerMeasuresOutputBuilder()
            .setResponse(msg)
            .build();
        return RpcResultBuilder.success(output).buildFuture();
    }

}
