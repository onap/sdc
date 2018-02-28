package org.openecomp.sdc.be.components.path.beans;

import fj.data.Either;
import org.openecomp.sdc.be.components.path.ForwardingPathValidator;
import org.openecomp.sdc.be.datatypes.elements.ForwardingPathDataDefinition;
import org.openecomp.sdc.exception.ResponseFormat;

import java.util.Collection;

public class ForwardingPathValidatorMock extends ForwardingPathValidator {
    @Override
    public Either<Boolean, ResponseFormat> validateForwardingPaths(Collection<ForwardingPathDataDefinition> paths, String serviceId,
                                                                   boolean isUpdate) {
        return Either.left(Boolean.TRUE);
    }
}
