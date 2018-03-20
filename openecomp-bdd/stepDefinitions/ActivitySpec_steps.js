/*
 * Copyright © 2016-2018 European Support Limited
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
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
const _ = require('lodash');

When('I want to create an ActivitySpec', function () {
  let path = '/activity-spec';
  return util.request(this.context, 'POST', path, this.context.inputData, false, 'activity_spec').then((result)=> {
    this.context.item = {id : result.data.id, versionId: result.data.versionId};
    this.context.activityspec = {id : result.data.id, versionId: result.data.versionId};
  });
});

When('I want to list ActivitySpecs with status {string}', function (string) {
  let path = '/activity-spec?status='+string;
  return util.request(this.context, 'GET', path, null, false, 'activity_spec').then((result)=> {
    this.context.listCount = result.data.listCount;
  });
});

When('I want to get the ActivitySpec for the current item', function () {
  let path = '/activity-spec/'+ this.context.item.id+'/versions/'+this.context.item.versionId ;
  return util.request(this.context, 'GET', path, null, false, 'activity_spec').then((result)=> {
  });
});

Then('I want to call action {string} on this ActivitySpec item', function(string)  {
  let path = '/activity-spec/'+ this.context.item.id+'/versions/'+this.context.item.versionId+'/actions' ;
  let inputData = JSON.parse('{"action" : "' +string+ '"}');
  return util.request(this.context, 'PUT', path, inputData, false, 'activity_spec')
});