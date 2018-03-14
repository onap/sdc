cookbook_file "/tmp/normatives.tar.gz" do
      source "normatives.tar.gz"
   end

be_ip=node['Nodes'][:BE]

bash "excuting-import_Normatives" do
   code <<-EOH
     set -x
     cd /tmp
     tar xvfz /tmp/normatives.tar.gz
     cd /tmp/normatives/scripts/import/tosca/
     /bin/chmod +x *.py

     # executing the normatives
     # add --debug=true to the importNormativeAll.py arguments to enable debug

     check_normative="/tmp/check_normative.out"
     status_code=$(curl -s -o ${check_normative} -w "%{http_code}\\n" -X GET -H 'Content-Type: application/json;charset=UTF-8' -H 'USER_ID: jh0003' -H 'X-ECOMP-RequestID: cbe744a0-037b-458f-aab5-df6e543c4090' "http://#{be_ip}:8080/sdc2/rest/v1/screen")
     if [ "$status_code" != 200 ] ; then
        exit "$status_code"
     fi

     #curl -s -X GET -H "Content-Type: application/json;charset=UTF-8" -H "USER_ID: jh0003" -H "X-ECOMP-RequestID: cbe744a0-037b-458f-aab5-df6e543c4090" "http://#{be_ip}:8080/sdc2/rest/v1/screen" > ${check_normative}

     resources_len=`cat ${check_normative}| jq '.["resources"]|length'`
     mkdir -p /var/lib/jetty/logs
     if [ $resources_len -eq 0 ] ; then
        python importONAPNormativeAll.py -i #{be_ip} > /var/lib/jetty/logs/importNormativeAll.log
            rc=$?
            if [[ $rc != 0 ]]; then exit $rc; fi
     else
        python upgradeONAPNormative.py -i #{be_ip} > /var/lib/jetty/logs/upgradeNormative.log
            rc=$?
            if [[ $rc != 0 ]]; then exit $rc; fi
     fi
   EOH
end
