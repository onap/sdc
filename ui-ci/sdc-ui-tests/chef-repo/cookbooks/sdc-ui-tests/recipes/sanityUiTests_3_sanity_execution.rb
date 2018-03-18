tests_base="/var/lib/tests"
ci_test_suite="#{ENV['SUITE_NAME']}.xml"

bash "echo status" do
   code <<-EOH
     echo "DOCKER STARTED"
   EOH
end

bash "run asdc ci sanity tests" do
cwd "#{tests_base}"
code <<-EOH
   cd "#{tests_base}"
   jar_file=`ls ui-ci*-jar-with-dependencies.jar`
   ./startTest.sh $jar_file #{ci_test_suite} > #{tests_base}/target/startTest.log 2>&1
   echo "return code from startTest.sh = [$?]"
   echo "DOCKER STARTED"
EOH
end
