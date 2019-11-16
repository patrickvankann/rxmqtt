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
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;

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

@RunWith(JUnit4.class)
public class SocketProxyTest {
    
    // Broker host and port
    private static final int BROKER_PORT = 9095;
    private static final String BROKER_HOST = "localhost";
    
    // Proxy host and port
    private static final int PROXY_PORT = 9096;
    private static final String PROXY_HOST = "localhost";

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
    public void whenPahoConnectToBrokerViaProxyThenItWorks() throws Throwable {
        
        // Wait a second
        Thread.sleep(1000);
        
        // Create the client
        MqttConnectOptions options = new MqttConnectOptions();
        options.setAutomaticReconnect(false);
        options.setCleanSession(false);
        options.setKeepAliveInterval(1);
        options.setConnectionTimeout(1);
        
        String proxyUrl = "tcp://" + PROXY_HOST + ":" + PROXY_PORT;
        MqttAsyncClient asyncClient = new MqttAsyncClient(proxyUrl, "test-client-id", new MemoryPersistence());
        
        // Connect, publish, disconnect
        AsyncPahoUtils.connect(asyncClient);
        AsyncPahoUtils.publish(asyncClient, "topical", "Test message".getBytes());
        AsyncPahoUtils.disconnect(asyncClient);
        
        // Check we received a message
        Assert.assertEquals(1, broker.getMessages().size());
        
    }
    
    @Test
    public void whenProxyDisabledThenPahoDisconnect() throws Throwable {
        
        // Wait a second
        Thread.sleep(1000);
        
        // Create the client
        MqttConnectOptions options = new MqttConnectOptions();
        options.setAutomaticReconnect(false);
        options.setCleanSession(false);
        options.setKeepAliveInterval(1);
        options.setConnectionTimeout(1);
        
        String proxyUrl = "tcp://" + PROXY_HOST + ":" + PROXY_PORT;
        MqttAsyncClient asyncClient = new MqttAsyncClient(proxyUrl, "test-client-id", new MemoryPersistence());
        
        // Connect, publish
        AsyncPahoUtils.connect(asyncClient, options);
        AsyncPahoUtils.publish(asyncClient, "topical", "Test message".getBytes());
        AsyncPahoUtils.subscribe(asyncClient, "topical2", (t, m) -> {});
        Assert.assertEquals(1, broker.getMessages().size());
        
        // Disable proxy
        brokerProxy.disable();
        Thread.sleep(3000);
        
        // Check disconnected
        Assert.assertFalse(asyncClient.isConnected());
        
    }
    
    
    @Test
    public void whenAutoReconnectThenPahoReconnectsAfterProxyDisable() throws Throwable {
        
        // Create the client
        MqttConnectOptions options = new MqttConnectOptions();
        options.setAutomaticReconnect(true);
        options.setCleanSession(false);
        options.setKeepAliveInterval(1);
        options.setConnectionTimeout(1);
        
        String proxyUrl = "tcp://" + PROXY_HOST + ":" + PROXY_PORT;
        MqttAsyncClient asyncClient = new MqttAsyncClient(proxyUrl, "test-client-id", new MemoryPersistence());
        
        // Connect, publish
        AsyncPahoUtils.connect(asyncClient, options);
        AsyncPahoUtils.publish(asyncClient, "topical", "Test message".getBytes());
        AsyncPahoUtils.subscribe(asyncClient, "topical2", (t, m) -> {});
        Assert.assertEquals(1, broker.getMessages().size());
        
        // Disable proxy
        brokerProxy.disable();
        Thread.sleep(5000);
        
        // Check disconnected
        Assert.assertFalse(asyncClient.isConnected());
        
        // Re-enable
        brokerProxy.enable();
        Thread.sleep(10000);
        AsyncPahoUtils.publish(asyncClient, "topical", "Test message".getBytes());
        
        
        // Check connected
        Assert.assertTrue(asyncClient.isConnected());
        
    }
    
}

abstract class AbstractSocketServer {
    
