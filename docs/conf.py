from docs_conf.conf import *

branch = 'latest'
doc_url = 'https://docs.onap.org/projects'
master_doc = 'index'

intersphinx_mapping = {}

# Latest (change to branch)
intersphinx_mapping['onap-aai-aai-common'] = ('{}/onap-aai-aai-common/en/%s'.format(doc_url) % branch, None)
intersphinx_mapping['onap-aai-sparky-be'] = ('{}/onap-aai-sparky-be/en/%s'.format(doc_url) % branch, None)
intersphinx_mapping['onap-ccsdk-cds'] = ('{}/onap-ccsdk-cds/en/%s'.format(doc_url) % branch, None)
intersphinx_mapping['onap-ccsdk-features'] = ('{}/onap-ccsdk-features/en/%s'.format(doc_url) % branch, None)
intersphinx_mapping['onap-ccsdk-distribution'] = ('{}/onap-ccsdk-distribution/en/%s'.format(doc_url) % branch, None)
intersphinx_mapping['onap-ccsdk-oran'] = ('{}/onap-ccsdk-oran/en/%s'.format(doc_url) % branch, None)
intersphinx_mapping['onap-cli'] = ('{}/onap-cli/en/%s'.format(doc_url) % branch, None)
intersphinx_mapping['onap-cps'] = ('{}/onap-cps/en/%s'.format(doc_url) % branch, None)
intersphinx_mapping['onap-dcaegen2'] = ('{}/onap-dcaegen2/en/%s'.format(doc_url) % branch, None)
intersphinx_mapping['onap-dmaap-messagerouter-messageservice'] = (
    '{}/onap-dmaap-messagerouter-messageservice/en/%s'.format(doc_url) % branch, None)
intersphinx_mapping['onap-dmaap-buscontroller'] = ('{}/onap-dmaap-buscontroller/en/%s'.format(doc_url) % branch, None)
intersphinx_mapping['onap-dmaap-datarouter'] = ('{}/onap-dmaap-datarouter/en/%s'.format(doc_url) % branch, None)
intersphinx_mapping['onap-dmaap-dbcapi'] = ('{}/onap-dmaap-dbcapi/en/%s'.format(doc_url) % branch, None)
intersphinx_mapping['onap-externalapi-nbi'] = ('{}/onap-externalapi-nbi/en/%s'.format(doc_url) % branch, None)
intersphinx_mapping['onap-holmes-engine-management'] = (
    '{}/onap-holmes-engine-management/en/%s'.format(doc_url) % branch, None)
intersphinx_mapping['onap-holmes-rule-management'] = (
    '{}/onap-holmes-rule-management/en/%s'.format(doc_url) % branch, None)
intersphinx_mapping['onap-integration'] = ('{}/onap-integration/en/%s'.format(doc_url) % branch, None)
intersphinx_mapping['onap-modeling-etsicatalog'] = ('{}/onap-modeling-etsicatalog/en/%s'.format(doc_url) % branch, None)
intersphinx_mapping['onap-modeling-modelspec'] = ('{}/onap-modeling-modelspec/en/%s'.format(doc_url) % branch, None)
intersphinx_mapping['onap-multicloud-framework'] = ('{}/onap-multicloud-framework/en/%s'.format(doc_url) % branch, None)
intersphinx_mapping['onap-msb-apigateway'] = ('{}/onap-msb-apigateway/en/%s'.format(doc_url) % branch, None)
intersphinx_mapping['onap-oom'] = ('{}/onap-oom/en/%s'.format(doc_url) % branch, None)
intersphinx_mapping['onap-oom-offline-installer'] = ('{}/onap-oom-offline-installer/en/%s'.format(doc_url) % branch, None)
intersphinx_mapping['onap-oom-platform-cert-service'] = (
    '{}/onap-oom-platform-cert-service/en/%s'.format(doc_url) % branch, None)
