from docs_conf.conf import *

branch = 'latest'
doc_onap_url = 'https://docs.onap.org'
master_doc = 'index'

linkcheck_ignore = [
    'http://localhost',
]

intersphinx_mapping = {}

intersphinx_mapping['onap-doc'] = ('{}/en/%s'.format(doc_onap_url) % branch, None)


def prepare_rst_epilog():
    git_branch = branch
    if git_branch == 'latest':
        git_branch = 'master'
    return """
.. |sdc-helm-validator-open-api| replace:: SDC Helm Validator OpenAPI.yaml
.. _sdc-helm-validator-open-api: https://gerrit.onap.org/r/gitweb?p=sdc/sdc-helm-validator.git;a=blob_plain;f=OpenAPI.yaml;hb=refs/heads/{branch}
""".format(branch=git_branch)

rst_epilog = prepare_rst_epilog()

html_last_updated_fmt = '%d-%b-%y %H:%M'

def setup(app):
    app.add_stylesheet("css/ribbon.css")


