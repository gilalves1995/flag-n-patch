// 3rd Party
import React from 'react';

// Components 
import NotificationList from './notification_list';

const NavBasic = (props) => {
    let unseen = 0;
    for(var i in props.notifications) {
        if(!props.notifications[i].wasSeen) {
            unseen ++;
        }
    }

    return (
        <ul className="navbar-nav d-flex justify-content-end w-100">
            <li className="nav-item mx-auto w-75 mt-1">
                <form className="input-group" onSubmit={e => props.onLocationSearch(e)}>
                    <input type="text" className="form-control" id="location-input" placeholder="Procurar localização..." />
                </form>
            </li>
            <li className="nav-item mr-3">
                <div className="btn-group">
                    <button className="btn btn-primary dropdown-toggle" data-toggle="dropdown" aria-haspopup="true" aria-expanded="false">
                            <i className="ion-android-notifications navbar-brand"> <span id="notiBadge" className="badge">{unseen}</span></i>    
                    </button>
                    <NotificationList notifications={props.notifications} />
                    {/*<div className="dropdown-menu dropdown-menu-right notificationDropdown">
                        <NotificationList notifications={props.notifications} />
                    </div>*/}

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
                        <button className="dropdown-item btn btn-primary" onClick={props.onLogout}>
                            Logout
                        </button>
                    </div>
                </div>
            </li>
        </ul>
    );
};

export default NavBasic;