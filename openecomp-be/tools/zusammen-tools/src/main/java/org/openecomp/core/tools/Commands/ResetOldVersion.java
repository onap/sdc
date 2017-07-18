package org.openecomp.core.tools.Commands;

import com.amdocs.zusammen.datatypes.Id;
import com.amdocs.zusammen.datatypes.SessionContext;
import com.amdocs.zusammen.plugin.statestore.cassandra.dao.impl.VersionCassandraDao;
import org.openecomp.core.tools.store.VersionInfoCassandraLoader;
import org.openecomp.core.tools.store.VspGeneralLoader;
import org.openecomp.core.tools.store.ElementHandler;
import org.openecomp.core.zusammen.impl.CassandraConnectionInitializer;
import org.openecomp.core.zusammen.plugin.dao.types.ElementEntity;
import org.openecomp.sdc.vendorsoftwareproduct.VendorSoftwareProductConstants;
import org.openecomp.sdc.versioning.dao.types.VersionInfoEntity;

import java.util.*;

public class ResetOldVersion {


  public static Map<String, List<String>> itemVersionMap = new HashMap<>();

  public static int count =0;
  public static void reset(SessionContext context, String oldVersion,String emptyOldVersion) {



    CassandraConnectionInitializer.setCassandraConnectionPropertiesToSystem();

    loadItemVersionInfo(context);

    Map<String, ElementEntity> generalElementMap =
        VspGeneralLoader.load(context,
             itemVersionMap);

    generalElementMap.values().forEach(elementEntity -> updateOldVersionFlag(elementEntity,
        oldVersion,"true".equals(emptyOldVersion)));


    itemVersionMap.entrySet().forEach(entry->entry.getValue().stream().filter
        (version->generalElementMap.containsKey(context.getUser().getUserName()+"_"+entry.getKey()
            +"_"+version)).forEach(version->ElementHandler.update(context,
        entry.getKey(),version,generalElementMap.get(context.getUser().getUserName()+"_"+entry.getKey()
            +"_"+version))));

    System.out.println("nymber of element updated:"+count);

  }

  private static void updateOldVersionFlag(ElementEntity elementEntity, String oldVersion,
                                           boolean emptyOldVersion) {

    if(!emptyOldVersion){
      elementEntity.getInfo().addProperty("oldVersion",oldVersion);
      count++;
    }else if(elementEntity.getInfo().getProperty("oldVersion")== null || ""
        .equals(elementEntity.getInfo().getProperty("oldVersion"))){
      elementEntity.getInfo().addProperty("oldVersion",oldVersion);
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
        (versionInfoEntity
            -> items.add(versionInfoEntity.getEntityId()));
    System.setProperty("cassandra.dox.keystore", "zusammen_dox");
    VersionCassandraDao versionCassandraDao = new VersionCassandraDao();

    items
        .forEach(itemId -> versionCassandraDao.list(context, context.getUser().getUserName(), new Id
            (itemId)).forEach(itemVersion -> addItemVersion(itemId, itemVersion.getId())));

  }

  private static void addItemVersion(String itemId, Id versionId) {
    if (!itemVersionMap.containsKey(itemId)) {
      itemVersionMap.put(itemId, new ArrayList<>());
    }
    itemVersionMap.get(itemId).add(versionId.getValue());
  }
}
