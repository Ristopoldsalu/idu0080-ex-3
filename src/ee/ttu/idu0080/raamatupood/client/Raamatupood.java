package ee.ttu.idu0080.raamatupood.client;

import ee.ttu.idu0080.raamatupood.server.EmbeddedBroker;
import ee.ttu.idu0080.raamatupood.types.Tellimus;
import ee.ttu.idu0080.raamatupood.types.TellimuseRida;
import ee.ttu.idu0080.raamatupood.types.Toode;
import org.apache.activemq.ActiveMQConnection;
import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.log4j.Logger;

import javax.jms.*;
import java.math.BigDecimal;
import java.util.Date;

/**
 * JMS sõnumite tootja. Ühendub brokeri url-ile
 * 
 * @author Allar Tammik
 * @date 08.03.2010
 */
public class Raamatupood extends MessageChanger {
	private static final Logger log = Logger.getLogger(Raamatupood.class);

	private long sleepTime = 1000; // 1000ms
	private int messageCount = 2;
	private long timeToLive = 1000000;

	public static void main(String[] args) {
		Raamatupood raamatupoodTool = new Raamatupood();
		raamatupoodTool.run();
	}

	public void run() {
		createConsumerForListeningRepliesOnQueue(EmbeddedBroker.SUBJECTRECEIVE, EmbeddedBroker.URL_RECEIVE, log, new MessageListenerImpl());
		createProducerAndSendTellimusOnQueue(EmbeddedBroker.SUBJECTSEND);
	}

	private void createProducerAndSendTellimusOnQueue(String queueName) {
		try {
			Connection connection;
			log.info("Connecting to URLSEND: " + EmbeddedBroker.URLSEND);
			log.debug("Sleeping between publish " + sleepTime + " ms");
			if (timeToLive != 0) {
				log.debug("Messages time to live " + timeToLive + " ms");
			}

			ActiveMQConnectionFactory connectionFactory = new ActiveMQConnectionFactory(
					ActiveMQConnection.DEFAULT_USER, ActiveMQConnection.DEFAULT_PASSWORD, EmbeddedBroker.URLSEND);
			connection = connectionFactory.createConnection();
			connection.start();

			Session session = connection.createSession(false,
					Session.AUTO_ACKNOWLEDGE);
			Destination destination = session.createQueue(queueName);
			MessageProducer producer = session.createProducer(destination);
			producer.setTimeToLive(timeToLive);

			sendLoop(session, producer);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}


	private void sendLoop(Session session, MessageProducer producer)
			throws Exception {

		for (int i = 0; i < messageCount || messageCount == 0; i++) {
			Tellimus tellimus = new Tellimus();
			tellimus.addTellimuseRida(new TellimuseRida(new Toode(4, "raamat", BigDecimal.valueOf(3L)), 5L));

			ObjectMessage objectMessage = session.createObjectMessage();
			objectMessage.setObject(tellimus); // peab olema Serializable
			producer.send(objectMessage);

			log.debug("Sending message: " + createMessageText(i));
			// ootab 1 sekundit
			Thread.sleep(sleepTime);
		}
	}

	private String createMessageText(int index) {
		return "Message: " + index + " sent at: " + (new Date()).toString();
	}

	class MessageListenerImpl extends MessageListener {
		private final Logger log = Logger.getLogger(MessageListenerImpl.class);

		@Override
		public void parseMessage(Message message) throws JMSException {
			if (message instanceof TextMessage) {
				TextMessage txtMsg = (TextMessage) message;
				String msg = txtMsg.getText();
				log.info("Received: " + msg);
			} else {
				log.info("Received: " + message);
			}
		}
	}
}


