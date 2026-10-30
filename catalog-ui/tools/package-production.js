'use strict';

const fs = require('fs');
const path = require('path');
const zlib = require('zlib');
const parse5 = require('parse5');

function packageProduction(dist = path.resolve(__dirname, '../dist')) {
    const root = path.resolve(dist);
    const scripts = [];

    function collect(directory) {
        fs.readdirSync(directory, {withFileTypes: true}).forEach(entry => {
            const filename = path.join(directory, entry.name);
            if (entry.isSymbolicLink()) {
                throw new Error('Unsafe symbolic link in dist: ' + filename);
            }
            if (entry.isDirectory()) {
                collect(filename);
            } else if (entry.isFile() && entry.name.endsWith('.js')) {
                scripts.push(filename);
            }
        });
    }

    function localReference(reference) {
        if (!reference || reference.startsWith('//') || reference.startsWith('#') ||
            reference.startsWith('?') || /^[a-z][a-z0-9+.-]*:/i.test(reference)) {
            return null;
        }
        const suffixIndex = reference.search(/[?#]/);
        const pathname = suffixIndex < 0 ? reference : reference.slice(0, suffixIndex);
        const suffix = suffixIndex < 0 ? '' : reference.slice(suffixIndex);
        const decoded = decodeURIComponent(pathname);
        if (decoded.includes('\\') || decoded.includes('\0') || decoded.trim() !== decoded) {
            throw new Error('Unsafe local URL: ' + reference);
        }
        const relative = decoded.startsWith('/sdc1/') ? decoded.slice('/sdc1/'.length) :
            decoded.startsWith('/') ? decoded.slice(1) : decoded;
        const filename = path.resolve(root, relative);
        const fromRoot = path.relative(root, filename);
        if (fromRoot === '..' || fromRoot.startsWith('..' + path.sep) || path.isAbsolute(fromRoot)) {
            throw new Error('Local URL escapes outside dist: ' + reference);
        }
        return {pathname, suffix, filename};
    }

    function rewrite(node) {
        const attributeName = node.tagName === 'link' ? 'href' :
            node.tagName === 'script' || node.tagName === 'img' ? 'src' : null;
        const attribute = attributeName && node.attrs.find(candidate => candidate.name === attributeName);
        if (attribute) {
            const local = localReference(attribute.value);
            if (local && node.tagName === 'script') {
                const original = local.filename.endsWith('.jsgz') ? local.filename.slice(0, -2) : local.filename;
                if (!scripts.includes(original)) {
                    throw new Error('Missing local script in dist: ' + attribute.value);
                }
                if (local.pathname.endsWith('.js')) {
                    attribute.value = local.pathname + 'gz' + local.suffix;
                }
            } else if (local && !local.pathname.startsWith('/')) {
                const asset = new URL(local.pathname, 'https://sdc.invalid/sdc1/');
                attribute.value = asset.pathname + local.suffix;
            }
        }
        (node.childNodes || []).forEach(rewrite);
    }

    if (fs.lstatSync(root).isSymbolicLink()) {
        throw new Error('Unsafe symbolic link for dist: ' + root);
    }
    collect(root);
    const indexPath = path.join(root, 'index.html');
    const document = parse5.parse(fs.readFileSync(indexPath, 'utf8'));
    rewrite(document);
    const html = parse5.serialize(document);
    scripts.sort().forEach(filename => {
        fs.writeFileSync(filename.slice(0, -3) + '.jsgz', zlib.gzipSync(fs.readFileSync(filename)));
    });
    fs.writeFileSync(indexPath, html);
}

module.exports = packageProduction;

if (require.main === module) {
    try {
        packageProduction();
    } catch (error) {
        console.error('Production packaging failed: ' + error.message);
        process.exitCode = 1;
    }
}