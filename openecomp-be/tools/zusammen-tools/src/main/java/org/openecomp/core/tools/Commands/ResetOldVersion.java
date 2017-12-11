/*
 * Copyright © 2016-2017 European Support Limited
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

package org.openecomp.core.tools.Commands;

import com.amdocs.zusammen.datatypes.Id;
import com.amdocs.zusammen.datatypes.SessionContext;
import com.amdocs.zusammen.plugin.statestore.cassandra.dao.impl.VersionCassandraDao;
import com.google.common.collect.Sets;
import org.apache.commons.lang3.StringUtils;
import org.openecomp.core.tools.store.ElementHandler;
import org.openecomp.core.tools.store.VersionInfoCassandraLoader;
import org.openecomp.core.tools.store.VspGeneralLoader;
import org.openecomp.core.zusammen.impl.CassandraConnectionInitializer;
import org.openecomp.core.zusammen.plugin.dao.types.ElementEntity;
import org.openecomp.sdc.logging.api.Logger;
import org.openecomp.sdc.logging.api.LoggerFactory;
import org.openecomp.sdc.vendorsoftwareproduct.VendorSoftwareProductConstants;
import org.openecomp.sdc.versioning.dao.types.Version;
import org.openecomp.sdc.versioning.dao.types.VersionInfoEntity;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static org.openecomp.core.tools.store.VspGeneralLoader.buildKey;

public class ResetOldVersion {


  private static final String OLD_VERSION = "oldVersion";

  private static final Logger LOGGER = LoggerFactory.getLogger(ResetOldVersion.class);
  private static final String CASSANDRA_DOX_KEYSTORE = "cassandra.dox.keystore";
  private static int count = 0;

  private ResetOldVersion() {
  }

  public static void reset(SessionContext context, String oldVersion, String emptyOldVersion) {
    Map<String, List<String>> itemVersionMap = new HashMap<>();
    Map<String, List<String>> itemChangeRefMap = new HashMap<>();

    CassandraConnectionInitializer.setCassandraConnectionPropertiesToSystem();

    loadItemVersionInfo(context, itemChangeRefMap, itemVersionMap);

    Map<String, ElementEntity> generalElementMap =
        VspGeneralLoader.load(context, itemVersionMap, itemChangeRefMap);

    generalElementMap.values().forEach(elementEntity -> updateOldVersionFlag(elementEntity,
        oldVersion, Boolean.TRUE.toString().equals(emptyOldVersion)));


    itemVersionMap.entrySet().forEach(entry -> updateElements(context, generalElementMap, entry));

    itemChangeRefMap.entrySet().forEach(entry -> updateElements(context, generalElementMap, entry));
    LOGGER.info("number of element updated:" + count);
  }

  private static void updateElements(SessionContext context, Map<String,
      ElementEntity> generalElementMap, Map.Entry<String, List<String>> entry) {

    entry.getValue().stream()
        .filter(changeRef -> generalElementMap.containsKey(buildKey(context, entry, changeRef)))
        .forEach(changeref -> ElementHandler.update(context, entry.getKey(), changeref, changeref,
            generalElementMap.get(buildKey(context, entry, changeref))));

  }

  private static void updateOldVersionFlag(ElementEntity elementEntity, String oldVersion,
                                           boolean emptyOldVersion) {

    if (!emptyOldVersion
        || StringUtils.isBlank(elementEntity.getInfo().getProperty(OLD_VERSION))) {
      elementEntity.getInfo().addProperty(OLD_VERSION, oldVersion);
      count++;
    }
  }

  private static void loadItemVersionInfo(SessionContext context,
                                          Map<String, List<String>> itemChangeRefMap,
                                          Map<String, List<String>> itemVersionMap) {

    List<String> items = new ArrayList<>();
    System.setProperty(CASSANDRA_DOX_KEYSTORE, "dox");
    VersionInfoCassandraLoader versionInfoCassandraLoader = new VersionInfoCassandraLoader();
    Collection<VersionInfoEntity> versions = versionInfoCassandraLoader.list();

    versions.stream().filter(versionInfoEntity -> versionInfoEntity.getEntityType()
        .equals(VendorSoftwareProductConstants.VENDOR_SOFTWARE_PRODUCT_VERSIONABLE_TYPE))
        .forEach(versionInfoEntity -> handleVersionInfoEntity(items, versionInfoEntity,
            itemChangeRefMap));

    System.setProperty(CASSANDRA_DOX_KEYSTORE, "zusammen_dox");
    VersionCassandraDao versionCassandraDao = new VersionCassandraDao();

    items.forEach(itemId -> versionCassandraDao.list(context, context.getUser().getUserName(),
        new Id(itemId)).forEach(itemVersion -> addItemVersion(itemId, itemVersion.getId(),
        itemVersionMap)));

  }

  private static void handleVersionInfoEntity(List<String> items,
                                              VersionInfoEntity versionInfoEntity,
                                              Map<String, List<String>> itemChangeRefMap) {
    items.add(versionInfoEntity.getEntityId());
    Set<Version> viewableVersions;
    if (versionInfoEntity.getViewableVersions() != null
        && !versionInfoEntity.getViewableVersions().isEmpty()) {
      viewableVersions = versionInfoEntity.getViewableVersions();
    } else {
      viewableVersions = Sets.newHashSet(versionInfoEntity.getActiveVersion());
    }
    addItemChangeRef(versionInfoEntity.getEntityId(), maxChangeRef(viewableVersions),
        itemChangeRefMap);
  }

  private static Id maxChangeRef(Set<Version> viewableVersions) {
    Optional<Version> maxVersion = viewableVersions.stream()
        .max(ResetOldVersion::evaluateMaxVersion);

    return maxVersion.map(version -> new Id(version.toString())).orElse(null);
  }

  private static int evaluateMaxVersion(Version version1, Version version2) {
    if (version1.getMajor() > version2.getMajor()) {
      return 1;
    } else if (version1.getMajor() == version2.getMajor()) {
      return Integer.compare(version1.getMinor(), version2.getMinor());
    } else {
      return -1;
    }
  }

  private static void addItemChangeRef(String itemId, Id changeRef,
                                       Map<String, List<String>> itemChangeRefMap) {
    addItemVersion(itemChangeRefMap, itemId, changeRef);
  }

  private static void addItemVersion(String itemId, Id versionId,
                                     Map<String, List<String>> itemVersionMap) {
    addItemVersion(itemVersionMap, itemId, versionId);
  }

  private static void addItemVersion(Map<String, List<String>> itemVersions, String itemId, Id id) {

    if (!itemVersions.containsKey(itemId)) {
      itemVersions.put(itemId, new ArrayList<>());
    }

    itemVersions.get(itemId).add(id.getValue());
  }
}