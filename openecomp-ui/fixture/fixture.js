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

'use strict';

const PORT = 4000;

function startFixtureServer() {
	return require('child_process').fork('fixture/express', [PORT]);
}

function buildProxyMiddleware() {
	return require('./middleware')(PORT);
}

function getProxyData(fixtureServerOptions, req, res, next) {
	if (!fixtureServerOptions.serverProcess) {
		fixtureServerOptions.serverProcess = startFixtureServer();
	}

	return fixtureServerOptions.proxy(req, res, next);
}

function wrapFixture(fixtureServerOptions, req, res, next) {
	if (fixtureServerOptions.proxy) {
		return getProxyData(fixtureServerOptions, req, res, next);
	} else {
		next();
	}
}


module.exports = function fixture(options) {

	let proxy;
	if(options.enabled) {
		proxy = buildProxyMiddleware();
	}

	let fixtureServerOptions = {
		proxy,
		serverProcess: null
	};

	(function startWatch() {
		var nodeWatch = require('node-watch');

		nodeWatch(['fixture/data', 'fixture/express.js'], function () {
			if (fixtureServerOptions.proxy && fixtureServerOptions.serverProcess) {
				fixtureServerOptions.serverProcess.kill();
				fixtureServerOptions.serverProcess = startFixtureServer();
			}
		});

		nodeWatch(['devConfig.json'], function () {
			let devConfigDefaults = require('../devConfig.defaults');
			require('fs').readFile('devConfig.json', (err, data) => {
				if (err) throw err;
				const config = Object.assign({}, devConfigDefaults, JSON.parse(data));
				if (config.useFixture) {
					fixtureServerOptions.proxy = proxy || buildProxyMiddleware();
				}
				else {
					fixtureServerOptions.proxy = null;
					if (fixtureServerOptions.serverProcess) {
						fixtureServerOptions.serverProcess.kill();
						fixtureServerOptions.serverProcess = null;
					}
				}
			});
		});

	})();

	return (req, res, next) => wrapFixture(fixtureServerOptions, req, res, next);
};
