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

var _event_protocol_formatter = require('./event_protocol_formatter');

var _event_protocol_formatter2 = _interopRequireDefault(_event_protocol_formatter);

var _get_color_fns = require('./get_color_fns');

var _get_color_fns2 = _interopRequireDefault(_get_color_fns);

var _javascript_snippet_syntax = require('./step_definition_snippet_builder/javascript_snippet_syntax');

var _javascript_snippet_syntax2 = _interopRequireDefault(_javascript_snippet_syntax);

var _json_formatter = require('./json_formatter');

var _json_formatter2 = _interopRequireDefault(_json_formatter);

var _path = require('path');

var _path2 = _interopRequireDefault(_path);

var _progress_bar_formatter = require('./progress_bar_formatter');

var _progress_bar_formatter2 = _interopRequireDefault(_progress_bar_formatter);

var _progress_formatter = require('./progress_formatter');

var _progress_formatter2 = _interopRequireDefault(_progress_formatter);

var _rerun_formatter = require('./rerun_formatter');

var _rerun_formatter2 = _interopRequireDefault(_rerun_formatter);

var _snippets_formatter = require('./snippets_formatter');

var _snippets_formatter2 = _interopRequireDefault(_snippets_formatter);

var _step_definition_snippet_builder = require('./step_definition_snippet_builder');

var _step_definition_snippet_builder2 = _interopRequireDefault(_step_definition_snippet_builder);

var _summary_formatter = require('./summary_formatter');

var _summary_formatter2 = _interopRequireDefault(_summary_formatter);

var _usage_formatter = require('./usage_formatter');

var _usage_formatter2 = _interopRequireDefault(_usage_formatter);

var _usage_json_formatter = require('./usage_json_formatter');

var _usage_json_formatter2 = _interopRequireDefault(_usage_json_formatter);

function _interopRequireDefault(obj) { return obj && obj.__esModule ? obj : { default: obj }; }

var FormatterBuilder = function () {
  function FormatterBuilder() {
    (0, _classCallCheck3.default)(this, FormatterBuilder);
  }

  (0, _createClass3.default)(FormatterBuilder, null, [{
    key: 'build',
    value: function build(type, options) {
      var Formatter = FormatterBuilder.getConstructorByType(type, options);
      var extendedOptions = (0, _extends3.default)({
        colorFns: (0, _get_color_fns2.default)(options.colorsEnabled),
        snippetBuilder: FormatterBuilder.getStepDefinitionSnippetBuilder(options)
      }, options);
      return new Formatter(extendedOptions);
    }
  }, {
    key: 'getConstructorByType',
    value: function getConstructorByType(type, options) {
      switch (type) {
        case 'event-protocol':
          return _event_protocol_formatter2.default;
        case 'json':
          return _json_formatter2.default;
        case 'progress':
          return _progress_formatter2.default;
        case 'progress-bar':
          return _progress_bar_formatter2.default;
        case 'rerun':
          return _rerun_formatter2.default;
        case 'snippets':
          return _snippets_formatter2.default;
        case 'summary':
          return _summary_formatter2.default;
        case 'usage':
          return _usage_formatter2.default;
        case 'usage-json':
          return _usage_json_formatter2.default;
        default:
          return FormatterBuilder.loadCustomFormatter(type, options);
      }
    }
  }, {
    key: 'getStepDefinitionSnippetBuilder',
    value: function getStepDefinitionSnippetBuilder(_ref) {
      var cwd = _ref.cwd,
          snippetInterface = _ref.snippetInterface,
          snippetSyntax = _ref.snippetSyntax,
          supportCodeLibrary = _ref.supportCodeLibrary;

      if (!snippetInterface) {
        snippetInterface = 'callback';
      }
      var Syntax = _javascript_snippet_syntax2.default;
      if (snippetSyntax) {
        var fullSyntaxPath = _path2.default.resolve(cwd, snippetSyntax);
        Syntax = require(fullSyntaxPath);
      }
      return new _step_definition_snippet_builder2.default({
        snippetSyntax: new Syntax(snippetInterface),
        parameterTypeRegistry: supportCodeLibrary.parameterTypeRegistry
      });
    }
  }, {
    key: 'loadCustomFormatter',
    value: function loadCustomFormatter(customFormatterPath, _ref2) {
      var cwd = _ref2.cwd;

      var fullCustomFormatterPath = _path2.default.resolve(cwd, customFormatterPath);
      var CustomFormatter = require(fullCustomFormatterPath);
      if (typeof CustomFormatter === 'function') {
        return CustomFormatter;
      } else if (CustomFormatter && typeof CustomFormatter.default === 'function') {
        return CustomFormatter.default;
      } else {
        throw new Error('Custom formatter (' + customFormatterPath + ') does not export a function');
      }
    }
  }]);
  return FormatterBuilder;
}();

