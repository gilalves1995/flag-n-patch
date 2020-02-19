// 3rd Party
import React, { Component } from 'react';
import { Link } from 'react-router-dom';


/*
    Landing Page da Aplicação.

    Apresentação, explicação e demonstração da Aplicação.

    Apresentação da equipa.
*/
class IndexPage extends Component {
    renderTeamMembers() {
        const team = [
            {
                name: 'João Dias',
                img: 'img/joao4.jpg',
            },
            {
                name: 'Gil Alves',
                img: 'img/gil.jpg',
            },
            {
                name: 'Michael Silva',
                img: 'img/michael1.jpg',
            },
            {
                name: 'Miguel Pereira',
                img: 'img/miguel.jpg',
            },
            {
                name: 'Ricardo Loureiro',
                img: 'img/ricardo.jpg',
            },
        ].map(member =>
            <div className="col-md d-flex justify-content-center" key={member.name}>
                <figure className="figure">
                    <img src={member.img} className="figure-img img-fluid rounded-circle"/>
                    <figcaption className="figure-caption text-center">
                        <strong>{member.name}</strong>
                    </figcaption>
                </figure>
            </div>
        );

        return(
            <div className="row py-4 w-100 d-flex justify-content-between">
                { team }
            </div>
        );
    }

    renderNews() {
        const newsCollection = [
            {
                date: '4/5/2017',
                title: 'Página do Facebook criada.',
                content: `Vejam a nossa página no <a href="https://www.facebook.com/FlagNPatch/" target="_blank">Facebook</a>!`
            },
            {
                date: '4/5/2017',
                title: 'Estudos de Frontend',
                content: 'Quase conluído! A nossa equipa está a aprendender todos os conceitos de Redux e já domina o React!'
            },
            {
                date: '4/5/2017',
                title: 'Update do Desenvolvimento de backend',
                content: 'Estamos a trabalhar nos nossos serviços e as operações principais do utilizador estão quase concluídas.'
            },
            {
                date: '24/03/2017',
                title: 'Página de apresentação lançada',
                content: 'Lançámos agora esta página para ser o nosso meio principal de reportar o nosso progresso com o mundo. Também serve como uma simples página de apresentação para nosso projeto!'
            },
            {
                date: '24/03/2017',
                title: 'Repositório principal criado',
                content: 'Criámos o repositório principal para acolher o desenvolvimento do nosso projeto, o que marca o início do nosso desenvolvimento!'
            },
            {
                date: '23/03/2017',
                title: 'Primeiro diagrama de Gantt terminado',
                content: 'O nosso diagrama de Gantt está completo e podemos começar a trabalhar. Agora temos um plano!'
            }
        ].map(news =>
            <div className="card mb-4 mx-auto" key={news.title}>
                <div className="card-header d-flex justify-content-between p-3">
                    <span className="h4">
                        {news.title}
                    </span>
                    <span className="color-main">{news.date}</span>
                </div>
                <div className="card-block">
                    <p>{news.content}</p>
                </div>
            </div>
        );

        return(
            <div className="d-flex flex-column w-100 news-container">
                { newsCollection }
            </div>
        );
    }