intersphinx_mapping['onap-optf-cmso'] = ('{}/onap-optf-cmso/en/%s'.format(doc_url) % branch, None)
intersphinx_mapping['onap-optf-osdf'] = ('{}/onap-optf-osdf/en/%s'.format(doc_url) % branch, None)
intersphinx_mapping['onap-optf-has'] = ('{}/onap-optf-has/en/%s'.format(doc_url) % branch, None)
intersphinx_mapping['onap-policy-clamp'] = ('{}/onap-policy-clamp/en/%s'.format(doc_url) % branch, None)
intersphinx_mapping['onap-policy-parent'] = ('{}/onap-policy-parent/en/%s'.format(doc_url) % branch, None)
intersphinx_mapping['onap-sdc'] = ('{}/onap-sdc/en/%s'.format(doc_url) % branch, None)
intersphinx_mapping['onap-sdnc-oam'] = ('{}/onap-sdnc-oam/en/%s'.format(doc_url) % branch, None)
intersphinx_mapping['onap-so'] = ('{}/onap-so/en/%s'.format(doc_url) % branch, None)
intersphinx_mapping['onap-usecase-ui'] = ('{}/onap-usecase-ui/en/%s'.format(doc_url) % branch, None)
intersphinx_mapping['onap-vfc-nfvo-lcm'] = ('{}/onap-vfc-nfvo-lcm/en/%s'.format(doc_url) % branch, None)
intersphinx_mapping['onap-vid'] = ('{}/onap-vid/en/%s'.format(doc_url) % branch, None)
intersphinx_mapping['onap-vnfrqts-guidelines'] = ('{}/onap-vnfrqts-guidelines/en/%s'.format(doc_url) % branch, None)
intersphinx_mapping['onap-vnfrqts-requirements'] = ('{}/onap-vnfrqts-requirements/en/%s'.format(doc_url) % branch, None)
intersphinx_mapping['onap-vnfrqts-testcases'] = ('{}/onap-vnfrqts-testcases/en/%s'.format(doc_url) % branch, None)
intersphinx_mapping['onap-vnfrqts-usecases'] = ('{}/onap-vnfrqts-usecases/en/%s'.format(doc_url) % branch, None)
intersphinx_mapping['onap-vnfsdk-model'] = ('{}/onap-vnfsdk-model/en/%s'.format(doc_url) % branch, None)
intersphinx_mapping['onap-vvp-documentation'] = ('{}/onap-vvp-documentation/en/%s'.format(doc_url) % branch, None)

# Guilin
branch = 'guilin'
intersphinx_mapping['onap-portal'] = ('{}/onap-portal/en/%s'.format(doc_url) % branch, None)

# Frankfurt
branch = 'frankfurt'
intersphinx_mapping['onap-appc'] = ('{}/onap-appc/en/%s'.format(doc_url) % branch, None)
intersphinx_mapping['onap-appc-deployment'] = ('{}/onap-appc-deployment/en/%s'.format(doc_url) % branch, None)
intersphinx_mapping['onap-music'] = ('{}/onap-music/en/%s'.format(doc_url) % branch, None)

# Latest
branch = 'latest'
intersphinx_mapping['onap-aaf-authz'] = ('{}/onap-aaf-authz/en/%s'.format(doc_url) % branch, None)
intersphinx_mapping['onap-aaf-sms'] = ('{}/onap-aaf-sms/en/%s'.format(doc_url) % branch, None)
intersphinx_mapping['onap-aai-event-client'] = ('{}/onap-aai-event-client/en/%s'.format(doc_url) % branch, None)
intersphinx_mapping['onap-aai-esr-gui'] = ('{}/onap-aai-esr-gui/en/%s'.format(doc_url) % branch, None)
intersphinx_mapping['onap-aai-esr-server'] = ('{}/onap-aai-esr-server/en/%s'.format(doc_url) % branch, None)
intersphinx_mapping['onap-ccsdk-apps'] = ('{}/onap-ccsdk-apps/en/%s'.format(doc_url) % branch, None)
intersphinx_mapping['onap-ccsdk-dashboard'] = ('{}/onap-ccsdk-dashboard/en/%s'.format(doc_url) % branch, None)
intersphinx_mapping['onap-ccsdk-platform-plugins'] = (
    '{}/onap-ccsdk-platform-plugins/en/%s'.format(doc_url) % branch, None)