    protected final InetSocketAddress listenAddress;
    protected final Thread thread;
    protected final AtomicBoolean started;
    
    protected final Logger logger = Logger
            .getLogger(getClass().getName());

    public AbstractSocketServer(final String address, final int port) {
        
        // Create a socket address
        this.listenAddress = new InetSocketAddress(address, port);
        
        // Thread to run server
        final Runnable server = () -> {
            try {
                startServer();
            } catch (IOException e) {
                logger.log(Level.SEVERE, e.getMessage(), e);
                throw new RuntimeException(e);
            }
        };
        this.thread = new Thread(server);
        this.started = new AtomicBoolean(false);
        
    }
    
    public void stop() throws InterruptedException {
        if (this.started.get()) {
            this.started.set(false);
            this.thread.join(1000);
        }
    }

    public void start() {
        if (!this.started.get()) {
            this.thread.start();
            this.started.set(true);
        }
    }

    protected abstract void startServer() throws IOException;
    
}


class ProxySocketServer extends AbstractSocketServer {
    
    // Buffer size
    private static final int BYTE_BUFFER_SIZE = 8192;
    
    // Map client channels to remote channels
    private final ConcurrentMap<SocketChannel, SocketChannel> proxy = new ConcurrentHashMap<SocketChannel, SocketChannel>();
    
    // Remote address that the proxy connects to
    private final InetSocketAddress remoteAddress;
    
    // Whether the proxy is forwarding to the remote server
    private final AtomicBoolean enabed;

    public ProxySocketServer(final String address, final int port,
            final String remoteAddress, final int remotePort) throws IOException {
        super(address, port);
        
        // Create a socket address
        this.remoteAddress = new InetSocketAddress(remoteAddress, remotePort);
        
        // Enable by default
        this.enabed = new AtomicBoolean(true);
        
    }
    
    @Override
    protected void startServer() throws IOException {
        
        logger.info("Proxy starting...");
        
        final Selector serverSelector = Selector.open();
        final Selector remoteSelector = Selector.open();
        
        final ServerSocketChannel serverSocket = ServerSocketChannel.open();
        serverSocket.bind(listenAddress);
        serverSocket.configureBlocking(false);
        serverSocket.register(serverSelector, SelectionKey.OP_ACCEPT);
        
        final ByteBuffer buffer = ByteBuffer.allocate(256); 
        
        while (this.started.get()) {
            
            // Register and read from clients
            if (serverSelector.select(1) > 0) {
                final Set<SelectionKey> selectedKeys = serverSelector.selectedKeys();
                final Iterator<SelectionKey> iter = selectedKeys.iterator();
                while (iter.hasNext()) {
                    
                    final SelectionKey key = iter.next();
     
                    if (key.isValid() && key.isAcceptable()) {
                        connectClientToRemote(serverSelector, remoteSelector, serverSocket);
                    }
     
                    if (key.isValid() && key.isReadable()) {
                        readFromClient(serverSelector, key, buffer);
                    }
                    iter.remove();
                }
            }
            
            // Select from remote
            if (remoteSelector.select(1) > 0) {
                final Iterator<SelectionKey> iter = remoteSelector.selectedKeys().iterator();
                while (iter.hasNext()) {
                    final SelectionKey key = iter.next();
                    
                    // Read from remote
                    if (key.isValid() && key.isReadable()) { 
                        
                        logger.info("Proxy reading from remote.");
                        
                        // Look up remote channel
                        final SocketChannel forwardTo = (SocketChannel) key.channel();
                        
                        // Read from remote to buffer
                        final ByteBuffer fromRemote = ByteBuffer.allocateDirect(BYTE_BUFFER_SIZE);
                        final int read = forwardTo.read(fromRemote);
                    
                        // Lookup the channel in the proxy map
                        for (final SocketChannel remoteChannel : proxy.keySet()) {
                            if (proxy.get(remoteChannel).equals(forwardTo)) {
                        
                                // Did we get data?
                                if (read > 0) {        
                                    // Return it to the client
                                    fromRemote.flip(); 
                                    if (this.enabed.get()) {
                                        logger.info("Enabled, writing data to client: " + StandardCharsets.UTF_8.decode(fromRemote.duplicate()));
                                        while(fromRemote.hasRemaining()) {
                                            remoteChannel.write(fromRemote);
                                        }
                                        //remoteChannel.write(fromRemote);
                                    } else {
                                        logger.info("Disabled, not writing data to client");
                                    }
                                    fromRemote.clear();
                                } else { 
                                    // Remote channel closed
                                    logger.info("Remote channel closing.");
                                    proxy.remove(remoteChannel);
                                    remoteChannel.close();
                                    while (remoteChannel.isOpen()) {
                                        logger.info("Waiting for remote channel to close.");
                                    }
                                    logger.info("Remote channel closed.");
                                }
                                break;
                            }
                        }
                    }
                    iter.remove();
                }
            }
            
        }
        
        logger.info("Proxy stopping...");
        serverSelector.close();
        serverSocket.close();
        while (serverSocket.isOpen()) {
            logger.info("Proxy waiting to stop...");
        }
        logger.info("Proxy stopped...");
        
    }

