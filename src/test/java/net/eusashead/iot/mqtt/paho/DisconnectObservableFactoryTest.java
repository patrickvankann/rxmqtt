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

import net.eusashead.iot.mqtt.paho.DisconnectObservableFactory.DisconnectActionListener;
import rx.Observable;
import rx.Observer;

@RunWith(JUnit4.class)
public class DisconnectObservableFactoryTest {

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Test
    public void whenCreateIsCalledThenAnObservableIsReturned() throws Exception {
        // Given
        final IMqttAsyncClient client = Mockito.mock(IMqttAsyncClient.class);
        final DisconnectObservableFactory factory = new DisconnectObservableFactory(client);
        final ArgumentCaptor<IMqttActionListener> actionListener = ArgumentCaptor.forClass(IMqttActionListener.class);

        // When
        final Observable<Void> obs = factory.create();

        // Then
        Assert.assertNotNull(obs);
        obs.subscribe();
        Mockito.verify(client).disconnect(Mockito.isNull(),
                actionListener.capture());
        Assert.assertTrue(actionListener.getValue() instanceof DisconnectObservableFactory.DisconnectActionListener);
    }

    @Test
    public void whenCreateIsCalledAndAnErrorOccursThenObserverOnErrorIsCalled() throws Throwable {
        expectedException.expectCause(isA(MqttException.class));
        final IMqttAsyncClient client = Mockito.mock(IMqttAsyncClient.class);
        Mockito.when(client.disconnect(Mockito.isNull(),
                Mockito.any(DisconnectObservableFactory.DisconnectActionListener.class)))
        .thenThrow(new MqttException(MqttException.REASON_CODE_CLIENT_CONNECTED));
        final DisconnectObservableFactory factory = new DisconnectObservableFactory(client);
        final Observable<Void> obs = factory.create();
        obs.toBlocking().first();
    }

    @Test
    public void whenOnSuccessIsCalledThenObserverOnNextAndOnCompletedAreCalled() throws Exception {
        @SuppressWarnings("unchecked")
        final Observer<Void> observer = Mockito.mock(Observer.class);
        final DisconnectActionListener listener = new DisconnectObservableFactory.DisconnectActionListener(observer);
        final IMqttToken asyncActionToken = Mockito.mock(IMqttToken.class);
        listener.onSuccess(asyncActionToken);
        Mockito.verify(observer).onNext(null);
        Mockito.verify(observer).onCompleted();
    }

}
