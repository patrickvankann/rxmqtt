package net.eusashead.iot.mqtt.paho;

import java.util.Objects;

import org.eclipse.paho.client.mqttv3.IMqttAsyncClient;

public class BaseObservableFactory {

    protected final IMqttAsyncClient client;

    public BaseObservableFactory(final IMqttAsyncClient client) {
        this.client = Objects.requireNonNull(client);
    }

    public IMqttAsyncClient getMqttAsyncClient() {
        return this.client;
    }

}