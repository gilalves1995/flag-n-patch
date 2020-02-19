import axios from 'axios';
import BASE_URL from '../utils/config';

export const LOAD_WORKERS_SUCCESS = "LOAD_WORKERS_SUCCESS";
export const LOAD_WORKERS_UNSUCCESS = "LOAD_WORKERS_UNSUCCESS"; 
export const LOAD_ADMIN_INFO = "LOAD_ADMIN_INFO";
export const LOAD_WORKER_REPORTS = "LOAD_WORKER_REPORTS";

export function loadWorkers(token) {
    return (dispatch) => {
        axios.post(`${BASE_URL}/admin/workerManagement/listWorkers`, token) 
            .then (response => {
                dispatch({
                    type: LOAD_WORKERS_SUCCESS,
                    payload: response
                }) 
            })
            .catch(err => {
                dispatch({
                    type: LOAD_WORKERS_UNSUCCESS,
                    payload: err
                }) 
            })
        }
}


export function loadAdminInfo(token) {
    return (dispatch) => {
        axios.post(`${BASE_URL}/admin/workerManagement/loadAdminInfo`, token) 
            .then (response => {
                dispatch({
                    type: LOAD_ADMIN_INFO,
                    payload: response
                }) 
            })
            .catch(err => {
            })
        }
}


export function getWorkerReports(token) {
    return (dispatch) => {
        axios.post(`${BASE_URL}/backoffice/report/listAll`, token) 
             .then(response => {
                dispatch({
                    type: LOAD_WORKER_REPORTS,
                    payload: response
                })
                console.log("ON ACTION: ", response);
             })
             .catch(err => {
                console.log("An error has occured while trying to load reports from this worker.", err);
             })
    }
}