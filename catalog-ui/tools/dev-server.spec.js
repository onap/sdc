const assert = require('assert');
const http = require('http');
const net = require('net');
const path = require('path');
const { spawn } = require('child_process');
const { EventEmitter } = require('events');
const express = require('express');
const proxy = require('http-proxy-middleware');
const mock = require('../configurations/mock.json').sdcConfig;

const resources = [];
const listen = server => new Promise((resolve, reject) => {
    server.once('error', reject);
    server.listen(0, '127.0.0.1', () => resolve(server.address().port));
});
const request = (port, path, options = {}) => new Promise((resolve, reject) => {
    const req = http.request({ host: '127.0.0.1', port, path, ...options }, res => {
        let body = '';
        res.on('data', chunk => { body += chunk; });
        res.on('end', () => resolve({ status: res.statusCode, headers: res.headers, body }));
    });
    req.on('error', reject);
    req.end(options.body);
});
const track = server => {
    const sockets = new Set();
    server.on('connection', socket => {
        sockets.add(socket);
        socket.on('close', () => sockets.delete(socket));
    });
    resources.push(() => new Promise(resolve => {
        sockets.forEach(socket => socket.destroy());
        server.close(resolve);
    }));
    return server;
};
const gateway = async (target, role = 'designer') => {
    const server = track(require('./dev-server').createGateway({ target, role }));
    return listen(server);
};
const backend = async () => {
    const server = track(http.createServer((req, res) => {
        let body = '';
        req.on('data', chunk => { body += chunk; });
        req.on('end', () => {
            res.statusCode = req.url.includes('missing') ? 404 : 201;
            res.setHeader('set-cookie', ['session=one; Secure; HttpOnly', 'second=two; Secure; SameSite=Lax']);
            res.end(JSON.stringify({ url: req.url, headers: req.headers, method: req.method, body }));
        });
    }));
    return listen(server);
};
const cliProxy = async (port, direct) => {
    const { createProxyConfig } = require('./dev-proxy');
    const entries = createProxyConfig({ SDC_BACKEND_HOST: '127.0.0.1', SDC_BACKEND_PORT: String(port), SDC_DIRECT_FE: String(direct) });
    const app = express();
    entries.forEach(entry => app.use(proxy(entry.context, entry)));
    app.use((req, res) => res.end('application'));
    const server = track(http.createServer(app));
    return listen(server);
};

afterEach(async () => {
    while (resources.length) await resources.pop()();
});

test('role adapter accepts npm role spellings and rejects unknown roles and arguments', () => {
    const { parseRole } = require('./dev-server');
    expect(parseRole([])).toBe('designer');
    expect(parseRole(['--env.role', 'admin'])).toBe('admin');
    expect(parseRole(['--', '--env.role=designer'])).toBe('designer');
    expect(() => parseRole(['--env.role', 'unknown'])).toThrow(/role/i);
    expect(() => parseRole(['--env.role'])).toThrow(/role/i);
    expect(() => parseRole(['--env.role=toString'])).toThrow(/role/i);
    expect(() => parseRole(['--host', '0.0.0.0'])).toThrow(/argument/i);
});

