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

import java.io.IOException;

import org.eclipse.paho.client.mqttv3.MqttAsyncClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import eu.rekawek.toxiproxy.Proxy;
import eu.rekawek.toxiproxy.ToxiproxyClient;
import net.eusashead.iot.mqtt.ObservableMqttClient;

/**
 * Test using Toxiproxy in front of RabbitMQ
 * Requires both docker containers to be running.
 * 
 * Used to simulate poor network connectivity.
 * 
 * Unfortunately, this test is proving to 
 * be quite unreliable so ignoring in the 
 * build.
 * 
 * @author pvk
 *
 */
@RunWith(JUnit4.class)
public class ToxiproxyConnectivityITCase {
	
    // Broker host and port
    private static final int BROKER_PORT = 1883;
    private static final String BROKER_HOST = "rabbitmq";
    
    // Proxy host and port
    private static final int PROXY_PORT = 1885;
    private static final String PROXY_HOST = "0.0.0.0";
    
    // Proxy
    private Proxy brokerProxy;
    
    @Before
    public void before() throws Exception {
        
        // Start the brokerProxy
        ToxiproxyClient proxyClient = new ToxiproxyClient(PROXY_HOST, 8474);
        proxyClient.getProxies().forEach(p -> {
			try {
				p.delete();
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		});
        this.brokerProxy = proxyClient.createProxy("broker", PROXY_HOST + ":" + PROXY_PORT, BROKER_HOST + ":" + BROKER_PORT);
        this.brokerProxy.enable();
        
    }
    
    @After
    public void after() throws Exception {
        
    	// Remove the proxy
        if (this.brokerProxy != null) {
            this.brokerProxy.delete();
        }
        
    }

    @Ignore
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
    
    
    private ObservableMqttClient observableClient(final MqttAsyncClient asyncClient, 
    		final MqttConnectOptions options) {
        return new PahoObservableMqttClient.Builder(asyncClient)
                .setConnectOptions(options)
                .build();
    }
    
}