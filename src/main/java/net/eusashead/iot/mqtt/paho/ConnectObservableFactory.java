package net.eusashead.iot.mqtt.paho;

import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.paho.client.mqttv3.IMqttAsyncClient;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;

import rx.AsyncEmitter.BackpressureMode;
import rx.Observable;
import rx.Observer;

public class ConnectObservableFactory extends BaseObservableFactory {
    
    static final class ConnectActionListener extends ObserverMqttActionListener<Void> {
        
        public ConnectActionListener(final Observer<? super Void> observer) {
            super(observer);
        }
        
        @Override
        public void onSuccess(IMqttToken asyncActionToken) {
            observer.onNext(null);
            observer.onCompleted();
        }
    }

    private final MqttConnectOptions options;
    
    private final static Logger LOGGER = Logger.getLogger(ConnectObservableFactory.class.getName());

    public ConnectObservableFactory(final IMqttAsyncClient client, final MqttConnectOptions options) {
        super(client);
        this.options = Objects.requireNonNull(options);
    }

    public Observable<Void> create() {
        return Observable.fromAsync(observer -> {
            try {
                client.connect(options, new ConnectActionListener(observer));
            } catch (MqttException exception) {
                if (LOGGER.isLoggable(Level.SEVERE)) {
                    LOGGER.log(Level.SEVERE, exception.getMessage(), exception);
                }
                observer.onError(exception);
            }
        }, BackpressureMode.BUFFER);
    }

    public MqttConnectOptions getOptions() {
        return options;
    }
}