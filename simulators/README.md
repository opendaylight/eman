# OpenDaylight Energy Management project

The eman (energy Management) project provides an management framework for 
energy aware devices, allowing for measurement and optimization of energy usage.

The contents of eman/simulator are assets used to provide mock instances of snmp and http
data producers, i.e. energy aware devices.

EOserver.py is a simple python web server that implements HTTP GET to provide an
 eoPowerMeasurement table. Usage is described in the file header
 
 energy-object.snmprec is a data file used by the snmpd agent to provide a representation 
 of eoPowerMeasurement table via SNMP. See OpenDaylight.org eman User Guide for instructions 
 on how to install and use snmpd.


