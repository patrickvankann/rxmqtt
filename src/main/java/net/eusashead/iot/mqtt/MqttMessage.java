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
        return this.id;
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
    public String toString() {
        final StringBuilder builder = new StringBuilder(50);
        builder.append("BasicMqttMessage [id=").append(this.id).append(", payload=")
                .append(Arrays.toString(this.payload)).append(", qos=").append(this.qos).append(", retained=")
                .append(this.retained).append("]");
        return builder.toString();
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + this.id;
        result = prime * result + Arrays.hashCode(this.payload);
        result = prime * result + this.qos;
        result = prime * result + (this.retained ? 1231 : 1237);
        return result;
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof BasicMqttMessage)) {
            return false;
        }
        final BasicMqttMessage other = (BasicMqttMessage) obj;
        if (this.id != other.id) {
            return false;
        }
        if (!Arrays.equals(this.payload, other.payload)) {
            return false;
        }
        if (this.qos != other.qos) {
            return false;
        }
        if (this.retained != other.retained) {
            return false;
        }
        return true;
    }

}
