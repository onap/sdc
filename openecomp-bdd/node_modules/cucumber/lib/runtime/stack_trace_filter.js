'use strict';

Object.defineProperty(exports, "__esModule", {
  value: true
});

var _classCallCheck2 = require('babel-runtime/helpers/classCallCheck');

var _classCallCheck3 = _interopRequireDefault(_classCallCheck2);

var _createClass2 = require('babel-runtime/helpers/createClass');

var _createClass3 = _interopRequireDefault(_createClass2);

var _lodash = require('lodash');

var _lodash2 = _interopRequireDefault(_lodash);

var _stackChain = require('stack-chain');

var _stackChain2 = _interopRequireDefault(_stackChain);

var _path = require('path');

var _path2 = _interopRequireDefault(_path);

function _interopRequireDefault(obj) { return obj && obj.__esModule ? obj : { default: obj }; }

var StackTraceFilter = function () {
  function StackTraceFilter() {
    (0, _classCallCheck3.default)(this, StackTraceFilter);

    this.cucumberPath = _path2.default.join(__dirname, '..', '..');
  }

  (0, _createClass3.default)(StackTraceFilter, [{
    key: 'filter',
    value: function filter() {
      var _this = this;

      this.currentFilter = _stackChain2.default.filter.attach(function (error, frames) {
        if (_this.isErrorInCucumber(frames)) {
          return frames;
        }
        var index = _lodash2.default.findIndex(frames, _this.isFrameInCucumber.bind(_this));
        if (index === -1) {
          return frames;
        } else {
          return frames.slice(0, index);
        }
      });
    }
  }, {
    key: 'isErrorInCucumber',
    value: function isErrorInCucumber(frames) {
      var filteredFrames = _lodash2.default.reject(frames, this.isFrameInNode.bind(this));
      return filteredFrames.length > 0 && this.isFrameInCucumber(filteredFrames[0]);
    }
  }, {
    key: 'isFrameInCucumber',
    value: function isFrameInCucumber(frame) {
      var fileName = frame.getFileName() || '';
      return _lodash2.default.startsWith(fileName, this.cucumberPath);
    }
  }, {
    key: 'isFrameInNode',
    value: function isFrameInNode(frame) {
      var fileName = frame.getFileName() || '';
      return !_lodash2.default.includes(fileName, _path2.default.sep);
    }
  }, {
    key: 'unfilter',
    value: function unfilter() {
      _stackChain2.default.filter.deattach(this.currentFilter);
    }
  }]);
  return StackTraceFilter;
}();

