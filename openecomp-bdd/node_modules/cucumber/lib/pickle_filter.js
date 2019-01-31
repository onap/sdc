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

var _path = require('path');

var _path2 = _interopRequireDefault(_path);

var _cucumberTagExpressions = require('cucumber-tag-expressions');

function _interopRequireDefault(obj) { return obj && obj.__esModule ? obj : { default: obj }; }

var FEATURE_LINENUM_REGEXP = /^(.*?)((?::[\d]+)+)?$/;
var tagExpressionParser = new _cucumberTagExpressions.TagExpressionParser();

var PickleFilter = function () {
  function PickleFilter(_ref) {
    var featurePaths = _ref.featurePaths,
        names = _ref.names,
        tagExpression = _ref.tagExpression;
    (0, _classCallCheck3.default)(this, PickleFilter);

    this.featureUriToLinesMapping = this.getFeatureUriToLinesMapping(featurePaths || []);
    this.names = names || [];
    if (tagExpression) {
      this.tagExpressionNode = tagExpressionParser.parse(tagExpression || '');
    }
  }

  (0, _createClass3.default)(PickleFilter, [{
    key: 'getFeatureUriToLinesMapping',
    value: function getFeatureUriToLinesMapping(featurePaths) {
      var mapping = {};
      featurePaths.forEach(function (featurePath) {
        var match = FEATURE_LINENUM_REGEXP.exec(featurePath);
        if (match) {
          var uri = _path2.default.resolve(match[1]);
          var linesExpression = match[2];
          if (linesExpression) {
            if (!mapping[uri]) {
              mapping[uri] = [];
            }
            linesExpression.slice(1).split(':').forEach(function (line) {
              mapping[uri].push(parseInt(line));
            });
          }
        }
      });
      return mapping;
    }
  }, {
    key: 'matches',
    value: function matches(_ref2) {
      var pickle = _ref2.pickle,
          uri = _ref2.uri;

      return this.matchesAnyLine({ pickle: pickle, uri: uri }) && this.matchesAnyName(pickle) && this.matchesAllTagExpressions(pickle);
    }
  }, {
    key: 'matchesAnyLine',
    value: function matchesAnyLine(_ref3) {
      var pickle = _ref3.pickle,
          uri = _ref3.uri;

      var lines = this.featureUriToLinesMapping[_path2.default.resolve(uri)];
      if (lines) {
        return _lodash2.default.size(_lodash2.default.intersection(lines, _lodash2.default.map(pickle.locations, 'line'))) > 0;
      } else {
        return true;
      }
    }
  }, {
    key: 'matchesAnyName',
    value: function matchesAnyName(pickle) {
      if (this.names.length === 0) {
        return true;
      }
      return _lodash2.default.some(this.names, function (name) {
        return pickle.name.match(name);
      });
    }
  }, {
    key: 'matchesAllTagExpressions',
    value: function matchesAllTagExpressions(pickle) {
      if (!this.tagExpressionNode) {
        return true;
      }
      return this.tagExpressionNode.evaluate(_lodash2.default.map(pickle.tags, 'name'));
    }
  }]);
  return PickleFilter;
}();

