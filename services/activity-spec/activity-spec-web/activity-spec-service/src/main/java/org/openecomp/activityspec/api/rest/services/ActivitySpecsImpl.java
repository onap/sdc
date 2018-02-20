package org.openecomp.activityspec.api.rest.services;

import org.openecomp.activityspec.api.rest.ActivitySpecs;
import org.openecomp.activityspec.api.rest.mapping.MapActivitySpecRequestDtoToActivitySpecEntity;
import org.openecomp.activityspec.api.rest.mapping.MapActivitySpecToActivitySpecCreateResponse;
import org.openecomp.activityspec.api.rest.mapping.MapActivitySpecToActivitySpecGetResponse;
import org.openecomp.activityspec.api.rest.mapping.MapItemToListResponseDto;
import org.openecomp.activityspec.api.rest.types.ActivitySpecActionRequestDto;
import org.openecomp.activityspec.api.rest.types.ActivitySpecCreateResponse;
import org.openecomp.activityspec.api.rest.types.ActivitySpecGetResponse;
import org.openecomp.activityspec.api.rest.types.ActivitySpecListResponseDto;
import org.openecomp.activityspec.api.rest.types.ActivitySpecRequestDto;
import org.openecomp.activityspec.be.ActivitySpecManager;
import org.openecomp.activityspec.be.dao.impl.ActivitySpecDaoZusammenImpl;
import org.openecomp.activityspec.be.dao.types.ActivitySpecEntity;
import org.openecomp.activityspec.be.impl.ActivitySpecManagerImpl;
import org.openecomp.core.dao.UniqueValueDaoFactory;
import org.openecomp.core.zusammen.api.ZusammenAdaptorFactory;
import org.openecomp.sdc.versioning.ItemManagerFactory;
import org.openecomp.sdc.versioning.VersioningManagerFactory;
import org.openecomp.sdc.versioning.dao.types.Version;

import org.openecomp.sdcrests.wrappers.GenericCollectionWrapper;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import javax.inject.Named;
import javax.ws.rs.core.Response;

@Named
@Service("activitySpecs")
@Scope(value = "prototype")
@Validated
public class ActivitySpecsImpl implements ActivitySpecs {


  private final ActivitySpecManager activitySpecManager =
      new ActivitySpecManagerImpl(ItemManagerFactory.getInstance().createInterface(),
          VersioningManagerFactory.getInstance().createInterface(),
          new ActivitySpecDaoZusammenImpl(ZusammenAdaptorFactory.getInstance().createInterface()),
          UniqueValueDaoFactory.getInstance().createInterface());

  @Override
  public Response createActivitySpec(ActivitySpecRequestDto request, String user) {
    ActivitySpecEntity activitySpec = new MapActivitySpecRequestDtoToActivitySpecEntity()
        .applyMapping(request, ActivitySpecEntity.class);

    activitySpec = activitySpecManager.createActivitySpec(activitySpec);
    ActivitySpecCreateResponse createActivitySpecResponse =
        new MapActivitySpecToActivitySpecCreateResponse().applyMapping(activitySpec,
            ActivitySpecCreateResponse.class);

    return Response.ok(createActivitySpecResponse).build();
  }

  @Override
  public Response getActivitySpec(String activitySpecId, String versionId, String user) {
    ActivitySpecEntity activitySpec = new ActivitySpecEntity();
    activitySpec.setId(activitySpecId);
    activitySpec.setVersion(new Version(versionId));
    final ActivitySpecEntity retrieved = activitySpecManager.get(activitySpec);
    ActivitySpecGetResponse getResponse = new MapActivitySpecToActivitySpecGetResponse()
        .applyMapping(retrieved, ActivitySpecGetResponse.class);
    return Response.ok(getResponse).build();
  }

  @Override
  public Response updateActivitySpec(ActivitySpecRequestDto request, String activitySpecId,
                                     String versionId, String user) {
    ActivitySpecEntity activitySpec = new MapActivitySpecRequestDtoToActivitySpecEntity()
        .applyMapping(request, ActivitySpecEntity.class);

    activitySpec.setId(activitySpecId);
    activitySpec.setVersion(new Version(versionId));

    activitySpecManager.update(activitySpec);

    return Response.ok().build();
  }

  @Override
  public Response actOnActivitySpec(ActivitySpecActionRequestDto request, String activitySpecId,
                                    String versionId, String user) {
    activitySpecManager.actOnAction(activitySpecId, versionId, request.getAction());
    return Response.ok().build();
  }

  @Override
  public Response list(String versionStatus, String user) {

    GenericCollectionWrapper<ActivitySpecListResponseDto> results = new GenericCollectionWrapper<>();
    MapItemToListResponseDto mapper = new MapItemToListResponseDto();
    activitySpecManager.list(versionStatus).stream()
        .sorted((o1, o2) -> o2.getModificationTime().compareTo(o1.getModificationTime()))
        .forEach(activitySpecItem -> results.add(mapper.applyMapping(activitySpecItem,
            ActivitySpecListResponseDto.class)));

    return Response.ok(results).build();
  }

}
