# Rx MQTT
An [RxJava](https://github.com/ReactiveX/RxJava) API for handling [MQTT](http://mqtt.org/) messages with an implementation using the [Paho](http://www.eclipse.org/paho/) Java MQTT library.

The main interface is `ObservableMqttClient`, which can be used to connect, publish and subscribe to a broker in a reactive way.

## API Changes in 1.2.0
In previous versions, the same `MqttMessage` class was used for both publishing and subscribing. However, some of the fields in a message received by a subscriber are not needed by a publisher - specifically, the message id. Message identifier is set by the underlying MQTT server, not the publishing client.

Additionally, the topic that the message was received on is useful to a subscriber but redundant for a publisher.

Hence `MqttMessage` is now split into sub-interfaces: `PublishMessage` and `SubscribeMessage`. Only `SubscribeMessage` requires a message id or topic.


## API Changes in 1.1.0
In the 1.0.x branch, all of the `ObservableMqttClient` methods returned an [`Observable<T>`](http://reactivex.io/RxJava/1.x/javadoc/rx/Observable.html). This was not ideal as some did not return any data (e.g. `Observable<Void>`) or returned just a single value (e.g `Observable<PublishToken>`).

With the release of RxJava 2.0 there are new reactive types that can be used to better model these methods. In the 1.1.0 release, the `ObservableMqttClient` API has changed to make use of more appropriate reactive types from RxJava 2.

1. Methods that previously returned `Observable<Void>` now return [`Completable`](http://reactivex.io/RxJava/2.x/javadoc/io/reactivex/Completable.html). This new type is described in the [What's Different in 2.0](https://github.com/ReactiveX/RxJava/wiki/What's-different-in-2.0#completable) document for RxJava. This is better from an idiomatic reactive programming perspective. In fact, this change is essential because RxJava 2 [no longer supports null](https://github.com/ReactiveX/RxJava/wiki/What's-different-in-2.0#nulls). Methods affected are connect(), disconnect(), unsubscribe() and close(). If you were handling onNext() for some reason, move this code to onComplete().
2. The publish() method that returned a 'one-shot' `Observable<PublishToken>` now uses [`Single<PublishToken>`](http://reactivex.io/RxJava/2.x/javadoc/io/reactivex/Single.html). If you were handling both onComplete() and onNext(), merge this code together as a `Single<T>` does both at the same time.
3. The subscribe() method now returns a [`Flowable<MqttMessage>`](http://reactivex.io/RxJava/2.x/javadoc/io/reactivex/Flowable.html) rather than an `Observable<MqttMessage>`. `Flowable<T>` is a more suitable choice for supporting [backpressure](https://github.com/ReactiveX/RxJava/wiki/What's-different-in-2.0#backpressure). You shouldn't need to change your code unless you want to support backpressure, in which case you should pass a [`FlowableSubscriber<MqttMessage>`](http://reactivex.io/RxJava/2.x/javadoc/io/reactivex/FlowableSubscriber.html) to `Flowable.subscribe(FlowableSubscriber<MqttMessage>)` (see below for more on backpressure).

## Backpressure
The subscribe() methods of `ObservableMqttClient` return a [`Flowable<MqttMessage>`](http://reactivex.io/RxJava/2.x/javadoc/io/reactivex/Flowable.html) using the [`BackpressureStrategy`](http://reactivex.io/RxJava/2.x/javadoc/io/reactivex/BackpressureStrategy.html) provided (the default will be `BackpressureStrategy.BUFFER`). Behind the scenes, this is done using the `Flowable.create(FlowableEmitter<T>, BackpressureStrategy)` factory method [described in the RxJava 2 documentation](http://reactivex.io/RxJava/2.x/javadoc/io/reactivex/Flowable.html#create(io.reactivex.FlowableOnSubscribe,%20io.reactivex.BackpressureStrategy)).

If you subscribe to this `Flowable<MqttMessage>` with a [`FlowableSubscriber<MqttMessage>`](http://reactivex.io/RxJava/2.x/javadoc/io/reactivex/FlowableSubscriber.html) the `BackpressureStrategy` will be applied if you are unable to request messages fast enough. A `FlowableSubscriber<T>` signals that it is ready to receive data by calling request() on the [`Subscription`](http://www.reactive-streams.org/reactive-streams-1.0.0-javadoc/org/reactivestreams/Subscription.html) to the [`Publisher<T>`](http://www.reactive-streams.org/reactive-streams-1.0.0-javadoc/org/reactivestreams/Publisher.html). This is described [in the RxJava documentation](http://reactivex.io/RxJava/2.x/javadoc/io/reactivex/Flowable.html#subscribe(io.reactivex.FlowableSubscriber)). 

Note that the other subscribe(xxx) methods of [`Flowable<T>`](http://reactivex.io/RxJava/2.x/javadoc/io/reactivex/Flowable.html) do not apply backpressure.

## Obtaining the client
The default Paho implementation of `ObservableMqttClient` can be obtained like this:

```java
final IMqttAsyncClient paho = new MqttAsyncClient(...);
final ObservableMqttClient client = PahoObservableMqttClient.builder(paho)
    ... // Customise
    .build();
```
The builder allows you to override the default [`BackpressureStrategy`](http://reactivex.io/RxJava/2.x/javadoc/io/reactivex/BackpressureStrategy.html) if desired (see above for a description of how backpressure works).

## Connecting
Asynchronously connect to the broker using an RxJava [`Completable`](http://reactivex.io/RxJava/2.x/javadoc/io/reactivex/Completable.html).

```java
client.connect().subscribe(() -> {
  // do something on completion
}, e -> {
  // do something on error
});
```
## Publishing
Asynchronously publish a message to the broker using an RxJava [`Single`](http://reactivex.io/RxJava/2.x/javadoc/io/reactivex/Single.html).

```java
final PublishMessage msg = PublishMessage.create(...);
client.publish("mytopic", msg).subscribe(t -> {
  // do something on success
}, e -> {
  // do something on error
});
```
## Subscribing
Asynchronously subscribe to a topic (or topics) using an RxJava [`Flowable<T>`](http://reactivex.io/RxJava/2.x/javadoc/io/reactivex/Flowable.html). For each message received, the subscriber is called with the message. The QOS level desired can be passed along with the topic.

```java
client.subscribe("mytopic", 1).subscribe(msg -> {
  // do something with message
  final byte[] body = msg.getPayload();
}, e -> {
  // do something on error
});
```

## Unsubscribing
Asynchronously unsubscribe from a topic (or topics) using an RxJava [`Completable`](http://reactivex.io/RxJava/2.x/javadoc/io/reactivex/Completable.html).

```java
client.unsubscribe("mytopic").subscribe(() -> {
  // do something on disconnect completion
}, e -> {
  // do something on error
});
```

## Disconnecting
Asynchronously disconnect from the broker using an RxJava [`Completable`](http://reactivex.io/RxJava/2.x/javadoc/io/reactivex/Completable.html).

```java
client.disconnect().subscribe(() -> {
  // do something on disconnect completion
}, e -> {
  // do something on error
});
```

## Closing
Asynchronously close the client and relase all resources using an RxJava [`Completable`](http://reactivex.io/RxJava/2.x/javadoc/io/reactivex/Completable.html).

```java
client.close().subscribe(() -> {
  // do something on close completion
}, e -> {
  // do something on error
});
```
## Releases
The binaries for each release should be available in [Maven Central](https://search.maven.org/#search%7Cga%7C1%7Ca%3A%22rxmqtt%22).


    <dependency>
        <groupId>net.eusashead.mqtt</groupId>
        <artifactId>rxmqtt</artifactId>
        <version>x.y.z.RELEASE</version>
    </dependency>

