package eu.europa.ec.fisheries.uvms.docker.validation.common;

import java.util.UUID;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.DeliveryMode;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageListener;
import javax.jms.MessageProducer;
import javax.jms.Queue;
import javax.jms.QueueBrowser;
import javax.jms.Session;
import javax.jms.TextMessage;

import org.apache.activemq.ActiveMQConnectionFactory;

public final class MessageHelper {

	private static final ConnectionFactory connectionFactory = new ActiveMQConnectionFactory("tcp://localhost:61616");

	public static Message getMessageResponse(String queueName, final String msg) throws Exception {
		ResponseQueueMessageListener listener = new ResponseQueueMessageListener();
		String responseQueueName = queueName + "Response" + UUID.randomUUID().toString().replaceAll("-", "");
		setupResponseConsumer(responseQueueName, listener);

		Connection connection = connectionFactory.createConnection();
		connection.setClientID(UUID.randomUUID().toString());

		final Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
		final Queue queue = session.createQueue(queueName);

		final MessageProducer messageProducer = session.createProducer(queue);
		messageProducer.setDeliveryMode(DeliveryMode.PERSISTENT);
		messageProducer.setTimeToLive(1000000000);
		TextMessage createTextMessage = session.createTextMessage(msg);
		final Queue responseQueue = session
				.createQueue(responseQueueName);
		createTextMessage.setJMSReplyTo(responseQueue);
		messageProducer.send(createTextMessage);
		session.close();

		while (listener.message == null)
			;

		connection.close();

		return listener.getMessage();
	}
	
	public static void sendMessage(String queueName, final String msg) throws Exception {
		String responseQueueName = queueName + "Response" + UUID.randomUUID().toString().replaceAll("-", "");

		Connection connection = connectionFactory.createConnection();
		connection.setClientID(UUID.randomUUID().toString());

		final Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
		final Queue queue = session.createQueue(queueName);

		final MessageProducer messageProducer = session.createProducer(queue);
		messageProducer.setDeliveryMode(DeliveryMode.PERSISTENT);
		messageProducer.setTimeToLive(1000000000);
		final Queue responseQueue = session
				.createQueue(responseQueueName);
		TextMessage createTextMessage = session.createTextMessage(msg);
		createTextMessage.setJMSReplyTo(responseQueue);
		messageProducer.send(createTextMessage);
		session.close();
	}
	

	private static void setupResponseConsumer(String queueName, ResponseQueueMessageListener listener) throws Exception {
		Connection consumerConnection = connectionFactory.createConnection();
		consumerConnection.setClientID(UUID.randomUUID().toString());
		final Session session = consumerConnection.createSession(false, Session.AUTO_ACKNOWLEDGE);
		final Queue responseQueue = session.createQueue(queueName);
		MessageConsumer consumer = session.createConsumer(responseQueue);

		consumer.setMessageListener(listener);
		consumerConnection.start();

	}

	private static class ResponseQueueMessageListener implements MessageListener {
		private volatile Message message = null;

		@Override
		public void onMessage(Message message) {
			this.message = message;
		}

		public Message getMessage() {
			return message;
		}
	}

	/**
	 * Check queue has elements.
	 *
	 * @param connection
	 * @param queueName
	 * @return
	 * @throws Exception
	 */

	public static boolean checkQueueHasElements(String queueName) throws Exception {
		Connection connection = connectionFactory.createConnection();
		final Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
		final Queue queue = session.createQueue(queueName);
		final QueueBrowser browser = session.createBrowser(queue);
		while (browser.getEnumeration().hasMoreElements()) {
			session.close();
			connection.close();
			return true;
		}
		session.close();
		connection.close();
		return false;
	}

}
