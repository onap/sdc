tests_base="/var/lib/tests"

template "sdc-yaml-config" do
    path "#{tests_base}/conf/sdc.yaml"
    source "sdc-sanity.yaml.erb"
    owner "root"
    group "root"
    mode "0755"
    variables ({
        :catalogBE_ip                 => node['Nodes']['BE'],
        :catalogBE_port               => node['BE'][:http_port],
        :webportal_ip                 => node['Nodes']['FE'],
        :webportal_port               => node['FE'][:http_port],
        :titan_file                   => "#{tests_base}/conf/titan.properties",
        :tests_base_ci                => "#{tests_base}/CI/tests",
        :components_path              => "#{tests_base}/CI/components",
        :importResourceConfigDir      => "#{tests_base}/CI/importResource",
        :importTypesDir               => "#{tests_base}/CI/importTypesTest",
        :importResourceTestsConfigDir => "#{tests_base}/CI/importResourceTests",
        :ConfigurationFile            => "#{tests_base}/conf/configuration.yaml",
        :errorConfigurationFile       => "#{tests_base}/conf/error-configuration.yaml",
        :CASSANDRA_IP                 => node['Nodes']['CS'].join(",").gsub(/[|]/,''),
        :CASSANDRA_PWD                => node['cassandra'][:cassandra_password],
        :CASSANDRA_USR                => node['cassandra'][:cassandra_user],
        :ES_IP                        => node['Nodes']['ES']
    })
end

replication_factor=1
template "titan.properties" do
   path "/#{tests_base}/conf/titan.properties"
   source "BE-titan.properties.erb"
   owner "root"
   group "root"
   mode "0755"
   variables({
      :CASSANDRA_IP => node['Nodes']['CS'].join(",").gsub(/[|]/,''),
      :CASSANDRA_PWD => node['cassandra'][:cassandra_password],
      :CASSANDRA_USR => node['cassandra'][:cassandra_user],
      :rep_factor => replication_factor,
      :DC_NAME      => node['cassandra']['datacenter_name']
   })
end
