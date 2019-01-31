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

var _time = require('./time');

var _time2 = _interopRequireDefault(_time);

var _uncaught_exception_manager = require('./uncaught_exception_manager');

var _uncaught_exception_manager2 = _interopRequireDefault(_uncaught_exception_manager);

var _util = require('util');

var _util2 = _interopRequireDefault(_util);

function _interopRequireDefault(obj) { return obj && obj.__esModule ? obj : { default: obj }; }

var UserCodeRunner = function () {
  function UserCodeRunner() {
    (0, _classCallCheck3.default)(this, UserCodeRunner);
  }

  (0, _createClass3.default)(UserCodeRunner, null, [{
    key: 'run',
    value: function () {
      var _ref2 = (0, _bluebird.coroutine)(function* (_ref) {
        var argsArray = _ref.argsArray,
            thisArg = _ref.thisArg,
            fn = _ref.fn,
            timeoutInMilliseconds = _ref.timeoutInMilliseconds;

        var callbackPromise = new _bluebird2.default(function (resolve, reject) {
          argsArray.push(function (error, result) {
            if (error) {
              reject(error);
            } else {
              resolve(result);
            }
          });
        });

        var fnReturn = void 0;
        try {
          fnReturn = fn.apply(thisArg, argsArray);
        } catch (e) {
          var _error = e instanceof Error ? e : new Error(_util2.default.format(e));
          return { error: _error };
        }

        var racingPromises = [];
        var callbackInterface = fn.length === argsArray.length;
        var promiseInterface = fnReturn && typeof fnReturn.then === 'function';

        if (callbackInterface && promiseInterface) {
          return {
            error: new Error('function uses multiple asynchronous interfaces: callback and promise\n' + 'to use the callback interface: do not return a promise\n' + 'to use the promise interface: remove the last argument to the function')
          };
        } else if (callbackInterface) {
          racingPromises.push(callbackPromise);
        } else if (promiseInterface) {
          racingPromises.push(fnReturn);
        } else {
          return { result: fnReturn };
        }

        var exceptionHandler = void 0;
        var uncaughtExceptionPromise = new _bluebird2.default(function (resolve, reject) {
          exceptionHandler = reject;
          _uncaught_exception_manager2.default.registerHandler(exceptionHandler);
        });
        racingPromises.push(uncaughtExceptionPromise);

        var timeoutId = void 0;
        if (timeoutInMilliseconds >= 0) {
          var timeoutPromise = new _bluebird2.default(function (resolve, reject) {
            timeoutId = _time2.default.setTimeout(function () {
              var timeoutMessage = 'function timed out after ' + timeoutInMilliseconds + ' milliseconds';
              reject(new Error(timeoutMessage));
            }, timeoutInMilliseconds);
          });
          racingPromises.push(timeoutPromise);
        }

        var error = void 0,
            result = void 0;
        try {
          result = yield _bluebird2.default.race(racingPromises);
        } catch (e) {
          if (e instanceof Error) {
            error = e;
          } else if (e) {
            error = new Error(_util2.default.format(e));
          } else {
            error = new Error('Promise rejected without a reason');
          }
        }

        _time2.default.clearTimeout(timeoutId);
        _uncaught_exception_manager2.default.unregisterHandler(exceptionHandler);

        return { error: error, result: result };
      });

      function run(_x) {
        return _ref2.apply(this, arguments);
      }

      return run;
    }()
  }]);
  return UserCodeRunner;
}();

