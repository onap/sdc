'use strict';

Object.defineProperty(exports, "__esModule", {
  value: true
});

var _bluebird = require('bluebird');

var _ = require('./');

var _2 = _interopRequireDefault(_);

var _verror = require('verror');

var _verror2 = _interopRequireDefault(_verror);

function _interopRequireDefault(obj) { return obj && obj.__esModule ? obj : { default: obj }; }

function exitWithError(error) {
  console.error(_verror2.default.fullStack(error)); // eslint-disable-line no-console
  process.exit(1);
}

exports.default = function () {
  var _ref = (0, _bluebird.coroutine)(function* () {
    var cwd = process.cwd();
    var cli = new _2.default({
      argv: process.argv,
      cwd: cwd,
      stdout: process.stdout
    });

    var success = void 0;
    try {
      success = yield cli.run();
    } catch (error) {
      exitWithError(error);
    }

    var exitCode = success ? 0 : 1;
    function exitNow() {
      process.exit(exitCode);
    }

    // If stdout.write() returned false, kernel buffer is not empty yet
    if (process.stdout.write('')) {
      exitNow();
    } else {
      process.stdout.on('drain', exitNow);
    }
  });

  function run() {
    return _ref.apply(this, arguments);
  }

  return run;
}();
//# sourceMappingURL=data:application/json;charset=utf-8;base64,eyJ2ZXJzaW9uIjozLCJzb3VyY2VzIjpbIi4uLy4uL3NyYy9jbGkvcnVuLmpzIl0sIm5hbWVzIjpbImV4aXRXaXRoRXJyb3IiLCJlcnJvciIsImNvbnNvbGUiLCJmdWxsU3RhY2siLCJwcm9jZXNzIiwiZXhpdCIsImN3ZCIsImNsaSIsImFyZ3YiLCJzdGRvdXQiLCJzdWNjZXNzIiwicnVuIiwiZXhpdENvZGUiLCJleGl0Tm93Iiwid3JpdGUiLCJvbiJdLCJtYXBwaW5ncyI6Ijs7Ozs7Ozs7QUFBQTs7OztBQUNBOzs7Ozs7QUFFQSxTQUFTQSxhQUFULENBQXVCQyxLQUF2QixFQUE4QjtBQUM1QkMsVUFBUUQsS0FBUixDQUFjLGlCQUFPRSxTQUFQLENBQWlCRixLQUFqQixDQUFkLEVBRDRCLENBQ1c7QUFDdkNHLFVBQVFDLElBQVIsQ0FBYSxDQUFiO0FBQ0Q7OztzQ0FFYyxhQUFxQjtBQUNsQyxRQUFNQyxNQUFNRixRQUFRRSxHQUFSLEVBQVo7QUFDQSxRQUFNQyxNQUFNLGVBQVE7QUFDbEJDLFlBQU1KLFFBQVFJLElBREk7QUFFbEJGLGNBRmtCO0FBR2xCRyxjQUFRTCxRQUFRSztBQUhFLEtBQVIsQ0FBWjs7QUFNQSxRQUFJQyxnQkFBSjtBQUNBLFFBQUk7QUFDRkEsZ0JBQVUsTUFBTUgsSUFBSUksR0FBSixFQUFoQjtBQUNELEtBRkQsQ0FFRSxPQUFPVixLQUFQLEVBQWM7QUFDZEQsb0JBQWNDLEtBQWQ7QUFDRDs7QUFFRCxRQUFNVyxXQUFXRixVQUFVLENBQVYsR0FBYyxDQUEvQjtBQUNBLGFBQVNHLE9BQVQsR0FBbUI7QUFDakJULGNBQVFDLElBQVIsQ0FBYU8sUUFBYjtBQUNEOztBQUVEO0FBQ0EsUUFBSVIsUUFBUUssTUFBUixDQUFlSyxLQUFmLENBQXFCLEVBQXJCLENBQUosRUFBOEI7QUFDNUJEO0FBQ0QsS0FGRCxNQUVPO0FBQ0xULGNBQVFLLE1BQVIsQ0FBZU0sRUFBZixDQUFrQixPQUFsQixFQUEyQkYsT0FBM0I7QUFDRDtBQUNGLEc7O1dBMUI2QkYsRzs7OztTQUFBQSxHIiwiZmlsZSI6InJ1bi5qcyIsInNvdXJjZXNDb250ZW50IjpbImltcG9ydCBDbGkgZnJvbSAnLi8nXG5pbXBvcnQgVkVycm9yIGZyb20gJ3ZlcnJvcidcblxuZnVuY3Rpb24gZXhpdFdpdGhFcnJvcihlcnJvcikge1xuICBjb25zb2xlLmVycm9yKFZFcnJvci5mdWxsU3RhY2soZXJyb3IpKSAvLyBlc2xpbnQtZGlzYWJsZS1saW5lIG5vLWNvbnNvbGVcbiAgcHJvY2Vzcy5leGl0KDEpXG59XG5cbmV4cG9ydCBkZWZhdWx0IGFzeW5jIGZ1bmN0aW9uIHJ1bigpIHtcbiAgY29uc3QgY3dkID0gcHJvY2Vzcy5jd2QoKVxuICBjb25zdCBjbGkgPSBuZXcgQ2xpKHtcbiAgICBhcmd2OiBwcm9jZXNzLmFyZ3YsXG4gICAgY3dkLFxuICAgIHN0ZG91dDogcHJvY2Vzcy5zdGRvdXRcbiAgfSlcblxuICBsZXQgc3VjY2Vzc1xuICB0cnkge1xuICAgIHN1Y2Nlc3MgPSBhd2FpdCBjbGkucnVuKClcbiAgfSBjYXRjaCAoZXJyb3IpIHtcbiAgICBleGl0V2l0aEVycm9yKGVycm9yKVxuICB9XG5cbiAgY29uc3QgZXhpdENvZGUgPSBzdWNjZXNzID8gMCA6IDFcbiAgZnVuY3Rpb24gZXhpdE5vdygpIHtcbiAgICBwcm9jZXNzLmV4aXQoZXhpdENvZGUpXG4gIH1cblxuICAvLyBJZiBzdGRvdXQud3JpdGUoKSByZXR1cm5lZCBmYWxzZSwga2VybmVsIGJ1ZmZlciBpcyBub3QgZW1wdHkgeWV0XG4gIGlmIChwcm9jZXNzLnN0ZG91dC53cml0ZSgnJykpIHtcbiAgICBleGl0Tm93KClcbiAgfSBlbHNlIHtcbiAgICBwcm9jZXNzLnN0ZG91dC5vbignZHJhaW4nLCBleGl0Tm93KVxuICB9XG59XG4iXX0=