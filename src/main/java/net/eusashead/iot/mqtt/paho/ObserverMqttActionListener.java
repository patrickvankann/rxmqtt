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

import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttToken;

import rx.Observer;

public abstract class ObserverMqttActionListener<T> implements IMqttActionListener {

    protected final static Logger LOGGER = Logger.getLogger(ObserverMqttActionListener.class.getName());

    protected final Observer<? super T> observer;
    
    public ObserverMqttActionListener(final Observer<? super T> observer) {
        this.observer = Objects.requireNonNull(observer);
    }

    @Override
    public void onFailure(final IMqttToken asyncActionToken, final Throwable exception) {
        if (LOGGER.isLoggable(Level.SEVERE)) {
            LOGGER.log(Level.SEVERE, exception.getMessage(), exception);
        }
        observer.onError(exception);
    }

   

}
