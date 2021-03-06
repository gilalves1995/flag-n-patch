// 3rd Party
import React, { Component } from 'react';
import { Field, reduxForm } from 'redux-form';


/*
    Formulário de login da aplicação.

    Recebe o callback para a submissão dos dados e invoca-o ao submeter.

    Trata de toda a sua validação e ligação à store do Redux.
*/
class LoginForm extends Component {
    renderField({ input, label, placeholder, type, meta: {touched, error} }) {
        return(
            <div className={`form-group${touched && error ? ' has-danger' : ''}`}>
                <label>{label}</label>
                <div className="input-group input-group-lg">
                    <input
                        className="form-control"
                        type={type}
                        placeholder={placeholder}
                        {...input}
                    />
                </div>
                <small className="form-control-feedback">
                    {touched ? error : ''}
                </small>
            </div>
        );
    }

    render() {
        // reduxForm injected props
        const {handleSubmit, pristine, invalid, submitting} = this.props;

        return(
            <form noValidate onSubmit={handleSubmit(this.props.onSubmit)}>
                <Field
                    name="email"
                    type="email"
                    label="Email:"
                    placeholder="you@example.com"
                    component={this.renderField}
                />
                <Field
                    name="password"
                    type="password"
                    label="Password:"
                    placeholder="Password"
                    component={this.renderField}
                />
                <button
                    type="submit"
                    className="btn btn-lg btn-primary form-control mb-2 mt-1"
                    disabled={pristine || invalid || submitting}
                >
                    Entrar
                </button>
            </form>
        );
    }
}

// Expressão regular para validar endereços de email
const emailRegex = /^(([^<>()\[\]\\.,;:\s@"]+(\.[^<>()\[\]\\.,;:\s@"]+)*)|(".+"))@((\[[0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}])|(([a-zA-Z\-0-9]+\.)+[a-zA-Z]{2,}))$/;

// Função onde acontece toda a validação dos campos do form.
function validate(values) {
    const errors = {};

    // Email validations
    if (!values.email) {
        errors.email = 'Obrigatório';
    } else if (!emailRegex.test(values.email)) {
        errors.email = 'Inválido';
    }

    // Password validations
    if (!values.password) {
        errors.password = 'Obrigatório';
    }

    return errors;
}

// Liga o formulário ao redux-form e, portanto, à store do Redux.
export default reduxForm({
    form: 'Login',
    validate
})(LoginForm);