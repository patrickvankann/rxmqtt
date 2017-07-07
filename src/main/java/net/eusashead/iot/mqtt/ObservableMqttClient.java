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

    String getClientId();

    boolean isConnected();

    Completable close();

    Completable connect();

    Completable disconnect();

    Flowable<PublishToken> publish(String topic, MqttMessage msg);

    Flowable<MqttMessage> subscribe(String[] topics, int[] qos);

    Flowable<MqttMessage> subscribe(String topic, int qos);

    Completable unsubscribe(String[] topics);

    Completable unsubscribe(String topic);

}
