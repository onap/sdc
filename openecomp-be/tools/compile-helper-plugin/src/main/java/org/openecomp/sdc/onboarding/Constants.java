/*
 * Copyright © 2018 European Support Limited
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on a "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.openecomp.sdc.onboarding;

public class Constants {


    public static final String UNICORN = "unicorn";
    public static final String EMPTY_STRING = "";
    public static final String JACOCO_SKIP = "jacoco.skip";
    public static final String JACOCO_BUILD = Boolean.toString(
            System.getProperties().containsKey(JACOCO_SKIP) && Boolean.FALSE.equals(Boolean.valueOf(
                    System.getProperties().getProperty(JACOCO_SKIP))));
    public static final String JACOCO = Boolean.valueOf(JACOCO_BUILD) ? UNICORN : EMPTY_STRING;
    public static final String PREFIX = System.getProperties().contains(UNICORN) ? EMPTY_STRING : UNICORN;
    public static final String FORK_COUNT = "fork.count";
    public static final String FORK_MODE = "fork.mode";
    public static final String SKIP_PMD = "skipPMD";
    public static final String JAVA_EXT = ".java";
    public static final String ANY_EXT = "*";
    public static final String SKIP_TEST_RUN = PREFIX + JACOCO + "skipTestRun";
    public static final String SKIP_TESTS = "skipTests";
    public static final String MAIN = "main";
    public static final String TEST = "test";
    public static final String RESOURCES_CHANGED = "resourcesChanged";
    public static final String ANSI_YELLOW = "\u001B[43m";
    public static final String ANSI_COLOR_RESET = "\u001B[0m";
    public static final String SKIP_MAIN_SOURCE_COMPILE = PREFIX + JACOCO + "skipMainSourceCompile";
    public static final String SKIP_TEST_SOURCE_COMPILE = PREFIX + JACOCO + "skipTestSourceCompile";
    public static final String MAIN_CHECKSUM = "mainChecksum";
    public static final String CHECKSUM = "checksum";
    public static final String TEST_CHECKSUM = "testChecksum";
    public static final String RESOURCE_CHECKSUM = "resourceChecksum";
    public static final String TEST_RESOURCE_CHECKSUM = "testResourceChecksum";
    public static final String MAIN_SOURCE_CHECKSUM = "mainSourceChecksum";
    public static final String TEST_SOURCE_CHECKSUM = "testSourceChecksum";
    public static final String GENERATED_SOURCE_CHECKSUM = "generatedSourceChecksum";
    public static final String EMPTY_JAR = "emptyJAR";
    public static final String JAR = "jar";
    public static final String SHA1 = "sha1";
    public static final String COLON = ":";
    public static final String DOT = ".";
    public static final String FULL_BUILD_DATA = "fullBuildData";
    public static final String FULL_RESOURCE_BUILD_DATA = "fullResourceBuildData";
    public static final String MODULE_BUILD_DATA = "moduleBuildData";
    public static final String RESOURCE_BUILD_DATA = "resourceBuildData";
    public static final String RESOURCE_ONLY = "resourceOnly";
    public static final String TEST_RESOURCE_ONLY = "testResourceOnly";
    public static final String INSTRUMENT_WITH_TEST_ONLY = "instrumentWithTestOnly";
    public static final String RESOURCE_WITH_TEST_ONLY = "resourceWithTestOnly";
    public static final String INSTRUMENT_ONLY = "instrumentOnly";
    public static final String TEST_ONLY = "testOnly";
    public static final String SKIP_RESOURCE_COLLECTION = PREFIX + JACOCO + "skipResourceCollection";
    public static final String SKIP_INSTALL = PREFIX + JACOCO + "skipInstall";


    private Constants() {
    }


}
