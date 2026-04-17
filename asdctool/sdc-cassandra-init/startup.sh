#!/bin/sh

echo ">>> Moving to /home/sdc/scripts"
cd /home/sdc/scripts

echo ">>> Running conditional_test.sh"
sh -x conditional_test.sh

echo ">>> Running change_cassandra_user.sh"
sh -x /home/sdc/scripts/change_cassandra_user.sh

echo ">>> Creating /tmp/config directory"
mkdir -p /tmp/config

echo ">>> Running create_dox_keyspace.sh"
sh -x /home/sdc/scripts/create_dox_keyspace.sh

echo ">>> Moving to /home/sdc/tools/build/scripts"
cd /home/sdc/tools/build/scripts

echo ">>> Running onboard-db-schema-creation.sh"
sh -x /home/sdc/tools/build/scripts/onboard-db-schema-creation.sh

echo ">>> Moving back to /home/sdc/scripts"
cd /home/sdc/scripts

echo ">>> Running create-alter-dox-db.sh"
sh -x /home/sdc/scripts/create-alter-dox-db.sh

echo ">>> Moving to /home/sdc/sdctool/scripts"
cd /home/sdc/sdctool/scripts

echo ">>> Running schemaCreation.sh"
sh -x /home/sdc/sdctool/scripts/schemaCreation.sh /home/sdc/sdctool/config

echo ">>> Running janusGraphSchemaCreation.sh"
sh -x /home/sdc/sdctool/scripts/janusGraphSchemaCreation.sh /home/sdc/sdctool/config

echo ">>> Moving back to /home/sdc/scripts"
cd /home/sdc/scripts

echo ">>> Running importconformance.sh"
sh -x /home/sdc/scripts/importconformance.sh

echo ">>> ALL TASKS COMPLETED SUCCESSFULLY"