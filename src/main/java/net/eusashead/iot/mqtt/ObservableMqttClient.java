package net.eusashead.iot.mqtt;

import io.reactivex.Completable;

/*
 * #[license]
 * rxmqtt
 * %%
 * Copyright (C) 2013 - 2017 Eusa's Head
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

import io.reactivex.Flowable;
import io.reactivex.Single;

/**
 * RxJava MQTT API
 * 
 * @author pvankann@gmail.com
 *
 */
public interface ObservableMqttClient {

    /**
     * Get the MQTT broker URI
     * 
     * @return the MQTT broker URI string
     */
    String getBrokerUri();

    /**
     * Get the MQTT client id from the underlying MQTT client
     * 
     * @return {@link String} client identifier
     */
    String getClientId();

    /**
     * Whether the MQTT client is connected to the broker
     * 
     * @return <code>true</code> if connected, <code>false</code> otherwise
     */
    boolean isConnected();

    /**
     * Close the MQTT client
     * 
     * @return {@link Completable} that will complete on successful closure
     */
    Completable close();

    /**
     * Connect the MQTT client
     * 
     * @return {@link Completable} that will complete on successful connection
     */
    Completable connect();

    /**
     * Disconnect the MQTT client
     * 
     * @return {@link Completable} that will complete on successful
     *         disconnection
     */
    Completable disconnect();

    /**
     * Publish an {@link MqttMessage} to a {@link String} topic at the given QOS
     * level
     * 
     * @return {@link Single} that will complete on successful publication with
     *         a {@link PublishToken}
     */
    Single<PublishToken> publish(String topic, MqttMessage msg);

    /**
     * Subscribe to multiple {@link String} topics to receive multiple
     * {@link MqttMessage} at the given QOS levels
     * 
     * @return {@link Single} that will complete on successful publication with
     *         a {@link PublishToken}
     */
    Flowable<MqttMessage> subscribe(String[] topics, int[] qos);

    /**
     * Subscribe to a {@link String} topic to receive multiple
     * {@link MqttMessage} at the supplied QOS level
     * 
     * @return {@link Flowable} that will receive multiple {@link MqttMessage}
     */
    Flowable<MqttMessage> subscribe(String topic, int qos);

    /**
     * Unsubscribe from the given topics {@link String} array
     * 
     * @return {@link Completable} that will complete on successful unsubscribe
     */
    Completable unsubscribe(String[] topics);

    /**
     * Unsubscribe from the given topic {@link String}
     * 
     * @return {@link Completable} that will complete on successful unsubscribe
     */
    Completable unsubscribe(String topic);

}
