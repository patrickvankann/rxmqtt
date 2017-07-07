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

import io.reactivex.Completable;
import io.reactivex.CompletableEmitter;

public class DisconnectFactory extends BaseMqttActionFactory {

    private final static Logger LOGGER = Logger.getLogger(DisconnectFactory.class.getName());

    static final class DisconnectActionListener extends CompletableEmitterMqttActionListener {

        public DisconnectActionListener(final CompletableEmitter emitter) {
            super(emitter);
        }

        @Override
        public void onSuccess(IMqttToken asyncActionToken) {
            emitter.onComplete();
        }
    }

    public DisconnectFactory(final IMqttAsyncClient client) {
        super(client);
    }

    public Completable create() {
        return Completable.create(emitter -> {

            try {
                client.disconnect(null, new DisconnectActionListener(emitter));
            } catch (MqttException exception) {
                if (LOGGER.isLoggable(Level.SEVERE)) {
                    LOGGER.log(Level.SEVERE, exception.getMessage(), exception);
                }
                emitter.onError(exception);
            }
        });
    }

}