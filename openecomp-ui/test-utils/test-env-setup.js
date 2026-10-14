var localStorageMock = (function() {
    var store = {};
    return {
        getItem: function(key) {
            return store[key];
        },
    setItem: function(key, value) {
        store[key] = value.toString();
    },
    clear: function() {
        store = {};
    }
  };
})();

Object.defineProperty(window, 'localStorage', { value: localStorageMock });

// jsdom has no dialog API; production gets the same shim from punch-outs.js.
require('../src/nfvo-utils/dialogShim.js');
