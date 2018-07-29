package org.openecomp.sdc.be.components.path.beans;

import fj.data.Either;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.openecomp.sdc.be.dao.api.ActionStatus;
import org.openecomp.sdc.be.dao.cassandra.CassandraOperationStatus;
import org.openecomp.sdc.be.dao.cassandra.ComponentCacheAccessor;
import org.openecomp.sdc.be.dao.cassandra.ComponentCassandraDao;
import org.openecomp.sdc.be.resources.data.ComponentCacheData;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Component("component-cassandra-dao")
public class ComponentCassandraDaoMock  extends ComponentCassandraDao {

        public static Integer DEFAULT_FETCH_SIZE = 500;
        private ComponentCacheAccessor componentCacheAccessor;

        public ComponentCassandraDaoMock() {
            super();
        }

        @PostConstruct
        public void init() {

        }

        public Either<List<ComponentCacheData>, ActionStatus> getComponents(List<String> ids) {

            return null;
        }

        public Either<List<ComponentCacheData>, ActionStatus> getAllComponentIdTimeAndType() {
            return null;
        }


        public Either<ComponentCacheData, ActionStatus> getComponent(String id) {

            return null;
        }

        public CassandraOperationStatus saveComponent(ComponentCacheData componentCacheData) {
            return null;
           }


        public Either<Boolean, CassandraOperationStatus> isTableEmpty(String tableName) {
            return null;
        }


        public Either<ImmutablePair<List<ComponentCacheData>, Set<String>>, ActionStatus> getComponents(
                Map<String, Long> idToTimestampMap) {

            return null;
        }

        public CassandraOperationStatus deleteComponent(String id) {
            return null;
        }

    }

