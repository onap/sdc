'use strict';

Object.defineProperty(exports, "__esModule", {
  value: true
});

var _extends2 = require('babel-runtime/helpers/extends');

var _extends3 = _interopRequireDefault(_extends2);

var _classCallCheck2 = require('babel-runtime/helpers/classCallCheck');

var _classCallCheck3 = _interopRequireDefault(_classCallCheck2);

var _createClass2 = require('babel-runtime/helpers/createClass');

var _createClass3 = _interopRequireDefault(_createClass2);

var _possibleConstructorReturn2 = require('babel-runtime/helpers/possibleConstructorReturn');

var _possibleConstructorReturn3 = _interopRequireDefault(_possibleConstructorReturn2);

var _inherits2 = require('babel-runtime/helpers/inherits');

var _inherits3 = _interopRequireDefault(_inherits2);

var _escapeStringRegexp = require('escape-string-regexp');

var _escapeStringRegexp2 = _interopRequireDefault(_escapeStringRegexp);

var _ = require('./');

var _2 = _interopRequireDefault(_);

var _path = require('path');

var _path2 = _interopRequireDefault(_path);

function _interopRequireDefault(obj) { return obj && obj.__esModule ? obj : { default: obj }; }

var EVENT_NAMES = ['source', 'attachment', 'gherkin-document', 'pickle', 'pickle-accepted', 'pickle-rejected', 'test-run-started', 'test-case-prepared', 'test-case-started', 'test-step-started', 'test-step-attachment', 'test-step-finished', 'test-case-finished', 'test-run-finished'];

var EventProtocolFormatter = function (_Formatter) {
  (0, _inherits3.default)(EventProtocolFormatter, _Formatter);

  function EventProtocolFormatter(options) {
    (0, _classCallCheck3.default)(this, EventProtocolFormatter);

    var _this = (0, _possibleConstructorReturn3.default)(this, (EventProtocolFormatter.__proto__ || Object.getPrototypeOf(EventProtocolFormatter)).call(this, options));

    EVENT_NAMES.forEach(function (eventName) {
      options.eventBroadcaster.on(eventName, function (data) {
        return _this.logEvent(eventName, data);
      });
    });

    var pathSepRegexp = new RegExp((0, _escapeStringRegexp2.default)(_path2.default.sep), 'g');
    var pathToRemove = _this.cwd.replace(pathSepRegexp, _path2.default.posix.sep) + _path2.default.posix.sep;
    _this.pathRegexp = new RegExp((0, _escapeStringRegexp2.default)(pathToRemove), 'g');
    return _this;
  }

  (0, _createClass3.default)(EventProtocolFormatter, [{
    key: 'logEvent',
    value: function logEvent(eventName, data) {
      var text = JSON.stringify((0, _extends3.default)({ type: eventName }, data), this.formatJsonData.bind(this));
      this.log(text + '\n');
    }
  }, {
    key: 'formatJsonData',
    value: function formatJsonData(key, value) {
      if (value instanceof Error) {
        return value.stack.replace(this.pathRegexp, '');
      } else {
        return value;
      }
    }
  }]);
  return EventProtocolFormatter;
}(_2.default);

