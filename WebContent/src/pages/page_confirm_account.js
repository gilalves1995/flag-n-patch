import React, { Component } from 'react';
import axios from 'axios';

import Modal from '../components/modal';
import Spinner from '../components/spinner';
import LoadingText from '../components/loading_text';
import Alert from '../components/alert';
import BASE_URL from '../utils/config';


class ConfirmAccountPage extends Component {
    constructor(props) {
        super(props);

        this.state = {
            errorMsg: '',
            successMsg: ''
        };
    }

    componentWillMount() {
        const {key, code } = this.props.match.params;

        axios.get(`${BASE_URL}/register/confirm/${key}/${code}`)
            .then(response => {
                console.log('Confirmed!', response);
                this.setState({ successMsg: 'Conta confirmada!' });
            })
            .catch(error => {
                console.log('Could not confirm...', error);
                this.setState({ errorMsg: 'Não foi possível verificar a conta' });
            });
    }

    render() {
        return(
            <div className="h-100">
                {/* Modal */}
                {(!this.state.errorMsg && !this.state.successMsg) &&
                <Modal>
                    <div className="d-flex justify-content-center align-items-center">
                        <div className="mr-3">
                            <Spinner />
                        </div>
                        <LoadingText message="A confirmar conta" />
                    </div>
                </Modal>}
                {/* Alert - only visible in case of error or success */}
                {(this.state.successMsg && 
                <Alert type="success" timeout={2} timeoutCallback={this.resetStateOrRedirect.bind(this)}>
                    <strong>{this.state.successMsg}</strong> {'A redireccionar para login...'}
                </Alert>)
                ||
                (this.state.errorMsg &&
                <Alert type="danger" timeout={9999999999999999999} timeoutCallback={this.resetStateOrRedirect.bind(this)}>
                    <strong>{this.state.errorMsg}</strong>
                </Alert>)}
            </div>
        );
    }

    resetStateOrRedirect() {
        if (this.state.successMsg)
            this.props.history.push('/login');
        else
            this.setState({ successMsg: '', errorMsg: '' });
    }
}

export default ConfirmAccountPage;