/*
 * Copyright © 2016-2018 European Support Limited
 * Modification Copyright (C) 2021 Nokia.
 *
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
 */

package org.openecomp.sdc.tosca.csar;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.google.common.collect.ImmutableMap;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openecomp.sdc.be.datatypes.enums.ResourceTypeEnum;
import org.openecomp.sdc.common.errors.Messages;


class SOL004ManifestOnboardingTest {

    private Manifest manifest;

    @BeforeEach
    public void setUp() {
        manifest = new SOL004ManifestOnboarding();
    }

    @Test
    public void testSuccessfulParsing() throws IOException {
        try (final InputStream manifestAsStream =
                 getClass().getResourceAsStream("/vspmanager.csar/manifest/ValidTosca.mf")) {
            manifest.parse(manifestAsStream);
            assertValidManifest(4, 5, Collections.emptyMap(), ResourceTypeEnum.VF, false);
        }
    }

    @Test
    public void testNoMetadataParsing() throws IOException {
        try (final InputStream manifestAsStream = getClass()
            .getResourceAsStream("/vspmanager.csar/manifest/invalid/no-metadata.mf")) {
            manifest.parse(manifestAsStream);
            final List<String> expectedErrorList = new ArrayList<>();
            expectedErrorList.add(
                buildErrorMessage(3, "Source: MainServiceTemplate.yaml", Messages.MANIFEST_START_METADATA)
            );
            assertInvalidManifest(expectedErrorList);
        }
    }

    @Test
    public void testBrokenMDParsing() throws IOException {
        try (final InputStream manifestAsStream =
                 getClass().getResourceAsStream("/vspmanager.csar/manifest/InvalidTosca2.mf")) {
            manifest.parse(manifestAsStream);
            final List<String> expectedErrorList = new ArrayList<>();
            expectedErrorList.add(Messages.MANIFEST_INVALID_LINE.formatMessage(9, "vnf_package_version: 1.0"));
            assertInvalidManifest(expectedErrorList);
        }
    }

    @Test
    public void testNoMetaParsing() throws IOException {
        try (final InputStream manifestAsStream = getClass()
            .getResourceAsStream("/vspmanager.csar/manifest/invalid/empty-metadata-with-source.mf")) {
            manifest.parse(manifestAsStream);
            final List<String> expectedErrorList = new ArrayList<>();
            expectedErrorList.add(
                buildErrorMessage(4, "Source: MainServiceTemplate.yaml",
                    Messages.MANIFEST_METADATA_INVALID_ENTRY1, "Source: MainServiceTemplate.yaml")
            );
            assertInvalidManifest(expectedErrorList);
        }
    }

    @Test
    public void testSuccessfulNonManoParsing() throws IOException {
        try (final InputStream manifestAsStream = getClass()
            .getResourceAsStream("/vspmanager.csar/manifest/ValidNonManoTosca.mf")) {
            manifest.parse(manifestAsStream);
            assertValidManifest(4, 5,
                ImmutableMap.of("foo_bar", 3, "prv.happy-nfv.cool", 3), ResourceTypeEnum.VF, false);
        }
    }

    @Test
    public void testInvalidNonManoParsing() throws IOException {
        try (final InputStream manifestAsStream = getClass()
            .getResourceAsStream("/vspmanager.csar/manifest/InValidNonManoTosca.mf")) {
            manifest.parse(manifestAsStream);
            final List<String> errorList = Collections.singletonList(
                buildErrorMessage(34, "vnf_product_name: Mock", Messages.MANIFEST_INVALID_NON_MANO_KEY,
                    "vnf_product_name")
            );
            assertInvalidManifest(errorList);
        }
    }

    private String buildErrorMessage(final int lineNumber, final String line, final Messages message,
                                     final Object... params) {
        return Messages.MANIFEST_ERROR_WITH_LINE.formatMessage(message.formatMessage(params), lineNumber, line);
    }

