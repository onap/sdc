'use strict';

Object.defineProperty(exports, "__esModule", {
  value: true
});

var _bluebird = require('bluebird');

var _bluebird2 = _interopRequireDefault(_bluebird);

var run = function () {
  var _ref2 = (0, _bluebird.coroutine)(function* (_ref) {
    var defaultTimeout = _ref.defaultTimeout,
        hookParameter = _ref.hookParameter,
        parameterTypeRegistry = _ref.parameterTypeRegistry,
        step = _ref.step,
        stepDefinition = _ref.stepDefinition,
        world = _ref.world;

    beginTiming();
    var error = void 0,
        result = void 0,
        parameters = void 0;

    try {
      parameters = yield _bluebird2.default.all(stepDefinition.getInvocationParameters({
        hookParameter: hookParameter,
        parameterTypeRegistry: parameterTypeRegistry,
        step: step,
        world: world
      }));
    } catch (err) {
      error = err;
    }

    if (!error) {
      var timeoutInMilliseconds = stepDefinition.options.timeout || defaultTimeout;

      var validCodeLengths = stepDefinition.getValidCodeLengths(parameters);
      if (_lodash2.default.includes(validCodeLengths, stepDefinition.code.length)) {
        var data = yield _user_code_runner2.default.run({
          argsArray: parameters,
          fn: stepDefinition.code,
          thisArg: world,
          timeoutInMilliseconds: timeoutInMilliseconds
        });
        error = data.error;
        result = data.result;
      } else {
        error = stepDefinition.getInvalidCodeLengthMessage(parameters);
      }
    }

    var testStepResult = { duration: endTiming() };

    if (result === 'skipped') {
      testStepResult.status = _status2.default.SKIPPED;
    } else if (result === 'pending') {
      testStepResult.status = _status2.default.PENDING;
    } else if (error) {
      testStepResult.exception = error;
      testStepResult.status = _status2.default.FAILED;
    } else {
      testStepResult.status = _status2.default.PASSED;
    }

    return testStepResult;
  });

  return function run(_x) {
    return _ref2.apply(this, arguments);
  };
}();

var _lodash = require('lodash');

var _lodash2 = _interopRequireDefault(_lodash);

var _status = require('../status');

var _status2 = _interopRequireDefault(_status);

var _time = require('../time');

var _time2 = _interopRequireDefault(_time);

var _user_code_runner = require('../user_code_runner');

var _user_code_runner2 = _interopRequireDefault(_user_code_runner);

function _interopRequireDefault(obj) { return obj && obj.__esModule ? obj : { default: obj }; }

var beginTiming = _time2.default.beginTiming,
    endTiming = _time2.default.endTiming;
