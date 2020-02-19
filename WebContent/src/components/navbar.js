// 3rd Party
import React, { Component } from 'react';
import { connect } from 'react-redux';
import { Link } from 'react-router-dom';

// Internal Modules
import NavBasic from './nav_basic';
import NavCore from './nav_core';
import NavWorker from './nav_worker';
import Modal from '../components/modal';
import Spinner from '../components/spinner';
import LoadingText from '../components/loading_text';
import { fetchTokenFromLocalstorage, doLogout } from '../actions/actions_token';
import NotificationList from './notification_list';

// Actions 
import { loadNotifications } from '../actions/actions_notifications';
import { searchLocation } from '../actions/actions_search_location';
import { unsetIntervalReports } from '../actions/actions_interval_reports';
import { unsetNewReportLocation } from '../actions/actions_reports';


/*
    Barra de navegação dinâmica presente em todas as páginas cuja route
    contém /app.

    É dinâmica no sentido em que se adapta a qualquer tipo de utilizador
    recorrendo a outros componentes, sendo assim um componente mais
    genérico.

    É responsável por gerir a sessão do utilizador. (Routing condicional)
*/
class Navbar extends Component {
    constructor(props) {
        super(props);

        this.state = {
            fetchedNotifications: false
        };
    }
    /*
        Verifica se já temos um token no estado da aplicação.
        Se não, tenta encontrar um token válido na localstorage.
        Se encontrar o token na localstorage este será injectado
        no estado e de seguida nas propriedades do componente,
        manifestando-se primariamente em compotWillReceiveProps.
    */
    componentWillMount() {
        if (!this.props.token.token) {
            this.props.dispatch(fetchTokenFromLocalstorage());
        }

    }

    componentWillUpdate() {
        if(this.props.token.token) {
           if((this.props.token.token.role === 'basic' || this.props.token.token.role === 'trial') && !this.state.fetchedNotifications) {
               this.props.dispatch(loadNotifications(JSON.parse(localStorage.getItem('token'))));
               this.setState({ fetchedNotifications: true });
           }
       }
    }

    componentDidUpdate() {
        if(this.props.token.token) {
           if((this.props.token.token.role === 'basic' || this.props.token.token.role === 'trial') && !this.state.fetchedNotifications) {
               this.props.dispatch(loadNotifications(JSON.parse(localStorage.getItem('token'))));
               this.setState({ fetchedNotifications: true });
           }
       }
    }

    /*
        Este método executa depois de componentWillMount e se
        um token válido tiver sido repescado da localstorage
        este obrigatóriamente estará em nextProps.
        Se não estiver significa que não se encontrou um token
        válido na localstorage e portanto redirecciona o
        utilizador para o ecrã de login.
    */
    componentWillReceiveProps(nextProps) {
        if (!nextProps.token.token) {
            this.props.history.push('/login');
        }
    }

    render() {
        // Não faz render sem ter token
        if (!this.props.token.token) {
            return <noscript />;
        }

        return (
            <nav className="navbar navbar-toggleable-md navbar-inverse bg-primary fixed-top">
                {/* Collapse */}
                <button className="navbar-toggler navbar-toggler-right" type="button" data-toggle="collapse" data-target="#navbarNav" aria-controls="navbarNav" aria-expanded="false" aria-label="Toggle navigation">
                    <span className="navbar-toggler-icon"></span>
                </button>
                {/* Brand Icon + Name */}
                <a className="navbar-brand" href="#" onClick={event => {
                    event.preventDefault();

                    this.props.dispatch(unsetNewReportLocation());
                    this.props.history.push('/login');
                }}>
                    <img src="/img/logo.svg" width={30} height={30} className="d-inline-block align-top mr-2" alt="" />
                    Flag N' Patch
                </a>
                <div className="collapse navbar-collapse mx-5" id="navbarNav">
                    {(function () {
                        switch (this.props.token.token.role) {
                            case 'trial':
                            case 'basic':
                                return <NavBasic 
                                    notifications={this.props.notificationList}
                                    onLocationSearch={this.onLocationSearch.bind(this)}
                                    onLogout={this.onLogout.bind(this)}
                                />;
                            case 'end':0
                                return <NavCore onLogout={this.onLogout.bind(this)} />
                            case 'core':
                                return <NavCore core="coreUser" onLogout={this.onLogout.bind(this)} />;
                            case 'work':
                                return <NavWorker onLogout={this.onLogout.bind(this)} />;
                        }
                    }).bind(this)()}
                </div>
                {/* Modal */}
                {this.props.token.loggingOut &&
                    <Modal>
                        <div className="d-flex justify-content-center align-items-center">
                            <div className="mr-3">
                                <Spinner />
                            </div>
                            <LoadingText message="A terminar sessão" />
                        </div>
                    </Modal>}
            </nav>
        );
    }

    onLogout() {
        this.props.dispatch(unsetIntervalReports());
        this.props.dispatch(doLogout(this.props.token.token));
    }

    onLocationSearch(event) {
        //
        event.preventDefault();

        const address = $('#location-input').val();
        $('#location-input').val('');

        const geocoder = new google.maps.Geocoder();
        geocoder.geocode({ address }, (results, status) => {
            const lat = results[0].geometry.location.lat();
            const lng = results[0].geometry.location.lng();
            this.props.dispatch(searchLocation({ lat, lng }));
        });
    }
}

/*
    Função que filtra as propriedades da store que interessam ao componente
    em questão e as torna disponíveis através de props.
*/
function mapStateToProps(store) {
    return {
        token: store.token,
        notificationList: store.notificationList,
        intervalReport: store.intervalReport
    };
}

// Ligar componente ao Redux
export default connect(mapStateToProps)(Navbar);


