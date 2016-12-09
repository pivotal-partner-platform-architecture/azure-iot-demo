# azure-iot-demo
Demo application of a virtual device running on the Azure IoT Hub.

# Description
This demo is composed of the following applications:

# azure-iot-device
This application represents a virtual wind speed device which shows the current wind speed as a dial on the UI, and also
sends the current wind speed to the Azure IoT Hub.  It also listens for commands sent to the device from the Azure IoT Hub.
 
# azure-iot-hub
This application listens for messages send to the Azure IoT Hub, and publishes them to the Spring Cloud Data Flow (SCDF) pipeline.  This application
is a SCDF source app.

# azure-iot-output
This application is a SCDF sink which listens on the pipeline for messages, and calculates the average wind speed over the last 30 seconds or so.  If 
the average wind speed is less than 10 mph, it sends a command to the virtual device to set the background color to yellow.  Likewise, an average between
10 and 30 is green, and above 30 is red.  Commands are sent to the device only when the color needs to be changed.

# Azure IoT Hub setup
You'll need an Azure account, and create an IoT Hub to use, follow the directions [here](https://docs.microsoft.com/en-us/azure/iot-hub/iot-hub-java-java-getstarted) including first sample application to create a device identity.  As you go along, be sure to capture:
* Hostname
* iothubowner Connection String
* iothubowner Primary Key
* Messaging Event Hub Compatible Name
* Messaging Event Hub Compatible Endpoint
* Sample application output Device ID
* Sample application output Device Key


# How to run these apps on Pivotal Cloud Foundry
TODO



