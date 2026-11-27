/*-
 * ============LICENSE_START=======================================================
 * SDC
 * ================================================================================
 * Copyright (C) 2026 Deutsche Telekom AG. All rights reserved.
 * ================================================================================
 * Ported from org.onap.portalsdk.core.onboarding.util.CipherUtil of the ONAP Portal SDK
 * (epsdk-fw 3.4.0), Copyright (C) 2017 AT&T Intellectual Property, Apache License 2.0.
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
package org.openecomp.sdc.common.util;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Properties;
import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import org.apache.commons.codec.binary.Base64;

/**
 * Encrypts the user identity the frontend hands to the browser and the backend reads back: the {@code USER_ID} cookie and
 * headers, and the legacy ONAP Portal {@code UserId} cookie.
 *
 * <p>The format is the Portal SDK's, byte for byte, because frontend and backend are upgraded separately and browsers keep
 * their cookies across an upgrade: {@code Base64(iv || AES/CBC/PKCS5Padding(value))} with a 16-byte random IV for the
 * {@code Pkc} methods, and {@code Base64(AES/ECB/PKCS5Padding(value))} for {@link #decrypt}. Base64 is commons-codec's, whose
 * lenient decoder accepts what the SDK accepted. Text is UTF-8 throughout, where the SDK mostly used the platform default; the
 * SDC images run with a UTF-8 default, so the bytes are the same there. It is not interchangeable with
 * {@code org.openecomp.sdc.securityutil.CipherUtil}, which is AES/GCM.</p>
 *
 * <p>Neither format is authenticated: CBC and ECB detect a modified ciphertext only when the padding breaks.</p>
 */
public final class UserIdCipher {

    private static final String KEY_FILE = "key.properties";
    private static final String KEY_PROPERTY = "cipher.enc.key";
    private static final String ALGORITHM = "AES";
    private static final String PKC_TRANSFORMATION = "AES/CBC/PKCS5PADDING";
    private static final String PKC_PROVIDER = "SunJCE";
    private static final int IV_LENGTH = 16;
    private static final SecureRandom RANDOM = new SecureRandom();

    private UserIdCipher() {
    }

    /**
     * Encrypts with the key {@code cipher.enc.key} from {@code key.properties} on the classpath.
     */
    public static String encryptPkc(String value) throws UserIdCipherException {
        return encryptPkc(value, KeyFile.key());
    }

    public static String encryptPkc(String value, String base64Key) throws UserIdCipherException {
        if (value == null) {
            throw new UserIdCipherException("Cannot encrypt a null value");
        }
        byte[] iv = new byte[IV_LENGTH];
        RANDOM.nextBytes(iv);
        try {
            Cipher cipher = Cipher.getInstance(PKC_TRANSFORMATION, PKC_PROVIDER);
            cipher.init(Cipher.ENCRYPT_MODE, secretKey(base64Key), new IvParameterSpec(iv));
            byte[] body = cipher.doFinal(value.getBytes(StandardCharsets.UTF_8));
            byte[] ivAndBody = Arrays.copyOf(iv, IV_LENGTH + body.length);
            System.arraycopy(body, 0, ivAndBody, IV_LENGTH, body.length);
            return Base64.encodeBase64String(ivAndBody);
        } catch (GeneralSecurityException | IllegalArgumentException e) {
            throw new UserIdCipherException("Encryption failed", e);
        }
    }

    /**
     * Decrypts with the key {@code cipher.enc.key} from {@code key.properties} on the classpath.
     */
    public static String decryptPkc(String encrypted) throws UserIdCipherException {
        return decryptPkc(encrypted, KeyFile.key());
    }

    public static String decryptPkc(String encrypted, String base64Key) throws UserIdCipherException {
        byte[] ivAndBody = Base64.decodeBase64(encrypted);
        // The SDK decrypted a bare IV to an empty string; no value it encrypted is shorter than IV plus one block.
        if (ivAndBody == null || ivAndBody.length < 2 * IV_LENGTH) {
            throw new UserIdCipherException("Encrypted value is shorter than an IV and one block");
        }
        try {
            Cipher cipher = Cipher.getInstance(PKC_TRANSFORMATION, PKC_PROVIDER);
            cipher.init(Cipher.DECRYPT_MODE, secretKey(base64Key), new IvParameterSpec(ivAndBody, 0, IV_LENGTH));
            return new String(cipher.doFinal(ivAndBody, IV_LENGTH, ivAndBody.length - IV_LENGTH), StandardCharsets.UTF_8);
        } catch (GeneralSecurityException | IllegalArgumentException e) {
            throw new UserIdCipherException("Decryption failed", e);
        }
    }

    /**
     * Decrypts the legacy ONAP Portal {@code UserId} cookie with the key {@code cipher.enc.key} from {@code key.properties} on
     * the classpath.
     */
    public static String decrypt(String encrypted) throws UserIdCipherException {
        if (encrypted == null) {
            throw new UserIdCipherException("Cannot decrypt a null value");
        }
        try {
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.DECRYPT_MODE, secretKey(KeyFile.key()));
            byte[] body = Base64.decodeBase64(encrypted.getBytes(StandardCharsets.UTF_8));
            return new String(cipher.doFinal(body), StandardCharsets.UTF_8);
        } catch (GeneralSecurityException | IllegalArgumentException e) {
            throw new UserIdCipherException("Decryption failed", e);
        }
    }

    private static SecretKeySpec secretKey(String base64Key) throws UserIdCipherException {
        if (base64Key == null) {
            throw new UserIdCipherException("No key given");
        }
        return new SecretKeySpec(Base64.decodeBase64(base64Key), ALGORITHM);
    }

    private static final class KeyFile {

        private static final String KEY;
        private static final IOException READ_FAILURE;

        static {
            String key = null;
            IOException readFailure = null;
            try {
                key = load();
            } catch (IOException e) {
                readFailure = e;
            }
            KEY = key;
            READ_FAILURE = readFailure;
        }

        private static String key() throws UserIdCipherException {
            if (READ_FAILURE != null) {
                throw new UserIdCipherException("Cannot read " + KEY_FILE + " from the classpath", READ_FAILURE);
            }
            if (KEY == null) {
                throw new UserIdCipherException(KEY_PROPERTY + " is not set in " + KEY_FILE + " on the classpath");
            }
            return KEY;
        }

        private static String load() throws IOException {
            try (InputStream in = UserIdCipher.class.getClassLoader().getResourceAsStream(KEY_FILE)) {
                if (in == null) {
                    return null;
                }
                Properties properties = new Properties();
                properties.load(in);
                String key = properties.getProperty(KEY_PROPERTY);
                return key == null ? null : key.trim();
            }
        }
    }
}
