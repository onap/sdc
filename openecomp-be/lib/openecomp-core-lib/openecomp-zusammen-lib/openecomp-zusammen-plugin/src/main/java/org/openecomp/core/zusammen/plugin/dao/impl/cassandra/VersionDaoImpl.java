package org.openecomp.core.zusammen.plugin.dao.impl.cassandra;

import com.amdocs.zusammen.datatypes.Id;
import com.amdocs.zusammen.datatypes.SessionContext;
import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Row;
import com.datastax.driver.mapping.annotations.Accessor;
import com.datastax.driver.mapping.annotations.Query;
import org.openecomp.core.zusammen.plugin.dao.VersionDao;
import org.openecomp.core.zusammen.plugin.dao.types.VersionEntity;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toMap;

public class VersionDaoImpl implements VersionDao {

  @Override
  public void create(SessionContext context, String space, Id itemId, VersionEntity version) {
    String baseVersion = version.getBaseId() != null ? version.getBaseId().toString() : null;

    getAccessor(context)
        .create(space, itemId.toString(), version.getId().toString(),
            baseVersion,
            version.getCreationTime(), version.getModificationTime());

  }

  @Override
  public void delete(SessionContext context, String space, Id itemId, Id versionId) {

    getAccessor(context).delete(space, itemId.toString(), versionId.toString());
  }

  @Override
  public void updateModificationTime(SessionContext context, String space, Id itemId,
                                     Id versionId, Date modificationTime) {
    getAccessor(context)
        .updateModificationTime(modificationTime, space, itemId.toString(), versionId.toString());
  }


  @Override
  public Collection<VersionEntity> list(SessionContext context, String space, Id itemId) {
    List<Row> rows = getAccessor(context).list(space, itemId.toString()).all();
    return rows == null ? new ArrayList<>() :
        rows.stream().map(VersionDaoImpl::convertToVersionEntity).collect(Collectors.toList());
  }


  @Override
  public Optional<VersionEntity> get(SessionContext context, String space, Id itemId,
                                     Id versionId) {
    Row row;

    row = getAccessor(context).get(space, itemId.toString(), versionId.toString()).one();


    return row == null ? Optional.empty() : Optional.of(convertToVersionEntity(row));
  }

  @Override
  public boolean checkHealth(SessionContext context) {
    return getAccessor(context).checkHealth().getColumnDefinitions()
        .contains(VersionField.VERSION_ID);
  }

  @Override
  public void createVersionElements(SessionContext context, String space, Id itemId,
                                    Id versionId, Id revisionId, Map<Id, Id> versionElementIds,
                                    Date publishTime, String message) {
    Map<String, String> elementIds = versionElementIds==null?null:versionElementIds.
        entrySet().
        stream().
        collect(toMap((Map.Entry<Id, Id>entry)->entry.getKey().getValue(),
                      (Map.Entry<Id, Id>entry)->entry.getValue().getValue()));

    getVersionElementsAccessor(context).create(space,itemId.toString(),versionId.toString(),
        revisionId.getValue(),elementIds,publishTime,message,context.getUser().getUserName());
  }


  private static VersionEntity convertToVersionEntity(Row row) {

    /*Id revisionId =  row.getColumnDefinitions().contains("revision_id")?new Id(row.getString
        (VersionField.REVISION_ID)):null;*/

    VersionEntity version = new VersionEntity(new Id(row.getString(VersionField.VERSION_ID)));
    return enrichVersionEntity(version, row);
  }

  static VersionEntity enrichVersionEntity(VersionEntity version, Row row) {
    version.setBaseId(new Id(row.getString(VersionField.BASE_VERSION_ID)));
    version.setCreationTime(row.getDate(VersionField.CREATION_TIME));
    version.setModificationTime(row.getDate(VersionField.MODIFICATION_TIME));
    return version;
  }

  private VersionAccessor getAccessor(SessionContext context) {
    return CassandraDaoUtils.getAccessor(context, VersionAccessor.class);
  }

  private VersionElementsAccessor getVersionElementsAccessor(SessionContext context) {
    return CassandraDaoUtils.getAccessor(context, VersionElementsAccessor.class);
  }

  @Accessor
  interface VersionAccessor {

    @Query(
        "INSERT INTO version (space, item_id, version_id, base_version_id, " +
            "creation_time, " +
            "modification_time) " +
            "VALUES (?, ?, ?, ?, ?, ?)")
    void create(String space, String itemId, String versionId, String baseVersionId,
                Date creationTime, Date modificationTime);

    @Query("UPDATE version SET modification_time=? WHERE space=? AND item_id=? AND version_id=? ")
    void updateModificationTime(Date modificationTime, String space, String itemId,
                                String versionId);

    @Query("DELETE FROM version WHERE space=? AND item_id=? AND version_id=? ")
    void delete(String space, String itemId, String versionId);

    @Query("SELECT version_id, base_version_id, creation_time, modification_time " +
        "FROM version WHERE space=? AND item_id=? AND version_id=?  ")
    ResultSet get(String space, String itemId, String versionId);

    /*@Query("SELECT version_id, base_version_id, creation_time, modification_time " +
        "FROM version WHERE space=? AND item_id=? AND version_id=? ")
    ResultSet get(String space, String itemId, String versionId);*/


    @Query("SELECT version_id, base_version_id, creation_time, modification_time " +
        "FROM version WHERE space=? AND item_id=?")
    ResultSet list(String space, String itemId);

    @Query("SELECT version_id FROM version LIMIT 1")
    ResultSet checkHealth();
  }

  private static final class VersionField {
    private static final String VERSION_ID = "version_id";
    private static final String BASE_VERSION_ID = "base_version_id";
    private static final String CREATION_TIME = "creation_time";
    private static final String MODIFICATION_TIME = "modification_time";
    //private static final String REVISION_ID = "revision_id";
  }

  @Accessor
  interface VersionElementsAccessor {

    @Query("INSERT INTO version_elements (space,item_id,version_id,revision_id,element_ids," +
        "publish_time,message,user) " +
        "VALUES (?,?,?,?,?,?,?,?)")
    void create(String space,
                String itemId,
                String versionId,
                String versionRevisionId,
                Map<String,String> elementIds,
                Date publishTime,
                String message,
                String user);



  }

 /* public static final class VersionElementsField {
    private static final String SPACE = "space";
    private static final String ITEM_ID = "item_id";
    private static final String VERSION_ID = "version_id";
    private static final String ELEMENT_IDS = "element_ids";
    private static final String REVISION_ID = "revision_id";

  }*/


}
