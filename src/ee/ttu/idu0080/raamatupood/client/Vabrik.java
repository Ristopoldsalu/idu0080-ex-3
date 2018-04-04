package ee.ttu.idu0080.raamatupood.client;

import ee.ttu.idu0080.raamatupood.server.EmbeddedBroker;
import ee.ttu.idu0080.raamatupood.types.Tellimus;
import org.apache.activemq.ActiveMQConnection;
import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.log4j.Logger;

import javax.jms.*;
import java.util.Date;

/**
 * JMS sõnumite tarbija. Ühendub broker-i urlile
 * 
 * @author Allar Tammik
 * @date 08.03.2010
 */
public class Vabrik extends MessageChanger{
	private static final Logger log = Logger.getLogger(Vabrik.class);

	private long sleepTime = 1000; // 1000ms
	private long timeToLive = 50000;

	public static void main(String[] args) {
		Vabrik vabrikTool = new Vabrik();
		vabrikTool.run();
	}

	private void run() {
		createConsumerForListeningRepliesOnQueue(EmbeddedBroker.SUBJECTSEND, EmbeddedBroker.URLSEND, log, new MessageListenerImpl());
	}


	class MessageListenerImpl extends MessageListener {
		private final Logger log = Logger.getLogger(MessageListenerImpl.class);

		@Override
		public void parseMessage(Message message) throws JMSException {
			if (message instanceof ObjectMessage) {
				ObjectMessage objectMessage = (ObjectMessage) message;
				if(objectMessage.getObject() instanceof Tellimus) {
					Tellimus tellimus = (Tellimus)objectMessage.getObject();
					parseResultSendAnswer(tellimus);
					log.info("Tellimus!!! Tellimusel on " + tellimus.getTellimuseRead().size() + " rida");
				}

				String msg = objectMessage.getObject().toString();
				log.info("Received: " + msg);

			} else {
				log.info("Received: " + message);
			}
		}

		private void parseResultSendAnswer(Tellimus tellimus) {
			try {
				Connection connection;
				log.info("Connecting to URLSEND: " + EmbeddedBroker.URL_RECEIVE);
				log.debug("Sleeping between publish " + sleepTime + " ms");
				if (timeToLive != 0) {
					log.debug("Messages time to live " + timeToLive + " ms");
				}

				// 1. Loome ühenduse
				ActiveMQConnectionFactory connectionFactory = new ActiveMQConnectionFactory(
						ActiveMQConnection.DEFAULT_USER, ActiveMQConnection.DEFAULT_PASSWORD, EmbeddedBroker.URL_RECEIVE);
				connection = connectionFactory.createConnection();
				connection.start();

				// 2. Loome sessiooni
				Session session = connection.createSession(false,
						Session.AUTO_ACKNOWLEDGE);
				Destination destination = session.createQueue(EmbeddedBroker.SUBJECTRECEIVE);

				// 3. Loome teadete saatja
				MessageProducer producer = session.createProducer(destination);
				producer.setTimeToLive(timeToLive);

				// 4. teadete saatmine
				sendAnswer(session, producer, tellimus);

			} catch (Exception e) {
				e.printStackTrace();
			}

		}

		private void sendAnswer(Session session, MessageProducer producer, Tellimus tellimus)
				throws Exception {
			if (tellimus.getTellimuseRead() != null) {
				// ootab 1 sekundi
				Thread.sleep(sleepTime);

				TextMessage message = session
						.createTextMessage(createMessageText(tellimus));
				log.debug("Sending message: " + message.getText());
				producer.send(message);
			}
		}

		private String createMessageText(Tellimus tellimus) {
			double totalPrice = calculateTotalPrice(tellimus);
			Integer amount = getTotalAmount(tellimus);
			return "Message: Telliti " + amount + " toodet hinnaga " + totalPrice + " at: " + (new Date()).toString();
		}
	}
}