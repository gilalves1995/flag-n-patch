// 3rd Party
import React, { Component } from 'react';
import { Field, reduxForm } from 'redux-form';

// Internal Modules
import SuggestionList from './suggestion-list';


/*

*/
class AddReportForm extends Component {
    adaptFileEventToValue(delegate) {
        return (event) => {
            delegate(event.target.files[0]);
            const reader = new FileReader();
            //reader.onload = (e) => this.props.imgPreview.src = e.target.result;
            reader.onload = (e) => this.props.onImageSelect(e.target.result);
            reader.readAsDataURL(event.target.files[0]);
        }
    }

    renderFileInput({ input: { value: omitValue, onChange, onBlur, ...inputProps }, meta: omitMeta, ...props }) {
        return(
            <div className="form-group">
                <div className="input-group input-group-lg">
                    <label htmlFor="image" className="form-control" id="image-input-label">
                        <span className="text-muted">{omitValue ? omitValue.name : 'Escolha uma foto...'}</span>
                        <i className="ion-camera"></i>
                        <input
                            type="file"
                            accept="image/*"
                            id="image"
                            className="d-none"
                            onChange={this.adaptFileEventToValue(onChange)}
                            onBlur={this.adaptFileEventToValue(onBlur)}
                            {...inputProps}
                            {...props}
                        />
                    </label>
                </div>
            </div>
        );
    }
    
    renderAddressField({ input }) {
        return(
            <div className="form-group">
                <div className="input-group input-group-lg">
                    <input
                        disabled
                        className="form-control"
                        type="text"
                        placeholder={this.props.address}
                        {...input}
                    />
                </div>
            </div>
        );
    }

    renderReportTypeSelect({ input, meta: {touched, error} }) {
        return(
            <div className={`form-group${touched ? (error ? ' has-danger' : ' has-success') : ''}`}>
                <div className="input-group input-group-lg">
                    <select
                        className={`form-control${touched ? (error ? ' form-control-danger' : ' form-control-success') : ''}`}
                        {...input}
                        onBlur={() => {input.onBlur(input.value)}}
                    >
                        <option value="Tipo de Ocorrência" defaultValue>Tipo de Ocorrência</option>
                        {this.props.types.map(type =>
                        <option key={type.id} value={type.name}>{type.name}</option>)}
                    </select>
                </div>
                <small className="form-control-feedback">
                    {touched ? error : ''}
                </small>
            </div>
        );
    }

    renderPrioritySelect({ input, meta: {touched, error} }) {
        return(
            <div className={`form-group${touched ? (error ? ' has-danger' : ' has-success') : ''}`}>
                <div className="input-group input-group-lg">
                    <select
                        className={`form-control${touched ? (error ? ' form-control-danger' : ' form-control-success') : ''}`}
                        {...input}
                    >
                        <option value="Prioridade" defaultValue>Prioridade</option>
                        <option value="1">1</option>
                        <option value="2">2</option>
                        <option value="3">3</option>
                        <option value="4">4</option>
                        <option value="5">5</option>
                    </select>
                </div>
                <small className="form-control-feedback">
                    {touched ? error : ''}
                </small>
            </div>
        );
    }

    renderDescriptionField({ input, meta: {touched, error} }) {
        return(
            <div className={`form-group${touched ? (error ? ' has-danger' : ' has-success') : ''}`}>
                <div className="input-group input-group-lg">
                    <textarea
                        className={`form-control${touched ? (error ? ' form-control-danger' : ' form-control-success') : ''}`}
                        rows="3"
                        placeholder="Insira uma descrição para a ocorrência..."
                        {...input}>
                    </textarea>
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
            <form onSubmit={handleSubmit(this.props.onSubmit)}>
                <Field
                    name="image"
                    component={this.renderFileInput.bind(this)}
                />
                <Field
                    name="addressAsStreet"
                    component={this.renderAddressField.bind(this)}
                />
                <Field
                    name="type"
                    onChange={event => this.props.getSuggestions(event.target.value)}
                    component={this.renderReportTypeSelect.bind(this)}
                />
                {this.props.suggestions.length > 0 &&
                <SuggestionList suggestions={this.props.suggestions} />}
                <Field
                    name="priority"
                    component={this.renderPrioritySelect.bind(this)}
                />
                <Field
                    name="description"
                    component={this.renderDescriptionField.bind(this)}
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

// Função onde acontece toda a validação dos campos do form.
function validate(values) {
    const errors = {};

    if (!values.type || values.type === 'Tipo de Ocorrência') {
        errors.type = 'Obrigatório';
    }

    if (!values.priority || values.priority === 'Prioridade') {
        errors.priority = 'Obrigatório';
    }

    return errors;
}

// Liga o formulário ao redux-form e, portanto, à store do Redux.
export default reduxForm({
    form: 'AddReport',
    validate
})(AddReportForm);