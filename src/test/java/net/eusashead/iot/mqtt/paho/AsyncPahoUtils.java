package net.eusashead.iot.mqtt.paho;

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
