import { LOAD_END_USERS } from '../actions/actions_end_users';

export default function (state = [], action) {
    switch (action.type) {
        case LOAD_END_USERS: {
            const data = action.payload;
            if(data.data) {
                console.log("users", data.data);
                return data.data;
            }
        }
    }
    return state;
}