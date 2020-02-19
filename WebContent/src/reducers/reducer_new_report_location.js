import {
    SET_NEW_REPORT_LOCATION,
    UNSET_NEW_REPORT_LOCATION
} from '../actions/actions_reports';


export default function(state = null, action) {
    switch (action.type) {
        case SET_NEW_REPORT_LOCATION: {
            console.log('Set location reducer', action.payload);
            return action.payload;
        }
        case UNSET_NEW_REPORT_LOCATION: {
            return null;
        }
    }
    return state;
}