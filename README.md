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
Asynchronously connect to the broker using an RxJava `Completable`.

```java
client.connect().subscribe(() -> {
  // do something on completion
}, e -> {
  // do something on error
});
```
## Publishing
Asynchronously publish a message to the broker using an RxJava `Single`.

```java
final MqttMessage msg = MqttMessage.create(...);
client.publish("mytopic", msg).subscribe(t -> {
  // do something on success
}, e -> {
  // do something on error
});
```
## Subscribing
Asynchronously subscribe to a topic (or topics) using an RxJava `Flowable`. For each message received, the subscriber is called with the message. The QOS level desired can be passed along with the topic.

```java
client.subscribe("mytopic", 1).subscribe(msg -> {
  // do something with message
  final byte[] body = msg.getPayload();
}, e -> {
  // do something on error
});
```

## Unsubscribing
Asynchronously unsubscribe from a topic (or topics) using an RxJava `Completable`.

```java
client.unsubscribe("mytopic").subscribe(() -> {
  // do something on disconnect completion
}, e -> {
  // do something on error
});
```

## Disconnecting
Asynchronously disconnect from the broker using an RxJava `Completable`.

```java
client.disconnect().subscribe(() -> {
  // do something on disconnect completion
}, e -> {
  // do something on error
});
```

## Closing
Asynchronously close the client and relase all resources using an RxJava `Completable`.

```java
client.close().subscribe(() -> {
  // do something on close completion
}, e -> {
  // do something on error
});
```