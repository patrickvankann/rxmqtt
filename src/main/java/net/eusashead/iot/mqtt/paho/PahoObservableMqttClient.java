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

import java.util.Objects;

import org.eclipse.paho.client.mqttv3.IMqttAsyncClient;
import org.eclipse.paho.client.mqttv3.MqttAsyncClient;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClientPersistence;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

import io.reactivex.BackpressureStrategy;
import io.reactivex.Completable;
import io.reactivex.Flowable;
import io.reactivex.Single;
import net.eusashead.iot.mqtt.ObservableMqttClient;
import net.eusashead.iot.mqtt.ObservableMqttClientBuilder;
import net.eusashead.iot.mqtt.PublishMessage;
import net.eusashead.iot.mqtt.PublishToken;
import net.eusashead.iot.mqtt.SubscribeMessage;

public class PahoObservableMqttClient implements ObservableMqttClient {

    public static class Builder implements ObservableMqttClientBuilder {

        private final IMqttAsyncClient client;
        private MqttConnectOptions connectOptions;
        private CloseFactory closeFactory;
        private ConnectFactory connectFactory;
        private DisconnectFactory disconnectFactory;
        private PublishFactory publishFactory;
        private SubscribeFactory subscribeFactory;
        private UnsubscribeFactory unsubscribeFactory;
        private BackpressureStrategy backpressureStrategy;

        public Builder(final String brokerUri) throws MqttException {
            this(brokerUri, MqttAsyncClient.generateClientId());
        }

        public Builder(final String brokerUri, final String clientId) throws MqttException {
            this(brokerUri, clientId, new MemoryPersistence());
        }

        public Builder(final String brokerUri, final String clientId, final MqttClientPersistence persistence)
                throws MqttException {
            this(new MqttAsyncClient(brokerUri, clientId, persistence));
        }

        public Builder(final IMqttAsyncClient client) {
            this.client = client;
            this.connectOptions = new MqttConnectOptions();
            this.closeFactory = new CloseFactory(client);
            this.connectFactory = new ConnectFactory(this.client, this.connectOptions);
            this.disconnectFactory = new DisconnectFactory(client);
            this.publishFactory = new PublishFactory(client);
            this.subscribeFactory = new SubscribeFactory(client);
            this.unsubscribeFactory = new UnsubscribeFactory(client);
            this.backpressureStrategy = BackpressureStrategy.BUFFER;
        }

        public IMqttAsyncClient getClient() {
            return this.client;
        }

        public MqttConnectOptions getConnectOptions() {
            return this.connectOptions;
        }

        public CloseFactory getCloseFactory() {
            return this.closeFactory;
        }

        public ConnectFactory getConnectFactory() {
            return this.connectFactory;
        }

        public DisconnectFactory getDisconnectFactory() {
            return this.disconnectFactory;
        }

        public PublishFactory getPublishFactory() {
            return this.publishFactory;
        }

        public SubscribeFactory getSubscribeFactory() {
            return this.subscribeFactory;
        }

        public UnsubscribeFactory getUnsubscribeFactory() {
            return this.unsubscribeFactory;
        }

        public BackpressureStrategy getBackpressureStrategy() {
            return this.backpressureStrategy;
        }

        public Builder setMqttCallback(final MqttCallback mqttCallback) {
            this.client.setCallback(Objects.requireNonNull(mqttCallback));
            return this;
        }

        public Builder setConnectOptions(final MqttConnectOptions connectOptions) {
            this.connectOptions = connectOptions;
            this.connectFactory = new ConnectFactory(this.client, this.connectOptions);
            return this;
        }

        public Builder setCloseFactory(final CloseFactory closeFactory) {
            this.closeFactory = Objects.requireNonNull(closeFactory);
            return this;
        }

        public Builder setConnectFactory(final ConnectFactory connectFactory) {
            this.connectFactory = Objects.requireNonNull(connectFactory);
            return this;
        }

        public Builder setDisconnectFactory(final DisconnectFactory disconnectFactory) {
            this.disconnectFactory = Objects.requireNonNull(disconnectFactory);
            return this;
        }

        public Builder setPublishFactory(final PublishFactory publishFactory) {
            this.publishFactory = Objects.requireNonNull(publishFactory);
            return this;
        }

        public Builder setSubscribeFactory(final SubscribeFactory subscribeFactory) {
            this.subscribeFactory = Objects.requireNonNull(subscribeFactory);
            return this;
        }

        public Builder setUnsubscribeFactory(final UnsubscribeFactory unsubscribeFactory) {
            this.unsubscribeFactory = Objects.requireNonNull(unsubscribeFactory);
            return this;
        }

