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

package org.openecomp.sdc.common.log;

import static org.assertj.core.api.Assertions.assertThat;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.joran.JoranConfigurator;
import ch.qos.logback.core.status.Status;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

/**
 * logback 1.5.13 removed JaninoEventEvaluator, so a shipped logback.xml that still uses
 * {@code <expression>} or {@code <if condition>} stops working once SDC moves to logback 1.5.
 */
class LogbackConfigParseTest {

    private static final String ROOT_ARTIFACT_ID = "sdc-main";
    private static final Set<String> SKIPPED_DIRS = Set.of("target", "node_modules", ".git");
    private static final Pattern LOGBACK_FILE = Pattern.compile(".*logback.*\\.xml");
    private static final Pattern JANINO_EXPRESSION = Pattern.compile("<expression[\\s>]");
    private static final Pattern JANINO_IF = Pattern.compile("<if[\\s>]");
    private static final Pattern INCLUDED_FRAGMENT = Pattern.compile("<included[\\s>]");
    private static final Pattern IGNORED_POM_BLOCKS = Pattern.compile("(?s)<parent>.*?</parent>|<!--.*?-->");
    // The sdc-fe logging config still uses SizeAndTimeBasedFNATP, which logback 1.5 keeps (deprecated);
    // both warnings logback 1.2 emits for it point at SizeAndTimeBasedRollingPolicy.
    private static final String KNOWN_DEPRECATION = "SizeAndTimeBasedRollingPolicy";
    private static final Pattern ARTIFACT_ID = Pattern.compile("<artifactId>\\s*([^<\\s]+)\\s*</artifactId>");

    @TempDir
    Path tempDir;

    static Stream<Path> shippedLogbackFiles() throws IOException {
        Path root = repoRoot();
        List<Path> files = new ArrayList<>();
        Files.walkFileTree(root, new SimpleFileVisitor<>() {
            @Override
            public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) {
                String name = dir.getFileName() == null ? "" : dir.getFileName().toString();
                boolean testSources = "test".equals(name) && dir.getParent() != null
                    && "src".equals(dir.getParent().getFileName().toString());
                return SKIPPED_DIRS.contains(name) || testSources ? FileVisitResult.SKIP_SUBTREE : FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
                if (LOGBACK_FILE.matcher(file.getFileName().toString()).matches()) {
                    files.add(root.relativize(file));
                }
                return FileVisitResult.CONTINUE;
            }
        });
        return files.stream().sorted();
    }

    @Test
    void findsTheShippedLogbackFiles() throws IOException {
        List<String> found = shippedLogbackFiles().map(p -> p.toString().replace('\\', '/')).collect(Collectors.toList());
        assertThat(found).contains(
            "catalog-be/src/main/resources/config/logback.xml",
            "catalog-be/src/main/docker/backend/files/logback.xml",
            "catalog-fe/src/main/resources/config/logback.xml",
            "openecomp-be/dist/sdc-onboard-backend-docker/artifacts/files/logback.xml",
            "openecomp-be/lib/openecomp-core-lib/openecomp-config-lib/src/main/resources/additional-logback.xml");
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("shippedLogbackFiles")
    void parsesWithoutJanino(Path relativeFile) throws Exception {
        Path file = repoRoot().resolve(relativeFile);
        String xml = Files.readString(file);
        assertThat(JANINO_EXPRESSION.matcher(xml).find()).as("%s uses a Janino <expression>", relativeFile).isFalse();
        assertThat(JANINO_IF.matcher(xml).find()).as("%s uses a Janino <if condition>", relativeFile).isFalse();

        Path configHome = tempDir.resolve("config");
        for (String component : List.of("catalog-be", "catalog-fe")) {
            Files.createDirectories(configHome.resolve(component));
            Files.writeString(configHome.resolve(component).resolve("configuration.yaml"), "");
        }
        // The sdc-fe logging config pins /var/log/onap in a local property, which a context
        // property cannot override, and the build user cannot write there.
        Path copy = tempDir.resolve(relativeFile.getFileName());
        Files.writeString(copy, xml.replace("/var/log/onap", tempDir.resolve("var-log").toString()));
        Path config = copy;
        if (INCLUDED_FRAGMENT.matcher(xml).find()) {
            // An <included> fragment cannot be configured on its own; a host config includes it.
            config = tempDir.resolve("host-logback.xml");
            Files.writeString(config, "<configuration><include file=\"" + copy + "\"/></configuration>");
        }

        Properties systemProperties = (Properties) System.getProperties().clone();
        LoggerContext ctx = new LoggerContext();
        try {
            ctx.putProperty("log.home", tempDir.resolve("log").toString());
            ctx.putProperty("config.home", configHome.toString());
            ctx.putProperty("HOME", tempDir.resolve("home").toString());
            JoranConfigurator configurator = new JoranConfigurator();
            configurator.setContext(ctx);
            configurator.doConfigure(config.toFile());

            List<String> problems = ctx.getStatusManager().getCopyOfStatusList().stream()
                .filter(s -> s.getLevel() >= Status.WARN)
                .filter(s -> !(s.getLevel() == Status.WARN && s.getMessage().contains(KNOWN_DEPRECATION)))
                .map(s -> s.getMessage() + (s.getThrowable() == null ? "" : " (" + s.getThrowable() + ")"))
                .collect(Collectors.toList());
            assertThat(problems).as("WARN/ERROR statuses of %s", relativeFile).isEmpty();
        } finally {
            ctx.stop();
            System.setProperties(systemProperties);
        }
    }

    private static Path repoRoot() throws IOException {
        for (Path dir = Paths.get("").toAbsolutePath(); dir != null; dir = dir.getParent()) {
            Path pom = dir.resolve("pom.xml");
            if (Files.isRegularFile(pom) && ROOT_ARTIFACT_ID.equals(projectArtifactId(pom))) {
                return dir;
            }
        }
        throw new IllegalStateException("No pom.xml with artifactId " + ROOT_ARTIFACT_ID + " above " + Paths.get("").toAbsolutePath());
    }

    private static String projectArtifactId(Path pom) throws IOException {
        Matcher m = ARTIFACT_ID.matcher(IGNORED_POM_BLOCKS.matcher(Files.readString(pom)).replaceAll(""));
        return m.find() ? m.group(1) : null;
    }
}
