# OpenDaylight Energy Management project

The eman (energy Management) project provides an management framework for 
energy aware devices, allowing for measurement and optimization of energy usage.

The contents of eman/simulator are assets used to provide mock instances of snmp and http
data producers; e.g. virtual energy aware devices.

Both simulators use python. Instructions for installing python are not provided here. 

We assume the reader is familiar with how to obtain, install and launch OpenDaylight. 
If not, please see http://docs.opendaylight.org/en/latest/getting-started-guide/index.html
for instructions

SNMP simulation
EOserver.py is a simple python web server that implements HTTP GET to provide an
 eoPowerMeasurement table. Usage is described in the file header
 
 energy-object.snmprec is a data file used by the snmpd agent to provide a representation 
 of eoPowerMeasurement table via SNMP. See OpenDaylight.org eman User Guide for instructions 
 on how to install and use snmpd.
 
 HTTP simulation
 In <yourpath>/eman/simualors/HHTP-simulator find two files:
 eoDeviceHTTPSim.py - python http server to handle HTTP GET requests from ODL eman
 eoDeviceMeasures.json - sample data
 
 1) To launch http simulator
 open a terminal 
 $ cd <yourpath>/eman/simualors/HHTP-simulator
 $ python eoDeviceHTTPSim.py &
 
 (to stop simulator, $ kill %python)
 
 - test
 $ curl http://localhost:9000
 { "eoDevices" : ...
 
2) To populate MD-SAL with contents of sample data and read data

open a terminal and launch ODL w/ eman features. 

from ODL client, such as Dlux, invoke 

POST /operations/eman:poll-eoDevice with body:
deviceIP: 127.0.0.1:9000
protocol: http
numSamples: 0
period: 0

This will populate MD-SAL with contents of sample data. To read sample data from MD-SAL API

GET <host>/operational/eman:eoDevices/EoDeviceEntry/0/EoDevice


