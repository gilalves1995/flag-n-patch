import { LOAD_TYPES } from '../actions/actions_report_types';

export default function(state =[], action) {
    switch(action.type) {
        case LOAD_TYPES: {
            const data = action.payload;
            console.log("data", data.data);
            return data.data;
        } 
    }
    return state;
}