    @Test
    public void testNonManoParsingWithGarbage() throws IOException {
        try (final InputStream manifestAsStream = getClass()
            .getResourceAsStream("/vspmanager.csar/manifest/InvalidToscaNonManoGarbageAtEnd.mf")) {
            manifest.parse(manifestAsStream);
            final List<String> errorList = Collections.singletonList(
                Messages.MANIFEST_ERROR_WITH_LINE.formatMessage(
                    Messages.MANIFEST_INVALID_NON_MANO_KEY.formatMessage("some garbage"),
                    34, "some garbage")
            );
            assertInvalidManifest(errorList);
        }
    }

    @Test
    public void testInvalidManifestFile() throws IOException {
        try (final InputStream manifestAsStream = getClass()
            .getResourceAsStream("/vspmanager.csar/manifest/SOME_WRONG_FILE")) {
            manifest.parse(manifestAsStream);
            assertInvalidManifest(Collections.singletonList(Messages.MANIFEST_PARSER_INTERNAL.getErrorMessage()));
        }
    }

    @Test
    public void testManifestSigned() throws IOException {
        try (final InputStream manifestAsStream = getClass()
            .getResourceAsStream("/vspmanager.csar/manifest/valid/signed.mf")) {
            manifest.parse(manifestAsStream);
            assertValidManifest(4, 3, Collections.emptyMap(), ResourceTypeEnum.VF, true);
        }
    }

    @Test
    public void testManifestSignedWithNonManoArtifacts() throws IOException {
        try (final InputStream manifestAsStream = getClass()
            .getResourceAsStream("/vspmanager.csar/manifest/valid/signed-with-non-mano.mf")) {
            manifest.parse(manifestAsStream);
            assertValidManifest(4, 3, ImmutableMap.of("foo_bar", 3), ResourceTypeEnum.VF, true);
            manifest.getType().ifPresent(typeEnum -> assertSame(typeEnum, ResourceTypeEnum.VF));
        }
    }

    @Test
    public void testManifestWithPnf() throws IOException {
        try (final InputStream manifestAsStream = getClass()
            .getResourceAsStream("/vspmanager.csar/manifest/valid/metadata-pnfd.mf")) {
            manifest.parse(manifestAsStream);
            assertValidManifest(4, 3, new HashMap<>(), ResourceTypeEnum.PNF, true);
        }
    }

    @Test
    public void testMetadataWithNoValue() throws IOException {
        try (final InputStream manifestAsStream = getClass()
            .getResourceAsStream("/vspmanager.csar/manifest/invalid/metadata-no-value.mf")) {
            manifest.parse(manifestAsStream);

            final List<String> expectedErrorList = new ArrayList<>();
            expectedErrorList.add(
                buildErrorMessage(3, "vnf_provider_id", Messages.MANIFEST_METADATA_INVALID_ENTRY1, "vnf_provider_id")
            );
            assertInvalidManifest(expectedErrorList);
        }
    }

    @Test
    public void testMetadataWithValueButNoEntry() throws IOException {
        try (final InputStream manifestAsStream = getClass()
            .getResourceAsStream("/vspmanager.csar/manifest/invalid/metadata-no-entry.mf")) {
            manifest.parse(manifestAsStream);

            final List<String> expectedErrorList = new ArrayList<>();
            expectedErrorList.add(
                buildErrorMessage(3, ": no-entry-value", Messages.MANIFEST_METADATA_INVALID_ENTRY1, ": no-entry-value")
            );
            assertInvalidManifest(expectedErrorList);
        }
    }

    @Test
    public void testMetadataWithIncorrectEntry() throws IOException {
        try (final InputStream manifestAsStream = getClass()
            .getResourceAsStream("/vspmanager.csar/manifest/invalid/metadata-incorrect-entry.mf")) {
            manifest.parse(manifestAsStream);
            final List<String> expectedErrorList = new ArrayList<>();
            expectedErrorList.add(
                buildErrorMessage(4, "vnf_release_data_time: 2019-08-29T22:17:39.275281",
                    Messages.MANIFEST_METADATA_INVALID_ENTRY1, "vnf_release_data_time: 2019-08-29T22:17:39.275281")
            );
            assertInvalidManifest(expectedErrorList);
        }
    }

