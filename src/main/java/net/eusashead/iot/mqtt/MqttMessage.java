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

    static MqttMessage create(final int id, final byte[] payload, final int qos, final boolean retained) {
        return new BasicMqttMessage(id, payload, qos, retained);
    }

    boolean isRetained();

    int getQos();

    byte[] getPayload();

    int getId();

}

class BasicMqttMessage implements MqttMessage {

    private final int id;
    private final byte[] payload;
    private final int qos;
    private final boolean retained;

    public BasicMqttMessage(final int id, final byte[] payload, final int qos, final boolean retained) {
        this.id = id;
        this.payload = Objects.requireNonNull(payload);
        this.qos = qos;
        this.retained = retained;
    }

    @Override
    public int getId() {
        return id;
    }

    @Override
    public byte[] getPayload() {
        return payload;
    }

    @Override
    public int getQos() {
        return qos;
    }

    @Override
    public boolean isRetained() {
        return retained;
    }

   

    @Override
    public String toString() {
        final StringBuilder builder = new StringBuilder(50);
        builder.append("BasicMqttMessage [id=").append(id).append(", payload=").append(Arrays.toString(payload))
                .append(", qos=").append(qos).append(", retained=").append(retained).append("]");
        return builder.toString();
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + id;
        result = prime * result + Arrays.hashCode(payload);
        result = prime * result + qos;
        result = prime * result + (retained ? 1231 : 1237);
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof BasicMqttMessage)) {
            return false;
        }
        BasicMqttMessage other = (BasicMqttMessage) obj;
        if (id != other.id) {
            return false;
        }
        if (!Arrays.equals(payload, other.payload)) {
            return false;
        }
        if (qos != other.qos) {
            return false;
        }
        if (retained != other.retained) {
            return false;
        }
        return true;
    }

}