test('login form, selected role cookies, posted identities and API identity headers agree', async () => {
    const target = await backend();
    const port = await gateway(`http://127.0.0.1:${target}`, 'admin');
    expect((await request(port, '/login')).body).toContain('Webseal simulator');
    const initial = await request(port, '/sdc1');
    expect(initial.headers['set-cookie'].join(';')).toContain(`USER_ID=${mock.userTypes.admin.userId}`);
    expect(JSON.parse(initial.body).headers.http_iv_user).toBe(mock.userTypes.admin.userId);
    const login = await request(port, '/login', { method: 'POST', body: 'userId=cs0008&password=ignored' });
    expect(login.status).toBe(302);
    expect(login.headers.location).toBe('/sdc1');
    const cookies = login.headers['set-cookie'].map(value => value.split(';')[0]).join('; ');
    for (const name of ['USER_ID', 'HTTP_IV_USER', 'HTTP_CSP_FIRSTNAME', 'HTTP_CSP_LASTNAME', 'HTTP_CSP_EMAIL', 'HTTP_IV_REMOTE_ADDRESS', 'HTTP_CSP_WSTYPE', 'EPService']) {
        expect(cookies).toContain(`${name}=`);
    }
    const selected = await request(port, '/sdc1/feProxy/rest/test', { headers: { cookie: cookies } });
    expect(JSON.parse(selected.body).headers.http_iv_user).toBe('cs0008');
    expect(selected.headers['set-cookie'].join(';')).toContain('USER_ID=cs0008');
    const override = await request(port, '/test', { headers: { HTTP_IV_USER: 'jh0003', HTTP_CSP_FIRSTNAME: 'Override' } });
    expect(JSON.parse(override.body).headers.http_iv_user).toBe('jh0003');
    expect(override.headers['set-cookie'].join(';')).toContain('HTTP_CSP_FIRSTNAME=Override');
    const fallback = await request(port, '/login', { method: 'POST', body: 'userId=unknown' });
    expect(fallback.headers['set-cookie'].join(';')).toContain('USER_ID=jh0003');
});

test.each([false, true])('real API proxy preserves bodies, errors and ordered routes (direct FE %s)', async direct => {
    const target = await backend();
    const internal = await cliProxy(target, direct);
    const port = await gateway(`http://127.0.0.1:${internal}`);
    const paths = [
        ['/sdc1/feProxy/rest/v1/user?x=1', '/sdc2/rest/v1/user?x=1'],
        ['/sdc1/feProxy/uicache/v1/catalog/resources/latestversion/notabstract/metadata?x=1', '/sdc2/rest/v1/catalog/resources/latestversion/notabstract/metadata?x=1'],
        ['/sdc1/feProxy/uicache/v1/catalog/services', '/sdc2/rest/v1/screen/services'],
        ['/sdc1/feProxy/uicache/other', '/sdc2/rest/other'],
        ['/sdc1/rest/config/ui/plugins', '/sdc1/rest/config/ui/plugins']
    ];
    for (const [path, rewritten] of paths) {
        const response = await request(port, path, { method: 'POST', body: 'payload', headers: { 'content-type': 'text/plain' } });
        const received = JSON.parse(response.body);
        expect(response.status).toBe(201);
        expect(received).toMatchObject({ url: direct ? path : rewritten, method: 'POST', body: 'payload' });
        expect(received.headers.http_iv_user).toBe('cs0008');
    }
    const missing = await request(port, '/sdc1/feProxy/rest/missing', { headers: { accept: 'text/html' } });
    expect(missing.status).toBe(404);
    expect(JSON.parse(missing.body).url).toContain('/missing');
    for (const path of ['/onboarding/punch-outs_en.js', '/sdc1/feProxy/onboarding-api/test', '/dcae/test', '/sdc1/feProxy/dcae-api/test', '/wf/test', '/sdc1/feProxy/wf/test']) {
        const response = await request(port, path);
        expect(JSON.parse(response.body).url).toBe(path);
        expect(response.headers['set-cookie'].filter(value => /^(session|second)=/.test(value)).every(value => !/;\s*Secure\b/i.test(value))).toBe(true);
    }
});

test('proxy configuration keeps environment target and cookie relaxation scoped', () => {
    const { createProxyConfig } = require('./dev-proxy');
    const entries = createProxyConfig({ SDC_BACKEND_PROTOCOL: 'https', SDC_BACKEND_HOST: 'example.test', SDC_BACKEND_PORT: '8181', SDC_DIRECT_FE: 'true' });
    expect(entries.every(entry => entry.target === 'https://example.test:8181')).toBe(true);
    expect(entries[0].pathRewrite).toBeUndefined();
    expect(entries[2].pathRewrite).toBeUndefined();
    expect(entries[0].onProxyRes).toBeUndefined();
    expect(Array.isArray(require('../proxy.conf'))).toBe(true);
});

