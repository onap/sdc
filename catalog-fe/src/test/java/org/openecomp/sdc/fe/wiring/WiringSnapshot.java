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

package org.openecomp.sdc.fe.wiring;

import static org.junit.jupiter.api.Assertions.fail;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Compares a rendered description of the production wiring with a file checked in under
 * {@code src/test/resources/wiring}. Run with {@code -Dwiring.snapshot.update=true} to rewrite the file when a
 * change to the wiring is intended, so that the change shows up in review as a diff of that file.
 */
final class WiringSnapshot {

    private static final Path DIRECTORY = Paths.get("src/test/resources/wiring");

    private WiringSnapshot() {
    }

    static void assertMatches(String actual, String snapshotName) throws IOException {
        Path snapshot = DIRECTORY.resolve(snapshotName);
        if (Boolean.getBoolean("wiring.snapshot.update")) {
            Files.createDirectories(DIRECTORY);
            Files.write(snapshot, actual.getBytes(StandardCharsets.UTF_8));
            return;
        }
        String expected = new String(Files.readAllBytes(snapshot), StandardCharsets.UTF_8);
        if (expected.equals(actual)) {
            return;
        }
        List<String> expectedLines = Arrays.asList(expected.split("\n"));
        List<String> actualLines = Arrays.asList(actual.split("\n"));
        List<String> missing = new ArrayList<>(expectedLines);
        missing.removeAll(actualLines);
        List<String> unexpected = new ArrayList<>(actualLines);
        unexpected.removeAll(expectedLines);
        StringBuilder message = new StringBuilder("Wiring differs from ").append(snapshot)
            .append(" (rerun with -Dwiring.snapshot.update=true if the change is intended)");
        missing.forEach(line -> message.append("\n- ").append(line));
        unexpected.forEach(line -> message.append("\n+ ").append(line));
        if (missing.isEmpty() && unexpected.isEmpty()) {
            message.append("\nSame lines, different order");
        }
        fail(message.toString());
    }
}
