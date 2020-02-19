// 3rd Party
import React from 'react';
import { Route, Switch } from 'react-router-dom';

// Pages
import ReportPage from '../pages/page_user_report';
import ReportDetailsPage from '../pages/page_user_report_details';


/*
    Trata do routing das páginas referentes aos reports (criar e visualizar).
*/
const ReportRouter = (props) => {
    return(
        <Switch>
            <Route path="/app/user/report/:id" component={ReportDetailsPage} />
            <Route path="/app/user/report" component={ReportPage} />
        </Switch>
    );
};

export default ReportRouter;