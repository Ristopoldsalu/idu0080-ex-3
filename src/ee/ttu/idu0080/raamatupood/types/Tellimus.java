package ee.ttu.idu0080.raamatupood.types;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * Created by risto on 04.04.2018.
 */
public class Tellimus implements Serializable {

    private static final long serialVersionUID = 1L;
    private ArrayList<TellimuseRida> tellimuseRead;

    public Tellimus(ArrayList<TellimuseRida> tellimuseRead) {
        this.tellimuseRead = tellimuseRead;
    }

    public Tellimus() {
    }

    public ArrayList<TellimuseRida> getTellimuseRead() {
        return tellimuseRead;
    }

    public void setTellimuseRead(ArrayList<TellimuseRida> tellimuseRead) {
        this.tellimuseRead = tellimuseRead;
    }

    public void addTellimuseRida(TellimuseRida tellimuseRida) {
        this.tellimuseRead.add(tellimuseRida);
    }
}
