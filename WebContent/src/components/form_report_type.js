import React, { Component } from 'react';

import { connect } from 'react-redux';
import axios from 'axios';
import { Field, reduxForm, reset } from 'redux-form';

import BASE_URL from '../utils/config';
import $ from 'jquery';

// Actions
import { loadReportTypes } from '../actions/actions_report_types';


class ReportTypeForm extends Component {

    constructor(props) {
        super(props);
        this.state = {
            selectedWorker: "",
            isATypeSelected: false
        }
        this.updateSelectedWorker = this.updateSelectedWorker.bind(this);
    }


    updateResponsible(token, worker) {
        const { alert } = this.refs;
        const id = this.props.selectedReportType.id;
        const email = worker.email;
        axios.post(`${BASE_URL}/admin/reportTypeManagement/changeResponsible/` + id, { token, email })
            .then(response => {
                if (response.status === 200) {
                    alert.classList.remove('d-none');
                    alert.classList.add('text-success');
                    alert.innerHTML = 'Responsável foi actualizado com sucesso.';
                    this.props.dispatch(loadReportTypes(JSON.parse(localStorage.getItem('token'))));

                    setTimeout(() => {
                        alert.classList.add('d-none');
                        alert.classList.remove('text-success');
                        alert.innerHTML = '';
                        if (this.state.selectedWorker !== "") {
                            console.log("setState will be updated...");
                            this.setState({ selectedWorker: "" });
                            this.updateSelectedWorker("");
                        }
                        this.props.dispatch(reset('ReportTypeForm'));
                    }, 3000);
                }
            })
            .catch(err => {
                if (err.response.status === 409) {
                    alert.classList.remove('d-none');
                    alert.classList.add('text-danger');
                    alert.innerHTML = 'Responsável selecionado já está encarregue deste tipo de ocorrências.';
                    setTimeout(() => {
                        alert.classList.add('d-none');
                        alert.classList.remove('text-danger');
                        alert.innerHTML = '';
                        if (this.state.selectedWorker !== "") {
                            console.log("setState will be updated...");
                            this.setState({ selectedWorker: "" });
                            this.updateSelectedWorker("");
                        }
                        this.props.dispatch(reset('ReportTypeForm'));
                    }, 3000);
                }
            })
    }

    addNewType(token, data) {
        const { alert } = this.refs;
        axios.post(`${BASE_URL}/admin/reportTypeManagement/addReportType`, { token, data })
            .then(response => {
                if (response.status === 200) {
                    alert.classList.remove('d-none');
                    alert.classList.add('text-success');
                    alert.innerHTML = 'Tipo de ocorrência registado com sucesso!';
                    this.props.dispatch(loadReportTypes(JSON.parse(localStorage.getItem('token'))));
                    setTimeout(() => {
                        alert.classList.add('d-none');
                        alert.classList.remove('text-danger');
                        alert.innerHTML = '';

                        if (this.state.selectedWorker !== "") {
                            console.log("setState will be updated...");
                            this.setState({ selectedWorker: "" });
                            this.updateSelectedWorker("");
                        }
                        this.props.dispatch(reset('ReportTypeForm'));


                    }, 3000);
                }
            })
            .catch(err => {
                if (err.response.status === 409) {
                    alert.classList.remove('d-none');
                    alert.classList.add('text-danger');
                    alert.innerHTML = 'Tipo de ocorrência já existe.';
                    setTimeout(() => {
                        alert.classList.add('d-none');
                        alert.classList.remove('alert-danger');
                        alert.innerHTML = '';
                    }, 3000);
                }
            })
    }

    onSubmit(data) {
        const token = JSON.parse(localStorage.getItem('token'));
        if (!this.props.selectedReportType) {
            this.addNewType(token, data);
        }
        else {
            this.updateResponsible(token, this.state.selectedWorker);
        }
    }


    updateSelectedReportType() {
        if (this.props.selectedReportType) {
            this.props.change("name", this.props.selectedReportType.name);
            $("#submitButton").html("Editar");
        }
        else {
            $("#submitButton").html("Guardar");
        }
    }

    componentWillUpdate() {
        this.updateSelectedReportType();
    }

    componentDidUpdate() {
        this.updateSelectedReportType();
    }

    componentWillReceiveProps() {
        if (this.props.selectedReportType) {
            this.setState({ isATypeSelected: true });
        }
        else {
            this.setState({ isATypeSelected: false });
        }
    }

