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

import java.util.Arrays;
import java.util.Objects;

public interface MqttMessage {

    boolean isRetained();

    int getQos();

    byte[] getPayload();

}

abstract class AbstractMqttMessage implements MqttMessage {

    protected final byte[] payload;
    protected final int qos;
    protected final boolean retained;

    AbstractMqttMessage(final byte[] payload, final int qos, final boolean retained) {
        this.payload = Objects.requireNonNull(payload);
        this.qos = qos;
        this.retained = retained;
    }
    
    @Override
    public byte[] getPayload() {
        return this.payload;
    }

    @Override
    public int getQos() {
        return this.qos;
    }

    @Override
    public boolean isRetained() {
        return this.retained;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + Arrays.hashCode(payload);
        result = prime * result + qos;
        result = prime * result + (retained ? 1231 : 1237);
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        AbstractMqttMessage other = (AbstractMqttMessage) obj;
        if (!Arrays.equals(payload, other.payload))
            return false;
        if (qos != other.qos)
            return false;
        if (retained != other.retained)
            return false;
        return true;
    }

}
