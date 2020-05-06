/*-
 * ============LICENSE_START=======================================================
 * SDC
 * ================================================================================
 * Copyright (C) 2019 AT&T Intellectual Property. All rights reserved.
 * ================================================================================
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * ============LICENSE_END=========================================================
 */

package org.openecomp.sdc.securityutil;

import static java.nio.charset.StandardCharsets.UTF_8;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fj.data.Either;
import java.io.UnsupportedEncodingException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Base64;
import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.ShortBufferException;
import javax.crypto.spec.GCMParameterSpec;

public class SecurityUtil {

    private static final Logger LOG = LoggerFactory.getLogger(SecurityUtil.class);

    public static final SecurityUtil INSTANCE = new SecurityUtil();
    public static final String ALGORITHM = "AES";
    public static final String CHARSET = UTF_8.name();

    public static final int GCM_TAG_LENGTH = 16;
    public static final int GCM_IV_LENGTH = 12;

    private static final Key secKey = generateKey(ALGORITHM);

    private SecurityUtil() {
    }

    public static SecretKey generateKey(String algorithm) {
        try {
            KeyGenerator kgen = KeyGenerator.getInstance(algorithm);
            kgen.init(128);
            return kgen.generateKey();
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e.toString());
        }
    }

    //obfuscates key prefix -> **********
    public String obfuscateKey(String sensitiveData) {

        if (sensitiveData != null) {
            int len = sensitiveData.length();
            StringBuilder builder = new StringBuilder(sensitiveData);
            for (int i = 0; i < len / 2; i++) {
                builder.setCharAt(i, '*');
            }
            return builder.toString();
        }
        return sensitiveData;
    }

    //@formatter:off

    /**
     * @param strDataToEncrypt - plain string to encrypt Encrypt the Data a. Declare / Initialize
     *                         the Data. Here the data is of type String b. Convert the Input Text
     *                         to Bytes c. Encrypt the bytes using doFinal method
     */
    //@formatter:on
    public static Either<String, String> encrypt(String strDataToEncrypt) {
        try {
            byte[] ciphertext = null;
            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            byte[] initVector = new byte[GCM_IV_LENGTH];
            new SecureRandom().nextBytes(initVector);
            GCMParameterSpec spec =
                new GCMParameterSpec(GCM_TAG_LENGTH * java.lang.Byte.SIZE, initVector);
            cipher.init(Cipher.ENCRYPT_MODE, secKey, spec);
            byte[] encoded = strDataToEncrypt.getBytes(java.nio.charset.StandardCharsets.UTF_8);
            ciphertext = Arrays
                .copyOf(initVector, initVector.length + cipher.getOutputSize(encoded.length));
            // Perform encryption
            cipher.doFinal(encoded, 0, encoded.length, ciphertext, initVector.length);
            String strCipherText = new String(Base64.getMimeEncoder().encode(ciphertext), CHARSET);
            return Either.left(strCipherText);
        } catch (NoSuchAlgorithmException | UnsupportedEncodingException | InvalidAlgorithmParameterException e) {
            LOG.warn(
                "cannot encrypt data unknown algorithm or missing encoding for {}",
                secKey.getAlgorithm());
        } catch (InvalidKeyException e) {
            LOG.warn(
                "invalid key recieved - > {} | {}",
                new String(Base64.getDecoder().decode(secKey.getEncoded())),
                e.getMessage());
        } catch (IllegalBlockSizeException | BadPaddingException | NoSuchPaddingException e) {
            LOG.warn(
                "bad algorithm definition (Illegal Block Size or padding), please review you algorithm block&padding",
                e.getMessage());
        } catch (ShortBufferException e) {
            LOG.warn(
                "the given output buffer is too small to hold the result",
                e.getMessage());
        }
        return Either.right("Cannot encrypt " + strDataToEncrypt);
    }

    //@formatter:off

    /**
     * Decrypt the Data
     *
     * @param byteCipherText  - should be valid bae64 input in the length of 16bytes
     * @param isBase64Decoded - is data already base64 encoded&aligned to 16 bytes a. Initialize a
     *                        new instance of Cipher for Decryption (normally don't reuse the same
     *                        object) b. Decrypt the cipher bytes using doFinal method
     */
    //@formatter:on
    public static Either<String, String> decrypt(byte[] byteCipherText, boolean isBase64Decoded) {
        try {
            if (isBase64Decoded) {
                byteCipherText = Base64.getDecoder().decode(byteCipherText);
            }
            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            byte[] initVector = Arrays.copyOfRange(byteCipherText, 0, GCM_IV_LENGTH);
            GCMParameterSpec spec =
                new GCMParameterSpec(GCM_TAG_LENGTH * java.lang.Byte.SIZE, initVector);
            cipher.init(Cipher.DECRYPT_MODE, secKey, spec);
            byte[] plaintext =
                cipher
                    .doFinal(byteCipherText, GCM_IV_LENGTH, byteCipherText.length - GCM_IV_LENGTH);
            String strDecryptedText = new String(plaintext);
            return Either.left(strDecryptedText);
        } catch (NoSuchAlgorithmException | InvalidAlgorithmParameterException e) {
            /* None of these exceptions should be possible if precond is met. */
            LOG.warn(
                "cannot decrypt data, unknown algorithm or missing encoding for {}",
                secKey.getAlgorithm());
        } catch (InvalidKeyException e) {
            LOG.warn(
                "invalid key recieved - > {} | {}",
                new String(Base64.getDecoder().decode(secKey.getEncoded())),
                e.getMessage());
        } catch (IllegalBlockSizeException | BadPaddingException | NoSuchPaddingException e) {
            /* these indicate corrupt or malicious ciphertext */
            LOG.warn(
                "bad algorithm definition (Illegal Block Size or padding), please review you algorithm block&padding",
                e.getMessage());
        }
        return Either.right("Decrypt FAILED");
    }

    public Either<String, String> decrypt(String byteCipherText) {
        try {
            return decrypt(byteCipherText.getBytes(CHARSET), true);
        } catch (UnsupportedEncodingException e) {
            LOG.warn(
                "Missing encoding for {} | {} ",
                secKey.getAlgorithm(),
                e.getMessage());
        }
        return Either.right("Decrypt FAILED");
    }
}
