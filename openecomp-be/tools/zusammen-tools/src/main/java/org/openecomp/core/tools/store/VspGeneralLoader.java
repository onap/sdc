package org.openecomp.core.tools.store;

import com.amdocs.zusammen.datatypes.Id;
import com.amdocs.zusammen.datatypes.SessionContext;
import com.amdocs.zusammen.plugin.statestore.cassandra.dao.types.ElementEntityContext;
import org.openecomp.core.zusammen.plugin.dao.impl.CassandraElementRepository;
import org.openecomp.core.zusammen.plugin.dao.types.ElementEntity;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class VspGeneralLoader {
  public static final String NAME = "name";
  public static final String GENERAL = "General";
  private static CassandraElementRepository cassandraElementRepository =
          new CassandraElementRepository();

  public static Map<String, ElementEntity> load(SessionContext context,
                                                Map<String, List<String>> vspItemVersionsMap,
                                                Map<String, List<String>> vspItemChangeRefssMap) {
    Map<String, ElementEntity> elementEntityMap = new HashMap<>();
    System.setProperty("cassandra.dox.keystore", "zusammen_dox");

    Id entityId;
    Id itemId;
    Id changeRefId;
    for (Map.Entry<String, List<String>> entry : vspItemVersionsMap.entrySet()) {

      for (String version : entry.getValue()) {


        itemId = new Id(entry.getKey());
        changeRefId = new Id(version);
        entityId = getEntityIdByInfoNameValue(context, itemId, changeRefId, null, null, NAME,
                "General");
        if (entityId != null) {
          Optional<ElementEntity> result =
                  cassandraElementRepository.get(context, new ElementEntityContext(
                                  context.getUser().getUserName(),
                                  itemId,
                                  changeRefId),
                          new ElementEntity(entityId));
          if (result.isPresent()) {
            elementEntityMap.put(context.getUser().getUserName() + "_" + entry.getKey()
                    + "_" + version, result.get());
          }
        }
      }
    }


    for (Map.Entry<String, List<String>> entry : vspItemChangeRefssMap.entrySet()) {

      for (String changeRef : entry.getValue()) {


        itemId = new Id(entry.getKey());

        entityId = getEntityIdByInfoNameValue(context, itemId, null, changeRef,null, NAME,
                GENERAL);
        if (entityId != null) {
          ElementEntityContext elementContext = new ElementEntityContext(
                  context.getUser().getUserName(),
                  itemId,
                  null);
          elementContext.setChangeRef(changeRef);
          Optional<ElementEntity> result =
                  cassandraElementRepository.get(context, elementContext,
                          new ElementEntity(entityId));
          if (result.isPresent()) {
            elementEntityMap.put(context.getUser().getUserName() + "_" + entry.getKey()
                    + "_" + changeRef, result.get());
          }
        }
      }
    }


    return elementEntityMap;
  }

  private static Id getEntityIdByInfoNameValue(SessionContext context,
                                               Id itemId,
                                               Id versionId,
                                               String changeRef,
                                               Id elementId,
                                               String name,
                                               String value) {


    ElementEntityContext elementContext = new ElementEntityContext(
            context.getUser().getUserName(),
            itemId,
            versionId);
    if (changeRef != null) {
      elementContext.setChangeRef(changeRef);
    }
    Optional<ElementEntity> result =
            cassandraElementRepository.get(context, elementContext,
                    new ElementEntity(Id.ZERO));
    if (result.isPresent()) {
      ElementEntity elementEntity = result.get();
      return elementEntity.getSubElementIds().stream().filter(subelementId -> {
        ElementEntityContext subElementContext = new ElementEntityContext(
                context.getUser().getUserName(),
                itemId,
                versionId);
        if(changeRef!= null){
          subElementContext.setChangeRef(changeRef);
        }
        Optional<ElementEntity> subElementEntity =
                cassandraElementRepository.get(context, subElementContext,
                        new ElementEntity(subelementId));
        if (subElementEntity.isPresent()) {
          if (NAME.equals(name)) {
            if (value.equals(subElementEntity.get().getInfo().getName())) {
              return true;
            }
          }
          if (value.equals(subElementEntity.get().getInfo().getProperty(name))) {
            return true;
          }
        }
        return false;

      }).findFirst().orElse(null);
    }
    return null;


  }
}
