import React, { Component } from 'react';

import { innerCoords, outerCoords } from '../utils/geojson';

//
const markerColors = {
    "Pendente": "959595",
    "Em Resolução": "FCF004",
    "Resolvido": "00FF00",
    "Rejeitado": "FF0000"
};

const ownMarkerIcons = {
    "Pendente": "icon_pendente.png",
    "Em Resolução": "icon_resolucao.png",
    "Resolvido": "icon_resolvido.png",
    "Rejeitado": "icon_rejeitado.png"
};

class GoogleMapWorker extends Component {

    shouldComponentUpdate() {
        return false;
    }

    componentDidMount() {
    
        // Instanciate the map and put it in the DOM.
        this.map = new google.maps.Map(this.refs.map, {
            scrollwheel: true,
            disableDefaultUI: true,
            disableDoubleClickZoom: true,
            zoom: 13,
            center: {
                lat: this.props.initialLat,
                lng: this.props.initialLng
            }
        });
        // Adiciona máscara e delimitações ao mapa
        this.map.data.add({ geometry: new google.maps.Data.Polygon([innerCoords, outerCoords]) });
        this.map.data.setStyle({ strokeWeight: 1, strokeOpacity: 0 });
        /*
           Se o utilizador der permissão para aceder à sua localização,
           centra na localização do mesmo.
       */
        if (navigator.geolocation) {
            navigator.geolocation.getCurrentPosition(position => {
                const pos = {
                    lat: position.coords.latitude,
                    lng: position.coords.longitude
                };

                this.map.setCenter(pos);
            });
        }
        //this.markers = this.buildMarkersArray(this.props.reports);
        //this.showMarkers();
        console.log('Reports:', this.props.reports);
        this.markers = this.props.reports.map(report => new google.maps.Marker({
            //icon: `https://chart.apis.google.com/chart?chst=d_map_pin_letter&chld=%E2%80%A2|${markerColors[report.statusDescription]}`,
            icon: {
                url: `/img/${ownMarkerIcons[report.statusDescription]}`,
                scaledSize: new google.maps.Size(35, 50),
            },

            position: {lat: report.lat, lng: report.lng},
            reportId: report.id
        }));
        console.log("before show markers", this.markers);
        this.showMarkers();
    }


    componentWillReceiveProps(nextProps) {

    }

    /*
       Extrai os markers de cada propriedade de reports e constrói um array
       de markers.
   */
    buildMarkersArray(reports) {
        const arr = [];
        for (let id in reports) {
            let report = reports[id];
            let marker = report.marker;
            marker.reportId = id;
            marker.addListener('click', () => {
                console.log('ReportID:', marker.reportId);
                this.props.onMarkerClick(marker.reportId);
            });
            arr.push(marker);
        }
        return arr;
    }

    // Torna todos os marcadores visíveis no mapa
    showMarkers() {
        console.log("on show markers", this.markers);

        //var directionsService = new google.maps.DirectionsService;
        //var directionsDisplay = new google.maps.DirectionsRenderer;

        for (let marker of this.markers) {
            marker.addListener('click', () => {
                console.log("A marker was clicked.");
                this.props.onMarkerClick(marker.reportId);

            });
            marker.setMap(this.map);
        }
    }
    render() {
        return <div id="map" ref="map" />;
    }


}

export default GoogleMapWorker; 