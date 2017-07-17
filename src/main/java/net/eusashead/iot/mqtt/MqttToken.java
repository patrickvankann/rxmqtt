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

public interface MqttToken {

    String getClientId();

    /**
     * Returns the topic string for the this token or null if nothing has been
     * published
     *
     * @return {@link String} topics for the token or null
     */
    String[] getTopics();

    /**
     * Returns the identifier for the message associated with this token
     *
     * @return {@link String} message identifier
     */
    public int getMessageId();

    /**
     * Whether a session is present for this topic
     *
     * @return <code>true</code> if session present or <code>false</code>
     *         otherwise
     */
    public boolean getSessionPresent();

}
