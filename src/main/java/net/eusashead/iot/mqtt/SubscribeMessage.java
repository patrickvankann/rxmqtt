package net.eusashead.iot.mqtt;

import java.util.Arrays;
import java.util.Objects;

/*
 * #[license]
 * rxmqtt
 * %%
 * Copyright (C) 2013 - 2018 Eusa's Head
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

public interface SubscribeMessage extends MqttMessage {

    int getId();

    String getTopic();

    static SubscribeMessage create(final int id, final String topic,
            final byte[] payload, final int qos, final boolean retained) {
        return new SubscribeMessageImpl(id, topic, payload, qos, retained);
    }

    class SubscribeMessageImpl extends AbstractMqttMessage
            implements SubscribeMessage {

        private final int id;
        private final String topic;

        private SubscribeMessageImpl(final int id, final String topic,
                final byte[] payload, final int qos, final boolean retained) {
            super(payload, qos, retained);
            this.id = Objects.requireNonNull(id);
            this.topic = Objects.requireNonNull(topic);
        }

        @Override
        public int getId() {
            return this.id;
        }

        @Override
        public String getTopic() {
            return this.topic;
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = super.hashCode();
            result = prime * result + id;
            result = prime * result + ((topic == null) ? 0 : topic.hashCode());
            return result;
        }

        @Override
        public boolean equals(final Object obj) {
            if (this == obj) {
                return true;
            }
            if (!super.equals(obj)) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            final SubscribeMessageImpl other = (SubscribeMessageImpl) obj;
            if (id != other.id) {
                return false;
            }
            if (topic == null) {
                if (other.topic != null) {
                    return false;
                }
            } else if (!topic.equals(other.topic)) {
                return false;
            }
            return true;
        }

        @Override
        public String toString() {
            return "SubscribeMessageImpl [id=" + id + ", topic=" + topic
                    + ", payload=" + Arrays.toString(payload) + ", qos=" + qos
                    + ", retained=" + retained + "]";
        }

    }

}
