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
     * Returns the topic string(s) for the action being tracked by this
     * token. If the action has not been initiated or the action has not
     * topic associated with it such as connect then null will be returned.
     *
     * @return the topic string(s) for the subscribe being tracked by this token or null
     */
    String[] getTopics();

    /**
     * Returns the message ID of the message that is associated with the token.
     * A message id of zero will be returned for tokens associated with
     * connect, disconnect and ping operations as there can only ever
     * be one of these outstanding at a time. For other operations
     * the MQTT message id flowed over the network.
     */
    public int getMessageId();
    
    
    /**
     * Returns the session present flag from a connack 
     */
    public boolean getSessionPresent();
    

}
