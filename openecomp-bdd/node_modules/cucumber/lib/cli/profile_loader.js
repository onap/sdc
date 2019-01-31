'use strict';

Object.defineProperty(exports, "__esModule", {
  value: true
});

var _typeof2 = require('babel-runtime/helpers/typeof');

var _typeof3 = _interopRequireDefault(_typeof2);

var _bluebird = require('bluebird');

var _classCallCheck2 = require('babel-runtime/helpers/classCallCheck');

var _classCallCheck3 = _interopRequireDefault(_classCallCheck2);

var _createClass2 = require('babel-runtime/helpers/createClass');

var _createClass3 = _interopRequireDefault(_createClass2);

var _lodash = require('lodash');

var _lodash2 = _interopRequireDefault(_lodash);

var _fs = require('mz/fs');

var _fs2 = _interopRequireDefault(_fs);

var _path = require('path');

var _path2 = _interopRequireDefault(_path);

var _stringArgv = require('string-argv');

var _stringArgv2 = _interopRequireDefault(_stringArgv);

function _interopRequireDefault(obj) { return obj && obj.__esModule ? obj : { default: obj }; }

var ProfileLoader = function () {
  function ProfileLoader(directory) {
    (0, _classCallCheck3.default)(this, ProfileLoader);

    this.directory = directory;
  }

  (0, _createClass3.default)(ProfileLoader, [{
    key: 'getDefinitions',
    value: function () {
      var _ref = (0, _bluebird.coroutine)(function* () {
        var definitionsFilePath = _path2.default.join(this.directory, 'cucumber.js');
        var exists = yield _fs2.default.exists(definitionsFilePath);
        if (!exists) {
          return {};
        }
        var definitions = require(definitionsFilePath);
        if ((typeof definitions === 'undefined' ? 'undefined' : (0, _typeof3.default)(definitions)) !== 'object') {
          throw new Error(definitionsFilePath + ' does not export an object');
        }
        return definitions;
      });

      function getDefinitions() {
        return _ref.apply(this, arguments);
      }

      return getDefinitions;
    }()
  }, {
    key: 'getArgv',
    value: function () {
      var _ref2 = (0, _bluebird.coroutine)(function* (profiles) {
        var definitions = yield this.getDefinitions();
        if (profiles.length === 0 && definitions['default']) {
          profiles = ['default'];
        }
        var argvs = profiles.map(function (profile) {
          if (!definitions[profile]) {
            throw new Error('Undefined profile: ' + profile);
          }
          return (0, _stringArgv2.default)(definitions[profile]);
        });
        return _lodash2.default.flatten(argvs);
      });

      function getArgv(_x) {
        return _ref2.apply(this, arguments);
      }

      return getArgv;
    }()
  }]);
  return ProfileLoader;
}();

