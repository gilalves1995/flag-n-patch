// 3rd Party
import React, { Component } from 'react';
import { Field, reduxForm } from 'redux-form';
import _ from 'lodash';


/*

*/
class RegisterForm extends Component {
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

    renderSelectDistrict({input, meta: {touched, error}}) {
        // Lista de chaves no objecto districts (nomes dos distritos) para lista de <option>
        const districts = _.keys(this.props.districts).map(district => <option key={district} value={district}>{district}</option>);

        return(
            <div className={`form-group${touched ? (error ? ' has-danger' : ' has-success') : ''}`}>
                <div className="input-group input-group-lg">
                    <select
                        className={`form-control${touched ? (error ? ' form-control-danger' : ' form-control-success') : ''}`}
                        {...input}
                    >
                        <option defaultValue>Distrito...</option>
                        {districts}
                    </select>
                </div>
                <small className="form-control-feedback">
                    {touched ? error : ''}
                </small>
            </div>
        );
    }

    renderSelectCounty({input, meta: {touched, error}}) {
        if (this.props.selectedDistrict) {
            // Lista de nomes de concelhos pertencentes ao distrito seleccionado para lista de <option>
            const counties = this.props.districts[this.props.selectedDistrict].map(county => <option key={county} value={county}>{county}</option>);

            return(
                this.props.selectedDistrict &&
                <div className={`form-group${touched ? (error ? ' has-danger' : ' has-success') : ''}`}>
                    <div className="input-group input-group-lg">
                        <select
                            className={`form-control${touched ? (error ? ' form-control-danger' : ' form-control-success') : ''}`}
                            {...input}
                        >
                            <option defaultValue>Concelho...</option>
                            {counties}
                        </select>
                    </div>
                    <small className="form-control-feedback">
                        {touched ? error : ''}
                    </small>
                </div>
            );
        } else {
            return <noscript />;
        }
    }

    render() {
        // reduxForm injected props
        const {handleSubmit, pristine, reset, submitting} = this.props;

        return(
            <form noValidate onSubmit={handleSubmit(this.props.onSubmit)}>
                <Field
                    name="name"
                    type="text"
                    placeholder="Nome"
                    component={this.renderInput}
                />
                <Field
                    name="email"
                    type="email"
                    placeholder="Email"
                    component={this.renderInput}
                />
                <Field
                    name="email_confirm"
                    type="email"
                    placeholder="Confirmar Email"
                    component={this.renderInput}
                />
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
                <Field
                    name="address.district"
                    component={this.renderSelectDistrict.bind(this)}
                    onChange={event => this.props.onDistrictSelect(event)}
                />
                <Field
                    name="address.county"
                    component={this.renderSelectCounty.bind(this)}
                    onChange={event => console.log('County changed!')}
                />
                <button
                    type="submit"
                    className="btn btn-success form-control mt-2"
                    disabled={pristine || submitting}
                >
                    Registar
                </button>
            </form>
        );
    }
}

// Expressão regular para validar endereços de email
const emailRegex = /^(([^<>()\[\]\\.,;:\s@"]+(\.[^<>()\[\]\\.,;:\s@"]+)*)|(".+"))@((\[[0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}])|(([a-zA-Z\-0-9]+\.)+[a-zA-Z]{2,}))$/;

// Função onde acontece toda a validação dos campos do form.
function validate(values) {
    const errors = {
        address: {}
    };

    if (!values.name) {
        errors.name = 'Obrigatório'
    }

    if (!values.email) {
        errors.email = 'Obrigatório'
    } else if (!emailRegex.test(values.email)) {
        errors.email = 'Endereço de email inválido'
    }

    if (!values.email_confirm) {
        errors.email_confirm = 'Obrigatório';
    } else if (values.email_confirm !== values.email) {
        errors.email_confirm = 'Os emails devem coincidir'
    }

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

    if (!values.address) {
        errors.address.district = 'Obrigatório';
    } else if (!values.address.district) {
        errors.address.district = 'Obrigatório';
    } else if (values.address.district === 'Distrito...') {
        errors.address.district = 'Obrigatório';
    } else if (values.address.county === 'Concelho...') {
        errors.address.county = 'Obrigatório';
    }

    return errors;
}

// Liga o formulário ao redux-form e, portanto, à store do Redux.
export default reduxForm({
    form: 'Register',
    validate
})(RegisterForm);