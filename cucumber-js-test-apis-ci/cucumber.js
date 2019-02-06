module.exports = {
	"default" : "--require stepDefinitions -f summary -r ./features -f summary:report/summary.txt -f json:report/report.json --format-options '{\"colorsEnabled\": false}'"
};