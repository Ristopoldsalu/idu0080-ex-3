package ee.ttu.idu0080.raamatupood.client;

import ee.ttu.idu0080.raamatupood.server.ExceptionListenerImpl;
import org.apache.activemq.ActiveMQConnection;
import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.log4j.Logger;

import javax.jms.Connection;
import javax.jms.Destination;
import javax.jms.MessageConsumer;
import javax.jms.Session;


class MessageChanger {

    void createConsumerForListeningRepliesOnQueue(String queueName, String urlName, Logger log, MessageListener messageListener) {
        Connection connection = null;
        try {
            log.info("Connecting to URLSEND: " + urlName);
            log.info("Consuming queue : " + queueName);

            ActiveMQConnectionFactory connectionFactory = new ActiveMQConnectionFactory(
                    ActiveMQConnection.DEFAULT_USER, ActiveMQConnection.DEFAULT_PASSWORD, urlName);
            connection = connectionFactory.createConnection();
            connection.setExceptionListener(new ExceptionListenerImpl());
            connection.start();


            Session session = connection.createSession(false,
                    Session.AUTO_ACKNOWLEDGE);

            Destination destination = session.createQueue(queueName);
            MessageConsumer consumer = session.createConsumer(destination);
            consumer.setMessageListener(messageListener);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
