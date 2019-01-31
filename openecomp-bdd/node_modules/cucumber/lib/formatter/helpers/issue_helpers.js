'use strict';

Object.defineProperty(exports, "__esModule", {
  value: true
});

var _defineProperty2 = require('babel-runtime/helpers/defineProperty');

var _defineProperty3 = _interopRequireDefault(_defineProperty2);

var _CHARACTERS, _IS_ISSUE;

exports.isIssue = isIssue;
exports.formatIssue = formatIssue;

var _lodash = require('lodash');

var _lodash2 = _interopRequireDefault(_lodash);

var _location_helpers = require('./location_helpers');

var _step_result_helpers = require('./step_result_helpers');

var _indentString = require('indent-string');

var _indentString2 = _interopRequireDefault(_indentString);

var _status = require('../../status');

var _status2 = _interopRequireDefault(_status);

var _figures = require('figures');

var _figures2 = _interopRequireDefault(_figures);

var _cliTable = require('cli-table');

var _cliTable2 = _interopRequireDefault(_cliTable);

var _keyword_type = require('./keyword_type');

var _keyword_type2 = _interopRequireDefault(_keyword_type);

var _step_arguments = require('../../step_arguments');

var _gherkin_document_parser = require('./gherkin_document_parser');

var _pickle_parser = require('./pickle_parser');

function _interopRequireDefault(obj) { return obj && obj.__esModule ? obj : { default: obj }; }

var CHARACTERS = (_CHARACTERS = {}, (0, _defineProperty3.default)(_CHARACTERS, _status2.default.AMBIGUOUS, _figures2.default.cross), (0, _defineProperty3.default)(_CHARACTERS, _status2.default.FAILED, _figures2.default.cross), (0, _defineProperty3.default)(_CHARACTERS, _status2.default.PASSED, _figures2.default.tick), (0, _defineProperty3.default)(_CHARACTERS, _status2.default.PENDING, '?'), (0, _defineProperty3.default)(_CHARACTERS, _status2.default.SKIPPED, '-'), (0, _defineProperty3.default)(_CHARACTERS, _status2.default.UNDEFINED, '?'), _CHARACTERS);

var IS_ISSUE = (_IS_ISSUE = {}, (0, _defineProperty3.default)(_IS_ISSUE, _status2.default.AMBIGUOUS, true), (0, _defineProperty3.default)(_IS_ISSUE, _status2.default.FAILED, true), (0, _defineProperty3.default)(_IS_ISSUE, _status2.default.PASSED, false), (0, _defineProperty3.default)(_IS_ISSUE, _status2.default.PENDING, true), (0, _defineProperty3.default)(_IS_ISSUE, _status2.default.SKIPPED, false), (0, _defineProperty3.default)(_IS_ISSUE, _status2.default.UNDEFINED, true), _IS_ISSUE);

function formatDataTable(arg) {
  var rows = arg.rows.map(function (row) {
    return row.cells.map(function (cell) {
      return cell.value.replace(/\\/g, '\\\\').replace(/\n/g, '\\n');
    });
  });
  var table = new _cliTable2.default({
    chars: {
      bottom: '',
      'bottom-left': '',
      'bottom-mid': '',
      'bottom-right': '',
      left: '|',
      'left-mid': '',
      mid: '',
      'mid-mid': '',
      middle: '|',
      right: '|',
      'right-mid': '',
      top: '',
      'top-left': '',
      'top-mid': '',
      'top-right': ''
    },
    style: {
      border: [],
      'padding-left': 1,
      'padding-right': 1
    }
  });
  table.push.apply(table, rows);
  return table.toString();
}

function formatDocString(arg) {
  return '"""\n' + arg.content + '\n"""';
}

