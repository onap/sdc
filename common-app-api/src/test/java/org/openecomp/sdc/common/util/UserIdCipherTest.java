/*-
 * ============LICENSE_START=======================================================
 * SDC
 * ================================================================================
 * Copyright (C) 2026 Deutsche Telekom AG. All rights reserved.
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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;
import java.util.Properties;
import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;

class UserIdCipherTest {

    private static final int IV_LENGTH = 16;
    private static final int BLOCK_SIZE = 16;
    private static Properties vectors;

    @BeforeAll
    static void loadVectors() throws IOException {
        vectors = new Properties();
        try (InputStream in = UserIdCipherTest.class.getResourceAsStream("/user-id-cipher/vectors.properties")) {
            vectors.load(in);
        }
    }

    private static String explicitKey() {
        return vectors.getProperty("key.explicit");
    }

    private static Stream<Arguments> pkcExplicitKeyVectors() {
        return vectors("pkc.explicit.%d.%d", 2);
    }

    private static Stream<Arguments> pkcKeyFileVectors() {
        return vectors("pkc.keyfile.%d.%d", 2);
    }

    private static Stream<Arguments> legacyKeyFileVectors() {
        return vectors("legacy.keyfile.%d", 1);
    }

    private static Stream<Arguments> plaintexts() {
        int count = Integer.parseInt(vectors.getProperty("count"));
        List<Arguments> arguments = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            arguments.add(Arguments.of(vectors.getProperty("plain." + i)));
        }
        return arguments.stream();
    }

    private static Stream<Arguments> vectors(String cipherKeyPattern, int perPlaintext) {
        int count = Integer.parseInt(vectors.getProperty("count"));
        List<Arguments> arguments = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            for (int r = 0; r < perPlaintext; r++) {
                String cipherText = vectors.getProperty(String.format(cipherKeyPattern, i, r));
                arguments.add(Arguments.of(cipherText, vectors.getProperty("plain." + i)));
            }
        }
        return arguments.stream();
    }

    @ParameterizedTest
    @MethodSource("pkcExplicitKeyVectors")
    void decryptPkcWithExplicitKeyReadsPortalSdkCiphertext(String cipherText, String plainText) throws Exception {
        assertEquals(plainText, UserIdCipher.decryptPkc(cipherText, explicitKey()));
    }

    @ParameterizedTest
    @MethodSource("pkcKeyFileVectors")
    void decryptPkcWithKeyFileReadsPortalSdkCiphertext(String cipherText, String plainText) throws Exception {
        assertEquals(plainText, UserIdCipher.decryptPkc(cipherText));
    }

    @ParameterizedTest
    @MethodSource("legacyKeyFileVectors")
    void decryptReadsPortalSdkLegacyCiphertext(String cipherText, String plainText) throws Exception {
        assertEquals(plainText, UserIdCipher.decrypt(cipherText));
    }

    @ParameterizedTest
    @MethodSource("plaintexts")
    void encryptPkcWithExplicitKeyRoundTrips(String plainText) throws Exception {
        assertEquals(plainText, UserIdCipher.decryptPkc(UserIdCipher.encryptPkc(plainText, explicitKey()), explicitKey()));
    }

    @ParameterizedTest
    @MethodSource("plaintexts")
    void encryptPkcWithKeyFileRoundTrips(String plainText) throws Exception {
        assertEquals(plainText, UserIdCipher.decryptPkc(UserIdCipher.encryptPkc(plainText)));
    }

    @ParameterizedTest
    @MethodSource("plaintexts")
    void encryptPkcPrefixesAnIvToOnePaddedCbcBody(String plainText) throws Exception {
        byte[] raw = Base64.getDecoder().decode(UserIdCipher.encryptPkc(plainText, explicitKey()));
        int paddedBody = (plainText.getBytes(StandardCharsets.UTF_8).length / BLOCK_SIZE + 1) * BLOCK_SIZE;
        assertEquals(IV_LENGTH + paddedBody, raw.length);
    }

    @Test
    void encryptPkcUsesAFreshIvEveryCall() throws Exception {
        assertNotEquals(UserIdCipher.encryptPkc("cs0008", explicitKey()), UserIdCipher.encryptPkc("cs0008", explicitKey()));
    }

    @Test
    void decryptPkcRejectsACiphertextWhosePaddingWasTamperedWith() {
        byte[] raw = Base64.getDecoder().decode(vectors.getProperty("pkc.explicit.0.0"));
        // "cs0008" is a single block ending in ten 0x0a padding bytes; the last IV byte XORs straight into the last of them.
        raw[IV_LENGTH - 1] ^= (byte) 0x80;
        String tampered = Base64.getEncoder().encodeToString(raw);
        assertThrows(UserIdCipherException.class, () -> UserIdCipher.decryptPkc(tampered, explicitKey()));
    }

    @Test
    void decryptPkcRejectsACiphertextCutInsideABlock() {
        String cipherText = vectors.getProperty("pkc.explicit.3.0");
        byte[] raw = Base64.getDecoder().decode(cipherText);
        byte[] cut = Arrays.copyOf(raw, raw.length - 5);
        assertThrows(UserIdCipherException.class,
            () -> UserIdCipher.decryptPkc(Base64.getEncoder().encodeToString(cut), explicitKey()));
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"AAAA", "AAAAAAAAAAAAAAAAAAAAAA=="})
    void decryptPkcRejectsCiphertextsShorterThanIvPlusOneBlock(String cipherText) {
        assertThrows(UserIdCipherException.class, () -> UserIdCipher.decryptPkc(cipherText, explicitKey()));
    }

    @ParameterizedTest
    @NullSource
    @ValueSource(strings = {"AAAA", "0XqZextxttCbXWdMGNTT0mivHxuc2QpXF53e"})
    void decryptRejectsCiphertextsThatAreNotWholeBlocks(String cipherText) {
        assertThrows(UserIdCipherException.class, () -> UserIdCipher.decrypt(cipherText));
    }

    @Test
    void decryptRejectsALegacyCiphertextWhoseLastBlockWasReplaced() {
        byte[] raw = Base64.getDecoder().decode(vectors.getProperty("legacy.keyfile.6"));
        // "0123456789abcdef" fills the first block exactly, so the second is all padding. ECB decrypts every block on its own,
        // so copying the first block over the second makes the last plaintext byte 'f' (0x66), which is never valid padding.
        System.arraycopy(raw, 0, raw, BLOCK_SIZE, BLOCK_SIZE);
        String tampered = Base64.getEncoder().encodeToString(raw);
        assertThrows(UserIdCipherException.class, () -> UserIdCipher.decrypt(tampered));
    }

    @Test
    void encryptPkcRejectsANullKey() {
        UserIdCipherException e = assertThrows(UserIdCipherException.class, () -> UserIdCipher.encryptPkc("cs0008", null));
        assertEquals("No key given", e.getMessage());
    }

    @Test
    void decryptOfAnEmptyLegacyCookieIsEmpty() throws Exception {
        // As in epsdk: PortalServlet then falls back to the default user instead of answering 305.
        assertEquals("", UserIdCipher.decrypt(""));
    }

    @Test
    void encryptPkcRejectsAKeyThatIsNotAnAesKey() {
        assertThrows(UserIdCipherException.class, () -> UserIdCipher.encryptPkc("cs0008", "AAAA"));
    }
}
