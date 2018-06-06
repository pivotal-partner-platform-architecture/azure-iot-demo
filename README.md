# azure-iot-demo
Demo application of a virtual device running on the Azure IoT Hub, publishing events to a Spring Cloud Data Flow (SCDF) server and receiving commands back.

# Description
This demo is composed of the following applications:

# azure-iot-device
This application represents a virtual wind speed device which shows the current wind speed as a dial on the UI, and also
sends the current wind speed to the Azure IoT Hub.  It also listens for commands sent to the device from the Azure IoT Hub.

![Azure Demo App](/images/azureapp.jpeg)

# azure-iot-create
This application is used to add a device to the Azure IoT Hub.  You supply the connection string and device id to add, and the application will display the device key.

# azure-iot-hub
This application listens for messages send to the Azure IoT Hub, and publishes them to the Spring Cloud Data Flow (SCDF) pipeline.  This application
is an SCDF source app.

# azure-iot-output
This application is an SCDF sink which listens on the pipeline for messages, calculates the average wind speed over the last 30 seconds or so, and stores the results in Redis.  If
the average wind speed is less than 10 mph, it sends a command to the virtual device to set the background color to yellow.  Likewise, an average between
10 and 30 is green, and above 30 is red.  Commands are sent to the device only when the color needs to be changed.

# azureiothub-spring-boot-autoconfigure
Spring boot starter auto config project.  It contains the azure libraries as well as components that
can be autowired to your application.  You don't include this project directly as a dependency - instead
you specify the azureiothub-spring-boot-starter dependency, which brings this project along for the ride.

# azureiothub-spring-boot-starter
Spring boot starter project for the Azure IoT Hub.  Any applications wanting to use the azure library
and/or common components just need to include this starter as a maven POM dependency.

# Azure IoT Hub setup
You'll need an Azure account, and an IoT Hub to use. Follow the directions [here]( https://docs.microsoft.com/en-us/azure/iot-hub/quickstart-send-telemetry-java) up to but not including the first sample application to create a device identity.  As you go along, be sure to capture:
* IoT Hub Name
* Device ID
* IoT Hub Hostname
* Device Connection String
* Event Hub Compatible Endpoint
* Event Hub Compatible Path
* IOT Hub owner Primary Key
* IoT Hub Connection String

Here is an example of the Azure CLI commands in the link above, and the results of each command:

```
// Add required extension
az extension add --name azure-cli-iot-ext

// Create the device - the IoT Hub Name is mjeffries-iot-hub, and the Device ID is MyJavaDevice:
az iot hub device-identity create --hub-name mjeffries-iot-hub --device-id MyJavaDevice
{
  "authentication": {
    "symmetricKey": {
      "primaryKey": "2kX/eAIsbhPFzsCVzs8FuSm2/Ajute85uTa4Fkt1H5I=",
      "secondaryKey": "CBhs/fmu8Don1hMbGocNsyxK5aAN61K72hHXp7ORVpE="
    },
    "type": "sas",
    "x509Thumbprint": {
      "primaryThumbprint": null,
      "secondaryThumbprint": null
    }
  },
  "capabilities": {
    "iotEdge": false
  },
  "cloudToDeviceMessageCount": 0,
  "connectionState": "Disconnected",
  "connectionStateUpdatedTime": "0001-01-01T00:00:00",
  "deviceId": "MyJavaDevice",
  "etag": "ODQxNDA5NDU1",
  "generationId": "636633062050290058",
  "lastActivityTime": "0001-01-01T00:00:00",
  "status": "enabled",
  "statusReason": null,
  "statusUpdatedTime": "0001-01-01T00:00:00"
}

// Device Connection String - the IoT Hub Name is mjeffries-iot-hub, and the Device ID is MyJavaDevice:
az iot hub device-identity show-connection-string --hub-name mjeffries-iot-hub --device-id MyJavaDevice --output table
-------------------------------------------------------------------------------------------------------------------------------
HostName=mjeffries-iot-hub.azure-devices.net;DeviceId=MyJavaDevice;SharedAccessKey=2kX/eAIsbhPFzsCVzs8FuSm2/Ajute85uTa4Fkt1H5I=

Note that the Hostname is the first part of the connection string, ex, mjeffries-iot-hub.azure-devices.net

// Events hub compatible Endpoint - the IoT Hub Name is mjeffries-iot-hub:
az iot hub show --query properties.eventHubEndpoints.events.endpoint --name mjeffries-iot-hub
"sb://ihsuproddmres020dednamespace.servicebus.windows.net/"

// Events Hub Compatible path - the IoT Hub Name is mjeffries-iot-hub:
az iot hub show --query properties.eventHubEndpoints.events.path --name mjeffries-iot-hub
"iothub-ehub-mjeffries-488688-1cf9d45391"

// IOT Hub owner Primary Key- the IoT Hub Name is mjeffries-iot-hub:
az iot hub policy show --name iothubowner --query primaryKey --hub-name mjeffries-iot-hub
"yvtBsXWJxEntKfiHWV5wBRuOTZpgidIFXx54NMc8IXc="

// IoT Hub Connection String - the IoT Hub Name is mjeffries-iot-hub:
az iot hub show-connection-string --hub-name mjeffries-iot-hub --output table
-----------------------------------------------------------------------------------------------------------------------------------------
HostName=mjeffries-iot-hub.azure-devices.net;SharedAccessKeyName=iothubowner;SharedAccessKey=yvtBsXWJxEntKfiHWV5wBRuOTZpgidIFXx54NMc8IXc=

```


