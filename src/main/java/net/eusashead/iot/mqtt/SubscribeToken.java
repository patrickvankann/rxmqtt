package net.eusashead.iot.mqtt;

import io.reactivex.Flowable;

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

/**
 * This interface isn't
 * exposed by the API currently 
 * and may be removed in a future release.
 * 
 * Currently, if a subscription succeeds
 * a {@link Flowable} is returned that
 * allows a subscriber to receive {@link MqttMessage}.
 * 
 * There is no separate acknowledgement that the
 * subscription succeeded.
 * 
 * @author patvanka
 *
 */
@Deprecated
public interface SubscribeToken extends MqttToken {
    
    /**
     * Returns the granted QoS 
     * when a subscription is acknowledged
     * 
     *  @return array of integers representing the QoS levels
     */
    public int[] getGrantedQos();
    
    
}
