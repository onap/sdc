package org.openecomp.sdc.be.togglz;

import org.openecomp.sdc.be.resources.data.togglz.ToggleableFeature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.togglz.core.Feature;
import org.togglz.core.manager.TogglzConfig;
import org.togglz.core.repository.StateRepository;
import org.togglz.core.repository.cache.CachingStateRepository;
import org.togglz.core.repository.file.FileBasedStateRepository;
import org.togglz.core.user.SimpleFeatureUser;
import org.togglz.core.user.UserProvider;

import java.io.File;

@Component
public class ToggleConfiguration implements TogglzConfig {
    @Autowired
    private CassandraCustomStateRepository cassandraCustomStateRepository;

    @Override
    public Class<? extends Feature> getFeatureClass() {
        return ToggleableFeature.class;
    }

    @Override
    public StateRepository getStateRepository() {
        return new CachingStateRepository(cassandraCustomStateRepository, 10000);
    }

    @Override
    public UserProvider getUserProvider() {
        return () -> new SimpleFeatureUser("admin", true);
    }
}