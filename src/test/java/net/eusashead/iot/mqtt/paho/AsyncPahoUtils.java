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
import java.util.concurrent.atomic.AtomicReference;

import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttAsyncClient;
import org.eclipse.paho.client.mqttv3.IMqttMessageListener;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.MqttPersistenceException;
import org.eclipse.paho.client.mqttv3.MqttSecurityException;

public class AsyncPahoUtils {

    public static void connect(final IMqttAsyncClient client) throws Throwable {
        CountDownLatch latch = new CountDownLatch(1);
        AtomicReference<Throwable> error = new AtomicReference<>();
        AtomicReference<Boolean> success = new AtomicReference<>(Boolean.TRUE);
        client.connect(null, new IMqttActionListener() {

            @Override
            public void onSuccess(IMqttToken asyncActionToken) {
                latch.countDown();
            }

            @Override
            public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                error.set(exception);
                success.set(Boolean.FALSE);
                latch.countDown();
            }
        });
        latch.await();
        if (!success.get()) {
            throw error.get();
        }
    }

    public static void disconnect(final IMqttAsyncClient client) throws MqttException, MqttSecurityException, InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        client.disconnect(null, new IMqttActionListener() {

            @Override
            public void onSuccess(IMqttToken asyncActionToken) {
                latch.countDown();
            }

            @Override
            public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                latch.countDown();
            }
        });
        latch.await();
    }

    public static void publish(final IMqttAsyncClient client, final String topic, final byte[] expectedPayload) throws MqttException, MqttPersistenceException, InterruptedException {
        final CountDownLatch latch = new CountDownLatch(1);
        client.publish(topic, new MqttMessage(expectedPayload), null, new IMqttActionListener() {

            @Override
            public void onSuccess(IMqttToken asyncActionToken) {
                latch.countDown();
            }

            @Override
            public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                latch.countDown();
            }
        });
        latch.await();
    }

    public static void subscribe(final IMqttAsyncClient client, final String topic, final IMqttMessageListener messageListener) throws MqttException, InterruptedException {
        final CountDownLatch latch = new CountDownLatch(1);
        IMqttActionListener callback = new IMqttActionListener() {

            @Override
            public void onSuccess(IMqttToken asyncActionToken) {
                latch.countDown();
            }

            @Override
            public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                latch.countDown();
            }
        };

        client.subscribe(topic, 0, null, callback, messageListener);
        latch.await();
    }

}
