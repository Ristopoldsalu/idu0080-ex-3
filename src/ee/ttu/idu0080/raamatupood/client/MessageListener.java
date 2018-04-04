package ee.ttu.idu0080.raamatupood.client;

import ee.ttu.idu0080.raamatupood.types.Tellimus;
import ee.ttu.idu0080.raamatupood.types.TellimuseRida;
import org.apache.log4j.Logger;

import javax.jms.JMSException;
import javax.jms.Message;

import static java.lang.Math.toIntExact;

public class MessageListener implements javax.jms.MessageListener {
    private final Logger log = Logger.getLogger(Raamatupood.MessageListenerImpl.class);

    public void onMessage(Message message) {
        try {
            parseMessage(message);

        } catch (JMSException e) {
            log.warn("Caught: " + e);
            e.printStackTrace();
        }
    }

    public void parseMessage(Message message) throws JMSException {}

    int getTotalAmount(Tellimus tellimus) {
        return tellimus.getTellimuseRead().stream()
                .mapToInt(tellimuseRida -> toIntExact(tellimuseRida.getKogus()))
                .sum();
    }

    double calculateTotalPrice(Tellimus tellimus) {
        return tellimus.getTellimuseRead().stream()
                .mapToDouble(this::getPriceWithAmount)
                .sum();
    }

    private double getPriceWithAmount(TellimuseRida tellimuseRida) {
        return tellimuseRida.getToode().getHind().longValue()*tellimuseRida.getKogus();
    }
}
