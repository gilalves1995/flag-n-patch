// 3rd Party
import React from 'react';
import { Route, Switch } from 'react-router-dom';

// Internal Modules
import ReportRouter from './router_user_report';

// Pages
import MapPage from '../pages/page_user_map';
import SettingsPage from '../pages/page_user_settings';
import NotificationDetailsPage from '../pages/page_notification_details';
 


/*
    Componente de routing das páginas de utilizador Basic.
    Liga um outro componente de routing que trata do routing das
    páginas referentes aos reports (criar e visualizar).
*/
const UserRouter = (props) => {
    return(
        <Switch>
            <Route path="/app/user/map" component={MapPage} />
            <Route path="/app/user/settings" component={SettingsPage} />
            <Route path="/app/user/report" component={ReportRouter} />
            <Route path="/app/user/notifications/:id" component= {NotificationDetailsPage} />
        </Switch>
    );
};

export default UserRouter;