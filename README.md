# Rx MQTT
An [RxJava](https://github.com/ReactiveX/RxJava) API for handling [MQTT](http://mqtt.org/) messages with an implementation using the [Paho](http://www.eclipse.org/paho/) Java MQTT library.

The main interface is `ObservableMqttClient`, which can be used to connect, publish and subscribe to a broker.

## Building
The default Paho implementation of `ObservableMqttClient` can be obtained like this:

```java
final IMqttAsyncClient paho = new MqttAsyncClient(...);
final ObservableMqttClient client = new PahoObservableMqttClient.builder(paho)
    ... // Customise
    .build();
```
## Connecting
Asynchronously connect to the broker.

```java
client.connect().subscribe(t -> {
  // do something on connection
}, e -> {
  // do something on error
});
```
## Publishing
Asynchronously publish a message to the broker.

```java
final MqttMessage msg = MqttMessage.create(...);
client.publish("mytopic", msg).subscribe(t -> {
  // do something on publication
}, e -> {
  // do something on error
});
```
## Subscribing
Asynchronously subscribe to a topic (or topics). For each message received, the subscriber is called with the message. The QOS level desired can be passed along with the topic.

```java
client.subscribe("mytopic", 1).subscribe(msg -> {
  // do something with message
  final byte[] body = msg.getPayload();
}, e -> {
  // do something on error
});
```

## Unsubscribing
Asynchronously unsubscribe from a topic (or topics).

```java
client.unsubscribe("mytopic").subscribe(t -> {
  // do something on disconnect completion
}, e -> {
  // do something on error
});
```

## Disconnecting
Asynchronously disconnect from the broker.

```java
client.disconnect().subscribe(t -> {
  // do something on disconnect completion
}, e -> {
  // do something on error
});
```

## Closing
Asynchronously close the client, releasing all resources.

```java
client.close().subscribe(t -> {
  // do something on close completion
}, e -> {
  // do something on error
});
```