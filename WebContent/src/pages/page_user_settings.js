// 3rd Party
import React, { Component } from 'react';
import { connect } from 'react-redux';


/*
    
*/
class SettingsPage extends Component {
    render() {
        return(
            <div>Settings Page</div>
        );
    }
}

function mapStateToProps(store) {
    return {
        token: store.token
    };
}

export default connect(mapStateToProps)(SettingsPage);