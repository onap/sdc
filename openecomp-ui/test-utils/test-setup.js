import mockRest from 'test-utils/MockRest.js';
import Enzyme from 'enzyme';
import Adapter from 'enzyme-adapter-react-16';

Enzyme.configure({ adapter: new Adapter() });
mockRest.resetQueue();
