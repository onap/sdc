'use strict';

Object.defineProperty(exports, "__esModule", {
  value: true
});
exports.getLanguages = getLanguages;
exports.getKeywords = getKeywords;

var _lodash = require('lodash');

var _lodash2 = _interopRequireDefault(_lodash);

var _gherkin = require('gherkin');

var _gherkin2 = _interopRequireDefault(_gherkin);

var _cliTable = require('cli-table');

var _cliTable2 = _interopRequireDefault(_cliTable);

var _titleCase = require('title-case');

var _titleCase2 = _interopRequireDefault(_titleCase);

function _interopRequireDefault(obj) { return obj && obj.__esModule ? obj : { default: obj }; }

var keywords = ['feature', 'background', 'scenario', 'scenarioOutline', 'examples', 'given', 'when', 'then', 'and', 'but'];

function getAsTable(header, rows) {
  var table = new _cliTable2.default({
    chars: {
      bottom: '',
      'bottom-left': '',
      'bottom-mid': '',
      'bottom-right': '',
      left: '',
      'left-mid': '',
      mid: '',
      'mid-mid': '',
      middle: ' | ',
      right: '',
      'right-mid': '',
      top: '',
      'top-left': '',
      'top-mid': '',
      'top-right': ''
    },
    style: {
      border: [],
      'padding-left': 0,
      'padding-right': 0
    }
  });
  table.push(header);
  table.push.apply(table, rows);
  return table.toString();
}

function getLanguages() {
  var rows = _lodash2.default.map(_gherkin2.default.DIALECTS, function (data, isoCode) {
    return [isoCode, data.name, data['native']];
  });
  return getAsTable(['ISO 639-1', 'ENGLISH NAME', 'NATIVE NAME'], rows);
}

