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


    public SubscribersEntity() {
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
