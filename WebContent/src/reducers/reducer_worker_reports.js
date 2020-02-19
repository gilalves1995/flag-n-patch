import { LOAD_WORKER_REPORTS } from '../actions/actions_workers';

import {
    ADD_STATE_FILTER,
    REMOVE_STATE_FILTER
} from '../actions/actions_reports';


const initialState = {
    allReports: [],
    reports: [],
    fetching: false,
    hidden: false,
    stateFilters: ['Pendente', 'Rejeitado', 'Resolvido', 'Em Resolução']
};

export default function (state = initialState, action) {
    switch (action.type) {
        case LOAD_WORKER_REPORTS: {
            const data = action.payload;
            console.log("data", data.data);
            return {
                ...state, 
                allReports: data.data,
                reports: data.data,
                fetching: true
            };
        }
        case ADD_STATE_FILTER: {
            const reportState = action.payload;

            console.log("REPORTS", state.reports);

            let reports = state.reports;
            for(var r in state.allReports) {
                if(state.allReports[r].statusDescription === reportState) {
                    reports.push(state.allReports[r]);
                }
            }
            
            const newState = {
                ...state,
                reports: reports,
                stateFilters: [...state.stateFilters, reportState]
            };
            console.log('New filter:', newState.stateFilters);

            return newState;
        }
        case REMOVE_STATE_FILTER: {
            const reportState = action.payload;
            const i = state.stateFilters.findIndex(element => element === reportState);
            
            let reports = []; 
            for(var r in state.reports) {
                if(state.reports[r].statusDescription !== reportState) {
                    reports.push(state.reports[r]);
                }
            }

            const newState = {
                ...state,
                reports: reports,
                stateFilters: [
                    ...state.stateFilters.slice(0, i),
                    ...state.stateFilters.slice(i+1)
                ]
            };
            console.log('New filter:', newState.stateFilters);

            return newState;
        }
    }
    return state;
}