import {
    SET_INTERVAL_REPORTS,
    UNSET_INTERVAL_REPORTS
} from '../actions/actions_interval_reports';


export default function(state = null, action) {
    switch (action.type) {
        case SET_INTERVAL_REPORTS: {
            return action.payload;
        }
        case UNSET_INTERVAL_REPORTS: {
            window.clearInterval(state);
            return null;
        }
    }
    return state;
}