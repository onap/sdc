const http = require('http');
const path = require('path');
const { spawn } = require('child_process');
const express = require('express');
const proxy = require('http-proxy-middleware');
const mock = require('../configurations/mock.json').sdcConfig;
const { proxyError } = require('./dev-proxy');

function parseRole(args) {
    let role = 'designer';
    for (let index = 0; index < args.length; index++) {
        const argument = args[index];
        if (argument === '--') continue;
        if (argument === '--env.role') role = args[++index];
        else if (argument.startsWith('--env.role=')) role = argument.slice('--env.role='.length);
        else throw new Error(`Unknown development server argument: ${argument}`);
    }
    if (!Object.prototype.hasOwnProperty.call(mock.userTypes, role)) {
        throw new Error(`Unknown development role: ${role}. Expected designer or admin.`);
    }
    return role;
}

function identity(request, role) {
    const cookies = {};
    for (const part of (request.headers.cookie || '').split(';')) {
        const separator = part.indexOf('=');
        if (separator < 0) continue;
        try {
            cookies[part.slice(0, separator).trim()] = decodeURIComponent(part.slice(separator + 1));
        } catch (error) {
            continue;
        }
    }
    const userId = request.headers.http_iv_user || request.headers.user_id || cookies.HTTP_IV_USER || cookies.USER_ID;
    const user = Object.values(mock.userTypes).find(candidate => candidate.userId === userId) || mock.userTypes[role];
    const value = (name, fallback) => request.headers[name.toLowerCase()] || cookies[name] || fallback;
    return {
        [mock.cookie.userIdSuffix]: userId || user.userId,
        HTTP_IV_USER: userId || user.userId,
        [mock.cookie.userFirstName]: value(mock.cookie.userFirstName, user.firstName),
        [mock.cookie.userLastName]: value(mock.cookie.userLastName, user.lastName),
        [mock.cookie.userEmail]: value(mock.cookie.userEmail, user.email),
        HTTP_IV_REMOTE_ADDRESS: '0.0.0.0',
        HTTP_CSP_WSTYPE: 'Intranet',
        [mock.cookie.portalCookie]: 'randomValue'
    };
}

function setCookies(response, values) {
    Object.entries(values).forEach(([name, value]) => response.cookie(name, value));
}

function createGateway({ target, role = 'designer' }) {
    parseRole(['--env.role', role]);
    const app = express();
    app.get('/login', (request, response) => response.type('html').send(`<!DOCTYPE html><html><head><title>Login page</title></head><body>
<h1>Webseal simulator</h1><h2>Login:</h2>
<form action="/login" method="post">
  <div>User id:</div><input type="text" name="userId"><br>
  <div>Password:</div><input type="password" name="password"><br><br>
  <input type="submit" value="Login">
</form></body></html>`));
    app.post('/login', express.text({ type: () => true, limit: '16kb' }), (request, response) => {
        const userId = new URLSearchParams(request.body || '').get('userId');
        const user = Object.values(mock.userTypes).find(candidate => candidate.userId === userId) || mock.userTypes[role];
        setCookies(response, identity({ headers: { http_iv_user: user.userId } }, role));
        response.redirect('/sdc1');
    });
    app.use((request, response, next) => {
        const values = identity(request, role);
        setCookies(response, values);
        Object.entries(values).forEach(([name, value]) => { request.headers[name.toLowerCase()] = value; });
        next();
    });
    const forward = proxy({
        target,
        changeOrigin: true,
        logLevel: 'silent',
        onError: proxyError,
        onProxyRes(upstream, request, response) {
            upstream.headers['set-cookie'] = (response.getHeader('set-cookie') || []).concat(upstream.headers['set-cookie'] || []);
        }
    });
    app.use(forward);
    const server = http.createServer(app);
    const sockets = new Set();
    server.on('connection', socket => {
        sockets.add(socket);
        socket.on('close', () => sockets.delete(socket));
    });
    server.on('upgrade', (request, socket, head) => {
        Object.entries(identity(request, role)).forEach(([name, value]) => { request.headers[name.toLowerCase()] = value; });
        forward.upgrade(request, socket, head);
    });
    server.destroyConnections = () => sockets.forEach(socket => socket.destroy());
    return server;
}

function listen(server, port) {
    return new Promise((resolve, reject) => {
        const fail = error => reject(error);
        server.once('error', fail);
        server.listen(port, '127.0.0.1', () => {
            server.removeListener('error', fail);
            resolve(server.address().port);
        });
    });
}

async function start(options = {}) {
    const env = options.env || process.env;
    const role = parseRole(options.role === undefined ? options.args || [] : ['--env.role', options.role]);
    const port = options.port === undefined ? Number(env.SDC_DEV_PORT || 9000) : options.port;
    if (!Number.isInteger(port) || port < (options.port === 0 ? 0 : 1) || port > 65535) {
        throw new Error('SDC_DEV_PORT must be an integer from 1 to 65535');
    }
    const reservation = http.createServer();
    const internalPort = await listen(reservation, 0);
    const server = createGateway({ target: `http://127.0.0.1:${internalPort}`, role });
    let publicPort;
    try {
        publicPort = await listen(server, port);
    } finally {
        await new Promise(resolve => reservation.close(resolve));
    }
    const signals = options.signals || process;
    let child;
    let closing;
    let finish;
    const stopped = new Promise(resolve => { finish = resolve; });
    const onSignal = () => close(0);
    const close = (code = 0) => {
        if (closing) return closing;
        closing = (async () => {
            signals.removeListener('SIGINT', onSignal);
            signals.removeListener('SIGTERM', onSignal);
            server.destroyConnections();
            const serverClosed = new Promise(resolve => server.close(resolve));
            if (child && child.pid && child.exitCode === null && child.signalCode === null) {
                await new Promise(resolve => {
                    const timeout = setTimeout(() => child.kill('SIGKILL'), 3000);
                    child.once('exit', () => { clearTimeout(timeout); resolve(); });
                    child.kill('SIGTERM');
                });
            }
            await serverClosed;
            finish(code);
        })();
        return closing;
    };
    signals.once('SIGINT', onSignal);
    signals.once('SIGTERM', onSignal);
    server.on('error', () => close(1));
    try {
        child = (options.spawn || spawn)(process.execPath, [
            require.resolve('@angular/cli/bin/ng'), 'serve',
            '--host', '127.0.0.1', '--port', String(internalPort),
            '--configuration', 'development', '--aot=false',
            '--proxy-config', path.resolve(__dirname, '../proxy.conf.js'),
            '--public-host', `http://127.0.0.1:${publicPort}`
        ], { cwd: path.resolve(__dirname, '..'), env, stdio: 'inherit' });
        child.once('error', () => close(1));
        child.once('exit', code => close(code || 1));
    } catch (error) {
        await close(1);
        throw error;
    }
    return { server, child, close, stopped };
}

if (require.main === module) {
    start({ args: process.argv.slice(2) }).then(running => {
        console.log(`SDC development gateway: http://127.0.0.1:${running.server.address().port}`);
        return running.stopped;
    }).then(code => { process.exitCode = code; }, error => {
        console.error(error.message);
        process.exitCode = 1;
    });
}

module.exports = { parseRole, createGateway, start };