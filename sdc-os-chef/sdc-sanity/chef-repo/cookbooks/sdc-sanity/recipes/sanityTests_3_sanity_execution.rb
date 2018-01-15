tests_base="/var/lib/tests"
ci_test_suite="pass.xml"

bash "run asdc ci sanity tests" do
cwd "#{tests_base}"
code <<-EOH
   cd "#{tests_base}"
   jar_file=`ls test-apis*-jar-with-dependencies.jar`
   ./startTest.sh $jar_file #{ci_test_suite} > #{tests_base}/target/startTest.log 2>&1
   echo "return code from startTest.sh = [$?]"
   echo "DOCKER STARTED"
EOH
timeout 72000
end
bash "echo status" do
   code <<-EOH
     echo "DOCKER STARTED"
   EOH
end