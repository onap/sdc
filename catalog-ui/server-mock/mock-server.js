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
var mockApis = require('../configurations/mock.json').sdcConfig;
//var mockUris = require('../configurations/mock.json');
var cors = require('cors');
var multer  = require('multer')
var basePathToMockData = './mock-data/';
var app = express();

var allowedHeaders = 'Content-Type,Authorization,If-Modified-Since,';
allowedHeaders += mockApis.cookie.userIdSuffix;
allowedHeaders += ','+mockApis.cookie.userEmail;
allowedHeaders += ','+mockApis.cookie.userFirstName;
allowedHeaders += ','+mockApis.cookie.userLastName;
allowedHeaders += ','+mockApis.cookie.xEcompRequestId;



app.use(cors({
    // origin: '*',
    origin: function(origin, callback) {
      callback(null, true);
    },
    methods: 'GET, POST, PUT, DELETE',
    allowedHeaders: allowedHeaders,
    credentials: true
}));

//set cookie middleware
app.use(function(req, res, next) {

  res.cookie(mockApis.cookie.userIdSuffix, req.headers[mockApis.cookie.userIdSuffix] || mockApis.userTypes.designer.userId );
  res.cookie(mockApis.cookie.userEmail, req.headers[mockApis.cookie.userEmail] ||  mockApis.userTypes.designer.email);
  res.cookie(mockApis.cookie.userFirstName, req.headers[mockApis.cookie.userFirstName] ||  mockApis.userTypes.designer.firstName);
  res.cookie(mockApis.cookie.userLastName, req.headers[mockApis.cookie.userLastName] ||  mockApis.userTypes.designer.lastName);
  res.cookie(mockApis.cookie.xEcompRequestId, req.headers[mockApis.cookie.xEcompRequestId] ||  mockApis.userTypes.designer.lastName);
  next();
});

var userRoutes = require('./routes/user');
app.use('/v1/user', userRoutes);
var resourceRoutes = require('./routes/resource');
app.use('/v1/resource', resourceRoutes);
var templateRoutes = require('./routes/template');
app.use('/v1/template', templateRoutes);
var propertyRoutes = require('./routes/property');
app.use('/v1/resource/:resourceId/property', propertyRoutes);
var resourcesRoutes = require('./routes/resources');
app.use('/v1/catalog/resources', resourcesRoutes);

/******************************************* MOCKS ENPOINTS *************************************************/
/* get user details */
// app.get(mockApis.api.GET_user, function (req, res) {
//   var user = require(basePathToMockData+'user/user.json');
//   res.send(user);
// });

/* get elements */
app.get(mockApis.api.GET_element, function (req, res) {

  var element = require(basePathToMockData+'element/element.json');
  res.send(element);
});

/* get elements */
app.get(mockApis.api.GET_catalog, function (req, res) {

  var element = require(basePathToMockData+'element/element.json');
  res.send(element);
});

/* get categories */
app.get(mockApis.api.GET_category, function (req, res) {

  var categories = require(basePathToMockData+'category/category.json');
  res.send(categories);
});


/* get categories */
app.get(mockApis.api.GET_configuration_ui, function (req, res) {

  var categories = require(basePathToMockData+'artifact/artifact-types.json');
  res.send(categories);
});








//upload artifact file
app.use(multer({ dest: './uploads/',
  rename: function (fieldname, filename) {
    return filename+Date.now();
  },
  onFileUploadStart: function (file) {
    console.log(file.originalname + ' is starting ...')
  },
  onFileUploadComplete: function (file) {
    console.log(file.fieldname + ' uploaded to  ' + file.path)
    done=true;
  }
}));

var done=false;
app.post(mockApis.api.GET_resource_artifact,function(req,res){
  if(done==true){
    console.log(req.files);
    res.end("File uploaded.");
  }
});


/**************************************************** *******************************************************/

var server = app.listen(9999, function () {
    console.log('mock server listening on port %d', server.address().port);
});
