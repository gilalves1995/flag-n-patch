import { SELECTED_TYPE } from '../actions/actions_report_types';

export default function (state = null, action) {
    switch (action.type) {
        case SELECTED_TYPE: {
            const data = action.payload;
            console.log(data);
            return data;
        }
    }
    return state;
}