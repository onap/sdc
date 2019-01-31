'use strict';

Object.defineProperty(exports, "__esModule", {
  value: true
});

var _classCallCheck2 = require('babel-runtime/helpers/classCallCheck');

var _classCallCheck3 = _interopRequireDefault(_classCallCheck2);

var _createClass2 = require('babel-runtime/helpers/createClass');

var _createClass3 = _interopRequireDefault(_createClass2);

var _possibleConstructorReturn2 = require('babel-runtime/helpers/possibleConstructorReturn');

var _possibleConstructorReturn3 = _interopRequireDefault(_possibleConstructorReturn2);

var _inherits2 = require('babel-runtime/helpers/inherits');

var _inherits3 = _interopRequireDefault(_inherits2);

var _lodash = require('lodash');

var _lodash2 = _interopRequireDefault(_lodash);

var _helpers = require('./helpers');

var _2 = require('./');

var _3 = _interopRequireDefault(_2);

var _cliTable = require('cli-table');

var _cliTable2 = _interopRequireDefault(_cliTable);

function _interopRequireDefault(obj) { return obj && obj.__esModule ? obj : { default: obj }; }

var UsageFormatter = function (_Formatter) {
  (0, _inherits3.default)(UsageFormatter, _Formatter);

  function UsageFormatter(options) {
    (0, _classCallCheck3.default)(this, UsageFormatter);

    var _this = (0, _possibleConstructorReturn3.default)(this, (UsageFormatter.__proto__ || Object.getPrototypeOf(UsageFormatter)).call(this, options));

    options.eventBroadcaster.on('test-run-finished', _this.logUsage.bind(_this));
    return _this;
  }

  (0, _createClass3.default)(UsageFormatter, [{
    key: 'logUsage',
    value: function logUsage() {
      var usage = (0, _helpers.getUsage)({
        stepDefinitions: this.supportCodeLibrary.stepDefinitions,
        eventDataCollector: this.eventDataCollector
      });
      if (usage.length === 0) {
        this.log('No step definitions');
        return;
      }
      var table = new _cliTable2.default({
        head: ['Pattern / Text', 'Duration', 'Location'],
        style: {
          border: [],
          head: []
        }
      });
      usage.forEach(function (_ref) {
        var line = _ref.line,
            matches = _ref.matches,
            meanDuration = _ref.meanDuration,
            pattern = _ref.pattern,
            uri = _ref.uri;

        var col1 = [pattern.toString()];
        var col2 = [];
        if (matches.length > 0) {
          if (isFinite(meanDuration)) {
            col2.push(parseFloat(meanDuration.toFixed(2)) + 'ms');
          } else {
            col2.push('-');
          }
        } else {
          col2.push('UNUSED');
        }
        var col3 = [(0, _helpers.formatLocation)({ line: line, uri: uri })];
        _lodash2.default.take(matches, 5).forEach(function (match) {
          col1.push('  ' + match.text);
          if (isFinite(match.duration)) {
            col2.push(match.duration + 'ms');
          } else {
            col2.push('-');
          }
          col3.push((0, _helpers.formatLocation)(match));
        });
        if (matches.length > 5) {
          col1.push('  ' + (matches.length - 5) + ' more');
        }
        table.push([col1.join('\n'), col2.join('\n'), col3.join('\n')]);
      });
      this.log(table.toString() + '\n');
    }
  }]);
  return UsageFormatter;
}(_3.default);

