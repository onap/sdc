package org.openecomp.core.tools.loaders.zusammen;

import com.amdocs.zusammen.datatypes.Id;
import com.amdocs.zusammen.datatypes.SessionContext;
import com.amdocs.zusammen.plugin.statestore.cassandra.dao.types.ElementEntityContext;
import org.openecomp.core.zusammen.plugin.dao.impl.CassandraElementRepository;
import org.openecomp.core.zusammen.plugin.dao.types.ElementEntity;
import org.openecomp.sdc.vendorsoftwareproduct.dao.impl.zusammen.StructureElement;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class VspGeneralLoader {
  public static Map<String, ElementEntity> load(SessionContext context,
                                                Map<String, List<String>> vspItemVersionsMap) {
    Map<String, ElementEntity> elementEntityMap = new HashMap<>();
    System.setProperty("cassandra.dox.keystore", "zusammen_dox");
    CassandraElementRepository cassandraElementRepository = new CassandraElementRepository();
    for (Map.Entry<String, List<String>> entry : vspItemVersionsMap.entrySet()) {

      for (String version : entry.getValue()) {

        Optional<ElementEntity> result =
            cassandraElementRepository.get(context, new ElementEntityContext(
                    context.getUser().getUserName(),
                    new Id(entry.getKey()),
                    new Id(version)),
                new ElementEntity(new Id(StructureElement.General.name())));
        if (result.isPresent()) {
          elementEntityMap.put(context.getUser().getUserName() + "_" + entry.getKey()
              + "_" + version, result.get());
        }
      }
    }

    return elementEntityMap;
  }
}