exports.default = { run: run };
//# sourceMappingURL=data:application/json;charset=utf-8;base64,eyJ2ZXJzaW9uIjozLCJzb3VyY2VzIjpbIi4uLy4uL3NyYy9ydW50aW1lL3N0ZXBfcnVubmVyLmpzIl0sIm5hbWVzIjpbImRlZmF1bHRUaW1lb3V0IiwiaG9va1BhcmFtZXRlciIsInBhcmFtZXRlclR5cGVSZWdpc3RyeSIsInN0ZXAiLCJzdGVwRGVmaW5pdGlvbiIsIndvcmxkIiwiYmVnaW5UaW1pbmciLCJlcnJvciIsInJlc3VsdCIsInBhcmFtZXRlcnMiLCJhbGwiLCJnZXRJbnZvY2F0aW9uUGFyYW1ldGVycyIsImVyciIsInRpbWVvdXRJbk1pbGxpc2Vjb25kcyIsIm9wdGlvbnMiLCJ0aW1lb3V0IiwidmFsaWRDb2RlTGVuZ3RocyIsImdldFZhbGlkQ29kZUxlbmd0aHMiLCJpbmNsdWRlcyIsImNvZGUiLCJsZW5ndGgiLCJkYXRhIiwicnVuIiwiYXJnc0FycmF5IiwiZm4iLCJ0aGlzQXJnIiwiZ2V0SW52YWxpZENvZGVMZW5ndGhNZXNzYWdlIiwidGVzdFN0ZXBSZXN1bHQiLCJkdXJhdGlvbiIsImVuZFRpbWluZyIsInN0YXR1cyIsIlNLSVBQRUQiLCJQRU5ESU5HIiwiZXhjZXB0aW9uIiwiRkFJTEVEIiwiUEFTU0VEIl0sIm1hcHBpbmdzIjoiOzs7Ozs7Ozs7Ozt1Q0FRQSxpQkFPRztBQUFBLFFBTkRBLGNBTUMsUUFOREEsY0FNQztBQUFBLFFBTERDLGFBS0MsUUFMREEsYUFLQztBQUFBLFFBSkRDLHFCQUlDLFFBSkRBLHFCQUlDO0FBQUEsUUFIREMsSUFHQyxRQUhEQSxJQUdDO0FBQUEsUUFGREMsY0FFQyxRQUZEQSxjQUVDO0FBQUEsUUFEREMsS0FDQyxRQUREQSxLQUNDOztBQUNEQztBQUNBLFFBQUlDLGNBQUo7QUFBQSxRQUFXQyxlQUFYO0FBQUEsUUFBbUJDLG1CQUFuQjs7QUFFQSxRQUFJO0FBQ0ZBLG1CQUFhLE1BQU0sbUJBQVFDLEdBQVIsQ0FDakJOLGVBQWVPLHVCQUFmLENBQXVDO0FBQ3JDVixvQ0FEcUM7QUFFckNDLG9EQUZxQztBQUdyQ0Msa0JBSHFDO0FBSXJDRTtBQUpxQyxPQUF2QyxDQURpQixDQUFuQjtBQVFELEtBVEQsQ0FTRSxPQUFPTyxHQUFQLEVBQVk7QUFDWkwsY0FBUUssR0FBUjtBQUNEOztBQUVELFFBQUksQ0FBQ0wsS0FBTCxFQUFZO0FBQ1YsVUFBTU0sd0JBQ0pULGVBQWVVLE9BQWYsQ0FBdUJDLE9BQXZCLElBQWtDZixjQURwQzs7QUFHQSxVQUFNZ0IsbUJBQW1CWixlQUFlYSxtQkFBZixDQUFtQ1IsVUFBbkMsQ0FBekI7QUFDQSxVQUFJLGlCQUFFUyxRQUFGLENBQVdGLGdCQUFYLEVBQTZCWixlQUFlZSxJQUFmLENBQW9CQyxNQUFqRCxDQUFKLEVBQThEO0FBQzVELFlBQU1DLE9BQU8sTUFBTSwyQkFBZUMsR0FBZixDQUFtQjtBQUNwQ0MscUJBQVdkLFVBRHlCO0FBRXBDZSxjQUFJcEIsZUFBZWUsSUFGaUI7QUFHcENNLG1CQUFTcEIsS0FIMkI7QUFJcENRO0FBSm9DLFNBQW5CLENBQW5CO0FBTUFOLGdCQUFRYyxLQUFLZCxLQUFiO0FBQ0FDLGlCQUFTYSxLQUFLYixNQUFkO0FBQ0QsT0FURCxNQVNPO0FBQ0xELGdCQUFRSCxlQUFlc0IsMkJBQWYsQ0FBMkNqQixVQUEzQyxDQUFSO0FBQ0Q7QUFDRjs7QUFFRCxRQUFNa0IsaUJBQWlCLEVBQUVDLFVBQVVDLFdBQVosRUFBdkI7O0FBRUEsUUFBSXJCLFdBQVcsU0FBZixFQUEwQjtBQUN4Qm1CLHFCQUFlRyxNQUFmLEdBQXdCLGlCQUFPQyxPQUEvQjtBQUNELEtBRkQsTUFFTyxJQUFJdkIsV0FBVyxTQUFmLEVBQTBCO0FBQy9CbUIscUJBQWVHLE1BQWYsR0FBd0IsaUJBQU9FLE9BQS9CO0FBQ0QsS0FGTSxNQUVBLElBQUl6QixLQUFKLEVBQVc7QUFDaEJvQixxQkFBZU0sU0FBZixHQUEyQjFCLEtBQTNCO0FBQ0FvQixxQkFBZUcsTUFBZixHQUF3QixpQkFBT0ksTUFBL0I7QUFDRCxLQUhNLE1BR0E7QUFDTFAscUJBQWVHLE1BQWYsR0FBd0IsaUJBQU9LLE1BQS9CO0FBQ0Q7O0FBRUQsV0FBT1IsY0FBUDtBQUNELEc7O2tCQXpEY0wsRzs7Ozs7QUFSZjs7OztBQUNBOzs7O0FBQ0E7Ozs7QUFDQTs7Ozs7O0lBR1FoQixXLGtCQUFBQSxXO0lBQWF1QixTLGtCQUFBQSxTO2tCQTZETixFQUFFUCxRQUFGLEUiLCJmaWxlIjoic3RlcF9ydW5uZXIuanMiLCJzb3VyY2VzQ29udGVudCI6WyJpbXBvcnQgXyBmcm9tICdsb2Rhc2gnXG5pbXBvcnQgU3RhdHVzIGZyb20gJy4uL3N0YXR1cydcbmltcG9ydCBUaW1lIGZyb20gJy4uL3RpbWUnXG5pbXBvcnQgVXNlckNvZGVSdW5uZXIgZnJvbSAnLi4vdXNlcl9jb2RlX3J1bm5lcidcbmltcG9ydCBQcm9taXNlIGZyb20gJ2JsdWViaXJkJ1xuXG5jb25zdCB7IGJlZ2luVGltaW5nLCBlbmRUaW1pbmcgfSA9IFRpbWVcblxuYXN5bmMgZnVuY3Rpb24gcnVuKHtcbiAgZGVmYXVsdFRpbWVvdXQsXG4gIGhvb2tQYXJhbWV0ZXIsXG4gIHBhcmFtZXRlclR5cGVSZWdpc3RyeSxcbiAgc3RlcCxcbiAgc3RlcERlZmluaXRpb24sXG4gIHdvcmxkXG59KSB7XG4gIGJlZ2luVGltaW5nKClcbiAgbGV0IGVycm9yLCByZXN1bHQsIHBhcmFtZXRlcnNcblxuICB0cnkge1xuICAgIHBhcmFtZXRlcnMgPSBhd2FpdCBQcm9taXNlLmFsbChcbiAgICAgIHN0ZXBEZWZpbml0aW9uLmdldEludm9jYXRpb25QYXJhbWV0ZXJzKHtcbiAgICAgICAgaG9va1BhcmFtZXRlcixcbiAgICAgICAgcGFyYW1ldGVyVHlwZVJlZ2lzdHJ5LFxuICAgICAgICBzdGVwLFxuICAgICAgICB3b3JsZFxuICAgICAgfSlcbiAgICApXG4gIH0gY2F0Y2ggKGVycikge1xuICAgIGVycm9yID0gZXJyXG4gIH1cblxuICBpZiAoIWVycm9yKSB7XG4gICAgY29uc3QgdGltZW91dEluTWlsbGlzZWNvbmRzID1cbiAgICAgIHN0ZXBEZWZpbml0aW9uLm9wdGlvbnMudGltZW91dCB8fCBkZWZhdWx0VGltZW91dFxuXG4gICAgY29uc3QgdmFsaWRDb2RlTGVuZ3RocyA9IHN0ZXBEZWZpbml0aW9uLmdldFZhbGlkQ29kZUxlbmd0aHMocGFyYW1ldGVycylcbiAgICBpZiAoXy5pbmNsdWRlcyh2YWxpZENvZGVMZW5ndGhzLCBzdGVwRGVmaW5pdGlvbi5jb2RlLmxlbmd0aCkpIHtcbiAgICAgIGNvbnN0IGRhdGEgPSBhd2FpdCBVc2VyQ29kZVJ1bm5lci5ydW4oe1xuICAgICAgICBhcmdzQXJyYXk6IHBhcmFtZXRlcnMsXG4gICAgICAgIGZuOiBzdGVwRGVmaW5pdGlvbi5jb2RlLFxuICAgICAgICB0aGlzQXJnOiB3b3JsZCxcbiAgICAgICAgdGltZW91dEluTWlsbGlzZWNvbmRzXG4gICAgICB9KVxuICAgICAgZXJyb3IgPSBkYXRhLmVycm9yXG4gICAgICByZXN1bHQgPSBkYXRhLnJlc3VsdFxuICAgIH0gZWxzZSB7XG4gICAgICBlcnJvciA9IHN0ZXBEZWZpbml0aW9uLmdldEludmFsaWRDb2RlTGVuZ3RoTWVzc2FnZShwYXJhbWV0ZXJzKVxuICAgIH1cbiAgfVxuXG4gIGNvbnN0IHRlc3RTdGVwUmVzdWx0ID0geyBkdXJhdGlvbjogZW5kVGltaW5nKCkgfVxuXG4gIGlmIChyZXN1bHQgPT09ICdza2lwcGVkJykge1xuICAgIHRlc3RTdGVwUmVzdWx0LnN0YXR1cyA9IFN0YXR1cy5TS0lQUEVEXG4gIH0gZWxzZSBpZiAocmVzdWx0ID09PSAncGVuZGluZycpIHtcbiAgICB0ZXN0U3RlcFJlc3VsdC5zdGF0dXMgPSBTdGF0dXMuUEVORElOR1xuICB9IGVsc2UgaWYgKGVycm9yKSB7XG4gICAgdGVzdFN0ZXBSZXN1bHQuZXhjZXB0aW9uID0gZXJyb3JcbiAgICB0ZXN0U3RlcFJlc3VsdC5zdGF0dXMgPSBTdGF0dXMuRkFJTEVEXG4gIH0gZWxzZSB7XG4gICAgdGVzdFN0ZXBSZXN1bHQuc3RhdHVzID0gU3RhdHVzLlBBU1NFRFxuICB9XG5cbiAgcmV0dXJuIHRlc3RTdGVwUmVzdWx0XG59XG5cbmV4cG9ydCBkZWZhdWx0IHsgcnVuIH1cbiJdfQ==