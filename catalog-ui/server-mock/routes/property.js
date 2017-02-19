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

var express = require('express');
var properties = require('../mock-data/resource/properties.json');

var router = express.Router();

router.get('/', function (req, res) {
  console.log('query');
  res.send(properties);
});

router.post('/:id', function (req, res) {
  console.log("post /:id", req);
  res.send(properties[0]);
});

router.get('/:id', function (req, res) {
  res.send(properties[0]);
});


router.post('/', function (req, res) {
  console.log("post ", req);
  res.send(properties[0]);
});
module.exports= router;
