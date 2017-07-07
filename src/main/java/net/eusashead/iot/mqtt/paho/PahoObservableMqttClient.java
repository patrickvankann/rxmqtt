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
import java.util.UUID;

import org.eclipse.paho.client.mqttv3.IMqttAsyncClient;
import org.eclipse.paho.client.mqttv3.MqttAsyncClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;

import io.reactivex.Completable;
import io.reactivex.Flowable;
import io.reactivex.Single;
import net.eusashead.iot.mqtt.MqttMessage;
import net.eusashead.iot.mqtt.ObservableMqttClient;
import net.eusashead.iot.mqtt.ObservableMqttClientBuilder;
import net.eusashead.iot.mqtt.PublishToken;

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

        public Builder(final String brokerUri) throws MqttException {
            this(new MqttAsyncClient(brokerUri, UUID.randomUUID().toString()));
        }

        public Builder(final String brokerUri, final String clientId) throws MqttException {
            this(new MqttAsyncClient(brokerUri, clientId));
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
        }

        public IMqttAsyncClient getClient() {
            return client;
        }

        public MqttConnectOptions getConnectOptions() {
            return connectOptions;
        }

        public CloseFactory getCloseFactory() {
            return closeFactory;
        }

        public ConnectFactory getConnectFactory() {
            return connectFactory;
        }

        public DisconnectFactory getDisconnectFactory() {
            return disconnectFactory;
        }

        public PublishFactory getPublishFactory() {
            return publishFactory;
        }

        public SubscribeFactory getSubscribeFactory() {
            return subscribeFactory;
        }

        public UnsubscribeFactory getUnsubscribeFactory() {
            return unsubscribeFactory;
        }

        public Builder setConnectOptions(final MqttConnectOptions connectOptions) {
            this.connectOptions = connectOptions;
            this.connectFactory = new ConnectFactory(this.client, this.connectOptions);
            return this;
        }

        public Builder setCloseFactory(CloseFactory closeFactory) {
            this.closeFactory = closeFactory;
            return this;
        }

        public Builder setConnectFactory(ConnectFactory connectFactory) {
            this.connectFactory = connectFactory;
            return this;
        }

        public Builder setDisconnectFactory(DisconnectFactory disconnectFactory) {
            this.disconnectFactory = disconnectFactory;
            return this;
        }

        public Builder setPublishFactory(PublishFactory publishFactory) {
            this.publishFactory = publishFactory;
            return this;
        }

        public Builder setSubscribeFactory(SubscribeFactory subscribeFactory) {
            this.subscribeFactory = subscribeFactory;
            return this;
        }

        public Builder setUnsubscribeFactory(UnsubscribeFactory unsubscribeFactory) {
            this.unsubscribeFactory = unsubscribeFactory;
            return this;
        }

        public PahoObservableMqttClient build() {
            return new PahoObservableMqttClient(client, closeFactory, connectFactory, disconnectFactory, publishFactory,
                    subscribeFactory, unsubscribeFactory);
        }

    }

    private final IMqttAsyncClient client;
    private final CloseFactory closeFactory;
    private final ConnectFactory connectFactory;
    private final DisconnectFactory disconnectFactory;
    private final PublishFactory publishFactory;
    private final SubscribeFactory subscribeFactory;
    private final UnsubscribeFactory unsubscribeFactory;

    private PahoObservableMqttClient(final IMqttAsyncClient client, final CloseFactory closeFactory,
            final ConnectFactory connectFactory, final DisconnectFactory disconnectFactory,
            final PublishFactory publishFactory, final SubscribeFactory subscribeFactory,
            final UnsubscribeFactory unsubscribeFactory) {
        this.client = Objects.requireNonNull(client);
        this.closeFactory = Objects.requireNonNull(closeFactory);
        this.connectFactory = Objects.requireNonNull(connectFactory);
        this.disconnectFactory = Objects.requireNonNull(disconnectFactory);
        this.publishFactory = Objects.requireNonNull(publishFactory);
        this.subscribeFactory = Objects.requireNonNull(subscribeFactory);
        this.unsubscribeFactory = Objects.requireNonNull(unsubscribeFactory);
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
     * net.eusashead.iot.mqtt.MqttMessage)
     */
    @Override
    public Single<PublishToken> publish(final String topic, final MqttMessage msg) {
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
    public Flowable<MqttMessage> subscribe(final String topics[], final int qos[]) {
        return this.subscribeFactory.create(topics, qos);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * net.eusashead.iot.mqtt.ObservableMqttClient#subscribe(java.lang.String,
     * int)
     */
    @Override
    public Flowable<MqttMessage> subscribe(final String topic, final int qos) {
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

    public static Builder build(final IMqttAsyncClient client) {
        return new Builder(client);
    }

}