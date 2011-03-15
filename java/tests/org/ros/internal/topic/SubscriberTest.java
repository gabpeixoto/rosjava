/*
 * Copyright (C) 2011 Google Inc.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package org.ros.internal.topic;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.Test;
import org.ros.internal.node.server.SlaveIdentifier;
import org.ros.internal.transport.ConnectionHeader;
import org.ros.internal.transport.ConnectionHeaderFields;
import org.ros.internal.transport.tcp.TcpRosConnection;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

/**
 * @author damonkohler@google.com (Damon Kohler)
 */
public class SubscriberTest {

  @Test
  public void testHandshake() throws IOException, URISyntaxException {
    Executor executor = Executors.newCachedThreadPool();
    Socket socket = mock(Socket.class);
    Map<String, String> header = new TopicDefinition("/foo",
        MessageDefinition.createFromMessage(new org.ros.message.std.String())).toHeader();
    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
    ConnectionHeader.writeHeader(header, outputStream);
    byte[] buffer = outputStream.toByteArray();
    when(socket.getInputStream()).thenReturn(new ByteArrayInputStream(buffer));
    outputStream = new ByteArrayOutputStream();
    when(socket.getOutputStream()).thenReturn(outputStream);

    SlaveIdentifier slaveIdentifier = new SlaveIdentifier("/caller", new URI("http://fake:1234"));
    Subscriber<org.ros.message.std.String> subscriber = Subscriber.create(
        slaveIdentifier,
        new TopicDefinition("/foo", MessageDefinition
            .createFromMessage(new org.ros.message.std.String())),
        org.ros.message.std.String.class, executor);
    TcpRosConnection.subscriberHandshake(socket, subscriber.getHeader());
    buffer = outputStream.toByteArray();
    Map<String, String> result = ConnectionHeader.readHeader(new ByteArrayInputStream(buffer));
    assertEquals(result.get(ConnectionHeaderFields.TYPE), header.get(ConnectionHeaderFields.TYPE));
    assertEquals(result.get(ConnectionHeaderFields.MD5_CHECKSUM),
        header.get(ConnectionHeaderFields.MD5_CHECKSUM));
  }
  
}
