import javax.crypto.*;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.RSAPrivateKeySpec;
import java.util.Base64;
import java.util.List;

/**
 * Created by gf38 on 05/04/16.
 */
public class Decryptor {

    // http://stackoverflow.com/questions/3243018/how-to-load-rsa-private-key-from-file
    private static final String PRIVATE_KEY_PATH = "ePeg_pkcs8";

    //Taken from https://docs.oracle.com/javase/8/docs/api/javax/crypto/Cipher.html
    private static final String ENCRYPTION_SYMM_PATTERN = "AES/CBC/PKCS5Padding";
    private static final String ENCRYPTION_SYMM_ALGORITHM = "AES";

    //Taken from http://stackoverflow.com/questions/5789685/rsa-encryption-with-given-public-key-in-java
    private static final String ENCRYPTION_ASYMM_ALGORITHM = "RSA";
    private static final String ENCRYPTION_ASYMM_PATTERN = "RSA/ECB/NoPadding";

    //With help from http://stackoverflow.com/questions/992019/java-256-bit-aes-password-based-encryption
    private static final String CHAR_ENCODING = "UTF-8";
    private static final int AES_KEY_SIZE = 256;

    //Create ciphers for encryption
    private static Cipher symmetricCipher, asymmetricCipher;

    public static void main(String[] args) throws NoSuchPaddingException, NoSuchAlgorithmException, IOException {

        boolean fromCsv = args.length > 0;

        //Fetch singletons
        symmetricCipher = Cipher.getInstance(ENCRYPTION_SYMM_PATTERN);
        asymmetricCipher = Cipher.getInstance(ENCRYPTION_ASYMM_PATTERN);


        FileWriter fileWriter = null;
        try {
            fileWriter = new FileWriter(args.length >= 2 ? args[1] : "decrypted.json", true);

            IDataAccessor dbAccessor = fromCsv ? new CSVAccessor(args[0]) : new DBAccessor();

            List<DBEntry> entries = dbAccessor.readDatabase();

            for (DBEntry entry : entries) {
                fileWriter.write(decrypt(entry));
            }
        } catch (IOException e) {
            System.out.println(e.getMessage());
        } catch (Exception e){
            System.out.println("Unhandled exception occurred: " + e.getMessage());
        } finally {
            if (fileWriter != null){
                fileWriter.flush();
                fileWriter.close();
            }
        }
    }

    private static String decrypt(DBEntry entry) {

        File privateKeyFile = new File(PRIVATE_KEY_PATH);

        byte[] encodedKey = new byte[(int) privateKeyFile.length()];

        try {
            new FileInputStream(privateKeyFile).read(encodedKey);

            PKCS8EncodedKeySpec pkcs8EncodedKeySpec = new PKCS8EncodedKeySpec(encodedKey);
            KeyFactory kf = KeyFactory.getInstance(ENCRYPTION_ASYMM_ALGORITHM);

            PrivateKey privateKey = kf.generatePrivate(pkcs8EncodedKeySpec);

            asymmetricCipher.init(Cipher.DECRYPT_MODE, privateKey);

            Base64.Decoder decoder = Base64.getDecoder();

            byte[] keyRes = decoder.decode(entry.getSecretkey());
            byte[] ivRes = decoder.decode(entry.getIv());

            byte[] aesKey = asymmetricCipher.doFinal(keyRes);

            byte[] iv = asymmetricCipher.doFinal(ivRes);

            SecretKey secretKey = new SecretKeySpec(aesKey, aesKey.length - (AES_KEY_SIZE / 8), (AES_KEY_SIZE / 8), ENCRYPTION_SYMM_ALGORITHM);

            symmetricCipher.init(Cipher.DECRYPT_MODE, secretKey, new IvParameterSpec(iv, iv.length - 16, 16));

            String data = new String(symmetricCipher.doFinal(decoder.decode(entry.getData().getBytes(CHAR_ENCODING))));

            return data;

        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (InvalidKeySpecException e) {
            e.printStackTrace();
        } catch (InvalidKeyException e) {
            e.printStackTrace();
        } catch (BadPaddingException e) {
            e.printStackTrace();
        } catch (IllegalBlockSizeException e) {
            e.printStackTrace();
        } catch (InvalidAlgorithmParameterException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "";
    }


}