    @Test
    public void testMetadataWithMixedEntries() throws IOException {
        try (final InputStream manifestAsStream = getClass()
            .getResourceAsStream("/vspmanager.csar/manifest/invalid/metadata-mixed-entries.mf")) {
            manifest.parse(manifestAsStream);
            final List<String> expectedErrorList = new ArrayList<>();
            expectedErrorList.add(buildErrorMessage(6, "", Messages.MANIFEST_METADATA_UNEXPECTED_ENTRY_TYPE));
            assertInvalidManifest(expectedErrorList);
        }
    }

    @Test
    public void testMetadataWithDuplicatedEntries() throws IOException {
        try (final InputStream manifestAsStream =
                 getClass().getResourceAsStream("/vspmanager.csar/manifest/invalid/metadata-duplicated-entries.mf")) {
            manifest.parse(manifestAsStream);
            final List<String> expectedErrorList = new ArrayList<>();
            expectedErrorList.add(
                buildErrorMessage(4, "vnf_product_name: vPP", Messages.MANIFEST_METADATA_DUPLICATED_ENTRY,
                    "vnf_product_name")
            );
            assertInvalidManifest(expectedErrorList);
        }
    }

    @Test
    public void testManifestNonManoKeyWithoutSources() throws IOException {
        try (final InputStream manifestAsStream =
                 getClass().getResourceAsStream("/vspmanager.csar/manifest/invalid/non-mano-key-with-no-sources.mf")) {
            manifest.parse(manifestAsStream);
            final List<String> expectedErrorList = new ArrayList<>();
            expectedErrorList.add(
                buildErrorMessage(11, "", Messages.MANIFEST_EMPTY_NON_MANO_KEY,
                    "foo_bar")
            );
            assertInvalidManifest(expectedErrorList);
        }
    }

    @Test
    public void testManifestNonManoKeyWithEmptySourceEntry() throws IOException {
        try (final InputStream manifestAsStream =
                 getClass().getResourceAsStream("/vspmanager.csar/manifest/invalid/non-mano-key-with-empty-source.mf")) {
            manifest.parse(manifestAsStream);
            final List<String> expectedErrorList = new ArrayList<>();
            expectedErrorList.add(
                buildErrorMessage(11, "Source:", Messages.MANIFEST_EMPTY_NON_MANO_SOURCE)
            );
            assertInvalidManifest(expectedErrorList);
        }
    }

    @Test
    public void testManifestWithEmptyMetadata() throws IOException {
        try (final InputStream manifestAsStream =
                 getClass().getResourceAsStream("/vspmanager.csar/manifest/invalid/empty-metadata.mf")) {
            manifest.parse(manifestAsStream);
            final List<String> expectedErrorList = new ArrayList<>();
            expectedErrorList.add(buildErrorMessage(2, "", Messages.MANIFEST_NO_METADATA));
            assertInvalidManifest(expectedErrorList);
        }
    }

    @Test
    public void testManifestSourceAlgorithmWithoutHash() throws IOException {
        try (final InputStream manifestAsStream =
                 getClass().getResourceAsStream("/vspmanager.csar/manifest/invalid/source-algorithm-without-hash.mf")) {
            manifest.parse(manifestAsStream);
            final List<String> expectedErrorList = new ArrayList<>();
            expectedErrorList.add(buildErrorMessage(9, "", Messages.MANIFEST_EXPECTED_HASH_ENTRY));
            assertInvalidManifest(expectedErrorList);
        }
    }

    @Test
    public void testManifestSourceHashWithoutAlgorithm() throws IOException {
        try (final InputStream manifestAsStream =
                 getClass().getResourceAsStream("/vspmanager.csar/manifest/invalid/source-hash-without-algorithm.mf")) {
            manifest.parse(manifestAsStream);
            final List<String> expectedErrorList = new ArrayList<>();
            expectedErrorList.add(buildErrorMessage(8, "Hash: 3b119b37da5b76ec7c933168b21cedd8", Messages.MANIFEST_EXPECTED_ALGORITHM_BEFORE_HASH));
            assertInvalidManifest(expectedErrorList);
        }
    }

