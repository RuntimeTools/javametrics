# Javametrics Design

Application Metrics for Java&trade; is composed of:
* An Agent which collects and publishes data
* API to allow data providers to send data to the Agent and listeners to receive data from the Agent
* Data providers
* Web application utilizing [RuntimeTools/graphmetrics](https://github.com/RuntimeTools/graphmetrics) to visualize the data 

## Components 
### Agent
An Agent that stores data added via the API and emits data to receivers.

### Javametrics API
Javametrics provides a Java API to interface with the Agent. The interface in the `com.ibm.javametrics` package consists of: 
* `Javametrics` - provides API to send data to the agent and to register listeners.
* `JavametricsListener` - implemented by registered listeners.
* `Topic` - API to send data to the agent for a named topic.

### Data Providers

#### MBean providers
`com.ibm.javametrics.dataproviders.MBeanDataProvider` is initialized statically in `com.ibm.javametrics.Javametrics` class. It gathers data from the MBean providers every 2 seconds and sends the data to the agent using the Javametrics API. MBean providers in the `com.ibm.javametrics.dataproviders` package are:
* `CPUDataProvider` - system and process cpu usage data
* `GCDataProvider` - garbage collection statistics
* `MemoryPoolDataProvider` - heap and native memory usage data

#### Instrumentation providers
Bytecode Instrumentation is used to gather HTTP data. Servlet and JSP classes are instrumented with callbacks to track and time the requests. The callbacks use the Javametrics API to send the data to the agent.

### Web Application
The web application uses the javascript graphs from the RuntimeTools/graphmetrics project. The index.html communicates with the server side over a WebSocket connecting to `com.ibm.javametrics.web.JavametricsWebSocket` which manages the session. `JavametricsWebSocket` registers itself with `com.ibm.javametrics.web.DataHandler` as an `Emitter`. 

`DataHandler` registers itself as a listener using the Javametrics API and implements the `receive(...)` method which receives data from the agent. Some processing of the data is performed, e.g. aggregating HTTP request data, before sending on the data via the `emit(...)` method of any registered Emitters. 


## Bytcode Injection
`com.ibm.javametrics.instrument.Agent` uses the `java.lang.instrument` package to create a java instrumentation agent. [ASM](http://asm.ow2.org/asm50/javadoc/user/index.html) is used to perform the code injection. 

* `Agent` is defined as the Premain-Class in the jar manifest. The `premain(...)` method is invoked on startup when the `-javaagent:<path to jar>` command line option is used. This adds a `ClassFileTransformer` to the Java instrumentation.
* `ClassTransformer` is a `java.lang.instrument.ClassFileTransformer` that implements the `transform(...)` method. The class bytecode is passed as a parameter to `transform`. A chain of ASM `ClassReader`. `ClassWriter` and Javametrics `ClassAdaptor` is set up which are called via a visitor pattern to perform any necessary byetcode changes.
* `ClassAdapter` extends the ASM `CLassVisitor`. In the `visit` code we determine which classes we want to instrument. In the `visitMethod` code we create an ASM `MethodVisitor` for each method we want to instrument.

### HTTP request instrumentation
To instrument HTTP request we inject callbacks on method entry and exit for:
* Servlets - classes that extend `javax.servlet.http.HttpServlet`. Methods `doGet`, `doPost` and `service`
* JSP pages - classes that implement `javax.servlet.jsp.HttpJspPage`. Method `_jspService`

`com,ibm.javametrics.instrument.ClassAdapter` will create a `ServletCallbackAdapter` which is the MethodVisitor that will inject the callback bytecode. 

`ServletCallbackAdapter` extends `org.objectweb.asm.commons.AdviceAdapter` which provides the simplest methods for generating and injecting code. We implement the visitor methods `onMethodEnter` and `onMethodExit` to inject calls to the static methods in `ServletCallback`.
