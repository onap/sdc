const mockApis = require('./configurations/mock.json').sdcConfig;
const proxy = require('http-proxy-middleware');
const devPort = 9000;

const fePort = 8181;
const feHost = "localhost";
const protocol="http";
const isDirectToFE = false;

/*
For kubernetes
const fePort = 30207;
const wfPort = 30256;
const feHost = "kubernetes_master";
const protocol="https";
const isDirectToFE = true;// whether to proxy to the k8s proxy or to the BE
*/
const portalCookieValue = "randomValue"; //for dev solely, in production - the webseal would add the cookie by itself.

module.exports = function (env) {

    // Set default user role
    if (!env) {
        env = {
            role: "designer"
        };
    }
    console.log("Starting dev server with role: " + env.role);

    const serverConfig = {
        port: devPort,
        historyApiFallback: true,
        inline: true,
        stats: {
            colors: true,
            exclude: ['node_modules']
        },
        setup: server => {
            let userType = mockApis.userTypes[env.role];

            let middlewares = [
                (req, res, next) => {
                    res.cookie(mockApis.cookie.userIdSuffix, req.headers[mockApis.cookie.userIdSuffix] || userType.userId);
                    res.cookie(mockApis.cookie.userEmail, req.headers[mockApis.cookie.userEmail] || userType.email);
                    res.cookie(mockApis.cookie.userFirstName, req.headers[mockApis.cookie.userFirstName] || userType.firstName);
                    res.cookie(mockApis.cookie.userLastName, req.headers[mockApis.cookie.userLastName] || userType.lastName);
                    res.cookie(mockApis.cookie.portalCookie, portalCookieValue);
                    next();
                }
            ];

            // Redirect all '/sdc1/feProxy/rest' to feHost
            let feProxyOptions = {
                target: protocol + '://' + feHost + ':' + fePort,
                changeOrigin: true,
                secure: false,
                logLevel: 'debug'
            }    
            if (isDirectToFE) {
                feProxyOptions.pathRewrite= {
                    '^/sdc1/feProxy/rest' : '/sdc1/feProxy/rest'
                }
            } else {
                feProxyOptions.pathRewrite= {
                    '^/sdc1/feProxy/rest' : '/sdc2/rest'
                }
            }    
            middlewares.push(
                proxy(['/sdc1/feProxy/rest'], feProxyOptions));

            // Redirect all '/sdc1/rest' to feHost
            middlewares.push(
                proxy(['/sdc1/rest'],{
                    target: protocol + '://' + feHost + ':' + fePort,
                    changeOrigin: true,
                    secure: false
                }));

            // Redirect dcae urls to feHost
            middlewares.push(
                proxy(['/dcae','/sdc1/feProxy/dcae-api'], {
                    target: protocol + '://' + feHost + ':' + fePort,
                    changeOrigin: true,
                    secure: false,
                    onProxyRes: (proxyRes, req, res) => {
                        let setCookie = proxyRes.headers['set-cookie'];
                        if (setCookie) {
                            setCookie[0] = setCookie[0].replace(/\bSecure\b(; )?/, '');
                        }
                    }
                }));

            // Redirect onboarding urls to feHost
            middlewares.push(
                proxy(['/onboarding', '/sdc1/feProxy/onboarding-api'], {
                    target: protocol + '://' + feHost + ':' + fePort,
                    changeOrigin: true,
                    secure: false,
                    onProxyRes: (proxyRes, req, res) => {
                        let setCookie = proxyRes.headers['set-cookie'];
                        if (setCookie) {
                            setCookie[0] = setCookie[0].replace(/\bSecure\b(; )?/, '');
                        }
                    }
                }));

            // Redirect workflow urls to feHost
            middlewares.push(
                proxy(['/sdc1/feProxy/wf', '/wf'], {
                    target: protocol + '://' + feHost + ':' + wfPort,
                    changeOrigin: true,
                    logLevel: 'debug',
                    secure: false,
                    pathRewrite: {
                        '^/sdc1/feProxy' : ''
                    },
                    onProxyRes: (proxyRes, req, res) => {
                        let setCookie = proxyRes.headers['set-cookie'];
                        if (setCookie) {
                            setCookie[0] = setCookie[0].replace(/\bSecure\b(; )?/, '');
                        }
                    }
                }));
            server.use(middlewares);
        }
    };

    return serverConfig;
};
