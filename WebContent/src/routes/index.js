// 3rd Party
import React from 'react';
import { Route, Switch } from 'react-router-dom';

// Internal Modules
import Navbar from '../components/navbar';
import UserRouter from './router_user';
import CoreRouter from './router_core';
import WorkerRouter from './router_worker';

/*
    Trata do routing de todas as páginas da aplicação após login.
    Junta as componentes de routing referentes a cada tipo de utilizador.
*/
const AppRouter = (props) => {
    return(
        <div>
            <Route path="/app" component={Navbar} />
            <Switch>
                <Route path="/app/user" component={UserRouter} />
                <Route path="/app/core" component={CoreRouter} />
                <Route path="/app/worker" component={WorkerRouter} />
            </Switch>
        </div>
    );
};

export default AppRouter;