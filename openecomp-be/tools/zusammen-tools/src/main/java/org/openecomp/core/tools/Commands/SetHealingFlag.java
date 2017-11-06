package org.openecomp.core.tools.Commands;


import com.datastax.driver.core.ResultSet;
import org.openecomp.core.tools.store.HealingHandler;
import org.openecomp.core.tools.store.VersionCassandraLoader;
import org.openecomp.core.tools.store.VersionInfoCassandraLoader;
import org.openecomp.core.tools.store.zusammen.datatypes.HealingEntity;
import org.openecomp.sdc.versioning.dao.types.VersionInfoEntity;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Objects;

/**
 * Created by ayalaben on 11/5/2017
 */
public class SetHealingFlag {

  public static void populateHealingTable(String oldVersion) {


    VersionInfoCassandraLoader versionInfoCassandraLoader = new VersionInfoCassandraLoader();
    Collection<VersionInfoEntity> versionInfoEntities = versionInfoCassandraLoader.list();

    ArrayList<HealingEntity> healingEntities = new ArrayList<HealingEntity>();

    versionInfoEntities.forEach(versionInfoEntity -> healingEntities.add(new HealingEntity
        ("GLOBAL_USER",versionInfoEntity.getEntityId(),versionInfoEntity.getActiveVersion()
            .toString(),true,oldVersion)));

    versionInfoEntities.forEach(versionInfoEntity -> {
      if (!Objects.isNull(versionInfoEntity.getCandidate())) {
        healingEntities.add(new HealingEntity
            ("GLOBAL_USER",versionInfoEntity.getEntityId(),versionInfoEntity.getCandidate()
                .getVersion().toString(),
                true,oldVersion));
      }
    });


    versionInfoEntities.forEach(versionInfoEntity -> versionInfoEntity.getViewableVersions()
        .forEach(version -> healingEntities.add(new HealingEntity
            ("GLOBAL_USER",versionInfoEntity.getEntityId(),version.toString(),
                true,oldVersion))));

    HealingHandler healingHandler = new HealingHandler();
    healingHandler.populateHealingTable(healingEntities);

  }
}
