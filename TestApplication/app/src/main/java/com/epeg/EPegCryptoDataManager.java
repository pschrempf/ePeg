package com.epeg;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.os.Environment;
import android.text.format.DateFormat;
import android.util.Base64;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.security.AlgorithmParameters;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.InvalidParameterSpecException;
import java.security.spec.RSAPublicKeySpec;
import java.util.ArrayList;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;

/**
 * Created by gregory on 16/03/16.
 *
 * The DAO that communicates between the DB helper and the rest of the application
 */
public class EPegCryptoDataManager {

    //Tag for logging stuff
    public static final String TAG = EPegCryptoDataManager.class.getSimpleName();

    //TODO replace these with actual identifiers
    public static final String TMP_DEVICE_ID = "FIRST ASUS TABLET";
    public static final String TMP_EXP_CONDUCTOR = "Silvia Paracchini";

    //Taken from https://docs.oracle.com/javase/8/docs/api/javax/crypto/Cipher.html
    private static final String ENCRYPTION_SYMM_PATTERN = "AES/CBC/PKCS5Padding";
    private static final String ENCRYPTION_SYMM_ALGORITHM = "AES";

    //Taken from http://stackoverflow.com/questions/5789685/rsa-encryption-with-given-public-key-in-java
    private static final String ENCRYPTION_ASYMM_ALGORITHM = "RSA";
    private static final String ENCRYPTION_ASYMM_PATTERN = "RSA/ECB/NoPadding";
    private static final BigInteger ENCRYPTION_PUBLIC_KEY_MODULUS = new BigInteger("B786CADA9C0B75571BDE3954B6F8FFED7D14308F4F479ED5D53872715D2EC1811122309D587D1403CD71930F4E66FB045195F3BF844129D700464B96F55A29FA31BB068EFD20AE376B92909FB1CDF5E476214E40D952108BBF7D2C7F2FE183D1760A19B125E9C2EAA774585B36A1696755D4CE00365C7E50FEFA79B989F6355A73FCD43FEE598926BF6EC1C6A512A69B081BB6EC38B66A07FF164F37E86914E4157422F80FDA4DED6C7DD595D7B57E9880023012296C745E9E86DEA3B1BA18AD359D509E2F6FDA8B53EAD6062F31E450D3B48F5E55A44C9F6F9C05CD6B8507442C2ACAF9186CC50C3F2ED470489E5BEAA5CBA4EE5660ADB6E691EEE45331C97B", 16);
    private static final BigInteger ENCRYPTION_PUBLIC_KEY_EXP = new BigInteger("010001", 16);

    //With help from http://stackoverflow.com/questions/992019/java-256-bit-aes-password-based-encryption
    private static final String CHAR_ENCODING = "UTF-8";
    private static final int AES_KEY_SIZE = 256;

    //For the .csv backup file
    private static final String FILE_DATA_BACKUP = "backup.csv";
    private static final String FILE_PUBLIC_DATA = "public.csv";

    //Fields for the database access
    private EPegSQLiteHelper dbHelper;
    private SQLiteDatabase database;

    //Create ciphers for encryption
    private Cipher symmetricCipher, asymmetricCipher;


