package org.openecomp.core.utilities.applicationconfig.dao;


import org.openecomp.core.utilities.applicationconfig.dao.type.ApplicationConfigEntity;

import com.datastax.oss.driver.api.core.cql.ResultSet;
import com.datastax.oss.driver.api.mapper.annotations.Dao;
import com.datastax.oss.driver.api.mapper.annotations.Query;

@Dao
public interface ApplicationConfigDaoAccessor {

    @Query("SELECT namespace, key, value FROM application_config WHERE namespace=?")
    ResultSet list(String namespace);

    @Query("INSERT INTO application_config (namespace, key, value) VALUES (?,?,?)")
    void updateApplicationConfigData(String namespace, String key, String value);

    @Query("SELECT namespace, key, value FROM application_config WHERE namespace=? AND key=?")
    ApplicationConfigEntity get(String namespace, String key);

    @Query("SELECT value, writetime(value) FROM application_config WHERE namespace=? AND key=?")
    ResultSet getValueAndTimestampOfConfigurationValue(String namespace, String key);
    }