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

import java.security.SecureRandom;
import java.util.Arrays;
import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import org.apache.commons.codec.binary.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CipherUtil {

    public static final int GCM_TAG_LENGTH = 16;
    public static final int GCM_IV_LENGTH = 12;
    private static final String ALGORITHM = "AES";
    private static final String ALGORITHM_DETAILS = ALGORITHM + "/GCM/NoPadding";
    private static final String CIPHER_PROVIDER = "SunJCE";
    private static final byte[] EMPTY_BYTE_ARRAY = new byte[0];
    private static final String ALGORITHM_NAME = "SHA1PRNG";
    private static Logger log = LoggerFactory.getLogger(CipherUtil.class.getName());

    private CipherUtil() {
    }

    /**
     * Encrypt the text using the secret key in key.properties file
     *
     * @param value string to encrypt
     * @return The encrypted string
     * @throws CipherUtilException In case of issue with the encryption
     */
    public static String encryptPKC(String value, String base64key) throws CipherUtilException {
        Cipher cipher;
        byte[] iv = new byte[GCM_IV_LENGTH];
        byte[] finalByte;
        try {
            cipher = Cipher.getInstance(ALGORITHM_DETAILS, CIPHER_PROVIDER);
            SecureRandom secureRandom = SecureRandom.getInstance(ALGORITHM_NAME);
            secureRandom.nextBytes(iv);
            GCMParameterSpec spec = new GCMParameterSpec(GCM_TAG_LENGTH * java.lang.Byte.SIZE, iv);
            cipher.init(Cipher.ENCRYPT_MODE, getSecretKeySpec(base64key), spec);
            finalByte = cipher.doFinal(value.getBytes());
        } catch (Exception ex) {
            log.error("encrypt failed", ex);
            throw new CipherUtilException(ex);
        }
        return Base64.encodeBase64String(addAll(iv, finalByte));
    }

    /**
     * Decrypts the text using the secret key in key.properties file.
     *
     * @param message The encrypted string that must be decrypted using the ONAP Portal Encryption Key
     * @return The String decrypted
     * @throws CipherUtilException if any decryption step fails
     */
    public static String decryptPKC(String message, String base64key) throws CipherUtilException {
        byte[] encryptedMessage = Base64.decodeBase64(message);
        Cipher cipher;
        byte[] decrypted;
        try {
            cipher = Cipher.getInstance(ALGORITHM_DETAILS, CIPHER_PROVIDER);
            byte[] initVector = Arrays.copyOfRange(encryptedMessage, 0, GCM_IV_LENGTH);
            GCMParameterSpec spec = new GCMParameterSpec(GCM_TAG_LENGTH * java.lang.Byte.SIZE, initVector);
            byte[] realData = subarray(encryptedMessage, GCM_IV_LENGTH, encryptedMessage.length);
            cipher.init(Cipher.DECRYPT_MODE, getSecretKeySpec(base64key), spec);
            decrypted = cipher.doFinal(realData);
        } catch (Exception ex) {
            log.error("decrypt failed", ex);
            throw new CipherUtilException(ex);
        }
        return new String(decrypted);
    }

    private static SecretKeySpec getSecretKeySpec(String keyString) {
        byte[] key = Base64.decodeBase64(keyString);
        return new SecretKeySpec(key, ALGORITHM);
    }

    private static byte[] clone(byte[] array) {
        return array == null ? null : array.clone();
    }

    private static byte[] addAll(byte[] array1, byte[] array2) {
        if (array1 == null) {
            return clone(array2);
        } else if (array2 == null) {
            return clone(array1);
        } else {
            byte[] joinedArray = new byte[array1.length + array2.length];
            System.arraycopy(array1, 0, joinedArray, 0, array1.length);
            System.arraycopy(array2, 0, joinedArray, array1.length, array2.length);
            return joinedArray;
        }
    }

    private static byte[] subarray(byte[] array, int startIndexInclusive, int endIndexExclusive) {
        if (array == null) {
            return new byte[0];
        } else {
            if (startIndexInclusive < 0) {
                startIndexInclusive = 0;
            }
            if (endIndexExclusive > array.length) {
                endIndexExclusive = array.length;
            }
            int newSize = endIndexExclusive - startIndexInclusive;
            if (newSize <= 0) {
                return EMPTY_BYTE_ARRAY;
            } else {
                byte[] subarray = new byte[newSize];
                System.arraycopy(array, startIndexInclusive, subarray, 0, newSize);
                return subarray;
            }
        }
    }
}
