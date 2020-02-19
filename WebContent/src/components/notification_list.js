import React, { Component } from 'react';

// Components 
import NotificationItem from './notification_item';


class NotificationList extends Component {
    render() {
        let items;
        console.log("this.props.notifications", this.props.notifications);
        if (this.props.notifications) {
            if (this.props.notifications.length === 0) {
                console.log("length", this.props.notifications.length);
            }
            else {
                items = this.props.notifications.sort(function (a, b) {
                    return b.exactModifiedDate - a.exactModifiedDate;
                }).map(item => {
                    return (<NotificationItem key={item.id} item={item} />);
                });
            }
        }
        return (
            <div className="dropdown-menu dropdown-menu-right scrollable-menu notificationDropdown">
                {items || <div>Sem notificações</div>}
            </div>
        );
    }
}
export default NotificationList;