test('API target connection failures stay 502 even when HTML is requested', async () => {
    const reservation = http.createServer();
    const unavailable = await listen(reservation);
    await new Promise(resolve => reservation.close(resolve));
    const internal = await cliProxy(unavailable, false);
    const port = await gateway(`http://127.0.0.1:${internal}`);
    const response = await request(port, '/sdc1/feProxy/rest/test', { headers: { accept: 'text/html' } });
    expect(response.status).toBe(502);
    expect(response.body).not.toContain('application');
});

test('installed CLI dev-server serves navigation/assets without masking API errors', async () => {
    const cliRoot = path.dirname(require.resolve('@angular/cli/package.json'));
    const webpack = require(require.resolve('webpack', { paths: [cliRoot] }));
    const DevServer = require(require.resolve('webpack-dev-server', { paths: [cliRoot] }));
    const { createProxyConfig } = require('./dev-proxy');
    const target = await backend();
    const compiler = webpack({
        entry: path.resolve(__dirname, '../configurations/mock.json'),
        output: { path: path.resolve(__dirname, '../dist'), filename: 'main.bundle.js', publicPath: '/' },
        plugins: [{ apply(instance) {
            instance.plugin('emit', (compilation, done) => {
                for (const [name, content] of Object.entries({ 'index.html': '<html>CLI application</html>', 'assets/icon.txt': 'asset' })) {
                    compilation.assets[name] = { source: () => content, size: () => Buffer.byteLength(content) };
                }
                done();
            });
        } }]
    });
    const server = new DevServer(compiler, {
        contentBase: false,
        publicPath: '/',
        quiet: true,
        historyApiFallback: { index: '/index.html', disableDotRule: true, htmlAcceptHeaders: ['text/html', 'application/xhtml+xml'] },
        proxy: createProxyConfig({ SDC_BACKEND_HOST: '127.0.0.1', SDC_BACKEND_PORT: String(target) })
    });
    resources.push(() => new Promise(resolve => server.close(resolve)));
    await new Promise((resolve, reject) => {
        server.listeningApp.once('error', reject);
        server.listen(0, '127.0.0.1', resolve);
    });
    const port = await gateway(`http://127.0.0.1:${server.listeningApp.address().port}`);
    for (const url of ['/', '/sdc1', '/workspace/service/general']) {
        expect((await request(port, url, { headers: { accept: 'text/html' } })).body).toContain('CLI application');
    }
    expect((await request(port, '/assets/icon.txt')).body).toBe('asset');
    expect((await request(port, '/main.bundle.js')).headers['content-type']).toContain('javascript');
    const response = await request(port, '/sdc1/feProxy/rest/missing', { headers: { accept: 'text/html' } });
    expect(response.status).toBe(404);
    expect(JSON.parse(response.body).url).toBe('/sdc2/rest/missing');
});

test('gateway delegates deep links and root/sdc1 assets unchanged and returns 502 on refused target', async () => {
    const target = await backend();
    const port = await gateway(`http://127.0.0.1:${target}`);
    for (const path of ['/', '/workspace/service/general', '/sdc1', '/main.bundle.js', '/sdc1/assets/icon.png']) {
        expect(JSON.parse((await request(port, path)).body).url).toBe(path);
    }
    const reservation = http.createServer();
    const unavailable = await listen(reservation);
    await new Promise(resolve => reservation.close(resolve));
    const broken = await gateway(`http://127.0.0.1:${unavailable}`);
    expect((await request(broken, '/sdc1/feProxy/rest/test')).status).toBe(502);
});

test('gateway forwards a real websocket upgrade and subsequent frames', async () => {
    const upstream = track(http.createServer());
    upstream.on('upgrade', (req, socket, head) => {
        socket.write('HTTP/1.1 101 Switching Protocols\r\nConnection: Upgrade\r\nUpgrade: websocket\r\n\r\n');
        if (head.length) socket.write(head);
        socket.on('data', data => socket.write(data));
    });
    const target = await listen(upstream);
    const port = await gateway(`http://127.0.0.1:${target}`);
    await new Promise((resolve, reject) => {
        const socket = net.connect(port, '127.0.0.1');
        const timeout = setTimeout(() => { socket.destroy(); reject(new Error('Websocket timeout')); }, 2000);
        let upgraded = false;
        socket.on('connect', () => socket.write('GET /sockjs-node/123/websocket HTTP/1.1\r\nHost: localhost\r\nConnection: Upgrade\r\nUpgrade: websocket\r\n\r\n'));
        socket.on('error', reject);
        socket.on('data', data => {
            if (!upgraded) {
                expect(data.toString()).toContain('101 Switching Protocols');
                upgraded = true;
                socket.write(Buffer.from([0x81, 0x02, 0x6f, 0x6b]));
            } else {
                assert.deepStrictEqual(data, Buffer.from([0x81, 0x02, 0x6f, 0x6b]));
                clearTimeout(timeout);
                socket.destroy();
                resolve();
            }
        });
    });
});

