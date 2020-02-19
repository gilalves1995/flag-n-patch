import React, { Component } from 'react';
import { connect } from 'react-redux';
import { Link } from 'react-router-dom';

// Components 
import WorkerReportList from '../components/report_list_worker';
import FilterButton from '../components/button_filters';
import FilterField from '../components/filter_field';
import SideMenu from '../components/side_menu';

// Actions 
import { getWorkerReports } from '../actions/actions_workers.js';
import { clearSelectedReports } from '../actions/actions_reports';

import { addStateFilter, removeStateFilter } from '../actions/actions_reports';


class WorkerReportPage extends Component {
    constructor(props) {
        super(props);
        this.state = {
            menuActiveRight: false
        }
    }


    componentWillMount() {
        
        //this.props.clearSelectedReports();
        this.props.dispatch(clearSelectedReports(token));
        const token = JSON.parse(localStorage.getItem('token'));
        //this.props.getWorkerReports(token);
        this.props.dispatch(getWorkerReports(token));

        console.log("PROPS", this.props);
    }

    componentDidMount() {
        const token = JSON.parse(localStorage.getItem('token'));
        //this.props.getWorkerReports(token);
        this.props.dispatch(getWorkerReports(token));
    }

    //onMapRedirect() {
    //    localStorage.setItem("workerReports", JSON.stringify(this.props.selectedWorkerReports));
    //}

    toggleFilterPanel() {
        this.setState({ menuActiveRight: !this.state.menuActiveRight });
    }

     onQuitRightSideMenuClick(event) {
        event.preventDefault();
        this.setState({ menuActiveRight: false });
    }

    reportStateFilter(event) {
        const { name, checked } = event.target;
        console.log(name, checked);
        if (checked) {
            this.props.dispatch(addStateFilter(name));
        } else {
            this.props.dispatch(removeStateFilter(name));
        }
    }


    render() {
        const username = JSON.parse(localStorage.getItem('token')).user;
        let unresolvedReports = 0;
        console.log("ADDED NOW", this.props.workerReports);
        this.props.workerReports.reports.map(item => {
            if (item.statusDescription === 'Pendente' || item.statusDescription === 'Em Resolução') {
                unresolvedReports++;
            }
        });

        let mapBttn;
        if (this.props.selectedWorkerReports.length >= 1) {
            console.log("Entered renderMapLinkBttn");
            mapBttn = (
                <div className="buttonToMap">
                    <Link className="btn btn-primary form-control mt-2 mb-5"
                        role="button"
                        to="/app/worker/map"> Ver no Mapa </Link>
                </div>
            );
        }

        return (
            <div className="container h-100 w-100">
                <div className="row justify-content-left">
                    <div className="workerWelcoming">
                        <h4> Bem vindo, {username}!</h4>
                        <h6> Tem actualmente {unresolvedReports} ocorrência(s) por resolver.</h6>
                    </div>

                    
                </div>
                <WorkerReportList history={this.props.history} reports={this.props.workerReports.reports} />

                <div className="row justify-content-left">
                    {mapBttn}
                </div>

                {this.state.menuActiveRight &&
                    <SideMenu side="right">
                        <a href="#" className="quit text-muted btn btn-primary" onClick={event => this.onQuitRightSideMenuClick(event)}>&times;</a>
                        <h4 className="text-muted">Filtrar por Estado:</h4>
                        <hr />
                        <FilterField name="Pendente" onChange={this.reportStateFilter.bind(this)} />
                        <FilterField name="Em Resolução" onChange={this.reportStateFilter.bind(this)} />
                        <FilterField name="Resolvido" onChange={this.reportStateFilter.bind(this)} />
                        <FilterField name="Rejeitado" onChange={this.reportStateFilter.bind(this)} />
                    </SideMenu>}
                <FilterButton onClick={this.toggleFilterPanel.bind(this)} />
            </div>

        );
    }
}

/*
function mapStateToProps({ workerReports, selectedWorkerReports }) {
    return { workerReports, selectedWorkerReports };
}
*/

function mapStateToProps(store) {
    return {
        workerReports: store.workerReports,
        selectedWorkerReports: store.selectedWorkerReports,
        
    };
}

export default connect(mapStateToProps)(WorkerReportPage);




