// 3rd Party
import React from 'react';
import { Link } from 'react-router-dom';


const NavWorker = (props) => {

    return (
        <ul className="navbar-nav d-flex justify-content-end w-100">
            <li className="nav-item mx-auto w-75 mt-1">
            </li>
            <li className="nav-item mr-3">
                <div className="btn-group">
                    <button type="button" className="btn btn-primary dropdown-toggle" data-toggle="dropdown" aria-haspopup="true" aria-expanded="false">
                        <i className="ion-wrench navbar-brand" />
                    </button>
                    <div id="workerNavbar" className="dropdown-menu dropdown-menu-right">
                        <h6 className="dropdown-header">Menu</h6>
                        <div className="dropdown-divider"></div>
                        <Link to="/app/worker/reports" className="dropdown-item btn btn-primary admin-navbar-item" >
                            Ocorrências
                        </Link>
                        <Link to="/app/worker/map" className="dropdown-item btn btn-primary admin-navbar-item">
                            Mapa
                        </Link>
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
                        <h6 className="dropdown-header">Conta</h6>
                        <div className="dropdown-divider"></div>
                        {/* Logout Button */}
                        <Link to="/app/worker/profile" className="dropdown-item btn btn-primary admin-navbar-item">
                            Perfil
                        </Link>
                        <button className="dropdown-item btn btn-primary admin-navbar-item" onClick={props.onLogout}>
                            Logout
                        </button>
                    </div>
                </div>
            </li>
        </ul>
    );
};

export default NavWorker;