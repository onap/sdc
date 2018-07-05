/*
* Copyright © 2016-2018 European Support Limited
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
*      http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/

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
  public static void populateHealingTableByItemVersion (String itemId, String versionId, String oldVersion) {
      VersionCassandraLoader versionCassandraLoader = new VersionCassandraLoader();
      ResultSet listItemVersion = versionCassandraLoader.listItemVersion();

      ArrayList<HealingEntity> healingEntities = new ArrayList<>();

      listItemVersion.iterator().forEachRemaining(entry -> {
          if (entry.getString("item_id").equals(itemId) && entry.getString("version_id").equals(versionId)) {
              healingEntities.add(new HealingEntity(entry.getString("space"), entry.getString("item_id"),
                      entry.getString("version_id"), true, oldVersion));
          }
      });

      HealingHandler healingHandler = new HealingHandler();
      healingHandler.populateHealingTable(healingEntities);

  }
}
