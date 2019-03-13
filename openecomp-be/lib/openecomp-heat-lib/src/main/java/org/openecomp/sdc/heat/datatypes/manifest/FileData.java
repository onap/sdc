/*-
 * ============LICENSE_START=======================================================
 * SDC
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights reserved.
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

package org.openecomp.sdc.heat.datatypes.manifest;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.collections4.CollectionUtils;

@Data
public class FileData {

    protected static final Set<Type> heatFileTypes =
            new HashSet<>(Arrays.asList(Type.HEAT, Type.HEAT_NET, Type.HEAT_VOL));

    @Getter(AccessLevel.NONE)
    @Setter(AccessLevel.NONE)
    private Boolean isBase;
    private String parentFile;
    private String file;
    private Type type;
    private List<FileData> data;

    public static Predicate<FileData> buildFileDataPredicateByType(Type... types) {
        return fileData -> Arrays.asList(types).contains(fileData.getType());
    }

    public static boolean isHeatFile(Type type) {
        return heatFileTypes.contains(type);
    }

    public Boolean getBase() {
        return isBase;
    }

    public void setBase(Boolean base) {
        isBase = base;
    }

    /**
     * Add file data.
     *
     * @param data the data
     */
    public void addFileData(FileData data) {
        if (CollectionUtils.isEmpty(this.data)) {
            this.data = new ArrayList<>();
        }
        this.data.add(data);
    }

    @AllArgsConstructor
    @Getter
    public enum Type {
        HEAT("HEAT"),
        HEAT_ENV("HEAT_ENV"),
        HEAT_NET("HEAT_NET"),
        HEAT_VOL("HEAT_VOL"),
        CHEF("CHEF"),
        PUPPET("PUPPET"),
        SHELL("SHELL"),
        YANG("YANG"),
        YANG_XML("YANG_XML"),
        BPEL("BPEL"),
        DG_XML("DG_XML"),
        MURANO_PKG("MURANO_PKG"),
        VENDOR_LICENSE("VENDOR_LICENSE"),
        VF_LICENSE("VF_LICENSE"),
        CLOUD_TECHNOLOGY_SPECIFIC_ARTIFACT("CLOUD_TECHNOLOGY_SPECIFIC_ARTIFACT"),
        OTHER("OTHER");

        private String displayName;

        public static boolean isArtifact(Type fileType) {
            return !Arrays.asList(HEAT, HEAT_ENV, HEAT_VOL).contains(fileType);
        }

        public static boolean canBeAssociated(Type fileType) {
            return HEAT_VOL == fileType;
        }
    }
}
