package net.eusashead.iot.mqtt.paho;

import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import net.eusashead.iot.mqtt.paho.ConnectObservableFactory.ConnectActionListener;
import rx.Observer;

@RunWith(JUnit4.class)
public class ObserverMqttActionListenerTest {
    
    @Test(expected=NullPointerException.class)
    public void whenTheConstructorIsCalledWithANullObserverANullPointerExceptionOccurs() {
        new ObserverMqttActionListener<Void>(null) {

            @Override
            public void onSuccess(IMqttToken asyncActionToken) {
                
            }
            
        };
    }
    
    @Test
    public void whenOnFailureIsCalledTheObserverIsNotifiedAndTheErrorIsLogged() throws Exception {
        // Given
        final Handler handler = Mockito.mock(Handler.class);
        final ArgumentCaptor<LogRecord> logRecord = ArgumentCaptor.forClass(LogRecord.class);
        Logger.getLogger(ObserverMqttActionListener.class.getName()).addHandler(handler);
        @SuppressWarnings("unchecked")
        Observer<Void> observer = Mockito.mock(Observer.class);
        final ConnectActionListener listener = new ConnectObservableFactory.ConnectActionListener(observer);
        final IMqttToken asyncActionToken = Mockito.mock(IMqttToken.class);
        final Throwable exception = Mockito.mock(Throwable.class);
        String expectedErrorMessage = "Error message";
        Mockito.when(exception.getMessage()).thenReturn(expectedErrorMessage);
        
        // When
        listener.onFailure(asyncActionToken, exception);
        
        // Then
        Mockito.verify(observer).onError(exception);
        Mockito.verify(handler).publish(logRecord.capture());
        Assert.assertEquals(Level.SEVERE, logRecord.getValue().getLevel());
        Assert.assertEquals(expectedErrorMessage, logRecord.getValue().getMessage());
        Assert.assertEquals(exception, logRecord.getValue().getThrown());
    }

}
