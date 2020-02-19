import {
    GET_NEW_REPORT_TYPES
} from '../actions/actions_reports';


export default function(state = null, action) {
    switch (action.type) {
        case GET_NEW_REPORT_TYPES + '_FULFILLED': {
            console.log('REDUCER - Got new report types', action.payload.data);
            return action.payload.data;
        }
    }
    return state;
}