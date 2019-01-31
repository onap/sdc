'use strict';

Object.defineProperty(exports, "__esModule", {
  value: true
});

var _classCallCheck2 = require('babel-runtime/helpers/classCallCheck');

var _classCallCheck3 = _interopRequireDefault(_classCallCheck2);

var _createClass2 = require('babel-runtime/helpers/createClass');

var _createClass3 = _interopRequireDefault(_createClass2);

var _cucumberExpressions = require('cucumber-expressions');

var _data_table = require('./data_table');

var _data_table2 = _interopRequireDefault(_data_table);

var _step_arguments = require('../step_arguments');

function _interopRequireDefault(obj) { return obj && obj.__esModule ? obj : { default: obj }; }

var StepDefinition = function () {
  function StepDefinition(_ref) {
    var code = _ref.code,
        line = _ref.line,
        options = _ref.options,
        pattern = _ref.pattern,
        uri = _ref.uri;
    (0, _classCallCheck3.default)(this, StepDefinition);

    this.code = code;
    this.line = line;
    this.options = options;
    this.pattern = pattern;
    this.uri = uri;
  }

  (0, _createClass3.default)(StepDefinition, [{
    key: 'buildInvalidCodeLengthMessage',
    value: function buildInvalidCodeLengthMessage(syncOrPromiseLength, callbackLength) {
      return 'function has ' + this.code.length + ' arguments' + ', should have ' + syncOrPromiseLength + ' (if synchronous or returning a promise)' + ' or ' + callbackLength + ' (if accepting a callback)';
    }
  }, {
    key: 'getInvalidCodeLengthMessage',
    value: function getInvalidCodeLengthMessage(parameters) {
      return this.buildInvalidCodeLengthMessage(parameters.length, parameters.length + 1);
    }
  }, {
    key: 'getInvocationParameters',
    value: function getInvocationParameters(_ref2) {
      var step = _ref2.step,
          parameterTypeRegistry = _ref2.parameterTypeRegistry,
          world = _ref2.world;

      var cucumberExpression = this.getCucumberExpression(parameterTypeRegistry);
      var stepNameParameters = cucumberExpression.match(step.text).map(function (arg) {
        return arg.getValue(world);
      });
      var iterator = (0, _step_arguments.buildStepArgumentIterator)({
        dataTable: function dataTable(arg) {
          return new _data_table2.default(arg);
        },
        docString: function docString(arg) {
          return arg.content;
        }
      });
      var stepArgumentParameters = step.arguments.map(iterator);
      return stepNameParameters.concat(stepArgumentParameters);
    }
  }, {
    key: 'getCucumberExpression',
    value: function getCucumberExpression(parameterTypeRegistry) {
      if (typeof this.pattern === 'string') {
        return new _cucumberExpressions.CucumberExpression(this.pattern, parameterTypeRegistry);
      } else {
        return new _cucumberExpressions.RegularExpression(this.pattern, parameterTypeRegistry);
      }
    }
  }, {
    key: 'getValidCodeLengths',
    value: function getValidCodeLengths(parameters) {
      return [parameters.length, parameters.length + 1];
    }
  }, {
    key: 'matchesStepName',
    value: function matchesStepName(_ref3) {
      var stepName = _ref3.stepName,
          parameterTypeRegistry = _ref3.parameterTypeRegistry;

      var cucumberExpression = this.getCucumberExpression(parameterTypeRegistry);
      return Boolean(cucumberExpression.match(stepName));
    }
  }]);
  return StepDefinition;
}();