# Build the projects
To build all the projects, just open a shell in the root of the project, and run
```
cd azure-iot-demo
mvn clean package
```

After building the azure-iot-hub and azure-iot-output projects, upload the jar files to the Azure blob store (or S3 or maven repo).

# Get your virtual device key from the Azure IoT Hub
Use the azure-iot-create application to add a device to your IoT Hub account.  Supply the entire connection string in double quotes for the first argument, and the new device ID as the second argument (ex. myFirstJavaDevice).  The device should already exist from a previous step, but it will be created if it doesn't already exist.

```
java -jar azure-iot-create/target/create-device-identity-1.0-SNAPSHOT.jar [IoT Hub Connection String] [Device ID]

For example:
java -jar azure-iot-create/target/create-device-identity-1.0-SNAPSHOT.jar  "HostName=mjeffries-iot-hub.azure-devices.net;SharedAccessKeyName=iothubowner;SharedAccessKey=yvtBsXWJxEntKfiHWV5wBRuOTZpgidIFXx54NMc8IXc=" MyJavaDevice

conn: HostName=mjeffries-iot-hub.azure-devices.net;SharedAccessKeyName=iothubowner;SharedAccessKey=yvtBsXWJxEntKfiHWV5wBRuOTZpgidIFXx54NMc8IXc=

Device id: MyJavaDevice

Device already exists!

Device id: MyJavaDevice

Device key: 2kX/eAIsbhPFzsCVzs8FuSm2/Ajute85uTa4Fkt1H5I=

```
Please sure to capture the displayed device key that was generated, you'll need it for your application manifest below.

# Spring Cloud Data Flow setup
Spring Cloud Data Flow (https://cloud.spring.io/spring-cloud-dataflow/) is a great tool for creating data microservices which can be deployed to Pivotal
Cloud Foundry(PCF).  This demo runs SCDF on PCF, so start [here](http://cloud.spring.io/spring-cloud-dataflow-server-cloudfoundry/) for details on setting up SCDF on PCF. We used the release version 1.0.1 for this demo, so click [here](http://docs.spring.io/spring-cloud-dataflow-server-cloudfoundry/docs/1.0.1.RELEASE/reference/htmlsingle/) for step by step instructions.   

You'll need a PCF environment available, with MySQL, RabbitMQ, and Redis installed.  We ran the demo apps on our PCF instance running on Azure, but it will work on any IaaS.
We're using the SCDF Rabbit bindings for this demo, not the Kafka bindings.  Follow the instructions above to download the jar files, deploy the dataflow server application to PCF, and then run the shell locally.

Once the shells starts, target the dataflow application URL as directed in the instructions, and import the rabbit binder apps.

Now you are ready to create the azure source and sink apps.  Run these commands to install the apps, either from my blob store on azure, your own blobstore, or your maven repo, which must be accessible from your PCF environment (not a local file URL).  If you have a maven repo, see the SCDF docs for the maven repo syntax.

```
app register --name azure-iot-hub --type source --uri https://mjeffriesblob.blob.core.windows.net/jars/azure-iot-hub-0.0.1-SNAPSHOT.jar
app info --id source:azure-iot-hub
```

```
app register --name azure-iot-output --type sink --uri https://mjeffriesblob.blob.core.windows.net/jars/azure-iot-output-0.0.1-SNAPSHOT.jar
app info --id sink:azure-iot-output
```

Now you are ready to create and deploy the stream to use these apps.  Just substitute the Azure IoT Hub values from when you set up your IoT hub
on Azure, and substitute a unique value for STREAM_NAME.

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
    SHARED_ACCESS_KEY: DEVICE_KEY
```

Once the app is deployed, note the URL from the output, and load the app into your browser.  Click the "Start" button to start sending data to the Azure IoT Hub, and "Stop" to pause the data.  You can use the "+" and "-" buttons to adjust the wind speed, then wait around 30 seconds for the background color to change (at 10 and 30 mph).

Use "cf apps" to get the name of the apps deployed to PCF by SCDF, and use "cf logs" to see the messages processed by each component application.