function formatStep(_ref) {
  var colorFns = _ref.colorFns,
      isBeforeHook = _ref.isBeforeHook,
      keyword = _ref.keyword,
      keywordType = _ref.keywordType,
      pickleStep = _ref.pickleStep,
      snippetBuilder = _ref.snippetBuilder,
      testStep = _ref.testStep;
  var status = testStep.result.status;

  var colorFn = colorFns[status];

  var identifier = void 0;
  if (testStep.sourceLocation) {
    identifier = keyword + (pickleStep.text || '');
  } else {
    identifier = isBeforeHook ? 'Before' : 'After';
  }

  var text = colorFn(CHARACTERS[status] + ' ' + identifier);

  var actionLocation = testStep.actionLocation;

  if (actionLocation) {
    text += ' # ' + colorFns.location((0, _location_helpers.formatLocation)(actionLocation));
  }
  text += '\n';

  if (pickleStep) {
    var str = void 0;
    var iterator = (0, _step_arguments.buildStepArgumentIterator)({
      dataTable: function dataTable(arg) {
        return str = formatDataTable(arg);
      },
      docString: function docString(arg) {
        return str = formatDocString(arg);
      }
    });
    _lodash2.default.each(pickleStep.arguments, iterator);
    if (str) {
      text += (0, _indentString2.default)(colorFn(str) + '\n', 4);
    }
  }
  var message = (0, _step_result_helpers.getStepMessage)({
    colorFns: colorFns,
    keywordType: keywordType,
    pickleStep: pickleStep,
    snippetBuilder: snippetBuilder,
    testStep: testStep
  });
  if (message) {
    text += (0, _indentString2.default)(message, 4) + '\n';
  }
  return text;
}

function isIssue(status) {
  return IS_ISSUE[status];
}

