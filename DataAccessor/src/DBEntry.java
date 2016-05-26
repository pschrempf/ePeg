import java.sql.Timestamp;

/**
 * Created by gf38 on 05/04/16.
 */
public class DBEntry {

    private String data;
    private String iv;
    private String secretkey;
    private Timestamp dateRecorded;

    private String decryptedData;

    public DBEntry(){

    }

    public DBEntry(String data, String iv, String secretkey, Timestamp dateRecorded) {
        this.data = data;
        this.iv = iv;
        this.secretkey = secretkey;
        this.dateRecorded = dateRecorded;
    }


    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    public String getIv() {
        return iv;
    }

    public void setIv(String iv) {
        this.iv = iv;
    }

    public String getSecretkey() {
        return secretkey;
    }

    public void setSecretkey(String secretkey) {
        this.secretkey = secretkey;
    }

    public Timestamp getDateRecorded() {
        return dateRecorded;
    }

    public void setDateRecorded(Timestamp dateRecorded) {
        this.dateRecorded = dateRecorded;
    }

    public String getDecryptedData() {
        return decryptedData;
    }

    public void setDecryptedData(String decryptedData) {
        this.decryptedData = decryptedData;
    }


}
