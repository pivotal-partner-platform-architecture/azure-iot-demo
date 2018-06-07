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

# Upload the SCDF components JAR files
After building the azure-iot-hub and azure-iot-output projects, upload the jar files to the Azure blob store (or S3 or maven repo).  Be sure to
allow public read access to the uploaded files.
```
Example:
- https://mjeffries.blob.core.windows.net/scdf/azure-iot-hub-0.0.1-SNAPSHOT.jar
- https://mjeffries.blob.core.windows.net/scdf/azure-iot-output-0.0.1-SNAPSHOT.jar
```

# Get your virtual device key from the Azure IoT Hub
Use the azure-iot-create application to add a device to your IoT Hub account.  Supply the entire IoT Hub connection string in double quotes for the first argument, and the new device ID as the second argument (ex. myFirstJavaDevice).  The device should already exist from a previous step, but it will be created if it doesn't already exist.

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
Please sure to capture the displayed Device Key that was generated, you'll need it for your application manifest below.

# Spring Cloud Data Flow (SCDF)
Spring Cloud Data Flow (https://cloud.spring.io/spring-cloud-dataflow/) is a great tool for creating data microservices which can be deployed to Pivotal Cloud Foundry(PCF).  This demo runs SCDF on Pivotal Web Services (PWS), our hosted version of Pivotal Cloud Foundry.

# PWS setup
You'll need to have or create an account on PWS (https://run.pivotal.io), and enter a credit card number in order get an organization with sufficient quota (~10 GB) for the demo.  Once you're logged in, go ahead and create the services instances you'll need (RabbitMQ, Redis, MySQL):
```
cf create-service rediscloud 30mb redis
cf create-service cloudamqp lemur rabbit
cf create-service cleardb spark my_mysql
```

# SCDF setup
To start this section, create a new folder on your local machine, outside of this Github project, such as ~/scdf.  Then open your terminal and change to that directory.

Now dowload the SCDF files you'll need.  We're using SCDF release 1.5 for this demo.
```
wget http://repo.spring.io/release/org/springframework/cloud/spring-cloud-dataflow-server-cloudfoundry/1.5.0.RELEASE/spring-cloud-dataflow-server-cloudfoundry-1.5.0.RELEASE.jar

wget http://repo.spring.io/release/org/springframework/cloud/spring-cloud-dataflow-shell/1.5.0.RELEASE/spring-cloud-dataflow-shell-1.5.0.RELEASE.jar
```

# Deploy the dataflow server
In the same folder, create a deployment manifest for the application, and edit it to include your PWS account information.  The file should be
named "manifest.yml":
```
---
applications:
- name: data-flow-server
  host: data-flow-server-YOUR_INITIALS
  memory: 2G
  disk_quota: 2G
  instances: 1
  path: ./spring-cloud-dataflow-server-cloudfoundry-1.5.0.RELEASE.jar
  env:
    SPRING_APPLICATION_NAME: data-flow-server
    SPRING_CLOUD_DEPLOYER_CLOUDFOUNDRY_URL: https://api.run.pivotal.io
    SPRING_CLOUD_DEPLOYER_CLOUDFOUNDRY_ORG: YOUR_ORG
    SPRING_CLOUD_DEPLOYER_CLOUDFOUNDRY_SPACE: YOUR_SPACE
    SPRING_CLOUD_DEPLOYER_CLOUDFOUNDRY_DOMAIN: cfapps.io
    SPRING_CLOUD_DEPLOYER_CLOUDFOUNDRY_USERNAME: YOUR_USERNAME
    SPRING_CLOUD_DEPLOYER_CLOUDFOUNDRY_PASSWORD: YOUR_PASSWORD
    SPRING_CLOUD_DEPLOYER_CLOUDFOUNDRY_STREAM_SERVICES: rabbit
    SPRING_CLOUD_DEPLOYER_CLOUDFOUNDRY_TASK_SERVICES: my_mysql
    SPRING_CLOUD_DEPLOYER_CLOUDFOUNDRY_SKIP_SSL_VALIDATION: false
    SPRING_APPLICATION_JSON: '{"maven": { "remote-repositories": { "repo1": { "url": "https://repo.spring.io/libs-release"} } } }'
services:
- my_mysql
```
Now go ahead and deploy the dataflow server to PCF:
```
cf push
```

When the application deployment is completed, note the URL for the application (ex. http://data-flow-server-mj.cfapps.io), which you'll need below.

# Start the dataflow Shell locally
In the same folder, run the following command to start the shell, then enter the URL of the dataflow server application when prompted:

```
java -jar spring-cloud-dataflow-shell-1.5.0.RELEASE.jar
  ____                              ____ _                __
 / ___| _ __  _ __(_)_ __   __ _   / ___| | ___  _   _  __| |
 \___ \| '_ \| '__| | '_ \ / _` | | |   | |/ _ \| | | |/ _` |
  ___) | |_) | |  | | | | | (_| | | |___| | (_) | |_| | (_| |
 |____/| .__/|_|  |_|_| |_|\__, |  \____|_|\___/ \__,_|\__,_|
  ____ |_|    _          __|___/                 __________
 |  _ \  __ _| |_ __ _  |  ___| | _____      __  \ \ \ \ \ \
 | | | |/ _` | __/ _` | | |_  | |/ _ \ \ /\ / /   \ \ \ \ \ \
 | |_| | (_| | || (_| | |  _| | | (_) \ V  V /    / / / / / /
 |____/ \__,_|\__\__,_| |_|   |_|\___/ \_/\_/    /_/_/_/_/_/

1.5.0.RELEASE

Welcome to the Spring Cloud Data Flow shell. For assistance hit TAB or type "help".
server-unknown:>dataflow config server http://data-flow-server-mj.cfapps.io
Shell mode: classic, Server mode: classic
dataflow:>
```

Now enter the line below to install the default apps to SCDF:
```
app import --uri http://bit.ly/Celsius-SR1-stream-applications-rabbit-maven
```

# Install your custom apps
Now you are ready to create the azure source and sink apps.  Run these commands to install the apps, referencing the blobstore location.

```
app register --name azure-iot-hub --type source --uri https://mjeffries.blob.core.windows.net/scdf/azure-iot-hub-0.0.1-SNAPSHOT.jar
app info --id source:azure-iot-hub
```

```
app register --name azure-iot-output --type sink --uri https://mjeffries.blob.core.windows.net/scdf/azure-iot-output-0.0.1-SNAPSHOT.jar
app info --id sink:azure-iot-output
```

# Create your new SCDF Stream
Now you are ready to create and deploy the stream to use these apps.  Just substitute the values from when you set up your IoT hub
on Azure.

```
stream create --name iot-stream --definition "azure-iot-hub --hubendpoint=[Event Hub Compatible Endpoint] --hubkey=[IOT Hub owner Primary Key] --hubname=[Event Hub Compatible Path] | azure-iot-output --hostname=[IoT Hub Hostname] --hubkey=[IOT Hub owner Primary Key]"

Example:
stream create --name iot-stream --definition "azure-iot-hub --hubendpoint=sb://ihsuproddmres020dednamespace.servicebus.windows.net/ --hubkey=yvtBsXWJxEntKfiHWV5wBRuOTZpgidIFXx54NMc8IXc= --hubname=iothub-ehub-mjeffries-488688-1cf9d45391 | azure-iot-output --hostname=mjeffries-iot-hub.azure-devices.net --hubkey=yvtBsXWJxEntKfiHWV5wBRuOTZpgidIFXx54NMc8IXc="
```

# Deploy your SCDF Stream
Now you can deploy the stream.  This will take a few minutes as the dataflow server will create and deploy 2 new apps to PCF, one for each of the
new SCDF components you created above.

```
stream deploy --name iot-stream --properties "deployer.azure-iot-hub.memory=2g,deployer.azure-iot-output.memory=2g,deployer.azure-iot-output.services=redis"

stream list
```

# Validate that the azure-iot-output app was bound to Redis
The deploy command above should deploy the azure-iot-output app, and bind it to the Redis service instance we created earlier.  You can run the "cf services"
 command to see if the azure-iot-output app is listed as a bound app for the redis service:
```
cf services

name       service      plan    bound apps                                                                                                last operation
my_mysql   cleardb      spark   data-flow-server                                                                                          create succeeded
rabbit     cloudamqp    lemur   data-flow-server-0VmmHoy-iot-stream-azure-iot-hub, data-flow-server-0VmmHoy-iot-stream-azure-iot-output   create succeeded
redis      rediscloud   30mb    data-flow-server-0VmmHoy-iot-stream-azure-iot-output                                                      create succeeded
```

If not bound, go ahead and bind explicitly and restart the app.  Both apps should start successfully after a few minutes.
```
cf bs data-flow-server-0VmmHoy-iot-stream-azure-iot-output redis
cf restage data-flow-server-0VmmHoy-iot-stream-azure-iot-output
```

# Deploy your Device app to PCF
Now you can build and push the azure-iot-device demo app to PCF.  Here is a sample manifest.yml content, just create this file in the azure-iot-device folder and again substitute your values.  Use the output of the "create-device-identity" app (above) to get the value for [Device Key]

Login to your PCF environment and run "cf push" from the azure-iot-device folder.

```
---
applications:
- name: azure-device-[Your Initials]
  memory: 1G
  buildpack: https://github.com/cloudfoundry/java-buildpack
  path: ./target/azure-iot-device-0.0.1-SNAPSHOT.jar
  env:
    HOSTNAME: [IoT Hub Hostname]
    DEVICE_ID: [Device ID]
    SHARED_ACCESS_KEY: [Device Key]

Example:
---
applications:
- name: azure-device-mj
  memory: 1G
  buildpack: https://github.com/cloudfoundry/java-buildpack
  path: ./target/azure-iot-device-0.0.1-SNAPSHOT.jar
  env:
    HOSTNAME: mjeffries-iot-hub.azure-devices.net
    DEVICE_ID: MyJavaDevice
    SHARED_ACCESS_KEY: 2kX/eAIsbhPFzsCVzs8FuSm2/Ajute85uTa4Fkt1H5I=
```

Once the device app is deployed, note the URL from the output, and load the app into your browser.  Click the "Start" button to start sending data to the Azure IoT Hub, and "Stop" to pause the data.  You can use the "+" and "-" buttons to adjust the wind speed, then wait around 30 seconds for the background color to change (at 10 and 30 mph).

Use "cf apps" to get the name of the apps deployed to PCF by SCDF, and use "cf logs" to see the messages processed by each component application.
