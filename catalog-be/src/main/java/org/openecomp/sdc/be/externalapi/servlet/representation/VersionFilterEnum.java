/*
 * ============LICENSE_START=======================================================
 *  Copyright (C) 2023 Nordix Foundation
 *  ================================================================================
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 *  SPDX-License-Identifier: Apache-2.0
 *  ============LICENSE_END=========================================================
 *
 *
 */

package org.openecomp.sdc.be.externalapi.servlet.representation;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.openecomp.sdc.be.model.Service;

@Getter
@AllArgsConstructor
public enum VersionFilterEnum {

    EQUALS("equals") {
        @Override
        public List<Service> filter(final List<Service> serviceList, final String version) {
            final Matcher matcher = Pattern.compile("(" + this.getName() + ")" + DELIMITER + "(" + VERSION_REGEX + ")").matcher(version);
            if (matcher.find()) {
                return serviceList.parallelStream()
                    .filter(service -> convertToInteger(service.getVersion()).compareTo(convertToInteger(matcher.group(2))) == 0)
                    .collect(Collectors.toList());
            }
            return serviceList;
        }
    },
    GREATER_THAN("greaterThan") {
        @Override
        public List<Service> filter(final List<Service> serviceList, final String version) {
            final Matcher matcher = Pattern.compile("(" + this.getName() + ")" + DELIMITER + "(" + VERSION_REGEX + ")").matcher(version);
            if (matcher.find()) {
                return serviceList.parallelStream()
                    .filter(service -> convertToInteger(service.getVersion()).compareTo(convertToInteger(matcher.group(2))) > 0)
                    .collect(Collectors.toList());
            }
            return serviceList;
        }
    },
    LESS_THAN("lessThan") {
        @Override
        public List<Service> filter(final List<Service> serviceList, final String version) {
            final Matcher matcher = Pattern.compile("(" + this.getName() + ")" + DELIMITER + "(" + VERSION_REGEX + ")").matcher(version);
            if (matcher.find()) {
                return serviceList.parallelStream()
                    .filter(service -> convertToInteger(service.getVersion()).compareTo(convertToInteger(matcher.group(2))) < 0)
                    .collect(Collectors.toList());
            }
            return serviceList;
        }
    },
    HIGHEST_MATCHING_VERSION_ONLY("highestMatchingVersionOnly") {
        @Override
        public List<Service> filter(final List<Service> serviceList, final String version) {
            final Matcher matcher = Pattern.compile("(" + this.getName() + ")" + DELIMITER + "([Tt][Rr][Uu][Ee])").matcher(version);
            if (matcher.find()) {
                return serviceList.parallelStream()
                    .collect(Collectors.groupingBy(Service::getName,
                        Collectors.maxBy(Comparator.comparingInt(service -> convertToInteger(service.getVersion())))))
                    .values().parallelStream().map(Optional::get).collect(Collectors.toList());
            }
            return serviceList;
        }
    };

    private final String name;

    private static final String VERSION_REGEX = "[0-9]+\\.?[0-9]?";
    private static final String DELIMITER = "[ :=]+";

    public abstract List<Service> filter(final List<Service> serviceList, final String version);

    public static Optional<VersionFilterEnum> getFilter(final String version) {
        final Matcher matcher = Pattern.compile("([a-zA-Z]+)" + DELIMITER).matcher(version);
        if (matcher.find()) {
            for (final VersionFilterEnum value : VersionFilterEnum.values()) {
                if (value.getName().equals(matcher.group(1))) {
                    return Optional.of(value);
                }
            }
        }
        return Optional.empty();
    }

    private static Integer convertToInteger(final String value) {
        return (int) (Float.parseFloat(value) * 10);
    }

}
