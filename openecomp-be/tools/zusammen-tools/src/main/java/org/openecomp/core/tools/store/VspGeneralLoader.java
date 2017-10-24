package org.openecomp.core.tools.store;

import com.amdocs.zusammen.datatypes.Id;
import com.amdocs.zusammen.datatypes.SessionContext;
import com.amdocs.zusammen.datatypes.item.Info;
import com.amdocs.zusammen.plugin.statestore.cassandra.dao.types.ElementEntityContext;
import org.openecomp.core.zusammen.plugin.dao.impl.CassandraElementRepository;
import org.openecomp.core.zusammen.plugin.dao.types.ElementEntity;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Objects;

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
                GENERAL);
        if (entityId != null) {
          Optional<ElementEntity> result =
                  cassandraElementRepository.get(context, new ElementEntityContext(
                                  context.getUser().getUserName(),
                                  itemId,
                                  changeRefId),
                          new ElementEntity(entityId));
          if (result.isPresent()) {
            elementEntityMap.put(buildKey(context, entry, version), result.get());
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
            elementEntityMap.put(buildKey(context, entry, changeRef), result.get());
          }
        }
      }
    }


    return elementEntityMap;
  }

  public static String buildKey(SessionContext context, Map.Entry<String, List<String>> entry, String version) {
    return String.format("%s_%s_%s", context.getUser().getUserName(), entry.getKey(), version);
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
        Optional<ElementEntity> subElementEntityOptional =
                cassandraElementRepository.get(context, subElementContext,
                        new ElementEntity(subelementId));
        if (subElementEntityOptional.isPresent()) {
          Info info = subElementEntityOptional.get().getInfo();
          if (isValid(name, info)) {
            return false;
          }
          if (NAME.equals(name)) {
            if (value.equals(info.getName())) {
              return true;
            }
          }
          if (value.equals(info.getProperty(name))) {
            return true;
          }
        }
        return false;

      }).findFirst().orElse(null);
    }
    return null;


  }

  private static boolean isValid(String name, Info info) {
    return Objects.isNull(info)|| Objects.isNull(info.getProperty(name));
  }


}
