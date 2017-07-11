# Rx MQTT
An [RxJava](https://github.com/ReactiveX/RxJava) API for handling [MQTT](http://mqtt.org/) messages with an implementation using the [Paho](http://www.eclipse.org/paho/) Java MQTT library.

The main interface is `ObservableMqttClient`, which can be used to connect, publish and subscribe to a broker.

## API Changes in 1.1.0
In the 1.0.x branch, all of the `ObservableMqttClient` methods returned `Observable<T>` objects. In many cases, with RxJava 2.0 this is no longer the ideal type to return as better alternatives exist.

So to better support RxJava 2.0 the `ObservableMqttClient` API has changed to make use of more appropriate reactive types.

1. Methods that returned `Observable<Void>` now return `Completable`. This new type is described in the [What's Different in 2.0](https://github.com/ReactiveX/RxJava/wiki/What's-different-in-2.0#completable) document for RxJava. This is not only better but essential now that RxJava [no longer supports null](https://github.com/ReactiveX/RxJava/wiki/What's-different-in-2.0#nulls). Methods affected are connect(), disconnect(), unsubscribe() and close(). If you were handling onNext() for some reason, move this code to onComplete().
2. The publish() method that returned a 'one-shot' `Observable<PublishToken>` now uses `Single<PublishToken>`. If you were handling both onComplete() and onNext(), merge this code together as a `Single<T>` does both at the same time.
3. The subscribe() method now returns a `Flowable<MqttMessage>` rather than an `Observable<MqttMessage>`. `Flowable<T>` is a more suitable choice for supporting [backpressure](https://github.com/ReactiveX/RxJava/wiki/What's-different-in-2.0#backpressure). You shouldn't need to change your code unless you want to support backpressure, in which case you should pass a `FlowableSubscriber<T>` to subscribe() (see below for more on backpressure).

## Backpressure
The subscribe() methods of `ObservableMqttClient` return a `Flowable<MqttMessage>` using the `BackpressureStrategy` provided (the default will be `BackpressureStrategy.BUFFER`. This is done via the `Flowable.create(FlowableEmitter<T>, BackpressureStrategy)` method [described in the RxJava 2 documentation](http://reactivex.io/RxJava/2.x/javadoc/io/reactivex/Flowable.html#create(io.reactivex.FlowableOnSubscribe,%20io.reactivex.BackpressureStrategy)).

If you subscribe to this `Flowable<MqttMessage>` with a `FlowableSubscriber<MqttMessage>` the `BackpressureStrategy` will be applied if you are unable to request messages fast enough. This is described [in the RxJava documentation](http://reactivex.io/RxJava/2.x/javadoc/io/reactivex/Flowable.html#subscribe(io.reactivex.FlowableSubscriber)). 

Note that the other subscribe() methods of `Flowable<T>` do not apply backpressure.

## Obtaining the client
The default Paho implementation of `ObservableMqttClient` can be obtained like this:

```java
final IMqttAsyncClient paho = new MqttAsyncClient(...);
final ObservableMqttClient client = PahoObservableMqttClient.builder(paho)
    ... // Customise
    .build();
```
The builder allows you to override the default `BackpressureStrategy` if desired (see above for a description of how backpressure works).

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
## Releases
The binaries for each release should be available in Maven Central.


    <dependency>
        <groupId>net.eusashead.mqtt</groupId>
        <artifactId>rxmqtt</artifactId>
        <version>x.y.z.RELEASE</version>
    </dependency>

