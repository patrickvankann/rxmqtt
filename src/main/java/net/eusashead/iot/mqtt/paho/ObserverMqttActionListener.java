package net.eusashead.iot.mqtt.paho;

import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttToken;

import rx.Observer;

public abstract class ObserverMqttActionListener<T> implements IMqttActionListener {

    protected final static Logger LOGGER = Logger.getLogger(ObserverMqttActionListener.class.getName());

    protected final Observer<? super T> observer;
    
    public ObserverMqttActionListener(final Observer<? super T> observer) {
        this.observer = Objects.requireNonNull(observer);
    }

    @Override
    public void onFailure(final IMqttToken asyncActionToken, final Throwable exception) {
        if (LOGGER.isLoggable(Level.SEVERE)) {
            LOGGER.log(Level.SEVERE, exception.getMessage(), exception);
        }
        observer.onError(exception);
    }

   

}
