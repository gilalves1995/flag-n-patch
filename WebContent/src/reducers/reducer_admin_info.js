import { LOAD_ADMIN_INFO } from '../actions/actions_workers';

export default function (state = [], action) {
    switch (action.type) {
        case LOAD_ADMIN_INFO: {
            const data = action.payload;
            console.log(data.data);
            return data.data;
        }
    }
    return state;
}