# Application Metrics for Java&trade;

Application Metrics for Java&trade; instruments the Java runtime for performance monitoring, providing the monitoring data visually with its built in dashboard

The following data collection sources are built-in:

 Source             | Description
:-------------------|:-------------------------------------------
 Environment        | Machine and runtime environment information
 CPU                | Process and system CPU
 GC                 | Percentage time spent in garbage collection
 Memory             | Java native and non-native memory usage
 HTTP               | HTTP request information
 

## Getting Started
### Prerequisites

The Application Metrics for Java agent requires Java version 8.

<a name="install"></a>
### Installation

Download the latest Application Metrics for Java release from [Github](http://github.com/runtimetools/javametrics/releases).
This contains:
* `javametrics.war` - Javametrics Web Application
* `javametrics.jar` - Javametrics agent
* `javametrics.so/dll/dylib`, `apiplugin.so/dll/dylib` and `envplugin.so/dll/dylib` - Javametrics native agent

#### Websphere Liberty
Unpack the `.zip` or `.tar.gz` archive that you downloaded in the previous step.  Copy the `javametrics.war` file into your [Websphere Liberty](https://developer.ibm.com/wasdev/websphere-liberty/) 'dropins' directory.

Javametrics also needs two Java options to be set in order to load the agent and the native agent.  A [jvm.options](https://www.ibm.com/support/knowledgecenter/en/SSAW57_liberty/com.ibm.websphere.wlp.nd.multiplatform.doc/ae/twlp_admin_customvars.html) file is the best way to configure this for Websphere Liberty and it should contain these two entries on separate lines, where `path_to_install_dir` is replaced with the actual path containing the javametrics files:

```
# Load Javametrics native agent
-agentlib:"/path_to_install_dir/javametrics"

# Load Javametrics Java agent
-javaagent:"/path_to_install_dir/javametrics.jar"
```

The URL for the dashboard consists of the server's default HTTP endpoint plus '/javametrics-dash'.  E.g. Running locally it might be: http://localhost:9080/javametrics-dash/

#### Spring
Coming soon

#### Apache Tomcat
Coming soon

<a name="api-doc"></a>
## API Documentation

Javametrics can be used to send and receive generic monitoring data from your application, as well as to enable or disable existing types of data.
___
#### `public class Javametrics`

Javametrics public API class. Used to create Topics which can send data to Javametrics. JSON formatted data can also be sent directly using sendJSON.

##### `public static Topic getTopic(String topicName)`

Get a Topic to send data on. If a topic with the given name already exists then that will be returned to you

 * **Parameters:** `topicName` — the name of the topic to be returned
 * **Returns:** Topic

##### `public static void sendJSON(String topicName, String payload)`

Send data to Javametrics

 * **Parameters:**
   * `topicName` — the name of the topic to send data on
   * `payload` — A JSON object formatted as a String

##### `public static boolean isEnabled(String topicName)`

Returns true if the given topic is enabled

 * **Parameters:** `topicName` — the name of the topic
 
#### `public static void addListener(JavametricsListener jml)`

Add a JavametricsListener, which will be informed of Javametrics events

 * **Parameters:** `jml` — the JavametricsListener to be added

#### `public static boolean removeListener(JavametricsListener jml)`

Remove a JavametricsListener

 * **Parameters:** `jml` — the JavametricsListener to be removed
 * **Returns:** true if the listener was registered
 
___
#### `public interface Topic`

A Javametrics topic on which data can be emitted. Create a new topic by calling Javametrics.createTopic(..) Topics created this way are registered with the Javametrics agent and are 'on' by default.

##### `public void send(String message)`

Send a message (sends if enabled)

 * **Parameters:** `message` — the message to be emitted

##### `public void send(long startTime, long endTime, String message)`

Send a message with a start and end time (sends if enabled)

 * **Parameters:**
   * `startTime` — start time in milliseconds (see System.currentTimeMillis for definition)
   * `endTime` — end time in milliseconds (see System.currentTimeMillis for definition)
   * `message` — the message to be emitted

##### `public void send(long startTime, long endTime)`

Send a timed event with a start and end time (sends if enabled)

 * **Parameters:**
   * `startTime` — start time in milliseconds (see System.currentTimeMillis for definition)
   * `endTime` — end time in milliseconds (see System.currentTimeMillis for definition)

##### `public void sendJSON(String payload)`

Send a JSON formatted String

 * **Parameters:** `payload` — A JSON object formatted as a String

##### `public void disable()`

Disable this topic (send methods will do nothing)

##### `public void enable()`

Enable this topic

##### `public boolean isEnabled()`

 * **Returns:** true if this topic is enabled
 
___
#### `public interface JavametricsListener`

A listener to Javametrics events

##### `public void receive(String pluginName, String data)`

Receive data from the Javametrics agent

 * **Parameters:**
   * `pluginName` — - the plugin that sent the data
   * `data` — - the data as a String
___

<a name="building"></a>
## Building from source

### To build the native agent:

 **Windows**

Requirements: Visual Studio 2015 or later, a Java 8 JDK and Cygwin

 * Make sure you are running inside a Visual Studio command shell to pick up the compiler.  There are 2 versions, a 32 and a 64 bit one so make sure you are running the right one for the level you want to build
 * Add Cygwin to your path, e.g. `set PATH=%PATH%;c:\cygwin64\bin`
 * Add JDK include directories to your path, e.g. `set INCLUDE=C:\jdk8_131\jdk1.8.0_131\include;C:\jdk8_131\jdk1.8.0_131\include\win32;%INCLUDE%`
 * `cd native`
 * `make BUILD=wa64 clean javametrics`  (to build on 64 bit Windows)

**Linux**

Requirements: GCC, GNU make and a Java 8 JDK

 * Add JDK include directories to your path, e.g. `export JAVA_SDK_INCLUDE=/usr/lib/default-java/include:/user/lib/default-java/include/linux`
 * `make BUILD=xa64 clean javametrics` (to build on 64 bit Linux)
 
**Mac**

Requirements: Xcode and a Java 8 JDK

 * You need to include two directories in the JAVA_SDK_INCLUDE path, e.g.: export "JAVA_SDK_INCLUDE=/Library/Java/JavaVirtualMachines/jdk1.8.0_121.jdk/Contents/Home/include/ -I/Library/Java/JavaVirtualMachines/jdk1.8.0_121.jdk/Contents/Home/include/darwin/"
 * `make BUILD=darwin64 clean javametrics`
 
### To build the jar and war files:

**All platforms**

Requirements: Apache Ant

Two `build.xml` ant build scripts are provided, one for building the javametrics.jar file and the other for building the javametrics.war fie.
 
## Source code
The source code for Application Metrics for Java is available in the [Javametrics Github project](http://github.com/RuntimeTools/javametrics).

## License
This project is released under an Apache 2.0 open source license.  

## Versioning scheme
This project uses a semver-parsable X.0.Z version number for releases, where X is incremented for breaking changes to the public API described in this document and Z is incremented for bug fixes **and** for non-breaking changes to the public API that provide new function.

## Version
0.0.0
