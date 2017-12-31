/*
 * Copyright © 2016-2017 European Support Limited
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

package org.openecomp.sdc.notification.dao.types;

import com.datastax.driver.mapping.annotations.Column;
import com.datastax.driver.mapping.annotations.PartitionKey;
import com.datastax.driver.mapping.annotations.Table;

import java.util.Objects;
import java.util.Set;

import static java.util.Objects.hash;

@Table(keyspace = "dox", name = "notification_subscribers")
public class SubscribersEntity {

    @PartitionKey
    @Column(name = "entity_id")
    private String entityId;

    @Column(name = "subscribers")
    private Set<String> subscribers;

    /**
     * Every entity class must have a default constructor according to
     * <a href="http://docs.datastax.com/en/developer/java-driver/2.1/manual/object_mapper/creating/">
     * Definition of mapped classes</a>.
     */
    public SubscribersEntity() {
        // Don't delete! Default constructor is required by DataStax driver
    }

    public SubscribersEntity(String entityId, Set<String> subscribers) {
        this.entityId = entityId;
        this.subscribers = subscribers;
    }

    public String getEntityId() {
        return entityId;
    }

    public void setEntityId(String entityId) {
        this.entityId = entityId;
    }

    public Set<String> getSubscribers() {
        return subscribers;
    }

    public void setSubscribers(Set<String> subscribers) {
        this.subscribers = subscribers;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SubscribersEntity that = (SubscribersEntity) o;
        return Objects.equals(entityId, that.entityId) &&
                Objects.equals(subscribers, that.subscribers);
    }

    @Override
    public int hashCode() {
        return hash(entityId, subscribers);
    }

    @Override
    public String toString() {
        return "SubscribersEntity{" +
                "entityId='" + entityId + '\'' +
                ", subscribers=" + subscribers +
                '}';
    }
}
