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

import static java.lang.Math.toIntExact;

/**
 * JMS sõnumite tarbija. Ühendub broker-i urlile
 * 
 * @author Allar Tammik
 * @date 08.03.2010
 */
public class Vabrik {
	private static final Logger log = Logger.getLogger(Vabrik.class);
	private static final String SUBJECTSEND = "tellimus.edastamine"; // järjekorra nimi
	private static final String SUBJECTRECEIVE = "tellimus.vastus";
	private String user = ActiveMQConnection.DEFAULT_USER;
	private String password = ActiveMQConnection.DEFAULT_PASSWORD;
	private String urlSend = EmbeddedBroker.URL;
	private String urlReceive = EmbeddedBroker.URL_RECEIVE;

	long sleepTime = 1000; // 1000ms

	private int messageCount = 2;
	private long timeToLive = 1000000;

	public static void main(String[] args) {
		Vabrik vabrikTool = new Vabrik();
		vabrikTool.run();
	}

	public void run() {
		createConsumer();

	}

	private void createConsumer() {
		Connection connection = null;
		try {
			log.info("Connecting to URL: " + urlSend);
			log.info("Consuming queue : " + SUBJECTSEND);

			// 1. Loome ühenduse
			ActiveMQConnectionFactory connectionFactory = new ActiveMQConnectionFactory(
					user, password, urlSend);
			connection = connectionFactory.createConnection();

			// Kui ühendus kaob, lõpetatakse Consumeri töö veateatega.
			connection.setExceptionListener(new ExceptionListenerImpl());

			// Käivitame ühenduse
			connection.start();

			// 2. Loome sessiooni
			/*
			 * createSession võtab 2 argumenti: 1. kas saame kasutada
			 * transaktsioone 2. automaatne kinnitamine
			 */
			Session session = connection.createSession(false,
					Session.AUTO_ACKNOWLEDGE);

			// Loome teadete sihtkoha (järjekorra). Parameetriks järjekorra nimi
			Destination destination = session.createQueue(SUBJECTSEND);

			// 3. Teadete vastuvõtja
			MessageConsumer consumer = session.createConsumer(destination);

			// Kui teade vastu võetakse käivitatakse onMessage()
			consumer.setMessageListener(new MessageListenerImpl());

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	class MessageListenerImpl implements javax.jms.MessageListener {
		private final Logger log = Logger.getLogger(MessageListenerImpl.class);

		public void onMessage(Message message) {
			try {
				if (message instanceof TextMessage) {
					TextMessage txtMsg = (TextMessage) message;
					String msg = txtMsg.getText();
					log.info("Received: " + msg);
				} else if (message instanceof ObjectMessage) {
					ObjectMessage objectMessage = (ObjectMessage) message;
					if(objectMessage.getObject() instanceof Tellimus)
					{
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
			log.info("Connecting to URL: " + urlReceive);
			log.debug("Sleeping between publish " + sleepTime + " ms");
			if (timeToLive != 0) {
				log.debug("Messages time to live " + timeToLive + " ms");
			}

			// 1. Loome ühenduse
			ActiveMQConnectionFactory connectionFactory = new ActiveMQConnectionFactory(
					user, password, urlReceive);
			connection = connectionFactory.createConnection();
			// Käivitame yhenduse
			connection.start();

			// 2. Loome sessiooni
			/*
			 * createSession võtab 2 argumenti: 1. kas saame kasutada
			 * transaktsioone 2. automaatne kinnitamine
			 */
			Session session = connection.createSession(false,
					Session.AUTO_ACKNOWLEDGE);

			// Loome teadete sihtkoha (järjekorra). Parameetriks järjekorra nimi
			Destination destination = session.createQueue(SUBJECTRECEIVE);

			// 3. Loome teadete saatja
			MessageProducer producer = session.createProducer(destination);


			// producer.setDeliveryMode(DeliveryMode.PERSISTENT);
			producer.setTimeToLive(timeToLive);

			// 4. teadete saatmine
			sendAnswer(session, producer, tellimus);

		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	protected void sendAnswer(Session session, MessageProducer producer, Tellimus tellimus)
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