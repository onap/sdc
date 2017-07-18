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
  private static CassandraElementRepository cassandraElementRepository =
      new CassandraElementRepository();

  public static Map<String, ElementEntity> load(SessionContext context,
                                                Map<String, List<String>> vspItemVersionsMap) {
    Map<String, ElementEntity> elementEntityMap = new HashMap<>();
    System.setProperty("cassandra.dox.keystore", "zusammen_dox");

    Id entityId;
    Id itemId;
    Id versionId;
    for (Map.Entry<String, List<String>> entry : vspItemVersionsMap.entrySet()) {

      for (String version : entry.getValue()) {


        itemId = new Id(entry.getKey());
        versionId = new Id(version);
        entityId = getEntityIdByInfoNameValue(context, itemId, versionId, null, "name",
            "General");
        if (entityId != null) {
          Optional<ElementEntity> result =
              cassandraElementRepository.get(context, new ElementEntityContext(
                      context.getUser().getUserName(),
                      itemId,
                      versionId),
                  new ElementEntity(entityId));
          if (result.isPresent()) {
            elementEntityMap.put(context.getUser().getUserName() + "_" + entry.getKey()
                + "_" + version, result.get());
          }
        }
      }
    }

    return elementEntityMap;
  }

  private static Id getEntityIdByInfoNameValue(SessionContext context, Id itemId, Id versionId,
                                               Id elementId, String
                                                       name, String value) {

    Id id;
    Optional<ElementEntity> result =
        cassandraElementRepository.get(context, new ElementEntityContext(
                context.getUser().getUserName(),
                itemId,
                versionId),
            new ElementEntity(Id.ZERO));
    if (result.isPresent()) {
      ElementEntity elementEntity = result.get();
      return elementEntity.getSubElementIds().stream().filter(subelementId -> {
        Optional<ElementEntity> subElementEntity =
            cassandraElementRepository.get(context, new ElementEntityContext(
                    context.getUser().getUserName(),
                    itemId,
                    versionId),
                new ElementEntity(subelementId));
        if (subElementEntity.isPresent()) {
          if("name".equals(name)){
            if(value.equals(subElementEntity.get().getInfo().getName())){
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
