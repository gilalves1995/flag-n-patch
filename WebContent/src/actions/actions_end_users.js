import axios from 'axios';
import BASE_URL from '../utils/config';

export const LOAD_END_USERS = 'LOAD_END_USERS';

export function loadEndUsers(token) {
    return (dispatch) => {
        axios.post(`${BASE_URL}/admin/endAccountManagement/loadEndAccounts`, token)
            .then(response => {
                dispatch({
                    type: LOAD_END_USERS,
                    payload: response
                })
            })
            .catch(err => {
                console.log("An error has occured", err);
            })
    }
}