exports.default = PickleFilter;
//# sourceMappingURL=data:application/json;charset=utf-8;base64,eyJ2ZXJzaW9uIjozLCJzb3VyY2VzIjpbIi4uL3NyYy9waWNrbGVfZmlsdGVyLmpzIl0sIm5hbWVzIjpbIkZFQVRVUkVfTElORU5VTV9SRUdFWFAiLCJ0YWdFeHByZXNzaW9uUGFyc2VyIiwiUGlja2xlRmlsdGVyIiwiZmVhdHVyZVBhdGhzIiwibmFtZXMiLCJ0YWdFeHByZXNzaW9uIiwiZmVhdHVyZVVyaVRvTGluZXNNYXBwaW5nIiwiZ2V0RmVhdHVyZVVyaVRvTGluZXNNYXBwaW5nIiwidGFnRXhwcmVzc2lvbk5vZGUiLCJwYXJzZSIsIm1hcHBpbmciLCJmb3JFYWNoIiwibWF0Y2giLCJleGVjIiwiZmVhdHVyZVBhdGgiLCJ1cmkiLCJyZXNvbHZlIiwibGluZXNFeHByZXNzaW9uIiwic2xpY2UiLCJzcGxpdCIsImxpbmUiLCJwdXNoIiwicGFyc2VJbnQiLCJwaWNrbGUiLCJtYXRjaGVzQW55TGluZSIsIm1hdGNoZXNBbnlOYW1lIiwibWF0Y2hlc0FsbFRhZ0V4cHJlc3Npb25zIiwibGluZXMiLCJzaXplIiwiaW50ZXJzZWN0aW9uIiwibWFwIiwibG9jYXRpb25zIiwibGVuZ3RoIiwic29tZSIsIm5hbWUiLCJldmFsdWF0ZSIsInRhZ3MiXSwibWFwcGluZ3MiOiI7Ozs7Ozs7Ozs7Ozs7O0FBQUE7Ozs7QUFDQTs7OztBQUNBOzs7O0FBRUEsSUFBTUEseUJBQXlCLHVCQUEvQjtBQUNBLElBQU1DLHNCQUFzQixpREFBNUI7O0lBRXFCQyxZO0FBQ25CLDhCQUFvRDtBQUFBLFFBQXRDQyxZQUFzQyxRQUF0Q0EsWUFBc0M7QUFBQSxRQUF4QkMsS0FBd0IsUUFBeEJBLEtBQXdCO0FBQUEsUUFBakJDLGFBQWlCLFFBQWpCQSxhQUFpQjtBQUFBOztBQUNsRCxTQUFLQyx3QkFBTCxHQUFnQyxLQUFLQywyQkFBTCxDQUM5QkosZ0JBQWdCLEVBRGMsQ0FBaEM7QUFHQSxTQUFLQyxLQUFMLEdBQWFBLFNBQVMsRUFBdEI7QUFDQSxRQUFJQyxhQUFKLEVBQW1CO0FBQ2pCLFdBQUtHLGlCQUFMLEdBQXlCUCxvQkFBb0JRLEtBQXBCLENBQTBCSixpQkFBaUIsRUFBM0MsQ0FBekI7QUFDRDtBQUNGOzs7O2dEQUUyQkYsWSxFQUFjO0FBQ3hDLFVBQU1PLFVBQVUsRUFBaEI7QUFDQVAsbUJBQWFRLE9BQWIsQ0FBcUIsdUJBQWU7QUFDbEMsWUFBTUMsUUFBUVosdUJBQXVCYSxJQUF2QixDQUE0QkMsV0FBNUIsQ0FBZDtBQUNBLFlBQUlGLEtBQUosRUFBVztBQUNULGNBQU1HLE1BQU0sZUFBS0MsT0FBTCxDQUFhSixNQUFNLENBQU4sQ0FBYixDQUFaO0FBQ0EsY0FBTUssa0JBQWtCTCxNQUFNLENBQU4sQ0FBeEI7QUFDQSxjQUFJSyxlQUFKLEVBQXFCO0FBQ25CLGdCQUFJLENBQUNQLFFBQVFLLEdBQVIsQ0FBTCxFQUFtQjtBQUNqQkwsc0JBQVFLLEdBQVIsSUFBZSxFQUFmO0FBQ0Q7QUFDREUsNEJBQ0dDLEtBREgsQ0FDUyxDQURULEVBRUdDLEtBRkgsQ0FFUyxHQUZULEVBR0dSLE9BSEgsQ0FHVyxVQUFTUyxJQUFULEVBQWU7QUFDdEJWLHNCQUFRSyxHQUFSLEVBQWFNLElBQWIsQ0FBa0JDLFNBQVNGLElBQVQsQ0FBbEI7QUFDRCxhQUxIO0FBTUQ7QUFDRjtBQUNGLE9BakJEO0FBa0JBLGFBQU9WLE9BQVA7QUFDRDs7O21DQUV3QjtBQUFBLFVBQWZhLE1BQWUsU0FBZkEsTUFBZTtBQUFBLFVBQVBSLEdBQU8sU0FBUEEsR0FBTzs7QUFDdkIsYUFDRSxLQUFLUyxjQUFMLENBQW9CLEVBQUVELGNBQUYsRUFBVVIsUUFBVixFQUFwQixLQUNBLEtBQUtVLGNBQUwsQ0FBb0JGLE1BQXBCLENBREEsSUFFQSxLQUFLRyx3QkFBTCxDQUE4QkgsTUFBOUIsQ0FIRjtBQUtEOzs7MENBRStCO0FBQUEsVUFBZkEsTUFBZSxTQUFmQSxNQUFlO0FBQUEsVUFBUFIsR0FBTyxTQUFQQSxHQUFPOztBQUM5QixVQUFNWSxRQUFRLEtBQUtyQix3QkFBTCxDQUE4QixlQUFLVSxPQUFMLENBQWFELEdBQWIsQ0FBOUIsQ0FBZDtBQUNBLFVBQUlZLEtBQUosRUFBVztBQUNULGVBQU8saUJBQUVDLElBQUYsQ0FBTyxpQkFBRUMsWUFBRixDQUFlRixLQUFmLEVBQXNCLGlCQUFFRyxHQUFGLENBQU1QLE9BQU9RLFNBQWIsRUFBd0IsTUFBeEIsQ0FBdEIsQ0FBUCxJQUFpRSxDQUF4RTtBQUNELE9BRkQsTUFFTztBQUNMLGVBQU8sSUFBUDtBQUNEO0FBQ0Y7OzttQ0FFY1IsTSxFQUFRO0FBQ3JCLFVBQUksS0FBS25CLEtBQUwsQ0FBVzRCLE1BQVgsS0FBc0IsQ0FBMUIsRUFBNkI7QUFDM0IsZUFBTyxJQUFQO0FBQ0Q7QUFDRCxhQUFPLGlCQUFFQyxJQUFGLENBQU8sS0FBSzdCLEtBQVosRUFBbUIsVUFBUzhCLElBQVQsRUFBZTtBQUN2QyxlQUFPWCxPQUFPVyxJQUFQLENBQVl0QixLQUFaLENBQWtCc0IsSUFBbEIsQ0FBUDtBQUNELE9BRk0sQ0FBUDtBQUdEOzs7NkNBRXdCWCxNLEVBQVE7QUFDL0IsVUFBSSxDQUFDLEtBQUtmLGlCQUFWLEVBQTZCO0FBQzNCLGVBQU8sSUFBUDtBQUNEO0FBQ0QsYUFBTyxLQUFLQSxpQkFBTCxDQUF1QjJCLFFBQXZCLENBQWdDLGlCQUFFTCxHQUFGLENBQU1QLE9BQU9hLElBQWIsRUFBbUIsTUFBbkIsQ0FBaEMsQ0FBUDtBQUNEOzs7OztrQkFqRWtCbEMsWSIsImZpbGUiOiJwaWNrbGVfZmlsdGVyLmpzIiwic291cmNlc0NvbnRlbnQiOlsiaW1wb3J0IF8gZnJvbSAnbG9kYXNoJ1xuaW1wb3J0IHBhdGggZnJvbSAncGF0aCdcbmltcG9ydCB7IFRhZ0V4cHJlc3Npb25QYXJzZXIgfSBmcm9tICdjdWN1bWJlci10YWctZXhwcmVzc2lvbnMnXG5cbmNvbnN0IEZFQVRVUkVfTElORU5VTV9SRUdFWFAgPSAvXiguKj8pKCg/OjpbXFxkXSspKyk/JC9cbmNvbnN0IHRhZ0V4cHJlc3Npb25QYXJzZXIgPSBuZXcgVGFnRXhwcmVzc2lvblBhcnNlcigpXG5cbmV4cG9ydCBkZWZhdWx0IGNsYXNzIFBpY2tsZUZpbHRlciB7XG4gIGNvbnN0cnVjdG9yKHsgZmVhdHVyZVBhdGhzLCBuYW1lcywgdGFnRXhwcmVzc2lvbiB9KSB7XG4gICAgdGhpcy5mZWF0dXJlVXJpVG9MaW5lc01hcHBpbmcgPSB0aGlzLmdldEZlYXR1cmVVcmlUb0xpbmVzTWFwcGluZyhcbiAgICAgIGZlYXR1cmVQYXRocyB8fCBbXVxuICAgIClcbiAgICB0aGlzLm5hbWVzID0gbmFtZXMgfHwgW11cbiAgICBpZiAodGFnRXhwcmVzc2lvbikge1xuICAgICAgdGhpcy50YWdFeHByZXNzaW9uTm9kZSA9IHRhZ0V4cHJlc3Npb25QYXJzZXIucGFyc2UodGFnRXhwcmVzc2lvbiB8fCAnJylcbiAgICB9XG4gIH1cblxuICBnZXRGZWF0dXJlVXJpVG9MaW5lc01hcHBpbmcoZmVhdHVyZVBhdGhzKSB7XG4gICAgY29uc3QgbWFwcGluZyA9IHt9XG4gICAgZmVhdHVyZVBhdGhzLmZvckVhY2goZmVhdHVyZVBhdGggPT4ge1xuICAgICAgY29uc3QgbWF0Y2ggPSBGRUFUVVJFX0xJTkVOVU1fUkVHRVhQLmV4ZWMoZmVhdHVyZVBhdGgpXG4gICAgICBpZiAobWF0Y2gpIHtcbiAgICAgICAgY29uc3QgdXJpID0gcGF0aC5yZXNvbHZlKG1hdGNoWzFdKVxuICAgICAgICBjb25zdCBsaW5lc0V4cHJlc3Npb24gPSBtYXRjaFsyXVxuICAgICAgICBpZiAobGluZXNFeHByZXNzaW9uKSB7XG4gICAgICAgICAgaWYgKCFtYXBwaW5nW3VyaV0pIHtcbiAgICAgICAgICAgIG1hcHBpbmdbdXJpXSA9IFtdXG4gICAgICAgICAgfVxuICAgICAgICAgIGxpbmVzRXhwcmVzc2lvblxuICAgICAgICAgICAgLnNsaWNlKDEpXG4gICAgICAgICAgICAuc3BsaXQoJzonKVxuICAgICAgICAgICAgLmZvckVhY2goZnVuY3Rpb24obGluZSkge1xuICAgICAgICAgICAgICBtYXBwaW5nW3VyaV0ucHVzaChwYXJzZUludChsaW5lKSlcbiAgICAgICAgICAgIH0pXG4gICAgICAgIH1cbiAgICAgIH1cbiAgICB9KVxuICAgIHJldHVybiBtYXBwaW5nXG4gIH1cblxuICBtYXRjaGVzKHsgcGlja2xlLCB1cmkgfSkge1xuICAgIHJldHVybiAoXG4gICAgICB0aGlzLm1hdGNoZXNBbnlMaW5lKHsgcGlja2xlLCB1cmkgfSkgJiZcbiAgICAgIHRoaXMubWF0Y2hlc0FueU5hbWUocGlja2xlKSAmJlxuICAgICAgdGhpcy5tYXRjaGVzQWxsVGFnRXhwcmVzc2lvbnMocGlja2xlKVxuICAgIClcbiAgfVxuXG4gIG1hdGNoZXNBbnlMaW5lKHsgcGlja2xlLCB1cmkgfSkge1xuICAgIGNvbnN0IGxpbmVzID0gdGhpcy5mZWF0dXJlVXJpVG9MaW5lc01hcHBpbmdbcGF0aC5yZXNvbHZlKHVyaSldXG4gICAgaWYgKGxpbmVzKSB7XG4gICAgICByZXR1cm4gXy5zaXplKF8uaW50ZXJzZWN0aW9uKGxpbmVzLCBfLm1hcChwaWNrbGUubG9jYXRpb25zLCAnbGluZScpKSkgPiAwXG4gICAgfSBlbHNlIHtcbiAgICAgIHJldHVybiB0cnVlXG4gICAgfVxuICB9XG5cbiAgbWF0Y2hlc0FueU5hbWUocGlja2xlKSB7XG4gICAgaWYgKHRoaXMubmFtZXMubGVuZ3RoID09PSAwKSB7XG4gICAgICByZXR1cm4gdHJ1ZVxuICAgIH1cbiAgICByZXR1cm4gXy5zb21lKHRoaXMubmFtZXMsIGZ1bmN0aW9uKG5hbWUpIHtcbiAgICAgIHJldHVybiBwaWNrbGUubmFtZS5tYXRjaChuYW1lKVxuICAgIH0pXG4gIH1cblxuICBtYXRjaGVzQWxsVGFnRXhwcmVzc2lvbnMocGlja2xlKSB7XG4gICAgaWYgKCF0aGlzLnRhZ0V4cHJlc3Npb25Ob2RlKSB7XG4gICAgICByZXR1cm4gdHJ1ZVxuICAgIH1cbiAgICByZXR1cm4gdGhpcy50YWdFeHByZXNzaW9uTm9kZS5ldmFsdWF0ZShfLm1hcChwaWNrbGUudGFncywgJ25hbWUnKSlcbiAgfVxufVxuIl19