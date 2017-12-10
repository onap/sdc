const SDC_CONFIG = {
	"environment": "dev",
	"api": {
		"GET_component": "/v1/catalog/:type/:id",
		"PUT_component": "/v1/catalog/:type/:id/metadata",
		"GET_component_validate_name": "/v1/catalog/:type/validate-name/:name",
		"POST_changeLifecycleState": "/v1/catalog/",
		"component_api_root": "/v1/catalog/",
		"GET_user": "/v1/user/:id",
		"GET_user_authorize": "/v1/user/authorize",
		"GET_all_users": "/v1/user/users",
		"POST_create_user": "/v1/user",
		"DELETE_delete_user": "/v1/user/:id",
		"POST_edit_user_role": "/v1/user/:id/role",
		"GET_resource": "/v1/catalog/resources/:id",
		"GET_resources_latestversion_notabstract":"/v1/catalog/:type/latestversion/notabstract/:id",
		"GET_resources_certified_not_abstract": "/v1/catalog/resources/certified/notabstract/:id",
		"GET_resources_certified_abstract": "/v1/catalog/resources/certified/abstract/:id",
		"GET_resource_property": "/v1/catalog/:type/:entityId/properties/:id",
		"PUT_resource": "/v1/catalog/resources/:id/metadata",
		"GET_resource_artifact": "/v1/catalog/:type/:entityId/artifacts/:id",
		"GET_download_instance_artifact": "/v1/catalog/:type/:entityId/resourceInstances/:instanceId/artifacts/:id",
		"POST_instance_artifact": "/v1/catalog/:type/:entityId/resourceInstance/:instanceId/artifacts/:id",
		"GET_resource_additional_information": "/v1/catalog/:type/:entityId/additionalinfo/:id",
		"GET_service_artifact": "/v1/catalog/services/:serviceId/artifacts/:id",
		"GET_resource_interface_artifact": "/v1/catalog/:type/:entityId/standard/:operation/artifacts/:id",
		"GET_resource_api_artifact": "/v1/catalog/:type/:entityId/artifacts/api/:id",
		"GET_configuration_ui": "/v1/configuration/ui",
		"GET_resource_validate_name": "/v1/catalog/resources/validate-name/:name",
		"GET_activity_log": "/v1/catalog/audit-records/:type/:id",
		"GET_service": "/v1/catalog/services/:id",
		"GET_service_validate_name": "/v1/catalog/services/validate-name/:name",
		"GET_service_distributions":"/v1/catalog/services/:uuid/distribution",
		"GET_service_distributions_components":"/v1/catalog/services/distribution/:distributionId",
		"POST_service_distribution_deploy" : "/v1/catalog/services/:serviceId/distribution/:distributionId/markDeployed",
		"GET_element": "/v1/followed",
		"GET_catalog": "/v1/screen",
		"GET_ecomp_menu_items": "/v1/user/:userId/functionalmenu",
		"GET_resource_category": "/v1/resourceCategories",
		"GET_service_category": "/v1/serviceCategories",
		"resource_instance": "/v1/catalog/:entityType/:entityId/resourceInstance/:id",
		"GET_resource_instance_property": "/v1/catalog/:type/:entityId/resourceInstance/:componentInstanceId/property/:propertyValueId",
		"GET_relationship": "/v1/catalog/:entityType/:entityId/resourceInstance/:action",
		"GET_lifecycle_state_resource": "/v1/catalog/:type/:id/lifecycleState/:action",
		"GET_lifecycle_state_CHECKIN":"lifecycleState/CHECKIN",
		"GET_lifecycle_state_CERTIFICATIONREQUEST":"lifecycleState/CERTIFICATIONREQUEST",
		"GET_lifecycle_state_UNDOCHECKOUT":"lifecycleState/UNDOCHECKOUT",
		"root": "/sdc1/feProxy/rest",
		"PUT_service": "/v1/catalog/services/:id/metadata",
		"GET_download_artifact": "/v1/catalog/",
        "GET_SDC_Version": "/version",
		"GET_categories": "/v1/categories/:types",
		"POST_category": "/v1/category/:types/:categoryId",
		"POST_subcategory": "/v1/category/:types/:categoryId/subCategory/:subCategoryId",
		"POST_change_instance_version": "/v1/catalog/:entityType/:entityId/resourceInstance/:id/changeVersion",
		"GET_requirements_capabilities": "/v1/catalog/requirmentsCapabilities/:type/:id",
		"GET_resource_artifact_types": "/v1/artifactTypes",
		"GET_onboarding": "/sdc1/feProxy/onboarding-api/v1.0/vendor-software-products/packages",
		"GET_component_from_csar_uuid": "/v1/catalog/resources/csar/:csar_uuid",
		"kibana": "/sdc1/kibanaProxy/"
	},
	"resourceTypesFilter":{
        "resource":["CP","VFC","VL"],
        "service":["CP","VF","VL", "PNF","CVFC","SERVICE", "Configuration"]
	},
	"logConfig": {
		"minLogLevel": "debug",
		"prefix": "sdcApp"
	},
	"cookie": {
		"junctionName": "IV_JCT",
		"prefix": "AMWEBJCT!",
		"userIdSuffix": "USER_ID",
		"userFirstName": "HTTP_CSP_FIRSTNAME",
		"userLastName": "HTTP_CSP_LASTNAME",
		"userEmail": "HTTP_CSP_EMAIL",
		"xEcompRequestId": " X-ECOMP-RequestID"
	},
	"imagesPath": "",
	"cpEndPointInstances" : ["cloudep","ossep","personep","premisesep"],
	"toscaFileExtension":"yaml,yml",
	"csarFileExtension":"csar",
    "showOutlook": false,
    "validationConfigPath":"configurations/validation.json",
	"categories": {},
	"testers": {
		"RESOURCE": {
			"Network L2-3": "DL-ASDCL1-3ResourceCertificationTeam",
			"Network L4+": "DL-ASDCL4-7ResourceCertificationTeam",
			"Application L4+": "DL-ASDCL4-7ResourceCertificationTeam",
			"default": "DL-ASDCL1-3ResourceCertificationTeam;DL-ASDCL4-7ResourceCertificationTeam"
		},
		"SERVICE": {
			"Network L1-3": "DL-ASDCL1-4ServiceCertificationTeam",
			"Network L4+": "DL-ASDCL4-7ServiceCertificationTeam",
			"default": "DL-ASDCL1-4ServiceCertificationTeam;DL-ASDCL4-7ServiceCertificationTeam"
		}
	},
	"roles": ["ADMIN", "TESTER", "GOVERNOR", "OPS", "DESIGNER"],
	"tutorial": {
		"tabs": [
			{
				"id":1,
				"name":"TUTRIAL_GENERAL_TAB_1",
				"defaultPage":1
			},
			{
				"id":2,
				"name":"TUTRIAL_GENERAL_TAB_2",
				"defaultPage":9
			},
			{
				"id":3,
				"name":"TUTRIAL_GENERAL_TAB_3",
				"defaultPage":12
			}
		],
		"pages":
		[
			{
				"id":1,
				"template": "text-template",
				"tab": 1,
				"data":{
					"title":"TUTORIAL_PAGE1_TITLE",
					"description":"TUTORIAL_PAGE1_TEXT"
				}

			},
			{
				"id":2,
				"template": "image-template",
				"tab": 1,
				"data":{
					"title":"TUTORIAL_PAGE2_TITLE",
					"description":"TUTORIAL_PAGE2_TEXT",
					"imageClass":"sdc-tutorial-page-2-image"
				}
			},
			{
				"id":3,
				"template": "image-template",
				"tab": 1,
				"data":{
					"title":"TUTORIAL_PAGE3_TITLE",
					"description":"TUTORIAL_PAGE3_TEXT",
					"imageClass":"sdc-tutorial-page-3-image"
				}
			},
			{
				"id":4,
				"template": "image-template",
				"tab": 1,
				"data":{
					"title":"TUTORIAL_PAGE4_TITLE",
					"description":"TUTORIAL_PAGE4_TEXT",
					"imageClass":"sdc-tutorial-page-4-image"
				}
			},
			{
				"id":5,
				"template": "image-template",
				"tab": 1,
				"data":{
					"title":"TUTORIAL_PAGE5_TITLE",
					"description":"TUTORIAL_PAGE5_TEXT",
					"imageClass":"sdc-tutorial-page-5-image"
				}
			},
			{
				"id":6,
				"template": "image-template",
				"tab": 1,
				"data":{
					"title":"TUTORIAL_PAGE6_TITLE",
					"description":"TUTORIAL_PAGE6_TEXT",
					"imageClass":"sdc-tutorial-page-6-image"
				}
			},
			{
				"id":7,
				"template": "image-template",
				"tab": 1,
				"data":{
					"title":"TUTORIAL_PAGE7_TITLE",
					"description":"TUTORIAL_PAGE7_TEXT",
					"imageClass":"sdc-tutorial-page-7-image"
				}
			},
			{
				"id":8,
				"template": "image-template",
				"tab": 1,
				"data":{
					"title":"TUTORIAL_PAGE8_TITLE",
					"description":"TUTORIAL_PAGE8_TEXT",
					"imageClass":"sdc-tutorial-page-8-image"
				}
			},
			{
				"id":9,
				"template": "text-template",
				"tab": 2,
				"data":{
					"title":"TUTORIAL_PAGE9_TITLE",
					"description":"TUTORIAL_PAGE9_TEXT"
				}
			},
			{
				"id":10,
				"template": "image-template",
				"tab": 2,
				"data":{
					"title":"TUTORIAL_PAGE10_TITLE",
					"description":"TUTORIAL_PAGE10_TEXT",
					"imageClass":"sdc-tutorial-page-10-image"
				}
			},
			{
				"id":11,
				"template": "image-template",
				"tab": 2,
				"data":{
					"title":"TUTORIAL_PAGE11_TITLE",
					"description":"TUTORIAL_PAGE11_TEXT",
					"imageClass":"sdc-tutorial-page-11-image"
				}
			},
			{
				"id":12,
				"template": "text-template",
				"tab": 3,
				"data":{
					"title":"TUTORIAL_PAGE12_TITLE",
					"description":"TUTORIAL_PAGE12_TEXT"
				}
			},
			{
				"id":13,
				"template": "image-template",
				"tab": 3,
				"data":{
					"title":"TUTORIAL_PAGE13_TITLE",
					"description":"TUTORIAL_PAGE13_TEXT",
					"imageClass":"sdc-tutorial-page-13-image"
				}
			},
			{
				"id":14,
				"template": "image-template",
				"tab": 3,
				"data":{
					"title":"TUTORIAL_PAGE14_TITLE",
					"description":"TUTORIAL_PAGE14_TEXT",
					"imageClass":"sdc-tutorial-page-14-image"
				}
			},
			{
				"id":15,
				"template": "image-template",
				"tab": 3,
				"data":{
					"title":"TUTORIAL_PAGE15_TITLE",
					"description":"TUTORIAL_PAGE15_TEXT",
					"imageClass":"sdc-tutorial-page-15-image"
				}
			},
			{
				"id":16,
				"template": "image-template",
				"tab": 3,
				"data":{
					"title":"TUTORIAL_PAGE16_TITLE",
					"description":"TUTORIAL_PAGE16_TEXT",
					"imageClass":"sdc-tutorial-page-16-image"
				}
			},
			{
				"id":17,
				"template": "image-template",
				"tab": 3,
				"data":{
					"title":"TUTORIAL_PAGE17_TITLE",
					"description":"TUTORIAL_PAGE17_TEXT",
					"imageClass":"sdc-tutorial-page-17-image"
				}
			},
			{
				"id":18,
				"template": "image-template",
				"tab": 3,
				"data":{
					"title":"TUTORIAL_PAGE18_TITLE",
					"description":"TUTORIAL_PAGE18_TEXT",
					"imageClass":"sdc-tutorial-page-18-image"
				}
			},
			{
				"id":19,
				"template": "image-template",
				"tab": 3,
				"data":{
					"title":"TUTORIAL_PAGE19_TITLE",
					"description":"TUTORIAL_PAGE19_TEXT",
					"imageClass":"sdc-tutorial-page-19-image"
				}
			},
			{
				"id":20,
				"template": "image-template",
				"tab": 3,
				"data":{
					"title":"TUTORIAL_PAGE20_TITLE",
					"description":"TUTORIAL_PAGE20_TEXT",
					"imageClass":"sdc-tutorial-page-20-image"
				}
			},
			{
				"id":21,
				"template": "image-template",
				"tab": 3,
				"data":{
					"title":"TUTORIAL_PAGE21_TITLE",
					"description":"TUTORIAL_PAGE21_TEXT",
					"imageClass":"sdc-tutorial-page-21-image"
				}
			},
			{
				"id":22,
				"template": "image-template",
				"tab": 3,
				"data":{
					"title":"TUTORIAL_PAGE22_TITLE",
					"description":"TUTORIAL_PAGE22_TEXT",
					"imageClass":"sdc-tutorial-page-22-image"
				}
			},
			{
				"id":23,
				"template": "image-template",
				"tab": 3,
				"data":{
					"title":"TUTORIAL_PAGE23_TITLE",
					"description":"TUTORIAL_PAGE23_TEXT",
					"imageClass":"sdc-tutorial-page-23-image"
				}
			},
			{
				"id":24,
				"template": "image-template",
				"tab": 3,
				"data":{
					"title":"TUTORIAL_PAGE24_TITLE",
					"description":"TUTORIAL_PAGE24_TEXT",
					"imageClass":"sdc-tutorial-page-24-image"
				}
			},
			{
				"id":25,
				"template": "image-template",
				"tab": 3,
				"data":{
					"title":"TUTORIAL_PAGE25_TITLE",
					"description":"TUTORIAL_PAGE25_TEXT",
					"imageClass":"sdc-tutorial-page-25-image"
				}
			},
			{
				"id":26,
				"template": "image-template",
				"tab": 3,
				"data":{
					"title":"TUTORIAL_PAGE26_TITLE",
					"description":"TUTORIAL_PAGE26_TEXT",
					"imageClass":"sdc-tutorial-page-26-image"
				}
			},
			{
				"id":27,
				"template": "image-template",
				"tab": 3,
				"data":{
					"title":"TUTORIAL_PAGE27_TITLE",
					"description":"TUTORIAL_PAGE27_TEXT",
					"imageClass":"sdc-tutorial-page-27-image"
				}
			},
			{
				"id":28,
				"template": "image-template",
				"tab": 3,
				"data":{
					"title":"TUTORIAL_PAGE28_TITLE",
					"description":"TUTORIAL_PAGE28_TEXT",
					"imageClass":"sdc-tutorial-page-28-image"
				}
			},
			{
				"id":29,
				"template": "image-template",
				"tab": 3,
				"data":{
					"title":"TUTORIAL_PAGE29_TITLE",
					"description":"TUTORIAL_PAGE29_TEXT",
					"imageClass":"sdc-tutorial-page-29-image"
				}
			},
			{
				"id":30,
				"template": "image-template",
				"tab": 3,
				"data":{
					"title":"TUTORIAL_PAGE30_TITLE",
					"description":"TUTORIAL_PAGE30_TEXT",
					"imageClass":"sdc-tutorial-page-30-image"
				}
			}

		]
	}
};

module.exports = SDC_CONFIG;
