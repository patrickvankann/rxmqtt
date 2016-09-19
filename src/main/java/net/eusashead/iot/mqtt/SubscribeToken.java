package net.eusashead.iot.mqtt;

@Deprecated
public interface SubscribeToken extends MqttToken {
    
    /**
     * Returns the granted QoS list from a suback 
     */
    public int[] getGrantedQos();
    
    
}
