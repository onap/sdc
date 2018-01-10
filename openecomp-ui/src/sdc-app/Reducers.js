import {combineReducers} from 'redux';
import onBoardingReducersMap from './onboarding/OnboardingReducersMap.js';
import flowsReducersMap from './flows/FlowsReducersMap.js';
import loaderReducer from 'nfvo-components/loader/LoaderReducer.js';
import globalModalReducer from 'nfvo-components/modal/GlobalModalReducer.js';
import notificationsReducer from 'sdc-app/onboarding/userNotifications/NotificationsReducer.js';

export default combineReducers({
	// on-boarding reducers
	...onBoardingReducersMap,

	// flows reducers
	...flowsReducersMap,
	modal: globalModalReducer,
	loader: loaderReducer,
	notifications: notificationsReducer
});
