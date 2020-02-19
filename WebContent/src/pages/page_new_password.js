// 3rd Party
import React, { Component } from 'react';
import axios from 'axios';

// Internal Modules
import NewPasswordForm from '../components/form_new_password';
import Modal from '../components/modal';
import Spinner from '../components/spinner';
import LoadingText from '../components/loading_text';
import Alert from '../components/alert';
import Logo from '../components/logo';
import BASE_URL from '../utils/config';


class NewPasswordPage extends Component {
    constructor(props) {
        super(props);

        this.state = {
            submitting: false,
            successMsg: '',
            errorMsg: ''
        };
    }

    render() {
        return(
            <div className="container h-100">
                <div className="row align-items-center justify-content-center h-100">
                    <div className="col-md-4">
                        <Logo />
                        <NewPasswordForm onSubmit={this.onSubmit.bind(this)} />
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
            </div>
        );
    }

    resetStateOrRedirect() {
        if (this.state.successMsg)
            this.props.history.push('/login');
        else
            this.setState({ successMsg: '', errorMsg: '' });
    }

    onSubmit(values) {
        console.log('Submitting...', values);
        const { key, code } = this.props.match.params;

        this.setState({ submitting: true });
        axios({
            method: 'post',
            responseType: 'json',
            url: `${BASE_URL}/login/resetPassword/${key}/${code}`,
            data: values
        })
        .then(response => {
            console.log('Password reset', response);
            this.setState({ submitting: false, successMsg: 'Sucesso!' });
        })
        .catch(error => {
            console.log('Could not reset password', error);
            this.setState({ submitting: false, errorMsg: error.response.data.output });
        });
    }
}

export default NewPasswordPage;