package net.eusashead.iot.mqtt.paho;

import java.util.Objects;

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

import io.reactivex.Single;
import io.reactivex.SingleEmitter;
import net.eusashead.iot.mqtt.MqttMessage;
import net.eusashead.iot.mqtt.PublishToken;

public class PublishFactory extends BaseMqttActionFactory {

    private final static Logger LOGGER = Logger.getLogger(PublishFactory.class.getName());

    static final class PublishActionListener extends BaseEmitterMqttActionListener {

        private final SingleEmitter<? super PublishToken> emitter;
        
        public PublishActionListener(final SingleEmitter<? super PublishToken> emitter) {
            this.emitter = Objects.requireNonNull(emitter);
        }
        
        @Override
        public OnError getOnError() {
            return new OnError() {
                
                @Override
                public void onError(Throwable t) {
                    emitter.onError(t);
                }
            };
        }

        @Override
        public void onSuccess(IMqttToken t) {

            PublishToken b = new PublishToken() {

                @Override
                public String getClientId() {
                    return t.getClient().getClientId();
                }

                @Override
                public String[] getTopics() {
                    return t.getTopics();
                }

                @Override
                public int getMessageId() {
                    return t.getMessageId();
                }

                @Override
                public boolean getSessionPresent() {
                    return t.getSessionPresent();
                }
                
            };
            emitter.onSuccess(b);
        }
    }

    public PublishFactory(final IMqttAsyncClient client) {
        super(client);
    }

    public Single<PublishToken> create(final String topic,
            final MqttMessage msg) {
        return Single.create(emitter -> {
            try {
                client.publish(topic, msg.getPayload(), msg.getQos(),
                        msg.isRetained(), null, new PublishActionListener(emitter));
            } catch (MqttException exception) {
                if (LOGGER.isLoggable(Level.SEVERE)) {
                    LOGGER.log(Level.SEVERE, exception.getMessage(), exception);
                }
                emitter.onError(exception);
            }
        });
    }

}
