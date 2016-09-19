package net.eusashead.iot.mqtt.paho;

import org.eclipse.paho.client.mqttv3.IMqttAsyncClient;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.mockito.Mockito;

import net.eusashead.iot.mqtt.MqttMessage;
import net.eusashead.iot.mqtt.PublishToken;
import net.eusashead.iot.mqtt.paho.PahoObservableMqttClient.Builder;
import rx.Observable;

@RunWith(JUnit4.class)
public class PahoObservableMqttClientTest {
    
    @Test(expected = NullPointerException.class)
    public void whenANullPahoMqttClientIsPassedTheConstructorThrowsAnError() {
        PahoObservableMqttClient.build(null).build();
    }

    @Test
    public void whenGetClientIdIsCalledItReturnsPahoClientId() {
        final String expectedClientId = "clientId";
        final IMqttAsyncClient client = Mockito.mock(IMqttAsyncClient.class);
        Mockito.when(client.getClientId()).thenReturn(expectedClientId);
        final Builder builder = new PahoObservableMqttClient.Builder(client);
        final PahoObservableMqttClient target = builder.build();
        Assert.assertEquals(expectedClientId, target.getClientId());
    }
    
    @Test
    public void whenGetBrokerUriIsCalledItReturnsPahoServerUrl() {
        final String expectedBrokerUri = "brokerUri";
        final IMqttAsyncClient client = Mockito.mock(IMqttAsyncClient.class);
        Mockito.when(client.getServerURI()).thenReturn(expectedBrokerUri);
        final Builder builder = new PahoObservableMqttClient.Builder(client);
        final PahoObservableMqttClient target = builder.build();
        Assert.assertEquals(expectedBrokerUri, target.getBrokerUri());
    }
    
    
    @Test
    public void whenThePahoClientIsConnectedIsConnectedReturnsTrue() {
        final IMqttAsyncClient client = Mockito.mock(IMqttAsyncClient.class);
        Mockito.when(client.isConnected()).thenReturn(true);
        final Builder builder = new PahoObservableMqttClient.Builder(client);
        final PahoObservableMqttClient target = builder.build();
        Assert.assertEquals(true, target.isConnected());
    }

    @Test(expected = NullPointerException.class)
    public void whenANullCloseFactoryIsProvidedAnErrorOccurs() {
        final Builder builder = builderWithMocks("clientId");
        builder.setCloseFactory(null);
        builder.build();
    }
    
    @Test
    public void whenCloseIsCalledThenCreateIsCalled() {
        final Builder builder = builderWithMocks("clientId");
        final Observable<Void> expected = Observable.just(null);
        Mockito.when(builder.getCloseFactory().create()).thenReturn(expected);
        final PahoObservableMqttClient target = builder.build();
        final Observable<Void> actual = target.close();
        Mockito.verify(builder.getCloseFactory()).create();
        Assert.assertEquals(expected, actual);
    }
    
    @Test(expected = NullPointerException.class)
    public void whenANullConnectactoryIsProvidedAnErrorOccurs() {
        final Builder builder = builderWithMocks("clientId");
        builder.setConnectFactory(null);
        builder.build();
    }
    
    @Test
    public void whenConnectIsCalledThenCreateIsCalled() {
        final Builder builder = builderWithMocks("clientId");
        final ConnectObservableFactory factory = builder.getConnectFactory();
        final Observable<Void> expected = Observable.just(null);
        Mockito.when(factory.create()).thenReturn(expected);
        final PahoObservableMqttClient target = builder.build();
        final Observable<Void> actual = target.connect();
        Mockito.verify(factory).create();
        Assert.assertEquals(expected, actual);
    }
    
    @Test(expected = NullPointerException.class)
    public void whenANullDisconnectFactoryIsProvidedAnErrorOccurs() {
        final Builder builder = builderWithMocks("clientId");
        builder.setConnectFactory(null);
        builder.build();
    }
    
    @Test
    public void whenDisconnectIsCalledThenCreateIsCalled() {
        final Builder builder = builderWithMocks("clientId");
        final DisconnectObservableFactory factory = builder.getDisconnectFactory();
        final Observable<Void> expected = Observable.just(null);
        Mockito.when(factory.create()).thenReturn(expected);
        final PahoObservableMqttClient target = builder.build();
        final Observable<Void> actual = target.disconnect();
        Mockito.verify(factory).create();
        Assert.assertEquals(expected, actual);
    }
    