exports.default = EventProtocolFormatter;
//# sourceMappingURL=data:application/json;charset=utf-8;base64,eyJ2ZXJzaW9uIjozLCJzb3VyY2VzIjpbIi4uLy4uL3NyYy9mb3JtYXR0ZXIvZXZlbnRfcHJvdG9jb2xfZm9ybWF0dGVyLmpzIl0sIm5hbWVzIjpbIkVWRU5UX05BTUVTIiwiRXZlbnRQcm90b2NvbEZvcm1hdHRlciIsIm9wdGlvbnMiLCJmb3JFYWNoIiwiZXZlbnRCcm9hZGNhc3RlciIsIm9uIiwiZXZlbnROYW1lIiwibG9nRXZlbnQiLCJkYXRhIiwicGF0aFNlcFJlZ2V4cCIsIlJlZ0V4cCIsInNlcCIsInBhdGhUb1JlbW92ZSIsImN3ZCIsInJlcGxhY2UiLCJwb3NpeCIsInBhdGhSZWdleHAiLCJ0ZXh0IiwiSlNPTiIsInN0cmluZ2lmeSIsInR5cGUiLCJmb3JtYXRKc29uRGF0YSIsImxvZyIsImtleSIsInZhbHVlIiwiRXJyb3IiLCJzdGFjayJdLCJtYXBwaW5ncyI6Ijs7Ozs7Ozs7Ozs7Ozs7Ozs7Ozs7Ozs7Ozs7QUFBQTs7OztBQUNBOzs7O0FBQ0E7Ozs7OztBQUVBLElBQU1BLGNBQWMsQ0FDbEIsUUFEa0IsRUFFbEIsWUFGa0IsRUFHbEIsa0JBSGtCLEVBSWxCLFFBSmtCLEVBS2xCLGlCQUxrQixFQU1sQixpQkFOa0IsRUFPbEIsa0JBUGtCLEVBUWxCLG9CQVJrQixFQVNsQixtQkFUa0IsRUFVbEIsbUJBVmtCLEVBV2xCLHNCQVhrQixFQVlsQixvQkFaa0IsRUFhbEIsb0JBYmtCLEVBY2xCLG1CQWRrQixDQUFwQjs7SUFpQnFCQyxzQjs7O0FBQ25CLGtDQUFZQyxPQUFaLEVBQXFCO0FBQUE7O0FBQUEsOEpBQ2JBLE9BRGE7O0FBRW5CRixnQkFBWUcsT0FBWixDQUFvQixxQkFBYTtBQUMvQkQsY0FBUUUsZ0JBQVIsQ0FBeUJDLEVBQXpCLENBQTRCQyxTQUE1QixFQUF1QztBQUFBLGVBQ3JDLE1BQUtDLFFBQUwsQ0FBY0QsU0FBZCxFQUF5QkUsSUFBekIsQ0FEcUM7QUFBQSxPQUF2QztBQUdELEtBSkQ7O0FBTUEsUUFBTUMsZ0JBQWdCLElBQUlDLE1BQUosQ0FBVyxrQ0FBbUIsZUFBS0MsR0FBeEIsQ0FBWCxFQUF5QyxHQUF6QyxDQUF0QjtBQUNBLFFBQU1DLGVBQ0osTUFBS0MsR0FBTCxDQUFTQyxPQUFULENBQWlCTCxhQUFqQixFQUFnQyxlQUFLTSxLQUFMLENBQVdKLEdBQTNDLElBQWtELGVBQUtJLEtBQUwsQ0FBV0osR0FEL0Q7QUFFQSxVQUFLSyxVQUFMLEdBQWtCLElBQUlOLE1BQUosQ0FBVyxrQ0FBbUJFLFlBQW5CLENBQVgsRUFBNkMsR0FBN0MsQ0FBbEI7QUFYbUI7QUFZcEI7Ozs7NkJBRVFOLFMsRUFBV0UsSSxFQUFNO0FBQ3hCLFVBQU1TLE9BQU9DLEtBQUtDLFNBQUwsMEJBQ1RDLE1BQU1kLFNBREcsSUFDV0UsSUFEWCxHQUVULEtBQUthLGNBRkksTUFFVCxJQUZTLEVBQWI7QUFJQSxXQUFLQyxHQUFMLENBQVNMLE9BQU8sSUFBaEI7QUFDRDs7O21DQUVjTSxHLEVBQUtDLEssRUFBTztBQUN6QixVQUFJQSxpQkFBaUJDLEtBQXJCLEVBQTRCO0FBQzFCLGVBQU9ELE1BQU1FLEtBQU4sQ0FBWVosT0FBWixDQUFvQixLQUFLRSxVQUF6QixFQUFxQyxFQUFyQyxDQUFQO0FBQ0QsT0FGRCxNQUVPO0FBQ0wsZUFBT1EsS0FBUDtBQUNEO0FBQ0Y7Ozs7O2tCQTdCa0J2QixzQiIsImZpbGUiOiJldmVudF9wcm90b2NvbF9mb3JtYXR0ZXIuanMiLCJzb3VyY2VzQ29udGVudCI6WyJpbXBvcnQgZXNjYXBlU3RyaW5nUmVnZXhwIGZyb20gJ2VzY2FwZS1zdHJpbmctcmVnZXhwJ1xuaW1wb3J0IEZvcm1hdHRlciBmcm9tICcuLydcbmltcG9ydCBwYXRoIGZyb20gJ3BhdGgnXG5cbmNvbnN0IEVWRU5UX05BTUVTID0gW1xuICAnc291cmNlJyxcbiAgJ2F0dGFjaG1lbnQnLFxuICAnZ2hlcmtpbi1kb2N1bWVudCcsXG4gICdwaWNrbGUnLFxuICAncGlja2xlLWFjY2VwdGVkJyxcbiAgJ3BpY2tsZS1yZWplY3RlZCcsXG4gICd0ZXN0LXJ1bi1zdGFydGVkJyxcbiAgJ3Rlc3QtY2FzZS1wcmVwYXJlZCcsXG4gICd0ZXN0LWNhc2Utc3RhcnRlZCcsXG4gICd0ZXN0LXN0ZXAtc3RhcnRlZCcsXG4gICd0ZXN0LXN0ZXAtYXR0YWNobWVudCcsXG4gICd0ZXN0LXN0ZXAtZmluaXNoZWQnLFxuICAndGVzdC1jYXNlLWZpbmlzaGVkJyxcbiAgJ3Rlc3QtcnVuLWZpbmlzaGVkJ1xuXVxuXG5leHBvcnQgZGVmYXVsdCBjbGFzcyBFdmVudFByb3RvY29sRm9ybWF0dGVyIGV4dGVuZHMgRm9ybWF0dGVyIHtcbiAgY29uc3RydWN0b3Iob3B0aW9ucykge1xuICAgIHN1cGVyKG9wdGlvbnMpXG4gICAgRVZFTlRfTkFNRVMuZm9yRWFjaChldmVudE5hbWUgPT4ge1xuICAgICAgb3B0aW9ucy5ldmVudEJyb2FkY2FzdGVyLm9uKGV2ZW50TmFtZSwgZGF0YSA9PlxuICAgICAgICB0aGlzLmxvZ0V2ZW50KGV2ZW50TmFtZSwgZGF0YSlcbiAgICAgIClcbiAgICB9KVxuXG4gICAgY29uc3QgcGF0aFNlcFJlZ2V4cCA9IG5ldyBSZWdFeHAoZXNjYXBlU3RyaW5nUmVnZXhwKHBhdGguc2VwKSwgJ2cnKVxuICAgIGNvbnN0IHBhdGhUb1JlbW92ZSA9XG4gICAgICB0aGlzLmN3ZC5yZXBsYWNlKHBhdGhTZXBSZWdleHAsIHBhdGgucG9zaXguc2VwKSArIHBhdGgucG9zaXguc2VwXG4gICAgdGhpcy5wYXRoUmVnZXhwID0gbmV3IFJlZ0V4cChlc2NhcGVTdHJpbmdSZWdleHAocGF0aFRvUmVtb3ZlKSwgJ2cnKVxuICB9XG5cbiAgbG9nRXZlbnQoZXZlbnROYW1lLCBkYXRhKSB7XG4gICAgY29uc3QgdGV4dCA9IEpTT04uc3RyaW5naWZ5KFxuICAgICAgeyB0eXBlOiBldmVudE5hbWUsIC4uLmRhdGEgfSxcbiAgICAgIDo6dGhpcy5mb3JtYXRKc29uRGF0YVxuICAgIClcbiAgICB0aGlzLmxvZyh0ZXh0ICsgJ1xcbicpXG4gIH1cblxuICBmb3JtYXRKc29uRGF0YShrZXksIHZhbHVlKSB7XG4gICAgaWYgKHZhbHVlIGluc3RhbmNlb2YgRXJyb3IpIHtcbiAgICAgIHJldHVybiB2YWx1ZS5zdGFjay5yZXBsYWNlKHRoaXMucGF0aFJlZ2V4cCwgJycpXG4gICAgfSBlbHNlIHtcbiAgICAgIHJldHVybiB2YWx1ZVxuICAgIH1cbiAgfVxufVxuIl19