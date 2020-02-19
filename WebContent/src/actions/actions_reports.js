import axios from 'axios';
import BASE_URL from '../utils/config';

export const LOAD_REPORTS_BY_TYPE = "LOAD_REPORTS_BY_TYPE";
export const GET_REPORTS = 'GET_REPORTS';
export const GET_REPORTS_ADD = 'GET_REPORTS_ADD';
export const GET_REPORTS_REPLACE = 'GET_REPORTS_REPLACE';
export const SET_HIDDEN = 'SET_HIDDEN';
export const UNSET_HIDDEN = 'UNSET_HIDDEN';
export const SET_NEW_REPORT_LOCATION = 'SET_NEW_REPORT_LOCATION';
export const UNSET_NEW_REPORT_LOCATION = 'UNSET_NEW_REPORT_LOCATION';
export const GET_NEW_REPORT_TYPES = 'GET_NEW_REPORT_TYPES';
export const SELECT_REPORT_ITEM = 'SELECT_REPORT_ITEM';
export const UNSELECT_REPORT_ITEM = 'UNSELECT_REPORT_ITEM';
export const CLEAR_SELECTED_REPORTS = "CLEAR_SELECTED_REPORTS";
export const COMPARE_SUGGESTIONS = 'COMPARE_SUGGESTIONS';
export const ADD_STATE_FILTER = 'ADD_STATE_FILTER';
export const REMOVE_STATE_FILTER = 'REMOVE_STATE_FILTER';
export const TOGGLE_FOLLOW = 'TOGGLE_FOLLOW';


export function getReportsByType(token, type, email) {
    return (dispatch) => {
        axios.post(`${BASE_URL}/admin/reportTypeManagement/loadFilteredReports`, { token, type, email })
            .then(response => {
                dispatch({
                    type: LOAD_REPORTS_BY_TYPE,
                    payload: response
                })
            })
            .catch(err => {
                console.log(err);
                console.log('An error has occured.');
            })
    }
}

export function getReports(token, cursor) {
    const data = cursor ? { token, cursor } : { token };
    return dispatch => {
        axios({
            method: 'post',
            responseType: 'json',
            url: `${BASE_URL}/reports/getA`,
            data: data
        })
            .then(response => {
                if (response.status === 200) {
                    dispatch(getReports(token, response.data.cursor));
                    if (cursor) {
                        dispatch({
                            type: GET_REPORTS_ADD,
                            payload: response.data.reports
                        });
                    } else {
                        dispatch({
                            type: GET_REPORTS_REPLACE,
                            payload: response.data.reports
                        });
                    }
                } else {
                    dispatch({
                        type: GET_REPORTS_ADD,
                        payload: []
                    });
                }
            })
            .catch(error => {
                console.log('Erro a obter reports...', { ...error });
            });
    };
}

export function setHidden() {
    return { type: SET_HIDDEN };
}

export function unsetHidden() {
    return { type: UNSET_HIDDEN };
}

export function setNewReportLocation(coords) {
    return dispatch => {
        const geocoder = new google.maps.Geocoder();
        geocoder.geocode({ location: coords }, (results, status) => {
            if (status === 'OK') {
                //
                const district = getAreaName('administrative_area_level_1', results);
                const county = getAreaName('administrative_area_level_2', results);
                const address = { district, county };

                //
                const addressComponents = results[0].address_components
                    .map((component) => component.long_name);
                const addressAsStreet = `${addressComponents[0]}, ${addressComponents[1]}, ${addressComponents[addressComponents.length - 1]}`;

                dispatch({
                    type: SET_NEW_REPORT_LOCATION,
                    payload: { coords, address, addressAsStreet }
                });
            } else {
                console.log('An error occurred while geocoding...');
            }
        });
    };
}

export function unsetNewReportLocation() {
    return { type: UNSET_NEW_REPORT_LOCATION }
}

export function getNewReportTypes(token, county) {
    return {
        type: GET_NEW_REPORT_TYPES,
        payload: axios.post(`${BASE_URL}/admin/reportTypeManagement/listReportTypes`, { token, county })
    };
}

export function addReport(report) {
    return {
        type: GET_REPORTS_ADD,
        payload: [report]
    };
}

function getAreaName(adminAreaLevel, results) {
    console.log('GEOCODING RESULTS', results);
    for (let result of results)
        for (let type of result.types)
            if (type === adminAreaLevel)
                return result.address_components[0].long_name;
    for (let result of results) {
        let { address_components } = result;
        for (let component of address_components)
            for (let type of component.types)
                if (type === adminAreaLevel)
                    return component.long_name;
    }

}

export function selectReport(report) {
    return {
        type: SELECT_REPORT_ITEM,
        payload: report
    }
}

export function unselectReport(index) {
    return {
        type: UNSELECT_REPORT_ITEM,
        payload: index
    }
}

export function clearSelectedReports() {
    return {
        type: CLEAR_SELECTED_REPORTS,
        payload: []
    }
}


export function compareSuggestions(suggestions) {
    return {
        type: COMPARE_SUGGESTIONS,
        payload: suggestions
    };
}

export function addStateFilter(state) {
    return {
        type: ADD_STATE_FILTER,
        payload: state
    };
    
}

export function removeStateFilter(state) {
    return {
        type: REMOVE_STATE_FILTER,
        payload: state
    };
}

export function toggleFollow(id) {
    console.log('ACTION id', id);
    return {
        type: TOGGLE_FOLLOW,
        payload: id
    };
}
