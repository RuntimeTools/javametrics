[![Build Status](https://travis-ci.org/RuntimeTools/javametrics.svg?branch=master)](https://travis-ci.org/RuntimeTools/javametrics)
[![codebeat badge](https://codebeat.co/badges/02e01f80-46ee-4a25-a409-ff1c65cb1421)](https://codebeat.co/projects/github-com-runtimetools-javametrics-master)
[![codecov](https://codecov.io/gh/RuntimeTools/javametrics/branch/master/graph/badge.svg)](https://codecov.io/gh/RuntimeTools/javametrics)
![Apache 2](https://img.shields.io/badge/license-Apache2-blue.svg?style=flat)
[![Homepage](https://img.shields.io/badge/homepage-Application%20Metrics%20for%20Java-blue.svg)](https://developer.ibm.com/javasdk/application-metrics-java/)

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

### Releases

Download the latest Application Metrics for Java release zip from [Github](http://github.com/runtimetools/javametrics/releases).
This contains:
* `webapp/dashboard/javametrics-dash-x.x.x.war` - Javametrics Web Application
* `webapp/prometheus/javametrics-prometheus-x.x.x.war` - Javametrics Prometheus Endpoint
* `agent/javametrics-agent-x.x.x.jar` - Javametrics agent and required ASM libraries
* `spring/javametrics-spring-x.x.x.jar` - Javametrics spring
* `rest/javametrics-rest-x.x.x.war` - Javametrics REST api package

### Building with Maven

To build with maven

```
git clone --recursive https://github.com/RuntimeTools/javametrics
cd javametrics
mvn install
```

To use the agent built locally, you will need to reference the your local javametrics directory when setting the javaagent parameter later in the instructions.


Javametrics is also released on Maven Central with the following artifacts


```
javametrics-agent
 <groupId>com.ibm.runtimetools</groupId>
 <artifactId>javametrics-agent</artifactId>

javametrics-dash
 <groupId>com.ibm.runtimetools</groupId>
 <artifactId>javametrics-dash</artifactId>

javametrics-prometheus
 <groupId>com.ibm.runtimetools</groupId>
 <artifactId>javametrics-prometheus</artifactId>

javametrics-spring
 <groupId>com.ibm.runtimetools</groupId>
 <artifactId>javametrics-spring</artifactId>

javametrics-rest
 <groupId>com.ibm.runtimetools</groupId>
 <artifactId>javametrics-rest</artifactId>

javametrics-codewind
 <groupId>com.ibm.runtimetools</groupId>
 <artifactId>javametrics-codewind</artifactId>

javametrics-codewind-spring
 <groupId>com.ibm.runtimetools</groupId>
 <artifactId>javametrics-codewind-spring</artifactId>
```

### Websphere Liberty
Unpack the release `.zip` archive that you downloaded in the previous step.  

Javametrics requires a Java option to be set in order to load the agent.  A [jvm.options](https://www.ibm.com/support/knowledgecenter/en/SSAW57_liberty/com.ibm.websphere.wlp.nd.multiplatform.doc/ae/twlp_admin_customvars.html) file is the best way to configure this for Websphere Liberty. It should contain the following entry, where `path_to_javametrics_agent_dir` is replaced with the actual path containing the javametrics file:

```
# Load Javametrics Java agent
-javaagent:<path_to_javametrics_agent_dir>/javametrics-agent-1.8.0.jar
```
If you have built the agent locally, your path_to_javametrics_agent_dir will need to point to your clone of javametrics.
e.g.
```
-javaagent:<path_to_git_home>/javametrics/javaagent/target/javametrics-agent-1.8.0.jar
```
* NOTE, if you move the javametrics-agent to another directory you need to make sure you take the asm folder with it.  The asm folder is required for the agent to run as it contains files that the agent needs

Copy the required war files into your [Websphere Liberty](https://developer.ibm.com/wasdev/websphere-liberty/) 'dropins' directory.
- `javametrics-dash-x.x.x.war` to use the Javametrics dashboard
- `javametrics-rest-x.x.x.war` to use the Javametrics REST api
- `javametrics-prometheus-x.x.x.war` to use Prometheus support

The URL for the dashboard consists of the server's default HTTP endpoint plus `/javametrics-dash/`.  E.g. Running locally it might be: http://localhost:9080/javametrics-dash/

The URL for the prometheus endpoint consists of the server's default HTTP endpoint plus the default prometheus metrics path `/metrics`.  E.g. Running locally it might be: http://localhost:9080/metrics/

The URL for the REST API context root consists of the server's default HTTP endpoint plus `/javametrics`.  E.g. Running locally it might be: http://localhost:9080/javametrics/api/v1/collections

### Open Liberty

Follow all the steps for [Websphere Liberty](#websphere-liberty) and in addition, make sure that the `websocket-1.0` feature is installed on the server. To do this, open the `server.xml` for the server in question, and in the `<featureManager>` tags add the following line
```xml
<feature>websocket-1.0</feature>
```

### Spring Boot
To enable Javametrics in a Spring Boot application you need to add an extra annotation to your main application class:
```
@ComponentScan(basePackages = {"com.ibm.javametrics.spring", "mypackage"})
```
In a Spring Boot starter project this goes above or below the `@SpringBootApplication` annotation and you will need to add the package that that class is in to the list of `basePackages` (in place of __mypackage__ above).

You also need to add the following dependencies to your pom.xml:

```
<dependency>
    <groupId>com.ibm.runtimetools</groupId>
    <artifactId>javametrics-spring</artifactId>
    <version>1.8.0</version>
</dependency>
<dependency>
    <groupId>com.ibm.runtimetools</groupId>
    <artifactId>javametrics-agent</artifactId>
    <version>1.8.0</version>
</dependency>
<dependency>
    <groupId>org.glassfish</groupId>
    <artifactId>javax.json</artifactId>
    <version>1.0.4</version>
</dependency>
 ```

Once you have launched your application you will find the dashboard at the server's default HTTP endpoint plus `/javametrics-dash/`.  E.g. running locally with Spring Boot it might be: http://localhost:8080/javametrics-dash/

The URL for the REST API context root consists of the server's default HTTP endpoint plus `/javametrics/`.  e.g. http://localhost:9080/javametrics/api/v1/collections

### Apache Tomcat
Coming soon

<a name="api-doc"></a>

## API Documentation
- [API Documentation](API-DOCUMENTATION.md)

## REST API Documentation
- [REST API](REST-API.md)

<a name="building"></a>

## Source code
The source code for Application Metrics for Java is available in the [Javametrics Github project](http://github.com/RuntimeTools/javametrics).

## License
This project is released under an Apache 2.0 open source license.  

## Versioning scheme
This project uses a semver-parsable X.0.Z version number for releases, where X is incremented for breaking changes to the public API described in this document and Z is incremented for bug fixes **and** for non-breaking changes to the public API that provide new function.

## Version
1.8.0
