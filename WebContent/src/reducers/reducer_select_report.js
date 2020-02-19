import { SELECT_REPORT_ITEM, UNSELECT_REPORT_ITEM, CLEAR_SELECTED_REPORTS } from '../actions/actions_reports';

export default function (state = [], action) {
    switch (action.type) {
        case SELECT_REPORT_ITEM: {
            return [...state, action.payload];
        }
        case UNSELECT_REPORT_ITEM: {
            return [
                ...state.slice(0, action.payload),
                ...state.slice(action.payload + 1)
            ]
        }
        case CLEAR_SELECTED_REPORTS: {
            return [];
        } 

    }
    return state;
}



