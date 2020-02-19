// 3rd Party
import React from 'react';
import { Route, Switch } from 'react-router-dom';

// Pages
import WorkerReportPage from '../pages/page_worker_initial';
import WorkerReportDetailsPage from '../pages/page_worker_report_details';
import WorkerMapPage from '../pages/page_worker_map';
import WorkerProfilePage from '../pages/page_worker_profile';

const WorkerRouter = (props) => {
    return(
        <Switch>
            <Route path="/app/worker/reports/details/:id" component={WorkerReportDetailsPage} />
            <Route path="/app/worker/reports" component={WorkerReportPage} />
            <Route path="/app/worker/map" component={WorkerMapPage} />     
            <Route path="/app/worker/profile" component={WorkerProfilePage} />
        </Switch>
    );
};

export default WorkerRouter;