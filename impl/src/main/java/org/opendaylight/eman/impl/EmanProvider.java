/*
 * Copyright © 2015 2016, Comcast Corporation and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.eman.impl;

import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.common.RpcResult;
import org.opendaylight.yangtools.yang.common.RpcResultBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Future;
import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.md.sal.binding.api.WriteTransaction;
import org.opendaylight.controller.md.sal.common.api.data.LogicalDatastoreType;
import org.opendaylight.controller.md.sal.common.api.data.TransactionCommitFailedException;

import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.eman.rev171208.EmanService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.eman.rev171208.EoDevices;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.eman.rev171208.EoDevicesBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.eman.rev171208.eodevices.EoDevice;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.eman.rev171208.eodevices.EoDeviceBuilder;

import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.eman.rev171208.PollEoDeviceInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.eman.rev171208.PollEoDeviceOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.eman.rev171208.PollEoDeviceOutputBuilder;

/*
*   EmanProvider implements EnamService. includes pollEoDevice RPC to read config data from device and write to MD-SAL
*/
public class EmanProvider implements EmanService, AutoCloseable {
    private static final Logger LOG = LoggerFactory.getLogger(EmanProvider.class);
    private final DataBroker dataBroker;
/*
    private final EmanSNMPBinding snmpBinding = new EmanSNMPBinding();
 */
    private final EmanHTTPBinding httpBinding = new EmanHTTPBinding();
    private final List<EoDevice> eoDeviceList = new ArrayList<>();
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
        String  msg = null;

        LOG.info("EmanProvider: pollEoDevice: ");

        // create top-level object and add an array object. As we read data, add to array
        if (eoDevices == null) {
             eoDevices = new EoDevicesBuilder()
            .setEoDevice(eoDeviceList)
            .build();
        }

         // parse input
         String deviceIP = input.getDeviceIP();
         String protocol = input.getProtocol();
         int numSamples = input.getNumSamples();
         int period = input.getPeriod();
         
         LOG.info("EmanProvider: pollEoDevice: deviceIP " + deviceIP + "protocol "+ protocol + "numSamples %d period %d" ,numSamples, period);

         LOG.info("EmanProvider: pollEoDevice: call binding");
        // SNMP binding has not been maintained
        if (protocol.equals("SNMP") ) {
            LOG.info("EmanProvider: pollEoDevice: snmp");
//            eoDevice = snmpBinding.getDevicePwrMsrSNMP(deviceIP, 0);
        }
        else {
            LOG.info("EmanProvider: pollEoDevice: http");
            eoDevice = httpBinding.getEoDevice(deviceIP, 0);            
        }

        if (eoDevice != null) {                    
            eoDeviceList.add(eoDevice);

            // Simple writes to MD-SAL of eoDevice and associated EoPowerMeasurement
            //   To do: extend to support flexible writes of entire model
            InstanceIdentifier<EoDevices> iid = InstanceIdentifier.create(EoDevices.class);
            WriteTransaction tx = dataBroker.newWriteOnlyTransaction();
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
