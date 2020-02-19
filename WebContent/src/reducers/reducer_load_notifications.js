import { LOAD_NOTIFICATIONS } from '../actions/actions_notifications';

export default function(state = [], action) {
    switch(action.type) {
        case LOAD_NOTIFICATIONS: {
            const data = action.payload;
            console.log("notifications", data.data);
            return data.data;
        }
    }
    return state;
}