exports.default = UsageFormatter;
//# sourceMappingURL=data:application/json;charset=utf-8;base64,eyJ2ZXJzaW9uIjozLCJzb3VyY2VzIjpbIi4uLy4uL3NyYy9mb3JtYXR0ZXIvdXNhZ2VfZm9ybWF0dGVyLmpzIl0sIm5hbWVzIjpbIlVzYWdlRm9ybWF0dGVyIiwib3B0aW9ucyIsImV2ZW50QnJvYWRjYXN0ZXIiLCJvbiIsImxvZ1VzYWdlIiwidXNhZ2UiLCJzdGVwRGVmaW5pdGlvbnMiLCJzdXBwb3J0Q29kZUxpYnJhcnkiLCJldmVudERhdGFDb2xsZWN0b3IiLCJsZW5ndGgiLCJsb2ciLCJ0YWJsZSIsImhlYWQiLCJzdHlsZSIsImJvcmRlciIsImZvckVhY2giLCJsaW5lIiwibWF0Y2hlcyIsIm1lYW5EdXJhdGlvbiIsInBhdHRlcm4iLCJ1cmkiLCJjb2wxIiwidG9TdHJpbmciLCJjb2wyIiwiaXNGaW5pdGUiLCJwdXNoIiwicGFyc2VGbG9hdCIsInRvRml4ZWQiLCJjb2wzIiwidGFrZSIsIm1hdGNoIiwidGV4dCIsImR1cmF0aW9uIiwiam9pbiJdLCJtYXBwaW5ncyI6Ijs7Ozs7Ozs7Ozs7Ozs7Ozs7Ozs7OztBQUFBOzs7O0FBQ0E7O0FBQ0E7Ozs7QUFDQTs7Ozs7O0lBRXFCQSxjOzs7QUFDbkIsMEJBQVlDLE9BQVosRUFBcUI7QUFBQTs7QUFBQSw4SUFDYkEsT0FEYTs7QUFFbkJBLFlBQVFDLGdCQUFSLENBQXlCQyxFQUF6QixDQUE0QixtQkFBNUIsRUFBbUQsTUFBS0MsUUFBeEQ7QUFGbUI7QUFHcEI7Ozs7K0JBRVU7QUFDVCxVQUFNQyxRQUFRLHVCQUFTO0FBQ3JCQyx5QkFBaUIsS0FBS0Msa0JBQUwsQ0FBd0JELGVBRHBCO0FBRXJCRSw0QkFBb0IsS0FBS0E7QUFGSixPQUFULENBQWQ7QUFJQSxVQUFJSCxNQUFNSSxNQUFOLEtBQWlCLENBQXJCLEVBQXdCO0FBQ3RCLGFBQUtDLEdBQUwsQ0FBUyxxQkFBVDtBQUNBO0FBQ0Q7QUFDRCxVQUFNQyxRQUFRLHVCQUFVO0FBQ3RCQyxjQUFNLENBQUMsZ0JBQUQsRUFBbUIsVUFBbkIsRUFBK0IsVUFBL0IsQ0FEZ0I7QUFFdEJDLGVBQU87QUFDTEMsa0JBQVEsRUFESDtBQUVMRixnQkFBTTtBQUZEO0FBRmUsT0FBVixDQUFkO0FBT0FQLFlBQU1VLE9BQU4sQ0FBYyxnQkFBbUQ7QUFBQSxZQUFoREMsSUFBZ0QsUUFBaERBLElBQWdEO0FBQUEsWUFBMUNDLE9BQTBDLFFBQTFDQSxPQUEwQztBQUFBLFlBQWpDQyxZQUFpQyxRQUFqQ0EsWUFBaUM7QUFBQSxZQUFuQkMsT0FBbUIsUUFBbkJBLE9BQW1CO0FBQUEsWUFBVkMsR0FBVSxRQUFWQSxHQUFVOztBQUMvRCxZQUFJQyxPQUFPLENBQUNGLFFBQVFHLFFBQVIsRUFBRCxDQUFYO0FBQ0EsWUFBSUMsT0FBTyxFQUFYO0FBQ0EsWUFBSU4sUUFBUVIsTUFBUixHQUFpQixDQUFyQixFQUF3QjtBQUN0QixjQUFJZSxTQUFTTixZQUFULENBQUosRUFBNEI7QUFDMUJLLGlCQUFLRSxJQUFMLENBQWFDLFdBQVdSLGFBQWFTLE9BQWIsQ0FBcUIsQ0FBckIsQ0FBWCxDQUFiO0FBQ0QsV0FGRCxNQUVPO0FBQ0xKLGlCQUFLRSxJQUFMLENBQVUsR0FBVjtBQUNEO0FBQ0YsU0FORCxNQU1PO0FBQ0xGLGVBQUtFLElBQUwsQ0FBVSxRQUFWO0FBQ0Q7QUFDRCxZQUFJRyxPQUFPLENBQUMsNkJBQWUsRUFBRVosVUFBRixFQUFRSSxRQUFSLEVBQWYsQ0FBRCxDQUFYO0FBQ0EseUJBQUVTLElBQUYsQ0FBT1osT0FBUCxFQUFnQixDQUFoQixFQUFtQkYsT0FBbkIsQ0FBMkIsaUJBQVM7QUFDbENNLGVBQUtJLElBQUwsUUFBZUssTUFBTUMsSUFBckI7QUFDQSxjQUFJUCxTQUFTTSxNQUFNRSxRQUFmLENBQUosRUFBOEI7QUFDNUJULGlCQUFLRSxJQUFMLENBQWFLLE1BQU1FLFFBQW5CO0FBQ0QsV0FGRCxNQUVPO0FBQ0xULGlCQUFLRSxJQUFMLENBQVUsR0FBVjtBQUNEO0FBQ0RHLGVBQUtILElBQUwsQ0FBVSw2QkFBZUssS0FBZixDQUFWO0FBQ0QsU0FSRDtBQVNBLFlBQUliLFFBQVFSLE1BQVIsR0FBaUIsQ0FBckIsRUFBd0I7QUFDdEJZLGVBQUtJLElBQUwsU0FBZVIsUUFBUVIsTUFBUixHQUFpQixDQUFoQztBQUNEO0FBQ0RFLGNBQU1jLElBQU4sQ0FBVyxDQUFDSixLQUFLWSxJQUFMLENBQVUsSUFBVixDQUFELEVBQWtCVixLQUFLVSxJQUFMLENBQVUsSUFBVixDQUFsQixFQUFtQ0wsS0FBS0ssSUFBTCxDQUFVLElBQVYsQ0FBbkMsQ0FBWDtBQUNELE9BMUJEO0FBMkJBLFdBQUt2QixHQUFMLENBQVNDLE1BQU1XLFFBQU4sS0FBbUIsSUFBNUI7QUFDRDs7Ozs7a0JBbERrQnRCLGMiLCJmaWxlIjoidXNhZ2VfZm9ybWF0dGVyLmpzIiwic291cmNlc0NvbnRlbnQiOlsiaW1wb3J0IF8gZnJvbSAnbG9kYXNoJ1xuaW1wb3J0IHsgZm9ybWF0TG9jYXRpb24sIGdldFVzYWdlIH0gZnJvbSAnLi9oZWxwZXJzJ1xuaW1wb3J0IEZvcm1hdHRlciBmcm9tICcuLydcbmltcG9ydCBUYWJsZSBmcm9tICdjbGktdGFibGUnXG5cbmV4cG9ydCBkZWZhdWx0IGNsYXNzIFVzYWdlRm9ybWF0dGVyIGV4dGVuZHMgRm9ybWF0dGVyIHtcbiAgY29uc3RydWN0b3Iob3B0aW9ucykge1xuICAgIHN1cGVyKG9wdGlvbnMpXG4gICAgb3B0aW9ucy5ldmVudEJyb2FkY2FzdGVyLm9uKCd0ZXN0LXJ1bi1maW5pc2hlZCcsIDo6dGhpcy5sb2dVc2FnZSlcbiAgfVxuXG4gIGxvZ1VzYWdlKCkge1xuICAgIGNvbnN0IHVzYWdlID0gZ2V0VXNhZ2Uoe1xuICAgICAgc3RlcERlZmluaXRpb25zOiB0aGlzLnN1cHBvcnRDb2RlTGlicmFyeS5zdGVwRGVmaW5pdGlvbnMsXG4gICAgICBldmVudERhdGFDb2xsZWN0b3I6IHRoaXMuZXZlbnREYXRhQ29sbGVjdG9yXG4gICAgfSlcbiAgICBpZiAodXNhZ2UubGVuZ3RoID09PSAwKSB7XG4gICAgICB0aGlzLmxvZygnTm8gc3RlcCBkZWZpbml0aW9ucycpXG4gICAgICByZXR1cm5cbiAgICB9XG4gICAgY29uc3QgdGFibGUgPSBuZXcgVGFibGUoe1xuICAgICAgaGVhZDogWydQYXR0ZXJuIC8gVGV4dCcsICdEdXJhdGlvbicsICdMb2NhdGlvbiddLFxuICAgICAgc3R5bGU6IHtcbiAgICAgICAgYm9yZGVyOiBbXSxcbiAgICAgICAgaGVhZDogW11cbiAgICAgIH1cbiAgICB9KVxuICAgIHVzYWdlLmZvckVhY2goKHsgbGluZSwgbWF0Y2hlcywgbWVhbkR1cmF0aW9uLCBwYXR0ZXJuLCB1cmkgfSkgPT4ge1xuICAgICAgbGV0IGNvbDEgPSBbcGF0dGVybi50b1N0cmluZygpXVxuICAgICAgbGV0IGNvbDIgPSBbXVxuICAgICAgaWYgKG1hdGNoZXMubGVuZ3RoID4gMCkge1xuICAgICAgICBpZiAoaXNGaW5pdGUobWVhbkR1cmF0aW9uKSkge1xuICAgICAgICAgIGNvbDIucHVzaChgJHtwYXJzZUZsb2F0KG1lYW5EdXJhdGlvbi50b0ZpeGVkKDIpKX1tc2ApXG4gICAgICAgIH0gZWxzZSB7XG4gICAgICAgICAgY29sMi5wdXNoKCctJylcbiAgICAgICAgfVxuICAgICAgfSBlbHNlIHtcbiAgICAgICAgY29sMi5wdXNoKCdVTlVTRUQnKVxuICAgICAgfVxuICAgICAgbGV0IGNvbDMgPSBbZm9ybWF0TG9jYXRpb24oeyBsaW5lLCB1cmkgfSldXG4gICAgICBfLnRha2UobWF0Y2hlcywgNSkuZm9yRWFjaChtYXRjaCA9PiB7XG4gICAgICAgIGNvbDEucHVzaChgICAke21hdGNoLnRleHR9YClcbiAgICAgICAgaWYgKGlzRmluaXRlKG1hdGNoLmR1cmF0aW9uKSkge1xuICAgICAgICAgIGNvbDIucHVzaChgJHttYXRjaC5kdXJhdGlvbn1tc2ApXG4gICAgICAgIH0gZWxzZSB7XG4gICAgICAgICAgY29sMi5wdXNoKCctJylcbiAgICAgICAgfVxuICAgICAgICBjb2wzLnB1c2goZm9ybWF0TG9jYXRpb24obWF0Y2gpKVxuICAgICAgfSlcbiAgICAgIGlmIChtYXRjaGVzLmxlbmd0aCA+IDUpIHtcbiAgICAgICAgY29sMS5wdXNoKGAgICR7bWF0Y2hlcy5sZW5ndGggLSA1fSBtb3JlYClcbiAgICAgIH1cbiAgICAgIHRhYmxlLnB1c2goW2NvbDEuam9pbignXFxuJyksIGNvbDIuam9pbignXFxuJyksIGNvbDMuam9pbignXFxuJyldKVxuICAgIH0pXG4gICAgdGhpcy5sb2codGFibGUudG9TdHJpbmcoKSArICdcXG4nKVxuICB9XG59XG4iXX0=