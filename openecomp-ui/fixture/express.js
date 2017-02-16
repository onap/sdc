/*-
 * ============LICENSE_START=======================================================
 * SDC
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights reserved.
 * ================================================================================
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * ============LICENSE_END=========================================================
 */

var args = process.argv.slice(2);

function defineRoutes(router) {

	//LICENSE-MODELS
	router.get('/v1.0/vendor-license-models', licenseModelsList);

	//FEATURE-GROUP
	router.get('/v1.0/vendor-license-models/:licenseModelId/feature-groups', featureGroupList);
	router.get('/v1.0/vendor-license-models/:licenseModelId/feature-groups/:featureGroupId', featureGroup);
	router.post('/v1.0/vendor-license-models/:licenseModelId/feature-groups', addFeatureGroup);
	router.delete('/v1.0/vendor-license-models/:licenseModelId/feature-groups/:featureGroupId', deletefeatureGroup);
	router.put('/v1.0/vendor-license-models/:licenseModelId/feature-groups/:featureGroupId', updatefeatureGroup);



	//LICENSE-AGREEMENT
	router.get('/v1.0/vendor-license-models/:licenseModelId/license-agreements', licenseAgreementList);
	router.post('/v1.0/vendor-license-models/:licenseModelId/license-agreements/', addLicenseAgreement);
	router.delete('/v1.0/vendor-license-models/:licenseModelId/license-agreements/:licenseAgreementId', deleteLicenseAgreement);
	router.put('/v1.0/vendor-license-models/:licenseModelId/license-agreements/:licenseAgreementId', updateLicenseAgreement);

	//ENTITLEMENT POOLS
	router.get('/v1.0/vendor-license-models/:licenseModelId/entitlement-pools', entitlementPoolsList);
	router.post('/v1.0/vendor-license-models/:licenseModelId/entitlement-pools', addEntitlementPool);
	router.put('/v1.0/vendor-license-models/:licenseModelId/entitlement-pools/:entitlementPoolId', updateEntitlementPool);
	router.delete('/v1.0/vendor-license-models/:licenseModelId/entitlement-pools/:entitlementPoolId', deleteEntitlementPool);

	//LICENSE KEY GROUPS
	router.get('/v1.0/vendor-license-models/:licenseModelId/license-key-groups', licenseKeyGroupsList);
	router.post('/v1.0/vendor-license-models/:licenseModelId/license-key-groups', addLicenseKeyGroup);
	router.delete('/v1.0/vendor-license-models/:licenseModelId/license-key-groups/:licenseKeyGroupId', deleteLicenseKeyGroup);
	router.put('/v1.0/vendor-license-models/:licenseModelId/license-key-groups/:licenseKeyGroupId', updateLicenseKeyGroup);

	//VENDOR SOFTWARE PRODUCT

	router.post('/v1.0/vendor-software-products/:vspId/upload', softwareProductUpload);
	router.get('/v1.0/vendor-software-products/:vspId', getSoftwareProduct);
	router.get('/v1.0/vendor-software-products', softwareProductList);

	router.put('/v1.0/vendor-software-products/:vspId/processes/:prcId', putSoftwareProductProcess);
	router.post('/v1.0/vendor-software-products/:vspId/processes', postSoftwareProductProcess);
}


function licenseModelsList(req, res) {
	res.json(require('./data/licenseModels'));
}

function featureGroupList(req, res) {
	res.json(require('./data/featureGroups'));
}

function featureGroup(req, res) {
	res.json(require('./data/featureGroup'));
}

function deletefeatureGroup(req, res) {
	res.json({
		returnCode: 'OK'
	});
}


function updatefeatureGroup(req, res) {
	res.json({
		returnCode: 'OK'
	});
}

function addFeatureGroup(req,res) {
	var  id = Math.floor(Math.random() * (100 - 1) + 1).toString();
	res.json({
		returnCode: 'OK',
		value: id
	})
}

/** ENTITLEMENT POOLS **/
function entitlementPoolsList(req, res) {
	res.json(require('./data/entitlementPools'));
}

function updateEntitlementPool(req, res) {
	res.json({
		returnCode: 'OK'
	});
}

function addEntitlementPool(req,res) {
	var  id = Math.floor(Math.random() * (100 - 1) + 1).toString();
	res.json({
		returnCode: 'OK',
		value: id
	})
}

function deleteEntitlementPool(req, res) {
	res.json({
		returnCode: 'OK'
	});
}

/** LICENSE KEY GROUPS */

function licenseKeyGroupsList(req, res) {
	res.json(require('./data/licenseKeyGroups'));
}

function addLicenseKeyGroup(req,res) {
	var  id = Math.floor(Math.random() * (100 - 1) + 1).toString();
	res.json({
		returnCode: 'OK',
		value: id
	})
}

function deleteLicenseKeyGroup(req, res) {
	res.json({
		returnCode: 'OK'
	});
}

function updateLicenseKeyGroup(req, res) {
	res.json({
		returnCode: 'OK'
	});
}

function licenseAgreementList(req, res) {
	res.json(require('./data/licenseAgreementList'));
}


function addLicenseAgreement(req,res) {
	var  id = Math.floor(Math.random() * (100 - 1) + 1).toString();
	res.json({
		returnCode: 'OK',
		value: id
	})
}
function deleteLicenseAgreement(req, res) {
	res.json({
		returnCode: 'OK'
	});
}
function updateLicenseAgreement(req, res) {
	res.json({
		returnCode: 'OK'
	});
}

/** VENDOR SOFTWARE PRODUCT */

function softwareProductUpload(req, res) {
	res.json({
		status: 'SUCCESS'
	});
}

function getSoftwareProduct(req, res) {
	res.json(require('./data/softwareProduct'));
}


function putSoftwareProductProcess(req, res) {
	res.json({
		status: 'SUCCESS'
	});
}

function postSoftwareProductProcess(req, res) {
	var  id = Math.floor(Math.random() * (100 - 1) + 1).toString();
	res.json({
		returnCode: 'OK',
		value: id
	});
}




function createFixtureServer(port) {
	var express = require('express');
	var app = express();
	var bodyParser = require('body-parser');
	app.use(bodyParser.urlencoded({extended: true}));
	app.use(bodyParser.json());

	var router = express.Router();

	defineRoutes(router);

	app.use('/api', router);
	app.use('/onboarding-api', router);
	app.use('/sdc1/feProxy/onboarding-api', router);

	app.listen(port);

	console.log('Fixture server is up. port->', port);
	//console.log(router.stack);
	return app;
}

/** SOFTWARE PRODUCT LIST **/
function softwareProductList(req, res) {
	res.json(require('./data/softwareProductList'));
}


createFixtureServer(args[0]);
