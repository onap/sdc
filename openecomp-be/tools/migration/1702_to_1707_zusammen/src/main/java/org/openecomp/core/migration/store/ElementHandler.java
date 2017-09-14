package org.openecomp.core.migration.store;

import com.amdocs.zusammen.datatypes.Id;
import com.amdocs.zusammen.datatypes.Namespace;
import com.amdocs.zusammen.datatypes.SessionContext;
import com.amdocs.zusammen.datatypes.item.Info;
import com.amdocs.zusammen.datatypes.item.Relation;
import com.amdocs.zusammen.plugin.statestore.cassandra.dao.types.ElementEntityContext;
import com.amdocs.zusammen.sdk.collaboration.types.CollaborationElement;
import org.openecomp.core.migration.MigrationMain;
import org.openecomp.core.model.types.ServiceTemplate;
import org.openecomp.core.zusammen.plugin.ZusammenPluginUtil;
import org.openecomp.core.zusammen.plugin.dao.impl.CassandraElementRepository;
import org.openecomp.core.zusammen.plugin.dao.types.ElementEntity;
import org.openecomp.sdc.versioning.dao.types.Version;
import org.openecomp.sdc.versioning.dao.types.VersionInfoEntity;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ElementHandler {

  private static final String GLOBAL_USER = "GLOBAL_USER";

  public static void save(SessionContext context,
                          CassandraElementRepository cassandraElementRepository,
                          String itemId, Version versionId,
                          CollaborationElement[] elements) {

    ElementEntityContext elementContext;
    for (CollaborationElement element : elements) {

      elementContext = new ElementEntityContext(GLOBAL_USER, new Id(itemId), getVersionId(itemId,
          versionId));
      ElementEntity elementEntity = ZusammenPluginUtil.getElementEntity(element);

      cassandraElementRepository.createNamespace(context, elementContext, elementEntity);

      cassandraElementRepository.create(context, elementContext, elementEntity);

      if (isActiveVersion(itemId, versionId)) {
        elementContext =
            new ElementEntityContext(GLOBAL_USER, new Id(itemId), new Id(versionId.toString()));
        cassandraElementRepository.create(context, elementContext, elementEntity);

      }
    }
  }

  public static CollaborationElement getElementEntity(String itemId,
                                                      String versionId,
                                                      String elementId,
                                                      List<String> elementPath,
                                                      Info info,
                                                      Collection<Relation> relations,
                                                      List<String> subElements,
                                                      byte[] data) {
    Namespace namespace = new Namespace();
    for (String pathElementId : elementPath) {
      namespace = new Namespace(namespace, new Id(pathElementId));
    }
    if (namespace.getValue() == null || namespace.getValue().equals("")) {
      namespace = Namespace.ROOT_NAMESPACE;
    }
    CollaborationElement elementEntity = new CollaborationElement(new Id(itemId), new Id(versionId),
        namespace, new Id(elementId));

    Id parentId = namespace.getParentElementId() != null ? namespace.getParentElementId() : Id.ZERO;
    elementEntity.setParentId(parentId);
    elementEntity.setInfo(info);
    elementEntity.setRelations(relations);
    if (subElements != null) {
      Set<Id> subElementSet = new HashSet<>();

      subElements.forEach(subElement -> subElementSet.add(new Id(subElement)));

    }
    if (data != null) {
      elementEntity.setData(new ByteArrayInputStream(data));
    }

    return elementEntity;
  }

  public static List<String> getElementPath(String... paths) {
    List<String> pathList = new ArrayList<>();
    if (paths != null) {
      Collections.addAll(pathList, paths);
    }
    return pathList;
  }

  public static Info getStructuralElementInfo(String elementName) {
    Info info = new Info();
    info.setName(elementName);
    return info;
  }


  private static Id getVersionId(String itemId, Version versionId) {
    VersionInfoEntity versionInfo =
        MigrationMain.getVersionInfoMap().get(itemId);
    if (versionInfo == null) {
      return new Id(versionId.toString());
    }
    Version lastVersion = versionInfo.getCandidate() != null ? versionInfo.getCandidate()
        .getVersion()
        : versionInfo.getActiveVersion();

    if (lastVersion.equals(versionId)) {
      return new Id(itemId);
    } else {
      return new Id(versionId.toString());
    }
  }

  private static boolean isActiveVersion(String itemId, Version versionId) {
    VersionInfoEntity versionInfo =
        MigrationMain.getVersionInfoMap().get(itemId);
    return versionInfo != null && versionInfo.getActiveVersion().equals(versionId);
  }


  public static Info getServiceModelElementInfo(String vspServiceModelEntityId,
                                                ServiceTemplate serviceTemplate) {
    Info info = ElementHandler.getStructuralElementInfo(vspServiceModelEntityId);
    info.addProperty("base", serviceTemplate.getBaseName());
    return info;

  }
}