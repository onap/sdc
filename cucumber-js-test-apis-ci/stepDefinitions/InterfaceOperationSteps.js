/*
 * Copyright © 2016-2017 European Support Limited
 *
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
 */
const {Then, When} = require('cucumber');
const assert = require('assert');
const util = require('../cucumber-common/utils/Utils.js');


When('I want to create a VF', function()  {
    let inputData = util.getJSONFromFile('resources/json/createVFWithoutCSAR.json');

    var resourceName = util.random();
    inputData.name =  resourceName;
    inputData.tags[0] = resourceName;

    var type = "resources";
    let path = '/catalog/' + type;
    return util.request(this.context, 'POST', path,  inputData, 'catalog').then(result => {
        this.context.component = {uniqueId : result.data.uniqueId, type : type, id : result.data.inputs[0].uniqueId};
});
});

When('I want to create a Service', function()  {
    let inputData = util.getJSONFromFile('resources/json/createService.json');

    var serviceName = util.random();
    inputData.name =  serviceName;
    inputData.tags[0] = serviceName;

    var type = "services";
    let path = '/catalog/' + type;
    return util.request(this.context, 'POST', path,  inputData,  'catalog').then(result => {
        this.context.component = {uniqueId : result.data.uniqueId, type : type, id : result.data.inputs[0].uniqueId};
});
});

function makeType() {
    var text = "";
    var possible = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";

    for (var i = 0; i < 5; i++)
        text += possible.charAt(Math.floor(Math.random() * possible.length));

    return text;
}

When('I want to create an Operation', function()  {
    let path = '/catalog/' + this.context.component.type + '/' + this.context.component.uniqueId + '/interfaceOperations';
    let inputData = util.getJSONFromFile('resources/json/interfaceOperation/createInterfaceOperations.json');
    var operationName = makeType();
    var interfaceType = makeType();
    inputData.interfaces.interface1.type = interfaceType;
    inputData.interfaces.interface1.operations.delete.name = operationName;
    inputData.interfaces.interface1.operations.delete.inputs.listToscaDataDefinition[0].name = util.random();
    inputData.interfaces.interface1.operations.delete.inputs.listToscaDataDefinition[0].inputId = this.context.component.id;
    inputData.interfaces.interface1.operations.delete.outputs.listToscaDataDefinition[0].name = util.random();
    inputData.interfaces.interface1.operations.delete.description = operationName + " description";

    return util.request(this.context, 'POST', path, inputData, 'catalog').then(result => {
            {intOperations = result.data.interfaces[0].operations};
        this.context.interface = {  interfaceUniqueId : result.data.interfaces[0].uniqueId,
                                    interfaceType : result.data.interfaces[0].type,
                                    operationUniqueId : Object.keys(intOperations)[0]
        };
});
});

When('I want to update an Operation', function () {
    let inputData = util.getJSONFromFile('resources/json/interfaceOperation/updateInterfaceOperation.json');
    let path = '/catalog/'+ this.context.component.type + '/'+ this.context.component.uniqueId +'/interfaceOperations';
    inputData.interfaces.interface1.operations.delete.uniqueId = this.context.interface.operationUniqueId;
    inputData.interfaces.interface1.type=this.context.interface.interfaceType;
    inputData.interfaces.interface1.operations.delete.inputs.listToscaDataDefinition[0].name = util.random();
    inputData.interfaces.interface1.operations.delete.inputs.listToscaDataDefinition[0].inputId = this.context.component.id;
    inputData.interfaces.interface1.operations.delete.outputs.listToscaDataDefinition[0].name = util.random();

    return util.request(this.context, 'PUT', path, inputData, 'catalog').then((result)=> {
    {intOperations = result.data.interfaces[0].operations};
            this.context.interface =  { interfaceUniqueId : result.data.interfaces[0].uniqueId,
                                        interfaceType : result.data.interfaces[0].type,
                                        operationUniqueId : Object.keys(intOperations)[0]
    };
});
});

When('I want to get an Operation by Id', function () {
    let path = '/catalog/'+ this.context.component.type + '/' + this.context.component.uniqueId + '/interfaces/' +
        this.context.interface.interfaceUniqueId + '/operations/' +this.context.interface.operationUniqueId ;
    return util.request(this.context, 'GET', path, null, 'catalog').then((result)=> {

    {intOperations = result.data.interfaces[0].operations};
    this.context.interface =  { interfaceUniqueId : result.data.interfaces[0].uniqueId,
                                interfaceType : result.data.interfaces[0].type,
                                operationUniqueId : Object.keys(intOperations)[0]
    };
    });

});

When('I want to list Operations', function () {
    let path = '/catalog/'+ this.context.component.type + '/' + this.context.component.uniqueId + '/filteredDataByParams?include=interfaces';
    return util.request(this.context, 'GET', path, null, 'catalog').then((result)=> {
    });
});


When('I want to delete an Operation', function()  {
    let path = '/catalog/'+ this.context.component.type + '/'+ this.context.component.uniqueId +'/interfaces/' +
        this.context.interface.interfaceUniqueId + '/operations/' +this.context.interface.operationUniqueId ;
    return util.request(this.context, 'DELETE', path, null, 'catalog');
});


When('I want to checkin this component', function () {
    let path = '/catalog/'+ this.context.component.type + '/' + this.context.component.uniqueId + '/lifecycleState/CHECKIN' ;
    let inputData = {userRemarks: 'checkin'};
    return util.request(this.context, 'POST', path, inputData, 'catalog').then((result)=> {
    this.context.component = {uniqueId : result.data.uniqueId, type : this.context.component.type};
});
});


Then('I want to submit this component', function () {
    let path = '/catalog/'+ this.context.component.type + '/' + this.context.component.uniqueId + '/lifecycleState/certificationRequest' ;
    let inputData = {userRemarks: 'submit'};
    return util.request(this.context, 'POST', path, inputData, 'catalog').then((result)=> {
    this.context.component = {uniqueId : result.data.uniqueId};
});
});

Then('I want to certify this component', function () {
    let path = '/catalog/'+ this.context.component.type +'/' + this.context.component.uniqueId + '/lifecycleState/certify' ;
    let inputData = {userRemarks: 'certify'};
    return util.request(this.context, 'POST', path, inputData, 'catalog').then((result)=> {
    this.context.component = {uniqueId : result.data.uniqueId};
});
});