# Javametrics Design

Application Metrics for Java&trade; is composed of:
* Native Agent based on [RuntimeTools/omr-agentcore](https://github.com/RuntimeTools/omr-agentcore)
* API to allow data providers to send data to the Agent and listeners to receive data from the Agent
* Data providers
* Web application utilizing [RuntimeTools/graphmetrics](https://github.com/RuntimeTools/graphmetrics) to visualize the data 

## Components 
### Native Agent
A Java native agent activated by the Java command line option `-agentpath`. The javametrics native library entrypoint is defined in `native/src/javametrics.cpp`. This initializes the omr-agentcore and activates the native plugins:
* `envplugin` - provides common environment data.
* `apiplugin` - provides a c++ api to the agent.

### Javametrics API
Javametrics provides a Java API to interface with the native agent. The interface in the `com.ibm.javametrics` package consists of: 
* `Javametrics` - provides API to send data to the native agent apiplugin and to register listeners.
* `JavametricsListener` - implemented by registered listeners.
* `Topic` - API to send data to the agent for a named topic.

### Data Providers

#### Native plugins
* Environment data is provided by the omr-agentcore `envplugin`.

#### MBean providers
`com.ibm.javametrics.dataproviders.MBeanDataProvider` is initialized statically in `com.ibm.javametrics.Javametrics` class. It gathers data from the MBean providers every 2 seconds and sends the data to the agent using the Javametrics API. MBean providers in the `com.ibm.javametrics.dataproviders` package are:
* `CPUDataProvider` - system and process cpu usage data
* `GCDataProvider` - garbage collection statistics
* `MemoryPoolDataProvider` - heap and native memory usage data

#### Instrumentation providers
Bytecode Instrumentation is used to gather HTTP data. Servlet and JSP classes are instrumented with callbacks to track and time the requests. The callbacks use the Javametrics API to send the data to the agent.

### Web Application
The web application uses the javascript graphs from the RuntimeTools/graphmetrics project. The index.html communicates with the server side over a WebSocket connecting to `com.ibm.javametrics.web.JavametricsWebSocket` which manages the session. `JavametricsWebSocket` registers itself with `com.ibm.javametrics.web.DataHandler` as an `Emitter`. 

`DataHandler` registers itself as a listener using the Javametrics API and implenents the `receive(...)` method which receives data from the agent. Some processing of the data is performed, e.g. aggregating HTTP request data, before sending on the data via the `emit(...)` method of any registered Emitters. 
