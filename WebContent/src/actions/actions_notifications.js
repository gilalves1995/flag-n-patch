import axios from 'axios';
import BASE_URL from '../utils/config'; 

export const LOAD_NOTIFICATIONS = "LOAD_NOTIFICATIONS";

export function loadNotifications(token) {
    return (dispatch) => {
        axios.post(`${BASE_URL}/operation/loadNotifications`, token)
             .then(response => {
                dispatch({
                    type: LOAD_NOTIFICATIONS,
                    payload: response
                })
             })
             .catch(err => {
                console.log("An error has occured while trying to load notifications", err);
             })
    }
}