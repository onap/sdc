tests_path = "/tmp/sdc-tests/"
ci_test_suite = "sanity.xml"


bash "extract asdc-tests" do
   code <<-EOH
        [ ! -d /tmp/sdc-tests/ ] && mkdir -p /tmp/sdc-tests
        cd /tmp/sdc-tests
        rm -rf *
        /bin/tar -xf /root/chef-solo/cookbooks/sdc-sanity/files/default/asdc-tests.tar --strip-components=1 -C /tmp/sdc-tests
        chmod -R 755 /tmp/sdc-tests
   EOH
end


template "sdc-yaml-config" do
    path "/tmp/sdc-tests/conf/sdc.yaml"
    source "sdc-sanity.yaml.erb"
    owner "root"
    group "root"
    mode "0755"
    variables ({
        :target_path                  => "#{tests_path}/target",
        :catalogBE_ip                 => node['Nodes']['BE'],
        :catalogBE_port               => node['BE'][:http_port],
        :webportal_ip                 => node['Nodes']['FE'],
        :webportal_port               => node['FE'][:http_port],
        :titan_file                   => "/tmp/sdc-tests/conf/titan.properties",
        :tests_path_ci                => "#{tests_path}/CI/tests",
        :components_path              => "#{tests_path}/CI/components",
        :importResourceConfigDir      => "#{tests_path}/CI/importResource",
        :importTypesDir               => "#{tests_path}/CI/importTypesTest",
        :importResourceTestsConfigDir => "#{tests_path}/CI/importResourceTests",
        :ConfigurationFile            => "#{tests_path}/conf/configuration.yaml",
        :errorConfigurationFile       => "#{tests_path}/conf/error-configuration.yaml",
        :CASSANDRA_IP                 => node['Nodes']['CS'],
        :CASSANDRA_PWD                => node['cassandra'][:cassandra_password],
        :CASSANDRA_USR                => node['cassandra'][:cassandra_user]
    })
end


replication_factor=1
template "titan.properties" do
    path "/tmp/sdc-tests/conf/titan.properties"
    source "BE-titan.properties.erb"
    owner "root"
    group "root"
    mode "0755"
    variables({
        :CASSANDRA_IP => node['Nodes']['CS'],
        :CASSANDRA_PWD => node['cassandra'][:cassandra_password],
        :CASSANDRA_USR => node['cassandra'][:cassandra_user],
        :DC_NAME => "DC-"+node.chef_environment,
	    :rep_factor => replication_factor	 
    })
end


bash "run asdc ci sanity tests" do
    cwd "#{tests_path}"
    code <<-EOH
        jar_file=`ls asdc-tests-*-jar-with-dependencies.jar`
        ./startTest.sh $jar_file #{ci_test_suite}
        echo "return code from startTest.sh = [$?]"
    EOH
    timeout 72000
end
