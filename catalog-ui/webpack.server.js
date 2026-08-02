const mockApis = require('./configurations/mock.json').sdcConfig;
const proxy = require('http-proxy-middleware');
const devPort = 9000;

const fePort = process.env.SDC_BACKEND_PORT || 8080;
const feHost = process.env.SDC_BACKEND_HOST || "localhost";
const protocol = process.env.SDC_BACKEND_PROTOCOL || "http";
const isDirectToFE = (process.env.SDC_DIRECT_FE || "false") === "true";

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

            server.get('/login', (req, res) => {
                res.send(`<!DOCTYPE html><html><head><title>Login page</title></head><body>
<h1>Webseal simulator</h1><h2>Login:</h2>
<form action="/login" method="post">
  <div>User id:</div><input type="text" name="userId"><br>
  <div>Password:</div><input type="password" name="password"><br><br>
  <input type="submit" value="Login">
</form></body></html>`);
            });

            server.post('/login', (req, res) => {
                let body = '';
                req.on('data', chunk => { body += chunk; });
                req.on('end', () => {
                    const params = new URLSearchParams(body);
                    const userId = params.get('userId') || userType.userId;
                    let user = userType;
                    Object.values(mockApis.userTypes).forEach(u => {
                        if (u.userId === userId) user = u;
                    });
                    res.cookie(mockApis.cookie.userIdSuffix, user.userId);
                    res.cookie('HTTP_IV_USER', user.userId);
                    res.cookie(mockApis.cookie.userFirstName, user.firstName);
                    res.cookie(mockApis.cookie.userLastName, user.lastName);
                    res.cookie(mockApis.cookie.userEmail, user.email);
                    res.cookie('HTTP_IV_REMOTE_ADDRESS', '0.0.0.0');
                    res.cookie('HTTP_CSP_WSTYPE', 'Intranet');
                    res.cookie(mockApis.cookie.portalCookie, portalCookieValue);
                    res.redirect('/sdc1');
                });
            });

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
            if (!isDirectToFE) {
              feProxyOptions.pathRewrite= {
                '^/sdc1/feProxy/rest' : '/sdc2/rest'
              }
            }
            middlewares.push(
                proxy(['/sdc1/feProxy/rest'], feProxyOptions));

            // Redirect all '/sdc1/feProxy/uicache' to feHost
            let uiCacheOptions = {
                target: protocol + '://' + feHost + ':' + fePort,
                changeOrigin: true,
                secure: false
            }
            // 'uicache' is a catalog-fe concept, not a backend one: FeProxyServlet maps it onto two
            // DIFFERENT backend paths. When this dev server targets the BE directly (the default),
            // nothing performs that mapping, so the affected page raises an error modal whose
            // backdrop then blocks every click. Mirror the servlet here; not applied with
            // SDC_DIRECT_FE, where the FE itself does it.
            //
            // Rule ORDER MATTERS — http-proxy-middleware applies the FIRST matching rule only
            // (path-rewriter.js returns false from its forEach). The left-palette rule must
            // therefore come before the catalog one, because the palette URL *starts with*
            // '/uicache/v1/catalog' and would otherwise be rewritten to '/v1/screen/resources/...',
            // which the BE answers with 500.
            if (!isDirectToFE) {
              uiCacheOptions.pathRewrite = {
                // FeProxyServlet:242-246 — the composition left palette keeps 'v1/catalog'
                // and only drops the facade segment.
                '^/sdc1/feProxy/uicache/v1/catalog/resources/latestversion/notabstract/metadata'
                    : '/sdc2/rest/v1/catalog/resources/latestversion/notabstract/metadata',
                // FeProxyServlet:224-226 — the CATALOG page's facade path becomes 'rest/v1/screen'.
                '^/sdc1/feProxy/uicache/v1/catalog' : '/sdc2/rest/v1/screen',
                '^/sdc1/feProxy/uicache' : '/sdc2/rest'
              }
            }
            middlewares.push(
              proxy(['/sdc1/feProxy/uicache'], uiCacheOptions));

            // Redirect all '/sdc1/rest' to feHost
            // NOT rewritten to '/sdc2/rest': the only caller is '/sdc1/rest/config/ui/plugins',
            // which catalog-fe serves from its own plugins-configuration.yaml and the BE does not
            // serve at all (every '/sdc2/rest/**/plugins' spelling returns 500). A rewrite would
            // turn a 404 into a 500, so the dev server leaves this 404 and the Playwright fixture
            // stubs the response instead — see stubPluginsConfig() in the playwright suite.
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
                proxy(['/wf', '/sdc1/feProxy/wf'], {
                    target: protocol + '://' + feHost + ':' + fePort,
                    changeOrigin: true,
                    logLevel: 'debug',
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
