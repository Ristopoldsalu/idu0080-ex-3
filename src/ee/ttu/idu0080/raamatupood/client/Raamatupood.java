package ee.ttu.idu0080.raamatupood.client;

import ee.ttu.idu0080.raamatupood.server.EmbeddedBroker;
import ee.ttu.idu0080.raamatupood.server.ExceptionListenerImpl;
import ee.ttu.idu0080.raamatupood.types.Car;
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
public class Raamatupood {
	private static final Logger log = Logger.getLogger(Raamatupood.class);
	private static final String SUBJECTSEND = "tellimus.edastamine"; // järjekorra nimi
	private static final String SUBJECTRECEIVE = "tellimus.vastus";


	private String user = ActiveMQConnection.DEFAULT_USER;// brokeri jaoks vaja
	private String password = ActiveMQConnection.DEFAULT_PASSWORD;

	private long sleepTime = 1000; // 1000ms

	private int messageCount = 2;
	private long timeToLive = 1000000;
	private String urlSend = EmbeddedBroker.URL;
	private String urlReceive = EmbeddedBroker.URL_RECEIVE;

	public static void main(String[] args) {
		Raamatupood raamatupoodTool = new Raamatupood();
		raamatupoodTool.run();
	}

	public void run() {
		createProducerAndSend();

		createConsumer();
	}



	private void createProducerAndSend() {
		try {
			Connection connection;
			log.info("Connecting to URL: " + urlSend);
			log.debug("Sleeping between publish " + sleepTime + " ms");
			if (timeToLive != 0) {
				log.debug("Messages time to live " + timeToLive + " ms");
			}

			// 1. Loome ühenduse
			ActiveMQConnectionFactory connectionFactory = new ActiveMQConnectionFactory(
					user, password, urlSend);
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
			Destination destination = session.createQueue(SUBJECTSEND);

			// 3. Loome teadete saatja
			MessageProducer producer = session.createProducer(destination);


			// producer.setDeliveryMode(DeliveryMode.PERSISTENT);
			producer.setTimeToLive(timeToLive);

			// 4. teadete saatmine
			sendLoop(session, producer);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void createConsumer() {
		Connection connection = null;
		try {
			log.info("Connecting to URL: " + urlReceive);
			log.info("Consuming queue : " + SUBJECTRECEIVE);

			// 1. Loome ühenduse
			ActiveMQConnectionFactory connectionFactory = new ActiveMQConnectionFactory(
					user, password, urlReceive);
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
			Destination destination = session.createQueue(SUBJECTRECEIVE);

			// 3. Teadete vastuvõtja
			MessageConsumer consumer = session.createConsumer(destination);

			// Kui teade vastu võetakse käivitatakse onMessage()
			consumer.setMessageListener(new MessageListenerImpl());

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

			// ootab 10 sekundit
			Thread.sleep(sleepTime);
		}
	}

	private String createMessageText(int index) {
		return "Message: " + index + " sent at: " + (new Date()).toString();
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
						Car auto=(Car)objectMessage.getObject();
						log.info("Auto!!! Autol on "+auto.getDoors()+" uks/ust");
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
}


