package net.eusashead.iot.mqtt.paho;

/*
 * #[license]
 * rxmqtt
 * %%
 * Copyright (C) 2013 - 2018 Eusa's Head
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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.eclipse.paho.client.mqttv3.MqttAsyncClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.TimerPingSender;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import io.moquette.interception.AbstractInterceptHandler;
import io.moquette.interception.messages.InterceptPublishMessage;
import io.moquette.server.Server;
import io.moquette.server.config.IConfig;
import io.moquette.server.config.MemoryConfig;
import net.eusashead.iot.mqtt.ObservableMqttClient;
import net.eusashead.iot.mqtt.PublishMessage;
import net.eusashead.iot.mqtt.SubscribeMessage;

@RunWith(JUnit4.class)
public class MoquetteConnectivityTest {
	
    // Broker host and port
    private static final int BROKER_PORT = 9097;
    private static final String BROKER_HOST = "127.0.0.1";
    
    // Proxy host and port
    private static final int PROXY_PORT = 9098;
    private static final String PROXY_HOST = "127.0.0.1";
    
    // Broker and proxy
    private MqttBroker broker;
    private ProxySocketServer brokerProxy;
    
    @Before
    public void before() throws Exception {
        
    	// Create broker and start
    	this.broker = new MqttBroker(BROKER_PORT, BROKER_HOST);
        this.broker.start();
        
        // Start the brokerProxy
        this.brokerProxy = new ProxySocketServer(PROXY_HOST, PROXY_PORT, BROKER_HOST, BROKER_PORT);
        this.brokerProxy.start();
        
    }
    
    @After
    public void after() throws Exception {
        
    	// Remove the proxy
        if (this.brokerProxy != null) {
            this.brokerProxy.stop();
        }
        
        // Stop the broker
        if (this.broker != null) {
            this.broker.stop();
        }
    }

    @Test
    public void whenWeConnectPahoAsyncToTestBrokerThenItCanPublishMessages() throws Throwable {
        
        // Connect, publish, disconnect
        MqttAsyncClient asyncClient = new MqttAsyncClient(this.broker.toUrl(), "test-client-id", new MemoryPersistence());
        AsyncPahoUtils.connect(asyncClient);
        AsyncPahoUtils.publish(asyncClient, "topical", "Test message".getBytes());
        AsyncPahoUtils.disconnect(asyncClient);
        
        // Check we received a message
        Assert.assertEquals(1, broker.getMessages().size());
    }
    
    @Test
    public void whenWeConnectObservableClientToTestBrokerThenItCanPublishMessages() throws Throwable {
        
        // Connect, publish, disconnect
        MqttAsyncClient asyncClient = new MqttAsyncClient(this.broker.toUrl(), "test-client-id", new MemoryPersistence());
        ObservableMqttClient observableClient = observableClient(asyncClient, new MqttConnectOptions());
        observableClient.connect().blockingAwait();
        observableClient.publish("topical", PublishMessage.create("Test message".getBytes(), 2, false)).blockingGet();
        observableClient.disconnect().blockingAwait();
        
        // Check we received a message
        Assert.assertEquals(1, broker.getMessages().size());
    }
    
    @Test
    public void whenBrokerIsStoppedThenClientIsDisconnected() throws Throwable {
        
    	// Create client with re-connect and dirty sessions
        MqttConnectOptions options = new MqttConnectOptions();
        options.setAutomaticReconnect(false);
        options.setCleanSession(false);
        options.setKeepAliveInterval(1);
        options.setConnectionTimeout(1);
        
        String proxyUrl = "tcp://" + PROXY_HOST + ":" + PROXY_PORT;
    	MqttAsyncClient asyncClient = new MqttAsyncClient(proxyUrl, "test-client-id", new MemoryPersistence());
        ObservableMqttClient observableClient = observableClient(asyncClient, options);
        
        // Connect
        observableClient.connect().blockingAwait();
        Assert.assertTrue(observableClient.isConnected());

        // Stop the broker proxy
        this.brokerProxy.disable();
        Thread.sleep(3000);
        Assert.assertFalse(observableClient.isConnected());
     
        // Restart the broker proxy
        this.brokerProxy.enable();
        Thread.sleep(3000);
        Assert.assertFalse(observableClient.isConnected());
        
    }
    
    @Test
    public void whenBrokerIsRestartedThenWithAutoReconnectClientIsReconnected() throws Throwable {
        
        // Create client with re-connect and dirty sessions
        MqttConnectOptions options = new MqttConnectOptions();
        options.setAutomaticReconnect(true);
        options.setCleanSession(false);
        options.setKeepAliveInterval(1);
        options.setConnectionTimeout(1);
        
        String proxyUrl = "tcp://" + PROXY_HOST + ":" + PROXY_PORT;
        MqttAsyncClient asyncClient = new MqttAsyncClient(proxyUrl, "test-client-id", new MemoryPersistence());
        ObservableMqttClient observableClient = observableClient(asyncClient, options);
        
        // Connect
        observableClient.connect().blockingAwait();
        Assert.assertTrue(observableClient.isConnected());

        // Stop the broker proxy
        this.brokerProxy.disable();
        Thread.sleep(3000);
        Assert.assertFalse(observableClient.isConnected());
     
        // Restart the broker proxy
        this.brokerProxy.enable();
        Thread.sleep(5000);
        Assert.assertTrue(observableClient.isConnected());	
    }
    
    /**
    * This test is unreliable because the subscriptions
    * don't always arrive.
    * Need to investigate why this is and reinstate.
    */

    @Ignore
    @Test
    public void whenAutoReconnectAndCleanSessionFalseThenNoNeedToResubscribe() throws Throwable {
    	
    	// Create a latch and a way to count subscription receipts
    	CountDownLatch latch = new CountDownLatch(2);
    	List<SubscribeMessage> subscribes = new ArrayList<>();
        
        // Create client with re-connect and dirty sessions
        MqttConnectOptions options = new MqttConnectOptions();
        options.setAutomaticReconnect(true);
        options.setCleanSession(false);
        options.setKeepAliveInterval(1);
        options.setConnectionTimeout(1);
        
        // Topic
        String topic = "topical";
        
        // Create clients
        String proxyUrl = "tcp://" + PROXY_HOST + ":" + PROXY_PORT;
        MqttAsyncClient publisher = new MqttAsyncClient(proxyUrl, "test-publisher-id", new MemoryPersistence(), new TimerPingSender());
        MqttAsyncClient subscriber = new MqttAsyncClient(proxyUrl, "test-subscriber-id", new MemoryPersistence(), new TimerPingSender());
        ObservableMqttClient publisherObs = observableClient(publisher, options);
        ObservableMqttClient subscriberObs = observableClient(subscriber, options);
        
        // Connect, subscribe, publish test message
        publisherObs.connect().blockingAwait();
        subscriberObs.connect().blockingAwait();
        Assert.assertTrue(subscriberObs.isConnected());
        Assert.assertTrue(publisherObs.isConnected());
        subscriberObs.subscribe(topic, 1).subscribe(m -> {
        	System.out.println(m);
        	subscribes.add(m);
        	latch.countDown();
        });
        publisherObs.publish(topic, PublishMessage.create("Test Message 1".getBytes(), 1, false)).blockingGet();
        Assert.assertEquals(1, broker.getMessages().size());
        
        // Stop the proxy, wait, check status
        brokerProxy.disable();
        Thread.sleep(3000);
        Assert.assertFalse(publisherObs.isConnected());
        Assert.assertFalse(subscriberObs.isConnected());
     
        // Restart the broker, wait, check status
        brokerProxy.enable();
        Thread.sleep(10000);
        Assert.assertTrue(publisherObs.isConnected());
        Assert.assertTrue(subscriberObs.isConnected());
        
        // Publish and check for subscription
        publisherObs.publish(topic, PublishMessage.create("Test Message 2".getBytes(), 1, false)).blockingGet();
        Assert.assertEquals(2, broker.getMessages().size());
        
        // Check we got all the messages on the subscription
        latch.await(3, TimeUnit.SECONDS);	
        Assert.assertEquals(2, subscribes.size());
    }
    
    private ObservableMqttClient observableClient(final MqttAsyncClient asyncClient, 
    		final MqttConnectOptions options) {
        return new PahoObservableMqttClient.Builder(asyncClient)
                .setConnectOptions(options)
                .build();
    }
    
}