    @Test(expected = NullPointerException.class)
    public void whenANullPublishFactoryIsProvidedAnErrorOccurs() {
        final Builder builder = builderWithMocks("clientId");
        builder.setPublishFactory(null);
        builder.build();
    }
    
    @Test
    public void whenPublishCalledThenCreateIsCalled() {
        final Builder builder = builderWithMocks("clientId");
        final PublishObservableFactory factory = builder.getPublishFactory();
        final Observable<PublishToken> expected = Observable.just(Mockito.mock(PublishToken.class));
        final String topic = "topic";
        final MqttMessage message = Mockito.mock(MqttMessage.class);
        Mockito.when(factory.create(topic, message)).thenReturn(expected);
        final PahoObservableMqttClient target = builder.build();
        final Observable<PublishToken> actual = target.publish(topic, message);
        Mockito.verify(factory).create( topic, message);
        Assert.assertEquals(expected, actual);
    }
    
    @Test(expected = NullPointerException.class)
    public void whenANullSubscribeFactoryIsProvidedAnErrorOccurs() {
        final Builder builder = builderWithMocks("clientId");
        builder.setSubscribeFactory(null);
        builder.build();
    }
    
    @Test
    public void whenSubscribeIsCalledThenCreateIsCalled() {
        final Builder builder = builderWithMocks("clientId");
        final SubscribeObservableFactory factory = builder.getSubscribeFactory();
        final Observable<MqttMessage> expected = Observable.just(Mockito.mock(MqttMessage.class));
        final String[] topic = new String[] { "topic" };
        final int[] qos = new int[]{ 1 };
        Mockito.when(factory.create(topic, qos)).thenReturn(expected);
        final PahoObservableMqttClient target = builder.build();
        final Observable<MqttMessage> actual = target.subscribe(topic, qos);
        Mockito.verify(factory).create(topic, qos);
        Assert.assertEquals(expected, actual);
    }
    
    @Test(expected = NullPointerException.class)
    public void whenANullUnsubscribeFactoryIsProvidedAnErrorOccurs() {
        final Builder builder = builderWithMocks("clientId");
        builder.setUnsubscribeFactory(null);
        builder.build();
    }
    
    @Test
    public void whenUnsubscribeIsCalledThenCreateIsCalled() {
        final Builder builder = builderWithMocks("clientId");
        final UnsubscribeObservableFactory factory = builder.getUnsubscribeFactory();
        final Observable<Void> expected = Observable.just(null);
        final String[] topic = new String[] { "topic" };
        Mockito.when(factory.create(topic)).thenReturn(expected);
        final PahoObservableMqttClient target = builder.build();
        final Observable<Void> actual = target.unsubscribe(topic);
        Mockito.verify(factory).create(topic);
        Assert.assertEquals(expected, actual);
    }
    
    private Builder builderWithMocks(final String expectedClientId) {
        final IMqttAsyncClient client = Mockito.mock(IMqttAsyncClient.class);
        Mockito.when(client.getClientId()).thenReturn(expectedClientId);
        final CloseObservableFactory closeFactory = Mockito.mock(CloseObservableFactory.class);
        final ConnectObservableFactory connectFactory = Mockito.mock(ConnectObservableFactory.class);
        final DisconnectObservableFactory disconnectFactory = Mockito.mock(DisconnectObservableFactory.class);
        final PublishObservableFactory publishFactory = Mockito.mock(PublishObservableFactory.class);
        final SubscribeObservableFactory subscribeFactory = Mockito.mock(SubscribeObservableFactory.class);
        final UnsubscribeObservableFactory unsubscribeFactory = Mockito.mock(UnsubscribeObservableFactory.class);
        return new PahoObservableMqttClient.Builder(client)
                .setCloseFactory(closeFactory)
                .setConnectFactory(connectFactory)
                .setDisconnectFactory(disconnectFactory)
                .setPublishFactory(publishFactory)
                .setSubscribeFactory(subscribeFactory)
                .setUnsubscribeFactory(unsubscribeFactory);
    }
    
}
