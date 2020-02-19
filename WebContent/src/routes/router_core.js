// 3rd Party
import React from 'react';
import { Route, Switch } from 'react-router-dom';

// Pages
import TypeManagementPage from '../pages/page_core_types';
import WorkerManagementPage from '../pages/page_core_workers';
import ReportPage from '../pages/page_core_worker_reports';
import AccountManagementPage from '../pages/page_core_end_accounts';
import StatisticsPage from '../pages/page_core_statistics';

/*

*/
const CoreRouter = (props) => {
    return(
        <Switch>
            <Route exact path="/app/core/management/statistics" component={StatisticsPage} />
            <Route exact path="/app/core/management/types" component={TypeManagementPage} />
            <Route exact path="/app/core/management/workers/reports/:id/:email" component={ReportPage} />
            <Route exact path="/app/core/management/workers" component={WorkerManagementPage} />
            <Route exact path="/app/core/management/accounts" component={AccountManagementPage} />
        </Switch>
    );
};

export default CoreRouter;