    render() {
        return(
            <div>
                <nav className="navbar navbar-toggleable-md navbar-inverse bg-inverse fixed-top">
                    <button className="navbar-toggler navbar-toggler-right" type="button" data-toggle="collapse" data-target="#navbarNav" aria-controls="navbarNav" aria-expanded="false" aria-label="Toggle navigation">
                        <span className="navbar-toggler-icon"></span>
                    </button>
                    <a className="navbar-brand" href="#top">
                        <img src="img/logo.svg" width={30} height={30} className="d-inline-block align-top mr-2" alt=""/>
                        Flag N' Patch
                    </a>
                    <div className="collapse navbar-collapse" id="navbarNav">
                        <ul className="navbar-nav ml-auto">
                            <li className="nav-item">
                                <a href="#howto" className="nav-link">
                                    Como usar
                                </a>
                            </li>
                            <li className="nav-item">
                                <a href="#team" className="nav-link">
                                    A equipa
                                </a>
                            </li>
                            <li className="nav-item">
                                <a href="#contacts" className="nav-link">
                                    Contactos
                                </a>
                            </li>
                            <li className="nav-item">
                                <a href="#news" className="nav-link">
                                    Notícias
                                </a>
                            </li>
                            <li className="nav-item app-launcher ml-4">
                                <Link className="nav-link btn btn-outline-success" to="/login">
                                    Web Application
                                </Link>
                            </li>
                        </ul>
                    </div>
                </nav>
                <div className="container-fluid">
                    <header className="row index-header" id="top">
                        <span className="h1 slogan">O poder de mudar a cidade...</span>
                        <span className="h1 slogan">...na palma da sua mão.</span>
                    </header>
                    <section className="row index-section" id="howto">
                        <h2 className="display-4 text-muted">Como usar</h2>
                        <p className="lead">Ao registar-se ganha o estatudo de Trial, e apenas pode registar uma ocorrência! Infelizmente nem comentar pode ! <br/>
                        Se confirmar o seu email, terá acesso imediato aos comentários e pode registar mais ocorrências ! <br/>
                            Se não quiser criar ocorrências, pode sempre apenas seguir ocorrências que já existam ! <br/>

                            Quando alguma ocorrência mudar de estado, irá ser notificado sobre tal e poderá ver mais informações sobre a alteração !
                            <br/>
                            Espermos por si na nossa aplicação e lembre-se Ajude-nos a Ajudar ! Flag N' Patch
                        </p>
                    </section>
                    <section className="row index-section" id="team">
                        <h2 className="display-4 text-muted">Equipa</h2>
                        { this.renderTeamMembers() }
                        <p className="lead">
                            A nossa equipa nasceu em meados de Fevereiro de 2017 e é formada por 5 engenheiros informáticos que diariamente trabalham para chegar a uma aplicação totalmente funcional e colaborativa. <br/>
Não existem hierarquias dentro do grupo, desde que cada elemento possa aprender e evoluir com o seu trabalho, dedicação e esforço, contribuindo igualmente para o desenvolvimento da aplicação. <br/>
Esta aplicação terá por base Web e Android para que possa ser usufruída por todos o tipo de população e tem como objectivo final ser uma ferramenta importante para o dia-a-dia da população, ajudando, por um lado, a população em geral na inserção de ocorrências que possam ter avistado. Por outro lado, as autarquias para a mais rápida resolução dos problemas a si entregues.
                        </p>
                    </section>
                    <section className="row index-section" id="contacts">
                        <h2 className="display-4 text-muted">Contactos</h2>
                        <p className="lead">Pode visitar-nos até dia 12/07/2017 ao vivo na sala 116 da Faculdade de Ciências e Tecnologias da Universidade Nova de Lisboa.
                       <br/> Pode ainda no próximo dia 18/07/2017 visitar o nosso stand na sala Ágora ao lado da biblioteca na anterior referida faculdade e conhecer o nosso produto melhor.
                          <br/>  Se tal não for possível, pode-nos contactar diretamente via Facebook na página <a target="_blank" href="https://www.facebook.com/FlagNPatch">Flag N' Patch</a> e prometemos ser breves na resposta ! Até Já !
                        </p>
                    </section>
                    <section className="row index-section" id="news">
                        <h2 className="display-4 text-muted">Notícias</h2>
                        { this.renderNews() }
                    </section>
                    <footer className="row bg-inverse p-4 d-flex justify-content-between">
                        <span className="text-white">Grupo Flag n' Patch , Sala 116 M</span>
                        <span className="text-white">&copy;2017 ADPC &mdash; FCT UNL Monte da Caparica, Almada, Portugal</span>
                    </footer>
                </div>
            </div>
        );
    }
};

export default IndexPage;
