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
const util = require('./Utils.js');


When('I want to create a VF', function()  {
    let inputData = util.getJSONFromFile('resources/json/operation/createVF.json');

    inputData.name =  util.random();
    inputData.tags[0] = util.random();

    var type = "resources";
    let path = '/catalog/' + type;
    return util.request(this.context, 'POST', path,  inputData, false, 'vf').then(result => {
        this.context.component = {uniqueId : result.data.uniqueId, type : type, id : result.data.inputs[0].uniqueId};
});
});

When('I want to create a Service', function()  {
    let inputData = util.getJSONFromFile('resources/json/operation/createService.json');

    inputData.name =  util.random();
    inputData.tags[0] = util.random();

    var type = "services";
    let path = '/catalog/' + type;
    return util.request(this.context, 'POST', path,  inputData, false, 'vf').then(result => {
        this.context.component = {uniqueId : result.data.uniqueId, type : type, id : null};
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
    let inputData;
    let path = '/catalog/' + this.context.component.type + '/' + this.context.component.uniqueId + '/interfaceOperations';
    if(this.context.component.id !== null) {
        inputData = util.getJSONFromFile('resources/json/operation/createOperationWithProperties.json');

        inputData.interfaceOperations.create.inputParams.listToscaDataDefinition[0].name = util.random();
        inputData.interfaceOperations.create.inputParams.listToscaDataDefinition[0].property = this.context.component.id;
    }
    else {
        inputData  = util.getJSONFromFile('resources/json/operation/createOperation.json');
    }
    inputData.interfaceOperations.create.operationType = makeType();
    inputData.interfaceOperations.create.description = makeType();
    //inputData.interfaceOperations.create.workflowId = makeType();
    //inputData.interfaceOperations.create.workflowVersionId = makeType();

    return util.request(this.context, 'POST', path, inputData, false, 'vf').then(result => {
    this.context.operation = {uniqueId : result.data.uniqueId, operationType : result.data.operationType};
});
});

When('I want to list Operations', function () {
    let path = '/catalog/'+ this.context.component.type + '/' + this.context.component.uniqueId + '/filteredDataByParams?include=interfaces';
    return util.request(this.context, 'GET', path, null, false, 'vf').then((result)=> {
    });
});

When('I want to get an Operation by Id', function () {
    let path = '/catalog/'+ this.context.component.type + '/' + this.context.component.uniqueId + '/interfaceOperations/' + this.context.operation.uniqueId;
    return util.request(this.context, 'GET', path, null, false, 'vf').then((result)=> {
    this.context.operation = {uniqueId : result.data.uniqueId, operationType : result.data.operationType};
});
});

When('I want to update an Operation', function () {
    let inputData = util.getJSONFromFile('resources/json/operation/updateOperation.json');
    let path = '/catalog/'+ this.context.component.type + '/'+ this.context.component.uniqueId +'/interfaceOperations';
    inputData.interfaceOperations.create.uniqueId = this.context.operation.uniqueId;
    inputData.interfaceOperations.create.operationType = this.context.operation.operationType;
    return util.request(this.context, 'PUT', path, inputData, false, 'vf').then((result)=> {
    this.context.operation = {uniqueId : result.data.uniqueId, operationType : result.data.operationType};
});
});


When('I want to delete an Operation', function()  {
    let path = '/catalog/'+ this.context.component.type + '/'+ this.context.component.uniqueId +'/interfaceOperations/' + this.context.operation.uniqueId;
    return util.request(this.context, 'DELETE', path, null, false, 'vf');
});


When('I want to checkin this component', function () {
    let path = '/catalog/'+ this.context.component.type + '/' + this.context.component.uniqueId + '/lifecycleState/CHECKIN' ;
    let inputData = {userRemarks: 'checkin'};
    return util.request(this.context, 'POST', path, inputData, false, 'vf').then((result)=> {
    this.context.component = {uniqueId : result.data.uniqueId, type : this.context.component.type};
});
});


Then('I want to submit this component', function () {
    let path = '/catalog/'+ this.context.component.type + '/' + this.context.component.uniqueId + '/lifecycleState/certificationRequest' ;
    let inputData = {userRemarks: 'submit'};
    return util.request(this.context, 'POST', path, inputData, false, 'vf').then((result)=> {
    this.context.vf = {uniqueId : result.data.uniqueId};
});
});

Then('I want to certify this component', function () {
    let path = '/catalog/'+ this.context.component.type +'/' + this.context.component.uniqueId + '/lifecycleState/certify' ;
    let inputData = {userRemarks: 'certify'};
    return util.request(this.context, 'POST', path, inputData, false, 'vf').then((result)=> {
    this.context.component = {uniqueId : result.data.uniqueId};
});
});