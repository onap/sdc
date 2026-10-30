'use strict';

const fs = require('fs');
const os = require('os');
const path = require('path');
const zlib = require('zlib');
const parse5 = require('parse5');
const childProcess = require('child_process');

describe('production packaging', () => {
    let temporary;
    let dist;

    function write(relative, content) {
        const target = path.join(dist, relative);
        fs.mkdirSync(path.dirname(target), {recursive: true});
        fs.writeFileSync(target, content);
    }

    function read(relative) {
        return fs.readFileSync(path.join(dist, relative));
    }

    function elements(tagName) {
        const matches = [];
        function visit(node) {
            if (node.tagName === tagName) {
                matches.push(node.attrs.reduce((attributes, attribute) => {
                    attributes[attribute.name] = attribute.value;
                    return attributes;
                }, {}));
            }
            (node.childNodes || []).forEach(visit);
        }
        visit(parse5.parse(read('index.html').toString()));
        return matches;
    }

    function packageDist() {
        return require('./package-production')(dist);
    }

    beforeEach(() => {
        temporary = fs.mkdtempSync(path.join(os.tmpdir(), 'sdc-package-'));
        dist = path.join(temporary, 'dist');
        fs.mkdirSync(dist);
        write('index.html', '<!doctype html><html><head><base href="/"></head><body></body></html>');
    });

    afterEach(() => {
        fs.rmSync(temporary, {recursive: true, force: true});
    });

    test('rewrites hashed scripts in order without losing queries, fragments or attributes', () => {
        write('index.html', '<!doctype html><base href="/"><script defer nonce="abc" data-label="a > b" src="/sdc1/inline.123.js?v=1&amp;x=2#boot"></script><script async crossorigin="anonymous" integrity="sha256-abc" src="./nested/main.456.js#main"></script><script>window.ready = true;</script>');
        write('inline.123.js', 'window.runtime = true;');
        write('nested/main.456.js', 'window.main = true;');

        packageDist();

        expect(elements('script')).toEqual([
            {defer: '', nonce: 'abc', 'data-label': 'a > b', src: '/sdc1/inline.123.jsgz?v=1&x=2#boot'},
            {async: '', crossorigin: 'anonymous', integrity: 'sha256-abc', src: './nested/main.456.jsgz#main'},
            {}
        ]);
        expect(read('index.html').toString()).toContain('window.ready = true;');
        expect(elements('base')).toEqual([{href: '/'}]);
    });

    test('compresses every nested JavaScript file with a lossless roundtrip and keeps CLI outputs', () => {
        const source = Buffer.from([0, 1, 2, 127, 128, 255]);
        write('nested/lazy.789.js', source);
        write('main.js', 'import("./nested/lazy.789.js");');
        write('styles.hash.css', 'body { color: red; }');
        write('main.js.map', '{"version":3}');

        packageDist();

        expect(read('nested/lazy.789.js')).toEqual(source);
        expect(zlib.gunzipSync(read('nested/lazy.789.jsgz'))).toEqual(source);
        expect(read('main.js').toString()).toBe('import("./nested/lazy.789.js");');
        expect(zlib.gunzipSync(read('main.jsgz'))).toEqual(read('main.js'));
        expect(read('styles.hash.css').toString()).toBe('body { color: red; }');
        expect(read('main.js.map').toString()).toBe('{"version":3}');
        expect(fs.existsSync(path.join(dist, 'styles.hash.cssgz'))).toBe(false);
        expect(fs.existsSync(path.join(dist, 'main.js.mapgz'))).toBe(false);
    });

    test('prefixes relative image and link assets while leaving the base and deployed URLs alone', () => {
        write('index.html', '<base href="/"><link href="assets/preloading.css?v=2#style" rel="stylesheet"><link rel="icon" href="./assets/favicon.png"><link href="/sdc1/styles.hash.css"><img alt="Splash" src="assets/splash.png?size=2&amp;x=1#logo"><img src="/shared/logo.png">');

        packageDist();

        expect(elements('base')).toEqual([{href: '/'}]);
        expect(elements('link').map(attributes => attributes.href)).toEqual([
            '/sdc1/assets/preloading.css?v=2#style', '/sdc1/assets/favicon.png', '/sdc1/styles.hash.css'
        ]);
        expect(elements('img')).toEqual([
            {alt: 'Splash', src: '/sdc1/assets/splash.png?size=2&x=1#logo'}, {src: '/shared/logo.png'}
        ]);
    });

    test('leaves external, protocol-relative, data, fragment and empty URLs unchanged', () => {
        const references = ['https://cdn.example/main.js', '//cdn.example/main.js', 'data:text/javascript,void(0)', '#local', '', 'mailto:team@example.com', 'blob:https://example.com/id'];
        write('index.html', references.map(reference => '<script src="' + reference + '"></script><link href="' + reference + '"><img src="' + reference + '">').join(''));

        packageDist();

        expect(elements('script').map(attributes => attributes.src)).toEqual(references);
        expect(elements('link').map(attributes => attributes.href)).toEqual(references);
        expect(elements('img').map(attributes => attributes.src)).toEqual(references);
    });

    test('is byte-stable on rerun and refreshes gzip from the original JavaScript', () => {
        write('index.html', '<script src="/sdc1/main.hash.js?v=1#boot"></script><img src="assets/logo.png">');
        write('main.hash.js', 'first();');
        packageDist();
        const index = read('index.html');
        const compressed = read('main.hash.jsgz');

        packageDist();

        expect(read('index.html')).toEqual(index);
        expect(read('main.hash.jsgz')).toEqual(compressed);
        expect(fs.existsSync(path.join(dist, 'main.hash.jsgzgz'))).toBe(false);
        write('main.hash.js', 'second();');
        packageDist();
        expect(zlib.gunzipSync(read('main.hash.jsgz')).toString()).toBe('second();');
    });

    test('resolves URL-encoded nested filenames and root-local script URLs', () => {
        write('index.html', '<script src="/sdc1/nested/a%20b.js?x=1#run"></script><script src="/main.js"></script>');
        write('nested/a b.js', 'nested();');
        write('main.js', 'main();');

        packageDist();

        expect(elements('script').map(attributes => attributes.src)).toEqual([
            '/sdc1/nested/a%20b.jsgz?x=1#run', '/main.jsgz'
        ]);
        expect(zlib.gunzipSync(read('nested/a b.jsgz')).toString()).toBe('nested();');
    });

    ['missing.js', '/sdc1/missing.jsgz?v=1'].forEach(reference => {
        test('rejects a missing script: ' + reference, () => {
            write('index.html', '<script src="' + reference + '"></script>');
            const index = read('index.html');

            expect(packageDist).toThrow(/missing.*script|script.*missing/i);
            expect(read('index.html')).toEqual(index);
        });
    });

    ['../outside.js', '/sdc1/../outside.js', '%2e%2e/outside.js', '/sdc1/%2e%2e%2foutside.js', '..%5coutside.js'].forEach(reference => {
        test('rejects script paths escaping dist: ' + reference, () => {
            fs.writeFileSync(path.join(temporary, 'outside.js'), 'outside();');
            write('index.html', '<script src="' + reference + '"></script>');

            expect(packageDist).toThrow(/outside|unsafe|escape|contain/i);
            expect(fs.existsSync(path.join(temporary, 'outside.jsgz'))).toBe(false);
        });
    });

    test('rejects symlinked input files and directories', () => {
        fs.writeFileSync(path.join(temporary, 'outside.js'), 'outside();');
        fs.symlinkSync(path.join(temporary, 'outside.js'), path.join(dist, 'main.js'));
        expect(packageDist).toThrow(/symlink|symbolic/i);
        fs.unlinkSync(path.join(dist, 'main.js'));
        fs.symlinkSync(temporary, path.join(dist, 'nested'));
        expect(packageDist).toThrow(/symlink|symbolic/i);
    });

    test('rejects a symlinked gzip destination without overwriting its target', () => {
        const outside = path.join(temporary, 'outside');
        fs.writeFileSync(outside, 'unchanged');
        write('main.js', 'main();');
        fs.symlinkSync(outside, path.join(dist, 'main.jsgz'));

        expect(packageDist).toThrow(/symlink|symbolic/i);
        expect(fs.readFileSync(outside, 'utf8')).toBe('unchanged');
    });

    test('CLI defaults to dist relative to the tool, independently of cwd', () => {
        const tools = path.join(temporary, 'tools');
        fs.mkdirSync(tools);
        fs.copyFileSync(path.join(__dirname, 'package-production.js'), path.join(tools, 'package-production.js'));
        write('index.html', '<script src="/sdc1/main.js"></script>');
        write('main.js', 'main();');
        const result = childProcess.spawnSync(process.execPath, [path.join(tools, 'package-production.js')], {
            cwd: os.tmpdir(), encoding: 'utf8',
            env: Object.assign({}, process.env, {NODE_PATH: path.resolve(__dirname, '../node_modules')})
        });

        expect(result.status).toBe(0);
        expect(elements('script')).toEqual([{src: '/sdc1/main.jsgz'}]);
        expect(zlib.gunzipSync(read('main.jsgz')).toString()).toBe('main();');
    });
});