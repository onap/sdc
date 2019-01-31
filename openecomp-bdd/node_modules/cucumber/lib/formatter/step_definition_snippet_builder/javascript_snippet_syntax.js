'use strict';

Object.defineProperty(exports, "__esModule", {
  value: true
});

var _classCallCheck2 = require('babel-runtime/helpers/classCallCheck');

var _classCallCheck3 = _interopRequireDefault(_classCallCheck2);

var _createClass2 = require('babel-runtime/helpers/createClass');

var _createClass3 = _interopRequireDefault(_createClass2);

function _interopRequireDefault(obj) { return obj && obj.__esModule ? obj : { default: obj }; }

var CALLBACK_NAME = 'callback';

var JavaScriptSnippetSyntax = function () {
  function JavaScriptSnippetSyntax(snippetInterface) {
    (0, _classCallCheck3.default)(this, JavaScriptSnippetSyntax);

    this.snippetInterface = snippetInterface;
  }

  (0, _createClass3.default)(JavaScriptSnippetSyntax, [{
    key: 'build',
    value: function build(_ref) {
      var _this = this;

      var comment = _ref.comment,
          generatedExpressions = _ref.generatedExpressions,
          functionName = _ref.functionName,
          stepParameterNames = _ref.stepParameterNames;

      var functionKeyword = 'function ';
      if (this.snippetInterface === 'generator') {
        functionKeyword += '*';
      }

      var implementation = void 0;
      if (this.snippetInterface === 'callback') {
        implementation = CALLBACK_NAME + '(null, \'pending\');';
      } else {
        implementation = "return 'pending';";
      }

      var definitionChoices = generatedExpressions.map(function (generatedExpression, index) {
        var prefix = index === 0 ? '' : '// ';
        var allParameterNames = generatedExpression.parameterNames.concat(stepParameterNames);
        if (_this.snippetInterface === 'callback') {
          allParameterNames.push(CALLBACK_NAME);
        }
        return prefix + functionName + "('" + generatedExpression.source.replace(/'/g, "\\'") + "', " + functionKeyword + '(' + allParameterNames.join(', ') + ') {\n';
      });

      return definitionChoices.join('') + ('  // ' + comment + '\n') + ('  ' + implementation + '\n') + '});';
    }
  }]);
  return JavaScriptSnippetSyntax;
}();

exports.default = JavaScriptSnippetSyntax;
//# sourceMappingURL=data:application/json;charset=utf-8;base64,eyJ2ZXJzaW9uIjozLCJzb3VyY2VzIjpbIi4uLy4uLy4uL3NyYy9mb3JtYXR0ZXIvc3RlcF9kZWZpbml0aW9uX3NuaXBwZXRfYnVpbGRlci9qYXZhc2NyaXB0X3NuaXBwZXRfc3ludGF4LmpzIl0sIm5hbWVzIjpbIkNBTExCQUNLX05BTUUiLCJKYXZhU2NyaXB0U25pcHBldFN5bnRheCIsInNuaXBwZXRJbnRlcmZhY2UiLCJjb21tZW50IiwiZ2VuZXJhdGVkRXhwcmVzc2lvbnMiLCJmdW5jdGlvbk5hbWUiLCJzdGVwUGFyYW1ldGVyTmFtZXMiLCJmdW5jdGlvbktleXdvcmQiLCJpbXBsZW1lbnRhdGlvbiIsImRlZmluaXRpb25DaG9pY2VzIiwibWFwIiwiZ2VuZXJhdGVkRXhwcmVzc2lvbiIsImluZGV4IiwicHJlZml4IiwiYWxsUGFyYW1ldGVyTmFtZXMiLCJwYXJhbWV0ZXJOYW1lcyIsImNvbmNhdCIsInB1c2giLCJzb3VyY2UiLCJyZXBsYWNlIiwiam9pbiJdLCJtYXBwaW5ncyI6Ijs7Ozs7Ozs7Ozs7Ozs7OztBQUFBLElBQU1BLGdCQUFnQixVQUF0Qjs7SUFFcUJDLHVCO0FBQ25CLG1DQUFZQyxnQkFBWixFQUE4QjtBQUFBOztBQUM1QixTQUFLQSxnQkFBTCxHQUF3QkEsZ0JBQXhCO0FBQ0Q7Ozs7Z0NBRTBFO0FBQUE7O0FBQUEsVUFBbkVDLE9BQW1FLFFBQW5FQSxPQUFtRTtBQUFBLFVBQTFEQyxvQkFBMEQsUUFBMURBLG9CQUEwRDtBQUFBLFVBQXBDQyxZQUFvQyxRQUFwQ0EsWUFBb0M7QUFBQSxVQUF0QkMsa0JBQXNCLFFBQXRCQSxrQkFBc0I7O0FBQ3pFLFVBQUlDLGtCQUFrQixXQUF0QjtBQUNBLFVBQUksS0FBS0wsZ0JBQUwsS0FBMEIsV0FBOUIsRUFBMkM7QUFDekNLLDJCQUFtQixHQUFuQjtBQUNEOztBQUVELFVBQUlDLHVCQUFKO0FBQ0EsVUFBSSxLQUFLTixnQkFBTCxLQUEwQixVQUE5QixFQUEwQztBQUN4Q00seUJBQW9CUixhQUFwQjtBQUNELE9BRkQsTUFFTztBQUNMUSx5QkFBaUIsbUJBQWpCO0FBQ0Q7O0FBRUQsVUFBTUMsb0JBQW9CTCxxQkFBcUJNLEdBQXJCLENBQ3hCLFVBQUNDLG1CQUFELEVBQXNCQyxLQUF0QixFQUFnQztBQUM5QixZQUFNQyxTQUFTRCxVQUFVLENBQVYsR0FBYyxFQUFkLEdBQW1CLEtBQWxDO0FBQ0EsWUFBTUUsb0JBQW9CSCxvQkFBb0JJLGNBQXBCLENBQW1DQyxNQUFuQyxDQUN4QlYsa0JBRHdCLENBQTFCO0FBR0EsWUFBSSxNQUFLSixnQkFBTCxLQUEwQixVQUE5QixFQUEwQztBQUN4Q1ksNEJBQWtCRyxJQUFsQixDQUF1QmpCLGFBQXZCO0FBQ0Q7QUFDRCxlQUNFYSxTQUNBUixZQURBLEdBRUEsSUFGQSxHQUdBTSxvQkFBb0JPLE1BQXBCLENBQTJCQyxPQUEzQixDQUFtQyxJQUFuQyxFQUF5QyxLQUF6QyxDQUhBLEdBSUEsS0FKQSxHQUtBWixlQUxBLEdBTUEsR0FOQSxHQU9BTyxrQkFBa0JNLElBQWxCLENBQXVCLElBQXZCLENBUEEsR0FRQSxPQVRGO0FBV0QsT0FwQnVCLENBQTFCOztBQXVCQSxhQUNFWCxrQkFBa0JXLElBQWxCLENBQXVCLEVBQXZCLGVBQ1FqQixPQURSLG1CQUVLSyxjQUZMLFdBR0EsS0FKRjtBQU1EOzs7OztrQkEvQ2tCUCx1QiIsImZpbGUiOiJqYXZhc2NyaXB0X3NuaXBwZXRfc3ludGF4LmpzIiwic291cmNlc0NvbnRlbnQiOlsiY29uc3QgQ0FMTEJBQ0tfTkFNRSA9ICdjYWxsYmFjaydcblxuZXhwb3J0IGRlZmF1bHQgY2xhc3MgSmF2YVNjcmlwdFNuaXBwZXRTeW50YXgge1xuICBjb25zdHJ1Y3RvcihzbmlwcGV0SW50ZXJmYWNlKSB7XG4gICAgdGhpcy5zbmlwcGV0SW50ZXJmYWNlID0gc25pcHBldEludGVyZmFjZVxuICB9XG5cbiAgYnVpbGQoeyBjb21tZW50LCBnZW5lcmF0ZWRFeHByZXNzaW9ucywgZnVuY3Rpb25OYW1lLCBzdGVwUGFyYW1ldGVyTmFtZXMgfSkge1xuICAgIGxldCBmdW5jdGlvbktleXdvcmQgPSAnZnVuY3Rpb24gJ1xuICAgIGlmICh0aGlzLnNuaXBwZXRJbnRlcmZhY2UgPT09ICdnZW5lcmF0b3InKSB7XG4gICAgICBmdW5jdGlvbktleXdvcmQgKz0gJyonXG4gICAgfVxuXG4gICAgbGV0IGltcGxlbWVudGF0aW9uXG4gICAgaWYgKHRoaXMuc25pcHBldEludGVyZmFjZSA9PT0gJ2NhbGxiYWNrJykge1xuICAgICAgaW1wbGVtZW50YXRpb24gPSBgJHtDQUxMQkFDS19OQU1FfShudWxsLCAncGVuZGluZycpO2BcbiAgICB9IGVsc2Uge1xuICAgICAgaW1wbGVtZW50YXRpb24gPSBcInJldHVybiAncGVuZGluZyc7XCJcbiAgICB9XG5cbiAgICBjb25zdCBkZWZpbml0aW9uQ2hvaWNlcyA9IGdlbmVyYXRlZEV4cHJlc3Npb25zLm1hcChcbiAgICAgIChnZW5lcmF0ZWRFeHByZXNzaW9uLCBpbmRleCkgPT4ge1xuICAgICAgICBjb25zdCBwcmVmaXggPSBpbmRleCA9PT0gMCA/ICcnIDogJy8vICdcbiAgICAgICAgY29uc3QgYWxsUGFyYW1ldGVyTmFtZXMgPSBnZW5lcmF0ZWRFeHByZXNzaW9uLnBhcmFtZXRlck5hbWVzLmNvbmNhdChcbiAgICAgICAgICBzdGVwUGFyYW1ldGVyTmFtZXNcbiAgICAgICAgKVxuICAgICAgICBpZiAodGhpcy5zbmlwcGV0SW50ZXJmYWNlID09PSAnY2FsbGJhY2snKSB7XG4gICAgICAgICAgYWxsUGFyYW1ldGVyTmFtZXMucHVzaChDQUxMQkFDS19OQU1FKVxuICAgICAgICB9XG4gICAgICAgIHJldHVybiAoXG4gICAgICAgICAgcHJlZml4ICtcbiAgICAgICAgICBmdW5jdGlvbk5hbWUgK1xuICAgICAgICAgIFwiKCdcIiArXG4gICAgICAgICAgZ2VuZXJhdGVkRXhwcmVzc2lvbi5zb3VyY2UucmVwbGFjZSgvJy9nLCBcIlxcXFwnXCIpICtcbiAgICAgICAgICBcIicsIFwiICtcbiAgICAgICAgICBmdW5jdGlvbktleXdvcmQgK1xuICAgICAgICAgICcoJyArXG4gICAgICAgICAgYWxsUGFyYW1ldGVyTmFtZXMuam9pbignLCAnKSArXG4gICAgICAgICAgJykge1xcbidcbiAgICAgICAgKVxuICAgICAgfVxuICAgIClcblxuICAgIHJldHVybiAoXG4gICAgICBkZWZpbml0aW9uQ2hvaWNlcy5qb2luKCcnKSArXG4gICAgICBgICAvLyAke2NvbW1lbnR9XFxuYCArXG4gICAgICBgICAke2ltcGxlbWVudGF0aW9ufVxcbmAgK1xuICAgICAgJ30pOydcbiAgICApXG4gIH1cbn1cbiJdfQ==