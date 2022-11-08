project = "onap"
release = "master"
version = "master"

author = "Open Network Automation Platform"
# yamllint disable-line rule:line-length
copyright = "ONAP. Licensed under Creative Commons Attribution 4.0 International License"

pygments_style = "sphinx"
html_theme = "sphinx_rtd_theme"
html_theme_options = {
  "style_nav_header_background": "white",
  "sticky_navigation": "False" }
html_logo = "_static/logo_onap_2017.png"
html_favicon = "_static/favicon.ico"
html_static_path = ["_static"]
html_show_sphinx = False

extensions = [
    'sphinx.ext.intersphinx',
    'sphinx.ext.graphviz',
    'sphinxcontrib.blockdiag',
    'sphinxcontrib.seqdiag',
    'sphinxcontrib.swaggerdoc',
    'sphinxcontrib.plantuml'
]

#
# Map to 'latest' if this file is used in 'latest' (master) 'doc' branch.
# Change to {releasename} after you have created the new 'doc' branch.
#

branch = 'latest'

intersphinx_mapping = {}
doc_url = 'https://docs.onap.org/projects'
master_doc = 'index'

exclude_patterns = ['.tox']

spelling_lang = "en_GB"

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
