from setuptools import setup, find_packages

setup(
    name='sdcBePy',
    version='1.7',
    packages=find_packages(),
    url='',
    license='',
    author='',
    author_email='',
    description='',
    package_data={'': ['data/*.json']},
    include_package_data=True,
    entry_points={
        "console_scripts": [
            "sdccheckbackend=sdcBePy.common.healthCheck:main",
            "sdcinit=sdcBePy.tosca.run:run"
        ]
    }, install_requires=['pycurl']
)
