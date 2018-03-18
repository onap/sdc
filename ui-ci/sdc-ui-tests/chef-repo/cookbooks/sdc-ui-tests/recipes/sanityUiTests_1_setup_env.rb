tests_base="/var/lib/tests"

 remote_directory '/var/lib/tests/testSuites' do
   source 'testSuites'
   owner 'root'
   group 'root'
   mode '0755'
   action :create
 end

 remote_directory '/var/lib/tests/Files' do
    source 'Files'
    owner 'root'
    group 'root'
    mode '0755'
    action :create
 end

 remote_directory '/var/lib/tests/conf' do
    source 'conf'
    owner 'root'
    group 'root'
    mode '0755'
    action :create
 end

 directory "create_target_dir" do
   path "/var/lib/tests/target"
   owner 'root'
   group 'root'
   mode '0755'
   action :create
 end

 directory "create_ExtentReport_dir" do
   path "/var/lib/tests/ExtentReport"
   owner 'root'
   group 'root'
   mode '0755'
   action :create
 end

 cookbook_file '/var/lib/tests/startTest.sh' do
    source 'startTest.sh'
    owner 'root'
    group 'root'
    mode '0755'
    action :create
 end