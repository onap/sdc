import OnboardingView from './OnboardingView.jsx';
import {connect} from 'react-redux';

const mapStateToProps = ({currentScreen}) => ({currentScreen});
const Onboarding = connect(mapStateToProps, null)(OnboardingView);
export default Onboarding;
