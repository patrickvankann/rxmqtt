package net.eusashead.iot.mqtt.paho;

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

import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttToken;

public abstract class BaseEmitterMqttActionListener implements IMqttActionListener {

    protected final static Logger LOGGER = Logger.getLogger(BaseEmitterMqttActionListener.class.getName());

    @Override
    public void onFailure(final IMqttToken asyncActionToken, final Throwable exception) {
        if (LOGGER.isLoggable(Level.SEVERE)) {
            LOGGER.log(Level.SEVERE, exception.getMessage(), exception);
        }
        getOnError().onError(exception);
    }

    /**
     * Return the {@link OnError} implementation
     * for this listener so that the onFailure()
     * method can call onError()
     * 
     * @return {@link OnError} implementation.
     */
    public abstract OnError getOnError();
}
