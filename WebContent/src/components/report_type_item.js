import React, { Component } from 'react';
import { connect } from 'react-redux';
import { Link } from 'react-router-dom';
import Modal from './modal';
import axios from 'axios';
import BASE_URL from '../utils/config';


// Actions 
import { selectType } from '../actions/actions_report_types';

class ReportTypeItem extends Component {

    constructor(props) {
        super(props);
        this.state = {
            selected: false,
            isModalActive: false
        }
    }

    updateItemStyle() {
        const id = "reportItem-" + this.props.item.id;
        const blocked = "reportBlocked-" + this.props.item.id;
        const symbol = "reportSymbol-" + this.props.item.id;
        if (this.props.item.isActive === false) {
            $(`#${id}`).css('color', 'red')
                .css('font-weight', 'bolder');
            $(`#${symbol}`).removeClass("fa fa-ban").addClass("fa fa-circle-o");
        }
        else {
            $(`#${id}`).css('color', 'black')
                .css('font-weight', 'normal');

            $(`#${symbol}`).removeClass("fa fa-circle-o").addClass("fa fa-ban");

        }
    }

    handleSelect() {
        const token = JSON.parse(localStorage.getItem('token'));
        if (!this.props.selected) {
            this.props.selectType(token, this.props.item);
        }
        else {
            this.props.selectType(token, null);
        }

    }

    updateSelectedType() {
        const { edit, currentResp } = this.refs;
        if (this.props.selected) {
            edit.innerHTML = "A editar...";
            edit.classList.add('text-danger');

            if (this.props.item.responsible) {
                currentResp.innerHTML = "Actual: " + this.props.item.responsible;
            }
            else currentResp.innerHTML = "Sem responsável associado."

        }
        if (!this.props.selected) {
            edit.innerHTML = "";
            currentResp.innerHTML = "";
        }

    }

    componentWillMount() {
        this.updateItemStyle();
    }

    componentDidMount() {
        this.updateItemStyle();
    }

    componentWillUpdate() {
        this.updateSelectedType();
    }
    componentDidUpdate() {
        this.updateSelectedType();
    }


    changeActiveProperty() {
        const id = "reportItem-" + this.props.item.id;
        const blocked = "reportBlocked-" + this.props.item.id;
        const symbol = "reportSymbol-" + this.props.item.id;
        const token = JSON.parse(localStorage.getItem('token'));

        axios.post(`${BASE_URL}/admin/reportTypeManagement/changeReportTypeStatus/${this.props.item.id}`, token)
            .then(response => {
                if (response.status === 200) {
                    if (response.data === false) {
                        console.log(false);
                        $(`#${id}`).css('color', 'red')
                            .css('font-weight', 'bolder');

                        $(`#${symbol}`).removeClass("fa fa-ban").addClass("fa fa-circle-o");
                        this.props.item.isActive = false;
                        this.closeModal();
                    }
                    else {
                        console.log(true);
                        $(`#${id}`).css('color', 'black')
                            .css('font-weight', 'normal');
                        $(`#${symbol}`).removeClass("fa fa-circle-o").addClass("fa fa-ban");
                        this.props.item.isActive = true;
                        this.closeModal();
                    }
                }
            })
            .catch(err => {
                console.log("Error while trying to update active status of report type");
            })
    }

    openModal() {
        if (!this.props.item.isActive) {
            this.changeActiveProperty();

            $("#changeNotifyMessage").html(`Tipo de ocorrência ${this.props.item.name} foi activado com sucesso.`);
            $("#changeNotifyMessage").css('color', 'green');
            setTimeout(function () {
                $("#changeNotifyMessage").html("");
            }, 3000);

        }
        else {
            this.setState({ isModalActive: true });
        }
    }

    closeModal() {
        this.setState({ isModalActive: false })
    }

    render() {
        const id = "reportItem-" + this.props.item.id;
        const blocked = "reportBlocked-" + this.props.item.id;
        const symbol = "reportSymbol-" + this.props.item.id;
        return (
            <div>
                {(this.state.isModalActive &&
                    <Modal>
                        <div className="container w-100">
                            <p>Tem a certeza que deseja desactivar o tipo de ocorrência <strong>{this.props.item.name}</strong>? </p>
                            <button className="btn btn-primary" onClick={this.changeActiveProperty.bind(this)}>Bloquear</button>
                            <button className="btn btn-primary ml-2" onClick={this.closeModal.bind(this)}>Cancelar</button>
                        </div>
                    </Modal>)}
                <div className="reportType">
                    <a href="#" onClick={this.handleSelect.bind(this)}><i className="fa fa-pencil edit-button" aria-hidden="true"></i></a>
                    <a href="#" onClick={this.openModal.bind(this)}><i id={symbol} className="fa fa-ban edit-button" aria-hidden="true"></i></a>
                    <li id={id}>{this.props.item.name} <span id={blocked} ref="edit"></span></li>
                    <div className="resp-display" ref="currentResp"></div>
                </div>
            </div>
        );
    }
}

function mapStateToProps({ selectedReportType }) {
    return { selectedReportType };
}

export default connect(mapStateToProps, { selectType })(ReportTypeItem);





