// 3rd Party
import React, { Component } from 'react';
import { connect } from 'react-redux';
import { Link } from 'react-router-dom';
import axios from 'axios';

// Internal Modules
import LoginForm from '../components/form_login';
import ResetPasswordForm from '../components/form_reset_password';
import Logo from '../components/logo';
import Divider from '../components/divider';
import GoogleOauthButton from '../components/button_oauth_google';
import FacebookOauthButton from '../components/button_oauth_facebook';
import Alert from '../components/alert';
import Spinner from '../components/spinner';
import LoadingText from '../components/loading_text';
import Modal from '../components/modal';
import { fetchTokenFromLocalstorage, doLogin } from '../actions/actions_token';
import { resetNetworkStatuses } from '../actions/actions_network';
import BASE_URL from '../utils/config';


/*
    Página de login na Aplicação comum a todos os tipos de
    utilizador.
*/
class LoginPage extends Component {
    constructor(props) {
        super(props);

        this.state = {
            submitResetPending: false,
            submitResetFulfilled: false,
            submitResetRejected: false,
            message: ''
        };
    }

    /*
        If there's a token is state, will redirect to proper main page.
        Otherwise will attempt to fetch token from localstorage.
        If a valid token is fetched then redirects to proper main page.
        Else, nothing happens.
    */
    componentWillMount() {
        const token = this.props.token.token;
        if (token) {
            this.redirectOnSession(token);
        } else {
            this.props.dispatch(fetchTokenFromLocalstorage());
        }
    }

    /*
        If token is already in state (i.e. comes in nextProps) then
        redirects to proper main page.
        Else, nothing happens.

        NOTA: Este método corre essencialmente após o login.    
    */
    componentWillReceiveProps(nextProps) {
        const token = nextProps.token.token;
        if (token) {
            this.redirectOnSession(token);
        }
    }

    render() {
        return(
            <div className="container h-100">
                <div className="row align-items-center justify-content-center h-100">
                    <div className="col-md-4 relative">
                        <Logo />
                        <LoginForm onSubmit={this.onSubmit.bind(this)} />
                        <Link className="btn btn-lg btn-success form-control mb-2 mt-1" to="/">
                            Voltar
                        </Link>
                        <div className="clearfix">
                            <a className="float-left" data-toggle="collapse" href="#collapse" aria-expanded="false" aria-controls="collapse">
                                Esqueceu-se?
                            </a>
                            <Link className="float-right" to="/register">
                                Registar
                            </Link>
                        </div>
                        <div className="collapse" id="collapse">
                            <ResetPasswordForm onSubmit={this.onSubmitResetPassword.bind(this)} />
                        </div>
                        <Divider />
                        <div className="row">
                            <div className="col-6">
                                <FacebookOauthButton />
                            </div>
                            <div className="col-6">
                                <GoogleOauthButton />
                            </div>
                        </div>
                    </div>
                    {/* Modal */}
                    {(this.props.token.loggingIn &&
                    <Modal>
                        <div className="d-flex justify-content-center align-items-center">
                            <div className="mr-3">
                                <Spinner />
                            </div>
                            <LoadingText message="A autenticar" />
                        </div>
                    </Modal>)
                    ||
                    (this.state.submitResetPending &&
                    <Modal>
                        <div className="d-flex justify-content-center align-items-center">
                            <div className="mr-3">
                                <Spinner />
                            </div>
                            <LoadingText message="A submeter" />
                        </div>
                    </Modal>)}
                    {/* Alert - only visible in case of error */}
                    {(this.props.token.error && 
                    <Alert type="danger" timeout={4} timeoutCallback={this.resetNetworkStatusGlobal.bind(this)}>
                        <strong>Error:</strong> {this.props.token.error.response.data.output}
                    </Alert>)
                    ||
                    (this.state.submitResetRejected &&
                    <Alert type="danger" timeout={4} timeoutCallback={this.resetNetworkStatusInternal.bind(this)}>
                        <strong>Error:</strong> {this.state.message}
                    </Alert>)
                    ||
                    (this.state.submitResetFulfilled &&
                    <Alert type="success" timeout={4} timeoutCallback={this.resetNetworkStatusInternal.bind(this)}>
                        <strong>Sucesso:</strong> Brevemente receberá um email.
                    </Alert>)}
                </div>
            </div>
        );
    }

    /*
        Recebe os valores submetidos no formulário de
        login e faz dispatch da acção de login com
        os valores.
    */
    onSubmit(values) {
        this.props.dispatch(doLogin(values));
    }

    /*
        Recebe o endereço de email introduzido no campo de email
        e envia uma mensagem ao servidor para que este envie um
        email ao utilizador com a possibilidade de alterar a
        password.
    */
    onSubmitResetPassword(values) {
        console.log('Submitting...', values);
        this.setState({ submitResetPending: true });
        axios({
            method: 'post',
            responseType: 'json',
            url: `${BASE_URL}/login/resetPassword`,
            data: values
        })
        .then(response => {
            console.log('Response', response);
            const successMessage = response.data;
            this.setState({ submitResetPending: false, submitResetFulfilled: true, message: successMessage });
        })
        .catch(error => {
            const e = {...error};
            const errorMessage = e.response.data.output;
            this.setState({ submitResetPending: false, submitResetRejected: true, message: errorMessage });
        });
    }

    /*
        Este método só é invocado no caso de haver um token / sessão.
        Dado a role do utilizador autenticado, redirecciona para a
        main page deste.
    */
    redirectOnSession(token) {
        switch (token.role) {
            case 'trial':
            case 'basic':
                this.props.history.push('/app/user/map'); 
                break;
            case 'end': 
                this.props.history.push('/app/core/management/types');
                break;
            case 'core':
                this.props.history.push('/app/core/management/statistics');
                break;
            case 'work': 
                this.props.history.push('/app/worker/reports');
        }
    }

    /**/
    resetNetworkStatusGlobal() {
        this.props.dispatch(resetNetworkStatuses());
    }

    /**/
    resetNetworkStatusInternal() {
        this.setState({
            submitResetPending: false,
            submitResetFulfilled: false,
            submitResetRejected: false,
            message: ''
        });
    }
}

/*
    Função que filtra as propriedades da store que interessam ao componente
    em questão e as torna disponíveis através de props.
*/
function mapStateToProps(store) {
    return {
        token: store.token
    };
}

// Ligar componente ao Redux
export default connect(mapStateToProps)(LoginPage);