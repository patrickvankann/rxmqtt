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

/**
 * This interface is needed because although FlowableEmitter, CompletableEmitter
 * and SingleEmitter all have an onError(Throwable) method there is no
 * super-interface containing this method.
 *
 * Because the {@link BaseEmitterMqttActionListener} class logs errors and calls
 * onError(Throwable) for emitters of all of the above types, a wrapper is
 * needed that exposes onError(Throwable) and defers it to the wrapped RxJava
 * type.
 *
 * @author patvanka
 *
 */
public interface OnError {

    /**
     * Action to take in the event of an error occurring
     *
     * @param exception
     *            {@link Throwable}
     */
    void onError(Throwable exception);

}
