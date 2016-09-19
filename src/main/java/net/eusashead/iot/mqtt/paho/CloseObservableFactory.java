package net.eusashead.iot.mqtt.paho;

import java.util.concurrent.Callable;

import org.eclipse.paho.client.mqttv3.IMqttAsyncClient;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttException;

import rx.Observable;
import rx.Observer;

public class CloseObservableFactory extends BaseObservableFactory {
    
    static final class CloseActionListener extends ObserverMqttActionListener<Void> {

        public CloseActionListener(final Observer<? super Void> observer) {
            super(observer);
        }

        @Override
        public void onSuccess(IMqttToken asyncActionToken) {
            observer.onNext(null);
            observer.onCompleted();
        }
    }

    public CloseObservableFactory(final IMqttAsyncClient client) {
        super(client);
    }

    public Observable<Void> create() {

        return Observable.fromCallable(new Callable<Void>() {

            @Override
            public Void call() throws MqttException {
                client.close();
                return null;
            }
        });
    }
}
