function relaxCookies(response) {
    const cookies = response.headers['set-cookie'];
    if (cookies) {
        response.headers['set-cookie'] = cookies.map(cookie => cookie.replace(/;\s*Secure\b/gi, ''));
    }
}

function proxyError(error, request, response) {
    if (typeof response.writeHead === 'function') {
        if (!response.headersSent) response.writeHead(502, { 'Content-Type': 'text/plain' });
        response.end('Development proxy target unavailable');
    } else {
        response.destroy();
    }
}

function createProxyConfig(env = process.env) {
    const target = `${env.SDC_BACKEND_PROTOCOL || 'http'}://${env.SDC_BACKEND_HOST || 'localhost'}:${env.SDC_BACKEND_PORT || 8080}`;
    const direct = env.SDC_DIRECT_FE === 'true';
    const entry = (context, options = {}) => ({
        context,
        target,
        changeOrigin: true,
        secure: false,
        logLevel: 'silent',
        onError: proxyError,
        ...options
    });
    return [
        entry(['/sdc1/feProxy/rest'], direct ? {} : { pathRewrite: { '^/sdc1/feProxy/rest': '/sdc2/rest' } }),
        entry(['/sdc1/feProxy/uicache'], direct ? {} : { pathRewrite: {
            '^/sdc1/feProxy/uicache/v1/catalog/resources/latestversion/notabstract/metadata': '/sdc2/rest/v1/catalog/resources/latestversion/notabstract/metadata',
            '^/sdc1/feProxy/uicache/v1/catalog': '/sdc2/rest/v1/screen',
            '^/sdc1/feProxy/uicache': '/sdc2/rest'
        } }),
        entry(['/sdc1/rest']),
        entry(['/dcae', '/sdc1/feProxy/dcae-api'], { onProxyRes: relaxCookies }),
        entry(['/onboarding', '/sdc1/feProxy/onboarding-api'], { onProxyRes: relaxCookies }),
        entry(['/wf', '/sdc1/feProxy/wf'], { onProxyRes: relaxCookies })
    ];
}

module.exports = { createProxyConfig, proxyError };