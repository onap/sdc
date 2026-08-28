# patches

This directory contains patches applied to `node_modules` via [patch-package](https://github.com/ds300/patch-package) after `npm install`.

Patches are applied automatically by the `postinstall` script in `package.json`:

```
"postinstall": "patch-package"
```

## Patches

### `onap-ui-common+1.0.119.patch`

**Package:** `onap-ui-common@1.0.119`
**File patched:** `lib/style.css`

Removes a single use of the deprecated `/deep/` shadow-piercing CSS combinator from the compiled stylesheet:

```css
/* before */
.file-upload .file-upload-input /deep/ .sdc-input { margin-bottom: 0px; }

/* after */
.file-upload .file-upload-input .sdc-input { margin-bottom: 0px; }
```

**Why:** `/deep/` is a non-standard Angular-era selector that Dart Sass rejects as a syntax error. Node Sass (libsass) silently accepted it, but since this project was migrated from `node-sass` to `sass` (Dart Sass), the invalid selector causes a build failure.

**Upstream:** The `/deep/` is in the pre-compiled `lib/style.css` artifact shipped with the npm package. The source SCSS that produced it is not present in the available `onap-ui-common` source repository.

`onap-ui-common@1.1.0` does replace it — with `::ng-deep`, which parses, so Dart Sass stops erroring and the patch is no longer needed to build. That is not a fix for this module: `::ng-deep` is an Angular compiler construct, and this is a React app that imports `lib/style.css` as a plain global stylesheet (`src/sdc-app/punch-outs.js`), so the browser discards the rule as an unknown pseudo-element and the `margin-bottom` is silently lost. Upgrading trades a build failure for a layout regression. A real upstream fix has to emit a plain descendant selector.

## Why `onap-ui-common` is pinned to an exact version

`package.json` pins `"onap-ui-common": "1.0.119"` rather than a range, deliberately. patch-package refuses to apply a patch whose filename names a different version, and this module's `yarn.lock` is gitignored — so under a range CI re-resolves on every build and any new upstream release turns it red. ONAP's Nexus `npm.public` group already serves 1.1.0 while public npmjs.org still tops out at 1.0.119, so a range fails CI while local builds stay green (SDC-4904). Moving the pin means regenerating the patch for the new version in the same commit.
