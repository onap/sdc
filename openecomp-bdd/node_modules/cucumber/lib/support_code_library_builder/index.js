'use strict';

Object.defineProperty(exports, "__esModule", {
  value: true
});
exports.SupportCodeLibraryBuilder = undefined;

var _classCallCheck2 = require('babel-runtime/helpers/classCallCheck');

var _classCallCheck3 = _interopRequireDefault(_classCallCheck2);

var _createClass2 = require('babel-runtime/helpers/createClass');

var _createClass3 = _interopRequireDefault(_createClass2);

var _lodash = require('lodash');

var _lodash2 = _interopRequireDefault(_lodash);

var _parameter_type_registry_builder = require('./parameter_type_registry_builder');

var _parameter_type_registry_builder2 = _interopRequireDefault(_parameter_type_registry_builder);

var _define_helpers = require('./define_helpers');

var _finalize_helpers = require('./finalize_helpers');

function _interopRequireDefault(obj) { return obj && obj.__esModule ? obj : { default: obj }; }

var SupportCodeLibraryBuilder = exports.SupportCodeLibraryBuilder = function () {
  function SupportCodeLibraryBuilder() {
    var _this = this;

    (0, _classCallCheck3.default)(this, SupportCodeLibraryBuilder);

    this.methods = {
      defineParameterType: (0, _define_helpers.defineParameterType)(this),
      After: (0, _define_helpers.defineTestCaseHook)(this, 'afterTestCaseHookDefinitions'),
      AfterAll: (0, _define_helpers.defineTestRunHook)(this, 'afterTestRunHookDefinitions'),
      Before: (0, _define_helpers.defineTestCaseHook)(this, 'beforeTestCaseHookDefinitions'),
      BeforeAll: (0, _define_helpers.defineTestRunHook)(this, 'beforeTestRunHookDefinitions'),
      defineSupportCode: function defineSupportCode(fn) {
        fn(_this.methods);
      },
      defineStep: (0, _define_helpers.defineStep)(this),
      setDefaultTimeout: function setDefaultTimeout(milliseconds) {
        _this.options.defaultTimeout = milliseconds;
      },
      setDefinitionFunctionWrapper: function setDefinitionFunctionWrapper(fn) {
        _this.options.definitionFunctionWrapper = fn;
      },
      setWorldConstructor: function setWorldConstructor(fn) {
        _this.options.World = fn;
      }
    };
    this.methods.Given = this.methods.When = this.methods.Then = this.methods.defineStep;
  }

  (0, _createClass3.default)(SupportCodeLibraryBuilder, [{
    key: 'finalize',
    value: function finalize() {
      var _this2 = this;

      (0, _finalize_helpers.wrapDefinitions)({
        cwd: this.cwd,
        definitionFunctionWrapper: this.options.definitionFunctionWrapper,
        definitions: _lodash2.default.chain(['afterTestCaseHook', 'afterTestRunHook', 'beforeTestCaseHook', 'beforeTestRunHook', 'step']).map(function (key) {
          return _this2.options[key + 'Definitions'];
        }).flatten().value()
      });
      this.options.afterTestCaseHookDefinitions.reverse();
      this.options.afterTestRunHookDefinitions.reverse();
      return this.options;
    }
  }, {
    key: 'reset',
    value: function reset(cwd) {
      this.cwd = cwd;
      this.options = _lodash2.default.cloneDeep({
        afterTestCaseHookDefinitions: [],
        afterTestRunHookDefinitions: [],
        beforeTestCaseHookDefinitions: [],
        beforeTestRunHookDefinitions: [],
        defaultTimeout: 5000,
        definitionFunctionWrapper: null,
        stepDefinitions: [],
        parameterTypeRegistry: _parameter_type_registry_builder2.default.build(),
        World: function World(_ref) {
          var attach = _ref.attach,
              parameters = _ref.parameters;

          this.attach = attach;
          this.parameters = parameters;
        }
      });
    }
  }]);
  return SupportCodeLibraryBuilder;
}();

