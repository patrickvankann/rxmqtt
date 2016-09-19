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

import net.eusashead.iot.mqtt.paho.UnsubscribeObservableFactory.UnsubscribeActionListener;
import rx.Observable;
import rx.Observer;

@RunWith(JUnit4.class)
public class UnsubscribeObservableFactoryTest {

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Test
    public void whenCreateIsCalledThenAnObservableIsReturned() throws Exception {
        // Given
        final IMqttAsyncClient client = Mockito.mock(IMqttAsyncClient.class);
        final UnsubscribeObservableFactory factory = new UnsubscribeObservableFactory(client);
        final String[] topics = new String[]{ "topic1", "topic2" };
        final ArgumentCaptor<IMqttActionListener> actionListener = ArgumentCaptor.forClass(IMqttActionListener.class);

        // When
        final Observable<Void> obs = factory.create(topics);

        // Then
        Assert.assertNotNull(obs);
        obs.subscribe();
        Mockito.verify(client).unsubscribe(Mockito.same(topics), Mockito.isNull(),
                actionListener.capture());
        Assert.assertTrue(actionListener.getValue() instanceof UnsubscribeObservableFactory.UnsubscribeActionListener);
    }

    @Test
    public void whenCreateIsCalledAndAnErrorOccursThenObserverOnErrorIsCalled() throws Throwable {
        expectedException.expectCause(isA(MqttException.class));
        final IMqttAsyncClient client = Mockito.mock(IMqttAsyncClient.class);
        Mockito.when(client.unsubscribe(Mockito.any(String[].class), Mockito.isNull(),
                Mockito.any(UnsubscribeObservableFactory.UnsubscribeActionListener.class)))
        .thenThrow(new MqttException(MqttException.REASON_CODE_CLIENT_CONNECTED));
        final UnsubscribeObservableFactory factory = new UnsubscribeObservableFactory(client);
        final Observable<Void> obs = factory.create(new String[]{ "topic1", "topic2" });
        obs.toBlocking().first();
    }

    @Test
    public void whenOnSuccessIsCalledThenObserverOnNextAndOnCompletedAreCalled() throws Exception {
        @SuppressWarnings("unchecked")
        final Observer<Void> observer = Mockito.mock(Observer.class);
        final UnsubscribeActionListener listener = new UnsubscribeObservableFactory.UnsubscribeActionListener(observer);
        final IMqttToken asyncActionToken = Mockito.mock(IMqttToken.class);
        listener.onSuccess(asyncActionToken);
        Mockito.verify(observer).onNext(null);
        Mockito.verify(observer).onCompleted();
    }

}
