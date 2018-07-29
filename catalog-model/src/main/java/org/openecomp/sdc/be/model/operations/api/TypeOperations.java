package org.openecomp.sdc.be.model.operations.api;

import fj.data.Either;
import org.openecomp.sdc.be.datatypes.tosca.ToscaDataDefinition;

import javax.validation.constraints.NotNull;

public interface TypeOperations<T extends ToscaDataDefinition> {

    T addType(T newTypeDefinition);

    T getType(String uniqueId);

    T getLatestType(String uniqueId);

    boolean isSameType(@NotNull T type1,@NotNull T type2);

    T updateType(T currentTypeDefinition, T newTypeDefinition);
    
    static <T> Either<T, StorageOperationStatus> mapOkStatus(StorageOperationStatus status, T obj) {
        return status != StorageOperationStatus.OK? Either.right(status) : Either.left(obj);
    }

}
