import React, { Component } from 'react';
import { connect } from 'react-redux';
import { Tab, Tabs, TabList, TabPanel } from 'react-tabs';

// Components 
import EndAccountList from '../components/end_account_list';
import EndAccountRegister from '../components/form_end_account_add';
import WorkerAccountAdd from '../components/form_add_worker';


// Actions 
import { loadEndUsers } from '../actions/actions_end_users';

class AccountManagementPage extends Component {
    constructor(props) {
        super(props);
        this.state = {
            adminForm: true
        }
    }

    getEndAccountList() {
        const token = JSON.parse(localStorage.getItem('token'));
        this.props.loadEndUsers(token);
    }

    componentWillMount() {
        this.getEndAccountList();
    }

    goToWorkerForm() {
        this.setState({ adminForm: false });
    }

    goToAdminForm() {
        this.setState({ adminForm: true });
    }

    render() {
        return (
            <div className="row grid-divider">
                <div className="col-sm-6 column-one">
                    {this.state.adminForm &&
                        <div>
                            <EndAccountRegister />
                            <div className="formWithLink">
                                <a onClick={this.goToWorkerForm.bind(this)} href="#"> Registar Trabalhador </a>
                            </div>
                        </div>
                    }
                    {!this.state.adminForm &&
                        <div>
                            <WorkerAccountAdd />
                            <div className="formWithLink">
                                <a onClick={this.goToAdminForm.bind(this)} href="#"> Registar Administrador </a>
                            </div>
                        </div>}
                </div>

                <div className="col-sm-6 column-two">
                    <EndAccountList userList={this.props.endAccounts} />
                </div>
                <div id="changeNotifyMessage"></div>
            </div>

        );
    }
}


function mapStateToProps({ endAccounts }) {
    return { endAccounts };
}

export default connect(mapStateToProps, { loadEndUsers })(AccountManagementPage);

