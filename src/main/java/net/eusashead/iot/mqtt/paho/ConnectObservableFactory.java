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
import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.paho.client.mqttv3.IMqttAsyncClient;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;

import rx.AsyncEmitter.BackpressureMode;
import rx.Observable;
import rx.Observer;

public class ConnectObservableFactory extends BaseObservableFactory {
    
    static final class ConnectActionListener extends ObserverMqttActionListener<Void> {
        
        public ConnectActionListener(final Observer<? super Void> observer) {
            super(observer);
        }
        
        @Override
        public void onSuccess(IMqttToken asyncActionToken) {
            observer.onNext(null);
            observer.onCompleted();
        }
    }

    private final MqttConnectOptions options;
    
    private final static Logger LOGGER = Logger.getLogger(ConnectObservableFactory.class.getName());

    public ConnectObservableFactory(final IMqttAsyncClient client, final MqttConnectOptions options) {
        super(client);
        this.options = Objects.requireNonNull(options);
    }

    public Observable<Void> create() {
        return Observable.fromEmitter(observer -> {
            try {
                client.connect(options, new ConnectActionListener(observer));
            } catch (MqttException exception) {
                if (LOGGER.isLoggable(Level.SEVERE)) {
                    LOGGER.log(Level.SEVERE, exception.getMessage(), exception);
                }
                observer.onError(exception);
            }
        }, BackpressureMode.BUFFER);
    }

    public MqttConnectOptions getOptions() {
        return options;
    }
}