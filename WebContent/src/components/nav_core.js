
// 3rd Party
import React, { Component } from 'react';
import { Link } from 'react-router-dom';
import SwitchButton from 'react-switch-button';
import axios from 'axios';
import BASE_URL from '../utils/config';


/*
*/

class NavCore extends Component {
    constructor(props) {
        super(props);
        this.state = {
            isAreaAvailable: false
        }
    }

    loadAreaState() {
        const token = JSON.parse(localStorage.getItem('token'));
        axios.post(`${BASE_URL}/admin/areaManagement/getAreaStatus`, token)
            .then(response => {
                console.log("response", response.data)
                if (response.data === true) {
                    $("[name='switch-1']").attr('checked', true);
                    $("#activeAreaIndicator").html("Activada");
                    $("#activeAreaIndicator").css('color', 'green');
                }
                else {
                    $("[name='switch-1']").attr('checked', false);
                    $("#activeAreaIndicator").html("Desactivada");
                    $("#activeAreaIndicator").css('color', 'red');
                }
            })
            .catch(err => {
                console.log("An error has occured while trying to load area state from the server.");
            })
    }

    activationButtonSetup() {
        $('#coreNavbar').on('click', function (event) {
            event.stopPropagation();
        });
        $("[name='switch-1']").on('change', function () {
            const token = JSON.parse(localStorage.getItem('token'));
            axios.post(`${BASE_URL}/admin/areaManagement/changeStatus`, token)
                .then(response => {
                    if (response.data === true) {
                        $("#activeAreaIndicator").html("Activada");
                        $("#activeAreaIndicator").css('color', 'green');
                        $("#changeNotifyMessage").html("A área do seu concelho foi activada com sucesso.");
                        $("#changeNotifyMessage").css('color', 'green');
                        setTimeout(function () {
                            $("#changeNotifyMessage").html("");
                        }, 3000);
                    }
                    else {
                        $("#activeAreaIndicator").html("Desactivada");
                        $("#activeAreaIndicator").css('color', 'red');
                        $("#changeNotifyMessage").html("A área do seu concelho foi desactivada.");
                        $("#changeNotifyMessage").css('color', 'red');
                        setTimeout(function () {
                            $("#changeNotifyMessage").html("");
                        }, 3000);
                    }
                })
                .catch(err => {
                    console.log("An error has occured while trying to update the area activation status.", err);
                })
        });
    }

    componentWillMount() {
        this.loadAreaState();
        this.activationButtonSetup();
    }

    componentDidMount() {
        this.loadAreaState();
        this.activationButtonSetup();
    }

    render() {
        let endAccountBttn;
        let activationAreaBttn;

        if (this.props.core) {
            console.log("é um utilizador core");
            endAccountBttn = (
                    <Link to="/app/core/management/accounts" className="dropdown-item btn btn-primary admin-navbar-item">
                        Contas
                    </Link>
                );

            activationAreaBttn = (
                <div>
                    <h6 className="dropdown-header">Àrea</h6>
                    <div className="dropdown-divider"></div>
                    <div className="dropdown-item">
                        <SwitchButton name="switch-1" />
                        <span id="activeAreaIndicator"> Desactivada </span>
                    </div>
                </div>
            );
        }
        else {
            activationAreaBttn = (
                <div>
                    <h6 className="dropdown-header">Àrea</h6>
                    <div className="dropdown-divider"></div>
                    <div className="dropdown-item">
                        <span id="activeAreaIndicator"> Desactivada </span>
                    </div>
                </div>
            );
        }


        return (
            <ul className="navbar-nav d-flex justify-content-end w-100">
                <li className="nav-item">
                    <div className="btn-group">
                        <button type="button" className="btn btn-primary dropdown-toggle" data-toggle="dropdown" aria-haspopup="true" aria-expanded="false">
                            <i className="ion-wrench navbar-brand" />
                        </button>
                        <div id="coreNavbar" className="dropdown-menu dropdown-menu-right">
                            <h6 className="dropdown-header">Gestão</h6>
                            <div className="dropdown-divider"></div>
                                <Link to="/app/core/management/types" className="dropdown-item btn btn-primary admin-navbar-item">
                                    Ocorrência Tipos
                                </Link>
                                <Link to="/app/core/management/workers" className="dropdown-item btn btn-primary admin-navbar-item">
                                    Trabalhadores
                                </Link>
                                <Link to="/app/core/management/statistics" className="dropdown-item btn btn-primary admin-navbar-item">
                                    Estatísticas
                                </Link>
                            {endAccountBttn}
                            {activationAreaBttn}
                        </div>
                    </div>
                </li>
                {/* User Menu Button */}
                <li className="nav-item">
                    <div className="btn-group">
                        <button type="button" className="btn btn-primary dropdown-toggle" data-toggle="dropdown" aria-haspopup="true" aria-expanded="false">
                            <i className="ion-android-person navbar-brand" />
                        </button>
                        <div className="dropdown-menu dropdown-menu-right">
                            <div className="dropdown-divider"></div>
                            {/* Logout Button */}
                            <button className="dropdown-item btn btn-primary" onClick={this.props.onLogout}>
                                Logout
                        </button>
                        </div>
                    </div>
                </li>
            </ul>
        );
    }
}

export default NavCore;