    protected void connectClientToRemote(Selector selector, Selector remoteSelector, 
            ServerSocketChannel serverSocket)
      throws IOException {
        
        logger.info("Proxy registering client.");
    
        final SocketChannel client = serverSocket.accept();
        client.configureBlocking(false);
        client.finishConnect();
        logger.info("Proxy registered client: " + client.socket().getRemoteSocketAddress().toString());    
        
        try {
            logger.info("Proxy opening remote address: " + this.remoteAddress);
            final SocketChannel remote = SocketChannel.open(this.remoteAddress);
            remote.configureBlocking(false);
            remote.finishConnect();
            remote.register(remoteSelector, SelectionKey.OP_READ);
            proxy.put(client, remote);    
            logger.info("Proxy opened remote address.");
        } catch (IOException e) {
            logger.log(Level.SEVERE, e.getMessage(), e);
            client.close();
            throw e;
        }
        
        client.register(selector, SelectionKey.OP_READ);
    }
    
    protected void readFromClient(final Selector selector, final SelectionKey key, 
            final ByteBuffer buffer)
              throws IOException {
        
        logger.info("Proxy reading from client.");
        
        // Get the client channel from key
        final SocketChannel client = (SocketChannel) key.channel();
        logger.info("Client channel blocking: " + client.isBlocking());
        
        // Get the remote channel
        SocketChannel remote = proxy.get(key.channel());
        if (remote != null ) {
            logger.info("Remote channel blocking: " + remote.isBlocking());
        } else {
            logger.info("Remote channel is null.");
        }
        
        // Allocate buffer for data from client
        ByteBuffer fromClient = ByteBuffer.allocateDirect(BYTE_BUFFER_SIZE);
        
        // Read from the client to the buffer
        int read = client.read(fromClient);                    
    
        // We have stopped reading
        if (read < 0) { 
            
            logger.info("Client sent no data, closing channels.");
            
            // Remove proxy and close socket channel
            if (remote != null ) {
                remote.close();
                proxy.remove(client);
            }
        }
        
        // We are reading
        if (read > 0) { 
            // Client sent data
            fromClient.flip();
            logger.info("Client sent data: " + StandardCharsets.UTF_8.decode(fromClient.duplicate()));
            // Check if the proxy is enabled
            if (this.enabed.get()) {
                // Send to remote if enabled
                logger.info("Enabled, sending data to remote");
                while(fromClient.hasRemaining()) {
                    remote.write(fromClient);
                }
                //remote.write(fromClient);
            } else {
                logger.info("Disabled, not sending data to remote");
            }
            fromClient.clear();
            client.register(selector, SelectionKey.OP_READ);                                        
        }
    }

    public void disable() {
        this.enabed.set(false);
    }

    public void enable() {
        this.enabed.set(true);
    }
    
}
