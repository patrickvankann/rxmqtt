package net.eusashead.iot.mqtt;

import rx.Observable;

/**
 * RxJava MQTT API
 * 
 * @author pvankann@gmail.com
 *
 */
public interface ObservableMqttClient {

    /**
     * Get the MQTT broker URI
     * 
     * @return the MQTT broker URI string
     */
    String getBrokerUri();

    String getClientId();

    boolean isConnected();

    Observable<Void> close();

    Observable<Void> connect();

    Observable<Void> disconnect();

    Observable<PublishToken> publish(String topic, MqttMessage msg);

    Observable<MqttMessage> subscribe(String[] topics, int[] qos);

    Observable<MqttMessage> subscribe(String topic, int qos);

    Observable<Void> unsubscribe(String[] topics);

    Observable<Void> unsubscribe(String topic);

}
