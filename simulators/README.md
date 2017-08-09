# OpenDaylight Energy Management project

The eman (energy Management) project provides an management framework for 
energy aware devices, allowing for measurement and optimization of energy usage.

The contents of eman/simulator are assets used to provide mock instances of snmp and http
data producers, i.e. energy aware devices.

HTTP simulator
EOserver.py is a simple python web server that implements HTTP GET to provide an
eoPowerMeasurement table. Usage is described in the file header
 
SNMP Simulator
energy-object.snmprec is a data file used by the snmpd agent to provide a representation 
of eoPowerMeasurement table via SNMP. 
 
The following describes a way to install and configure an SNMP simulator
on localhost.

on MAC OS, open terminal

1. Install snmpsim.
    $ sudo apt install snmpsim
 
2. configure filesystem
    mkdir ~/.snmpsim, then mkdir ~/.snmpsim/data/
    
3. Install mock data. This file is used by pysnmp to provide mock data for an APSIS agent
    copy eman/simulators/data/energy-object.snmprec to ~/.snmpsim/data/.
    
4. launch snmp simulator:
    $ sudo snmpsimd.py --agent-udpv4-endpoint=127.0.0.1:161
        —process-group=<your group> —process-user=<your user>
    
5. VerifyOpen another terminal window and execute:
    $ snmpget -v2c -c energy-object localhost:161 1.3.6.1.2.1.229.0.1.0.

    The result should be ‘1’, as defined in your snmprec file

.. note:: group and user are settings within our local OS.
For Mac users, look at settings/users and groups.
If port 161 is not available, use another unprivileged port such as 1161.

.. note:: snmpget queries snmpsimd to return a value for the OID 1.3.6.1.2.1.229.0.1.0.
According to the energy-object.snmprec file, the value for that OID is ‘1’.
Try other OIDs, or edit the snmprec file to see your results