    @Test
    public void testManifestSourceAlgorithmWithoutValue() throws IOException {
        try (final InputStream manifestAsStream =
                 getClass().getResourceAsStream("/vspmanager.csar/manifest/invalid/source-algorithm-without-value.mf")) {
            manifest.parse(manifestAsStream);
            final List<String> expectedErrorList = new ArrayList<>();
            expectedErrorList.add(buildErrorMessage(8, "Algorithm:", Messages.MANIFEST_EXPECTED_ALGORITHM_VALUE));
            assertInvalidManifest(expectedErrorList);
        }
    }

    @Test
    public void testManifestSourceHashWithoutValue() throws IOException {
        try (final InputStream manifestAsStream =
                 getClass().getResourceAsStream("/vspmanager.csar/manifest/invalid/source-hash-without-value.mf")) {
            manifest.parse(manifestAsStream);
            final List<String> expectedErrorList = new ArrayList<>();
            expectedErrorList.add(buildErrorMessage(9, "Hash:", Messages.MANIFEST_EXPECTED_HASH_VALUE));
            assertInvalidManifest(expectedErrorList);
        }
    }

    @Test
    public void testEmptyManifest() throws IOException {
        try (final InputStream manifestAsStream =
                 getClass().getResourceAsStream("/vspmanager.csar/manifest/invalid/empty-manifest.mf")) {
            manifest.parse(manifestAsStream);
            final List<String> expectedErrorList = new ArrayList<>();
            expectedErrorList.add(Messages.MANIFEST_EMPTY.getErrorMessage());
            assertInvalidManifest(expectedErrorList);
        }
    }

    @Test
    public void testManifestWithDuplicatedCmsSignature()
        throws IOException, NoSuchFieldException, IllegalAccessException {
        try (final InputStream manifestAsStream =
                 getClass().getResourceAsStream("/vspmanager.csar/manifest/valid/signed.mf")) {
            //forcing an existing signature
            final Field cmsSignatureField = AbstractOnboardingManifest.class.getDeclaredField("cmsSignature");
            cmsSignatureField.setAccessible(true);
            cmsSignatureField.set(manifest, "any value");
            manifest.parse(manifestAsStream);

            final List<String> expectedErrorList = new ArrayList<>();
            expectedErrorList
                .add(buildErrorMessage(18, "-----BEGIN CMS-----", Messages.MANIFEST_SIGNATURE_DUPLICATED));
            assertInvalidManifest(expectedErrorList);
        }
    }

    @Test
    public void testGetEntry() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        final Method getEntryMethod = AbstractOnboardingManifest.class.getDeclaredMethod("readEntryName", String.class);
        getEntryMethod.setAccessible(true);
        final Optional<String> noEntry = (Optional<String>) getEntryMethod.invoke(manifest, ":");
        assertFalse(noEntry.isPresent(), "Entry should not be present");

        final Optional<String> blankEntry = (Optional<String>) getEntryMethod.invoke(manifest, "        :");
        assertFalse(blankEntry.isPresent(), "Entry should not be present");

        final Optional<String> noColon = (Optional<String>) getEntryMethod.invoke(manifest, "anyKeyWithoutColon   ");
        assertFalse(noColon.isPresent(), "Entry should not be present");

        final Optional<String> blank = (Optional<String>) getEntryMethod.invoke(manifest, "   ");
        assertFalse(blank.isPresent(), "Entry should not be present");

        final Optional<String> empty = (Optional<String>) getEntryMethod.invoke(manifest, "");
        assertFalse(empty.isPresent(), "Entry should not be present");

        final Optional<String> nul1 = (Optional<String>) getEntryMethod.invoke(manifest, new Object[]{null});
        assertFalse(nul1.isPresent(), "Entry should not be present");

