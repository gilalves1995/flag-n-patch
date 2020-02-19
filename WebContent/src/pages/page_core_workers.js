import React, { Component } from 'react';
import { connect } from 'react-redux';

// Components
import WorkerItem from '../components/worker_item';

// Actions 
import { loadAdminInfo } from '../actions/actions_workers';

class WorkerManagementPage extends Component {


    getWorkers() {
        const token = JSON.parse(localStorage.getItem('token'));
        this.props.loadAdminInfo(token);
    }

    componentWillMount() {
        this.getWorkers();
    }

    render() {
        let workerItems;
        if (this.props.generalAdminInfo) {
            workerItems = this.props.generalAdminInfo.map(item => {
                return (<WorkerItem key={item.email} item={item} />)
            });
        }

        return (
            <div className="container">
                <div className="admin_table">
                    <table className="table">
                        <thead>
                            <tr>
                                <th>Email</th>
                                <th>Nome</th>
                                <th>Área</th>
                                <th>Serviços</th>
                                <th>Por resolver</th>
                            </tr>
                        </thead>
                        <tbody>
                            {workerItems}
                        </tbody>
                    </table>
                </div>
            </div>
        );
    }
}


function mapStateToProps({ generalAdminInfo }) {
    return { generalAdminInfo };
}

export default connect(mapStateToProps, { loadAdminInfo })(WorkerManagementPage);