function getKeywords(isoCode) {
  var language = _gherkin2.default.DIALECTS[isoCode];
  var rows = _lodash2.default.map(keywords, function (keyword) {
    var words = _lodash2.default.map(language[keyword], function (s) {
      return '"' + s + '"';
    }).join(', ');
    return [(0, _titleCase2.default)(keyword), words];
  });
  return getAsTable(['ENGLISH KEYWORD', 'NATIVE KEYWORDS'], rows);
}
//# sourceMappingURL=data:application/json;charset=utf-8;base64,eyJ2ZXJzaW9uIjozLCJzb3VyY2VzIjpbIi4uLy4uL3NyYy9jbGkvaTE4bi5qcyJdLCJuYW1lcyI6WyJnZXRMYW5ndWFnZXMiLCJnZXRLZXl3b3JkcyIsImtleXdvcmRzIiwiZ2V0QXNUYWJsZSIsImhlYWRlciIsInJvd3MiLCJ0YWJsZSIsImNoYXJzIiwiYm90dG9tIiwibGVmdCIsIm1pZCIsIm1pZGRsZSIsInJpZ2h0IiwidG9wIiwic3R5bGUiLCJib3JkZXIiLCJwdXNoIiwiYXBwbHkiLCJ0b1N0cmluZyIsIm1hcCIsIkRJQUxFQ1RTIiwiZGF0YSIsImlzb0NvZGUiLCJuYW1lIiwibGFuZ3VhZ2UiLCJ3b3JkcyIsImtleXdvcmQiLCJzIiwiam9pbiJdLCJtYXBwaW5ncyI6Ijs7Ozs7UUFnRGdCQSxZLEdBQUFBLFk7UUFPQUMsVyxHQUFBQSxXOztBQXZEaEI7Ozs7QUFDQTs7OztBQUNBOzs7O0FBQ0E7Ozs7OztBQUVBLElBQU1DLFdBQVcsQ0FDZixTQURlLEVBRWYsWUFGZSxFQUdmLFVBSGUsRUFJZixpQkFKZSxFQUtmLFVBTGUsRUFNZixPQU5lLEVBT2YsTUFQZSxFQVFmLE1BUmUsRUFTZixLQVRlLEVBVWYsS0FWZSxDQUFqQjs7QUFhQSxTQUFTQyxVQUFULENBQW9CQyxNQUFwQixFQUE0QkMsSUFBNUIsRUFBa0M7QUFDaEMsTUFBTUMsUUFBUSx1QkFBVTtBQUN0QkMsV0FBTztBQUNMQyxjQUFRLEVBREg7QUFFTCxxQkFBZSxFQUZWO0FBR0wsb0JBQWMsRUFIVDtBQUlMLHNCQUFnQixFQUpYO0FBS0xDLFlBQU0sRUFMRDtBQU1MLGtCQUFZLEVBTlA7QUFPTEMsV0FBSyxFQVBBO0FBUUwsaUJBQVcsRUFSTjtBQVNMQyxjQUFRLEtBVEg7QUFVTEMsYUFBTyxFQVZGO0FBV0wsbUJBQWEsRUFYUjtBQVlMQyxXQUFLLEVBWkE7QUFhTCxrQkFBWSxFQWJQO0FBY0wsaUJBQVcsRUFkTjtBQWVMLG1CQUFhO0FBZlIsS0FEZTtBQWtCdEJDLFdBQU87QUFDTEMsY0FBUSxFQURIO0FBRUwsc0JBQWdCLENBRlg7QUFHTCx1QkFBaUI7QUFIWjtBQWxCZSxHQUFWLENBQWQ7QUF3QkFULFFBQU1VLElBQU4sQ0FBV1osTUFBWDtBQUNBRSxRQUFNVSxJQUFOLENBQVdDLEtBQVgsQ0FBaUJYLEtBQWpCLEVBQXdCRCxJQUF4QjtBQUNBLFNBQU9DLE1BQU1ZLFFBQU4sRUFBUDtBQUNEOztBQUVNLFNBQVNsQixZQUFULEdBQXdCO0FBQzdCLE1BQU1LLE9BQU8saUJBQUVjLEdBQUYsQ0FBTSxrQkFBUUMsUUFBZCxFQUF3QixVQUFDQyxJQUFELEVBQU9DLE9BQVAsRUFBbUI7QUFDdEQsV0FBTyxDQUFDQSxPQUFELEVBQVVELEtBQUtFLElBQWYsRUFBcUJGLEtBQUssUUFBTCxDQUFyQixDQUFQO0FBQ0QsR0FGWSxDQUFiO0FBR0EsU0FBT2xCLFdBQVcsQ0FBQyxXQUFELEVBQWMsY0FBZCxFQUE4QixhQUE5QixDQUFYLEVBQXlERSxJQUF6RCxDQUFQO0FBQ0Q7O0FBRU0sU0FBU0osV0FBVCxDQUFxQnFCLE9BQXJCLEVBQThCO0FBQ25DLE1BQU1FLFdBQVcsa0JBQVFKLFFBQVIsQ0FBaUJFLE9BQWpCLENBQWpCO0FBQ0EsTUFBTWpCLE9BQU8saUJBQUVjLEdBQUYsQ0FBTWpCLFFBQU4sRUFBZ0IsbUJBQVc7QUFDdEMsUUFBTXVCLFFBQVEsaUJBQUVOLEdBQUYsQ0FBTUssU0FBU0UsT0FBVCxDQUFOLEVBQXlCO0FBQUEsbUJBQVNDLENBQVQ7QUFBQSxLQUF6QixFQUF3Q0MsSUFBeEMsQ0FBNkMsSUFBN0MsQ0FBZDtBQUNBLFdBQU8sQ0FBQyx5QkFBVUYsT0FBVixDQUFELEVBQXFCRCxLQUFyQixDQUFQO0FBQ0QsR0FIWSxDQUFiO0FBSUEsU0FBT3RCLFdBQVcsQ0FBQyxpQkFBRCxFQUFvQixpQkFBcEIsQ0FBWCxFQUFtREUsSUFBbkQsQ0FBUDtBQUNEIiwiZmlsZSI6ImkxOG4uanMiLCJzb3VyY2VzQ29udGVudCI6WyJpbXBvcnQgXyBmcm9tICdsb2Rhc2gnXG5pbXBvcnQgR2hlcmtpbiBmcm9tICdnaGVya2luJ1xuaW1wb3J0IFRhYmxlIGZyb20gJ2NsaS10YWJsZSdcbmltcG9ydCB0aXRsZUNhc2UgZnJvbSAndGl0bGUtY2FzZSdcblxuY29uc3Qga2V5d29yZHMgPSBbXG4gICdmZWF0dXJlJyxcbiAgJ2JhY2tncm91bmQnLFxuICAnc2NlbmFyaW8nLFxuICAnc2NlbmFyaW9PdXRsaW5lJyxcbiAgJ2V4YW1wbGVzJyxcbiAgJ2dpdmVuJyxcbiAgJ3doZW4nLFxuICAndGhlbicsXG4gICdhbmQnLFxuICAnYnV0J1xuXVxuXG5mdW5jdGlvbiBnZXRBc1RhYmxlKGhlYWRlciwgcm93cykge1xuICBjb25zdCB0YWJsZSA9IG5ldyBUYWJsZSh7XG4gICAgY2hhcnM6IHtcbiAgICAgIGJvdHRvbTogJycsXG4gICAgICAnYm90dG9tLWxlZnQnOiAnJyxcbiAgICAgICdib3R0b20tbWlkJzogJycsXG4gICAgICAnYm90dG9tLXJpZ2h0JzogJycsXG4gICAgICBsZWZ0OiAnJyxcbiAgICAgICdsZWZ0LW1pZCc6ICcnLFxuICAgICAgbWlkOiAnJyxcbiAgICAgICdtaWQtbWlkJzogJycsXG4gICAgICBtaWRkbGU6ICcgfCAnLFxuICAgICAgcmlnaHQ6ICcnLFxuICAgICAgJ3JpZ2h0LW1pZCc6ICcnLFxuICAgICAgdG9wOiAnJyxcbiAgICAgICd0b3AtbGVmdCc6ICcnLFxuICAgICAgJ3RvcC1taWQnOiAnJyxcbiAgICAgICd0b3AtcmlnaHQnOiAnJ1xuICAgIH0sXG4gICAgc3R5bGU6IHtcbiAgICAgIGJvcmRlcjogW10sXG4gICAgICAncGFkZGluZy1sZWZ0JzogMCxcbiAgICAgICdwYWRkaW5nLXJpZ2h0JzogMFxuICAgIH1cbiAgfSlcbiAgdGFibGUucHVzaChoZWFkZXIpXG4gIHRhYmxlLnB1c2guYXBwbHkodGFibGUsIHJvd3MpXG4gIHJldHVybiB0YWJsZS50b1N0cmluZygpXG59XG5cbmV4cG9ydCBmdW5jdGlvbiBnZXRMYW5ndWFnZXMoKSB7XG4gIGNvbnN0IHJvd3MgPSBfLm1hcChHaGVya2luLkRJQUxFQ1RTLCAoZGF0YSwgaXNvQ29kZSkgPT4ge1xuICAgIHJldHVybiBbaXNvQ29kZSwgZGF0YS5uYW1lLCBkYXRhWyduYXRpdmUnXV1cbiAgfSlcbiAgcmV0dXJuIGdldEFzVGFibGUoWydJU08gNjM5LTEnLCAnRU5HTElTSCBOQU1FJywgJ05BVElWRSBOQU1FJ10sIHJvd3MpXG59XG5cbmV4cG9ydCBmdW5jdGlvbiBnZXRLZXl3b3Jkcyhpc29Db2RlKSB7XG4gIGNvbnN0IGxhbmd1YWdlID0gR2hlcmtpbi5ESUFMRUNUU1tpc29Db2RlXVxuICBjb25zdCByb3dzID0gXy5tYXAoa2V5d29yZHMsIGtleXdvcmQgPT4ge1xuICAgIGNvbnN0IHdvcmRzID0gXy5tYXAobGFuZ3VhZ2Vba2V5d29yZF0sIHMgPT4gYFwiJHtzfVwiYCkuam9pbignLCAnKVxuICAgIHJldHVybiBbdGl0bGVDYXNlKGtleXdvcmQpLCB3b3Jkc11cbiAgfSlcbiAgcmV0dXJuIGdldEFzVGFibGUoWydFTkdMSVNIIEtFWVdPUkQnLCAnTkFUSVZFIEtFWVdPUkRTJ10sIHJvd3MpXG59XG4iXX0=