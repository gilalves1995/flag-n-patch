import { LOAD_REPORTS_BY_TYPE } from '../actions/actions_reports';

export default function(state=[], action) {
    switch(action.type) {
        case LOAD_REPORTS_BY_TYPE: {
            const data = action.payload;
            console.log(data.data);
            return data.data;
        } 
    }
    return state;
} 