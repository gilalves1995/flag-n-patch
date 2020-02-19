import React, { Component } from 'react';
import axios from 'axios';
import BASE_URL from '../utils/config';
import PieChart from 'react-simple-pie-chart';

const reportTypeColor = {
    "Árvores e Espaços Verdes": "#86af49",
    "Estradas e Sinalização": "#d5e1df",
    "Iluminação Pública": "#e3eaa7",
    "Higiene Urbana e Animais": "#618685",
    "Saneamento": "#dac292"
};

const markerColors = {
    "Pendente": "#959595",
    "Em Resolução": "#FCF004",
    "Resolvido": "#00FF00",
    "Rejeitado": "#FF0000",
    "Com erros": "black"
};


class StatisticsPage extends Component {
    constructor(props) {
        super(props);
        this.state = {
            typeGraphData: [],
            statusGraphData: [],
            username: ''
        }
    }

    loadGeneralStatisticsData() {
        const token = JSON.parse(localStorage.getItem('token'));
        axios.post(`${BASE_URL}/admin/graph/reports`, { token })
            .then(response => {
                if (response.status === 200) {
                    console.log("graph data", response.data);
                    this.setState({ typeGraphData: this.buildSliceArray(response.data.graph1) });
                    this.setState({ statusGraphData: this.buildStatusSliceArray(response.data.graph2) });
                }
            })
            .catch(err => {
                console.log("An error has occured while trying to load graph data from the server", err);
            })
    }



    componentWillMount() {
        const token = JSON.parse(localStorage.getItem('token'));
        if (token) {
            this.setState({ username: token.user });
        }
        this.loadGeneralStatisticsData();
    }

    buildSliceArray(graph) {
        console.log("in buildSliceArray", graph);

        let slices = [];
        $.each(graph, function (key, value) {
            let obj = {
                key: key,
                color: reportTypeColor[key],
                value: value
            }
            slices.push(obj);

        });
        return slices;
    }

    buildStatusSliceArray(graph) {
        console.log("in buildSliceArray", graph);

        let slices = [];
        $.each(graph, function (key, value) {
            let obj = {
                key: key,
                color: markerColors[key],
                value: value
            }
            slices.push(obj);

        });
        return slices;
    }


    /*
         if (err.response.status === 409) {
                    alert.classList.remove('d-none');
                    alert.classList.add('text-danger');
                    alert.innerHTML = 'Responsável selecionado já está encarregue deste tipo de ocorrências.';
                    setTimeout(() => {
                        alert.classList.add('d-none');
                        alert.classList.remove('text-danger');
                        alert.innerHTML = '';
                        if (this.state.selectedWorker !== "") {
                            console.log("setState will be updated...");
                            this.setState({ selectedWorker: "" });
                            this.updateSelectedWorker("");
                        }
                        this.props.dispatch(reset('ReportTypeForm'));
                    }, 3000);
                }

    */


    // 2017-07-10 ----> "date":"09/07/2017"   
    handleDateSubmit() {
        const { alert } = this.refs;
        const token = JSON.parse(localStorage.getItem('token'));
        const initialDate = $("#datePicker").val();
        const date = initialDate.split("-").reverse().join("/");

        axios.post(`${BASE_URL}/admin/graph/reports`, { token, date })
            .then(response => {
                if (response.status === 200) {
                    this.setState({ typeGraphData: this.buildSliceArray(response.data.graph1) });
                    this.setState({ statusGraphData: this.buildStatusSliceArray(response.data.graph2) });
                }
            })
            .catch(err => {
                if (err.response.status === 400) {
                    alert.classList.remove('d-none');
                    alert.classList.add('text-danger');
                    alert.innerHTML = 'Não foram encontrados dados para o dia seleccionado.';
                    setTimeout(() => {
                        alert.classList.add('d-none');
                        alert.classList.remove('text-danger');
                        alert.innerHTML = '';
                    }, 3000);
                    console.log("An error occured while trying to load statistics info.", err);
                }
            })
    }

    showGeneral() {
        const token = JSON.parse(localStorage.getItem('token'));
        axios.post(`${BASE_URL}/admin/graph/reports`, {token})
            .then(response => {
                if (response.status === 200) {
                    this.setState({ typeGraphData: this.buildSliceArray(response.data.graph1) });
                    this.setState({ statusGraphData: this.buildStatusSliceArray(response.data.graph2) });
                }
            })
            .catch(err => {
                console.log("An error occured while trying to load statistics info.", err);
            })
    }



    render() {
        let sliceData;
        let graphLegend;
        let statusSliceData;
        let statusGraphLegend;

        if (this.state.typeGraphData) {
            sliceData = this.state.typeGraphData;
            console.log("sliceData on render", sliceData);

            graphLegend = sliceData.map(item => {
                return (
                    <li key={item.key} className="graph-lengend-item">
                        <i className="fa fa-square" aria-hidden="true" style={{ color: item.color }}></i>{item.key}
                        <span className="amount"> <i>({item.value})</i></span>
                    </li>
                );
            });
        }

        if (this.state.statusGraphData) {
            statusSliceData = this.state.statusGraphData;
            console.log("sliceData on render", statusSliceData);
            statusGraphLegend = statusSliceData.map(item => {
                return (
                    <li key={item.key} className="graph-lengend-item">
                        <i className="fa fa-square" aria-hidden="true" style={{ color: item.color }}></i>{item.key}
                        <span className="amount"> <i>({item.value})</i></span>
                    </li>
                );
            });
        }

        return (
            <div className="container h-100">
                <div className="row justify-content-left custom-container">
                    <div className="statTitle">
                        <h4 className="customTitle"> Bem vindo, {this.state.username}! </h4>
                        <h6 className="customSubTitle"> Estatísticas gerais do seu concelho: </h6>
                    </div>
                    <div className="col-md-4 offset-md-8">
                        <h6 className="form-descript"> Adicione uma data: </h6>
                        <input id="datePicker" type="date" className=" form-control col-md-11" />
                        <button className="btn btn-primary col-md-6 mt-2"
                            onClick={this.handleDateSubmit.bind(this)}> No dia selecionado</button>

                        <button className="btn btn-primary col-md-5 mt-2 ml-2"
                            onClick={this.showGeneral.bind(this)}> Até hoje </button>
                        <div className="submit-error" ref="alert"> </div>
                    </div>
                </div>

                <div className="row justify-content-center custom-container mt-1">
                    <div className="col-md-4 custom-pie-chart">
                        <h6> Tipos de ocorrências desde a criação de conta: </h6>
                        <PieChart
                            slices={this.state.typeGraphData}
                        />
                        <ul className="graphLegend">
                            {graphLegend}
                        </ul>
                    </div>
                    <div className="col-md-4 offset-md-2 custom-pie-chart">
                        <h6> Estado de ocorrências: </h6>
                        <PieChart
                            slices={statusSliceData}
                        />
                        <ul className="graphLegend">
                            {statusGraphLegend}
                        </ul>
                    </div>
                </div>
            </div>

        );
    }
}

export default StatisticsPage;