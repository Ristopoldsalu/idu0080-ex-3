package ee.ttu.idu0080.raamatupood.types;

/**
 * Created by risto on 04.04.2018.
 */
public class TellimuseRida {

    Toode toode;
    long kogus;

    public TellimuseRida(Toode toode, long kogus) {
        this.toode = toode;
        this.kogus = kogus;
    }

    public Toode getToode() {
        return toode;
    }

    public void setToode(Toode toode) {
        this.toode = toode;
    }

    public long getKogus() {
        return kogus;
    }

    public void setKogus(long kogus) {
        this.kogus = kogus;
    }
}
