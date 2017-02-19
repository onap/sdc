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
/// <reference path="../../../references"/>

describe("property form View Model ", () => {

    let $controllerMock:ng.IControllerService;
    let $qMock:ng.IQService;
    let $httpBackendMock:ng.IHttpBackendService;
    let $scopeMock:Sdc.ViewModels.Wizard.IPropertyFormViewModelScope;
    let $stateMock:ng.ui.IStateService;
    let $stateParams:any;
    let component = {
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
    };


    beforeEach(angular.mock.module('sdcApp'));

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
        // $httpBackendMock.expectGET(/.*resources\/certified\/abstract.*/).respond(200, JSON.stringify({}));
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
        //getAllEntitiesDefered = $qMock.defer();
        //getAllEntitiesDefered.resolve(getAllEntitiesResponseMock);
        //entityServiceMock = jasmine.createSpyObj('entityServiceMock', ['getAllComponents']);
        //entityServiceMock.getAllComponents.and.returnValue(getAllEntitiesDefered.promise);

        // $stateParams['show'] = '';

        /**
         * Need to inject into the controller only the objects that we want to MOCK
         * those that we need to change theirs behaviors
         */
        $controllerMock(Sdc.ViewModels.Wizard.PropertyFormViewModel, {
            '$scope': $scopeMock,
            'property': new Sdc.Models.PropertyModel(),
            'component': component,
        });

    }));

    describe("when Controller 'PropertyFormViewModel' created", () => {

        it('should have a regexp per each type', () => {
            $scopeMock.$apply();
            expect(Object.keys($scopeMock.listRegex).length).toBe($scopeMock.editPropertyModel["simpleTypes"].length);
        });

        it('should have equal regexps for map and list', () => {
            $scopeMock.$apply();
            expect(Object.keys($scopeMock.listRegex).length).toBe(Object.keys($scopeMock.mapRegex).length);
        });

    });

    /*describe("when Controller 'DashboardViewModel' created", () => {

        it('should generate all entities', () => {
            $scopeMock.$apply();
            expect($scopeMock.components.length).toBe(getAllEntitiesResponseMock.length);
        });


        it('should show tutorial page ', () => {
            $stateParams.show = 'tutorial';

            $controllerMock(Sdc.ViewModels.DashboardViewModel, {
                '$scope': $scopeMock,
                '$stateParams': $stateParams,
                'Sdc.Services.EntityService': entityServiceMock,
                //to complete injects
            });

            $scopeMock.$apply();
            expect($scopeMock.isFirstTime).toBeTruthy();
            expect($scopeMock.showTutorial).toBeTruthy();
        });

    });


    describe("when function 'entitiesCount' invoked", () => {

        beforeEach(() => {
            $controllerMock(Sdc.ViewModels.DashboardViewModel, {
                '$scope': $scopeMock,
                'Sdc.Services.EntityService': entityServiceMock,
            });
            $scopeMock.$apply();
        });

        it('should return entities count per folder', () => {

        });


    });*/
});
