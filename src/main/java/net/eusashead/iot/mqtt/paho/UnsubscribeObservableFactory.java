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

import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.paho.client.mqttv3.IMqttAsyncClient;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttException;

import rx.AsyncEmitter.BackpressureMode;
import rx.Observable;
import rx.Observer;

public class UnsubscribeObservableFactory extends BaseObservableFactory {
    
    private final static Logger LOGGER = Logger.getLogger(UnsubscribeObservableFactory.class.getName());

    static final class UnsubscribeActionListener extends ObserverMqttActionListener<Void> {

        public UnsubscribeActionListener(final Observer<? super Void> observer) {
            super(observer);
        }

        @Override
        public void onSuccess(IMqttToken asyncActionToken) {
            observer.onNext(null);
            observer.onCompleted();
        }
    }
    
    public UnsubscribeObservableFactory(final IMqttAsyncClient client) {
        super(client);
    }
    
    public Observable<Void> create(final String[] topics) {
        
        return Observable.fromAsync(observer -> {
            try {
                client.unsubscribe(topics, null, new UnsubscribeActionListener(observer));
            } catch (MqttException exception) {
                if (LOGGER.isLoggable(Level.SEVERE)) {
                    LOGGER.log(Level.SEVERE, exception.getMessage(), exception);
                }
                observer.onError(exception);
            }
        }, BackpressureMode.BUFFER);
    }
    
}
