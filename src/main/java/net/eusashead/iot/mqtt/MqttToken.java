package net.eusashead.iot.mqtt;

public interface MqttToken {
    
    String getClientId();

    /**
     * Returns the topic string(s) for the action being tracked by this
     * token. If the action has not been initiated or the action has not
     * topic associated with it such as connect then null will be returned.
     *
     * @return the topic string(s) for the subscribe being tracked by this token or null
     */
    String[] getTopics();

    /**
     * Returns the message ID of the message that is associated with the token.
     * A message id of zero will be returned for tokens associated with
     * connect, disconnect and ping operations as there can only ever
     * be one of these outstanding at a time. For other operations
     * the MQTT message id flowed over the network.
     */
    public int getMessageId();
    
    
    /**
     * Returns the session present flag from a connack 
     */
    public boolean getSessionPresent();
    

}
