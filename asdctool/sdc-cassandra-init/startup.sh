#!/bin/sh

mkdir -p /tmp/writable-config /tmp/writable-config/sdctool /tmp/writable-config/tools /home/sdc/asdctool/logs/SDC/SDC-TOOL

chmod -R 770 /home/sdc/asdctool/logs/SDC/SDC-TOOL

cp -r /tmp/config/cassandra-db-scripts-common/* /tmp/writable-config/
cp -r /tmp/tools/* /tmp/writable-config/tools
cp -r /tmp/sdctool/* /tmp/writable-config/sdctool
cp -r /tmp/writable-config/janusgraph.properties /tmp/writable-config/sdctool/config
cp -r /tmp/writable-config/configuration.yaml /tmp/writable-config/sdctool/config

sh -x /tmp/writable-config/change_cassandra_user.sh
sh -x /tmp/writable-config/create_dox_keyspace.sh

cd /tmp/writable-config/tools/build/scripts
sh -x /tmp/writable-config/tools/build/scripts/onboard-db-schema-creation.sh

chmod -R 770 /tmp/writable-config/sdctool

# Update java options in schema scripts
sed -i 's/java \(.*\) -cp/java \1 -Djava.io.tmpdir=\/tmp\/writable-config\/tmp -cp/' \
    /tmp/writable-config/sdctool/scripts/{schemaCreation.sh,janusGraphSchemaCreation.sh,sdcSchemaFileImport.sh}

sh -x /tmp/writable-config/create-alter-dox-db.sh
sh -x /tmp/writable-config/sdctool/scripts/schemaCreation.sh /tmp/writable-config/sdctool/config
sh -x /tmp/writable-config/sdctool/scripts/janusGraphSchemaCreation.sh /tmp/writable-config/sdctool/config
sh -x /tmp/writable-config/importconformance.sh
