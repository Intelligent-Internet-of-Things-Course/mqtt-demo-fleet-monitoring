# Fleet Vehicle Emulator & Demo

Simple Vehicle Emulator simulating a vehicle moving on a GPS path
defined through a GPX file. 

Each GPS sample is sent through MQTT to a target MQTT Broker configured through 
a configuration file. 

When the vehicle ends the available waypoints in the file it starts moving backward 
on the same path

The library used to process and parse the GPX file is JPX: https://github.com/jenetics/jpx

Each vehicle provides and use also a Battery Level Resource to monitor its internal electric energy management.