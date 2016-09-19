package net.eusashead.iot.mqtt.paho;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicReference;

import org.eclipse.paho.client.mqttv3.IMqttMessageListener;
import org.eclipse.paho.client.mqttv3.MqttAsyncClient;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public class AsyncPubSubITCase {

    // TCP MQTT broker
    public static final String TCP_BROKER_URL = "tcp://192.168.99.100:1883";

    // Websocket MQTT broker
    public static final String WS_BROKER_URL = "ws://192.168.99.100:15675/ws";

    private static final String CLIENT_ID = "test-mqtt-client";
    private static final String TOPIC = "test-mqtt-topic";

    @Test
    public void itCanPubAndSubToWebsocketBroker() throws Throwable {
        itCanPubAndSubToBroker(WS_BROKER_URL);
    }

    @Test
    public void itCanPubAndSubToTcpBroker() throws Throwable {
        itCanPubAndSubToBroker(TCP_BROKER_URL);
    }

    private void itCanPubAndSubToBroker(final String brokerUrl) throws Throwable {

        // Create async MQTT clients
        final MqttAsyncClient pubClient = new MqttAsyncClient(brokerUrl, CLIENT_ID + "-pub");
        AsyncPahoUtils.connect(pubClient);
        final MqttAsyncClient subClient = new MqttAsyncClient(brokerUrl, CLIENT_ID + "-sub");
        AsyncPahoUtils.connect(subClient);

        final CountDownLatch latch = new CountDownLatch(1);
        final AtomicReference<MqttMessage> msg = new AtomicReference<MqttMessage>();

        // Subscribe
        final IMqttMessageListener messageListener = new IMqttMessageListener() {

            @Override
            public void messageArrived(String topic, MqttMessage message) throws Exception {
                msg.set(message);
                latch.countDown();
            }
        };
        AsyncPahoUtils.subscribe(subClient, TOPIC, messageListener);

        // Publish the sensor data
        final byte[] expectedPayload = new byte[] { 'a', 'b', 'c' };
        AsyncPahoUtils.publish(pubClient, TOPIC, expectedPayload);

        // Await message publish and receipt
        latch.await();

        // Get the message received by the callback
        final MqttMessage receivedMessage = msg.get();
        Assert.assertNotNull(receivedMessage);
        Assert.assertNotNull(receivedMessage.getPayload());
        Assert.assertArrayEquals(expectedPayload, receivedMessage.getPayload());

        // Close the clients
        AsyncPahoUtils.disconnect(pubClient);
        AsyncPahoUtils.disconnect(subClient);

    }

}