        final Optional<String> entry = (Optional<String>) getEntryMethod
            .invoke(manifest, "      entry to     test     :       : a value ::: test test:   ");
        assertTrue(entry.isPresent(), "Entry should be present");
        assertEquals("entry to     test", entry.get(), "Entry should be as expected");
    }

    @Test
    public void testGetValue() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        final Method getValueMethod = AbstractOnboardingManifest.class.getDeclaredMethod("readEntryValue", String.class);
        getValueMethod.setAccessible(true);
        final Optional<String> noValue = (Optional<String>) getValueMethod.invoke(manifest, ":");
        assertFalse(noValue.isPresent(), "Value should not be present");

        final Optional<String> blankValue = (Optional<String>) getValueMethod.invoke(manifest, ":          ");
        assertFalse(blankValue.isPresent(), "Value should not be present");

        final Optional<String> noColon = (Optional<String>) getValueMethod.invoke(manifest, "anyKeyWithoutColon   ");
        assertFalse(noColon.isPresent(), "Value should not be present");

        final Optional<String> blank = (Optional<String>) getValueMethod.invoke(manifest, "   ");
        assertFalse(blank.isPresent(), "Value should not be present");

        final Optional<String> empty = (Optional<String>) getValueMethod.invoke(manifest, "");
        assertFalse(empty.isPresent(), "Value should not be present");

        final Optional<String> nul1 = (Optional<String>) getValueMethod.invoke(manifest, new Object[]{null});
        assertFalse(nul1.isPresent(), "Value should not be present");

        final Optional<String> value = (Optional<String>) getValueMethod
            .invoke(manifest, "attribute     :       : a value ::: test test:   ");
        assertTrue(value.isPresent(), "Value should be present");
        assertEquals(": a value ::: test test:", value.get(), "Value should be as expected");
    }

    @Test
    public void testSuccessfulSignedManifestWithIndividualSignature() throws IOException {
        try (final InputStream manifestAsStream =
                 getClass().getResourceAsStream("/vspmanager.csar/manifest/valid/individualSignature/signedWithIndividualSignature.mf")) {
            manifest.parse(manifestAsStream);
            assertValidManifest(4, 3, Collections.emptyMap(), ResourceTypeEnum.VF, true);
        }
    }

    @Test
    public void testSuccessfulUnsignedManifestWithIndividualSignaturee() throws IOException {
        try (final InputStream manifestAsStream =
                 getClass().getResourceAsStream("/vspmanager.csar/manifest/valid/individualSignature/unsignedWithIndividualSignature.mf")) {
            manifest.parse(manifestAsStream);
            assertValidManifest(4, 3, Collections.emptyMap(), ResourceTypeEnum.VF, false);
        }
    }

    @Test
    public void testSuccessfulSignedManifestWithIndividualSignatureAndHash() throws IOException {
        try (final InputStream manifestAsStream =
                 getClass().getResourceAsStream("/vspmanager.csar/manifest/valid/individualSignature/signedWithIndividualSignatureAndHash.mf")) {
            manifest.parse(manifestAsStream);
            assertValidManifest(4, 3, Collections.emptyMap(), ResourceTypeEnum.VF, true);
        }
    }

    @Test
    public void testSuccessfulSignedManifestWithIndividualSignatureAndCommonCert() throws IOException {
        try (final InputStream manifestAsStream =
                 getClass().getResourceAsStream("/vspmanager.csar/manifest/valid/individualSignature/signedWithIndividualSignatureCommonCert.mf")) {
            manifest.parse(manifestAsStream);
            assertValidManifest(4, 3, Collections.emptyMap(), ResourceTypeEnum.VF, true);
        }
    }

    @Test
    public void testEmptyIndividualSignature() throws IOException {
        try (final InputStream manifestAsStream =
                 getClass().getResourceAsStream("/vspmanager.csar/manifest/invalid/individualSignature/signedWithEmptyIndividualSignature.mf")) {
            manifest.parse(manifestAsStream);
            final List<String> expectedErrorList = List.of(
                buildErrorMessage(
                    8, "Signature:", Messages.MANIFEST_EXPECTED_SIGNATURE_VALUE
                ));
            assertInvalidManifest(expectedErrorList);
        }
    }

    @Test
    public void testEmptyIndividualCertificate() throws IOException {
        try (final InputStream manifestAsStream =
                 getClass().getResourceAsStream("/vspmanager.csar/manifest/invalid/individualSignature/signedWithEmptyIndividualCertificate.mf")) {
            manifest.parse(manifestAsStream);
            final List<String> expectedErrorList = List.of(
                buildErrorMessage(
                    9, "Certificate:", Messages.MANIFEST_EXPECTED_CERTIFICATE_VALUE
                ));
            assertInvalidManifest(expectedErrorList);
        }
    }

    @Test
    public void testOnlyIndividualCertificateNoSignature() throws IOException {
        try (final InputStream manifestAsStream =
                 getClass().getResourceAsStream("/vspmanager.csar/manifest/invalid/individualSignature/signedWithIndividualCertificateNoSignature.mf")) {
            manifest.parse(manifestAsStream);
            final List<String> expectedErrorList = List.of(
                buildErrorMessage(
                    8, "Certificate: TOSCA-Metadata/TOSCA.cert", Messages.MANIFEST_EXPECTED_SIGNATURE_BEFORE_CERTIFICATE
                ));
            assertInvalidManifest(expectedErrorList);
        }
    }

    @Test
    public void testSuccessfulParsingWithCompatibleSpecficationVersion() throws IOException {
        try (final InputStream manifestAsStream =
            getClass().getResourceAsStream("/vspmanager.csar/manifest/ValidToscaVersion3.mf")) {
            manifest.parse(manifestAsStream);
            assertValidManifest(8, 5, Collections.emptyMap(), ResourceTypeEnum.VF, false);
        }
    }

    @Test
    public void testFailedParsingWithCompatibleSpecficationVersion() throws IOException {
        try (final InputStream manifestAsStream =
            getClass().getResourceAsStream("/vspmanager.csar/manifest/InvalidToscaVersion3.mf")) {
            manifest.parse(manifestAsStream);
            final List<String> errorList = Collections.singletonList(
                    Messages.MANIFEST_ERROR_WITH_LINE.formatMessage(
                        Messages.MANIFEST_METADATA_INVALID_ENTRY1.formatMessage("vnf_test: 1.0"),
                        4, "vnf_test: 1.0")
                );
                assertInvalidManifest(errorList);
        }
    }

    private void assertValidManifest(final int expectedMetadataSize, final int expectedSourcesSize,
                                     final Map<String, Integer> expectedNonManoKeySize,
                                     final ResourceTypeEnum resourceType, final boolean isSigned) {
        assertAll(
            "manifest should be valid",
            () -> assertTrue(manifest.getErrors().isEmpty(), "Should have no errors"),
            () -> assertTrue(manifest.isValid(), "Should be valid"),
            () -> assertTrue(manifest.getType().isPresent(), "Should have a type"),
            () -> assertEquals(resourceType, manifest.getType().get(),  "Type should be as expected"),
            () -> assertEquals(isSigned, manifest.isSigned(), "Signature status should be as expected")
        );
        assertAll(
            "manifest should have expected fields",
            () -> assertEquals(expectedMetadataSize, manifest.getMetadata().keySet().size(),
                "Metadata should have the expected size"),
            () -> assertEquals(expectedSourcesSize, manifest.getSources().size(),
                "Sources should have the expected size"),
            () -> assertEquals(expectedNonManoKeySize.keySet().size(), manifest.getNonManoSources().keySet().size(),
                "Non Mano Sources keys should have the expected size")
        );
        for (final Entry<String, Integer> nonManoKeyAndSize : expectedNonManoKeySize.entrySet()) {
            final String nonManoKey = nonManoKeyAndSize.getKey();
            assertAll(
                "",
                () -> assertTrue(manifest.getNonManoSources().containsKey(nonManoKey),
                    "Should contain expected Non Mano Sources key"),
                () -> assertEquals(nonManoKeyAndSize.getValue(),manifest.getNonManoSources().get(nonManoKey).size(),
                    String.format("Non Mano Sources keys %s should have the expected sources size", nonManoKey))
            );
        }
    }

    private void assertInvalidManifest(final List<String> expectedErrorList) {
        assertAll(
            "manifest should be invalid and should contain expected errors",
            () -> assertFalse(manifest.isValid(), "Should be invalid"),
            () -> assertArrayEquals(manifest.getErrors().toArray(), expectedErrorList.toArray(), "Should have expected errors")
        );
    }
}