exports.default = new SupportCodeLibraryBuilder();
//# sourceMappingURL=data:application/json;charset=utf-8;base64,eyJ2ZXJzaW9uIjozLCJzb3VyY2VzIjpbIi4uLy4uL3NyYy9zdXBwb3J0X2NvZGVfbGlicmFyeV9idWlsZGVyL2luZGV4LmpzIl0sIm5hbWVzIjpbIlN1cHBvcnRDb2RlTGlicmFyeUJ1aWxkZXIiLCJtZXRob2RzIiwiZGVmaW5lUGFyYW1ldGVyVHlwZSIsIkFmdGVyIiwiQWZ0ZXJBbGwiLCJCZWZvcmUiLCJCZWZvcmVBbGwiLCJkZWZpbmVTdXBwb3J0Q29kZSIsImZuIiwiZGVmaW5lU3RlcCIsInNldERlZmF1bHRUaW1lb3V0Iiwib3B0aW9ucyIsImRlZmF1bHRUaW1lb3V0IiwibWlsbGlzZWNvbmRzIiwic2V0RGVmaW5pdGlvbkZ1bmN0aW9uV3JhcHBlciIsImRlZmluaXRpb25GdW5jdGlvbldyYXBwZXIiLCJzZXRXb3JsZENvbnN0cnVjdG9yIiwiV29ybGQiLCJHaXZlbiIsIldoZW4iLCJUaGVuIiwiY3dkIiwiZGVmaW5pdGlvbnMiLCJjaGFpbiIsIm1hcCIsImtleSIsImZsYXR0ZW4iLCJ2YWx1ZSIsImFmdGVyVGVzdENhc2VIb29rRGVmaW5pdGlvbnMiLCJyZXZlcnNlIiwiYWZ0ZXJUZXN0UnVuSG9va0RlZmluaXRpb25zIiwiY2xvbmVEZWVwIiwiYmVmb3JlVGVzdENhc2VIb29rRGVmaW5pdGlvbnMiLCJiZWZvcmVUZXN0UnVuSG9va0RlZmluaXRpb25zIiwic3RlcERlZmluaXRpb25zIiwicGFyYW1ldGVyVHlwZVJlZ2lzdHJ5IiwiYnVpbGQiLCJhdHRhY2giLCJwYXJhbWV0ZXJzIl0sIm1hcHBpbmdzIjoiOzs7Ozs7Ozs7Ozs7Ozs7QUFBQTs7OztBQUNBOzs7O0FBQ0E7O0FBTUE7Ozs7SUFFYUEseUIsV0FBQUEseUI7QUFDWCx1Q0FBYztBQUFBOztBQUFBOztBQUNaLFNBQUtDLE9BQUwsR0FBZTtBQUNiQywyQkFBcUIseUNBQW9CLElBQXBCLENBRFI7QUFFYkMsYUFBTyx3Q0FBbUIsSUFBbkIsRUFBeUIsOEJBQXpCLENBRk07QUFHYkMsZ0JBQVUsdUNBQWtCLElBQWxCLEVBQXdCLDZCQUF4QixDQUhHO0FBSWJDLGNBQVEsd0NBQW1CLElBQW5CLEVBQXlCLCtCQUF6QixDQUpLO0FBS2JDLGlCQUFXLHVDQUFrQixJQUFsQixFQUF3Qiw4QkFBeEIsQ0FMRTtBQU1iQyx5QkFBbUIsK0JBQU07QUFDdkJDLFdBQUcsTUFBS1AsT0FBUjtBQUNELE9BUlk7QUFTYlEsa0JBQVksZ0NBQVcsSUFBWCxDQVRDO0FBVWJDLHlCQUFtQix5Q0FBZ0I7QUFDakMsY0FBS0MsT0FBTCxDQUFhQyxjQUFiLEdBQThCQyxZQUE5QjtBQUNELE9BWlk7QUFhYkMsb0NBQThCLDBDQUFNO0FBQ2xDLGNBQUtILE9BQUwsQ0FBYUkseUJBQWIsR0FBeUNQLEVBQXpDO0FBQ0QsT0FmWTtBQWdCYlEsMkJBQXFCLGlDQUFNO0FBQ3pCLGNBQUtMLE9BQUwsQ0FBYU0sS0FBYixHQUFxQlQsRUFBckI7QUFDRDtBQWxCWSxLQUFmO0FBb0JBLFNBQUtQLE9BQUwsQ0FBYWlCLEtBQWIsR0FBcUIsS0FBS2pCLE9BQUwsQ0FBYWtCLElBQWIsR0FBb0IsS0FBS2xCLE9BQUwsQ0FBYW1CLElBQWIsR0FBb0IsS0FBS25CLE9BQUwsQ0FBYVEsVUFBMUU7QUFDRDs7OzsrQkFFVTtBQUFBOztBQUNULDZDQUFnQjtBQUNkWSxhQUFLLEtBQUtBLEdBREk7QUFFZE4sbUNBQTJCLEtBQUtKLE9BQUwsQ0FBYUkseUJBRjFCO0FBR2RPLHFCQUFhLGlCQUFFQyxLQUFGLENBQVEsQ0FDbkIsbUJBRG1CLEVBRW5CLGtCQUZtQixFQUduQixvQkFIbUIsRUFJbkIsbUJBSm1CLEVBS25CLE1BTG1CLENBQVIsRUFPVkMsR0FQVSxDQU9OO0FBQUEsaUJBQU8sT0FBS2IsT0FBTCxDQUFhYyxNQUFNLGFBQW5CLENBQVA7QUFBQSxTQVBNLEVBUVZDLE9BUlUsR0FTVkMsS0FUVTtBQUhDLE9BQWhCO0FBY0EsV0FBS2hCLE9BQUwsQ0FBYWlCLDRCQUFiLENBQTBDQyxPQUExQztBQUNBLFdBQUtsQixPQUFMLENBQWFtQiwyQkFBYixDQUF5Q0QsT0FBekM7QUFDQSxhQUFPLEtBQUtsQixPQUFaO0FBQ0Q7OzswQkFFS1UsRyxFQUFLO0FBQ1QsV0FBS0EsR0FBTCxHQUFXQSxHQUFYO0FBQ0EsV0FBS1YsT0FBTCxHQUFlLGlCQUFFb0IsU0FBRixDQUFZO0FBQ3pCSCxzQ0FBOEIsRUFETDtBQUV6QkUscUNBQTZCLEVBRko7QUFHekJFLHVDQUErQixFQUhOO0FBSXpCQyxzQ0FBOEIsRUFKTDtBQUt6QnJCLHdCQUFnQixJQUxTO0FBTXpCRyxtQ0FBMkIsSUFORjtBQU96Qm1CLHlCQUFpQixFQVBRO0FBUXpCQywrQkFBdUIsMENBQXVCQyxLQUF2QixFQVJFO0FBU3pCbkIsYUFUeUIsdUJBU0s7QUFBQSxjQUF0Qm9CLE1BQXNCLFFBQXRCQSxNQUFzQjtBQUFBLGNBQWRDLFVBQWMsUUFBZEEsVUFBYzs7QUFDNUIsZUFBS0QsTUFBTCxHQUFjQSxNQUFkO0FBQ0EsZUFBS0MsVUFBTCxHQUFrQkEsVUFBbEI7QUFDRDtBQVp3QixPQUFaLENBQWY7QUFjRDs7Ozs7a0JBR1ksSUFBSXRDLHlCQUFKLEUiLCJmaWxlIjoiaW5kZXguanMiLCJzb3VyY2VzQ29udGVudCI6WyJpbXBvcnQgXyBmcm9tICdsb2Rhc2gnXG5pbXBvcnQgVHJhbnNmb3JtTG9va3VwQnVpbGRlciBmcm9tICcuL3BhcmFtZXRlcl90eXBlX3JlZ2lzdHJ5X2J1aWxkZXInXG5pbXBvcnQge1xuICBkZWZpbmVUZXN0UnVuSG9vayxcbiAgZGVmaW5lUGFyYW1ldGVyVHlwZSxcbiAgZGVmaW5lVGVzdENhc2VIb29rLFxuICBkZWZpbmVTdGVwXG59IGZyb20gJy4vZGVmaW5lX2hlbHBlcnMnXG5pbXBvcnQgeyB3cmFwRGVmaW5pdGlvbnMgfSBmcm9tICcuL2ZpbmFsaXplX2hlbHBlcnMnXG5cbmV4cG9ydCBjbGFzcyBTdXBwb3J0Q29kZUxpYnJhcnlCdWlsZGVyIHtcbiAgY29uc3RydWN0b3IoKSB7XG4gICAgdGhpcy5tZXRob2RzID0ge1xuICAgICAgZGVmaW5lUGFyYW1ldGVyVHlwZTogZGVmaW5lUGFyYW1ldGVyVHlwZSh0aGlzKSxcbiAgICAgIEFmdGVyOiBkZWZpbmVUZXN0Q2FzZUhvb2sodGhpcywgJ2FmdGVyVGVzdENhc2VIb29rRGVmaW5pdGlvbnMnKSxcbiAgICAgIEFmdGVyQWxsOiBkZWZpbmVUZXN0UnVuSG9vayh0aGlzLCAnYWZ0ZXJUZXN0UnVuSG9va0RlZmluaXRpb25zJyksXG4gICAgICBCZWZvcmU6IGRlZmluZVRlc3RDYXNlSG9vayh0aGlzLCAnYmVmb3JlVGVzdENhc2VIb29rRGVmaW5pdGlvbnMnKSxcbiAgICAgIEJlZm9yZUFsbDogZGVmaW5lVGVzdFJ1bkhvb2sodGhpcywgJ2JlZm9yZVRlc3RSdW5Ib29rRGVmaW5pdGlvbnMnKSxcbiAgICAgIGRlZmluZVN1cHBvcnRDb2RlOiBmbiA9PiB7XG4gICAgICAgIGZuKHRoaXMubWV0aG9kcylcbiAgICAgIH0sXG4gICAgICBkZWZpbmVTdGVwOiBkZWZpbmVTdGVwKHRoaXMpLFxuICAgICAgc2V0RGVmYXVsdFRpbWVvdXQ6IG1pbGxpc2Vjb25kcyA9PiB7XG4gICAgICAgIHRoaXMub3B0aW9ucy5kZWZhdWx0VGltZW91dCA9IG1pbGxpc2Vjb25kc1xuICAgICAgfSxcbiAgICAgIHNldERlZmluaXRpb25GdW5jdGlvbldyYXBwZXI6IGZuID0+IHtcbiAgICAgICAgdGhpcy5vcHRpb25zLmRlZmluaXRpb25GdW5jdGlvbldyYXBwZXIgPSBmblxuICAgICAgfSxcbiAgICAgIHNldFdvcmxkQ29uc3RydWN0b3I6IGZuID0+IHtcbiAgICAgICAgdGhpcy5vcHRpb25zLldvcmxkID0gZm5cbiAgICAgIH1cbiAgICB9XG4gICAgdGhpcy5tZXRob2RzLkdpdmVuID0gdGhpcy5tZXRob2RzLldoZW4gPSB0aGlzLm1ldGhvZHMuVGhlbiA9IHRoaXMubWV0aG9kcy5kZWZpbmVTdGVwXG4gIH1cblxuICBmaW5hbGl6ZSgpIHtcbiAgICB3cmFwRGVmaW5pdGlvbnMoe1xuICAgICAgY3dkOiB0aGlzLmN3ZCxcbiAgICAgIGRlZmluaXRpb25GdW5jdGlvbldyYXBwZXI6IHRoaXMub3B0aW9ucy5kZWZpbml0aW9uRnVuY3Rpb25XcmFwcGVyLFxuICAgICAgZGVmaW5pdGlvbnM6IF8uY2hhaW4oW1xuICAgICAgICAnYWZ0ZXJUZXN0Q2FzZUhvb2snLFxuICAgICAgICAnYWZ0ZXJUZXN0UnVuSG9vaycsXG4gICAgICAgICdiZWZvcmVUZXN0Q2FzZUhvb2snLFxuICAgICAgICAnYmVmb3JlVGVzdFJ1bkhvb2snLFxuICAgICAgICAnc3RlcCdcbiAgICAgIF0pXG4gICAgICAgIC5tYXAoa2V5ID0+IHRoaXMub3B0aW9uc1trZXkgKyAnRGVmaW5pdGlvbnMnXSlcbiAgICAgICAgLmZsYXR0ZW4oKVxuICAgICAgICAudmFsdWUoKVxuICAgIH0pXG4gICAgdGhpcy5vcHRpb25zLmFmdGVyVGVzdENhc2VIb29rRGVmaW5pdGlvbnMucmV2ZXJzZSgpXG4gICAgdGhpcy5vcHRpb25zLmFmdGVyVGVzdFJ1bkhvb2tEZWZpbml0aW9ucy5yZXZlcnNlKClcbiAgICByZXR1cm4gdGhpcy5vcHRpb25zXG4gIH1cblxuICByZXNldChjd2QpIHtcbiAgICB0aGlzLmN3ZCA9IGN3ZFxuICAgIHRoaXMub3B0aW9ucyA9IF8uY2xvbmVEZWVwKHtcbiAgICAgIGFmdGVyVGVzdENhc2VIb29rRGVmaW5pdGlvbnM6IFtdLFxuICAgICAgYWZ0ZXJUZXN0UnVuSG9va0RlZmluaXRpb25zOiBbXSxcbiAgICAgIGJlZm9yZVRlc3RDYXNlSG9va0RlZmluaXRpb25zOiBbXSxcbiAgICAgIGJlZm9yZVRlc3RSdW5Ib29rRGVmaW5pdGlvbnM6IFtdLFxuICAgICAgZGVmYXVsdFRpbWVvdXQ6IDUwMDAsXG4gICAgICBkZWZpbml0aW9uRnVuY3Rpb25XcmFwcGVyOiBudWxsLFxuICAgICAgc3RlcERlZmluaXRpb25zOiBbXSxcbiAgICAgIHBhcmFtZXRlclR5cGVSZWdpc3RyeTogVHJhbnNmb3JtTG9va3VwQnVpbGRlci5idWlsZCgpLFxuICAgICAgV29ybGQoeyBhdHRhY2gsIHBhcmFtZXRlcnMgfSkge1xuICAgICAgICB0aGlzLmF0dGFjaCA9IGF0dGFjaFxuICAgICAgICB0aGlzLnBhcmFtZXRlcnMgPSBwYXJhbWV0ZXJzXG4gICAgICB9XG4gICAgfSlcbiAgfVxufVxuXG5leHBvcnQgZGVmYXVsdCBuZXcgU3VwcG9ydENvZGVMaWJyYXJ5QnVpbGRlcigpXG4iXX0=