exports.default = StepDefinition;
//# sourceMappingURL=data:application/json;charset=utf-8;base64,eyJ2ZXJzaW9uIjozLCJzb3VyY2VzIjpbIi4uLy4uL3NyYy9tb2RlbHMvc3RlcF9kZWZpbml0aW9uLmpzIl0sIm5hbWVzIjpbIlN0ZXBEZWZpbml0aW9uIiwiY29kZSIsImxpbmUiLCJvcHRpb25zIiwicGF0dGVybiIsInVyaSIsInN5bmNPclByb21pc2VMZW5ndGgiLCJjYWxsYmFja0xlbmd0aCIsImxlbmd0aCIsInBhcmFtZXRlcnMiLCJidWlsZEludmFsaWRDb2RlTGVuZ3RoTWVzc2FnZSIsInN0ZXAiLCJwYXJhbWV0ZXJUeXBlUmVnaXN0cnkiLCJ3b3JsZCIsImN1Y3VtYmVyRXhwcmVzc2lvbiIsImdldEN1Y3VtYmVyRXhwcmVzc2lvbiIsInN0ZXBOYW1lUGFyYW1ldGVycyIsIm1hdGNoIiwidGV4dCIsIm1hcCIsImFyZyIsImdldFZhbHVlIiwiaXRlcmF0b3IiLCJkYXRhVGFibGUiLCJkb2NTdHJpbmciLCJjb250ZW50Iiwic3RlcEFyZ3VtZW50UGFyYW1ldGVycyIsImFyZ3VtZW50cyIsImNvbmNhdCIsInN0ZXBOYW1lIiwiQm9vbGVhbiJdLCJtYXBwaW5ncyI6Ijs7Ozs7Ozs7Ozs7Ozs7QUFBQTs7QUFDQTs7OztBQUNBOzs7O0lBRXFCQSxjO0FBQ25CLGdDQUFtRDtBQUFBLFFBQXJDQyxJQUFxQyxRQUFyQ0EsSUFBcUM7QUFBQSxRQUEvQkMsSUFBK0IsUUFBL0JBLElBQStCO0FBQUEsUUFBekJDLE9BQXlCLFFBQXpCQSxPQUF5QjtBQUFBLFFBQWhCQyxPQUFnQixRQUFoQkEsT0FBZ0I7QUFBQSxRQUFQQyxHQUFPLFFBQVBBLEdBQU87QUFBQTs7QUFDakQsU0FBS0osSUFBTCxHQUFZQSxJQUFaO0FBQ0EsU0FBS0MsSUFBTCxHQUFZQSxJQUFaO0FBQ0EsU0FBS0MsT0FBTCxHQUFlQSxPQUFmO0FBQ0EsU0FBS0MsT0FBTCxHQUFlQSxPQUFmO0FBQ0EsU0FBS0MsR0FBTCxHQUFXQSxHQUFYO0FBQ0Q7Ozs7a0RBRTZCQyxtQixFQUFxQkMsYyxFQUFnQjtBQUNqRSxhQUNFLGtCQUNBLEtBQUtOLElBQUwsQ0FBVU8sTUFEVixHQUVBLFlBRkEsR0FHQSxnQkFIQSxHQUlBRixtQkFKQSxHQUtBLDBDQUxBLEdBTUEsTUFOQSxHQU9BQyxjQVBBLEdBUUEsNEJBVEY7QUFXRDs7O2dEQUUyQkUsVSxFQUFZO0FBQ3RDLGFBQU8sS0FBS0MsNkJBQUwsQ0FDTEQsV0FBV0QsTUFETixFQUVMQyxXQUFXRCxNQUFYLEdBQW9CLENBRmYsQ0FBUDtBQUlEOzs7bURBRStEO0FBQUEsVUFBdENHLElBQXNDLFNBQXRDQSxJQUFzQztBQUFBLFVBQWhDQyxxQkFBZ0MsU0FBaENBLHFCQUFnQztBQUFBLFVBQVRDLEtBQVMsU0FBVEEsS0FBUzs7QUFDOUQsVUFBTUMscUJBQXFCLEtBQUtDLHFCQUFMLENBQTJCSCxxQkFBM0IsQ0FBM0I7QUFDQSxVQUFNSSxxQkFBcUJGLG1CQUN4QkcsS0FEd0IsQ0FDbEJOLEtBQUtPLElBRGEsRUFFeEJDLEdBRndCLENBRXBCO0FBQUEsZUFBT0MsSUFBSUMsUUFBSixDQUFhUixLQUFiLENBQVA7QUFBQSxPQUZvQixDQUEzQjtBQUdBLFVBQU1TLFdBQVcsK0NBQTBCO0FBQ3pDQyxtQkFBVztBQUFBLGlCQUFPLHlCQUFjSCxHQUFkLENBQVA7QUFBQSxTQUQ4QjtBQUV6Q0ksbUJBQVc7QUFBQSxpQkFBT0osSUFBSUssT0FBWDtBQUFBO0FBRjhCLE9BQTFCLENBQWpCO0FBSUEsVUFBTUMseUJBQXlCZixLQUFLZ0IsU0FBTCxDQUFlUixHQUFmLENBQW1CRyxRQUFuQixDQUEvQjtBQUNBLGFBQU9OLG1CQUFtQlksTUFBbkIsQ0FBMEJGLHNCQUExQixDQUFQO0FBQ0Q7OzswQ0FFcUJkLHFCLEVBQXVCO0FBQzNDLFVBQUksT0FBTyxLQUFLUixPQUFaLEtBQXdCLFFBQTVCLEVBQXNDO0FBQ3BDLGVBQU8sNENBQXVCLEtBQUtBLE9BQTVCLEVBQXFDUSxxQkFBckMsQ0FBUDtBQUNELE9BRkQsTUFFTztBQUNMLGVBQU8sMkNBQXNCLEtBQUtSLE9BQTNCLEVBQW9DUSxxQkFBcEMsQ0FBUDtBQUNEO0FBQ0Y7Ozt3Q0FFbUJILFUsRUFBWTtBQUM5QixhQUFPLENBQUNBLFdBQVdELE1BQVosRUFBb0JDLFdBQVdELE1BQVgsR0FBb0IsQ0FBeEMsQ0FBUDtBQUNEOzs7MkNBRW9EO0FBQUEsVUFBbkNxQixRQUFtQyxTQUFuQ0EsUUFBbUM7QUFBQSxVQUF6QmpCLHFCQUF5QixTQUF6QkEscUJBQXlCOztBQUNuRCxVQUFNRSxxQkFBcUIsS0FBS0MscUJBQUwsQ0FBMkJILHFCQUEzQixDQUEzQjtBQUNBLGFBQU9rQixRQUFRaEIsbUJBQW1CRyxLQUFuQixDQUF5QlksUUFBekIsQ0FBUixDQUFQO0FBQ0Q7Ozs7O2tCQTFEa0I3QixjIiwiZmlsZSI6InN0ZXBfZGVmaW5pdGlvbi5qcyIsInNvdXJjZXNDb250ZW50IjpbImltcG9ydCB7IEN1Y3VtYmVyRXhwcmVzc2lvbiwgUmVndWxhckV4cHJlc3Npb24gfSBmcm9tICdjdWN1bWJlci1leHByZXNzaW9ucydcbmltcG9ydCBEYXRhVGFibGUgZnJvbSAnLi9kYXRhX3RhYmxlJ1xuaW1wb3J0IHsgYnVpbGRTdGVwQXJndW1lbnRJdGVyYXRvciB9IGZyb20gJy4uL3N0ZXBfYXJndW1lbnRzJ1xuXG5leHBvcnQgZGVmYXVsdCBjbGFzcyBTdGVwRGVmaW5pdGlvbiB7XG4gIGNvbnN0cnVjdG9yKHsgY29kZSwgbGluZSwgb3B0aW9ucywgcGF0dGVybiwgdXJpIH0pIHtcbiAgICB0aGlzLmNvZGUgPSBjb2RlXG4gICAgdGhpcy5saW5lID0gbGluZVxuICAgIHRoaXMub3B0aW9ucyA9IG9wdGlvbnNcbiAgICB0aGlzLnBhdHRlcm4gPSBwYXR0ZXJuXG4gICAgdGhpcy51cmkgPSB1cmlcbiAgfVxuXG4gIGJ1aWxkSW52YWxpZENvZGVMZW5ndGhNZXNzYWdlKHN5bmNPclByb21pc2VMZW5ndGgsIGNhbGxiYWNrTGVuZ3RoKSB7XG4gICAgcmV0dXJuIChcbiAgICAgICdmdW5jdGlvbiBoYXMgJyArXG4gICAgICB0aGlzLmNvZGUubGVuZ3RoICtcbiAgICAgICcgYXJndW1lbnRzJyArXG4gICAgICAnLCBzaG91bGQgaGF2ZSAnICtcbiAgICAgIHN5bmNPclByb21pc2VMZW5ndGggK1xuICAgICAgJyAoaWYgc3luY2hyb25vdXMgb3IgcmV0dXJuaW5nIGEgcHJvbWlzZSknICtcbiAgICAgICcgb3IgJyArXG4gICAgICBjYWxsYmFja0xlbmd0aCArXG4gICAgICAnIChpZiBhY2NlcHRpbmcgYSBjYWxsYmFjayknXG4gICAgKVxuICB9XG5cbiAgZ2V0SW52YWxpZENvZGVMZW5ndGhNZXNzYWdlKHBhcmFtZXRlcnMpIHtcbiAgICByZXR1cm4gdGhpcy5idWlsZEludmFsaWRDb2RlTGVuZ3RoTWVzc2FnZShcbiAgICAgIHBhcmFtZXRlcnMubGVuZ3RoLFxuICAgICAgcGFyYW1ldGVycy5sZW5ndGggKyAxXG4gICAgKVxuICB9XG5cbiAgZ2V0SW52b2NhdGlvblBhcmFtZXRlcnMoeyBzdGVwLCBwYXJhbWV0ZXJUeXBlUmVnaXN0cnksIHdvcmxkIH0pIHtcbiAgICBjb25zdCBjdWN1bWJlckV4cHJlc3Npb24gPSB0aGlzLmdldEN1Y3VtYmVyRXhwcmVzc2lvbihwYXJhbWV0ZXJUeXBlUmVnaXN0cnkpXG4gICAgY29uc3Qgc3RlcE5hbWVQYXJhbWV0ZXJzID0gY3VjdW1iZXJFeHByZXNzaW9uXG4gICAgICAubWF0Y2goc3RlcC50ZXh0KVxuICAgICAgLm1hcChhcmcgPT4gYXJnLmdldFZhbHVlKHdvcmxkKSlcbiAgICBjb25zdCBpdGVyYXRvciA9IGJ1aWxkU3RlcEFyZ3VtZW50SXRlcmF0b3Ioe1xuICAgICAgZGF0YVRhYmxlOiBhcmcgPT4gbmV3IERhdGFUYWJsZShhcmcpLFxuICAgICAgZG9jU3RyaW5nOiBhcmcgPT4gYXJnLmNvbnRlbnRcbiAgICB9KVxuICAgIGNvbnN0IHN0ZXBBcmd1bWVudFBhcmFtZXRlcnMgPSBzdGVwLmFyZ3VtZW50cy5tYXAoaXRlcmF0b3IpXG4gICAgcmV0dXJuIHN0ZXBOYW1lUGFyYW1ldGVycy5jb25jYXQoc3RlcEFyZ3VtZW50UGFyYW1ldGVycylcbiAgfVxuXG4gIGdldEN1Y3VtYmVyRXhwcmVzc2lvbihwYXJhbWV0ZXJUeXBlUmVnaXN0cnkpIHtcbiAgICBpZiAodHlwZW9mIHRoaXMucGF0dGVybiA9PT0gJ3N0cmluZycpIHtcbiAgICAgIHJldHVybiBuZXcgQ3VjdW1iZXJFeHByZXNzaW9uKHRoaXMucGF0dGVybiwgcGFyYW1ldGVyVHlwZVJlZ2lzdHJ5KVxuICAgIH0gZWxzZSB7XG4gICAgICByZXR1cm4gbmV3IFJlZ3VsYXJFeHByZXNzaW9uKHRoaXMucGF0dGVybiwgcGFyYW1ldGVyVHlwZVJlZ2lzdHJ5KVxuICAgIH1cbiAgfVxuXG4gIGdldFZhbGlkQ29kZUxlbmd0aHMocGFyYW1ldGVycykge1xuICAgIHJldHVybiBbcGFyYW1ldGVycy5sZW5ndGgsIHBhcmFtZXRlcnMubGVuZ3RoICsgMV1cbiAgfVxuXG4gIG1hdGNoZXNTdGVwTmFtZSh7IHN0ZXBOYW1lLCBwYXJhbWV0ZXJUeXBlUmVnaXN0cnkgfSkge1xuICAgIGNvbnN0IGN1Y3VtYmVyRXhwcmVzc2lvbiA9IHRoaXMuZ2V0Q3VjdW1iZXJFeHByZXNzaW9uKHBhcmFtZXRlclR5cGVSZWdpc3RyeSlcbiAgICByZXR1cm4gQm9vbGVhbihjdWN1bWJlckV4cHJlc3Npb24ubWF0Y2goc3RlcE5hbWUpKVxuICB9XG59XG4iXX0=