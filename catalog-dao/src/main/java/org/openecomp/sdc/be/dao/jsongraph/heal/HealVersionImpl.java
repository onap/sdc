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

package org.openecomp.sdc.be.dao.jsongraph.heal;

import com.google.common.base.MoreObjects;
import java.util.Objects;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

class HealVersionImpl<T extends Comparable> implements HealVersion<T> {

    private final T version;

    public HealVersionImpl(T version) {
        Objects.requireNonNull(version, "Version cannot be null");
        this.version = version;
    }

    public T getVersion() {
        return version;
    }


    @Override
    public int compareTo(HealVersion o) {
        return this.version.compareTo( o.getVersion());
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (!(o instanceof HealVersionImpl)) {
            return false;
        }

        HealVersionImpl that = (HealVersionImpl) o;

        return new EqualsBuilder().append(getVersion(), that.getVersion()).isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37).append(getVersion()).toHashCode();
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this).add("version", version).toString();
    }
}
