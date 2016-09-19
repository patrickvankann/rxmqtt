package net.eusashead.iot.mqtt.paho;

import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.paho.client.mqttv3.IMqttAsyncClient;
import org.eclipse.paho.client.mqttv3.IMqttMessageListener;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttException;

import net.eusashead.iot.mqtt.MqttMessage;
import rx.AsyncEmitter.BackpressureMode;
import rx.Observable;
import rx.Observer;

public class SubscribeObservableFactory extends BaseObservableFactory {
    
    static final class SubscribeActionListener extends ObserverMqttActionListener<MqttMessage> {

        public SubscribeActionListener(final Observer<? super MqttMessage> observer) {
            super(observer);
        }

        @Override
        public void onSuccess(IMqttToken asyncActionToken) {
            // Does nothing
        }
    }

    private final static Logger LOGGER = Logger.getLogger(SubscribeActionListener.class.getName());

    public SubscribeObservableFactory(final IMqttAsyncClient client) {
        super(client);
    }

    public Observable<MqttMessage> create(final String[] topics,
            final int[] qos) {
        return Observable.fromAsync(observer -> {
            
            // Message listeners
            final SubscriberMqttMessageListener[] listeners = new SubscriberMqttMessageListener[topics.length];
            for (int i = 0; i < topics.length; i++) {
                listeners[i] = new SubscriberMqttMessageListener(observer);
            }
            
            try {
                client.subscribe(topics, qos, null, new SubscribeActionListener(observer), listeners);
            } catch (MqttException exception) {
                if (LOGGER.isLoggable(Level.SEVERE)) {
                    LOGGER.log(Level.SEVERE, exception.getMessage(), exception);
                }
                observer.onError(exception);
            }
        }, BackpressureMode.BUFFER);
    }

}

class SubscriberMqttMessageListener implements IMqttMessageListener {
    
    private final static Logger LOGGER = Logger.getLogger(SubscriberMqttMessageListener.class.getName());
    
    private final Observer<? super MqttMessage> observer;

    SubscriberMqttMessageListener(final Observer<? super MqttMessage> observer) {
        this.observer = Objects.requireNonNull(observer);
    }

    @Override
    public void messageArrived(final String topic, final org.eclipse.paho.client.mqttv3.MqttMessage message) {
        LOGGER.log(Level.FINE, String.format("Message %s received on topic %s", message.getId(), topic));
        observer.onNext(
                MqttMessage.create(message.getId(), message.getPayload(), message.getQos(), message.isRetained()));
    }
}
