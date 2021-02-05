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
            "sdcuserinit=sdcBePy.users.run:main",
            "sdcimportall=sdcBePy.tosca.imports.run:run",
            "sdcimportdata=sdcBePy.tosca.imports.runNormativeElement:run_import_data",
            "sdcimportcapabilities=sdcBePy.tosca.imports.runNormativeElement:run_import_capabilities",
            "sdcimportrelationship=sdcBePy.tosca.imports.runNormativeElement:run_import_relationship",
            "sdcimportinterfacelifecycle=sdcBePy.tosca.imports.runNormativeElement:run_import_interface_lifecycle",
            "sdcimportcategories=sdcBePy.tosca.imports.runNormativeElement:run_import_categories",
            "sdcimportgroup=sdcBePy.tosca.imports.runNormativeElement:run_import_group",
            "sdcimportpolicy=sdcBePy.tosca.imports.runNormativeElement:run_import_policy",
            "sdcimportnormative=sdcBePy.tosca.imports.runNormativeType:run_import_normative",
            "sdcimportheat=sdcBePy.tosca.imports.runNormativeType:run_import_heat",
            "sdcimportnfv=sdcBePy.tosca.imports.runNormativeType:run_import_nfv",
            "sdcimportnfv271=sdcBePy.tosca.imports.runNormativeType:run_import_nfv_2_7_1",
            "sdcimportnfv331=sdcBePy.tosca.imports.runNormativeType:run_import_nfv_3_3_1",
            "sdcimportnfv411=sdcBePy.tosca.imports.runNormativeType:run_import_nfv_4_1_1",
            "sdcimportonap=sdcBePy.tosca.imports.runNormativeType:run_import_onap",
            "sdcimportsol=sdcBePy.tosca.imports.runNormativeType:run_import_sol",
            "sdcimportannotation=sdcBePy.tosca.imports.runNormativeType:run_import_annotation",
            "sdcimportgeneric=sdcBePy.tosca.imports.runGenericNormative:main",
            "sdcupgradeall=sdcBePy.tosca.upgrade.run:run",
            "sdcupgradenfv=sdcBePy.tosca.upgrade.runUpgradeNormative:run_upgrade_nfv",
            "sdcupgradeonap=sdcBePy.tosca.upgrade.runUpgradeNormative:run_upgrade_onap",
            "sdcupgradesol=sdcBePy.tosca.upgrade.runUpgradeNormative:run_upgrade_sol",
            "sdcupgradeheat1707=sdcBePy.tosca.upgrade.runUpgradeNormative:run_upgrade_heat1707",
            "sdcupgradeheat1707_3537=sdcBePy.tosca.upgrade.runUpgradeNormative:run_upgrade_heat1707_3537",
            "sdcupgradeheatversion=sdcBePy.tosca.upgrade.runUpgradeNormative:run_upgrade_heat_version",
            "sdccheckbackend=sdcBePy.common.healthCheck:main",
            "sdcconsumerinit=sdcBePy.consumers.run:main",
            "sdcinit=sdcBePy.tosca.run:run"
        ]
    }, install_requires=['pycurl']
)
