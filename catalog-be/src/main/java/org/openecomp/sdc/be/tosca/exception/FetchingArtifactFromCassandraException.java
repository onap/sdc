package org.openecomp.sdc.be.tosca.exception;

import org.openecomp.sdc.be.dao.api.ActionStatus;
import org.openecomp.sdc.be.dao.cassandra.CassandraOperationStatus;

public class FetchingArtifactFromCassandraException extends Exception {

    public final CassandraOperationStatus cassandraOperationStatus;
    public final ActionStatus actionStatus;

    public FetchingArtifactFromCassandraException(CassandraOperationStatus status, ActionStatus actionStatus) {
        this.cassandraOperationStatus = status;
        this.actionStatus = actionStatus;
    }
}