test.each(['SIGINT', 'SIGTERM'])('launcher uses loopback JIT/proxy flags, serves traffic and cleans up on %s', async signal => {
    const { start } = require('./dev-server');
    const signals = new EventEmitter();
    let invocation;
    let ready;
    const childReady = new Promise(resolve => { ready = resolve; });
    const running = await start({ port: 0, signals, spawn(command, args, options) {
        invocation = { command, args, options };
        const internal = args[args.indexOf('--port') + 1];
        const child = spawn(command, ['-e', `require('http').createServer((req,res)=>res.end('cli')).listen(${internal},'127.0.0.1',()=>console.log('ready'))`], { ...options, stdio: ['ignore', 'pipe', 'pipe'] });
        child.stdout.once('data', ready);
        return child;
    } });
    resources.push(() => running.close());
    expect(running.server.address().address).toBe('127.0.0.1');
    expect(invocation.command).toBe(process.execPath);
    expect(invocation.args).toEqual(expect.arrayContaining(['serve', '--host', '127.0.0.1', '--target', 'development', '--aot=false', '--proxy-config', '--public-host']));
    expect(invocation.args[invocation.args.indexOf('--port') + 1]).not.toBe(String(running.server.address().port));
    expect(invocation.args[invocation.args.indexOf('--public-host') + 1]).toBe(`http://127.0.0.1:${running.server.address().port}`);
    await childReady;
    expect((await request(running.server.address().port, '/sdc1')).body).toBe('cli');
    signals.emit(signal);
    expect(await running.stopped).toBe(0);
    expect(signals.listenerCount('SIGTERM')).toBe(0);
    expect(running.child.exitCode !== null || running.child.signalCode !== null).toBe(true);
});

test('launcher rejects invalid ports and roles before invoking spawn', async () => {
    const { start } = require('./dev-server');
    let spawned = false;
    const launch = () => { spawned = true; };
    for (const port of ['bad', '-1', '65536', '1.5', '0']) {
        await expect(start({ env: { SDC_DEV_PORT: port }, spawn: launch })).rejects.toThrow(/SDC_DEV_PORT/);
    }
    const openingSocket = jest.spyOn(http.Server.prototype, 'listen').mockImplementation(() => {
        throw new Error('opened socket before validation');
    });
    try {
        await expect(start({ role: 'unknown', spawn: launch })).rejects.toThrow(/role/i);
        expect(openingSocket).not.toHaveBeenCalled();
    } finally {
        openingSocket.mockRestore();
    }
    expect(spawned).toBe(false);
});

test('launcher closes gateway on child failure and spawn error, and does not spawn on occupied port', async () => {
    const { start } = require('./dev-server');
    const running = await start({ port: 0, signals: new EventEmitter(), spawn(command, args, options) {
        return spawn(command, ['-e', 'process.exit(7)'], options);
    } });
    expect(await running.stopped).toBe(7);
    expect(running.server.listening).toBe(false);
    const failed = await start({ port: 0, signals: new EventEmitter(), spawn() {
        return spawn('/nonexistent/sdc-test-command', []);
    } });
    expect(await failed.stopped).toBe(1);
    expect(failed.server.listening).toBe(false);
    const occupied = track(http.createServer());
    const port = await listen(occupied);
    let spawned = false;
    await expect(start({ port, spawn() { spawned = true; } })).rejects.toMatchObject({ code: 'EADDRINUSE' });
    expect(spawned).toBe(false);
});