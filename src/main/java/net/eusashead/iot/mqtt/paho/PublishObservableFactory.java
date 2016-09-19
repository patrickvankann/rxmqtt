package net.eusashead.iot.mqtt.paho;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.paho.client.mqttv3.IMqttAsyncClient;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttException;

import net.eusashead.iot.mqtt.MqttMessage;
import net.eusashead.iot.mqtt.PublishToken;
import rx.AsyncEmitter.BackpressureMode;
import rx.Observable;
import rx.Observer;

public class PublishObservableFactory extends BaseObservableFactory {

    private final static Logger LOGGER = Logger.getLogger(PublishObservableFactory.class.getName());

    static final class PublishActionListener extends ObserverMqttActionListener<PublishToken> {

        public PublishActionListener(final Observer<? super PublishToken> observer) {
            super(observer);
        }

        @Override
        public void onSuccess(IMqttToken t) {

            PublishToken b = new PublishToken() {

                @Override
                public String getClientId() {
                    return t.getClient().getClientId();
                }

                @Override
                public String[] getTopics() {
                    return t.getTopics();
                }

                @Override
                public int getMessageId() {
                    return t.getMessageId();
                }

                @Override
                public boolean getSessionPresent() {
                    return t.getSessionPresent();
                }
                
            };
            observer.onNext(b);
            observer.onCompleted();
        }
    }

    public PublishObservableFactory(final IMqttAsyncClient client) {
        super(client);
    }

    public Observable<PublishToken> create(final String topic,
            final MqttMessage msg) {
        return Observable.fromAsync(observer -> {
            try {
                client.publish(topic, msg.getPayload(), msg.getQos(),
                        msg.isRetained(), null, new PublishActionListener(observer));
            } catch (MqttException exception) {
                if (LOGGER.isLoggable(Level.SEVERE)) {
                    LOGGER.log(Level.SEVERE, exception.getMessage(), exception);
                }
                observer.onError(exception);
            }
        }, BackpressureMode.BUFFER);
    }

}
