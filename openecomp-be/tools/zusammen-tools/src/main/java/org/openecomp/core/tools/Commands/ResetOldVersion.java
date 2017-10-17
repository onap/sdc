package org.openecomp.core.tools.Commands;

import com.amdocs.zusammen.datatypes.Id;
import com.amdocs.zusammen.datatypes.SessionContext;
import com.amdocs.zusammen.plugin.statestore.cassandra.dao.impl.VersionCassandraDao;
import com.google.common.collect.Sets;
import org.openecomp.core.tools.store.ElementHandler;
import org.openecomp.core.tools.store.VersionInfoCassandraLoader;
import org.openecomp.core.tools.store.VspGeneralLoader;
import org.openecomp.core.zusammen.impl.CassandraConnectionInitializer;
import org.openecomp.core.zusammen.plugin.dao.types.ElementEntity;
import org.openecomp.sdc.vendorsoftwareproduct.VendorSoftwareProductConstants;
import org.openecomp.sdc.versioning.dao.types.Version;
import org.openecomp.sdc.versioning.dao.types.VersionInfoEntity;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class ResetOldVersion {


  public static Map<String, List<String>> itemVersionMap = new HashMap<>();
  public static Map<String, List<String>> itemChangeRefMap = new HashMap<>();

  public static int count = 0;

  public static void reset(SessionContext context, String oldVersion, String emptyOldVersion) {


    CassandraConnectionInitializer.setCassandraConnectionPropertiesToSystem();

    loadItemVersionInfo(context);

    Map<String, ElementEntity> generalElementMap =
            VspGeneralLoader.load(context,
                    itemVersionMap,itemChangeRefMap);

    generalElementMap.values().forEach(elementEntity -> updateOldVersionFlag(elementEntity,
            oldVersion, "true".equals(emptyOldVersion)));


    itemVersionMap.entrySet().forEach(entry -> entry.getValue().stream().filter
            (version -> generalElementMap
                    .containsKey(context.getUser().getUserName() + "_" + entry.getKey()
                            + "_" + version)).forEach(version -> ElementHandler.update(context,
            entry.getKey(), version,null,
            generalElementMap.get(context.getUser().getUserName() + "_" + entry.getKey()
                    + "_" + version))));

    itemChangeRefMap.entrySet().forEach(entry -> entry.getValue().stream().filter
            (changeRef -> generalElementMap
                    .containsKey(context.getUser().getUserName() + "_" + entry.getKey()
                            + "_" + changeRef)).forEach(changeref -> ElementHandler.update(context,
            entry.getKey(),changeref, changeref,
            generalElementMap.get(context.getUser().getUserName() + "_" + entry.getKey()
                    + "_" + changeref))));

    System.out.println("number of element updated:" + count);

  }

  private static void updateOldVersionFlag(ElementEntity elementEntity, String oldVersion,
                                           boolean emptyOldVersion) {

    if (!emptyOldVersion) {
      elementEntity.getInfo().addProperty("oldVersion", oldVersion);
      count++;
    } else if (elementEntity.getInfo().getProperty("oldVersion") == null || ""
            .equals(elementEntity.getInfo().getProperty("oldVersion"))) {
      elementEntity.getInfo().addProperty("oldVersion", oldVersion);
      count++;
    }


  }


  private static void loadItemVersionInfo(SessionContext context) {

    List<String> items = new ArrayList<>();
    System.setProperty("cassandra.dox.keystore", "dox");
    VersionInfoCassandraLoader versionInfoCassandraLoader = new VersionInfoCassandraLoader();
    Collection<VersionInfoEntity> versions =
            versionInfoCassandraLoader.list();

    versions.stream().filter(versionInfoEntity -> versionInfoEntity.getEntityType().equals
            (VendorSoftwareProductConstants.VENDOR_SOFTWARE_PRODUCT_VERSIONABLE_TYPE)).forEach
            (versionInfoEntity -> {
              items.add(versionInfoEntity.getEntityId());
              Set<Version> viewableVersions;
              if (versionInfoEntity.getViewableVersions()!= null && !versionInfoEntity.getViewableVersions().isEmpty()){
                viewableVersions = versionInfoEntity.getViewableVersions();
              } else {
                viewableVersions = Sets.newHashSet(versionInfoEntity.getActiveVersion());
              }
              addItemchangeRef(versionInfoEntity.getEntityId(),maxChangeRef(viewableVersions));
            });
    System.setProperty("cassandra.dox.keystore", "zusammen_dox");
    VersionCassandraDao versionCassandraDao = new VersionCassandraDao();

    items.forEach(itemId -> versionCassandraDao.list(context, context.getUser().getUserName(),
            new Id(itemId)).forEach(itemVersion -> addItemVersion(itemId, itemVersion.getId())));

  }

  private static Id maxChangeRef(Set<Version> viewableVersions) {

    return new Id(viewableVersions.stream().max((o1, o2) -> o1.getMajor() > o2.getMajor()
            ?1:o1.getMajor
            () == o2.getMajor() ? (o1.getMinor() > o2.getMinor() ? 1: o1.getMinor() == o2.getMinor()
            ? 0 : -1) : -1).get().toString());

  }

  private static void addItemchangeRef(String itemId, Id changeRef) {
    addItemVersion(itemChangeRefMap,itemId,changeRef);
  }

  private static void addItemVersion(String itemId, Id versionId) {
    addItemVersion(itemVersionMap,itemId,versionId);
  }

  private static void addItemVersion(Map<String,List<String>> itemVersions,String itemId, Id
          id) {
    if (!itemVersions.containsKey(itemId)) {
      itemVersions.put(itemId, new ArrayList<>());
    }
    itemVersions.get(itemId).add(id.getValue());
  }
}