exports.default = ProfileLoader;
//# sourceMappingURL=data:application/json;charset=utf-8;base64,eyJ2ZXJzaW9uIjozLCJzb3VyY2VzIjpbIi4uLy4uL3NyYy9jbGkvcHJvZmlsZV9sb2FkZXIuanMiXSwibmFtZXMiOlsiUHJvZmlsZUxvYWRlciIsImRpcmVjdG9yeSIsImRlZmluaXRpb25zRmlsZVBhdGgiLCJqb2luIiwiZXhpc3RzIiwiZGVmaW5pdGlvbnMiLCJyZXF1aXJlIiwiRXJyb3IiLCJwcm9maWxlcyIsImdldERlZmluaXRpb25zIiwibGVuZ3RoIiwiYXJndnMiLCJtYXAiLCJwcm9maWxlIiwiZmxhdHRlbiJdLCJtYXBwaW5ncyI6Ijs7Ozs7Ozs7Ozs7Ozs7Ozs7Ozs7QUFBQTs7OztBQUNBOzs7O0FBQ0E7Ozs7QUFDQTs7Ozs7O0lBRXFCQSxhO0FBQ25CLHlCQUFZQyxTQUFaLEVBQXVCO0FBQUE7O0FBQ3JCLFNBQUtBLFNBQUwsR0FBaUJBLFNBQWpCO0FBQ0Q7Ozs7O3VEQUVzQjtBQUNyQixZQUFNQyxzQkFBc0IsZUFBS0MsSUFBTCxDQUFVLEtBQUtGLFNBQWYsRUFBMEIsYUFBMUIsQ0FBNUI7QUFDQSxZQUFNRyxTQUFTLE1BQU0sYUFBR0EsTUFBSCxDQUFVRixtQkFBVixDQUFyQjtBQUNBLFlBQUksQ0FBQ0UsTUFBTCxFQUFhO0FBQ1gsaUJBQU8sRUFBUDtBQUNEO0FBQ0QsWUFBTUMsY0FBY0MsUUFBUUosbUJBQVIsQ0FBcEI7QUFDQSxZQUFJLFFBQU9HLFdBQVAsdURBQU9BLFdBQVAsT0FBdUIsUUFBM0IsRUFBcUM7QUFDbkMsZ0JBQU0sSUFBSUUsS0FBSixDQUFVTCxzQkFBc0IsNEJBQWhDLENBQU47QUFDRDtBQUNELGVBQU9HLFdBQVA7QUFDRCxPOzs7Ozs7Ozs7OztzREFFYUcsUSxFQUFVO0FBQ3RCLFlBQU1ILGNBQWMsTUFBTSxLQUFLSSxjQUFMLEVBQTFCO0FBQ0EsWUFBSUQsU0FBU0UsTUFBVCxLQUFvQixDQUFwQixJQUF5QkwsWUFBWSxTQUFaLENBQTdCLEVBQXFEO0FBQ25ERyxxQkFBVyxDQUFDLFNBQUQsQ0FBWDtBQUNEO0FBQ0QsWUFBTUcsUUFBUUgsU0FBU0ksR0FBVCxDQUFhLFVBQVNDLE9BQVQsRUFBa0I7QUFDM0MsY0FBSSxDQUFDUixZQUFZUSxPQUFaLENBQUwsRUFBMkI7QUFDekIsa0JBQU0sSUFBSU4sS0FBSixDQUFVLHdCQUF3Qk0sT0FBbEMsQ0FBTjtBQUNEO0FBQ0QsaUJBQU8sMEJBQVdSLFlBQVlRLE9BQVosQ0FBWCxDQUFQO0FBQ0QsU0FMYSxDQUFkO0FBTUEsZUFBTyxpQkFBRUMsT0FBRixDQUFVSCxLQUFWLENBQVA7QUFDRCxPOzs7Ozs7Ozs7Ozs7a0JBOUJrQlgsYSIsImZpbGUiOiJwcm9maWxlX2xvYWRlci5qcyIsInNvdXJjZXNDb250ZW50IjpbImltcG9ydCBfIGZyb20gJ2xvZGFzaCdcbmltcG9ydCBmcyBmcm9tICdtei9mcydcbmltcG9ydCBwYXRoIGZyb20gJ3BhdGgnXG5pbXBvcnQgc3RyaW5nQXJndiBmcm9tICdzdHJpbmctYXJndidcblxuZXhwb3J0IGRlZmF1bHQgY2xhc3MgUHJvZmlsZUxvYWRlciB7XG4gIGNvbnN0cnVjdG9yKGRpcmVjdG9yeSkge1xuICAgIHRoaXMuZGlyZWN0b3J5ID0gZGlyZWN0b3J5XG4gIH1cblxuICBhc3luYyBnZXREZWZpbml0aW9ucygpIHtcbiAgICBjb25zdCBkZWZpbml0aW9uc0ZpbGVQYXRoID0gcGF0aC5qb2luKHRoaXMuZGlyZWN0b3J5LCAnY3VjdW1iZXIuanMnKVxuICAgIGNvbnN0IGV4aXN0cyA9IGF3YWl0IGZzLmV4aXN0cyhkZWZpbml0aW9uc0ZpbGVQYXRoKVxuICAgIGlmICghZXhpc3RzKSB7XG4gICAgICByZXR1cm4ge31cbiAgICB9XG4gICAgY29uc3QgZGVmaW5pdGlvbnMgPSByZXF1aXJlKGRlZmluaXRpb25zRmlsZVBhdGgpXG4gICAgaWYgKHR5cGVvZiBkZWZpbml0aW9ucyAhPT0gJ29iamVjdCcpIHtcbiAgICAgIHRocm93IG5ldyBFcnJvcihkZWZpbml0aW9uc0ZpbGVQYXRoICsgJyBkb2VzIG5vdCBleHBvcnQgYW4gb2JqZWN0JylcbiAgICB9XG4gICAgcmV0dXJuIGRlZmluaXRpb25zXG4gIH1cblxuICBhc3luYyBnZXRBcmd2KHByb2ZpbGVzKSB7XG4gICAgY29uc3QgZGVmaW5pdGlvbnMgPSBhd2FpdCB0aGlzLmdldERlZmluaXRpb25zKClcbiAgICBpZiAocHJvZmlsZXMubGVuZ3RoID09PSAwICYmIGRlZmluaXRpb25zWydkZWZhdWx0J10pIHtcbiAgICAgIHByb2ZpbGVzID0gWydkZWZhdWx0J11cbiAgICB9XG4gICAgY29uc3QgYXJndnMgPSBwcm9maWxlcy5tYXAoZnVuY3Rpb24ocHJvZmlsZSkge1xuICAgICAgaWYgKCFkZWZpbml0aW9uc1twcm9maWxlXSkge1xuICAgICAgICB0aHJvdyBuZXcgRXJyb3IoJ1VuZGVmaW5lZCBwcm9maWxlOiAnICsgcHJvZmlsZSlcbiAgICAgIH1cbiAgICAgIHJldHVybiBzdHJpbmdBcmd2KGRlZmluaXRpb25zW3Byb2ZpbGVdKVxuICAgIH0pXG4gICAgcmV0dXJuIF8uZmxhdHRlbihhcmd2cylcbiAgfVxufVxuIl19