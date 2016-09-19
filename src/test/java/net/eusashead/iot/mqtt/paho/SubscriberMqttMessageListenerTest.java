package net.eusashead.iot.mqtt.paho;

import org.eclipse.paho.client.mqttv3.IMqttMessageListener;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import net.eusashead.iot.mqtt.MqttMessage;
import rx.Observer;

@RunWith(JUnit4.class)
public class SubscriberMqttMessageListenerTest {
    
    @Test
    public void whenAValidObserverIsPassedToTheConstructorThenItIsConstructedWithoutError() {
        @SuppressWarnings("unchecked")
        final Observer<MqttMessage> observer = Mockito.mock(Observer.class);
        new SubscriberMqttMessageListener(observer);
    }
    
    @Test(expected=NullPointerException.class)
    public void whenANulldObserverIsPassedToTheConstructorThenItThrowsAnError() {
        final Observer<MqttMessage> observer = null;
        new SubscriberMqttMessageListener(observer);
    }
    
    @Test
    public void whenAMessageArrivesThenTheObserverIsNotified() throws Exception {
        @SuppressWarnings("unchecked")
        final Observer<MqttMessage> observer = Mockito.mock(Observer.class);
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