intersphinx_mapping['onap-logging-analytics'] = ('{}/onap-logging-analytics/en/%s'.format(doc_url) % branch, None)
intersphinx_mapping['onap-logging-analytics-pomba-pomba-audit-common'] = (
    '{}/onap-logging-analytics-pomba-pomba-audit-common/en/%s'.format(doc_url) % branch, None)
intersphinx_mapping['onap-modeling-toscaparsers'] = (
    '{}/onap-modeling-toscaparsers/en/%s'.format(doc_url) % branch, None)
intersphinx_mapping['onap-msb-discovery'] = ('{}/onap-msb-discovery/en/%s'.format(doc_url) % branch, None)
intersphinx_mapping['onap-msb-java-sdk'] = ('{}/onap-msb-java-sdk/en/%s'.format(doc_url) % branch, None)
intersphinx_mapping['onap-msb-swagger-sdk'] = ('{}/onap-msb-swagger-sdk/en/%s'.format(doc_url) % branch, None)
intersphinx_mapping['onap-multicloud-azure'] = ('{}/onap-multicloud-azure/en/%s'.format(doc_url) % branch, None)
intersphinx_mapping['onap-multicloud-k8s'] = ('{}/onap-multicloud-k8s/en/%s'.format(doc_url) % branch, None)
intersphinx_mapping['onap-music-distributed-kv-store'] = (
    '{}/onap-music-distributed-kv-store/en/%s'.format(doc_url) % branch, None)
intersphinx_mapping['onap-oparent-cia'] = ('{}/onap-oparent-cia/en/%s'.format(doc_url) % branch, None)
intersphinx_mapping['onap-osa'] = ('{}/onap-osa/en/%s'.format(doc_url) % branch, None)
intersphinx_mapping['onap-sdc-sdc-distribution-client'] = (
    '{}/onap-sdc-sdc-distribution-client/en/%s'.format(doc_url) % branch, None)
intersphinx_mapping['onap-sdc-sdc-workflow-designer'] = (
    '{}/onap-sdc-sdc-workflow-designer/en/%s'.format(doc_url) % branch, None)
intersphinx_mapping['onap-sdc-sdc-tosca'] = ('{}/onap-sdc-sdc-tosca/en/%s'.format(doc_url) % branch, None)
intersphinx_mapping['onap-sdc-sdc-docker-base'] = ('{}/onap-sdc-sdc-docker-base/en/%s'.format(doc_url) % branch, None)
intersphinx_mapping['onap-so-libs'] = ('{}/onap-so-libs/en/%s'.format(doc_url) % branch, None)
intersphinx_mapping['onap-vfc-nfvo-driver-vnfm-svnfm'] = (
    '{}/onap-vfc-nfvo-driver-vnfm-svnfm/en/%s'.format(doc_url) % branch, None)

linkcheck_ignore = [
    'http://localhost',
    'https://example.com',
    'about:config',
    # this URL is not directly reachable and must be configured in the system hosts file.
    'https://portal.api.simpledemo.onap.org:30225/ONAPPORTAL/login.htm',
    # anchor issues
    'https://docs.onap.org/projects/onap-integration/en/latest/docs_usecases_release.html#.*',
    'https://docs.linuxfoundation.org/docs/communitybridge/easycla/contributors/contribute-to-a-gerrit-project#.*',
    'https://docs.onap.org/projects/onap-integration/en/latest/docs_robot.html#docs-robot',
    'https://docs.onap.org/projects/onap-integration/en/latest/docs_usecases_release.html#docs-usecases-release',
    'https://docs.onap.org/projects/onap-integration/en/latest/docs_usecases.html#docs-usecases',
    'https://docs.onap.org/projects/onap-integration/en/latest/usecases/release_non_functional_requirements.html#release-non-functional-requirements',
]


html_last_updated_fmt = '%d-%b-%y %H:%M'


def setup(app):
    app.add_css_file("css/ribbon.css")
