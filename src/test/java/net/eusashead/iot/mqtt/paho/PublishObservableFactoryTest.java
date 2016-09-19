package net.eusashead.iot.mqtt.paho;

import static org.hamcrest.Matchers.isA;

import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttAsyncClient;
import org.eclipse.paho.client.mqttv3.IMqttToken;
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
import net.eusashead.iot.mqtt.MqttToken;
import net.eusashead.iot.mqtt.PublishToken;
import net.eusashead.iot.mqtt.paho.PublishObservableFactory.PublishActionListener;
import rx.Observable;
import rx.Observer;

@RunWith(JUnit4.class)
public class PublishObservableFactoryTest {

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Test
    public void whenCreateIsCalledThenAnObservableIsReturned() throws Exception {
        // Given
        final IMqttAsyncClient client = Mockito.mock(IMqttAsyncClient.class);
        final PublishObservableFactory factory = new PublishObservableFactory(client);
        final String topic = "topic1";
        final MqttMessage msg = MqttMessage.create(0, new byte[] { 'a',  'b',  'c' }, 1, true);
        final ArgumentCaptor<IMqttActionListener> actionListener = ArgumentCaptor.forClass(IMqttActionListener.class);

        // When
        final Observable<PublishToken> obs = factory.create(topic, msg);

        // Then
        Assert.assertNotNull(obs);
        obs.subscribe();
        Mockito.verify(client).publish(Mockito.same(topic), 
                Mockito.same(msg.getPayload()), Mockito.anyInt(),
                Mockito.anyBoolean(), Mockito.any(),
                actionListener.capture());
        Assert.assertTrue(actionListener.getValue() instanceof PublishObservableFactory.PublishActionListener);
    }

    @Test
    public void whenCreateIsCalledAndAnErrorOccursThenObserverOnErrorIsCalled() throws Throwable {
        expectedException.expectCause(isA(MqttException.class));
        final IMqttAsyncClient client = Mockito.mock(IMqttAsyncClient.class);
        Mockito.when(client.publish(Mockito.any(String.class), 
                Mockito.any(byte[].class), Mockito.any(int.class), 
                Mockito.any(boolean.class), Mockito.isNull(),
                Mockito.any(PublishObservableFactory.PublishActionListener.class)))
        .thenThrow(new MqttException(MqttException.REASON_CODE_CLIENT_CONNECTED));
        final PublishObservableFactory factory = new PublishObservableFactory(client);
        final Observable<PublishToken> obs = factory.create("topic1", Mockito.mock(MqttMessage.class));
        obs.toBlocking().first();
    }

    @Test
    public void whenOnSuccessIsCalledThenObserverOnNextAndOnCompletedAreCalled() throws Exception {
        @SuppressWarnings("unchecked")
        final Observer<MqttToken> observer = Mockito.mock(Observer.class);
        final PublishActionListener listener = new PublishObservableFactory.PublishActionListener(observer);
        final IMqttAsyncClient client = Mockito.mock(IMqttAsyncClient.class);
        Mockito.when(client.getClientId()).thenReturn("client_id");
        final IMqttToken iMqttDeliveryToken = Mockito.mock(IMqttToken.class);
        Mockito.when(iMqttDeliveryToken.getClient()).thenReturn(client);
        Mockito.when(iMqttDeliveryToken.getMessageId()).thenReturn(123);
        Mockito.when(iMqttDeliveryToken.getSessionPresent()).thenReturn(false);
        Mockito.when(iMqttDeliveryToken.getGrantedQos()).thenReturn(new int[0]);
        Mockito.when(iMqttDeliveryToken.getTopics()).thenReturn(new String[]{"topic"});
        final ArgumentCaptor<MqttToken> publishToken = ArgumentCaptor.forClass(MqttToken.class);
        listener.onSuccess(iMqttDeliveryToken);
        Mockito.verify(observer).onNext(publishToken.capture());
        Mockito.verify(observer).onCompleted();
        Assert.assertNotNull(iMqttDeliveryToken);
        Assert.assertNotNull(publishToken);
        Assert.assertNotNull(publishToken.getValue().getClientId());
        Assert.assertEquals(iMqttDeliveryToken.getClient().getClientId(), publishToken.getValue().getClientId());
        Assert.assertNotNull(publishToken.getValue().getMessageId());
        Assert.assertEquals(iMqttDeliveryToken.getMessageId(), publishToken.getValue().getMessageId());
        Assert.assertNotNull(publishToken.getValue().getTopics());
        Assert.assertArrayEquals(iMqttDeliveryToken.getTopics(), publishToken.getValue().getTopics());
        Assert.assertNotNull(publishToken.getValue().getMessageId());
        Assert.assertEquals(iMqttDeliveryToken.getMessageId(), publishToken.getValue().getMessageId());
    }

}
