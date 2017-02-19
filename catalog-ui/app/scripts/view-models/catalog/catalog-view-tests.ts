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
/// <reference path="../../references"/>
describe("test catalog-view", () => {

    let $controllerMock:ng.IControllerService;
    let $qMock:ng.IQService;
    let $httpBackendMock:ng.IHttpBackendService;
    let $scopeMock:Sdc.ViewModels.ICatalogViewModelScope;
    let $stateMock:ng.ui.IStateService;
    let $stateParams:any;
    let entityServiceMock;
    let cacheServiceMock;

    beforeEach(angular.mock.module('sdcApp'));

    let getAllEntitiesResponseMock  =  [
        {
            "uniqueId": "855acdc7-7976-4913-9fa6-25220bd5a069",
            "uuid": "8bc54f94-082c-42fa-9049-84767df3ff05",
            "contactId": "qa1234",
            "category": "VoIP Call Control",
            "creationDate": 1447234712398,
            "description": "ddddd",
            "highestVersion": true,
            "icon": "mobility",
            "lastUpdateDate": 1447234712398,
            "lastUpdaterUserId": "cs0008",
            "lastUpdaterFullName": "Carlos Santana",
            "lifecycleState": "NOT_CERTIFIED_CHECKOUT",
            "distributionStatus": "DISTRIBUTION_NOT_APPROVED",
            "projectCode": "233233",
            "name": "mas mas mas mas mas mas mas mas mas mas mas mas ma",
            "version": "0.1",
            "type": 0,
            "tags": [
                "mas mas mas mas mas mas mas mas mas mas mas mas ma"
            ],
            "systemName": "MasMasMasMasMasMasMasMasMasMasMasMasMa",
            "vnf": true,
            "$$hashKey": "object:30"
        },
        {
            "uniqueId": "4bb577ce-cb2c-4cb7-bb39-58644b5e73cb",
            "uuid": "e27f4723-c9ec-4160-89da-dbf84d19a7e3",
            "contactId": "qa1111",
            "category": "Mobility",
            "creationDate": 1447238503181,
            "description": "aqa",
            "highestVersion": true,
            "icon": "call_controll",
            "lastUpdateDate": 1447248991388,
            "lastUpdaterUserId": "jm0007",
            "lastUpdaterFullName": "Joni Mitchell",
            "lifecycleState": "CERTIFIED",
            "distributionStatus": "DISTRIBUTION_REJECTED",
            "projectCode": "111111",
            "name": "martin18",
            "version": "1.0",
            "type": 0,
            "tags": [
                "martin18"
            ],
            "systemName": "Martin18",
            "vnf": true
        },
        {
            "uniqueId": "f192f4a6-7fbf-42e4-a546-37509df28dc1",
            "uuid": "0b77dc0d-222e-4d10-85cd-e420c9481417",
            "contactId": "fd1212",
            "category": "Application Layer 4+/Web Server",
            "creationDate": 1447233679778,
            "description": "geefw",
            "highestVersion": true,
            "icon": "database",
            "lastUpdateDate": 1447233681582,
            "lastUpdaterUserId": "cs0008",
            "lastUpdaterFullName": "Carlos Santana",
            "lifecycleState": "NOT_CERTIFIED_CHECKOUT",
            "name": "ger",
            "version": "0.1",
            "type": 1,
            "tags": [
                "ger"
            ],
            "vendorName": "fewwfe",
            "vendorRelease": "fewew",
            "systemName": "Ger",
            "$$hashKey": "object:31"
        },
        {
            "uniqueId": "78392d08-1859-47c2-b1f2-1a35b7f8c30e",
            "uuid": "8cdd63b2-6a62-4376-9012-624f424f71d4",
            "contactId": "qw1234",
            "category": "Application Layer 4+/Application Servers",
            "creationDate": 1447234046114,
            "description": "test",
            "highestVersion": true,
            "icon": "router",
            "lastUpdateDate": 1447234050545,
            "lastUpdaterUserId": "cs0008",
            "lastUpdaterFullName": "Carlos Santana",
            "lifecycleState": "NOT_CERTIFIED_CHECKOUT",
            "name": "test",
            "version": "0.1",
            "type": 1,
            "tags": [
                "test"
            ],
            "vendorName": "test",
            "vendorRelease": "test",
            "systemName": "Test",
            "$$hashKey": "object:32"
        },
        {
            "uniqueId": "939e153d-2236-410f-b4a9-3b4bf8c79c9e",
            "uuid": "84862547-4f56-4058-b78e-40df5f374d7e",
            "contactId": "qw1234",
            "category": "Application Layer 4+/Application Servers",
            "creationDate": 1447235242560,
            "description": "jlk",
            "highestVersion": true,
            "icon": "database",
            "lastUpdateDate": 1447235328062,
            "lastUpdaterUserId": "cs0008",
            "lastUpdaterFullName": "Carlos Santana",
            "lifecycleState": "NOT_CERTIFIED_CHECKIN",
            "name": "new",
            "version": "0.1",
            "type": 1,
            "tags": [
                "new"
            ],
            "vendorName": "e",
            "vendorRelease": "e",
            "systemName": "New",
            "$$hashKey": "object:33"
        },
        {
            "uniqueId": "ece818e0-fd59-477a-baf6-e27461a7ce23",
            "uuid": "8db823c2-6a9c-4636-8676-f5e713270dd7",
            "contactId": "uf2345",
            "category": "Network Layer 2-3/Router",
            "creationDate": 1447235352429,
            "description": "u",
            "highestVersion": true,
            "icon": "network",
            "lastUpdateDate": 1447235370064,
            "lastUpdaterUserId": "cs0008",
            "lastUpdaterFullName": "Carlos Santana",
            "lifecycleState": "NOT_CERTIFIED_CHECKOUT",
            "name": "u",
            "version": "0.1",
            "type": 1,
            "tags": [
                "u"
            ],
            "vendorName": "u",
            "vendorRelease": "u",
            "systemName": "U",
            "$$hashKey": "object:34"
        }
    ];

    let resourceCategoriesResponseMock = [{"name":"Network L2-3","normalizedName":"network l2-3","uniqueId":"resourceNewCategory.network l2-3","subcategories":[{"name":"Gateway","normalizedName":"gateway","uniqueId":"resourceNewCategory.network l2-3.gateway","icons":["gateway"]},{"name":"Infrastructure","normalizedName":"infrastructure","uniqueId":"resourceNewCategory.network l2-3.infrastructure","icons":["ucpe"]},{"name":"WAN Connectors","normalizedName":"wan connectors","uniqueId":"resourceNewCategory.network l2-3.wan connectors","icons":["network","connector","port"]},{"name":"LAN Connectors","normalizedName":"lan connectors","uniqueId":"resourceNewCategory.network l2-3.lan connectors","icons":["network","connector","port"]},{"name":"Router","normalizedName":"router","uniqueId":"resourceNewCategory.network l2-3.router","icons":["router","vRouter"]}]},{"name":"Network L4+","normalizedName":"network l4+","uniqueId":"resourceNewCategory.network l4+","subcategories":[{"name":"Common Network Resources","normalizedName":"common network resources","uniqueId":"resourceNewCategory.network l4+.common network resources","icons":["network"]}]},{"name":"Application L4+","normalizedName":"application l4+","uniqueId":"resourceNewCategory.application l4+","subcategories":[{"name":"Load Balancer","normalizedName":"load balancer","uniqueId":"resourceNewCategory.application l4+.load balancer","icons":["loadBalancer"]},{"name":"Media Servers","normalizedName":"media servers","uniqueId":"resourceNewCategory.application l4+.media servers","icons":["applicationServer"]},{"name":"Application Server","normalizedName":"application server","uniqueId":"resourceNewCategory.application l4+.application server","icons":["applicationServer"]},{"name":"Database","normalizedName":"database","uniqueId":"resourceNewCategory.application l4+.database","icons":["database"]},{"name":"Call Control","normalizedName":"call control","uniqueId":"resourceNewCategory.application l4+.call control","icons":["call_controll"]},{"name":"Border Element","normalizedName":"border element","uniqueId":"resourceNewCategory.application l4+.border element","icons":["borderElement"]},{"name":"Web Server","normalizedName":"web server","uniqueId":"resourceNewCategory.application l4+.web server","icons":["applicationServer"]},{"name":"Firewall","normalizedName":"firewall","uniqueId":"resourceNewCategory.application l4+.firewall","icons":["firewall"]}]},{"name":"Generic","normalizedName":"generic","uniqueId":"resourceNewCategory.generic","subcategories":[{"name":"Database","normalizedName":"database","uniqueId":"resourceNewCategory.generic.database","icons":["database"]},{"name":"Abstract","normalizedName":"abstract","uniqueId":"resourceNewCategory.generic.abstract","icons":["objectStorage","compute"]},{"name":"Network Elements","normalizedName":"network elements","uniqueId":"resourceNewCategory.generic.network elements","icons":["network","connector"]},{"name":"Infrastructure","normalizedName":"infrastructure","uniqueId":"resourceNewCategory.generic.infrastructure","icons":["connector"]}]},{"name":"NewCategory","normalizedName":"newcategory","uniqueId":"resourceNewCategory.newcategory","subcategories":[{"name":"MyNewSubCategory","normalizedName":"mynewsubcategory","uniqueId":"resourceNewCategory.newcategory.mynewsubcategory"}]}];

    let getAllEntitiesDefered:ng.IDeferred<any> = null;

    beforeEach(angular.mock.inject((_$controller_:ng.IControllerService,
                                    _$httpBackend_:ng.IHttpBackendService,
                                    _$rootScope_,
                                    _$q_:ng.IQService,
                                    _$state_:ng.ui.IStateService,
                                    _$stateParams_:any) => {

        $controllerMock  = _$controller_;
        $httpBackendMock = _$httpBackend_
        $scopeMock       = _$rootScope_.$new();
        $qMock           = _$q_;
        $stateMock       = _$state_;
        $stateParams = _$stateParams_;


        //handle all http request thet not relevant to the tests
        $httpBackendMock.expectGET(/.*languages\/en_US.json.*/).respond(200, JSON.stringify({}));
        $httpBackendMock.expectGET(/.*rest\/version.*/).respond(200, JSON.stringify({}));
        $httpBackendMock.expectGET(/.*configuration\/ui.*/).respond(200, JSON.stringify({}));
        $httpBackendMock.expectGET(/.*user\/authorize.*/).respond(200, JSON.stringify({}));
        $httpBackendMock.expectGET(/.*categories\/services.*/).respond(200, JSON.stringify({}));
        $httpBackendMock.expectGET(/.*categories\/resources.*/).respond(200, JSON.stringify({}));
        $httpBackendMock.expectGET(/.*categories\/products.*/).respond(200, JSON.stringify({}));
        $httpBackendMock.expectGET('http://feHost:8181/sdc1/feProxy/rest/version').respond(200, JSON.stringify({}));

        /**
         * Mock the service
         * @type {any}
         */
        getAllEntitiesDefered = $qMock.defer();
        getAllEntitiesDefered.resolve(getAllEntitiesResponseMock);

        cacheServiceMock = jasmine.createSpyObj('cacheServiceMock', ['get']);
        cacheServiceMock.get.and.callFake(function(string){return resourceCategoriesResponseMock;});
        /*
        cacheServiceMock.get.and.callFake(function(value:string){
            switch(value){
                case 'serviceCategories':
                    console.log('serviceCategories');
                    break;
                case 'resourceCategories':
                    console.log('resourceCategories');
                    break;
                case 'productCategories':
                    console.log('productCategories');
                    break;
                default :
                    console.log('default');
                    break;
            }
        });
        */

        entityServiceMock = jasmine.createSpyObj('entityServiceMock', ['getCatalog']);
        entityServiceMock.getCatalog.and.returnValue(getAllEntitiesDefered.promise);

        // $stateParams['show'] = '';

        /**
         * Need to inject into the controller only the objects that we want to MOCK
         * those that we need to change theirs behaviors
         */
        $controllerMock(Sdc.ViewModels.CatalogViewModel, {
            '$scope': $scopeMock,
            '$stateParams': $stateParams,
            'Sdc.Services.EntityService': entityServiceMock,
            'Sdc.Services.CacheService': cacheServiceMock
        });

    }));


        beforeEach(function () {
        });

        describe("test GUI events on checkbox type resource click", function () {

            /**
             * The function checks only for resource type.
             * Select the Resource and verify that the sub resources are selected.
             *
             */
            it('test onComponentTypeClick (check select checkbox of Resource type)', function () {
                $scopeMock.$apply();
                $scopeMock.checkboxesFilter.selectedComponentTypes = ['Resource'];
                $scopeMock.gui.onComponentTypeClick('Resource');
                expect($scopeMock.checkboxesFilter.selectedResourceSubTypes.length === 4).toBeTruthy();
            });

            /**
             * The function checks only for resource type.
             * Un select the Resource and verify that the sub resources are selected.
             *
             */
            it('test onComponentTypeClick (check un select checkbox of Resource type)', function () {
                $scopeMock.$apply();
                $scopeMock.gui.onComponentTypeClick('Resource');
                expect($scopeMock.checkboxesFilter.selectedResourceSubTypes.length === 0).toBeTruthy();
            });

        });

        describe("test GUI events on checkbox main category click -> sub categories are selected", function () {

            /**
             * The function checks that after selecting 2 main categories, the subcategories are selected also.
             *
             */
            it('test onComponentTypeClick (check select checkbox of Resource type)', function () {
                let category1 = resourceCategoriesResponseMock[0];
                let category2 = resourceCategoriesResponseMock[1];

                $scopeMock.$apply();
                $scopeMock.checkboxesFilter.selectedCategoriesModel = [category1.uniqueId, category2.uniqueId];
                $scopeMock.gui.onCategoryClick(category1);
                $scopeMock.gui.onCategoryClick(category2);

                expect($scopeMock.checkboxesFilter.selectedCategoriesModel.length===8).toBeTruthy();
            });

        });


});
