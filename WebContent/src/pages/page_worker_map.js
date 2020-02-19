import React, { Component } from 'react';
import { connect } from 'react-redux';
import { Link } from 'react-router-dom';
import axios from 'axios';
import BASE_URL from '../utils/config';

// Components 
import GoogleMapWorker from '../components/map_google_worker';
import { INTERVAL_TIME } from '../utils/config';
import SideMenu from '../components/side_menu';


// Actions
import { getReports, setHidden, unsetHidden, setNewReportLocation } from '../actions/actions_reports';
import { setIntervalReports } from '../actions/actions_interval_reports';
import { getWorkerReports } from '../actions/actions_workers.js';

const markerColors = {
    "Pendente": "#959595",
    "Em Resolução": "#FCF004",
    "Resolvido": "#00FF00",
    "Rejeitado": "#FF0000"
};

class WorkerMapPage extends Component {
    constructor(props) {
        super(props);
        this.state = {
            menuActive: false,
            selectedReport: null,
            selectedState: null
        }
    }

    onQuitRightSideMenuClick(event) {
        event.preventDefault();
        this.setState({ menuActive: false });
    }

    componentWillMount() {
        const token = JSON.parse(localStorage.getItem('token'));
        if (this.props.selectedWorkerReports.length === 0) {
            this.props.getWorkerReports(token);
        }
    }

    goBack() {
        this.props.history.push('/app/worker/reports');
    }

    onMarkerClick(reportId) {
        console.log("workerReports", this.props.workerReports.reports);
        console.log("reportId", reportId);
        let report = this.props.workerReports.reports.find(report => report.id === reportId);

        //console.log('SELECTED REPORT CRL', this.props.reports.reports[reportId].report);
        console.log('SELECTED REPORT CRL', report);
        //this.setState({ menuActive: true, selectedReport: this.props.reports.reports[reportId].report });
        this.setState({ menuActive: true, selectedReport: report });
    }

    render() {
        const btnClasses = `d-flex justify-content-center align-items-center btn btn-danger rounded-circle`;
        console.log("workerReports", this.props.workerReports.reports);

        let reports;
        if (this.props.selectedWorkerReports.length !== 0) {
            reports = this.props.selectedWorkerReports;
        }
        else {
            reports = this.props.workerReports.reports;
        }

        let descriptionTextarea;
        if(this.state.selectedState) {
            descriptionTextarea = (<textarea placeholder="Insira uma descrição" className="w-75 mt-4 form-control"></textarea>);
        }

        return (
            <div className="h-100">
                {this.state.menuActive &&
                    <SideMenu side="left">
                        <a href="#" className="quit text-muted btn btn-primary" onClick={event => this.onQuitRightSideMenuClick(event)}>&times;</a>
                        <img className="details-image" src={this.state.selectedReport.imageUrl} />
                        <div className="row my-3">
                            <div className="col">
                                <strong>Morada:</strong> {this.state.selectedReport.addressAsStreet}
                            </div>
                        </div>
                        <div className="row my-3">
                            <div className="col">
                                <strong>Tipo:</strong> {this.state.selectedReport.type}
                            </div>
                        </div>
                        <div className="row my-3">
                            <div className="col">
                                <strong>Estado:</strong> {this.state.selectedReport.statusDescription}
                                <i className="fa fa-circle" aria-hidden="true" style={{ color: markerColors[this.state.selectedReport.statusDescription] }}></i>
                            </div>
                        </div>
                        <div className="row my-3">
                            <div className="col">
                                <strong>Descrição:</strong><br />
                                {this.state.selectedReport.description}
                            </div>
                        </div>
                        <div className="row my-3">
                            <div className="col text-right">
                                <Link to={`/app/worker/reports/details/${this.state.selectedReport.id}`}>
                                    Ver mais >>
                            </Link>
                            </div>
                        </div>
                    </SideMenu>}
                <GoogleMapWorker
                    initialLat={38.6359837}
                    initialLng={-9.1874799}
                    reports={reports}
                    onMarkerClick={this.onMarkerClick.bind(this)}
                />
                <div className="d-flex justify-content-center align-items-center goBackBttnItemMap">
                    <i onClick={this.goBack.bind(this)} className="fa fa-arrow-left" aria-hidden="true"><span> Voltar </span> </i>
                </div>
            </div>
        );
    }
}

function mapStateToProps(store) {
    return {
        selectedWorkerReports: store.selectedWorkerReports,
        workerReports: store.workerReports
    };
}

export default connect(mapStateToProps, { getWorkerReports })(WorkerMapPage);
