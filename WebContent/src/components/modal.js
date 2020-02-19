// 3rd Party
import React, { Component } from 'react';
import ReactDOM from 'react-dom';
import { Provider } from 'react-redux';

// Internal Modules
import { store } from '../index';


/*

*/
class Modal extends Component {
    componentDidMount() {
        this.modalTarget = document.createElement('div');
        this.modalTarget.className = 'modal-custom-container';
        document.body.appendChild(this.modalTarget);
        this._render();
        //$('#modal').modal('show');
    }

    componentWillUpdate() {
        this._render();
    }

    componentWillUnmount() {
        //const backdrop = document.querySelector('.modal-backdrop');
        ReactDOM.unmountComponentAtNode(this.modalTarget);
        document.body.removeChild(this.modalTarget);
        //backdrop.innerHTML = '';
        //document.body.removeChild(backdrop);
        //$('#modal').modal('hide');
    }

    _render() {
        ReactDOM.render(
            <Provider store={store}>
                <div className="modal-custom">
                    {this.props.children}
                </div>
                {/*<div className="modal fade" id="modal">
                    <div className="modal-dialog" role="document">
                        <div className="modal-content">
                            <div className="modal-body">
                                {this.props.children}
                            </div>
                        </div>
                    </div>
                </div>*/}
            </Provider>
        , this.modalTarget);
    }

    render() {
        return <noscript />;
    }
}

export default Modal;