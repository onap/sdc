#onboarding_version = "#{node['onboarding'][:version]}"
#GROUP_ID = "com/att/asdc/onboarding/#{onboarding_version}"
#NEXUS_IP = "#{node['nexus_ip']}"

bash "Excuting openecomp-zusammen-migration-1707.0.0-SNAPSHOT.jar" do
   code <<-EOH
    [ -d /var/tmp/onboarding/migration ] && rm -rf /var/tmp/onboarding || mkdir -p /var/tmp/onboarding/migration
    cd /var/tmp/onboarding/migration
    /bin/tar -xzf /root/chef-solo/cookbooks/cassandra-actions/files/default/zusammen.tgz -C /var/tmp/onboarding/migration
    cd /var/tmp/onboarding/migration 
    java -Dlog.home=/var/tmp/onboarding/migration/logs -Dconfiguration.yaml=/tmp/sdctool/config/configuration.yaml -jar openecomp-zusammen-migration-1707.0.0-SNAPSHOT.jar org.openecomp.core.migration.MigrationMain
   EOH
end

