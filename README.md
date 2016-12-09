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
You'll need an Azure account, and create an IoT Hub to use, follow the directions [here](https://docs.microsoft.com/en-us/azure/iot-hub/iot-hub-java-java-getstarted) up to and including the first sample application to create a device identity.  As you go along, be sure to capture:
* Hostname
* iothubowner Connection String
* iothubowner Primary Key
* Messaging Event Hub Compatible Name
* Messaging Event Hub Compatible Endpoint
* Sample application output Device ID
* Sample application output Device Key

# Spring Cloud Data Flow setup
Spring Cloud Data Flow [SCDF](https://cloud.spring.io/spring-cloud-dataflow/) is a great tool for creating data microservices which can be deployed to Pivotal
Cloud Foundry(PCF).  This demo runs SCDF on PCF, so start [here](http://cloud.spring.io/spring-cloud-dataflow-server-cloudfoundry/) for details on setting up SCDF on PCF. I used the release version 1.0.1 for this demo, so click [here](http://docs.spring.io/spring-cloud-dataflow-server-cloudfoundry/docs/1.0.1.RELEASE/reference/htmlsingle/) for step by step instructions.   

You'll need a PCF environment available, with MySQL, RabbitMQ, and Redis installed.  We're using the SCDF Rabbit bindings for this demo, not the Kafka bindings.  Follow the instructions above to download the jar files, deploy the dataflow server application to PCF, and then run the shell locally.




