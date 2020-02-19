import React, { Component } from 'react';
import axios from 'axios';
import $ from 'jquery';
import uuid from 'uuid';
import BASE_URL from '../utils/config';

// Components 
import Modal from './modal';

// Actions 
import { loadEndUsers } from '../actions/actions_end_users';

class EndAccountItem extends Component {
    constructor(props) {
        super(props);
        this.state = {
            isModalActive: false
        }
    }

    updateItemStyle() {
        const tmp = this.props.item.email.split('.').join('').replace('@', '');
        const id = "item-" + tmp;
        const blocked = "blocked-" + tmp;
        const symbol = "symbol-" + tmp;

        if (this.props.item.isAccountBlocked === true) {
            $(`#${id}`).css('color', 'red')
                .css('font-weight', 'bolder');
            $(`#${blocked}`).html(' - Bloqueada');
            $(`#${blocked}`).css('font-weight', 'normal');
            $(`#${symbol}`).removeClass("fa fa-ban").addClass("fa fa-circle-o");
        }
        else {
            $(`#${id}`).css('color', 'black')
                .css('font-weight', 'normal');
            $(`#${blocked}`).html('');
            $(`#${symbol}`).removeClass("fa fa-circle-o").addClass("fa fa-ban");
        }
    }

    componentWillMount() {
        this.updateItemStyle();
    }

    componentDidMount() {
        this.updateItemStyle();
    }

    openModal() {
        if (this.props.item.isAccountBlocked) {
            this.updateBlockStatus();

        }
        else {
            this.setState({ isModalActive: true });
        }
    }

    closeModal() {
        this.setState({ isModalActive: false })
    }


    updateBlockStatus() {
        const token = JSON.parse(localStorage.getItem('token'));
        const tmp = this.props.item.email.split('.').join('').replace('@', '');
        const id = "item-" + tmp;
        const blocked = "blocked-" + tmp;
        const symbol = "symbol-" + tmp;

        console.log("update block status was called", tmp);

        axios.post(`${BASE_URL}/admin/endAccountManagement/updateBlockStatus/` + this.props.item.email, token)
            .then(response => {
                if (response.status === 200) {
                    if (response.data === true) {
                        $(`#${id}`).css('color', 'red')
                            .css('font-weight', 'bolder');
                        $(`#${blocked}`).html(' - Bloqueada');
                        $(`#${blocked}`).css('font-weight', 'normal');
                        $(`#${symbol}`).removeClass("fa fa-ban").addClass("fa fa-circle-o");
                        this.props.item.isAccountBlocked = true;
                        this.closeModal();

                    }
                    else {
                        $(`#${id}`).css('color', 'black')
                            .css('font-weight', 'normal');
                        $(`#${blocked}`).html('');
                        $(`#${symbol}`).removeClass("fa fa-circle-o").addClass("fa fa-ban");
                        this.props.item.isAccountBlocked = false;

                        $("#changeNotifyMessage").html(`A conta seleccionada foi activada com sucesso.`);
                        $("#changeNotifyMessage").css('color', 'green');
                        setTimeout(function () {
                            $("#changeNotifyMessage").html("");
                        }, 3000);
                    }
                }
            })
            .catch(err => {
                console.log("An error has occured while trying to update blocking status.");
            })
    }

    render() {
        const tmp = this.props.item.email.split('.').join('').replace('@', '');
        return (
            <div>
                {(this.state.isModalActive &&
                    <Modal>
                        <div className="container w-100">
                            <p>Tem a certeza que deseja bloquear a conta <strong>{this.props.item.email}</strong>?</p>
                            <button className="btn btn-primary" onClick={this.updateBlockStatus.bind(this)}>Bloquear</button>
                            <button className="btn btn-primary ml-2" onClick={this.closeModal.bind(this)}>Cancelar</button>
                        </div>
                    </Modal>)}

                <div className="end-account-item">
                    <a href="#" onClick={this.openModal.bind(this)}><i id={`symbol-${tmp}`} className="fa fa-ban edit-button" aria-hidden="true"></i></a>
                    <a href={`#${tmp}`} data-toggle="collapse"><i className="fa fa-info-circle edit-button" aria-hidden="true"></i></a>
                    <li id={`item-${tmp}`} >{this.props.item.email} <span id={`blocked-${tmp}`}></span> </li>

                    <div id={`${tmp}`} className="collapse">
                        <ul className="end-info-display">
                            <li> <strong>Nome:</strong> {this.props.item.name} </li>
                            <li> <strong>Identificador interno:</strong> {this.props.item.internalId} </li>
                        </ul>
                    </div>
                </div>
            </div>

        );
    }
}

export default EndAccountItem;

