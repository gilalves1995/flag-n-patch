import React, { Component } from 'react';

// Components
import EndAccountItem from './end_account_item';


class EndAccountList extends Component {
    render() {
        let endAccountList;
        if (this.props.userList) {
            endAccountList = this.props.userList.map(item => {
                return (<EndAccountItem key={item.email} item={item} />);
            });
        }

        return (
            <div className="col-sm-5 pt-5 mt-5 ml-5">
                <h4 className="form-descript"> Elementos da sua autarquia: </h4>
                <div className="typeList">
                    {endAccountList}
                </div>
            </div>
        );
    }
}

export default EndAccountList;



