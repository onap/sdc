/*
* Copyright © 2016-2018 European Support Limited
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

const {Then, When, Given} = require('cucumber');
const assert = require('assert');
const util = require('./Utils.js');


/**
 * @module Toggle
 * @description this step will retrun and print to console the list of toggled features with their status
 * @exampleFile Example_Toggle.feature
 * @step I want to list Togglz
 **/

Then('I want to list Togglz', function()  {
    let path = '/togglz' ;
   return util.request(this.context, 'GET', path).then(result => {
   	    const featureList = result.data.features;
   	    console.log(featureList);
   	});

});

/**
 * @module Toggle
 * @description this step will set the status for all toggled features
 * @exampleFile Example_Toggle.feature
 * @step I want to set all Togglz  to be "true/false"
 **/

Then('I want to set all Togglz to be {string}', function(string)  {
    let path = '/togglz/state/' + string ;
    return util.request(this.context, 'PUT', path);
});