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

#### Websphere Liberty
Unpack the `.zip` or `.tar.gz` archive that you downloaded in the previous step.  Copy the `javametrics.war` file into your [Websphere Liberty](https://developer.ibm.com/wasdev/websphere-liberty/) 'dropins' directory.

Javametrics requires a Java option to be set in order to load the agent.  A [jvm.options](https://www.ibm.com/support/knowledgecenter/en/SSAW57_liberty/com.ibm.websphere.wlp.nd.multiplatform.doc/ae/twlp_admin_customvars.html) file is the best way to configure this for Websphere Liberty. It should contain the following entry, where `path_to_install_dir` is replaced with the actual path containing the javametrics file:

```
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
- [API Documentation](API-DOCUMENTATION.md)

<a name="building"></a>

## Building the jar and war files from source

Requirements: Maven

To build javametrics, run `mvn clean package` from the root project.  This will build a zip file in the distribution directory containing 
`javametrics-agent.jar`, `javametrics-web.war` and a `lib/` directory with the `asm*.jar` files.
 
## Source code
The source code for Application Metrics for Java is available in the [Javametrics Github project](http://github.com/RuntimeTools/javametrics).

## License
This project is released under an Apache 2.0 open source license.  

## Versioning scheme
This project uses a semver-parsable X.0.Z version number for releases, where X is incremented for breaking changes to the public API described in this document and Z is incremented for bug fixes **and** for non-breaking changes to the public API that provide new function.

## Version
0.0.0
