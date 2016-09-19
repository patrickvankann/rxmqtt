package net.eusashead.iot.mqtt.paho;

import static org.hamcrest.Matchers.isA;

import org.eclipse.paho.client.mqttv3.IMqttAsyncClient;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.mockito.Mockito;

import rx.Observable;

@RunWith(JUnit4.class)
public class CloseObservableFactoryTest {

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Test
    public void whenCreateIsCalledThenAnObservableIsReturned() throws Exception {
        // Given
        final IMqttAsyncClient client = Mockito.mock(IMqttAsyncClient.class);
        final CloseObservableFactory factory = new CloseObservableFactory(client);

        // When
        final Observable<Void> obs = factory.create();

        // Then
        Assert.assertNotNull(obs);
        obs.subscribe();
        Mockito.verify(client).close();
    }

    @Test
    public void whenCreateIsCalledAndAnErrorOccursThenObserverOnErrorIsCalled() throws Throwable {
        expectedException.expectCause(isA(MqttException.class));
        final IMqttAsyncClient client = Mockito.mock(IMqttAsyncClient.class);
        Mockito.doThrow(new MqttException(MqttException.REASON_CODE_CLIENT_CONNECTED)).when(client).close();
        final CloseObservableFactory factory = new CloseObservableFactory(client);
        final Observable<Void> obs = factory.create();
        obs.toBlocking().first();
    }

}
