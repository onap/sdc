package org.openecomp.core.tools.commands;

import com.datastax.driver.core.ResultSet;
import org.openecomp.core.tools.store.HealingHandler;
import org.openecomp.core.tools.store.VersionCassandraLoader;
import org.openecomp.core.tools.store.zusammen.datatypes.HealingEntity;

import java.util.ArrayList;


/**
 * Created by ayalaben on 10/15/2017
 */
public class SetHealingFlag {


  private SetHealingFlag(){}

  public static void populateHealingTable(String oldVersion) {

    VersionCassandraLoader versionCassandraLoader = new VersionCassandraLoader();
    ResultSet listItemVersion = versionCassandraLoader.listItemVersion();

    ArrayList<HealingEntity> healingEntities = new ArrayList<>();

    listItemVersion.iterator().forEachRemaining(entry -> healingEntities.add(new HealingEntity
        (entry.getString(0),entry.getString(1),entry.getString(2),true,oldVersion)));

    HealingHandler healingHandler = new HealingHandler();
    healingHandler.populateHealingTable(healingEntities);

  }
}
