package ee.ttu.idu0080.raamatupood.client;

import ee.ttu.idu0080.raamatupood.server.EmbeddedBroker;
import ee.ttu.idu0080.raamatupood.server.ExceptionListenerImpl;
import ee.ttu.idu0080.raamatupood.types.Tellimus;
import ee.ttu.idu0080.raamatupood.types.TellimuseRida;
import org.apache.activemq.ActiveMQConnection;
import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.log4j.Logger;

import javax.jms.*;
import java.util.Date;

import static ee.ttu.idu0080.raamatupood.server.EmbeddedBroker.URLSEND;
import static ee.ttu.idu0080.raamatupood.server.EmbeddedBroker.URL_RECEIVE;
import static java.lang.Math.toIntExact;
import static org.apache.activemq.ActiveMQConnection.DEFAULT_PASSWORD;
import static org.apache.activemq.ActiveMQConnection.DEFAULT_USER;

/**
 * JMS sõnumite tarbija. Ühendub broker-i urlile
 * 
 * @author Allar Tammik
 * @date 08.03.2010
 */
public class Vabrik {
	private static final Logger log = Logger.getLogger(Vabrik.class);

	private long sleepTime = 1000; // 1000ms
	private long timeToLive = 1000000;

	public static void main(String[] args) {
		Vabrik vabrikTool = new Vabrik();
		vabrikTool.run();
	}

	private void run() {
		createConsumer(EmbeddedBroker.SUBJECTSEND);

	}

	private void createConsumer(String queueName) {
		Connection connection = null;
		try {
			log.info("Connecting to URLSEND: " + EmbeddedBroker.URLSEND);
			log.info("Consuming queue : " + queueName);

			// 1. Loome ühenduse
			ActiveMQConnectionFactory connectionFactory = new ActiveMQConnectionFactory(
					ActiveMQConnection.DEFAULT_USER, ActiveMQConnection.DEFAULT_PASSWORD, EmbeddedBroker.URLSEND);
			connection = connectionFactory.createConnection();
			connection.setExceptionListener(new ExceptionListenerImpl());
			connection.start();

			// 2. Loome sessiooni
			Session session = connection.createSession(false,
					Session.AUTO_ACKNOWLEDGE);
			Destination destination = session.createQueue(queueName);

			// 3. Teadete vastuvõtja
			MessageConsumer consumer = session.createConsumer(destination);
			consumer.setMessageListener(new MessageListenerImpl());

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	class MessageListenerImpl implements javax.jms.MessageListener {
		private final Logger log = Logger.getLogger(MessageListenerImpl.class);

		public void onMessage(Message message) {
			try {
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

			} catch (JMSException e) {
				log.warn("Caught: " + e);
				e.printStackTrace();
			}
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

	private int getTotalAmount(Tellimus tellimus) {
		return tellimus.getTellimuseRead().stream()
			.mapToInt(tellimuseRida -> toIntExact(tellimuseRida.getKogus()))
			.sum();
	}

	private double calculateTotalPrice(Tellimus tellimus) {
		return tellimus.getTellimuseRead().stream()
		   .mapToDouble(this::getPriceWithAmount)
		   .sum();
	}

	private double getPriceWithAmount(TellimuseRida tellimuseRida) {
		return tellimuseRida.getToode().getHind().longValue()*tellimuseRida.getKogus();
	}


}