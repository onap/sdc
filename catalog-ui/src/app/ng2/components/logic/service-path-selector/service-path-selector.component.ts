import {Component, Input, KeyValueDiffer, IterableDiffers, KeyValueDiffers, DoCheck} from '@angular/core';
import {Service} from "app/models/components/service";
import {TranslateService} from "app/ng2/shared/translator/translate.service";
import {ForwardingPath} from "app/models/forwarding-path";
import {DropdownValue} from "app/ng2/components/ui/form-components/dropdown/ui-element-dropdown.component";

@Component({
	selector: 'service-path-selector',
	templateUrl: './service-path-selector.component.html',
	styleUrls:['service-path-selector.component.less'],
	providers: [TranslateService]
})

export class ServicePathSelectorComponent implements DoCheck {

	defaultSelectedId: string;
	hideAllValue: string;
	hideAllId: string = '0';
	showAllValue: string;
	showAllId: string = '1';

	paths: Array<ForwardingPath> = [];
	dropdownOptions: Array<DropdownValue>;
	differ: KeyValueDiffer;

	@Input() service: Service;
	@Input() drawPath: Function;
	@Input() deletePaths: Function;
	@Input() selectedPathId: string;

	constructor(private differs: KeyValueDiffers, private translateService: TranslateService) {

		this.defaultSelectedId = this.hideAllId;
		this.convertPathsToDropdownOptions();

		this.translateService.languageChangedObservable.subscribe(lang => {
			this.hideAllValue = this.translateService.translate("SERVICE_PATH_SELECTOR_HIDE_ALL_VALUE");
			this.showAllValue = this.translateService.translate("SERVICE_PATH_SELECTOR_SHOW_ALL_VALUE");
			this.convertPathsToDropdownOptions();
		});

	}

	ngOnInit(): void {

		this.selectedPathId = this.defaultSelectedId;
		this.differ = this.differs.find(this.service.forwardingPaths).create(null);

	}

	ngDoCheck(): void {

		const pathsChanged = this.differ.diff(this.service.forwardingPaths);

		if (pathsChanged) {
			let oldPaths = _.cloneDeep(this.paths);
			this.populatePathsFromService();

			if (!(_.isEqual(oldPaths, this.paths))) {
				this.convertPathsToDropdownOptions();

				let temp = this.selectedPathId;
				this.selectedPathId = '-1';

				setTimeout(() => {
					this.selectedPathId = temp;
					this.onSelectPath();
				}, 0);
			}
		}

	}

	populatePathsFromService(): void {

		this.paths = [];
		let {forwardingPaths} = this.service;

		_.forEach(forwardingPaths, path => {
			this.paths.push(path);
		});
		this.paths.sort((a:ForwardingPath, b:ForwardingPath)=> {
			return a.name.localeCompare(b.name);
		});

	}

	convertPathsToDropdownOptions(): void {

		let result = [
			new DropdownValue(this.hideAllId, this.hideAllValue),
			new DropdownValue(this.showAllId, this.showAllValue)
		];

		_.forEach(this.paths, (value: ForwardingPath) => {
			result[result.length] = new DropdownValue(value.uniqueId, value.name);
		});

		this.dropdownOptions = result;

	}

	onSelectPath = (): void => {

		if (this.selectedPathId !== '-1') {
			this.deletePaths();

			switch (this.selectedPathId) {
				case this.hideAllId:
					break;

				case this.showAllId:
					_.forEach(this.paths, path =>
						this.drawPath(path)
					);
					break;

				default:
					let path = this.paths.find(path =>
						path.uniqueId === this.selectedPathId
					);
					if (!path) {
						this.selectedPathId = this.defaultSelectedId;
						this.onSelectPath(); // currently does nothing in default case, but if one day it does, we want the selection to behave accordingly.
						break;
					}
					this.drawPath(path);
					break;
			}
		}

	}
}
