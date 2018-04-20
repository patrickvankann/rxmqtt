package net.eusashead.iot.mqtt.paho;

/*
 * #[license]
 * rxmqtt
 * %%
 * Copyright (C) 2013 - 2016 Eusa's Head
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * %[license]
 */

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import org.eclipse.paho.client.mqttv3.IMqttAsyncClient;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttAsyncClient;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import io.reactivex.Completable;
import io.reactivex.Single;
import net.eusashead.iot.mqtt.MqttMessage;
import net.eusashead.iot.mqtt.PublishMessage;
import net.eusashead.iot.mqtt.PublishToken;

@RunWith(JUnit4.class)
public class PahoObservableMqttClientITCase {

    // Websocket MQTT broker
    public static final String WS_BROKER_URL = "ws://localhost:15675/ws";

    private static final String CLIENT_ID = "test-mqtt-client";
    private static final String TOPIC = "test-mqtt-topic";

    private IMqttAsyncClient asyncClient;
    private PahoObservableMqttClient observableClient;

    @Before
    public void before() throws Exception {
        this.asyncClient = new MqttAsyncClient(WS_BROKER_URL, CLIENT_ID);
        this.observableClient = new PahoObservableMqttClient.Builder(
                this.asyncClient).build();
    }

    @After
    public void after() throws Exception {
        if (this.asyncClient.isConnected()) {
            AsyncPahoUtils.disconnect(this.asyncClient);
            this.asyncClient.close();
        }
    }

    @Test
    public void itCanConnect() throws Throwable {
        Assert.assertFalse(this.asyncClient.isConnected());
        Assert.assertFalse(this.observableClient.isConnected());

        final Completable obs = this.observableClient.connect();
        obs.blockingAwait();

        Assert.assertTrue(this.asyncClient.isConnected());
        Assert.assertTrue(this.observableClient.isConnected());
    }

    @Test
    public void itCanDisconnect() throws Throwable {

        AsyncPahoUtils.connect(this.asyncClient);
        Assert.assertTrue(this.asyncClient.isConnected());
        Assert.assertTrue(this.observableClient.isConnected());

        final Completable obs1 = this.observableClient.disconnect();
        obs1.blockingAwait();

        Assert.assertFalse(this.asyncClient.isConnected());
        Assert.assertFalse(this.observableClient.isConnected());

    }

    @Test(expected = MqttException.class)
    public void itCanClose() throws Throwable {
        Assert.assertFalse(this.asyncClient.isConnected());
        Assert.assertFalse(this.observableClient.isConnected());

        final Completable obs1 = this.observableClient.connect();
        obs1.blockingAwait();
        final Completable obs2 = this.observableClient.disconnect();
        obs2.blockingAwait();
        final Completable obs3 = this.observableClient.close();
        obs3.blockingAwait();

        // Should error
        AsyncPahoUtils.connect(this.asyncClient);

    }

    @Test
    public void itCanSubscribe() throws Throwable {

        AsyncPahoUtils.connect(this.asyncClient);

        final CountDownLatch latch = new CountDownLatch(2);
        final AtomicReference<IMqttDeliveryToken> token = new AtomicReference<>();
        final AtomicReference<MqttMessage> result = new AtomicReference<>();

        // Callback to monitor delivery completion
        this.asyncClient.setCallback(new MqttCallback() {

            @Override
            public void messageArrived(final String topic,
                    final org.eclipse.paho.client.mqttv3.MqttMessage m)
                    throws Exception {
            }

            @Override
            public void deliveryComplete(final IMqttDeliveryToken t) {
                token.set(t);
                latch.countDown();
            }

            @Override
            public void connectionLost(final Throwable cause) {
            }
        });

        // Subscribe
        final PublishMessage expected = PublishMessage
                .create(new byte[] { 'a', 'b', 'c' }, 1, false);
        this.observableClient.subscribe(TOPIC, 1).subscribe(r -> {
            result.set(r);
            latch.countDown();
        });

        // Publish a test message
        AsyncPahoUtils.publish(this.asyncClient, TOPIC, expected.getPayload());

        // Await for async completion
        latch.await();
        Assert.assertNotNull(result.get());
        Assert.assertArrayEquals(expected.getPayload(),
                result.get().getPayload());
        Assert.assertNotNull(token.get());
    }

    @Test
    public void itCanSubscribeMultipleMessages() throws Throwable {

        AsyncPahoUtils.connect(this.asyncClient);

        final CountDownLatch latch = new CountDownLatch(4);
        final AtomicInteger messageCount = new AtomicInteger(0);

        // Callback to monitor delivery completion
        this.asyncClient.setCallback(new MqttCallback() {

            @Override
            public void messageArrived(final String topic,
                    final org.eclipse.paho.client.mqttv3.MqttMessage m)
                    throws Exception {
            }

            @Override
            public void deliveryComplete(final IMqttDeliveryToken t) {
                latch.countDown();
            }

            @Override
            public void connectionLost(final Throwable cause) {
            }
        });

        // Subscribe
        this.observableClient.subscribe(TOPIC, 1).subscribe(r -> {
            messageCount.incrementAndGet();
            latch.countDown();
        });

        // Publish a test message
        AsyncPahoUtils.publish(this.asyncClient, TOPIC,
                new byte[] { 'a', 'b', 'c' });
        AsyncPahoUtils.publish(this.asyncClient, TOPIC,
                new byte[] { 'd', 'e', 'f' });

        // Await for async completion
        latch.await();
        Assert.assertEquals(2, messageCount.get());
    }

    @Test
    public void itCanPublish() throws Throwable {

        AsyncPahoUtils.connect(this.asyncClient);

        final CountDownLatch latch = new CountDownLatch(2);
        final AtomicReference<IMqttDeliveryToken> token = new AtomicReference<>();
        final AtomicReference<PublishToken> pubToken = new AtomicReference<>();

        // Callback to monitor delivery completion
        this.asyncClient.setCallback(new MqttCallback() {

            @Override
            public void messageArrived(final String topic,
                    final org.eclipse.paho.client.mqttv3.MqttMessage message)
                    throws Exception {
            }

            @Override
            public void deliveryComplete(final IMqttDeliveryToken t) {
                token.set(t);
                latch.countDown();
            }

            @Override
            public void connectionLost(final Throwable cause) {
            }
        });

        // Publish the message
        final PublishMessage msg = PublishMessage
                .create(new byte[] { 'a', 'b', 'c' }, 1, false);
        final Single<PublishToken> obs = this.observableClient.publish(TOPIC,
                msg);

        // Subscribe for result
        obs.subscribe(r -> {
            pubToken.set(r);
            latch.countDown();
        });

        // Await for async completion
        latch.await();
        final IMqttDeliveryToken iMqttDeliveryToken = token.get();
        final PublishToken publishToken = pubToken.get();
        Assert.assertNotNull(iMqttDeliveryToken);
        Assert.assertNotNull(publishToken);
        Assert.assertNotNull(publishToken.getClientId());
        Assert.assertEquals(iMqttDeliveryToken.getClient().getClientId(),
                publishToken.getClientId());
        Assert.assertNotNull(publishToken.getMessageId());
        Assert.assertEquals(iMqttDeliveryToken.getMessageId(),
                publishToken.getMessageId());
        Assert.assertNotNull(publishToken.getTopics());
        Assert.assertArrayEquals(iMqttDeliveryToken.getTopics(),
                publishToken.getTopics());
        Assert.assertNotNull(publishToken.getMessageId());
        Assert.assertEquals(iMqttDeliveryToken.getMessageId(),
                publishToken.getMessageId());

    }

}
