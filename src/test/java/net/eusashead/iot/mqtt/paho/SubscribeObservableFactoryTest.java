package net.eusashead.iot.mqtt.paho;

import static org.hamcrest.Matchers.isA;

import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttAsyncClient;
import org.eclipse.paho.client.mqttv3.IMqttMessageListener;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import net.eusashead.iot.mqtt.MqttMessage;
import rx.Observable;

@RunWith(JUnit4.class)
public class SubscribeObservableFactoryTest {
    
    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Test
    public void whenCreateIsCalledThenAnObservableIsReturned() throws Exception {
        final IMqttAsyncClient client = Mockito.mock(IMqttAsyncClient.class);
        final SubscribeObservableFactory factory = new SubscribeObservableFactory(client);
        final ArgumentCaptor<IMqttActionListener> actionListener = ArgumentCaptor.forClass(IMqttActionListener.class);
        final ArgumentCaptor<IMqttMessageListener[]> messageListener = ArgumentCaptor.forClass(IMqttMessageListener[].class);
        final String[] topics = new String[]{ "topic1", "topic2" };
        final int[] qos = new int[]{ 1, 2 };
        final Observable<MqttMessage> obs = factory.create(topics, qos);
        Assert.assertNotNull(obs);
        obs.subscribe();
        Mockito.verify(client).subscribe(Mockito.same(topics),
                Mockito.same(qos),
                Mockito.isNull(),
                actionListener.capture(),
                messageListener.capture());
        Assert.assertTrue(actionListener.getValue() instanceof SubscribeObservableFactory.SubscribeActionListener);
        Assert.assertTrue(messageListener.getValue() instanceof SubscriberMqttMessageListener[]);
        Assert.assertEquals(2, messageListener.getValue().length);
    }

    @Test
    public void whenCreateIsCalledAndAnErrorOccursThenObserverOnErrorIsCalled() throws Throwable {
        expectedException.expectCause(isA(MqttException.class));
        final ArgumentCaptor<IMqttActionListener> actionListener = ArgumentCaptor.forClass(IMqttActionListener.class);
        final ArgumentCaptor<IMqttMessageListener[]> messageListener = ArgumentCaptor.forClass(IMqttMessageListener[].class);
        final String[] topics = new String[]{ "topic1", "topic2" };
        final int[] qos = new int[]{ 1, 2 };
        final IMqttAsyncClient client = Mockito.mock(IMqttAsyncClient.class);
        Mockito.when(client.subscribe(Mockito.same(topics),
                Mockito.same(qos),
                Mockito.isNull(),
                actionListener.capture(),
                messageListener.capture()))
                .thenThrow(new MqttException(MqttException.REASON_CODE_CLIENT_CONNECTED));
        final SubscribeObservableFactory factory = new SubscribeObservableFactory(client);
        final Observable<MqttMessage> obs = factory.create(topics, qos);
        obs.toBlocking().first();
    }
    
}
