import React, { Component } from 'react';
import $ from 'jquery';
import axios from 'axios';
import { Link } from 'react-router-dom';
import BASE_URL from '../utils/config';

class NotificationItem extends Component {

    updateNotificationSymbolColor() {
        const notificationId = this.props.item.id;
        if (this.props.item.wasSeen === false) {
            $(`#${notificationId}`).css('background', '#43EF8A');
            $(`#${notificationId}`).css('border', '#43EF8A');
        }
        else {
            $(`#${notificationId}`).css('background', '#D5D1D0');
            $(`#${notificationId}`).css('border', '#D5D1D0');
        }
    }

    /*
    background: #A4F4C5;
        border: 1px solid #A4F4C5;
    */

    updateSeenStatus() {
        console.log("Entered updateSeenStatus");
        const notificationId = this.props.item.id;
        const token = JSON.parse(localStorage.getItem('token'));
        if (this.props.item.wasSeen === false) {
            axios.put(`${BASE_URL}/operation/seeNotification/${notificationId}`, token)
                .then(response => {
                    if (response.status === 200) {
                        this.props.item.wasSeen = true;
                        this.updateNotificationSymbolColor();
                    }
                })
                .catch(err => {
                    console.log("An error has occured when trying to see notification.");
                })
        }
    }



    handleDetailsRedirect() {
        localStorage.setItem("notification", JSON.stringify(this.props.item));
        console.log("after stringify", localStorage.getItem("notification"));
        console.log("after parse", JSON.parse(localStorage.getItem("notification")));
    }

    componentWillMount() {
        this.updateNotificationSymbolColor();
    }

    componentDidMount() {
        this.updateNotificationSymbolColor();
    }

    handleMouseEnter() {
        const notificationId = this.props.item.id;
        $(`#${notificationId}`).css('background', 'white');
        $(`#${notificationId}`).css('border', 'white');
    }

    handleMouseLeave() {
        const notificationId = this.props.item.id;
        if (this.props.item.wasSeen === false) {
            $(`#${notificationId}`).css('background', '#43EF8A');
            $(`#${notificationId}`).css('border', '#43EF8A');
        }
        else {
            $(`#${notificationId}`).css('background', '#D5D1D0');
            $(`#${notificationId}`).css('border', '#D5D1D0');
        }

    }

    render() {
        const link = "/app/user/notifications/".concat(this.props.item.id);
        const notificationId = this.props.item.id;
        return (
            <div onMouseEnter={this.handleMouseEnter.bind(this)} onMouseLeave={this.handleMouseLeave.bind(this)}
                id={notificationId} className="notification-item dropdown-item" onClick={this.updateSeenStatus.bind(this)}>
                {/*<i className="fa fa-arrow-right" aria-hidden="true"></i>*/}



                <Link style={{ color: 'black' }} to={link} role="button" className="btn btn-pink" onClick={this.handleDetailsRedirect.bind(this)}>
                    <div className="notification-item-content">
                        Uma ocorrência seguida por si sofreu uma alteração de estado de <strong> {this.props.item.prevStatus} </strong>
                        para <strong> {this.props.item.newStatus}</strong>.
                    </div>
                </Link>

            </div>
        );
    }
}

export default NotificationItem;