    public EPegCryptoDataManager(Context context){
        dbHelper = new EPegSQLiteHelper(context);

        try {
            //Fetch singletons
            symmetricCipher = Cipher.getInstance(ENCRYPTION_SYMM_PATTERN);
            asymmetricCipher = Cipher.getInstance(ENCRYPTION_ASYMM_PATTERN);

            //Initialise the public key for the encryption
            KeyFactory keyFactory = KeyFactory.getInstance(ENCRYPTION_ASYMM_ALGORITHM);
            RSAPublicKeySpec keySpec = new RSAPublicKeySpec(ENCRYPTION_PUBLIC_KEY_MODULUS, ENCRYPTION_PUBLIC_KEY_EXP);
            RSAPublicKey publicKey = (RSAPublicKey) keyFactory.generatePublic(keySpec);

            //Initialise the asymmetric cipher with the key
            asymmetricCipher.init(Cipher.ENCRYPT_MODE, publicKey);

        } catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException | InvalidKeySpecException e) {
            //This bit cannot occur, because all specified constants are required to be implemented after the Java 7 standard
            e.printStackTrace();
        }
    }

    /**
     * This method takes in a UTF-8 encoded string and performs PGP style encryption on it.
     * @param data UTF-8 encoded String containing message to be encrypted
     * @return {@link EPegCryptoDataManager.EncryptionResult} containing the ciphertext,
     * the encrypted symmetric key and the encrypted initialisation vector for the AES CBC algorithm
     */
    public EncryptionResult encryptString(String data){

        //Initialise return object
        EncryptionResult res = new EncryptionResult();
        try {

            //Generate new 256 bit AES key
            KeyGenerator keyGenerator = KeyGenerator.getInstance(ENCRYPTION_SYMM_ALGORITHM);
            keyGenerator.init(AES_KEY_SIZE, new SecureRandom());
            SecretKey secretKey = keyGenerator.generateKey();

            //Initialise the symmetric cipher with the newly generated key
            symmetricCipher.init(Cipher.ENCRYPT_MODE, secretKey);

            AlgorithmParameters params = symmetricCipher.getParameters();

            //Set all fields int the return object
            res.setSecretKey(
                    asymmetricCipher.doFinal(
                            secretKey.getEncoded()));
            res.setInitVector(
                    asymmetricCipher.doFinal(
                            params.getParameterSpec(IvParameterSpec.class).getIV()));
            res.setCypherText(
                    symmetricCipher.doFinal(data.getBytes(CHAR_ENCODING)));

        } catch (NoSuchAlgorithmException | InvalidKeyException | InvalidParameterSpecException |
                BadPaddingException | IllegalBlockSizeException | UnsupportedEncodingException e) {
            e.printStackTrace();
            //This bit should never be executed as all constants specified above comply to the Java 7 standard
        }

        return res;
    }

    /**
     * Opens the database for writing
     */
    public void open(){
        database = dbHelper.getWritableDatabase();

        /*Log.i(TAG, "Upgrading db");
        dbHelper.onUpgrade(database,1,1);*/
    }

    /**
     * Closes the database connection
     */
    public void close(){
        dbHelper.close();
    }

    public void writeStudy(JSONObject study, String deviceID, String conductor){
        try {

            ArrayList<String> tempArray = new ArrayList<>();

            JSONObject participant = study.getJSONObject(Study.JSON_PARTICIPANT_TAG);
            JSONArray trials = study.getJSONArray(Study.JSON_TRIALS_ARRAY_TAG);

            tempArray.add(participant.getString(Participant.JSON_PARTICIPANT_ID_TAG));
            tempArray.add(participant.getString(Participant.JSON_DOM_HAND_TAG));

            String date = (DateFormat.format("dd-MM-yyyy hh:mm:ss", new java.util.Date()).toString());

            tempArray.add(date);

            for (int i = 0; i < trials.length(); i++) {
                JSONObject trial = trials.getJSONObject(i);

                tempArray.add(trial.getJSONObject(Trial.JSON_MEASUREMENTS_TAG).getString(Trial.JSON_SUM_TIME_TAG));
            }

            writeBackUp(FILE_PUBLIC_DATA, tempArray.toArray( new String[0]));


            String studyData = study.toString();

            ContentValues values = new ContentValues();

            EncryptionResult firstStage = encryptString(studyData);

            values.put(EPegSQLiteHelper.FIELD_DATA, firstStage.getCypherText());
            values.put(EPegSQLiteHelper.FIELD_KEY, firstStage.getSecretKey());
            values.put(EPegSQLiteHelper.FIELD_IV, firstStage.getInitVector());

            values.put(EPegSQLiteHelper.FIELD_EXP_CONDUCTOR, conductor);
            values.put(EPegSQLiteHelper.FIELD_DEVICE_ID, deviceID);

            String[] backupData = {firstStage.getCypherText(), firstStage.getSecretKey(), firstStage.getInitVector()};

            writeBackUp(FILE_DATA_BACKUP, backupData);

            long id = database.insert(EPegSQLiteHelper.TABLE_NAME, null, values);

            Log.d(TAG, "New study recorded, with ID " + id + " on " + deviceID + " by " + conductor);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private boolean writeBackUp(String fileName, String[] data){
        if ( !isExternalStorageWritable() )
            return false;

        File backup = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM), fileName);
        boolean firstTime = !backup.exists();

        FileOutputStream fileOutputStream = null;

        try {
            fileOutputStream = new FileOutputStream( backup, true );

            StringBuilder outputBuilder = new StringBuilder();

            // Add array headings
            if(firstTime){
                outputBuilder.append("Participant ID,Dominant Hand,Date");
                for (int i = 0; i < data.length-3; i++) {
                    outputBuilder.append(",Total time " + i);
                }
                outputBuilder.append("\n");
            }

            for (int i = 0; i < data.length; i++) {
                outputBuilder.append(data[i]);
                outputBuilder.append((i == data.length - 1) ? "\n" : "," );
            }

            fileOutputStream.write(outputBuilder.toString().getBytes());

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if(fileOutputStream != null)
            {
                try {
                    fileOutputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        return true;
    }

    private boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            return true;
        }
        return false;
    }

    /**
     * Encompasses the results of a single encryption process to then be further passed on and inserted to the database
     * Takes in the raw output {@link Cipher}s generate as a byte array and encodes them as Base64 Strings for storage.
     */
    private class EncryptionResult {

        private String secretKey;
        private String initVector;
        private String cypherText;

        public String getSecretKey() {
            return secretKey;
        }

        public void setSecretKey(byte[] secretKey) {
            this.secretKey = Base64.encodeToString(secretKey, Base64.NO_WRAP);
        }

        public String getInitVector() {
            return initVector;
        }

        public void setInitVector(byte[] initVector) {
            this.initVector = Base64.encodeToString(initVector, Base64.NO_WRAP);
        }

        public String getCypherText() {
            return cypherText;
        }

        public void setCypherText(byte[] cypherText) {
            this.cypherText = Base64.encodeToString(cypherText, Base64.NO_WRAP);
        }
    }
}
