// 3rd Party
import React, { Component } from 'react';
import { Field, reduxForm } from 'redux-form';


/*

*/
class ResetPasswordForm extends Component {
    renderEmailField({ input, meta: {touched, error} }) {
        return(
            <div className={`mb-1 form-group${touched && error ? ' has-danger' : ''}`}>
                <div className="input-group input-group-lg">
                    <input
                        className="form-control"
                        type="email"
                        placeholder="Insira endereço email"
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
                    component={this.renderEmailField}
                />
                <button
                    type="submit"
                    className="btn btn-lg btn-danger form-control mb-2 mt-0"
                    disabled={pristine || invalid || submitting}
                >
                    Recuperar
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

    return errors;
}

// Liga o formulário ao redux-form e, portanto, à store do Redux.
export default reduxForm({
    form: 'ResetPassword',
    validate
})(ResetPasswordForm);