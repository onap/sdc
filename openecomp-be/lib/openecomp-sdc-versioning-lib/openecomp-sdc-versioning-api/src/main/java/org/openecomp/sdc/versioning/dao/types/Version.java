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
package org.openecomp.sdc.versioning.dao.types;

import com.datastax.driver.mapping.annotations.Transient;
import com.datastax.driver.mapping.annotations.UDT;
import java.util.Date;
import java.util.Map;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@UDT(name = "version", keyspace = "dox")
@Getter
@Setter
@NoArgsConstructor
public class Version {

    public static final String VERSION_STRING_VIOLATION_MSG = "Version string must be in the format of: {integer}.{integer}";
    @Transient
    private String id;
    private int major; // TODO: 6/7/2017 remove!

    private int minor; // TODO: 6/7/2017 remove!
    @Transient
    private String name;
    @Transient
    private String description;
    @Transient
    private String baseId;
    @Transient
    private Date creationTime;
    @Transient
    private Date modificationTime;
    @Transient
    private VersionStatus status = VersionStatus.Draft;
    @Transient
    private VersionState state;
    @Transient
    private Map<String, Object> additionalInfo;

    public Version(String id) {
        this.id = id;
    }

    @Deprecated
    public Version(int major, int minor) {
        this.major = major;
        this.minor = minor;
    }

    /**
     * Value of version.
     *
     * @param versionString the version string
     * @return the version
     */
    public static Version valueOf(String versionString) {
        if (versionString == null) {
            return null;
        }
        String[] versionLevels = versionString.split("\\.");
        Version version;
        if (versionLevels.length != 2) {
            throw new IllegalArgumentException(VERSION_STRING_VIOLATION_MSG);
        }
        try {
            version = new Version(Integer.parseInt(versionLevels[0]), Integer.parseInt(versionLevels[1]));
        } catch (Exception ex) {
            throw new IllegalArgumentException(VERSION_STRING_VIOLATION_MSG);
        }
        return version;
    }

    public Version calculateNextCandidate() {
        return new Version(major, minor + 1);
    }

    public Version calculateNextFinal() {
        return new Version(major + 1, 0);
    }

    @Transient
    public boolean isFinal() {
        return major != 0 && minor == 0;
    }

    @Override
    public int hashCode() {
        int result = major;
        result = 31 * result + minor;
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        Version version = (Version) obj;
        return major == version.major && minor == version.minor;
    }

    public int compareTo(Version other) {
        if (this.major > other.major) {
            return 1;
        } else if (this.major < other.major) {
            return -1;
        } else if (this.major == other.major) {
            return Integer.compare(this.minor, other.minor);
        }
        return 0;
    }

    @Override
    public String toString() {
        return name != null ? name : major + "." + minor;
    }

    @Override
    public Version clone() {
        Version version = new Version();
        version.setStatus(this.getStatus());
        version.setCreationTime(this.getCreationTime());
        version.setName(this.getName());
        version.setBaseId(this.getBaseId());
        version.setMajor(this.major);
        version.setMinor(this.minor);
        version.setModificationTime(this.getModificationTime());
        version.setDescription(this.description);
        version.setId(this.getId());
        return version;
    }
}
