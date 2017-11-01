tests_base="/var/lib/tests"
ci_test_suite="onap.xml"

bash "run asdc ci sanity tests" do
cwd "#{tests_base}"
code <<-EOH
   cd "#{tests_base}"
   jar_file=`ls test-apis*-jar-with-dependencies.jar`
   ./startTest.sh $jar_file #{ci_test_suite} > #{tests_base}/target/startTest.out 2>&1
   echo "return code from startTest.sh = [$?]"
EOH
timeout 72000
end