# Prerequisites

1. install [node.js](http://nodejs.org/download/)
2. install [git](http://git-scm.com/). __Make sure to select the option to add git into $PATH__
3. install dependencies [express,cors] npm install express, npm install cors



# Create the server file 
Example:

#############################################
ar express = require('express');
var mockUris = require('../configurations/mock.json');
var cors = require('cors');


var app = express();

// declare server cross browser 
app.use(cors({
    origin: '*',
    methods: 'GET, POST, PUT, DELETE',
    allowedHeaders: 'Content-Type,Authorization,If-Modified-Since'
}));

/******************************************* MOCKS ENPOINTS *************************************************/
/* poiFind */
app.get('/v1' + mockUris.generalConf.getPoiFind.split('v1')[1], function (req, res) {
    var pois = require('./data/poi/poi-search.json'); // the json response for the api call
    res.send(pois);
});

/**************************************************** *******************************************************/
// declare  server listener  port
var server = app.listen(9999, function () {
    console.log('mock server listening on port %d', server.address().port);
});

################################

#create mockDate

1. create json file with the response.
2. add the api end point in the server file and declare the json file for the response/  



# Running the server

1. go to server file folder
2. run command : node <FileName>

