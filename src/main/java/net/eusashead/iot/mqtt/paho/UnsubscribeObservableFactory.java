package net.eusashead.iot.mqtt.paho;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.paho.client.mqttv3.IMqttAsyncClient;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttException;

import rx.AsyncEmitter.BackpressureMode;
import rx.Observable;
import rx.Observer;

public class UnsubscribeObservableFactory extends BaseObservableFactory {
    
    private final static Logger LOGGER = Logger.getLogger(UnsubscribeObservableFactory.class.getName());

    static final class UnsubscribeActionListener extends ObserverMqttActionListener<Void> {

        public UnsubscribeActionListener(final Observer<? super Void> observer) {
            super(observer);
        }

        @Override
        public void onSuccess(IMqttToken asyncActionToken) {
            observer.onNext(null);
            observer.onCompleted();
        }
    }
    
    public UnsubscribeObservableFactory(final IMqttAsyncClient client) {
        super(client);
    }
    
    public Observable<Void> create(final String[] topics) {
        
        return Observable.fromAsync(observer -> {
            try {
                client.unsubscribe(topics, null, new UnsubscribeActionListener(observer));
            } catch (MqttException exception) {
                if (LOGGER.isLoggable(Level.SEVERE)) {
                    LOGGER.log(Level.SEVERE, exception.getMessage(), exception);
                }
                observer.onError(exception);
            }
        }, BackpressureMode.BUFFER);
    }
    
}