class PublisherListener extends AbstractInterceptHandler {
    
    private final List<InterceptPublishMessage> messages = new ArrayList<>();
    
    @Override
    public void onPublish(InterceptPublishMessage message) {
        System.out.println("Published message:" + message);
        this.messages.add(message);
    }

    @Override
    public String getID() {
        return "ID1234";
    }
    
    public List<InterceptPublishMessage> getMessages() {
        return this.messages;
    }
    
}

interface Endpoint {
    
    int port();
    
    String host();
    
    String toUrl();
}

interface Service {
    
    void start() throws Exception;
    
    void stop();
}

class MqttBroker implements Endpoint, Service {
    
    private final int port;
    private final String host;
    private final IConfig config;
    private final Server server;
    private final PublisherListener handler;
    
    public MqttBroker(final int port, 
            final String host) {
        
        // Host and port
        this.port = port;
        this.host = host;
        
        // Create the PublisherListener
        this.handler = new PublisherListener();
        
        // Create the server
        final Properties properties = new Properties();
        properties.setProperty("port", String.valueOf(port));
        properties.setProperty("host", host);
        properties.setProperty("allow_anonymous", "true");
        this.config = new MemoryConfig(properties);
        this.server = new Server();
        
    }

    @Override
    public int port() {
        return port;
    }

    @Override
    public String host() {
        return host;
    }

    @Override
    public String toUrl() {
        return String.format("tcp://%s:%s", host, port);
    }

    @Override
    public void start() throws Exception {
        server.startServer(this.config,
                Arrays.asList(this.handler));
    }

    @Override
    public void stop() {
        server.stopServer();
    }
    
    
    
    public List<InterceptPublishMessage> getMessages() {
        return this.handler.getMessages();
    }
    
}