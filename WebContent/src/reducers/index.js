// 3rd Party
import { combineReducers } from 'redux';
import { reducer as formReducer } from 'redux-form';

// Internal Modules
import tokenReducer from './reducer_token';
import reportTypesReducer from './reducer_report_types';
import selectedReportTypeReducer from './reducer_selected_type';
import workersReducer from './reducer_workers';
import adminInfoReducer from './reducer_admin_info';
import loadReportsByTypeReducer from './reducer_reports_by_type';
import loadEndAccountsReducer from './reducer_end_account';
import loadNotificationsReducer from './reducer_load_notifications';
import reportsReducer from './reducer_reports';
import newReportLocationReducer from './reducer_new_report_location';
import newReportTypesReducer from './reducer_new_report_types';
import loadWorkerReports from './reducer_worker_reports';
import selectReportItems from './reducer_select_report';
import searchLocationReducer from './reducer_search_location';
import intervalReportReducer from './reducer_interval_report';



/*
    Objecto que representa a store do Redux.

    Define-se aqui quais as propriedades (items) da store (estado global da aplicação).

    Faz o mapping entre propriedades e os seus reducers.
*/
const rootReducer = combineReducers({
    // generic store properties
    form: formReducer,
    token: tokenReducer,
    // basic store properties
    reports: reportsReducer,
    newReportLocation: newReportLocationReducer,
    newReportTypes: newReportTypesReducer,
    searchLocation: searchLocationReducer,
    intervalReport: intervalReportReducer,
    // core store properties
    reportTypes: reportTypesReducer,
    selectedReportType: selectedReportTypeReducer,
    workers: workersReducer,
    generalAdminInfo: adminInfoReducer,
    reportsByType: loadReportsByTypeReducer,
    endAccounts: loadEndAccountsReducer,
    notificationList: loadNotificationsReducer,
    workerReports: loadWorkerReports,
    selectedWorkerReports: selectReportItems
});

export default rootReducer;
