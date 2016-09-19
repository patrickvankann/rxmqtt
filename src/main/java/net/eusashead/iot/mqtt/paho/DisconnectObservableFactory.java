package net.eusashead.iot.mqtt.paho;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.paho.client.mqttv3.IMqttAsyncClient;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttException;

import rx.AsyncEmitter.BackpressureMode;
import rx.Observable;
import rx.Observer;

public class DisconnectObservableFactory extends BaseObservableFactory {
    
    private final static Logger LOGGER = Logger.getLogger(DisconnectObservableFactory.class.getName());

    static final class DisconnectActionListener extends ObserverMqttActionListener<Void> {

        public DisconnectActionListener(final Observer<? super Void> observer) {
            super(observer);
        }

        @Override
        public void onSuccess(IMqttToken asyncActionToken) {
            observer.onNext(null);
            observer.onCompleted();
        }
    }

    public DisconnectObservableFactory(final IMqttAsyncClient client) {
        super(client);
    }

    public Observable<Void> create() {
        return Observable.fromAsync(observer -> {
            try {
                client.disconnect(null, new DisconnectActionListener(observer));
            } catch (MqttException exception) {
                if (LOGGER.isLoggable(Level.SEVERE)) {
                    LOGGER.log(Level.SEVERE, exception.getMessage(), exception);
                }
                observer.onError(exception);
            }
        }, BackpressureMode.BUFFER);
    }

}