    renderField(field) {
        const fieldName = field.input.name;
        const fieldDisabled = field.disabled;

        const { meta: { touched, error } } = field;
        const className = `form-group ${touched && error ? 'has-danger' : ''}`;
        return (
            <div className={className}>
                <div className="input-group input-group-sm">
                    <input
                        disabled={fieldDisabled}
                        type={fieldName}
                        placeholder={fieldName.charAt(0).toUpperCase() + fieldName.slice(1)}
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

    updateSelectedWorker(worker) {
        console.log("updateSelectedWorker was called...", worker);
        this.setState({ selectedWorker: worker });
        if (worker) {
            console.log("deu merda");
            $("#detailsTitle").html("Detalhes do trabalhador:");
            $("#detailsTitle").css("font-size", "13px")
                .css("font-weight", "bold");
            if (worker.isAccountBlocked) {
                $("#accountStatusItem").html("Estado da conta: Bloqueada");
            }
            else {
                $("#accountStatusItem").html("Estado da conta: Activa");
            }
            $("#workerInfoTitle").html("Informação do trabalhador:");
            $("#workerInfoTitle").css('font-weight', 'bold')
                .css("font-size", "13px");
            if (worker.workerInfo !== "") {
                $("#workerInfoItem").html(`${worker.workerInfo}`);
            }
            else {
                $("#workerInfoItem").html("Sem informação.");
            }
        }
        else {
            console.log("fixolas");
            $("#detailsTitle").html("");
            $("#accountStatusTitle").html("");
            $("#accountStatusItem").html("");
            $("#workerInfoTitle").html("");
            $("#workerInfoItem").html("");
        }

    }

    render() {
        const { handleSubmit, pristine, submitting } = this.props;
        console.log("workers", this.props.workers);
        let displayWorkers = this.props.workers.map(worker => {
            return (<option key={worker.email} value={worker.email}>{worker.email}</option>);
        });
        return (
            <div className="container-fluid d-flex h-100">
                <div className="row justify-content-center align-self-center w-100">
                    <div className="col-sm-9 pt-5 mt-5">

                        <h4 className="form-descript"> Adicionar um novo tipo de ocorrência:</h4>
                        <form onSubmit={handleSubmit(this.onSubmit.bind(this))}>
                            <Field
                                name="name"
                                disabled={this.props.selectedReportType != null}
                                component={this.renderField}

                            //disabled = { this.props.selectedReportType !== null  }    
                            />
                            <div className="form-group">
                                <div className="input-group input-group-sm">
                                    <Field
                                        id="workerList"
                                        name="responsible"
                                        component="select"
                                        className="form-control"
                                        onChange={event => {

                                            var x = document.getElementById('workerList');
                                            var selected = event.currentTarget.value;
                                            var worker = this.props.workers.find(worker => worker.email === selected);
                                            if (!worker) {
                                                this.updateSelectedWorker("");
                                            }
                                            else {
                                                this.updateSelectedWorker(worker);
                                            }
                                        }}
                                    >
                                        <option value=""> Nenhum utilizador para associar </option>
                                        {displayWorkers}
                                    </Field>
                                </div>
                            </div>
                            <button id="submitButton" type="submit" className="btn btn-primary form-control mt-2">Guardar</button>
                        </form>

                        <div className="submit-error" ref="alert"></div>


                        <div className="displayInfo">
                            <ul className="workerInfoList">
                                <div className="dropdown-divider"></div>
                                <h6 id="detailsTitle"></h6>
                                <li>{this.state.selectedWorker.name}</li>
                                <li>{this.state.selectedWorker.email}</li>
                                <li>{this.state.selectedWorker.nif}</li>
                                <li id="accountStatusItem"></li><br />
                                <div className="displayInfoWorkerDesc">
                                    <span id="workerInfoTitle"></span><li id="workerInfoItem"></li>
                                </div>
                            </ul>
                        </div>
                    </div>
                </div>
            </div>
        );
    }
}


function validate(values) {
    const errors = {};
    if (!values.name) {
        errors.name = 'Deve introduzir um nome para o tipo de ocorrência.';
    }
    return errors;
}

function mapStateToProps({ selectedReportType }) {
    return { selectedReportType };
}

export default reduxForm({
    form: 'ReportTypeForm',
    validate
})(connect(mapStateToProps, {})(ReportTypeForm))
