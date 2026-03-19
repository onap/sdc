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

**Upstream:** The `/deep/` is in the pre-compiled `lib/style.css` artifact shipped with the npm package. The source SCSS that produced it is not present in the available `onap-ui-common` source repository. This patch is intended as a temporary workaround until a fixed version of `onap-ui-common` is published.
