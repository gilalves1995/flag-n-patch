// 3rd Party
import React from 'react';
import { BrowserRouter, Route, Switch } from 'react-router-dom';

// Internal Modules
import AppRouter from '../routes';

// Pages
import IndexPage from '../pages/page_index';
import NotFoundPage from '../pages/page_error_404';
import LoginPage from '../pages/page_login';
import RegisterPage from '../pages/page_register';
import NewPasswordPage from '../pages/page_new_password';
import ConfirmAccountPage from '../pages/page_confirm_account';


/*
    Componente que contém toda a lógica de topo
    de routing da aplicação.
    Contém todas as routes das páginas comuns a todos
    os utilizadores e depois liga um outro component de
    routing que trata do routing de todas as páginas da
    aplicação após login.
*/
const App = (props) => {
    return(
        <BrowserRouter>
            <Switch>
                <Route exact path="/" component={IndexPage} />
                <Route path="/login" component={LoginPage} />
                <Route path="/register" component={RegisterPage} />
                <Route path="/newpassword/:key/:code" component={NewPasswordPage} />
                <Route path="/confirm/:key/:code" component={ConfirmAccountPage} />
                {/*<Route path="resetPassword" component={} />
                <Route path="confirmEmail" component={} />*/}
                <Route path="/app" component={AppRouter} />
                <Route component={NotFoundPage} />
            </Switch>
        </BrowserRouter>
    );
};

export default App;
