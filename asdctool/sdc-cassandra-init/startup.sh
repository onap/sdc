#!/bin/sh

cd /home/sdc/scripts
sh -x conditional_test.sh
sh -x /home/sdc/scripts/change_cassandra_user.sh
mkdir -p /tmp/config
sh -x /home/sdc/scripts/create_dox_keyspace.sh
cd /home/sdc/tools/build/scripts
sh -x /home/sdc/tools/build/scripts/onboard-db-schema-creation.sh
cd /home/sdc/scripts
sh -x /home/sdc/scripts/create-alter-dox-db.sh
cd /home/sdc/sdctool/scripts
sh -x /home/sdc/sdctool/scripts/schemaCreation.sh /home/sdc/sdctool/config
sh -x /home/sdc/sdctool/scripts/janusGraphSchemaCreation.sh /home/sdc/sdctool/config
cd /home/sdc/scripts
sh -x /home/sdc/scripts/importconformance.sh