exports.default = FormatterBuilder;
//# sourceMappingURL=data:application/json;charset=utf-8;base64,eyJ2ZXJzaW9uIjozLCJzb3VyY2VzIjpbIi4uLy4uL3NyYy9mb3JtYXR0ZXIvYnVpbGRlci5qcyJdLCJuYW1lcyI6WyJGb3JtYXR0ZXJCdWlsZGVyIiwidHlwZSIsIm9wdGlvbnMiLCJGb3JtYXR0ZXIiLCJnZXRDb25zdHJ1Y3RvckJ5VHlwZSIsImV4dGVuZGVkT3B0aW9ucyIsImNvbG9yRm5zIiwiY29sb3JzRW5hYmxlZCIsInNuaXBwZXRCdWlsZGVyIiwiZ2V0U3RlcERlZmluaXRpb25TbmlwcGV0QnVpbGRlciIsImxvYWRDdXN0b21Gb3JtYXR0ZXIiLCJjd2QiLCJzbmlwcGV0SW50ZXJmYWNlIiwic25pcHBldFN5bnRheCIsInN1cHBvcnRDb2RlTGlicmFyeSIsIlN5bnRheCIsImZ1bGxTeW50YXhQYXRoIiwicmVzb2x2ZSIsInJlcXVpcmUiLCJwYXJhbWV0ZXJUeXBlUmVnaXN0cnkiLCJjdXN0b21Gb3JtYXR0ZXJQYXRoIiwiZnVsbEN1c3RvbUZvcm1hdHRlclBhdGgiLCJDdXN0b21Gb3JtYXR0ZXIiLCJkZWZhdWx0IiwiRXJyb3IiXSwibWFwcGluZ3MiOiI7Ozs7Ozs7Ozs7Ozs7Ozs7OztBQUFBOzs7O0FBQ0E7Ozs7QUFDQTs7OztBQUNBOzs7O0FBQ0E7Ozs7QUFDQTs7OztBQUNBOzs7O0FBQ0E7Ozs7QUFDQTs7OztBQUNBOzs7O0FBQ0E7Ozs7QUFDQTs7OztBQUNBOzs7Ozs7SUFFcUJBLGdCOzs7Ozs7OzBCQUNOQyxJLEVBQU1DLE8sRUFBUztBQUMxQixVQUFNQyxZQUFZSCxpQkFBaUJJLG9CQUFqQixDQUFzQ0gsSUFBdEMsRUFBNENDLE9BQTVDLENBQWxCO0FBQ0EsVUFBTUc7QUFDSkMsa0JBQVUsNkJBQVlKLFFBQVFLLGFBQXBCLENBRE47QUFFSkMsd0JBQWdCUixpQkFBaUJTLCtCQUFqQixDQUFpRFAsT0FBakQ7QUFGWixTQUdEQSxPQUhDLENBQU47QUFLQSxhQUFPLElBQUlDLFNBQUosQ0FBY0UsZUFBZCxDQUFQO0FBQ0Q7Ozt5Q0FFMkJKLEksRUFBTUMsTyxFQUFTO0FBQ3pDLGNBQVFELElBQVI7QUFDRSxhQUFLLGdCQUFMO0FBQ0U7QUFDRixhQUFLLE1BQUw7QUFDRTtBQUNGLGFBQUssVUFBTDtBQUNFO0FBQ0YsYUFBSyxjQUFMO0FBQ0U7QUFDRixhQUFLLE9BQUw7QUFDRTtBQUNGLGFBQUssVUFBTDtBQUNFO0FBQ0YsYUFBSyxTQUFMO0FBQ0U7QUFDRixhQUFLLE9BQUw7QUFDRTtBQUNGLGFBQUssWUFBTDtBQUNFO0FBQ0Y7QUFDRSxpQkFBT0QsaUJBQWlCVSxtQkFBakIsQ0FBcUNULElBQXJDLEVBQTJDQyxPQUEzQyxDQUFQO0FBcEJKO0FBc0JEOzs7MERBT0U7QUFBQSxVQUpEUyxHQUlDLFFBSkRBLEdBSUM7QUFBQSxVQUhEQyxnQkFHQyxRQUhEQSxnQkFHQztBQUFBLFVBRkRDLGFBRUMsUUFGREEsYUFFQztBQUFBLFVBRERDLGtCQUNDLFFBRERBLGtCQUNDOztBQUNELFVBQUksQ0FBQ0YsZ0JBQUwsRUFBdUI7QUFDckJBLDJCQUFtQixVQUFuQjtBQUNEO0FBQ0QsVUFBSUcsNENBQUo7QUFDQSxVQUFJRixhQUFKLEVBQW1CO0FBQ2pCLFlBQU1HLGlCQUFpQixlQUFLQyxPQUFMLENBQWFOLEdBQWIsRUFBa0JFLGFBQWxCLENBQXZCO0FBQ0FFLGlCQUFTRyxRQUFRRixjQUFSLENBQVQ7QUFDRDtBQUNELGFBQU8sOENBQWlDO0FBQ3RDSCx1QkFBZSxJQUFJRSxNQUFKLENBQVdILGdCQUFYLENBRHVCO0FBRXRDTywrQkFBdUJMLG1CQUFtQks7QUFGSixPQUFqQyxDQUFQO0FBSUQ7Ozt3Q0FFMEJDLG1CLFNBQThCO0FBQUEsVUFBUFQsR0FBTyxTQUFQQSxHQUFPOztBQUN2RCxVQUFNVSwwQkFBMEIsZUFBS0osT0FBTCxDQUFhTixHQUFiLEVBQWtCUyxtQkFBbEIsQ0FBaEM7QUFDQSxVQUFNRSxrQkFBa0JKLFFBQVFHLHVCQUFSLENBQXhCO0FBQ0EsVUFBSSxPQUFPQyxlQUFQLEtBQTJCLFVBQS9CLEVBQTJDO0FBQ3pDLGVBQU9BLGVBQVA7QUFDRCxPQUZELE1BRU8sSUFDTEEsbUJBQ0EsT0FBT0EsZ0JBQWdCQyxPQUF2QixLQUFtQyxVQUY5QixFQUdMO0FBQ0EsZUFBT0QsZ0JBQWdCQyxPQUF2QjtBQUNELE9BTE0sTUFLQTtBQUNMLGNBQU0sSUFBSUMsS0FBSix3QkFDaUJKLG1CQURqQixrQ0FBTjtBQUdEO0FBQ0Y7Ozs7O2tCQXZFa0JwQixnQiIsImZpbGUiOiJidWlsZGVyLmpzIiwic291cmNlc0NvbnRlbnQiOlsiaW1wb3J0IEV2ZW50UHJvdG9jb2xGb3JtYXR0ZXIgZnJvbSAnLi9ldmVudF9wcm90b2NvbF9mb3JtYXR0ZXInXG5pbXBvcnQgZ2V0Q29sb3JGbnMgZnJvbSAnLi9nZXRfY29sb3JfZm5zJ1xuaW1wb3J0IEphdmFzY3JpcHRTbmlwcGV0U3ludGF4IGZyb20gJy4vc3RlcF9kZWZpbml0aW9uX3NuaXBwZXRfYnVpbGRlci9qYXZhc2NyaXB0X3NuaXBwZXRfc3ludGF4J1xuaW1wb3J0IEpzb25Gb3JtYXR0ZXIgZnJvbSAnLi9qc29uX2Zvcm1hdHRlcidcbmltcG9ydCBwYXRoIGZyb20gJ3BhdGgnXG5pbXBvcnQgUHJvZ3Jlc3NCYXJGb3JtYXR0ZXIgZnJvbSAnLi9wcm9ncmVzc19iYXJfZm9ybWF0dGVyJ1xuaW1wb3J0IFByb2dyZXNzRm9ybWF0dGVyIGZyb20gJy4vcHJvZ3Jlc3NfZm9ybWF0dGVyJ1xuaW1wb3J0IFJlcnVuRm9ybWF0dGVyIGZyb20gJy4vcmVydW5fZm9ybWF0dGVyJ1xuaW1wb3J0IFNuaXBwZXRzRm9ybWF0dGVyIGZyb20gJy4vc25pcHBldHNfZm9ybWF0dGVyJ1xuaW1wb3J0IFN0ZXBEZWZpbml0aW9uU25pcHBldEJ1aWxkZXIgZnJvbSAnLi9zdGVwX2RlZmluaXRpb25fc25pcHBldF9idWlsZGVyJ1xuaW1wb3J0IFN1bW1hcnlGb3JtYXR0ZXIgZnJvbSAnLi9zdW1tYXJ5X2Zvcm1hdHRlcidcbmltcG9ydCBVc2FnZUZvcm1hdHRlciBmcm9tICcuL3VzYWdlX2Zvcm1hdHRlcidcbmltcG9ydCBVc2FnZUpzb25Gb3JtYXR0ZXIgZnJvbSAnLi91c2FnZV9qc29uX2Zvcm1hdHRlcidcblxuZXhwb3J0IGRlZmF1bHQgY2xhc3MgRm9ybWF0dGVyQnVpbGRlciB7XG4gIHN0YXRpYyBidWlsZCh0eXBlLCBvcHRpb25zKSB7XG4gICAgY29uc3QgRm9ybWF0dGVyID0gRm9ybWF0dGVyQnVpbGRlci5nZXRDb25zdHJ1Y3RvckJ5VHlwZSh0eXBlLCBvcHRpb25zKVxuICAgIGNvbnN0IGV4dGVuZGVkT3B0aW9ucyA9IHtcbiAgICAgIGNvbG9yRm5zOiBnZXRDb2xvckZucyhvcHRpb25zLmNvbG9yc0VuYWJsZWQpLFxuICAgICAgc25pcHBldEJ1aWxkZXI6IEZvcm1hdHRlckJ1aWxkZXIuZ2V0U3RlcERlZmluaXRpb25TbmlwcGV0QnVpbGRlcihvcHRpb25zKSxcbiAgICAgIC4uLm9wdGlvbnNcbiAgICB9XG4gICAgcmV0dXJuIG5ldyBGb3JtYXR0ZXIoZXh0ZW5kZWRPcHRpb25zKVxuICB9XG5cbiAgc3RhdGljIGdldENvbnN0cnVjdG9yQnlUeXBlKHR5cGUsIG9wdGlvbnMpIHtcbiAgICBzd2l0Y2ggKHR5cGUpIHtcbiAgICAgIGNhc2UgJ2V2ZW50LXByb3RvY29sJzpcbiAgICAgICAgcmV0dXJuIEV2ZW50UHJvdG9jb2xGb3JtYXR0ZXJcbiAgICAgIGNhc2UgJ2pzb24nOlxuICAgICAgICByZXR1cm4gSnNvbkZvcm1hdHRlclxuICAgICAgY2FzZSAncHJvZ3Jlc3MnOlxuICAgICAgICByZXR1cm4gUHJvZ3Jlc3NGb3JtYXR0ZXJcbiAgICAgIGNhc2UgJ3Byb2dyZXNzLWJhcic6XG4gICAgICAgIHJldHVybiBQcm9ncmVzc0JhckZvcm1hdHRlclxuICAgICAgY2FzZSAncmVydW4nOlxuICAgICAgICByZXR1cm4gUmVydW5Gb3JtYXR0ZXJcbiAgICAgIGNhc2UgJ3NuaXBwZXRzJzpcbiAgICAgICAgcmV0dXJuIFNuaXBwZXRzRm9ybWF0dGVyXG4gICAgICBjYXNlICdzdW1tYXJ5JzpcbiAgICAgICAgcmV0dXJuIFN1bW1hcnlGb3JtYXR0ZXJcbiAgICAgIGNhc2UgJ3VzYWdlJzpcbiAgICAgICAgcmV0dXJuIFVzYWdlRm9ybWF0dGVyXG4gICAgICBjYXNlICd1c2FnZS1qc29uJzpcbiAgICAgICAgcmV0dXJuIFVzYWdlSnNvbkZvcm1hdHRlclxuICAgICAgZGVmYXVsdDpcbiAgICAgICAgcmV0dXJuIEZvcm1hdHRlckJ1aWxkZXIubG9hZEN1c3RvbUZvcm1hdHRlcih0eXBlLCBvcHRpb25zKVxuICAgIH1cbiAgfVxuXG4gIHN0YXRpYyBnZXRTdGVwRGVmaW5pdGlvblNuaXBwZXRCdWlsZGVyKHtcbiAgICBjd2QsXG4gICAgc25pcHBldEludGVyZmFjZSxcbiAgICBzbmlwcGV0U3ludGF4LFxuICAgIHN1cHBvcnRDb2RlTGlicmFyeVxuICB9KSB7XG4gICAgaWYgKCFzbmlwcGV0SW50ZXJmYWNlKSB7XG4gICAgICBzbmlwcGV0SW50ZXJmYWNlID0gJ2NhbGxiYWNrJ1xuICAgIH1cbiAgICBsZXQgU3ludGF4ID0gSmF2YXNjcmlwdFNuaXBwZXRTeW50YXhcbiAgICBpZiAoc25pcHBldFN5bnRheCkge1xuICAgICAgY29uc3QgZnVsbFN5bnRheFBhdGggPSBwYXRoLnJlc29sdmUoY3dkLCBzbmlwcGV0U3ludGF4KVxuICAgICAgU3ludGF4ID0gcmVxdWlyZShmdWxsU3ludGF4UGF0aClcbiAgICB9XG4gICAgcmV0dXJuIG5ldyBTdGVwRGVmaW5pdGlvblNuaXBwZXRCdWlsZGVyKHtcbiAgICAgIHNuaXBwZXRTeW50YXg6IG5ldyBTeW50YXgoc25pcHBldEludGVyZmFjZSksXG4gICAgICBwYXJhbWV0ZXJUeXBlUmVnaXN0cnk6IHN1cHBvcnRDb2RlTGlicmFyeS5wYXJhbWV0ZXJUeXBlUmVnaXN0cnlcbiAgICB9KVxuICB9XG5cbiAgc3RhdGljIGxvYWRDdXN0b21Gb3JtYXR0ZXIoY3VzdG9tRm9ybWF0dGVyUGF0aCwgeyBjd2QgfSkge1xuICAgIGNvbnN0IGZ1bGxDdXN0b21Gb3JtYXR0ZXJQYXRoID0gcGF0aC5yZXNvbHZlKGN3ZCwgY3VzdG9tRm9ybWF0dGVyUGF0aClcbiAgICBjb25zdCBDdXN0b21Gb3JtYXR0ZXIgPSByZXF1aXJlKGZ1bGxDdXN0b21Gb3JtYXR0ZXJQYXRoKVxuICAgIGlmICh0eXBlb2YgQ3VzdG9tRm9ybWF0dGVyID09PSAnZnVuY3Rpb24nKSB7XG4gICAgICByZXR1cm4gQ3VzdG9tRm9ybWF0dGVyXG4gICAgfSBlbHNlIGlmIChcbiAgICAgIEN1c3RvbUZvcm1hdHRlciAmJlxuICAgICAgdHlwZW9mIEN1c3RvbUZvcm1hdHRlci5kZWZhdWx0ID09PSAnZnVuY3Rpb24nXG4gICAgKSB7XG4gICAgICByZXR1cm4gQ3VzdG9tRm9ybWF0dGVyLmRlZmF1bHRcbiAgICB9IGVsc2Uge1xuICAgICAgdGhyb3cgbmV3IEVycm9yKFxuICAgICAgICBgQ3VzdG9tIGZvcm1hdHRlciAoJHtjdXN0b21Gb3JtYXR0ZXJQYXRofSkgZG9lcyBub3QgZXhwb3J0IGEgZnVuY3Rpb25gXG4gICAgICApXG4gICAgfVxuICB9XG59XG4iXX0=