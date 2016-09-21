package net.eusashead.iot.mqtt;

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

import rx.Observable;

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

    Observable<Void> close();

    Observable<Void> connect();

    Observable<Void> disconnect();

    Observable<PublishToken> publish(String topic, MqttMessage msg);

    Observable<MqttMessage> subscribe(String[] topics, int[] qos);

    Observable<MqttMessage> subscribe(String topic, int qos);

    Observable<Void> unsubscribe(String[] topics);

    Observable<Void> unsubscribe(String topic);

}
