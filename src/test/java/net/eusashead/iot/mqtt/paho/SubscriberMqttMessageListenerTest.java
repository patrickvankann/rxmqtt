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

import org.eclipse.paho.client.mqttv3.IMqttMessageListener;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import io.reactivex.FlowableEmitter;
import net.eusashead.iot.mqtt.MqttMessage;

@RunWith(JUnit4.class)
public class SubscriberMqttMessageListenerTest {
    
    @Test
    public void whenAValidObserverIsPassedToTheConstructorThenItIsConstructedWithoutError() {
        @SuppressWarnings("unchecked")
        final FlowableEmitter<MqttMessage> observer = Mockito.mock(FlowableEmitter.class);
        new SubscriberMqttMessageListener(observer);
    }
    
    @Test(expected=NullPointerException.class)
    public void whenANulldObserverIsPassedToTheConstructorThenItThrowsAnError() {
        final FlowableEmitter<MqttMessage> observer = null;
        new SubscriberMqttMessageListener(observer);
    }
    
    @Test
    public void whenAMessageArrivesThenTheObserverIsNotified() throws Exception {
        @SuppressWarnings("unchecked")
        final FlowableEmitter<MqttMessage> observer = Mockito.mock(FlowableEmitter.class);
        final IMqttMessageListener listener = new SubscriberMqttMessageListener(observer);
        final String expectedTopic = "expected";
        final byte[] expectedPayload = new byte[]{ 'a', 'b', 'c' };
        final org.eclipse.paho.client.mqttv3.MqttMessage expectedMessage = new org.eclipse.paho.client.mqttv3.MqttMessage(expectedPayload);
        expectedMessage.setQos(2);
        expectedMessage.setId(1);
        expectedMessage.setRetained(true);
        final ArgumentCaptor<MqttMessage> actualMessage = ArgumentCaptor.forClass(MqttMessage.class);
        listener.messageArrived(expectedTopic, expectedMessage);
        Mockito.verify(observer).onNext(actualMessage.capture());
        Assert.assertArrayEquals(expectedPayload, actualMessage.getValue().getPayload());
        Assert.assertEquals(2, actualMessage.getValue().getQos());
        Assert.assertEquals(1, actualMessage.getValue().getId());
        Assert.assertTrue(actualMessage.getValue().isRetained());
    }
}