exports.default = StackTraceFilter;
//# sourceMappingURL=data:application/json;charset=utf-8;base64,eyJ2ZXJzaW9uIjozLCJzb3VyY2VzIjpbIi4uLy4uL3NyYy9ydW50aW1lL3N0YWNrX3RyYWNlX2ZpbHRlci5qcyJdLCJuYW1lcyI6WyJTdGFja1RyYWNlRmlsdGVyIiwiY3VjdW1iZXJQYXRoIiwiam9pbiIsIl9fZGlybmFtZSIsImN1cnJlbnRGaWx0ZXIiLCJmaWx0ZXIiLCJhdHRhY2giLCJlcnJvciIsImZyYW1lcyIsImlzRXJyb3JJbkN1Y3VtYmVyIiwiaW5kZXgiLCJmaW5kSW5kZXgiLCJpc0ZyYW1lSW5DdWN1bWJlciIsInNsaWNlIiwiZmlsdGVyZWRGcmFtZXMiLCJyZWplY3QiLCJpc0ZyYW1lSW5Ob2RlIiwibGVuZ3RoIiwiZnJhbWUiLCJmaWxlTmFtZSIsImdldEZpbGVOYW1lIiwic3RhcnRzV2l0aCIsImluY2x1ZGVzIiwic2VwIiwiZGVhdHRhY2giXSwibWFwcGluZ3MiOiI7Ozs7Ozs7Ozs7Ozs7O0FBQUE7Ozs7QUFDQTs7OztBQUNBOzs7Ozs7SUFFcUJBLGdCO0FBQ25CLDhCQUFjO0FBQUE7O0FBQ1osU0FBS0MsWUFBTCxHQUFvQixlQUFLQyxJQUFMLENBQVVDLFNBQVYsRUFBcUIsSUFBckIsRUFBMkIsSUFBM0IsQ0FBcEI7QUFDRDs7Ozs2QkFFUTtBQUFBOztBQUNQLFdBQUtDLGFBQUwsR0FBcUIscUJBQVdDLE1BQVgsQ0FBa0JDLE1BQWxCLENBQXlCLFVBQUNDLEtBQUQsRUFBUUMsTUFBUixFQUFtQjtBQUMvRCxZQUFJLE1BQUtDLGlCQUFMLENBQXVCRCxNQUF2QixDQUFKLEVBQW9DO0FBQ2xDLGlCQUFPQSxNQUFQO0FBQ0Q7QUFDRCxZQUFNRSxRQUFRLGlCQUFFQyxTQUFGLENBQVlILE1BQVosRUFBc0IsTUFBS0ksaUJBQTNCLGFBQWQ7QUFDQSxZQUFJRixVQUFVLENBQUMsQ0FBZixFQUFrQjtBQUNoQixpQkFBT0YsTUFBUDtBQUNELFNBRkQsTUFFTztBQUNMLGlCQUFPQSxPQUFPSyxLQUFQLENBQWEsQ0FBYixFQUFnQkgsS0FBaEIsQ0FBUDtBQUNEO0FBQ0YsT0FWb0IsQ0FBckI7QUFXRDs7O3NDQUVpQkYsTSxFQUFRO0FBQ3hCLFVBQU1NLGlCQUFpQixpQkFBRUMsTUFBRixDQUFTUCxNQUFULEVBQW1CLEtBQUtRLGFBQXhCLE1BQW1CLElBQW5CLEVBQXZCO0FBQ0EsYUFDRUYsZUFBZUcsTUFBZixHQUF3QixDQUF4QixJQUE2QixLQUFLTCxpQkFBTCxDQUF1QkUsZUFBZSxDQUFmLENBQXZCLENBRC9CO0FBR0Q7OztzQ0FFaUJJLEssRUFBTztBQUN2QixVQUFNQyxXQUFXRCxNQUFNRSxXQUFOLE1BQXVCLEVBQXhDO0FBQ0EsYUFBTyxpQkFBRUMsVUFBRixDQUFhRixRQUFiLEVBQXVCLEtBQUtsQixZQUE1QixDQUFQO0FBQ0Q7OztrQ0FFYWlCLEssRUFBTztBQUNuQixVQUFNQyxXQUFXRCxNQUFNRSxXQUFOLE1BQXVCLEVBQXhDO0FBQ0EsYUFBTyxDQUFDLGlCQUFFRSxRQUFGLENBQVdILFFBQVgsRUFBcUIsZUFBS0ksR0FBMUIsQ0FBUjtBQUNEOzs7K0JBRVU7QUFDVCwyQkFBV2xCLE1BQVgsQ0FBa0JtQixRQUFsQixDQUEyQixLQUFLcEIsYUFBaEM7QUFDRDs7Ozs7a0JBdENrQkosZ0IiLCJmaWxlIjoic3RhY2tfdHJhY2VfZmlsdGVyLmpzIiwic291cmNlc0NvbnRlbnQiOlsiaW1wb3J0IF8gZnJvbSAnbG9kYXNoJ1xuaW1wb3J0IHN0YWNrQ2hhaW4gZnJvbSAnc3RhY2stY2hhaW4nXG5pbXBvcnQgcGF0aCBmcm9tICdwYXRoJ1xuXG5leHBvcnQgZGVmYXVsdCBjbGFzcyBTdGFja1RyYWNlRmlsdGVyIHtcbiAgY29uc3RydWN0b3IoKSB7XG4gICAgdGhpcy5jdWN1bWJlclBhdGggPSBwYXRoLmpvaW4oX19kaXJuYW1lLCAnLi4nLCAnLi4nKVxuICB9XG5cbiAgZmlsdGVyKCkge1xuICAgIHRoaXMuY3VycmVudEZpbHRlciA9IHN0YWNrQ2hhaW4uZmlsdGVyLmF0dGFjaCgoZXJyb3IsIGZyYW1lcykgPT4ge1xuICAgICAgaWYgKHRoaXMuaXNFcnJvckluQ3VjdW1iZXIoZnJhbWVzKSkge1xuICAgICAgICByZXR1cm4gZnJhbWVzXG4gICAgICB9XG4gICAgICBjb25zdCBpbmRleCA9IF8uZmluZEluZGV4KGZyYW1lcywgOjp0aGlzLmlzRnJhbWVJbkN1Y3VtYmVyKVxuICAgICAgaWYgKGluZGV4ID09PSAtMSkge1xuICAgICAgICByZXR1cm4gZnJhbWVzXG4gICAgICB9IGVsc2Uge1xuICAgICAgICByZXR1cm4gZnJhbWVzLnNsaWNlKDAsIGluZGV4KVxuICAgICAgfVxuICAgIH0pXG4gIH1cblxuICBpc0Vycm9ySW5DdWN1bWJlcihmcmFtZXMpIHtcbiAgICBjb25zdCBmaWx0ZXJlZEZyYW1lcyA9IF8ucmVqZWN0KGZyYW1lcywgOjp0aGlzLmlzRnJhbWVJbk5vZGUpXG4gICAgcmV0dXJuIChcbiAgICAgIGZpbHRlcmVkRnJhbWVzLmxlbmd0aCA+IDAgJiYgdGhpcy5pc0ZyYW1lSW5DdWN1bWJlcihmaWx0ZXJlZEZyYW1lc1swXSlcbiAgICApXG4gIH1cblxuICBpc0ZyYW1lSW5DdWN1bWJlcihmcmFtZSkge1xuICAgIGNvbnN0IGZpbGVOYW1lID0gZnJhbWUuZ2V0RmlsZU5hbWUoKSB8fCAnJ1xuICAgIHJldHVybiBfLnN0YXJ0c1dpdGgoZmlsZU5hbWUsIHRoaXMuY3VjdW1iZXJQYXRoKVxuICB9XG5cbiAgaXNGcmFtZUluTm9kZShmcmFtZSkge1xuICAgIGNvbnN0IGZpbGVOYW1lID0gZnJhbWUuZ2V0RmlsZU5hbWUoKSB8fCAnJ1xuICAgIHJldHVybiAhXy5pbmNsdWRlcyhmaWxlTmFtZSwgcGF0aC5zZXApXG4gIH1cblxuICB1bmZpbHRlcigpIHtcbiAgICBzdGFja0NoYWluLmZpbHRlci5kZWF0dGFjaCh0aGlzLmN1cnJlbnRGaWx0ZXIpXG4gIH1cbn1cbiJdfQ==