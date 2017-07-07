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

import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import io.reactivex.CompletableEmitter;

@RunWith(JUnit4.class)
public class BaseEmitterMqttActionListenerTest {
    
    @Test
    public void whenOnFailureIsCalledTheObserverIsNotifiedAndTheErrorIsLogged() throws Exception {
        // Given
        final Handler handler = Mockito.mock(Handler.class);
        final ArgumentCaptor<LogRecord> logRecord = ArgumentCaptor.forClass(LogRecord.class);
        Logger.getLogger(BaseEmitterMqttActionListener.class.getName()).addHandler(handler);
        final CompletableEmitter observer = Mockito.mock(CompletableEmitter.class);
        final BaseEmitterMqttActionListener listener = new BaseEmitterMqttActionListener() {
            
            @Override
            public OnError getOnError() {
                return new OnError() {
                    
                    @Override
                    public void onError(Throwable exception) {
                        observer.onError(exception);
                    }
                };
            }
            
            @Override
            public void onSuccess(IMqttToken arg0) {
                // Not invoked
            }
        };
        
        final IMqttToken asyncActionToken = Mockito.mock(IMqttToken.class);
        final Throwable exception = Mockito.mock(Throwable.class);
        String expectedErrorMessage = "Error message";
        Mockito.when(exception.getMessage()).thenReturn(expectedErrorMessage);
        
        // When
        listener.onFailure(asyncActionToken, exception);
        
        // Then
        Mockito.verify(observer).onError(exception);
        Mockito.verify(handler).publish(logRecord.capture());
        Assert.assertEquals(Level.SEVERE, logRecord.getValue().getLevel());
        Assert.assertEquals(expectedErrorMessage, logRecord.getValue().getMessage());
        Assert.assertEquals(exception, logRecord.getValue().getThrown());
    }

}
