export const SET_INTERVAL_REPORTS = 'SET_INTERVAL_REPORTS';
export const UNSET_INTERVAL_REPORTS = 'UNSET_INTERVAL_REPORTS';


export function setIntervalReports(intervalId) {
    return {
        type: SET_INTERVAL_REPORTS,
        payload: intervalId
    };
}

export function unsetIntervalReports() {
    return { type: UNSET_INTERVAL_REPORTS };
}