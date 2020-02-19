// 3rd Party
import React, { Component } from 'react';
import { Field, reduxForm } from 'redux-form';


class NewPasswordForm extends Component {
    renderInput({input, placeholder, type, meta: {touched, error}}) {
        return(
            <div className={`form-group${touched ? (error ? ' has-danger' : ' has-success') : ''}`}>
                <div className="input-group input-group-lg">
                    <input
                        className={`form-control${touched ? (error ? ' form-control-danger' : ' form-control-success') : ''}`}
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
        const {handleSubmit, pristine, reset, submitting} = this.props;

        return(
            <form noValidate onSubmit={handleSubmit(this.props.onSubmit)}>
                <Field
                    name="password"
                    type="password"
                    placeholder="Password"
                    component={this.renderInput}
                />
                <Field
                    name="password_confirm"
                    type="password"
                    placeholder="Confirmar Password"
                    component={this.renderInput}
                />
                <button
                    type="submit"
                    className="btn btn-success form-control mt-2"
                    disabled={pristine || submitting}
                >
                    Alterar
                </button>
            </form>
        );
    }
}

function validate(values) {
    const errors = {};

    if (!values.password) {
        errors.password = 'Obrigatório'
    } else if (values.password.length < 6) {
        errors.password = 'Password demasiado curta'
    }

    if (!values.password_confirm) {
        errors.password_confirm = 'Obrigatório';
    } else if (values.password_confirm !== values.password) {
        errors.password_confirm = 'As passwords devem coincidir'
    }

    return errors;
}

export default reduxForm({
    form: 'NewPassword',
    validate
})(NewPasswordForm);