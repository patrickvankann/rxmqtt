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

import org.eclipse.paho.client.mqttv3.IMqttAsyncClient;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.mockito.Mockito;

import io.reactivex.BackpressureStrategy;
import io.reactivex.Completable;
import io.reactivex.Flowable;
import io.reactivex.Single;
import net.eusashead.iot.mqtt.MqttMessage;
import net.eusashead.iot.mqtt.PublishToken;
import net.eusashead.iot.mqtt.paho.PahoObservableMqttClient.Builder;

@RunWith(JUnit4.class)
public class PahoObservableMqttClientTest {

    @Test(expected = NullPointerException.class)
    public void whenANullPahoMqttClientIsPassedTheConstructorThrowsAnError() {
        PahoObservableMqttClient.builder((IMqttAsyncClient) null).build();
    }

    @Test(expected = NullPointerException.class)
    public void whenANullPahoMqttBrokerUriIsPassedTheConstructorThrowsAnError() throws MqttException {
        PahoObservableMqttClient.builder((String) null).build();
    }

    @Test(expected = IllegalArgumentException.class)
    public void whenANullPahoMqttBrokerUriAndClientIdIsPassedTheConstructorThrowsAnError() throws MqttException {
        PahoObservableMqttClient.builder(null, null).build();
    }

    @Test(expected = IllegalArgumentException.class)
    public void whenANullPahoMqttBrokerUriAndClientIdAndMqttClientPersistenceIsPassedTheConstructorThrowsAnError() throws MqttException {
        PahoObservableMqttClient.builder(null, null, null).build();
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
        final Completable expected = Completable.complete();
        Mockito.when(builder.getCloseFactory().create()).thenReturn(expected);
        final PahoObservableMqttClient target = builder.build();
        final Completable actual = target.close();
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
        final ConnectFactory factory = builder.getConnectFactory();
        final Completable expected = Completable.complete();
        Mockito.when(factory.create()).thenReturn(expected);
        final PahoObservableMqttClient target = builder.build();
        final Completable actual = target.connect();
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
        final DisconnectFactory factory = builder.getDisconnectFactory();
        final Completable expected = Completable.complete();
        Mockito.when(factory.create()).thenReturn(expected);
        final PahoObservableMqttClient target = builder.build();
        final Completable actual = target.disconnect();
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
        final PublishFactory factory = builder.getPublishFactory();
        final Single<PublishToken> expected = Single.just(Mockito.mock(PublishToken.class));
        final String topic = "topic";
        final MqttMessage message = Mockito.mock(MqttMessage.class);
        Mockito.when(factory.create(topic, message)).thenReturn(expected);
        final PahoObservableMqttClient target = builder.build();
        final Single<PublishToken> actual = target.publish(topic, message);
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
        final SubscribeFactory factory = builder.getSubscribeFactory();
        final Flowable<MqttMessage> expected = Flowable.just(Mockito.mock(MqttMessage.class));
        final String[] topic = new String[] { "topic" };
        final int[] qos = new int[]{ 1 };
        Mockito.when(factory.create(topic, qos, BackpressureStrategy.BUFFER)).thenReturn(expected);
        final PahoObservableMqttClient target = builder.build();
        final Flowable<MqttMessage> actual = target.subscribe(topic, qos);
        Mockito.verify(factory).create(topic, qos, BackpressureStrategy.BUFFER);
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
        final UnsubscribeFactory factory = builder.getUnsubscribeFactory();
        final Completable expected = Completable.complete();
        final String[] topic = new String[] { "topic" };
        Mockito.when(factory.create(topic)).thenReturn(expected);
        final PahoObservableMqttClient target = builder.build();
        final Completable actual = target.unsubscribe(topic);
        Mockito.verify(factory).create(topic);
        Assert.assertEquals(expected, actual);
    }
    
    private Builder builderWithMocks(final String expectedClientId) {
        final IMqttAsyncClient client = Mockito.mock(IMqttAsyncClient.class);
        Mockito.when(client.getClientId()).thenReturn(expectedClientId);
        final CloseFactory closeFactory = Mockito.mock(CloseFactory.class);
        final ConnectFactory connectFactory = Mockito.mock(ConnectFactory.class);
        final DisconnectFactory disconnectFactory = Mockito.mock(DisconnectFactory.class);
        final PublishFactory publishFactory = Mockito.mock(PublishFactory.class);
        final SubscribeFactory subscribeFactory = Mockito.mock(SubscribeFactory.class);
        final UnsubscribeFactory unsubscribeFactory = Mockito.mock(UnsubscribeFactory.class);
        return new PahoObservableMqttClient.Builder(client)
                .setCloseFactory(closeFactory)
                .setConnectFactory(connectFactory)
                .setDisconnectFactory(disconnectFactory)
                .setPublishFactory(publishFactory)
                .setSubscribeFactory(subscribeFactory)
                .setUnsubscribeFactory(unsubscribeFactory);
    }
    
}
