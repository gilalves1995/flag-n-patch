import React, { Component } from 'react';
import axios from 'axios';
import BASE_URL from '../utils/config';

const markerColors = {
    "Pendente": "#959595",
    "Em Resolução": "#FCF004",
    "Resolvido": "#00FF00",
    "Rejeitado": "#FF0000"
};


class NotificationDetailsPage extends Component {
    constructor(props) {
        super(props);
        this.state = {
            notification: null,
            report: '',
            days: null,
            prevPath: ''
        }
    }

    loadReportDetails(reportId) {
        console.log("state", this.state);
        const token = JSON.parse(localStorage.getItem('token'));

        axios.post(`${BASE_URL}/report/reportById/${reportId}`, token)
            .then(response => {
                if (response.status === 200) {
                    console.log("Success!", response.data);
                    this.setState({ report: response.data });
                }
            })
            .catch(err => {
                console.log("An error occured while trying to load reports details", err);
            })
    }


    componentWillMount() {
        const notification = JSON.parse(localStorage.getItem('notification'));
        this.loadReportDetails(notification.reportId);

        // calculate how many days has passed since it was changed
        var changeDate = notification.modifiedDate.split(" ")[0].split("/").reverse().join("/");

        var start = new Date(changeDate),
            end = new Date(),
            diff = new Date(end - start),
            days = diff / 1000 / 60 / 60 / 24;

        console.log("changeDate", notification.modifiedDate);
        console.log("today", new Date());
        console.log("days have passed", days);
        console.log("days have passed 2", Math.round(days));
        this.setState({ days: Math.round(days) });
    }

    goBack() {
        this.props.history.push('/app/user/map');
    }

    render() {
        const report = this.state.report;
        const notification = JSON.parse(localStorage.getItem('notification'));
        let dayAmount;
        if (this.state.days) {

            if (this.state.days < 1) {
                dayAmount = "Hoje";
            }
            else if (this.state.days === 1) {
                dayAmount = "Há 1 dia";
            }
            else {
                dayAmount = `Há ${this.state.days} dias`;
            }
        }

        console.log("previousPath", this.state.prevPath);
        return (
            <div className="container h-100">

                <div className="row">
                    <div className="col-md-2 goBackBttnItem">
                        <i className="fa fa-arrow-left" aria-hidden="true"> <span id="goBackLabel" onClick={this.goBack.bind(this)}> Voltar </span></i>
                    </div>
                </div>
                <div className="row justify-content-left custom-container mt-2">
                    <div className="col-md-6">
                        <figure>
                            <img src={this.state.report.imageUrl} className="w-100 details-image" />
                        </figure>
                        <div className="row my-3">
                            <div className="col">
                                <strong>Morada:</strong> {report.addressAsStreet}
                            </div>
                        </div>
                        <div className="row my-3">
                            <div className="col">
                                <strong>Tipo:</strong> {report.type}
                            </div>
                        </div>
                        <div className="row my-3">
                            <div className="col">
                                <strong>Estado:</strong> {report.statusDescription}
                                <i className="fa fa-circle i" aria-hidden="true" style={{ color: markerColors[report.statusDescription] }}></i>
                            </div>
                        </div>
                        <div className="row my-3">
                            <div className="col">
                                <strong>Descrição:</strong><br />
                                {report.description}
                            </div>
                        </div>
                    </div>
                    <div className="col-md-5">
                        <div className="updateReportStatusArea">
                            <h6> Detalhes da mudança de estado: </h6>
                            <ul className="notificationDetailList">
                                <li>Mudança de estado: <strong>{notification.prevStatus}</strong> para <strong>{notification.newStatus}</strong></li>
                                <li>Modificado por <strong>{notification.modifiedBy} </strong> em
                                     <strong> {notification.modifiedDate}</strong> <i style={{ color: 'grey' }}>({dayAmount})</i></li><br />
                                <li> <strong>Descrição: </strong> {notification.description} </li>
                            </ul>
                        </div>
                    </div>
                </div>
            </div>
        );
    }



}

export default NotificationDetailsPage;
