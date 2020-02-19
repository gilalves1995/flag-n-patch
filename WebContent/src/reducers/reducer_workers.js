import { LOAD_WORKERS_SUCCESS, LOAD_WORKERS_UNSUCCESS } from '../actions/actions_workers';

export default function (state = [], action) {
    switch (action.type) {
        case LOAD_WORKERS_SUCCESS: {
            const data = action.payload;
            if(data.data) {
                return data.data;
            }
        }
    }
    return state;
}