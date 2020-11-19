from docs_conf.conf import *

branch = 'guilin'
doc_onap_url = 'https://docs.onap.org'
master_doc = 'index'

linkcheck_ignore = [
    'http://localhost',
]

intersphinx_mapping = {}

intersphinx_mapping['onap-doc'] = ('{}/en/%s'.format(doc_onap_url) % branch, None)

html_last_updated_fmt = '%d-%b-%y %H:%M'

def setup(app):
    app.add_stylesheet("css/ribbon.css")
