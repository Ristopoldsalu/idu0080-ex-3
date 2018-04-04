package ee.ttu.idu0080.raamatupood.server;

import org.apache.log4j.Logger;

import javax.jms.JMSException;

/**
 * Created by risto on 04.04.2018.
 */
public class ExceptionListenerImpl implements javax.jms.ExceptionListener {
    private static final Logger log = Logger.getLogger(ExceptionListenerImpl.class);

    public synchronized void onException(JMSException ex) {
        log.error("JMS Exception occured. Shutting down client.");
        ex.printStackTrace();
    }
}

