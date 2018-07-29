/*
 * Copyright © 2016-2018 European Support Limited
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.onap.sdc.activityspec.api.rest.services;

import org.onap.sdc.activityspec.api.rest.ActivitySpecs;
import org.onap.sdc.activityspec.api.rest.mapping.MapActivitySpecRequestDtoToActivitySpecEntity;
import org.onap.sdc.activityspec.api.rest.mapping.MapActivitySpecToActivitySpecCreateResponse;
import org.onap.sdc.activityspec.api.rest.mapping.MapActivitySpecToActivitySpecGetResponse;
import org.onap.sdc.activityspec.api.rest.mapping.MapItemToListResponseDto;
import org.onap.sdc.activityspec.api.rest.types.*;
import org.onap.sdc.activityspec.be.ActivitySpecManager;
import org.onap.sdc.activityspec.be.dao.impl.ActivitySpecDaoZusammenImpl;
import org.onap.sdc.activityspec.be.dao.types.ActivitySpecEntity;
import org.onap.sdc.activityspec.be.impl.ActivitySpecManagerImpl;
import org.openecomp.core.dao.UniqueValueDaoFactory;
import org.openecomp.core.zusammen.api.ZusammenAdaptorFactory;
import org.openecomp.sdc.common.errors.CoreException;
import org.openecomp.sdc.common.errors.ErrorCode;
import org.openecomp.sdc.versioning.ItemManagerFactory;
import org.openecomp.sdc.versioning.VersioningManagerFactory;
import org.openecomp.sdc.versioning.dao.types.Version;
import org.openecomp.sdcrests.wrappers.GenericCollectionWrapper;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import javax.ws.rs.core.Response;

@Service("activitySpecs")
@Validated
public class ActivitySpecsImpl implements ActivitySpecs {


    private final ActivitySpecManager activitySpecManager =
            new ActivitySpecManagerImpl(ItemManagerFactory.getInstance().createInterface(),
                                               VersioningManagerFactory.getInstance().createInterface(),
                                               new ActivitySpecDaoZusammenImpl(ZusammenAdaptorFactory.getInstance()
                                                                                                .createInterface()),
                                               UniqueValueDaoFactory.getInstance().createInterface());

    @Override
    public Response createActivitySpec(ActivitySpecRequestDto request) {
        ActivitySpecEntity activitySpec =
                new MapActivitySpecRequestDtoToActivitySpecEntity().applyMapping(request, ActivitySpecEntity.class);

        activitySpec = activitySpecManager.createActivitySpec(activitySpec);
        ActivitySpecCreateResponse createActivitySpecResponse = new MapActivitySpecToActivitySpecCreateResponse()
                                                                        .applyMapping(activitySpec,
                                                                                ActivitySpecCreateResponse.class);

        return Response.ok(createActivitySpecResponse).build();
    }

    @Override
    public Response getActivitySpec(String activitySpecId, String versionId) {
        ActivitySpecEntity activitySpec = new ActivitySpecEntity();
        activitySpec.setId(activitySpecId);
        activitySpec.setVersion(new Version(versionId));
        final ActivitySpecEntity retrieved = activitySpecManager.get(activitySpec);
        ActivitySpecGetResponse getResponse =
                new MapActivitySpecToActivitySpecGetResponse().applyMapping(retrieved, ActivitySpecGetResponse.class);
        return Response.ok(getResponse).build();
    }

    @Override
    public Response updateActivitySpec(ActivitySpecRequestDto request, String activitySpecId, String versionId) {
        ActivitySpecEntity activitySpec =
                new MapActivitySpecRequestDtoToActivitySpecEntity().applyMapping(request, ActivitySpecEntity.class);

        activitySpec.setId(activitySpecId);
        activitySpec.setVersion(new Version(versionId));

        activitySpecManager.update(activitySpec);

        return Response.ok(new InternalEmptyObject()).build();
    }

    @Override
    public Response actOnActivitySpec(ActivitySpecActionRequestDto request, String activitySpecId, String versionId) {
        if (request.getAction() == null) {
            throw new CoreException(new ErrorCode.ErrorCodeBuilder().withMessage("Mandatory action field is missing")
                                                                    .build());
        }
        activitySpecManager.actOnAction(activitySpecId, versionId, request.getAction());
        return Response.ok(new InternalEmptyObject()).build();
    }

    @Override
    public Response list(String versionStatus) {

        GenericCollectionWrapper<ActivitySpecListResponseDto> results = new GenericCollectionWrapper<>();
        MapItemToListResponseDto mapper = new MapItemToListResponseDto();
        activitySpecManager.list(versionStatus).stream()
                           .sorted((o1, o2) -> o2.getModificationTime().compareTo(o1.getModificationTime())).forEach(
                               activitySpecItem -> results.add(mapper.applyMapping(activitySpecItem,
                                      ActivitySpecListResponseDto.class)));

        return Response.ok(results).build();
    }
}
