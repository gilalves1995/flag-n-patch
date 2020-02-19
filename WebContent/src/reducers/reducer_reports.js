import {
    GET_REPORTS_ADD,
    GET_REPORTS_REPLACE,
    SET_HIDDEN,
    UNSET_HIDDEN,
    COMPARE_SUGGESTIONS,
    ADD_STATE_FILTER,
    REMOVE_STATE_FILTER,
    TOGGLE_FOLLOW
} from '../actions/actions_reports';


//
const markerColors = {
    "Pendente": "959595",
    "Em Resolução": "FCF004",
    "Resolvido": "00FF00",
    "Rejeitado": "FF0000"
};

const ownMarkerIcons = {
    "Pendente": "icon_pendente.png",
    "Em Resolução": "icon_resolucao.png",
    "Resolvido": "icon_resolvido.png",
    "Rejeitado": "icon_rejeitado.png"
};

//
function buildReportsObject(reports) {
    const user = JSON.parse(localStorage.getItem('token')).user;
    let obj = {};
    for (let i in reports) {
        let report = reports[i];
        let id = report.id;
        obj[id] = { report };
        let icon;
        if (user === report.user) {
            icon = {
                url: `/img/${ownMarkerIcons[report.statusDescription]}`,
                scaledSize: new google.maps.Size(35, 50),
                //origin: new google.maps.Point(0,0),
                //anchor: new google.maps.Point(0, 0)
            };
        } else {
            icon = `https://chart.apis.google.com/chart?chst=d_map_pin_letter&chld=%E2%80%A2|${markerColors[report.statusDescription]}`;
        }
        /*const icon = user === report.user ?
            `/img/${ownMarkerIcons[report.statusDescription]}` :
            `https://chart.apis.google.com/chart?chst=d_map_pin_letter&chld=%E2%80%A2|${markerColors[report.statusDescription]}`;*/
        obj[id].marker = new google.maps.Marker({
            icon,
            position: {lat: report.lat, lng: report.lng}
        });
    }
    return obj;
}

const initialState = {
    reports: null,
    fetching: false,
    hidden: false,
    stateFilters: ['Pendente', 'Rejeitado', 'Resolvido', 'Em Resolução']
};

export default function(state = initialState, action) {
    switch (action.type) {
        case GET_REPORTS_REPLACE: {
            const reports = action.payload;
            const reportsRep = buildReportsObject(reports);
            return {
                ...state,
                reports: reportsRep,
                fetching: true
            };
        }
        case GET_REPORTS_ADD: {
            const addedReports = action.payload;
            if (addedReports.length > 0) {
                const addedReportsAsObject = buildReportsObject(addedReports);
                const reports = Object.assign({}, state.reports, addedReportsAsObject);
                return {
                    ...state,
                    reports
                };
            } else {
                return {
                    ...state,
                    fetching: false
                };
            }
        }
        case SET_HIDDEN: {
            return { ...state, hidden: true }
        }
        case UNSET_HIDDEN: {
            return { ...state, hidden: false }
        }
        case COMPARE_SUGGESTIONS: {
            const suggestions = action.payload;
            if (!suggestions || suggestions.length === 0)
                return state;
                
            const missing = [];
            for (let suggestion of suggestions) {
                if (!state.reports[suggestion.id]) {
                    missing.push(suggestion);
                }
            }
            if (missing.length > 0) {
                console.log('Something missing on reports from suggestions:', missing);
                const addedReportsAsObject = buildReportsObject(missing);
                const reports = Object.assign({}, state.reports, addedReportsAsObject);
                return {
                    ...state,
                    reports
                };
            } else {
                console.log('Nothing missing on reports from suggestions');
                return state;
            }
        }
        case ADD_STATE_FILTER: {
            const reportState = action.payload;
            
            const newState = {
                ...state,
                stateFilters: [...state.stateFilters, reportState]
            };
            console.log('New filter:', newState.stateFilters);

            return newState;
        }
        case REMOVE_STATE_FILTER: {
            const reportState = action.payload;
            const i = state.stateFilters.findIndex(element => element === reportState);

            const newState = {
                ...state,
                stateFilters: [
                    ...state.stateFilters.slice(0, i),
                    ...state.stateFilters.slice(i+1)
                ]
            };
            console.log('New filter:', newState.stateFilters);

            return newState;
        }
        case TOGGLE_FOLLOW: {
            const report = state.reports[action.payload];
            console.log('Report to change:', report);

            const changedReport = {
                report: {
                    ...report.report,
                    isFollowing: !report.report.isFollowing
                },
                marker: report.marker
            };
            console.log('Changed report:', changedReport);

            const reports = {
                ...state.reports,
                [action.payload]: changedReport
            };
            console.log('NEW STATE', reports);

            return {
                ...state,
                reports
            };
        }
    }
    return state;
}