exports.default = UserCodeRunner;
//# sourceMappingURL=data:application/json;charset=utf-8;base64,eyJ2ZXJzaW9uIjozLCJzb3VyY2VzIjpbIi4uL3NyYy91c2VyX2NvZGVfcnVubmVyLmpzIl0sIm5hbWVzIjpbIlVzZXJDb2RlUnVubmVyIiwiYXJnc0FycmF5IiwidGhpc0FyZyIsImZuIiwidGltZW91dEluTWlsbGlzZWNvbmRzIiwiY2FsbGJhY2tQcm9taXNlIiwicmVzb2x2ZSIsInJlamVjdCIsInB1c2giLCJlcnJvciIsInJlc3VsdCIsImZuUmV0dXJuIiwiYXBwbHkiLCJlIiwiRXJyb3IiLCJmb3JtYXQiLCJyYWNpbmdQcm9taXNlcyIsImNhbGxiYWNrSW50ZXJmYWNlIiwibGVuZ3RoIiwicHJvbWlzZUludGVyZmFjZSIsInRoZW4iLCJleGNlcHRpb25IYW5kbGVyIiwidW5jYXVnaHRFeGNlcHRpb25Qcm9taXNlIiwicmVnaXN0ZXJIYW5kbGVyIiwidGltZW91dElkIiwidGltZW91dFByb21pc2UiLCJzZXRUaW1lb3V0IiwidGltZW91dE1lc3NhZ2UiLCJyYWNlIiwiY2xlYXJUaW1lb3V0IiwidW5yZWdpc3RlckhhbmRsZXIiXSwibWFwcGluZ3MiOiI7Ozs7Ozs7Ozs7Ozs7Ozs7OztBQUNBOzs7O0FBQ0E7Ozs7QUFDQTs7Ozs7O0lBRXFCQSxjOzs7Ozs7Ozs0REFDaUQ7QUFBQSxZQUFqREMsU0FBaUQsUUFBakRBLFNBQWlEO0FBQUEsWUFBdENDLE9BQXNDLFFBQXRDQSxPQUFzQztBQUFBLFlBQTdCQyxFQUE2QixRQUE3QkEsRUFBNkI7QUFBQSxZQUF6QkMscUJBQXlCLFFBQXpCQSxxQkFBeUI7O0FBQ2xFLFlBQU1DLGtCQUFrQix1QkFBWSxVQUFTQyxPQUFULEVBQWtCQyxNQUFsQixFQUEwQjtBQUM1RE4sb0JBQVVPLElBQVYsQ0FBZSxVQUFTQyxLQUFULEVBQWdCQyxNQUFoQixFQUF3QjtBQUNyQyxnQkFBSUQsS0FBSixFQUFXO0FBQ1RGLHFCQUFPRSxLQUFQO0FBQ0QsYUFGRCxNQUVPO0FBQ0xILHNCQUFRSSxNQUFSO0FBQ0Q7QUFDRixXQU5EO0FBT0QsU0FSdUIsQ0FBeEI7O0FBVUEsWUFBSUMsaUJBQUo7QUFDQSxZQUFJO0FBQ0ZBLHFCQUFXUixHQUFHUyxLQUFILENBQVNWLE9BQVQsRUFBa0JELFNBQWxCLENBQVg7QUFDRCxTQUZELENBRUUsT0FBT1ksQ0FBUCxFQUFVO0FBQ1YsY0FBTUosU0FBUUksYUFBYUMsS0FBYixHQUFxQkQsQ0FBckIsR0FBeUIsSUFBSUMsS0FBSixDQUFVLGVBQUtDLE1BQUwsQ0FBWUYsQ0FBWixDQUFWLENBQXZDO0FBQ0EsaUJBQU8sRUFBRUosYUFBRixFQUFQO0FBQ0Q7O0FBRUQsWUFBTU8saUJBQWlCLEVBQXZCO0FBQ0EsWUFBTUMsb0JBQW9CZCxHQUFHZSxNQUFILEtBQWNqQixVQUFVaUIsTUFBbEQ7QUFDQSxZQUFNQyxtQkFBbUJSLFlBQVksT0FBT0EsU0FBU1MsSUFBaEIsS0FBeUIsVUFBOUQ7O0FBRUEsWUFBSUgscUJBQXFCRSxnQkFBekIsRUFBMkM7QUFDekMsaUJBQU87QUFDTFYsbUJBQU8sSUFBSUssS0FBSixDQUNMLDJFQUNFLDBEQURGLEdBRUUsd0VBSEc7QUFERixXQUFQO0FBT0QsU0FSRCxNQVFPLElBQUlHLGlCQUFKLEVBQXVCO0FBQzVCRCx5QkFBZVIsSUFBZixDQUFvQkgsZUFBcEI7QUFDRCxTQUZNLE1BRUEsSUFBSWMsZ0JBQUosRUFBc0I7QUFDM0JILHlCQUFlUixJQUFmLENBQW9CRyxRQUFwQjtBQUNELFNBRk0sTUFFQTtBQUNMLGlCQUFPLEVBQUVELFFBQVFDLFFBQVYsRUFBUDtBQUNEOztBQUVELFlBQUlVLHlCQUFKO0FBQ0EsWUFBTUMsMkJBQTJCLHVCQUFZLFVBQVNoQixPQUFULEVBQWtCQyxNQUFsQixFQUEwQjtBQUNyRWMsNkJBQW1CZCxNQUFuQjtBQUNBLCtDQUF5QmdCLGVBQXpCLENBQXlDRixnQkFBekM7QUFDRCxTQUhnQyxDQUFqQztBQUlBTCx1QkFBZVIsSUFBZixDQUFvQmMsd0JBQXBCOztBQUVBLFlBQUlFLGtCQUFKO0FBQ0EsWUFBSXBCLHlCQUF5QixDQUE3QixFQUFnQztBQUM5QixjQUFNcUIsaUJBQWlCLHVCQUFZLFVBQVNuQixPQUFULEVBQWtCQyxNQUFsQixFQUEwQjtBQUMzRGlCLHdCQUFZLGVBQUtFLFVBQUwsQ0FBZ0IsWUFBVztBQUNyQyxrQkFBTUMsaUJBQ0osOEJBQ0F2QixxQkFEQSxHQUVBLGVBSEY7QUFJQUcscUJBQU8sSUFBSU8sS0FBSixDQUFVYSxjQUFWLENBQVA7QUFDRCxhQU5XLEVBTVR2QixxQkFOUyxDQUFaO0FBT0QsV0FSc0IsQ0FBdkI7QUFTQVkseUJBQWVSLElBQWYsQ0FBb0JpQixjQUFwQjtBQUNEOztBQUVELFlBQUloQixjQUFKO0FBQUEsWUFBV0MsZUFBWDtBQUNBLFlBQUk7QUFDRkEsbUJBQVMsTUFBTSxtQkFBUWtCLElBQVIsQ0FBYVosY0FBYixDQUFmO0FBQ0QsU0FGRCxDQUVFLE9BQU9ILENBQVAsRUFBVTtBQUNWLGNBQUlBLGFBQWFDLEtBQWpCLEVBQXdCO0FBQ3RCTCxvQkFBUUksQ0FBUjtBQUNELFdBRkQsTUFFTyxJQUFJQSxDQUFKLEVBQU87QUFDWkosb0JBQVEsSUFBSUssS0FBSixDQUFVLGVBQUtDLE1BQUwsQ0FBWUYsQ0FBWixDQUFWLENBQVI7QUFDRCxXQUZNLE1BRUE7QUFDTEosb0JBQVEsSUFBSUssS0FBSixDQUFVLG1DQUFWLENBQVI7QUFDRDtBQUNGOztBQUVELHVCQUFLZSxZQUFMLENBQWtCTCxTQUFsQjtBQUNBLDZDQUF5Qk0saUJBQXpCLENBQTJDVCxnQkFBM0M7O0FBRUEsZUFBTyxFQUFFWixZQUFGLEVBQVNDLGNBQVQsRUFBUDtBQUNELE87Ozs7Ozs7Ozs7OztrQkE5RWtCVixjIiwiZmlsZSI6InVzZXJfY29kZV9ydW5uZXIuanMiLCJzb3VyY2VzQ29udGVudCI6WyJpbXBvcnQgUHJvbWlzZSBmcm9tICdibHVlYmlyZCdcbmltcG9ydCBUaW1lIGZyb20gJy4vdGltZSdcbmltcG9ydCBVbmNhdWdodEV4Y2VwdGlvbk1hbmFnZXIgZnJvbSAnLi91bmNhdWdodF9leGNlcHRpb25fbWFuYWdlcidcbmltcG9ydCB1dGlsIGZyb20gJ3V0aWwnXG5cbmV4cG9ydCBkZWZhdWx0IGNsYXNzIFVzZXJDb2RlUnVubmVyIHtcbiAgc3RhdGljIGFzeW5jIHJ1bih7IGFyZ3NBcnJheSwgdGhpc0FyZywgZm4sIHRpbWVvdXRJbk1pbGxpc2Vjb25kcyB9KSB7XG4gICAgY29uc3QgY2FsbGJhY2tQcm9taXNlID0gbmV3IFByb21pc2UoZnVuY3Rpb24ocmVzb2x2ZSwgcmVqZWN0KSB7XG4gICAgICBhcmdzQXJyYXkucHVzaChmdW5jdGlvbihlcnJvciwgcmVzdWx0KSB7XG4gICAgICAgIGlmIChlcnJvcikge1xuICAgICAgICAgIHJlamVjdChlcnJvcilcbiAgICAgICAgfSBlbHNlIHtcbiAgICAgICAgICByZXNvbHZlKHJlc3VsdClcbiAgICAgICAgfVxuICAgICAgfSlcbiAgICB9KVxuXG4gICAgbGV0IGZuUmV0dXJuXG4gICAgdHJ5IHtcbiAgICAgIGZuUmV0dXJuID0gZm4uYXBwbHkodGhpc0FyZywgYXJnc0FycmF5KVxuICAgIH0gY2F0Y2ggKGUpIHtcbiAgICAgIGNvbnN0IGVycm9yID0gZSBpbnN0YW5jZW9mIEVycm9yID8gZSA6IG5ldyBFcnJvcih1dGlsLmZvcm1hdChlKSlcbiAgICAgIHJldHVybiB7IGVycm9yIH1cbiAgICB9XG5cbiAgICBjb25zdCByYWNpbmdQcm9taXNlcyA9IFtdXG4gICAgY29uc3QgY2FsbGJhY2tJbnRlcmZhY2UgPSBmbi5sZW5ndGggPT09IGFyZ3NBcnJheS5sZW5ndGhcbiAgICBjb25zdCBwcm9taXNlSW50ZXJmYWNlID0gZm5SZXR1cm4gJiYgdHlwZW9mIGZuUmV0dXJuLnRoZW4gPT09ICdmdW5jdGlvbidcblxuICAgIGlmIChjYWxsYmFja0ludGVyZmFjZSAmJiBwcm9taXNlSW50ZXJmYWNlKSB7XG4gICAgICByZXR1cm4ge1xuICAgICAgICBlcnJvcjogbmV3IEVycm9yKFxuICAgICAgICAgICdmdW5jdGlvbiB1c2VzIG11bHRpcGxlIGFzeW5jaHJvbm91cyBpbnRlcmZhY2VzOiBjYWxsYmFjayBhbmQgcHJvbWlzZVxcbicgK1xuICAgICAgICAgICAgJ3RvIHVzZSB0aGUgY2FsbGJhY2sgaW50ZXJmYWNlOiBkbyBub3QgcmV0dXJuIGEgcHJvbWlzZVxcbicgK1xuICAgICAgICAgICAgJ3RvIHVzZSB0aGUgcHJvbWlzZSBpbnRlcmZhY2U6IHJlbW92ZSB0aGUgbGFzdCBhcmd1bWVudCB0byB0aGUgZnVuY3Rpb24nXG4gICAgICAgIClcbiAgICAgIH1cbiAgICB9IGVsc2UgaWYgKGNhbGxiYWNrSW50ZXJmYWNlKSB7XG4gICAgICByYWNpbmdQcm9taXNlcy5wdXNoKGNhbGxiYWNrUHJvbWlzZSlcbiAgICB9IGVsc2UgaWYgKHByb21pc2VJbnRlcmZhY2UpIHtcbiAgICAgIHJhY2luZ1Byb21pc2VzLnB1c2goZm5SZXR1cm4pXG4gICAgfSBlbHNlIHtcbiAgICAgIHJldHVybiB7IHJlc3VsdDogZm5SZXR1cm4gfVxuICAgIH1cblxuICAgIGxldCBleGNlcHRpb25IYW5kbGVyXG4gICAgY29uc3QgdW5jYXVnaHRFeGNlcHRpb25Qcm9taXNlID0gbmV3IFByb21pc2UoZnVuY3Rpb24ocmVzb2x2ZSwgcmVqZWN0KSB7XG4gICAgICBleGNlcHRpb25IYW5kbGVyID0gcmVqZWN0XG4gICAgICBVbmNhdWdodEV4Y2VwdGlvbk1hbmFnZXIucmVnaXN0ZXJIYW5kbGVyKGV4Y2VwdGlvbkhhbmRsZXIpXG4gICAgfSlcbiAgICByYWNpbmdQcm9taXNlcy5wdXNoKHVuY2F1Z2h0RXhjZXB0aW9uUHJvbWlzZSlcblxuICAgIGxldCB0aW1lb3V0SWRcbiAgICBpZiAodGltZW91dEluTWlsbGlzZWNvbmRzID49IDApIHtcbiAgICAgIGNvbnN0IHRpbWVvdXRQcm9taXNlID0gbmV3IFByb21pc2UoZnVuY3Rpb24ocmVzb2x2ZSwgcmVqZWN0KSB7XG4gICAgICAgIHRpbWVvdXRJZCA9IFRpbWUuc2V0VGltZW91dChmdW5jdGlvbigpIHtcbiAgICAgICAgICBjb25zdCB0aW1lb3V0TWVzc2FnZSA9XG4gICAgICAgICAgICAnZnVuY3Rpb24gdGltZWQgb3V0IGFmdGVyICcgK1xuICAgICAgICAgICAgdGltZW91dEluTWlsbGlzZWNvbmRzICtcbiAgICAgICAgICAgICcgbWlsbGlzZWNvbmRzJ1xuICAgICAgICAgIHJlamVjdChuZXcgRXJyb3IodGltZW91dE1lc3NhZ2UpKVxuICAgICAgICB9LCB0aW1lb3V0SW5NaWxsaXNlY29uZHMpXG4gICAgICB9KVxuICAgICAgcmFjaW5nUHJvbWlzZXMucHVzaCh0aW1lb3V0UHJvbWlzZSlcbiAgICB9XG5cbiAgICBsZXQgZXJyb3IsIHJlc3VsdFxuICAgIHRyeSB7XG4gICAgICByZXN1bHQgPSBhd2FpdCBQcm9taXNlLnJhY2UocmFjaW5nUHJvbWlzZXMpXG4gICAgfSBjYXRjaCAoZSkge1xuICAgICAgaWYgKGUgaW5zdGFuY2VvZiBFcnJvcikge1xuICAgICAgICBlcnJvciA9IGVcbiAgICAgIH0gZWxzZSBpZiAoZSkge1xuICAgICAgICBlcnJvciA9IG5ldyBFcnJvcih1dGlsLmZvcm1hdChlKSlcbiAgICAgIH0gZWxzZSB7XG4gICAgICAgIGVycm9yID0gbmV3IEVycm9yKCdQcm9taXNlIHJlamVjdGVkIHdpdGhvdXQgYSByZWFzb24nKVxuICAgICAgfVxuICAgIH1cblxuICAgIFRpbWUuY2xlYXJUaW1lb3V0KHRpbWVvdXRJZClcbiAgICBVbmNhdWdodEV4Y2VwdGlvbk1hbmFnZXIudW5yZWdpc3RlckhhbmRsZXIoZXhjZXB0aW9uSGFuZGxlcilcblxuICAgIHJldHVybiB7IGVycm9yLCByZXN1bHQgfVxuICB9XG59XG4iXX0=