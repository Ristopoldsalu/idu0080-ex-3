package ee.ttu.idu0080.raamatupood.types;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * Created by risto on 04.04.2018.
 */
public class Toode implements Serializable {

    Integer kood;
    String nimetus;
    BigDecimal hind;

    public Toode(Integer kood, String nimetus, BigDecimal hind) {
        this.kood = kood;
        this.nimetus = nimetus;
        this.hind = hind;
    }

    public Integer getKood() {
        return kood;
    }

    public void setKood(Integer kood) {
        this.kood = kood;
    }

    public String getNimetus() {
        return nimetus;
    }

    public void setNimetus(String nimetus) {
        this.nimetus = nimetus;
    }

    public BigDecimal getHind() {
        return hind;
    }

    public void setHind(BigDecimal hind) {
        this.hind = hind;
    }
}
