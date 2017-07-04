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
