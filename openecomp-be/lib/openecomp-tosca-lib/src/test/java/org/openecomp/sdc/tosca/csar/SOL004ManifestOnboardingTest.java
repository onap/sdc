/*
 * Copyright © 2016-2018 European Support Limited
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

import static junit.framework.TestCase.assertSame;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

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
import org.junit.Before;
import org.junit.Test;
import org.openecomp.sdc.be.datatypes.enums.ResourceTypeEnum;
import org.openecomp.sdc.common.errors.Messages;

public class SOL004ManifestOnboardingTest {

    private Manifest manifest;

    @Before
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
        assertThat("Entry should not be present", noEntry.isPresent(), is(false));

        final Optional<String> blankEntry = (Optional<String>) getEntryMethod.invoke(manifest, "        :");
        assertThat("Entry should not be present", blankEntry.isPresent(), is(false));

        final Optional<String> noColon = (Optional<String>) getEntryMethod.invoke(manifest, "anyKeyWithoutColon   ");
        assertThat("Entry should not be present", noColon.isPresent(), is(false));

        final Optional<String> blank = (Optional<String>) getEntryMethod.invoke(manifest, "   ");
        assertThat("Entry should not be present", blank.isPresent(), is(false));

        final Optional<String> empty = (Optional<String>) getEntryMethod.invoke(manifest, "");
        assertThat("Entry should not be present", empty.isPresent(), is(false));

        final Optional<String> nul1 = (Optional<String>) getEntryMethod.invoke(manifest, new Object[]{null});
        assertThat("Entry should not be present", nul1.isPresent(), is(false));

        final Optional<String> entry = (Optional<String>) getEntryMethod
            .invoke(manifest, "      entry to     test     :       : a value ::: test test:   ");
        assertThat("Entry should be present", entry.isPresent(), is(true));
        assertThat("Entry should be as expected", entry.get(), equalTo("entry to     test"));
    }

    @Test
    public void testGetValue() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        final Method getValueMethod = AbstractOnboardingManifest.class.getDeclaredMethod("readEntryValue", String.class);
        getValueMethod.setAccessible(true);
        final Optional<String> noValue = (Optional<String>) getValueMethod.invoke(manifest, ":");
        assertThat("Value should not be present", noValue.isPresent(), is(false));

        final Optional<String> blankValue = (Optional<String>) getValueMethod.invoke(manifest, ":          ");
        assertThat("Value should not be present", blankValue.isPresent(), is(false));

        final Optional<String> noColon = (Optional<String>) getValueMethod.invoke(manifest, "anyKeyWithoutColon   ");
        assertThat("Value should not be present", noColon.isPresent(), is(false));

        final Optional<String> blank = (Optional<String>) getValueMethod.invoke(manifest, "   ");
        assertThat("Value should not be present", blank.isPresent(), is(false));

        final Optional<String> empty = (Optional<String>) getValueMethod.invoke(manifest, "");
        assertThat("Value should not be present", empty.isPresent(), is(false));

        final Optional<String> nul1 = (Optional<String>) getValueMethod.invoke(manifest, new Object[]{null});
        assertThat("Value should not be present", nul1.isPresent(), is(false));

        final Optional<String> value = (Optional<String>) getValueMethod
            .invoke(manifest, "attribute     :       : a value ::: test test:   ");
        assertThat("Value should be present", value.isPresent(), is(true));
        assertThat("Value should be as expected", value.get(), equalTo(": a value ::: test test:"));
    }

    private void assertValidManifest(final int expectedMetadataSize, final int expectedSourcesSize,
                                     final Map<String, Integer> expectedNonManoKeySize,
                                     final ResourceTypeEnum resourceType, final boolean isSigned) {
        assertThat("Should have no errors", manifest.getErrors(), is(empty()));
        assertThat("Should be valid", manifest.isValid(), is(true));
        assertThat("Metadata should have the expected size",
            manifest.getMetadata().keySet(), hasSize(expectedMetadataSize));
        assertThat("Sources should have the expected size", manifest.getSources(), hasSize(expectedSourcesSize));
        assertThat("Non Mano Sources keys should have the expected size",
            manifest.getNonManoSources().keySet(), hasSize(expectedNonManoKeySize.keySet().size()));
        for (final Entry<String, Integer> nonManoKeyAndSize : expectedNonManoKeySize.entrySet()) {
            final String nonManoKey = nonManoKeyAndSize.getKey();
            assertThat("Should contain expected Non Mano Sources key",
                manifest.getNonManoSources().keySet(), hasItem(nonManoKey));
            assertThat(String.format("Non Mano Sources keys %s should have the expected sources size", nonManoKey),
                manifest.getNonManoSources().get(nonManoKey).size(), equalTo(nonManoKeyAndSize.getValue()));
        }
        assertThat("Should have a type", manifest.getType().isPresent(), is(true));
        assertThat("Type should be as expected", manifest.getType().get(), equalTo(resourceType));
        assertThat("Signature status should be as expected", manifest.isSigned(), is(isSigned));
    }

    private void assertInvalidManifest(final List<String> expectedErrorList) {
        assertThat("Should be invalid", manifest.isValid(), is(false));
        assertThat("Should have the expected error quantity", manifest.getErrors(), hasSize(expectedErrorList.size()));
        assertThat("Should have expected errors", manifest.getErrors(),
            containsInAnyOrder(expectedErrorList.toArray(new String[0])));
    }
}
