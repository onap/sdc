package org.openecomp.sdc.security;

import fj.data.Either;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

public class SecurityUtil {

    private static final Logger LOG = LoggerFactory.getLogger( SecurityUtil.class );
    private static final byte[] KEY = new byte[]{-64,5,-32 ,-117 ,-44,8,-39, 1, -9, 36,-46,-81, 62,-15,-63,-75};
    public static final SecurityUtil INSTANCE = new SecurityUtil();
    public static final String ALGORITHM = "AES" ;
    public static final String CHARSET = StandardCharsets.UTF_8.name();

    private static Key secKey = null ;

    private SecurityUtil(){ super(); }

    /**
     *
     * cmd commands >$PROGRAM_NAME decrypt "$ENCRYPTED_MSG"
     *              >$PROGRAM_NAME encrypt "message"
    **/
    public static void main(String[] args) throws Exception {
        if ( args!=null && args.length>1){
            fj.data.Either res = null;
            final String op = args[0].trim().toLowerCase();
            try{
                switch(op) {
                    case "decrypt":
                        res = INSTANCE.decrypt(Base64.getDecoder().decode(args[1]), true);
                        break;
                    case "encrypt":
                        res = INSTANCE.encrypt(args[1]);
                        break;
                    default:
                        LOG.warn("Unfamiliar command please use: \n>aes <encrypt/decrypt> 'message to encrypt/decrypt' ");
                }
            }catch(Exception e){
                LOG.warn("Exception while message encryption or decryption");
                throw e;
            }
            LOG.debug( "output: {}", res!=null && res.isLeft() ? res.left().value() : "ERROR" );
        }
    }


    static {
        try{
            secKey = generateKey( KEY, ALGORITHM );
        }
        catch(Exception e){
            LOG.warn("cannot generate key for " + ALGORITHM + " {}", e);
        }
    }



    public static Key generateKey(final byte[] key, String algorithm){
        return new SecretKeySpec(key, algorithm);
    }

    //obfuscates key prefix -> **********
    public String obfuscateKey(String sensitiveData){

        if (sensitiveData != null){
            int len = sensitiveData.length();
            StringBuilder builder = new StringBuilder(sensitiveData);
            for (int i=0; i<len/2; i++){
                builder.setCharAt(i, '*');
            }
            return builder.toString();
        }
        return sensitiveData;
    }

    /**
     *  @param strDataToEncrypt - plain string to encrypt
     *  Encrypt the Data
     * 		a. Declare / Initialize the Data. Here the data is of type String
     * 		b. Convert the Input Text to Bytes
     * 		c. Encrypt the bytes using doFinal method
     */
    public Either<String,String> encrypt(String strDataToEncrypt){
        if (strDataToEncrypt != null ){
            try {
                LOG.debug("Encrypt key -> {}", secKey);
                Cipher aesCipherForEncryption = Cipher.getInstance("AES");          // Must specify the mode explicitly as most JCE providers default to ECB mode!!
                aesCipherForEncryption.init(Cipher.ENCRYPT_MODE, secKey);
                byte[] byteDataToEncrypt = strDataToEncrypt.getBytes();
                byte[] byteCipherText = aesCipherForEncryption.doFinal(byteDataToEncrypt);
                String strCipherText = new String( java.util.Base64.getMimeEncoder().encode(byteCipherText), CHARSET );
                LOG.debug("Cipher Text generated using AES is {}", strCipherText);
                return Either.left(strCipherText);
            } catch( NoSuchAlgorithmException | UnsupportedEncodingException e){
                LOG.warn("cannot encrypt data unknown algorithm or missing encoding for " + secKey.getAlgorithm() + " {}", e);
            } catch( InvalidKeyException e){
                LOG.warn("invalid key recieved - > " + java.util.Base64.getDecoder().decode(secKey.getEncoded()) + " {}", e);
            } catch( IllegalBlockSizeException | BadPaddingException | NoSuchPaddingException  e){
                LOG.warn("bad algorithm definition (Illegal Block Size or padding), please review you algorithm block&padding {}", e);
            }
        }
        return Either.right("Cannot encrypt "+strDataToEncrypt);
    }

    /**
     * Decrypt the Data
     * @param byteCipherText - should be valid bae64 input in the length of 16bytes
     * @param isBase64Decoded - is data already base64 encoded&aligned to 16 bytes
     * 		a. Initialize a new instance of Cipher for Decryption (normally don't reuse the same object)
     * 		b. Decrypt the cipher bytes using doFinal method
     */
    public Either<String,String> decrypt(byte[] byteCipherText , boolean isBase64Decoded){
        if (byteCipherText != null){
            byte[] alignedCipherText = byteCipherText;
            try{
                if (isBase64Decoded)
                    alignedCipherText = Base64.getDecoder().decode(byteCipherText);
                LOG.debug("Decrypt key -> {}", secKey.getEncoded());
                Cipher aesCipherForDecryption = Cipher.getInstance("AES"); // Must specify the mode explicitly as most JCE providers default to ECB mode!!
                aesCipherForDecryption.init(Cipher.DECRYPT_MODE, secKey);
                byte[] byteDecryptedText = aesCipherForDecryption.doFinal(alignedCipherText);
                String strDecryptedText = new String(byteDecryptedText);
                String obfuscateKey = obfuscateKey( strDecryptedText );
                LOG.debug("Decrypted Text message is: {}" , obfuscateKey);
                return Either.left(strDecryptedText);
            } catch( NoSuchAlgorithmException e){
                LOG.warn("cannot encrypt data unknown algorithm or missing encoding for " + secKey.getAlgorithm() + " {}", e);
            } catch( InvalidKeyException e){
                LOG.warn("invalid key recieved - > " + java.util.Base64.getDecoder().decode(secKey.getEncoded()) + " {}", e);
            } catch( IllegalBlockSizeException | BadPaddingException | NoSuchPaddingException  e){
                LOG.warn( "bad algorithm definition (Illegal Block Size or padding), please review you algorithm block&padding {}", e);
            }
        }
        return Either.right("Decrypt FAILED");
    }

    public Either<String,String> decrypt(String byteCipherText){
        try {
            return decrypt(byteCipherText.getBytes(CHARSET),true);
        } catch( UnsupportedEncodingException e ){
            LOG.warn("Missing encoding for " + secKey.getAlgorithm() + " {}", e);
        }
        return Either.right("Decrypt FAILED");
    }
}
