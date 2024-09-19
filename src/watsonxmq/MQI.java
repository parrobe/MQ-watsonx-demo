/*
Copyright (c) Rob Parker 2024

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at:

  http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.

 Contributors:
   Rob Parker - Initial Contribution
*/
package watsonxmq;

import javax.jms.Connection;
import javax.jms.Destination;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.Session;

import com.ibm.msg.client.jms.JmsConnectionFactory;
import com.ibm.msg.client.jms.JmsFactoryFactory;
import com.ibm.msg.client.wmq.WMQConstants;

import javax.jms.MessageProducer;
import javax.jms.TextMessage;

/**
 * This class handles the connection to the IBM MQ queue manager as well as
 * provides methods to put or get messages.
 * It is based off the JMSConsumer and JMSProducer sample applications provided
 * with IBM MQ.
 */
public class MQI {

	private Connection connection = null;
	private Session session = null;

	private String qmgrname;
	private String host;
	private int port;
	private String channel;

	public MQI(String qmgrname, String host, int port, String channel) {
		this.qmgrname = qmgrname;
		this.host = host;
		this.port = port;
		this.channel = channel;
	}

	/**
	 * Creates a connection and session to an IBM MQ queue manager.
	 * 
	 * @throws Exception
	 */
	public void createConnection() throws Exception {
		try {
			JmsFactoryFactory ff = JmsFactoryFactory.getInstance(WMQConstants.WMQ_PROVIDER);
			JmsConnectionFactory cf = ff.createConnectionFactory();

			// Set the properties
			cf.setStringProperty(WMQConstants.WMQ_HOST_NAME, host);
			cf.setIntProperty(WMQConstants.WMQ_PORT, port);
			cf.setStringProperty(WMQConstants.WMQ_CHANNEL, channel);

			cf.setIntProperty(WMQConstants.WMQ_CONNECTION_MODE, WMQConstants.WMQ_CM_CLIENT);

			cf.setStringProperty(WMQConstants.WMQ_QUEUE_MANAGER, qmgrname);

			// Create JMS connection
			connection = cf.createConnection();
			session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
		} catch (Exception e) {
			e.printStackTrace();
			throw e;
		}
	}

	/**
	 * Creates a consumer, connects to the queue manager via the previously
	 * establish connection and recieves a message from the given queue.
	 * 
	 * @param queue The queue to consume a message from.
	 * @return The received message or "";
	 * @throws Exception
	 */
	public String receiveMessage(String queue) throws Exception {
		if (session == null) {
			throw new Exception("MQI not connected.");
		}
		Destination destination = session.createQueue(queue);
		MessageConsumer consumer = session.createConsumer(destination);

		// Start the connection
		connection.start();

		Message message;
		message = consumer.receive(10);
		if (message == null) {
			return "";
		}

		return message.getBody(String.class);
	}
}
