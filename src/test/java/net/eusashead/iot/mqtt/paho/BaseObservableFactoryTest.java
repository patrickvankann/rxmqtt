package net.eusashead.iot.mqtt.paho;

/*
 * #[license]
 * rxmqtt
 * %%
 * Copyright (C) 2013 - 2016 Eusa's Head
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * %[license]
 */

import org.eclipse.paho.client.mqttv3.IMqttAsyncClient;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.mockito.Mockito;

@RunWith(JUnit4.class)
public class BaseObservableFactoryTest {
    
    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Test
    public void whenAValidMqttAsyncClientIsSuppliedItIsConstructed() {
        final IMqttAsyncClient client = Mockito.mock(IMqttAsyncClient.class);
        final BaseObservableFactory factory = new BaseObservableFactory(client);
        Assert.assertEquals(client, factory.getMqttAsyncClient());
    }
    
    @Test(expected=NullPointerException.class)
    public void whenANullMqttAsyncClientIsSuppliedAnExceptionIsThrown() {
        new BaseObservableFactory(null);
    }
    
}
