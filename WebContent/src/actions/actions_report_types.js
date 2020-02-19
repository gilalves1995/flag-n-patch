import axios from 'axios';
import BASE_URL from '../utils/config';

export const LOAD_TYPES = 'LOAD_TYPES';
export const SELECTED_TYPE = 'SELECTED_TYPE';

export function loadReportTypes(token) {
    return (dispatch) => {
        axios.post(`${BASE_URL}/admin/reportTypeManagement/listReportTypes`, { token: token })
            .then(response => {
                dispatch({
                    type: LOAD_TYPES,
                    payload: response
                })
                console.log(response);
            })
            .catch(err => {
                console.log(err);
                console.log('An error has occured.');
            })
    }
}


export function selectType(token, newSelectedType) {
    return (dispatch) => {
        dispatch({
            type: SELECTED_TYPE,
            payload: newSelectedType
        });
    }

}
