package org.openecomp.core.migration.store;

import com.amdocs.zusammen.datatypes.Id;
import com.amdocs.zusammen.datatypes.Namespace;
import com.amdocs.zusammen.datatypes.SessionContext;
import com.amdocs.zusammen.datatypes.item.Info;
import com.amdocs.zusammen.datatypes.item.ItemVersionData;
import com.amdocs.zusammen.datatypes.item.Relation;
import com.amdocs.zusammen.plugin.statestore.cassandra.dao.impl.ItemCassandraDao;
import com.amdocs.zusammen.plugin.statestore.cassandra.dao.impl.VersionCassandraDao;
import com.amdocs.zusammen.plugin.statestore.cassandra.dao.types.ElementEntity;

import org.openecomp.core.migration.MigrationMain;
import org.openecomp.sdc.vendorlicense.dao.types.VendorLicenseModelEntity;
import org.openecomp.sdc.versioning.dao.types.Version;
import org.openecomp.sdc.versioning.dao.types.VersionInfoEntity;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ItemHandler {

    public static ElementEntity getElementEntity(String id, List<String>
      elementPath, Info info, Collection<Relation> relations, List<String> subElements,
                                                byte[] data) {
    ElementEntity elementEntity = new ElementEntity(new Id(id));

    Namespace namespace = new Namespace();
    for (String elementId : elementPath) {
      namespace = new Namespace(namespace, new Id(elementId));
    }
    elementEntity.setNamespace(namespace);
    elementEntity.setParentId(namespace.getParentElementId());
    elementEntity.setInfo(info);
    elementEntity.setRelations(relations);
    if(subElements!=null) {
      Set<Id> subElementSet = new HashSet<>();

      subElements.forEach(subElement -> subElementSet.add(new Id(subElement)));

      elementEntity.setSubElementIds(subElementSet);
    }

    //elementEntity.setData(new ByteArrayInputStream(data));
    return elementEntity;
  }

  public static List<String> getElementPath(String... paths) {
    List<String> pathList = new ArrayList<>();
    for (String path : paths) {
      pathList.add(path);
    }
    return pathList;
  }

  public static Info getStractualElementInfo(String elementName) {
    Info info = new Info();
    info.setName(elementName);
    return info;
  }

  public static void save(SessionContext context, ItemCassandraDao itemCassandraDao,
                          VersionCassandraDao versionCassandraDao,
                          String itemId, Version versionId, Info info,
                          ItemVersionData itemVersionData, Long writetimeMicroSeconds) {

    Date date = writetimeMicroSeconds==null?new Date():new Date(writetimeMicroSeconds);
    itemCassandraDao.create(context,new Id(itemId),info,date);

    versionCassandraDao.create(context, context.getUser().getUserName(),new Id(itemId),
        null,getVersionId(itemId,versionId),itemVersionData,date);

    if(isActiveVersion(itemId,versionId)){
      versionCassandraDao.create(context, context.getUser().getUserName(),new Id(itemId),
          null,new Id(versionId.toString()),itemVersionData,date);
    }
  }



  private static Info getInfo(VendorLicenseModelEntity vlmEntity) {
    Info info = new Info();

    info.setName(vlmEntity.getVendorName());
    info.setDescription(vlmEntity.getDescription());
    info.addProperty("iconRef",vlmEntity.getIconRef());
    info.addProperty("type",vlmEntity.getIconRef());
    return info;
  }

  private static Id getVersionId(String itemId, Version versionId) {
    VersionInfoEntity versionInfo =
        MigrationMain.versionInfoMap.get(itemId);
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
        MigrationMain.versionInfoMap.get(itemId);
    if (versionInfo == null) {
      return false;
    }

    return versionInfo.getActiveVersion().equals(versionId);
  }


}