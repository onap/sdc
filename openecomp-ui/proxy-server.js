'use strict';

const proxy = require('http-proxy-middleware');

let localDevConfig = {};
try {
	localDevConfig = require('./devConfig');
} catch (e) {}
const devConfig = Object.assign({}, require('./devConfig.defaults'), localDevConfig);
let devPort = process.env.PORT || devConfig.port;


module.exports = function (server) {
	let cookieRules = devConfig.proxyConfig.cookieReplaceRules;
	let cookies = devConfig.proxyConfig.cookies;
	console.log('---------------------');

	let proxyConfigDefaults = {
		changeOrigin: true,
		secure: false,
		onProxyRes: (proxyRes, req, res) => {
			let setCookie = proxyRes.headers['set-cookie'];
			if (setCookie) {
				cookieRules.forEach(function(rule) {
					setCookie[0] = setCookie[0].replace(rule.replace, rule.with);
				});
			}
			if (proxyRes.statusCode === 302 && proxyRes.headers.location.indexOf(devConfig.proxyConfig.login) > -1) {
				proxyRes.headers.location = `http://localhost:${devPort}/${devConfig.proxyConfig.redirectionPath}`;
				let myCookies = [];
				for (let cookie in cookies) {
					myCookies.push(cookie + '=' + cookies[cookie]);
				}
				res.setHeader('Set-Cookie', myCookies);
			}
		}
	};

	let middlewares = [
		(req, res, next) => {
			devConfig.proxyConfig.urlReplaceRules.forEach(function(rule) {
				if (req.url.indexOf(rule.url) > -1) {
					req.url = req.url.replace(rule.replace, rule.with);
				}
			});
			devConfig.proxyConfig.jsReplaceRules.forEach(function(rule) {
				let regex = new RegExp('^(.*)' + rule.replace);
				let match = req.url.match(regex);
				let newUrl = match && match[1] + rule.with + '.js';
				if (newUrl) {
					console.log(`REWRITING URL: ${req.url} -> ${newUrl}`);
					req.url = newUrl;
				}
			});
			next();
		}
	];


	let proxies = [];

	// standalone back-end (proxyTarget) has higher priority, so it should be first
	if (devConfig.proxyTarget) {
		console.log('Onboarding proxy set to : ' + devConfig.proxyTarget);
		proxies.push({
			target : devConfig.proxyTarget,
			config: devConfig.proxyConfig.onboardingProxy}
		);
	} else {
		console.log('Catalog proxy set to : ' + devConfig.proxyCatalogTarget);
	}
	console.log('Catalog proxy set to : ' + devConfig.proxyCatalogTarget);
	proxies.push({
		target : devConfig.proxyCatalogTarget,
		config: devConfig.proxyConfig.catalogProxy}
	);
	proxies.forEach(function(p) {
		middlewares.push(
			proxy(p.config.proxy, Object.assign({}, proxyConfigDefaults, {
				target: p.target,
				pathRewrite: p.config.rewrite
			}))
		);

	});

	let websocketTarget = devConfig.proxyCatalogTarget;
	if (devConfig.proxyWebsocketTarget) {
		websocketTarget = devConfig.proxyWebsocketTarget;
	}
	console.log('Websocket proxy set to : ' + websocketTarget);
	console.log('---------------------');
	var wsProxy = proxy(devConfig.proxyConfig.websocketProxy.proxy, Object.assign({}, proxyConfigDefaults, {
		target: websocketTarget,
		ws: true
	}))
	middlewares.push(wsProxy);


	server.use(middlewares);
	server.on('upgrade', wsProxy.upgrade);
};
