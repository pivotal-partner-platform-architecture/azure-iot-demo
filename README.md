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
You'll need an Azure account, and an IoT Hub to use. Follow the directions [here](https://docs.microsoft.com/en-us/azure/iot-hub/iot-hub-java-java-getstarted) up to but not including the first sample application to create a device identity.  As you go along, be sure to capture:
* Hostname
* iothubowner - Connection String
* iothubowner - Primary Key
* Messaging - Event Hub Compatible Name
* Messaging - Event Hub Compatible Endpoint

# Add your virtual device to Azure IoT Hub
Use the azure-iot-create application to add a device to your IoT Hub account.  Supply the entire connection string in double quotes for the first argument, and the new device ID as the second argument (ex. myFirstJavaDevice).

```
cd azure-iot-create
mvn clean package
java -jar target/create-device-identity-1.0-SNAPSHOT.jar CONNECTION_STRING DEVICE_ID
```
Please sure to capture the displayed device key that was generated, you'll need it for your application manifest below.

# Build the other applications
```
cd azure-iot-hub
mvn clean package -DskipTests

cd azure-iot-device
mvn clean package -DskipTests

cd azure-iot-output
mvn clean package -DskipTests

```
After building the azure-iot-hub and azure-iot-output projects, upload the jar files to the Azure blob store (or S3 or maven repo).

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
