const mock = () => {
    let storage = {};
    return {
  getItem: key => (key in storage ? storage[key] : null),
             setItem: (key, value) => (storage[key] = value || ''),
             removeItem: key => delete storage[key],
             clear: () => (storage = {})
    };
  };

  Object.defineProperty(window, 'localStorage', { value: mock() });
  Object.defineProperty(window, 'sessionStorage', { value: mock() });
  Object.defineProperty(window, 'getComputedStyle', {
        value: () => ['-webkit-appearance'],

  });

  // webpack.common.js:17 script-loads lodash.min.js into the vendor bundle, so `_` is an ambient
  // global at runtime and 6 files under ng2/ rely on that (`import Dictionary = _.Dictionary`, plus
  // `_.keyBy` at automated-upgrade.service.ts:89). Jest never mirrored it, so a spec that reaches
  // any of them — even transitively, e.g. via workspace-container.component.ts — died with
  // `ReferenceError: _ is not defined` before its first test ran.
  (global as any)._ = require('lodash');
