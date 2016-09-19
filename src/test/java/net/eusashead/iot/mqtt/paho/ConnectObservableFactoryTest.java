package net.eusashead.iot.mqtt.paho;

import static org.hamcrest.Matchers.isA;

import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttAsyncClient;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import net.eusashead.iot.mqtt.paho.ConnectObservableFactory.ConnectActionListener;
import rx.Observable;
import rx.Observer;

@RunWith(JUnit4.class)
public class ConnectObservableFactoryTest {
    
    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Test
    public void whenCreateIsCalledThenAnObservableIsReturned() throws Exception {
        final IMqttAsyncClient client = Mockito.mock(IMqttAsyncClient.class);
        final MqttConnectOptions options = Mockito.mock(MqttConnectOptions.class);
        final ConnectObservableFactory factory = new ConnectObservableFactory(client, options);
        final ArgumentCaptor<IMqttActionListener> actionListener = ArgumentCaptor.forClass(IMqttActionListener.class);
        final Observable<Void> obs = factory.create();
        Assert.assertNotNull(obs);
        obs.subscribe();
        Mockito.verify(client).connect(Mockito.same(options),
                actionListener.capture());
        Assert.assertTrue(actionListener.getValue() instanceof ConnectObservableFactory.ConnectActionListener);
    }

    @Test
    public void whenCreateIsCalledAndAnErrorOccursThenObserverOnErrorIsCalled() throws Throwable {
        expectedException.expectCause(isA(MqttException.class));
        final MqttConnectOptions options = Mockito.mock(MqttConnectOptions.class);
        final IMqttAsyncClient client = Mockito.mock(IMqttAsyncClient.class);
        Mockito.when(client.connect(Mockito.same(options),
                Mockito.any(ConnectObservableFactory.ConnectActionListener.class)))
                .thenThrow(new MqttException(MqttException.REASON_CODE_CLIENT_CONNECTED));
        final ConnectObservableFactory factory = new ConnectObservableFactory(client, options);
        final Observable<Void> obs = factory.create();
        obs.toBlocking().first();
    }

    @Test
    public void whenOnSuccessIsCalledThenObserverOnNextAndOnCompletedAreCalled() throws Exception {
        @SuppressWarnings("unchecked")
        final Observer<Void> observer = Mockito.mock(Observer.class);
        final ConnectActionListener listener = new ConnectObservableFactory.ConnectActionListener(observer);
        final IMqttToken asyncActionToken = Mockito.mock(IMqttToken.class);
        listener.onSuccess(asyncActionToken);
        Mockito.verify(observer).onNext(null);
        Mockito.verify(observer).onCompleted();
    }
    
}
