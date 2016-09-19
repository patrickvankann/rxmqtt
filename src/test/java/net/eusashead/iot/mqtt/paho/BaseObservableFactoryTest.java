package net.eusashead.iot.mqtt.paho;

import org.eclipse.paho.client.mqttv3.IMqttAsyncClient;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.mockito.Mockito;

@RunWith(JUnit4.class)
public class BaseObservableFactoryTest {
    
    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Test
    public void whenAValidMqttAsyncClientIsSuppliedItIsConstructed() {
        final IMqttAsyncClient client = Mockito.mock(IMqttAsyncClient.class);
        final BaseObservableFactory factory = new BaseObservableFactory(client);
        Assert.assertEquals(client, factory.getMqttAsyncClient());
    }
    
    @Test(expected=NullPointerException.class)
    public void whenANullMqttAsyncClientIsSuppliedAnExceptionIsThrown() {
        new BaseObservableFactory(null);
    }
    
}