function formatIssue(_ref2) {
  var colorFns = _ref2.colorFns,
      gherkinDocument = _ref2.gherkinDocument,
      number = _ref2.number,
      pickle = _ref2.pickle,
      snippetBuilder = _ref2.snippetBuilder,
      testCase = _ref2.testCase;

  var prefix = number + ') ';
  var text = prefix;
  var scenarioLocation = (0, _location_helpers.formatLocation)(testCase.sourceLocation);
  text += 'Scenario: ' + pickle.name + ' # ' + colorFns.location(scenarioLocation) + '\n';
  var stepLineToKeywordMap = (0, _gherkin_document_parser.getStepLineToKeywordMap)(gherkinDocument);
  var stepLineToPickledStepMap = (0, _pickle_parser.getStepLineToPickledStepMap)(pickle);
  var isBeforeHook = true;
  var previousKeywordType = _keyword_type2.default.PRECONDITION;
  _lodash2.default.each(testCase.steps, function (testStep) {
    isBeforeHook = isBeforeHook && !testStep.sourceLocation;
    var keyword = void 0,
        keywordType = void 0,
        pickleStep = void 0;
    if (testStep.sourceLocation) {
      pickleStep = stepLineToPickledStepMap[testStep.sourceLocation.line];
      keyword = (0, _pickle_parser.getStepKeyword)({ pickleStep: pickleStep, stepLineToKeywordMap: stepLineToKeywordMap });
      keywordType = (0, _keyword_type.getStepKeywordType)({
        keyword: keyword,
        language: gherkinDocument.feature.language,
        previousKeywordType: previousKeywordType
      });
    }
    var formattedStep = formatStep({
      colorFns: colorFns,
      isBeforeHook: isBeforeHook,
      keyword: keyword,
      keywordType: keywordType,
      pickleStep: pickleStep,
      snippetBuilder: snippetBuilder,
      testStep: testStep
    });
    text += (0, _indentString2.default)(formattedStep, prefix.length);
    previousKeywordType = keywordType;
  });
  return text + '\n';
}
//# sourceMappingURL=data:application/json;charset=utf-8;base64,eyJ2ZXJzaW9uIjozLCJzb3VyY2VzIjpbIi4uLy4uLy4uL3NyYy9mb3JtYXR0ZXIvaGVscGVycy9pc3N1ZV9oZWxwZXJzLmpzIl0sIm5hbWVzIjpbImlzSXNzdWUiLCJmb3JtYXRJc3N1ZSIsIkNIQVJBQ1RFUlMiLCJBTUJJR1VPVVMiLCJjcm9zcyIsIkZBSUxFRCIsIlBBU1NFRCIsInRpY2siLCJQRU5ESU5HIiwiU0tJUFBFRCIsIlVOREVGSU5FRCIsIklTX0lTU1VFIiwiZm9ybWF0RGF0YVRhYmxlIiwiYXJnIiwicm93cyIsIm1hcCIsInJvdyIsImNlbGxzIiwiY2VsbCIsInZhbHVlIiwicmVwbGFjZSIsInRhYmxlIiwiY2hhcnMiLCJib3R0b20iLCJsZWZ0IiwibWlkIiwibWlkZGxlIiwicmlnaHQiLCJ0b3AiLCJzdHlsZSIsImJvcmRlciIsInB1c2giLCJhcHBseSIsInRvU3RyaW5nIiwiZm9ybWF0RG9jU3RyaW5nIiwiY29udGVudCIsImZvcm1hdFN0ZXAiLCJjb2xvckZucyIsImlzQmVmb3JlSG9vayIsImtleXdvcmQiLCJrZXl3b3JkVHlwZSIsInBpY2tsZVN0ZXAiLCJzbmlwcGV0QnVpbGRlciIsInRlc3RTdGVwIiwic3RhdHVzIiwicmVzdWx0IiwiY29sb3JGbiIsImlkZW50aWZpZXIiLCJzb3VyY2VMb2NhdGlvbiIsInRleHQiLCJhY3Rpb25Mb2NhdGlvbiIsImxvY2F0aW9uIiwic3RyIiwiaXRlcmF0b3IiLCJkYXRhVGFibGUiLCJkb2NTdHJpbmciLCJlYWNoIiwiYXJndW1lbnRzIiwibWVzc2FnZSIsImdoZXJraW5Eb2N1bWVudCIsIm51bWJlciIsInBpY2tsZSIsInRlc3RDYXNlIiwicHJlZml4Iiwic2NlbmFyaW9Mb2NhdGlvbiIsIm5hbWUiLCJzdGVwTGluZVRvS2V5d29yZE1hcCIsInN0ZXBMaW5lVG9QaWNrbGVkU3RlcE1hcCIsInByZXZpb3VzS2V5d29yZFR5cGUiLCJQUkVDT05ESVRJT04iLCJzdGVwcyIsImxpbmUiLCJsYW5ndWFnZSIsImZlYXR1cmUiLCJmb3JtYXR0ZWRT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QVArQixLQUFYLENBQXRCO0FBU0FNLFlBQVEsNEJBQWF5QixhQUFiLEVBQTRCWCxPQUFPWSxNQUFuQyxDQUFSO0FBQ0FQLDBCQUFzQjVCLFdBQXRCO0FBQ0QsR0F2QkQ7QUF3QkEsU0FBT1MsT0FBTyxJQUFkO0FBQ0QiLCJmaWxlIjoiaXNzdWVfaGVscGVycy5qcyIsInNvdXJjZXNDb250ZW50IjpbImltcG9ydCBfIGZyb20gJ2xvZGFzaCdcbmltcG9ydCB7IGZvcm1hdExvY2F0aW9uIH0gZnJvbSAnLi9sb2NhdGlvbl9oZWxwZXJzJ1xuaW1wb3J0IHsgZ2V0U3RlcE1lc3NhZ2UgfSBmcm9tICcuL3N0ZXBfcmVzdWx0X2hlbHBlcnMnXG5pbXBvcnQgaW5kZW50U3RyaW5nIGZyb20gJ2luZGVudC1zdHJpbmcnXG5pbXBvcnQgU3RhdHVzIGZyb20gJy4uLy4uL3N0YXR1cydcbmltcG9ydCBmaWd1cmVzIGZyb20gJ2ZpZ3VyZXMnXG5pbXBvcnQgVGFibGUgZnJvbSAnY2xpLXRhYmxlJ1xuaW1wb3J0IEtleXdvcmRUeXBlLCB7IGdldFN0ZXBLZXl3b3JkVHlwZSB9IGZyb20gJy4va2V5d29yZF90eXBlJ1xuaW1wb3J0IHsgYnVpbGRTdGVwQXJndW1lbnRJdGVyYXRvciB9IGZyb20gJy4uLy4uL3N0ZXBfYXJndW1lbnRzJ1xuaW1wb3J0IHsgZ2V0U3RlcExpbmVUb0tleXdvcmRNYXAgfSBmcm9tICcuL2doZXJraW5fZG9jdW1lbnRfcGFyc2VyJ1xuaW1wb3J0IHsgZ2V0U3RlcExpbmVUb1BpY2tsZWRTdGVwTWFwLCBnZXRTdGVwS2V5d29yZCB9IGZyb20gJy4vcGlja2xlX3BhcnNlcidcblxuY29uc3QgQ0hBUkFDVEVSUyA9IHtcbiAgW1N0YXR1cy5BTUJJR1VPVVNdOiBmaWd1cmVzLmNyb3NzLFxuICBbU3RhdHVzLkZBSUxFRF06IGZpZ3VyZXMuY3Jvc3MsXG4gIFtTdGF0dXMuUEFTU0VEXTogZmlndXJlcy50aWNrLFxuICBbU3RhdHVzLlBFTkRJTkddOiAnPycsXG4gIFtTdGF0dXMuU0tJUFBFRF06ICctJyxcbiAgW1N0YXR1cy5VTkRFRklORURdOiAnPydcbn1cblxuY29uc3QgSVNfSVNTVUUgPSB7XG4gIFtTdGF0dXMuQU1CSUdVT1VTXTogdHJ1ZSxcbiAgW1N0YXR1cy5GQUlMRURdOiB0cnVlLFxuICBbU3RhdHVzLlBBU1NFRF06IGZhbHNlLFxuICBbU3RhdHVzLlBFTkRJTkddOiB0cnVlLFxuICBbU3RhdHVzLlNLSVBQRURdOiBmYWxzZSxcbiAgW1N0YXR1cy5VTkRFRklORURdOiB0cnVlXG59XG5cbmZ1bmN0aW9uIGZvcm1hdERhdGFUYWJsZShhcmcpIHtcbiAgY29uc3Qgcm93cyA9IGFyZy5yb3dzLm1hcChyb3cgPT4ge1xuICAgIHJldHVybiByb3cuY2VsbHMubWFwKGNlbGwgPT4ge1xuICAgICAgcmV0dXJuIGNlbGwudmFsdWUucmVwbGFjZSgvXFxcXC9nLCAnXFxcXFxcXFwnKS5yZXBsYWNlKC9cXG4vZywgJ1xcXFxuJylcbiAgICB9KVxuICB9KVxuICBjb25zdCB0YWJsZSA9IG5ldyBUYWJsZSh7XG4gICAgY2hhcnM6IHtcbiAgICAgIGJvdHRvbTogJycsXG4gICAgICAnYm90dG9tLWxlZnQnOiAnJyxcbiAgICAgICdib3R0b20tbWlkJzogJycsXG4gICAgICAnYm90dG9tLXJpZ2h0JzogJycsXG4gICAgICBsZWZ0OiAnfCcsXG4gICAgICAnbGVmdC1taWQnOiAnJyxcbiAgICAgIG1pZDogJycsXG4gICAgICAnbWlkLW1pZCc6ICcnLFxuICAgICAgbWlkZGxlOiAnfCcsXG4gICAgICByaWdodDogJ3wnLFxuICAgICAgJ3JpZ2h0LW1pZCc6ICcnLFxuICAgICAgdG9wOiAnJyxcbiAgICAgICd0b3AtbGVmdCc6ICcnLFxuICAgICAgJ3RvcC1taWQnOiAnJyxcbiAgICAgICd0b3AtcmlnaHQnOiAnJ1xuICAgIH0sXG4gICAgc3R5bGU6IHtcbiAgICAgIGJvcmRlcjogW10sXG4gICAgICAncGFkZGluZy1sZWZ0JzogMSxcbiAgICAgICdwYWRkaW5nLXJpZ2h0JzogMVxuICAgIH1cbiAgfSlcbiAgdGFibGUucHVzaC5hcHBseSh0YWJsZSwgcm93cylcbiAgcmV0dXJuIHRhYmxlLnRvU3RyaW5nKClcbn1cblxuZnVuY3Rpb24gZm9ybWF0RG9jU3RyaW5nKGFyZykge1xuICByZXR1cm4gJ1wiXCJcIlxcbicgKyBhcmcuY29udGVudCArICdcXG5cIlwiXCInXG59XG5cbmZ1bmN0aW9uIGZvcm1hdFN0ZXAoe1xuICBjb2xvckZucyxcbiAgaXNCZWZvcmVIb29rLFxuICBrZXl3b3JkLFxuICBrZXl3b3JkVHlwZSxcbiAgcGlja2xlU3RlcCxcbiAgc25pcHBldEJ1aWxkZXIsXG4gIHRlc3RTdGVwXG59KSB7XG4gIGNvbnN0IHsgc3RhdHVzIH0gPSB0ZXN0U3RlcC5yZXN1bHRcbiAgY29uc3QgY29sb3JGbiA9IGNvbG9yRm5zW3N0YXR1c11cblxuICBsZXQgaWRlbnRpZmllclxuICBpZiAodGVzdFN0ZXAuc291cmNlTG9jYXRpb24pIHtcbiAgICBpZGVudGlmaWVyID0ga2V5d29yZCArIChwaWNrbGVTdGVwLnRleHQgfHwgJycpXG4gIH0gZWxzZSB7XG4gICAgaWRlbnRpZmllciA9IGlzQmVmb3JlSG9vayA/ICdCZWZvcmUnIDogJ0FmdGVyJ1xuICB9XG5cbiAgbGV0IHRleHQgPSBjb2xvckZuKENIQVJBQ1RFUlNbc3RhdHVzXSArICcgJyArIGlkZW50aWZpZXIpXG5cbiAgY29uc3QgeyBhY3Rpb25Mb2NhdGlvbiB9ID0gdGVzdFN0ZXBcbiAgaWYgKGFjdGlvbkxvY2F0aW9uKSB7XG4gICAgdGV4dCArPSAnICMgJyArIGNvbG9yRm5zLmxvY2F0aW9uKGZvcm1hdExvY2F0aW9uKGFjdGlvbkxvY2F0aW9uKSlcbiAgfVxuICB0ZXh0ICs9ICdcXG4nXG5cbiAgaWYgKHBpY2tsZVN0ZXApIHtcbiAgICBsZXQgc3RyXG4gICAgY29uc3QgaXRlcmF0b3IgPSBidWlsZFN0ZXBBcmd1bWVudEl0ZXJhdG9yKHtcbiAgICAgIGRhdGFUYWJsZTogYXJnID0+IChzdHIgPSBmb3JtYXREYXRhVGFibGUoYXJnKSksXG4gICAgICBkb2NTdHJpbmc6IGFyZyA9PiAoc3RyID0gZm9ybWF0RG9jU3RyaW5nKGFyZykpXG4gICAgfSlcbiAgICBfLmVhY2gocGlja2xlU3RlcC5hcmd1bWVudHMsIGl0ZXJhdG9yKVxuICAgIGlmIChzdHIpIHtcbiAgICAgIHRleHQgKz0gaW5kZW50U3RyaW5nKGNvbG9yRm4oc3RyKSArICdcXG4nLCA0KVxuICAgIH1cbiAgfVxuICBjb25zdCBtZXNzYWdlID0gZ2V0U3RlcE1lc3NhZ2Uoe1xuICAgIGNvbG9yRm5zLFxuICAgIGtleXdvcmRUeXBlLFxuICAgIHBpY2tsZVN0ZXAsXG4gICAgc25pcHBldEJ1aWxkZXIsXG4gICAgdGVzdFN0ZXBcbiAgfSlcbiAgaWYgKG1lc3NhZ2UpIHtcbiAgICB0ZXh0ICs9IGluZGVudFN0cmluZyhtZXNzYWdlLCA0KSArICdcXG4nXG4gIH1cbiAgcmV0dXJuIHRleHRcbn1cblxuZXhwb3J0IGZ1bmN0aW9uIGlzSXNzdWUoc3RhdHVzKSB7XG4gIHJldHVybiBJU19JU1NVRVtzdGF0dXNdXG59XG5cbmV4cG9ydCBmdW5jdGlvbiBmb3JtYXRJc3N1ZSh7XG4gIGNvbG9yRm5zLFxuICBnaGVya2luRG9jdW1lbnQsXG4gIG51bWJlcixcbiAgcGlja2xlLFxuICBzbmlwcGV0QnVpbGRlcixcbiAgdGVzdENhc2Vcbn0pIHtcbiAgY29uc3QgcHJlZml4ID0gbnVtYmVyICsgJykgJ1xuICBsZXQgdGV4dCA9IHByZWZpeFxuICBjb25zdCBzY2VuYXJpb0xvY2F0aW9uID0gZm9ybWF0TG9jYXRpb24odGVzdENhc2Uuc291cmNlTG9jYXRpb24pXG4gIHRleHQgKz1cbiAgICAnU2NlbmFyaW86ICcgK1xuICAgIHBpY2tsZS5uYW1lICtcbiAgICAnICMgJyArXG4gICAgY29sb3JGbnMubG9jYXRpb24oc2NlbmFyaW9Mb2NhdGlvbikgK1xuICAgICdcXG4nXG4gIGNvbnN0IHN0ZXBMaW5lVG9LZXl3b3JkTWFwID0gZ2V0U3RlcExpbmVUb0tleXdvcmRNYXAoZ2hlcmtpbkRvY3VtZW50KVxuICBjb25zdCBzdGVwTGluZVRvUGlja2xlZFN0ZXBNYXAgPSBnZXRTdGVwTGluZVRvUGlja2xlZFN0ZXBNYXAocGlja2xlKVxuICBsZXQgaXNCZWZvcmVIb29rID0gdHJ1ZVxuICBsZXQgcHJldmlvdXNLZXl3b3JkVHlwZSA9IEtleXdvcmRUeXBlLlBSRUNPTkRJVElPTlxuICBfLmVhY2godGVzdENhc2Uuc3RlcHMsIHRlc3RTdGVwID0+IHtcbiAgICBpc0JlZm9yZUhvb2sgPSBpc0JlZm9yZUhvb2sgJiYgIXRlc3RTdGVwLnNvdXJjZUxvY2F0aW9uXG4gICAgbGV0IGtleXdvcmQsIGtleXdvcmRUeXBlLCBwaWNrbGVTdGVwXG4gICAgaWYgKHRlc3RTdGVwLnNvdXJjZUxvY2F0aW9uKSB7XG4gICAgICBwaWNrbGVTdGVwID0gc3RlcExpbmVUb1BpY2tsZWRTdGVwTWFwW3Rlc3RTdGVwLnNvdXJjZUxvY2F0aW9uLmxpbmVdXG4gICAgICBrZXl3b3JkID0gZ2V0U3RlcEtleXdvcmQoeyBwaWNrbGVTdGVwLCBzdGVwTGluZVRvS2V5d29yZE1hcCB9KVxuICAgICAga2V5d29yZFR5cGUgPSBnZXRTdGVwS2V5d29yZFR5cGUoe1xuICAgICAgICBrZXl3b3JkLFxuICAgICAgICBsYW5ndWFnZTogZ2hlcmtpbkRvY3VtZW50LmZlYXR1cmUubGFuZ3VhZ2UsXG4gICAgICAgIHByZXZpb3VzS2V5d29yZFR5cGVcbiAgICAgIH0pXG4gICAgfVxuICAgIGNvbnN0IGZvcm1hdHRlZFN0ZXAgPSBmb3JtYXRTdGVwKHtcbiAgICAgIGNvbG9yRm5zLFxuICAgICAgaXNCZWZvcmVIb29rLFxuICAgICAga2V5d29yZCxcbiAgICAgIGtleXdvcmRUeXBlLFxuICAgICAgcGlja2xlU3RlcCxcbiAgICAgIHNuaXBwZXRCdWlsZGVyLFxuICAgICAgdGVzdFN0ZXBcbiAgICB9KVxuICAgIHRleHQgKz0gaW5kZW50U3RyaW5nKGZvcm1hdHRlZFN0ZXAsIHByZWZpeC5sZW5ndGgpXG4gICAgcHJldmlvdXNLZXl3b3JkVHlwZSA9IGtleXdvcmRUeXBlXG4gIH0pXG4gIHJldHVybiB0ZXh0ICsgJ1xcbidcbn1cbiJdfQ==