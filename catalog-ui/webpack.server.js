const mockApis = require('./configurations/mock.json').sdcConfig;
const proxy = require('http-proxy-middleware');
const devPort = 9000;
const fePort = 8181;
const feHost = "localhost";
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
            middlewares.push(
                proxy(['/sdc1/feProxy/rest', '/sdc1/feProxy/uicache'], {
                    target: 'http://' + feHost + ':' + fePort,
                    changeOrigin: true,
                    secure: false
                }));

            // Redirect all '/sdc1/rest' to feHost
            middlewares.push(
                proxy(['/sdc1/rest'], {
                    target: 'http://' + feHost + ':' + fePort,
                    changeOrigin: true,
                    secure: false
                }));

            // Redirect dcae urls to feHost
            middlewares.push(
                proxy(['/dcae', '/sdc1/feProxy/dcae-api'], {
                    target: 'http://' + feHost + ':' + fePort,
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
                    target: 'http://' + feHost + ':' + fePort,
                    changeOrigin: true,
                    secure: false,
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
