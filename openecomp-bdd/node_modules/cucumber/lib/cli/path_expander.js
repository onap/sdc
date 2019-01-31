'use strict';

Object.defineProperty(exports, "__esModule", {
  value: true
});

var _bluebird = require('bluebird');

var _bluebird2 = _interopRequireDefault(_bluebird);

var _classCallCheck2 = require('babel-runtime/helpers/classCallCheck');

var _classCallCheck3 = _interopRequireDefault(_classCallCheck2);

var _createClass2 = require('babel-runtime/helpers/createClass');

var _createClass3 = _interopRequireDefault(_createClass2);

var _lodash = require('lodash');

var _lodash2 = _interopRequireDefault(_lodash);

var _fs = require('mz/fs');

var _fs2 = _interopRequireDefault(_fs);

var _glob = require('glob');

var _glob2 = _interopRequireDefault(_glob);

var _path = require('path');

var _path2 = _interopRequireDefault(_path);

function _interopRequireDefault(obj) { return obj && obj.__esModule ? obj : { default: obj }; }

var PathExpander = function () {
  function PathExpander(directory) {
    (0, _classCallCheck3.default)(this, PathExpander);

    this.directory = directory;
  }

  (0, _createClass3.default)(PathExpander, [{
    key: 'expandPathsWithExtensions',
    value: function () {
      var _ref = (0, _bluebird.coroutine)(function* (paths, extensions) {
        var _this = this;

        var expandedPaths = yield _bluebird2.default.map(paths, function () {
          var _ref2 = (0, _bluebird.coroutine)(function* (p) {
            return yield _this.expandPathWithExtensions(p, extensions);
          });

          return function (_x3) {
            return _ref2.apply(this, arguments);
          };
        }());
        return _lodash2.default.uniq(_lodash2.default.flatten(expandedPaths));
      });

      function expandPathsWithExtensions(_x, _x2) {
        return _ref.apply(this, arguments);
      }

      return expandPathsWithExtensions;
    }()
  }, {
    key: 'expandPathWithExtensions',
    value: function () {
      var _ref3 = (0, _bluebird.coroutine)(function* (p, extensions) {
        var fullPath = _path2.default.resolve(this.directory, p);
        var stats = yield _fs2.default.stat(fullPath);
        if (stats.isDirectory()) {
          return yield this.expandDirectoryWithExtensions(fullPath, extensions);
        } else {
          return [fullPath];
        }
      });

      function expandPathWithExtensions(_x4, _x5) {
        return _ref3.apply(this, arguments);
      }

      return expandPathWithExtensions;
    }()
  }, {
    key: 'expandDirectoryWithExtensions',
    value: function () {
      var _ref4 = (0, _bluebird.coroutine)(function* (realPath, extensions) {
        var pattern = realPath + '/**/*.';
        if (extensions.length > 1) {
          pattern += '{' + extensions.join(',') + '}';
        } else {
          pattern += extensions[0];
        }
        var results = yield _bluebird2.default.promisify(_glob2.default)(pattern);
        return results.map(function (filePath) {
          return filePath.replace(/\//g, _path2.default.sep);
        });
      });

      function expandDirectoryWithExtensions(_x6, _x7) {
        return _ref4.apply(this, arguments);
      }

      return expandDirectoryWithExtensions;
    }()
  }]);
  return PathExpander;
}();

exports.default = PathExpander;
//# sourceMappingURL=data:application/json;charset=utf-8;base64,eyJ2ZXJzaW9uIjozLCJzb3VyY2VzIjpbIi4uLy4uL3NyYy9jbGkvcGF0aF9leHBhbmRlci5qcyJdLCJuYW1lcyI6WyJQYXRoRXhwYW5kZXIiLCJkaXJlY3RvcnkiLCJwYXRocyIsImV4dGVuc2lvbnMiLCJleHBhbmRlZFBhdGhzIiwibWFwIiwicCIsImV4cGFuZFBhdGhXaXRoRXh0ZW5zaW9ucyIsInVuaXEiLCJmbGF0dGVuIiwiZnVsbFBhdGgiLCJyZXNvbHZlIiwic3RhdHMiLCJzdGF0IiwiaXNEaXJlY3RvcnkiLCJleHBhbmREaXJlY3RvcnlXaXRoRXh0ZW5zaW9ucyIsInJlYWxQYXRoIiwicGF0dGVybiIsImxlbmd0aCIsImpvaW4iLCJyZXN1bHRzIiwicHJvbWlzaWZ5IiwiZmlsZVBhdGgiLCJyZXBsYWNlIiwic2VwIl0sIm1hcHBpbmdzIjoiOzs7Ozs7Ozs7Ozs7Ozs7Ozs7QUFBQTs7OztBQUNBOzs7O0FBQ0E7Ozs7QUFDQTs7Ozs7O0lBR3FCQSxZO0FBQ25CLHdCQUFZQyxTQUFaLEVBQXVCO0FBQUE7O0FBQ3JCLFNBQUtBLFNBQUwsR0FBaUJBLFNBQWpCO0FBQ0Q7Ozs7O3FEQUUrQkMsSyxFQUFPQyxVLEVBQVk7QUFBQTs7QUFDakQsWUFBTUMsZ0JBQWdCLE1BQU0sbUJBQVFDLEdBQVIsQ0FBWUgsS0FBWjtBQUFBLCtDQUFtQixXQUFNSSxDQUFOLEVBQVc7QUFDeEQsbUJBQU8sTUFBTSxNQUFLQyx3QkFBTCxDQUE4QkQsQ0FBOUIsRUFBaUNILFVBQWpDLENBQWI7QUFDRCxXQUYyQjs7QUFBQTtBQUFBO0FBQUE7QUFBQSxZQUE1QjtBQUdBLGVBQU8saUJBQUVLLElBQUYsQ0FBTyxpQkFBRUMsT0FBRixDQUFVTCxhQUFWLENBQVAsQ0FBUDtBQUNELE87Ozs7Ozs7Ozs7O3NEQUU4QkUsQyxFQUFHSCxVLEVBQVk7QUFDNUMsWUFBTU8sV0FBVyxlQUFLQyxPQUFMLENBQWEsS0FBS1YsU0FBbEIsRUFBNkJLLENBQTdCLENBQWpCO0FBQ0EsWUFBTU0sUUFBUSxNQUFNLGFBQUdDLElBQUgsQ0FBUUgsUUFBUixDQUFwQjtBQUNBLFlBQUlFLE1BQU1FLFdBQU4sRUFBSixFQUF5QjtBQUN2QixpQkFBTyxNQUFNLEtBQUtDLDZCQUFMLENBQW1DTCxRQUFuQyxFQUE2Q1AsVUFBN0MsQ0FBYjtBQUNELFNBRkQsTUFFTztBQUNMLGlCQUFPLENBQUNPLFFBQUQsQ0FBUDtBQUNEO0FBQ0YsTzs7Ozs7Ozs7Ozs7c0RBRW1DTSxRLEVBQVViLFUsRUFBWTtBQUN4RCxZQUFJYyxVQUFVRCxXQUFXLFFBQXpCO0FBQ0EsWUFBSWIsV0FBV2UsTUFBWCxHQUFvQixDQUF4QixFQUEyQjtBQUN6QkQscUJBQVcsTUFBTWQsV0FBV2dCLElBQVgsQ0FBZ0IsR0FBaEIsQ0FBTixHQUE2QixHQUF4QztBQUNELFNBRkQsTUFFTztBQUNMRixxQkFBV2QsV0FBVyxDQUFYLENBQVg7QUFDRDtBQUNELFlBQU1pQixVQUFVLE1BQU0sbUJBQVFDLFNBQVIsaUJBQXdCSixPQUF4QixDQUF0QjtBQUNBLGVBQU9HLFFBQVFmLEdBQVIsQ0FBWTtBQUFBLGlCQUFZaUIsU0FBU0MsT0FBVCxDQUFpQixLQUFqQixFQUF3QixlQUFLQyxHQUE3QixDQUFaO0FBQUEsU0FBWixDQUFQO0FBQ0QsTzs7Ozs7Ozs7Ozs7O2tCQS9Ca0J4QixZIiwiZmlsZSI6InBhdGhfZXhwYW5kZXIuanMiLCJzb3VyY2VzQ29udGVudCI6WyJpbXBvcnQgXyBmcm9tICdsb2Rhc2gnXG5pbXBvcnQgZnMgZnJvbSAnbXovZnMnXG5pbXBvcnQgZ2xvYiBmcm9tICdnbG9iJ1xuaW1wb3J0IHBhdGggZnJvbSAncGF0aCdcbmltcG9ydCBQcm9taXNlIGZyb20gJ2JsdWViaXJkJ1xuXG5leHBvcnQgZGVmYXVsdCBjbGFzcyBQYXRoRXhwYW5kZXIge1xuICBjb25zdHJ1Y3RvcihkaXJlY3RvcnkpIHtcbiAgICB0aGlzLmRpcmVjdG9yeSA9IGRpcmVjdG9yeVxuICB9XG5cbiAgYXN5bmMgZXhwYW5kUGF0aHNXaXRoRXh0ZW5zaW9ucyhwYXRocywgZXh0ZW5zaW9ucykge1xuICAgIGNvbnN0IGV4cGFuZGVkUGF0aHMgPSBhd2FpdCBQcm9taXNlLm1hcChwYXRocywgYXN5bmMgcCA9PiB7XG4gICAgICByZXR1cm4gYXdhaXQgdGhpcy5leHBhbmRQYXRoV2l0aEV4dGVuc2lvbnMocCwgZXh0ZW5zaW9ucylcbiAgICB9KVxuICAgIHJldHVybiBfLnVuaXEoXy5mbGF0dGVuKGV4cGFuZGVkUGF0aHMpKVxuICB9XG5cbiAgYXN5bmMgZXhwYW5kUGF0aFdpdGhFeHRlbnNpb25zKHAsIGV4dGVuc2lvbnMpIHtcbiAgICBjb25zdCBmdWxsUGF0aCA9IHBhdGgucmVzb2x2ZSh0aGlzLmRpcmVjdG9yeSwgcClcbiAgICBjb25zdCBzdGF0cyA9IGF3YWl0IGZzLnN0YXQoZnVsbFBhdGgpXG4gICAgaWYgKHN0YXRzLmlzRGlyZWN0b3J5KCkpIHtcbiAgICAgIHJldHVybiBhd2FpdCB0aGlzLmV4cGFuZERpcmVjdG9yeVdpdGhFeHRlbnNpb25zKGZ1bGxQYXRoLCBleHRlbnNpb25zKVxuICAgIH0gZWxzZSB7XG4gICAgICByZXR1cm4gW2Z1bGxQYXRoXVxuICAgIH1cbiAgfVxuXG4gIGFzeW5jIGV4cGFuZERpcmVjdG9yeVdpdGhFeHRlbnNpb25zKHJlYWxQYXRoLCBleHRlbnNpb25zKSB7XG4gICAgbGV0IHBhdHRlcm4gPSByZWFsUGF0aCArICcvKiovKi4nXG4gICAgaWYgKGV4dGVuc2lvbnMubGVuZ3RoID4gMSkge1xuICAgICAgcGF0dGVybiArPSAneycgKyBleHRlbnNpb25zLmpvaW4oJywnKSArICd9J1xuICAgIH0gZWxzZSB7XG4gICAgICBwYXR0ZXJuICs9IGV4dGVuc2lvbnNbMF1cbiAgICB9XG4gICAgY29uc3QgcmVzdWx0cyA9IGF3YWl0IFByb21pc2UucHJvbWlzaWZ5KGdsb2IpKHBhdHRlcm4pXG4gICAgcmV0dXJuIHJlc3VsdHMubWFwKGZpbGVQYXRoID0+IGZpbGVQYXRoLnJlcGxhY2UoL1xcLy9nLCBwYXRoLnNlcCkpXG4gIH1cbn1cbiJdfQ==