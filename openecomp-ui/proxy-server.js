'use strict';

const proxy = require('http-proxy-middleware');

let localDevConfig = {};
try {
	localDevConfig = require('./devConfig');
} catch (e) {}
const devConfig = Object.assign({}, require('./devConfig.defaults'), localDevConfig);
let devPort = process.env.PORT || devConfig.port;

let jsonConfig = {
	"appContextPath" : "/onboarding"
};

try {
	jsonConfig = require('./src/sdc-app/config/config.json');
} catch (e) {
	console.log('could not load config. using deault value instead');
}

module.exports = function (server) {
	let proxyConfigDefaults = {
		changeOrigin: true,
		secure: false,
		onProxyRes: (proxyRes, req, res) => {
			let setCookie = proxyRes.headers['set-cookie'];
			if (setCookie) {
				setCookie[0] = setCookie[0].replace(/\bSecure\b(; )?/, '');
			}
			if (proxyRes.statusCode === 302 && proxyRes.headers.location.indexOf('login') > -1) {
				proxyRes.headers.location = `http://localhost:${devPort}/sdc1#/onboardVendor`;
				res.setHeader('Set-Cookie', [
					'HTTP_CSP_EMAIL=csantana@sdc.com',
					'HTTP_CSP_FIRSTNAME=Carlos',
					'HTTP_CSP_LASTNAME=Santana',
					'HTTP_CSP_WSTYPE=Intranet',
					'HTTP_IV_REMOTE_ADDRESS=0.0.0.0',
					'HTTP_IV_USER=cs0008',
					'USER_ID=cs0008'
				]);
			}
		}
	};

	let middlewares = [
		(req, res, next) => {
			if (req.url.indexOf('/proxy-designer1') > -1) {
				req.url = req.url.replace('/proxy-designer1', '');
			}

			if (req.url.indexOf(jsonConfig.appContextPath + '/resources') > -1) {
				req.url = req.url.replace(jsonConfig.appContextPath, '');
			}

			let match = req.url.match(/^(.*)_en.js$/);
			let newUrl = match && match[1] + '.js';
			if (newUrl) {
				console.log(`REWRITING URL: ${req.url} -> ${newUrl}`);
				req.url = newUrl;
			}
			next();
		}
	];

	// standalon back-end (proxyTarget) has higher priority, so it should be first
	if (devConfig.proxyTarget) {
		middlewares.push(
			proxy(['/api', '/onboarding-api', '/sdc1/feProxy/onboarding-api'], Object.assign({}, proxyConfigDefaults, {
				target: devConfig.proxyTarget,
				pathRewrite: {
					'/sdc1/feProxy/onboarding-api': '/onboarding-api'
				}
			}))
		);
	}

	// ATT environment (proxyATTTarget) has lower priority, so it should be second
	if (devConfig.proxyATTTarget) {
		middlewares.push(
			proxy(['/sdc1', '/onboarding-api', '/scripts', '/styles'], Object.assign({}, proxyConfigDefaults, {
				target: devConfig.proxyATTTarget,
				pathRewrite: {
					// Workaround for some weird proxy issue
					'/sdc1/feProxy/onboarding-api': '/sdc1/feProxy/onboarding-api',
					'/onboarding-api': '/sdc1/feProxy/onboarding-api'
				}
			}))
		);
	}
	server.use(middlewares);
};
