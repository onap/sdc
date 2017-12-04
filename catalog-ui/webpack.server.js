let path = require('path');

const mockApis = require('./configurations/mock.json').sdcConfig;
const proxy = require('http-proxy-middleware');
const devPort = 9000;
const fePort = 8181;

module.exports = function(env) {

    // Set default role
    if (!env) {
        env = {
            role: "designer"
        };
    }
    console.log("Starting dev server with role: " + env.role);

    const ServerConfig = {
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
                    next();
                }
            ];

            // Redirect all '/sdc1/feProxy/rest' to feHost
            middlewares.push(
                proxy(['/sdc1/feProxy/rest'],{
                    target: 'http://192.168.50.5:' + fePort,
                    changeOrigin: true,
				    secure: false
                }));

            // Redirect dcae urls to feHost
            middlewares.push(
                proxy(['/dcae','/sdc1/feProxy/dcae-api'],{
                    target: 'http://192.168.50.5:' + fePort,
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
                proxy(['/onboarding','/sdc1/feProxy/onboarding-api'],{
                    target: 'http://192.168.50.5:' + fePort,
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

    return ServerConfig;
}
