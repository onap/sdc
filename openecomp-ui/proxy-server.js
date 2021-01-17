'use strict';

const proxy = require('http-proxy-middleware');

const devConfig = require('./tools/getDevConfig');
let devPort = process.env.PORT || devConfig.port;

module.exports = function(server) {
    console.log('');
    console.log('---------------------');
    console.log('---------------------');
    console.log('---------------------');
    console.log(
        'Local URL: http://localhost:' + devPort + '/sdc1/#!/onboardVendor'
    );
    console.log('---------------------');
    console.log('---------------------');
    console.log('---------------------');
    console.log('Starting dev server with role: ' + devConfig.env.role);
    let userType = devConfig.userTypes[devConfig.env.role];

    let proxyConfigDefaults = {
        changeOrigin: true,
        secure: false,
        logLevel: 'debug',
        onProxyRes: (proxyRes, req, res) => {
            res.cookie(
                devConfig.cookie.userIdSuffix,
                req.headers[devConfig.cookie.userIdSuffix] || userType.userId
            );
            res.cookie(
                devConfig.cookie.userEmail,
                req.headers[devConfig.cookie.userEmail] || userType.email
            );
            res.cookie(
                devConfig.cookie.userFirstName,
                req.headers[devConfig.cookie.userFirstName] ||
                userType.firstName
            );
            res.cookie(
                devConfig.cookie.userLastName,
                req.headers[devConfig.cookie.userLastName] || userType.lastName
            );
            if (
                proxyRes &&
                proxyRes.headers &&
                proxyRes.headers.location &&
                proxyRes.headers.location.indexOf('login') > -1
            ) {
                proxyRes.headers.location = `http://localhost:${devPort}/${
                    devConfig.proxyConfig.redirectionPath
                    }`;
            }
        }
    };

    let middlewares = [
        (req, res, next) => {
            devConfig.proxyConfig.urlReplaceRules.forEach(function(rule) {
                if (req.url.indexOf(rule.url) > -1) {
                    req.url = req.url.replace(rule.replace, rule.with);
                    next();
                }
            });
            devConfig.proxyConfig.jsReplaceRules.forEach(function(rule) {
                let regex = new RegExp('^(.*)' + rule.replace);
                let match = req.url.match(regex);
                let newUrl = match && match[1] + rule.with;
                if (newUrl) {
                    console.log(`REWRITING URL: ${req.url} -> ${newUrl}`);
                    req.url = newUrl;
                    next();
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
            target: devConfig.proxyTarget,
            config: devConfig.proxyConfig.onboardingProxy
        });
    } else {
        console.log(
            'Onboarding proxy set to : ' + devConfig.proxyCatalogTarget
        );
    }
    console.log('Catalog proxy set to : ' + devConfig.proxyCatalogTarget);
    proxies.push({
        target: devConfig.proxyCatalogTarget,
        config: devConfig.proxyConfig.catalogProxy
    });
    proxies.forEach(function(p) {
        console.log(
            'adding: ' + p.target + ' with rewrite: ' + p.config.rewrite
        );
        middlewares.push(
            proxy(
                p.config.proxy,
                Object.assign({}, proxyConfigDefaults, {
                    target: p.target,
                    loglevel: 'debug',
                    pathRewrite: p.config.rewrite
                })
            )
        );
    });

    if (devConfig.proxyConfig.websocketProxy.enabled) {
        let websocketTarget = devConfig.proxyCatalogTarget;
        if (devConfig.proxyWebsocketTarget) {
            websocketTarget = devConfig.proxyWebsocketTarget;
        }
        console.log('Websocket proxy set to : ' + websocketTarget);
        console.log('---------------------');
        var wsProxy = proxy(
            devConfig.proxyConfig.websocketProxy.proxy,
            Object.assign({}, proxyConfigDefaults, {
                target: websocketTarget,
                ws: true
            })
        );
        middlewares.push(wsProxy);
        server.use(middlewares);
        server.on('upgrade', wsProxy.upgrade);
    } else {
        server.use(middlewares);
    }
};
