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

Once the shells starts, target the dataflow application URL as directed in the instructions, and import the rabbit binder apps.

Now you are ready to create the azure source and sink apps.  Run these commands to install the apps, either from my blob store on azure, or your own blobstore, which must be accessible from your PCF environment (not a local file URL).

```
app register --name azure-iot-hub --type source --uri https://mjeffriesblob.blob.core.windows.net/jars/azure-iot-hub-0.0.1-SNAPSHOT.jar
app info --id source:azure-iot-hub
```

```
app register --name azure-iot-output --type sink --uri https://mjeffriesblob.blob.core.windows.net/jars/azure-iot-output-0.0.1-SNAPSHOT.jar
app info --id sink:azure-iot-output
```

Now you are ready to create and deploy the stream to use these apps.  Just substitute the Azure IoT Hub values from when you set up your IoT hub
on Azure, and select a unique STREAM_NAME.

```
stream create --name STREAM_NAME --definition "azure-iot-hub --hubendpoint=HUB_ENDPOINT_URL --hubkey=HUB_KEY--hubname=HUB_NAME | azure-iot-output --hostname=HOST_NAME --hubkey=HUB_KEY"

stream deploy --name STREAM_NAME --properties "app.azure-iot-hub.spring.cloud.deployer.cloudfoundry.memory=2048,app.azure-iot-output.spring.cloud.deployer.cloudfoundry.memory=2048,app.azure-iot-output.spring.cloud.deployer.cloudfoundry.services=redis"

stream list
```

Now you can build and push the azure-iot-device app to PCF.  Here is a sample manifest.yml content, just create this file in the azure-iot-device folder and again substitute your values.  Login to your PCF environment and run "cf push" from the azure-iot-device folder.

```
---
applications:
- name: azure-device-YOUR_INITIALS
  memory: 1G
  buildpack: https://github.com/cloudfoundry/java-buildpack
  path: ./target/azure-iot-device-0.0.1-SNAPSHOT.jar
  env:
    HOSTNAME: HOST_NAME
    DEVICE_ID: DEVICE_ID
    SHARED_ACCESS_KEY: SHARED_ACCESS_KEY
```



    





