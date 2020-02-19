// 3rd Party
import React from 'react';


/*
    Botão apresentado em cima do mapa no canto inferior
    direito e que permite criar uma ocorrência.
*/
const AddReportButton = (props) => {
    const btnClasses = `d-flex justify-content-center align-items-center btn btn-danger rounded-circle add-button${props.active? '' : ' disabled'}`;

    return(
        <div className="d-flex justify-content-center align-items-center report-button-div">
            <button className={btnClasses} onClick={props.onClick}>
                <i className="ion-plus" />
            </button>
        </div>
    );
}

export default AddReportButton;