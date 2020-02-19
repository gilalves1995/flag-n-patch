// 3rd Party
import React, { Component } from 'react';
import { connect } from 'react-redux';
import { Link } from 'react-router-dom';

// Internal Modules
import GoogleMap from '../components/map_google';
import AddReportButton from '../components/button_add_report';
import SideMenu from '../components/side_menu';
import FilterButton from '../components/button_filters';
import FilterField from '../components/filter_field';
import { INTERVAL_TIME } from '../utils/config';

// Actions
import { getReports, setHidden, unsetHidden, setNewReportLocation, addStateFilter, removeStateFilter } from '../actions/actions_reports';
import { setIntervalReports } from '../actions/actions_interval_reports';


//
const markerColors = {
    "Pendente": "#959595",
    "Em Resolução": "#FCF004",
    "Resolvido": "#00FF00",
    "Rejeitado": "#FF0000"
};

/*
    
*/
class MapPage extends Component {
    constructor(props) {
        super(props);

        this.state = {
            btnActive: true,
            menuActiveLeft: false,
            menuActiveRight: false,
            selectedReport: null
        };

        // Add event listener to ESC key
        document.onkeydown = event => {
            event = event || window.event;
            if (event.keyCode === 27)
                this.onEscapePress();
        };
    }

    /*
        Faz o pedido dos markers somente se existir uma sessão.
    */
    componentWillMount() {
        const previousLocation = localStorage.getItem('previousLocation');
        if (previousLocation)
            localStorage.removeItem('previousLocation');

        if (this.props.token.token && !previousLocation) {
            this.props.dispatch(getReports(this.props.token.token));
            
            // Se não temos o interval definido (rotina background)
            if (!this.props.intervalReport) {
                const intervalId = setInterval(() => {
                    console.log('[Background Routine] Reports');
                    this.props.dispatch(getReports(this.props.token.token));
                }, INTERVAL_TIME);
                this.props.dispatch(setIntervalReports(intervalId));
            }
        } else if (this.props.reports.hidden) {
            this.props.dispatch(unsetHidden());
        }
    }

    /*
        Se existirem coordenadas para adicionar um novo report,
        faz redirect para a página de adicionar report.
    */
    componentWillReceiveProps(nextProps) {
        if (nextProps.newReportLocation)
            this.props.history.push('/app/user/report');
    }

    componentWillUnmount() {
        document.onkeydown = null;
    }

    render() {
        return(
            <div className="h-100">
                {this.state.menuActiveLeft &&
                <SideMenu side="left">
                    <a href="#" className="quit text-muted btn btn-primary" onClick={event => this.onQuitLeftSideMenuClick(event)}>&times;</a>
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
                            <i className="fa fa-circle" aria-hidden="true" style={{color: markerColors[this.state.selectedReport.statusDescription]}}></i>
                        </div>
                    </div>
                    <div className="row my-3">
                        <div className="col">
                            <strong>Descrição:</strong><br/>
                            {this.state.selectedReport.description}
                        </div>
                    </div>
                    <div className="row my-3">
                        <div className="col text-right">
                            <Link to={`/app/user/report/${this.state.selectedReport.id}`}>
                                Ver mais >>
                            </Link>
                        </div>
                    </div>
                </SideMenu>}
                {this.state.menuActiveRight &&
                <SideMenu side="right">
                    <a href="#" className="quit text-muted btn btn-primary" onClick={event => this.onQuitRightSideMenuClick(event)}>&times;</a>
                    <h4 className="text-muted">Filtrar por Estado:</h4>
                    <hr/>
                    <FilterField name="Pendente" onChange={this.reportStateFilter.bind(this)} />
                    <FilterField name="Em Resolução" onChange={this.reportStateFilter.bind(this)} />
                    <FilterField name="Resolvido" onChange={this.reportStateFilter.bind(this)} />
                    <FilterField name="Rejeitado" onChange={this.reportStateFilter.bind(this)} />
                </SideMenu>}
                <GoogleMap
                    initialLat={38.6359837}
                    initialLng={-9.1874799}
                    reports={this.props.reports}
                    onLocationSelected={this.onLocationSelected.bind(this)}
                    onMarkerClick={this.onMarkerClick.bind(this)}
                    searchLocation={this.props.searchLocation}
                />
                <FilterButton onClick={this.toggleFilterPanel.bind(this)}/>
                <AddReportButton
                    active={this.state.btnActive}
                    onClick={this.onAddReportClick.bind(this)}
                />
                {!this.state.btnActive &&
                <div className="exit-info">
                    <strong>Pressione ESC para sair do modo Adicionar Ocorrência</strong>
                </div>}
            </div>
        );
    }

    onAddReportClick() {
        this.setState({ btnActive: false, menuActiveLeft: false });
        this.props.dispatch(setHidden());
    }

    onEscapePress() {
        this.setState({ btnActive: true, menuActiveLeft: false, menuActiveRight: false });
        this.props.dispatch(unsetHidden());
    }

    onLocationSelected(coords) {
        this.props.dispatch(setNewReportLocation(coords));
    }

    onMarkerClick(reportId) {
        this.setState({ menuActiveLeft: true, selectedReport: this.props.reports.reports[reportId].report });
    }

    onQuitLeftSideMenuClick(event) {
        event.preventDefault();

        this.setState({ menuActiveLeft: false });
    }

    onQuitRightSideMenuClick(event) {
        event.preventDefault();

        this.setState({ menuActiveRight: false });
    }

    toggleFilterPanel() {
        this.setState({ menuActiveRight: !this.state.menuActiveRight });
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
}

function mapStateToProps(store) {
    return {
        token: store.token,
        reports: store.reports,
        newReportLocation: store.newReportLocation,
        searchLocation: store.searchLocation,
        intervalReport: store.intervalReport
    };
}

export default connect(mapStateToProps)(MapPage);