        public Builder setBackpressureStrategy(final BackpressureStrategy backpressureStrategy) {
            this.backpressureStrategy = Objects.requireNonNull(backpressureStrategy);
            return this;
        }

        @Override
        public PahoObservableMqttClient build() {
            return new PahoObservableMqttClient(this);
        }

    }

    private final IMqttAsyncClient client;
    private final CloseFactory closeFactory;
    private final ConnectFactory connectFactory;
    private final DisconnectFactory disconnectFactory;
    private final PublishFactory publishFactory;
    private final SubscribeFactory subscribeFactory;
    private final UnsubscribeFactory unsubscribeFactory;
    private final BackpressureStrategy backpressureStrategy;

    private PahoObservableMqttClient(final Builder builder) {
        this.client = builder.client;
        this.closeFactory = builder.closeFactory;
        this.connectFactory = builder.connectFactory;
        this.disconnectFactory = builder.disconnectFactory;
        this.publishFactory = builder.publishFactory;
        this.subscribeFactory = builder.subscribeFactory;
        this.unsubscribeFactory = builder.unsubscribeFactory;
        this.backpressureStrategy = builder.backpressureStrategy;
    }

    /*
     * (non-Javadoc)
     *
     * @see net.eusashead.iot.mqtt.ObservableMqttClient#getClientId()
     */
    @Override
    public String getClientId() {
        return this.client.getClientId();
    }

    /*
     * (non-Javadoc)
     *
     * @see net.eusashead.iot.mqtt.ObservableMqttClient#getBrokerUri()
     */
    @Override
    public String getBrokerUri() {
        return this.client.getServerURI();
    }

    /*
     * (non-Javadoc)
     *
     * @see net.eusashead.iot.mqtt.ObservableMqttClient#isConnected()
     */
    @Override
    public boolean isConnected() {
        return this.client.isConnected();
    }

    /*
     * (non-Javadoc)
     *
     * @see net.eusashead.iot.mqtt.ObservableMqttClient#close()
     */
    @Override
    public Completable close() {
        return this.closeFactory.create();
    }

    /*
     * (non-Javadoc)
     *
     * @see net.eusashead.iot.mqtt.ObservableMqttClient#connect()
     */
    @Override
    public Completable connect() {
        return this.connectFactory.create();
    }

    /*
     * (non-Javadoc)
     *
     * @see net.eusashead.iot.mqtt.ObservableMqttClient#disconnect()
     */
    @Override
    public Completable disconnect() {
        return this.disconnectFactory.create();
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * net.eusashead.iot.mqtt.ObservableMqttClient#publish(java.lang.String,
     * net.eusashead.iot.mqtt.PublishMessage)
     */
    @Override
    public Single<PublishToken> publish(final String topic, final PublishMessage msg) {
        return this.publishFactory.create(topic, msg);

    }

    /*
     * (non-Javadoc)
     *
     * @see
     * net.eusashead.iot.mqtt.ObservableMqttClient#subscribe(java.lang.String[],
     * int[])
     */
    @Override
    public Flowable<SubscribeMessage> subscribe(final String topics[], final int qos[]) {
        return this.subscribeFactory.create(topics, qos, this.backpressureStrategy);
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * net.eusashead.iot.mqtt.ObservableMqttClient#subscribe(java.lang.String,
     * int)
     */
    @Override
    public Flowable<SubscribeMessage> subscribe(final String topic, final int qos) {
        Objects.requireNonNull(topic);
        Objects.requireNonNull(qos);
        return this.subscribe(new String[] { topic }, new int[] { qos });
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * net.eusashead.iot.mqtt.ObservableMqttClient#unsubscribe(java.lang.String[
     * ])
     */
    @Override
    public Completable unsubscribe(final String[] topics) {
        return this.unsubscribeFactory.create(topics);
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * net.eusashead.iot.mqtt.ObservableMqttClient#unsubscribe(java.lang.String)
     */
    @Override
    public Completable unsubscribe(final String topic) {
        Objects.requireNonNull(topic);
        return this.unsubscribe(new String[] { topic });
    }

    public static Builder builder(final String brokerUri) throws MqttException {
        return builder(brokerUri, MqttAsyncClient.generateClientId());
    }

    public static Builder builder(final String brokerUri, final String clientId) throws MqttException {
        return builder(brokerUri, clientId, new MemoryPersistence());
    }

    public static Builder builder(final String brokerUri, final String clientId,
            final MqttClientPersistence persistence) throws MqttException {
        return builder(new MqttAsyncClient(brokerUri, clientId, persistence));
    }

    public static Builder builder(final IMqttAsyncClient client) {
        return new Builder(client);
    }

}
