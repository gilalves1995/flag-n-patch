// 3rd Party
import React, { Component } from 'react';
import { connect } from 'react-redux';
import { reset, change } from 'redux-form';
import { Link } from 'react-router-dom';
import axios from 'axios';

// Internal Modules
import RegisterForm from '../components/form_register';
import Modal from '../components/modal';
import Spinner from '../components/spinner';
import LoadingText from '../components/loading_text';
import Alert from '../components/alert';
import BASE_URL from '../utils/config';
import { fetchTokenFromLocalstorage } from '../actions/actions_token';


/*
    Página para registo de utilizadores na
    aplicação.

    TODO:
        - Redireccional p/ página principal se sessão
        - load screen enquanto não se tem os dados necessários ao render inicial
        - mensagens de erro / sucesso
*/
class RegisterPage extends Component {
    constructor(props) {
        super(props);

        this.state = {
            districts: null,
            selectedDistrict: '',
            submitting: false,
            successMsg: '',
            errorMsg: ''
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
        Else, fetches districts data to present in the form.

        NOTA: Este método corre essencialmente após o login.    
    */
    componentWillReceiveProps(nextProps) {
        const token = nextProps.token.token;
        if (token) {
            this.redirectOnSession(token);
        } else {
            this.fetchDistricts();
        }
    }

    render() {
        // Alterar para loading...
        if (!this.state.districts) {
            return (
                <div className="container h-100">
                    <div className="row align-items-center justify-content-center h-100">
                        <Spinner />
                        <LoadingText message="A carregar" />
                    </div>
                </div>
            );
        }

        return(
            <div className="container h-100">
                <div className="row align-items-center justify-content-center h-100">
                    <div className="col-md-4">
                        <RegisterForm 
                            districts={this.state.districts}
                            selectedDistrict={this.state.selectedDistrict}
                            onDistrictSelect={this.onDistrictSelect.bind(this)}
                            onSubmit={this.onSubmit.bind(this)}
                        />
                        <Link className="btn btn-danger form-control mt-2" to="/login">
                            Cancelar
                        </Link>
                    </div>
                </div>
                {/* Modal */}
                {this.state.submitting &&
                <Modal>
                    <div className="d-flex justify-content-center align-items-center">
                        <div className="mr-3">
                            <Spinner />
                        </div>
                        <LoadingText message="A registar" />
                    </div>
                </Modal>}
                {/* Alert - only visible in case of error or success */}
                {(this.state.successMsg && 
                <Alert type="success" timeout={2} timeoutCallback={this.resetStateOrRedirect.bind(this)}>
                    <strong>{this.state.successMsg}</strong> {'A redireccionar para login...'}
                </Alert>)
                ||
                (this.state.errorMsg &&
                <Alert type="danger" timeout={4} timeoutCallback={this.resetStateOrRedirect.bind(this)}>
                    <strong>{this.state.errorMsg}</strong>
                </Alert>)}
            </div>
        );
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
            case 'core':
                this.props.history.push('/app/core/management/types');
        }
    }

    fetchDistricts() {
        axios.get(`${BASE_URL}/operation/address`)
            .then(response => {
                this.setState({ districts: response.data });
            })
            .catch(error => {
                console.log('Error!', error);
            });
    }

    /*
        Redirecciona para login em caso de registo com sucesso.
        Faz dismiss do alert de erro em caso de insucesso.
    */
    resetStateOrRedirect() {
        if (this.state.successMsg)
            this.props.history.push('/login');
        else
            this.setState({ successMsg: '', errorMsg: '' });
    }

    onDistrictSelect(event) {
        const district = event.currentTarget.value;
        if (district !== 'Distrito...')
            this.setState({ selectedDistrict: district });
        else
            this.setState({ selectedDistrict: '' });
        // Força a mudança de valor no campo address.county
        this.props.dispatch(change('Register', 'address.county', 'Concelho...'));
    }

    onSubmit(values) {
        this.setState({ submitting: true });
        axios({
            method: 'post',
            responseType: 'json',
            url: `${BASE_URL}/register/trial`,
            data: values
        })
        .then(response => {
            this.props.dispatch(reset('Register'));
            this.setState({ submitting: false, successMsg: 'Sucesso!' });
        })
        .catch(error => {
            this.setState({ submitting: false, errorMsg: error.response.data.output });
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
export default connect(mapStateToProps)(RegisterPage);