import React, { Component } from 'react';
import { connect } from 'react-redux';
import { Field, reduxForm, reset } from 'redux-form';

import BASE_URL from '../utils/config';
import axios from 'axios';


// Actions 
import { loadEndUsers } from '../actions/actions_end_users';


class EndAccountRegister extends Component {

    onSubmit(values) {
        const { alert } = this.refs;
        const token = JSON.parse(localStorage.getItem('token'));
        const data = {
            name: values.name,
            email: values.email,
            email_confirm: values.email_confirm,
            password: " ",
            password_confirm: " ",
            address: {
                district: "",
                county: ""
            },
            internalId: values.internal_id
        }

        axios.post(`${BASE_URL}/register/end`, { token, data })
            .then(response => {
                if (response.status === 200) {
                    alert.classList.remove('d-none');
                    alert.classList.add('text-success');
                    alert.innerHTML = 'Nova conta registada com sucesso.';
                    this.props.dispatch(loadEndUsers(JSON.parse(localStorage.getItem('token'))));

                    setTimeout(() => {
                        alert.classList.add('d-none');
                        alert.classList.remove('text-success');
                        alert.innerHTML = '';
                        this.props.dispatch(reset('CoreAccountForm'));

                    }, 3000);
                }
            })
            .catch(err => {
                if (err.response.status === 409) {
                    alert.classList.remove('d-none');
                    alert.classList.add('text-danger');
                    alert.innerHTML = 'Email introduzido já existe.';
                    setTimeout(() => {
                        alert.classList.add('d-none');
                        alert.classList.remove('text-danger');
                        alert.innerHTML = '';
                        this.props.dispatch(reset('CoreAccountForm'));
                    }, 3000);
                }
            })
    }

    renderField(field) {
        const { meta: { touched, error } } = field;
        const className = `form-group ${touched && error ? 'has-danger' : ''}`;
        return (
            <div className={className}>
                <div className="input-group input-group-sm">
                    <input
                        type={field.type}
                        placeholder={field.placeholder}
                        className="form-control input-sm"
                        {...field.input}
                    />
                </div>
                <small className="form-control-feedback">
                    {touched ? error : ''}
                </small>
            </div>
        );
    }

    render() {
        const { handleSubmit, pristine, submitting } = this.props;

        return (
            <div>
                <div className="row justify-content-center">
                    <div className="col-sm-9 pt-5 mt-5">
                        <h4 className="form-descript"> Registar um novo administrador: </h4>
                        <form onSubmit={handleSubmit(this.onSubmit.bind(this))}>
                            <Field
                                type="text"
                                name="name"
                                placeholder="Name"
                                component={this.renderField}
                            />
                            <Field
                                name="email"
                                type="email"
                                placeholder="Email"
                                component={this.renderField}
                            />
                            <Field
                                name="email_confirm"
                                type="email"
                                placeholder="Email Confirmação"
                                component={this.renderField}
                            />
                            <Field
                                name="internal_id"
                                type="text"
                                placeholder="Identificador interno (opcional)"
                                component={this.renderField}
                            />
                            <button id="submitButton" type="submit" className="btn btn-primary form-control mt-2">Registar</button>
                        </form>
                    </div>

                </div>
                <div className="submit-success" ref="alert"> </div>
            </div>



        );
    }
}

function validate(values) {
    const errors = {};
    if (!values.email) {
        errors.email = "Deve introduzir um email válido.";
    }
    if (!values.name) {
        errors.name = "Deve introduzir um nome válido.";
    }
    if (values.email !== values.email_confirm) {
        errors.email_confirm = "O email introduzido e a respectiva confirmação não correspondem.";
    }
    return errors;
}


function mapStateToProps({ }) {
    return {};
}

export default reduxForm({
    form: 'CoreAccountForm',
    validate
})(connect(mapStateToProps, {})(EndAccountRegister));



