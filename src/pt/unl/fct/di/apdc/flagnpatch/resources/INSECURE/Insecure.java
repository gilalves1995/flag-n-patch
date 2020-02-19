package pt.unl.fct.di.apdc.flagnpatch.resources.INSECURE;

import com.google.appengine.api.datastore.*;
import com.google.appengine.api.taskqueue.Queue;
import com.google.appengine.api.taskqueue.QueueFactory;
import com.google.appengine.api.taskqueue.TaskOptions;
import com.google.appengine.api.users.User;
import com.google.gson.*;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.RandomStringUtils;
import pt.unl.fct.di.apdc.flagnpatch.entities.*;
import pt.unl.fct.di.apdc.flagnpatch.inputData.*;
import pt.unl.fct.di.apdc.flagnpatch.resources.general.Utils;
import pt.unl.fct.di.apdc.flagnpatch.utils.*;

import javax.cache.Cache;
import javax.cache.CacheException;
import javax.cache.CacheFactory;
import javax.cache.CacheManager;
import javax.naming.Name;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.text.Normalizer;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.logging.Logger;

/**
 * Created by michael on 21-06-2017.
 */

@Path("/insecure")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
public class Insecure {
    private static final String[] nomes = {
            "Ana",
            "Maria",
            "Rita",
            "Inês",
            "Catarina",
            "José",
            "Manuel",
            "João",
            "Gil",
            "Michael",
            "Miguel",
            "Ricardo",
            "Vanessa"};

    private static final String[] apelidos = {"Abelheira",
            "Aboim",
            "Abrantes",
            "Abrantes",
            "Abreu",
            "Abreu",
            "Abreu",
            "Abreu",
            "Abreu",
            "Abrunhosa",
            "Adrião",
            "Agostinho",
            "Aguiar",
            "Aguiar",
            "Aires",
            "Aires",
            "Albano",
            "Albuquerque",
            "Alcântara",
            "Alecrim",
            "Alegria",
            "Aleixo",
            "Alencar",
            "Alfaiate",
            "Almeida",
            "Almeida",
            "Almeida",
            "Almeida",
            "Alvaredo",
            "Alvarenga",
            "Alves",
            "Alves",
            "Alvim",
            "Amado",
            "Amaral",
            "Amaral",
            "Amorim",
            "Amorim",
            "Amorim",
            "Andrade",
            "Andrade",
            "Andrade",
            "Anjos",
            "Anjos",
            "Antas",
            "Antunes",
            "Antunes",
            "Antunes",
            "Apolinário",
            "Aragão",
            "Aragão",
            "Aranha",
            "Arantes",
            "Araújo",
            "Areias",
            "Areias",
            "Arruda",
            "Ascenção",
            "Assis",
            "Assunção",
            "Assunção",
            "Avelar",
            "Azevedo",
            "Azevedo",
            "Bacelar",
            "Baião",
            "Balsemão",
            "Bandeira",
            "Baptista",
            "Barata",
            "Barbosa",
            "Barbosa",
            "Barbosa",
            "Barcelos",
            "Barcelos",
            "Barradas",
            "Barreira",
            "Barreiros",
            "Barreto",
            "Barros",
            "Barroso",
            "Barroso",
            "Barroso",
            "Bastos",
            "Bastos",
            "Batista",
            "Beja",
            "Beja",
            "Beltrão",
            "Bento",
            "Bento",
            "Bernardes",
            "Bernardes",
            "Bernardino",
            "Bernardino",
            "Bessa",
            "Bezerra",
            "Bispo",
            "Bispo",
            "Borba",
            "Borges",
            "Borges",
            "Borges",
            "Botelho",
            "Braga",
            "Branco",
            "Branco",
            "Brandão",
            "Brandão",
            "Brás",
            "Brito",
            "Brito",
            "Brito",
            "Cabanas",
            "Cabral",
            "Cabrita",
            "Cadete",
            "Caetano",
            "Caetano",
            "Caires",
            "Caires",
            "Cairo",
            "Cairo",
            "Calado",
            "Calapez",
            "Caldas",
            "Caldas",
            "Caldeira",
            "Calixto",
            "Camacho",
            "Camacho",
            "Camara",
            "Câmara",
            "Camargo",
            "Camões",
            "Campelo",
            "Campos",
            "Campos",
            "Campos",
            "Campos",
            "Candeias",
            "Caneira",
            "Canelas",
            "Cardoso",
            "Cardoso",
            "Cardoso",
            "Cardoso",
            "Carmo",
            "Carneiro",
            "Carneiro",
            "Carneiro",
            "Carneiro",
            "Carrasco",
            "Carreira",
            "Carreira",
            "Carreiro",
            "Carrilho",
            "Carvalhal",
            "Carvalhal",
            "Carvalheira",
            "Carvalho",
            "Carvalho",
            "Carvalho",
            "Carvalho",
            "Carvalho",
            "Castanheira",
            "Castelo",
            "Castilho",
            "Castro",
            "Castro",
            "Cavaco",
            "Cavadas",
            "Cerqueira",
            "Cerqueira",
            "Cerqueira",
            "Chaves",
            "Clemente",
            "Coelho",
            "Coelho",
            "Coelho",
            "Coelho",
            "Conceição",
            "Conceição",
            "Conceição",
            "Conde",
            "Constantino",
            "Cordeiro",
            "Correia",
            "Correia",
            "Correia",
            "Cortez",
            "Costa",
            "Costa",
            "Coutinho",
            "Couto",
            "Craveiro",
            "Crespo",
            "Cruz",
            "Cruz",
            "Cunha",
            "Cunha",
            "Custóias",

            "Damasco",
            "Dâmaso",
            "Dantas",

            "Delgado",
            "Dias",
            "Dinis",
            "Dinis",
            "Dolores",
            "Domingos",
            "Domingues",
            "Dominguez",
            "Dourado",
            "Duarte",
            "Duarte",
            "Duarte",
            "Duarte",
            "Duque",
            "Durão",
            "Eanes",
            "Encarnação",
            "Encarnação",
            "Esperança",
            "Esperança",
            "Esteves",
            "Estrada",
            "Estrela",
            "Fagundes",
            "Falcão",
            "Faria",
            "Farias",
            "Farinha",
            "Feijão",
            "Feitosa",
            "Fernandes",
            "Fernandes",
            "Fernardes",
            "Ferrão",
            "Ferraz",
            "Ferreira",
            "Ferreira",
            "Ferreira",
            "Ferreira",
            "Ferreira",
            "Ferreira",
            "Ferreira",
            "Ferro",
            "Fialho",
            "Figo",
            "Figueira",
            "Figueiredo",
            "Figueiredo",
            "Filgueiras",
            "Filho",
            "Flores",
            "Fonseca",
            "Fonseca",
            "Fontes",
            "Fortes",
            "Frade",
            "Fraga",
            "França",
            "Franco",
            "Franco",
            "Frazão",
            "Freire",
            "Freitas",
            "Freitas",
            "Frias",
            "Frutuoso",
            "Furtado",
            "Gadelha",
            "Gago",
            "Galvão",
            "Galvão",
            "Gama",
            "Gama",
            "Gama",
            "Gameiro",
            "Garcia",
            "Garcia",
            "Garcia",
            "Garcia",
            "Gaspar",
            "Gaspar",
            "Gentil",
            "Geraldes",
            "Geraldo",
            "Gil",
            "Gil",
            "Girão",
            "Godinho",
            "Gomes",
            "Gomes",
            "Gomes",
            "Gonçalves",
            "Gonçalves",
            "Gonzaga",
            "Gouveia",
            "Gouveia",
            "Gouveia",
            "Graça",
            "Graça",
            "Granado",
            "Guedes",
            "Guedes",
            "Guerra",
            "Guerra",
            "Guerreiro",
            "Guerreiro",
            "Guerreiro",
            "Guerrero",
            "Guido",
            "Guilhermino",
            "Guimarães",
            "Guimarães",
            "Guterres",
            "Henriques",
            "Hipólito",
            "Hora",
            "Horta",
            "Infante",
            "Irmão",
            "Isaías",
            "Januário",
            "Jardim",
            "Jesus",
            "Jordão",
            "Junior",
            "Júnior",
            "Junqueira",
            "Justino",
            "Lacerda",
            "Ladeia",
            "Ladeira",
            "Lage",
            "Lage",
            "Lamas",
            "Lameira",
            "Lameiras",
            "Laviano",
            "Leal",
            "Leal",
            "Leão",
            "Leitão",
            "Leite",
            "Lemos",
            "Lemos",
            "Ligeiro",
            "Lima",
            "Lima",
            "Lima",
            "Lino",
            "Lino",
            "Lisboa",
            "Litos",
            "Lobão",
            "Lobato",
            "Lobo",
            "Lobo",
            "Lomanto",
            "Lopes",
            "Loredo",
            "Loureiro",
            "Lourenço",
            "Lourenço",
            "Louro",
            "Lousada",
            "Lucas",
            "Luz",
            "Macedo",
            "Macedo",
            "Macedo",
            "Machado",
            "Machado",
            "Macieira",
            "Madeira",
            "Madureira",
            "Madureira",
            "Magalhães",
            "Maia",
            "Maia",
            "Malheiro",
            "Malveiro",
            "Manhães",
            "Manso",
            "Manso",
            "Marinho",
            "Marques",
            "Marques",
            "Marques",
            "Marquês",
            "Marquês",
            "Martins",
            "Martins",
            "Martins",
            "Martins",
            "Mascarenhas",
            "Matos",
            "Matos",
            "Matoso",
            "Medeiros",
            "Medeiros",
            "Medeiros",
            "Meira",
            "Meira",
            "Meireles",
            "Meleiro",
            "Melo",
            "Melo",
            "Mendes",
            "Mendonça",
            "Meneses",
            "Menezes",
            "Menezes",
            "Mesquita",
            "Messias",
            "Messias",
            "Miranda",
            "Miranda",
            "Miranda",
            "Moitinho",
            "Moniz",
            "Montanha",
            "Monteiro",
            "Monteiro",
            "Morais",
            "Morais",
            "Moreira",
            "Moreira",
            "Moreira",
            "Moreno",
            "Morgado",
            "Morgado",
            "Mota",
            "Mota",
            "Moura",
            "Moura",
            "Mourão",
            "Mourão",
            "Mourinho",
            "Mourinho",
            "Mourinho",
            "Moutinho",
            "Moutinho",
            "Moutinho",
            "Muniz",
            "Nascimento",
            "Nascimento",
            "Navarro",
            "Navarro",
            "Nazario",
            "Neiva",
            "Neto",
            "Neto",
            "Neves",
            "Neves",
            "Niza",
            "Nobre",
            "Nóbrega",
            "Nogueira",
            "Noronha",
            "Novaes",
            "Novo",
            "Novo",
            "Nunes",
            "Nunes",
            "Oliveira",
            "Oliveira",
            "Oliveira",
            "Oliveira",
            "Oliveira",
            "Onofre",
            "Organista",
            "Osório",
            "Osório",
            "Otelo",
            "Otero",
            "Pacheco",
            "Pacheco",
            "Paco",
            "Padrão",
            "Padre",
            "Pães",
            "Paião",
            "Paias",
            "Paiva",
            "Paiva",
            "Paixão",
            "Paladino",
            "Palermo",
            "Palhares",
            "Palma",
            "Paredes",
            "Paredes",
            "Parente",
            "Parreira",
            "Pascoal",
            "Pascoal",
            "Passos",
            "Passos",
            "Peçanha",
            "Pedreira",
            "Pedrosa",
            "Pedrosa",
            "Pedroso",
            "Pedroso",
            "Peixoto",
            "Peixoto",
            "Penha",
            "Penim",
            "Penteado",
            "Peralta",
            "Perdigão",
            "Pereira",
            "Pereira",
            "Pereira",
            "Pereira",
            "Peres",
            "Peres",
            "Peseiro",
            "Pessoa",
            "Pessoa",
            "Pestana",
            "Pietra",
            "Pimenta",
            "Pimentel",
            "Pinheiro",
            "Pinheiro",
            "Pinho",
            "Pinto",
            "Pinto",
            "Pinto",
            "Pinto",
            "Pinto",
            "Pires",
            "Pires",
            "Pires",
            "Ponte",
            "Pontes",
            "Pontes",
            "Portela",
            "Porto",
            "Porto",
            "Portugal",
            "Postiga",
            "Praça",
            "Prado",
            "Proença",
            "Prudente",
            "Quadros",
            "Quaresma",
            "Queiroga",
            "Queirós",
            "Queiroz",
            "Quintais",
            "Quintanilha",
            "Quintas",
            "Quintas",
            "Rainho",
            "Ramalho",
            "Ramires",
            "Ramirez",
            "Ramos",
            "Ramos",
            "Raposo",
            "Rebelo",
            "Rego",
            "Reis",
            "Reis",
            "Resende",
            "Resende",
            "Ribas",
            "Ribeiro",
            "Ribeiro",
            "Rinaldo",
            "Rio",
            "Rios",
            "Rocha",
            "Rocha",
            "Rodrigues",
            "Rodrigues",
            "Rodrigues",
            "Rodrigues",
            "Rodrigues",
            "Romão",
            "Romeiro",
            "Romero",
            "Ronaldo",
            "Roquete",
            "Rosa",
            "Rosário",
            "Ruas",
            "Rufino",
            "Ruivo",
            "Sá",
            "Sabrosa",
            "Sacramento",
            "Salazar",
            "Saldanha",
            "Salgado",
            "Salgado",
            "Salomão",
            "Salvador",
            "Sampaio",
            "Sanches",
            "Sanchez",
            "Santana",
            "Santiago",
            "Santos",
            "Santos",
            "Santos",
            "Santos",
            "Santos",
            "Saraiva",
            "Sardinha",
            "Sarmento",
            "Seabra",
            "Seixas",
            "Sequeira",
            "Serra",
            "Silva",
            "Silva",
            "Silva",
            "Silva",
            "Silva",
            "Silva",
            "Silva",
            "Silva",
            "Silvano",
            "Silvano",
            "Silveira",
            "Silvinho",
            "Simão",
            "Simőes",
            "Simões",
            "Soares",
            "Soares",
            "Soares",
            "Soares",
            "Sousa",
            "Sousa",
            "Sousa",
            "Sousa",
            "Souto",
            "Taborda",
            "Tavares",
            "Tavares",
            "Taveira",
            "Teixeira",
            "Teixeira",
            "Teixeira",
            "Toledo",
            "Tomé",
            "Torres",
            "Torres",
            "Torres",
            "Travassos",
            "Valente",
            "Valente",
            "Valverde",
            "Varela",
            "Vargas",
            "Vasconcelos",
            "Vasques",
            "Vasques",
            "Veiga",
            "Verde",
            "Veríssimo",
            "Viana",
            "Viana",
            "Vicente",
            "Vicente",
            "Vidal",
            "Viegas",
            "Vieira",
            "Vieira",
            "Vilaça",
            "Vilarinho",
            "Vilhena"};


    // Logger object
    private static final Logger LOG = Logger.getLogger(Insecure.class.getName());
    // Datastore link
    private static final DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
    // Converts objects to JSON
    private final Gson g = new Gson();

    // Empty constructor for code 'correctness'
    public Insecure() {
    }

    @POST
    @Path("/database")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response databaseSetup(String jsonString) {

        JsonElement jelement = new JsonParser().parse(jsonString);
        JsonObject jobject = jelement.getAsJsonObject();
        JsonArray data2 = jobject.getAsJsonArray("role");
        Map<String, ArrayList<String>> map = new HashMap<>();

        for (int i = 0; i < data2.size(); i++) {
            JsonElement e = data2.get(i);
            JsonObject j = e.getAsJsonObject();
            JsonArray arr = j.get("resources").getAsJsonArray();
            Iterator<JsonElement> iterator = arr.iterator();

            ArrayList<String> list = new ArrayList<>();

            while (iterator.hasNext()) {
                ResourcesEnum d = null;
                JsonElement t = iterator.next();
                JsonObject t2 = t.getAsJsonObject();
                String resourceNameInput = t2.get("resourceName").getAsString();
                System.out.println("input " + resourceNameInput);
                for (ResourcesEnum rr : ResourcesEnum.values()) {
                    if (rr.getResourceName().equals(resourceNameInput)) {
                        d = rr;
                        list.add(d.getResourceName());
                        break;
                    }
                }

                // list.add(d.getResourceName());
            }
            RoleSetup data = new RoleSetup(j.get(RoleSetup.PROPERTY_NAME).getAsString(),
                    j.get(RoleSetup.PROPERTY_DESCRIPTION).getAsString(),
                    list);

            //   byte [] bytes=Utils.serializeArrayList(list);
            map.put(j.get(RoleSetup.PROPERTY_NAME).getAsString(), list);


           /* Cache cache;
            try {
                CacheFactory cacheFactory = CacheManager.getInstance().getCacheFactory();
                cache = cacheFactory.createCache(Collections.emptyMap());
                cache.put("oi", list);

            } catch (CacheException e2) {
                return Response.status(Response.Status.BAD_GATEWAY).build();
            }*/

            TransactionOptions options = TransactionOptions.Builder.withXG(true);
            Transaction txn = datastore.beginTransaction(options);
            Entity reportEntity = new Entity(RoleSetup.KIND, data.name);
            reportEntity.setIndexedProperty(RoleSetup.PROPERTY_NAME, data.name);
            reportEntity.setIndexedProperty(RoleSetup.PROPERTY_DESCRIPTION, data.description);
            reportEntity.setIndexedProperty(RoleSetup.PROPERTY_RESOURCES, data.resources);
            datastore.put(txn, reportEntity);
            txn.commit();
        }

        Cache cache;
        try {
            CacheFactory cacheFactory = CacheManager.getInstance().getCacheFactory();
            cache = cacheFactory.createCache(Collections.emptyMap());
            cache.putAll(map);

        } catch (CacheException e) {
            return Response.status(Response.Status.BAD_GATEWAY).build();
        }

        map.clear();

        TransactionOptions options = TransactionOptions.Builder.withXG(true);
        Transaction txn = datastore.beginTransaction(options);
        JsonArray data3 = jobject.getAsJsonArray("districts");
        ArrayList<String> counties = new ArrayList<>();
        Entity reportEntity;
        for (int i = 0; i < data3.size(); i++) {
            JsonElement e = data3.get(i);
            JsonObject j = e.getAsJsonObject();
            JsonObject distr = j.get("district").getAsJsonObject();

            // distr.get("name").getAsString()
            String districtName = distr.get("name").getAsString();
            counties = new ArrayList<>();

            String s = Normalizer.normalize(districtName, Normalizer.Form.NFD);
            s = s.replaceAll("[^a-zA-Z0-9]", "");

            reportEntity = new Entity(Utils.ENTITY_ADDRESSES, s);

            JsonArray countiesArr = distr.get("counties").getAsJsonArray();
            Iterator<JsonElement> it = countiesArr.iterator();
            while (it.hasNext()) {

                JsonElement jp = it.next();
                JsonObject p = jp.getAsJsonObject();
                JsonObject county = p.get("county").getAsJsonObject();
                counties.add(county.get("name").getAsString());
                // System.out.println(county.get("name").getAsString());
            }


            reportEntity.setProperty("district", districtName);
            reportEntity.setProperty("counties", counties);
            datastore.put(txn, reportEntity);
            //  map.put(districtName, counties);
        }
      /*  try {
            CacheFactory cacheFactory = CacheManager.getInstance().getCacheFactory();
            cache = cacheFactory.createCache(Collections.emptyMap());
            cache.putAll(map);

        } catch (CacheException e) {
            return Response.status(Response.Status.BAD_GATEWAY).build();
        }*/

        JsonObject obj4 = jobject.getAsJsonObject("utils");
        UtilsEntity data = g.fromJson(obj4.toString(), UtilsEntity.class);

        Entity utilsEntity = new Entity(UtilsEntity.KIND, data.type);
        utilsEntity.setIndexedProperty(UtilsEntity.PROPERTY_TYPE, data.type);
        utilsEntity.setIndexedProperty(UtilsEntity.PROPERTY_NUMBER_USER, data.numUser);

        datastore.put(txn, utilsEntity);


        txn.commit();

        return Response.ok().build();
    }


    @GET
    @Path("/users")
    @Produces(MediaType.APPLICATION_JSON)
    public Response listUsers() {
        Query query = new Query(User_Global.KIND);
        Iterator<Entity> results = datastore.prepare(query).asIterator();
        List<User_Global> allUsers = new ArrayList<>();
        while (results.hasNext()) {
            Entity user = results.next();

            EmbeddedEntity embeddedEntity = (EmbeddedEntity) user.getProperty(User_Global.PROPERTY_ADDR);
            AddressData addressData = new AddressData((String) embeddedEntity.getProperty(AddressData.PROPERTY_DISTRICT),
                    (String) embeddedEntity.getProperty(AddressData.PROPERTY_COUNTY));
            String role = (String) user.getProperty(User_Global.PROPERTY_ROLE);
            if (role.equalsIgnoreCase(RolesEnum.BASIC_USER.getRoleDescription())) {
                User_FrontOffice userObject = new User_FrontOffice((String) user.getProperty(User_Global.PROPERTY_NAME),
                        user.getKey().getName(),

                        addressData,
                        (String) user.getProperty(User_Global.PROPERTY_ROLE), (int) user.getProperty(User_FrontOffice.PROPERTY_NUMBER_REPORTS),
                        (int) user.getProperty(User_FrontOffice.PROPERTY_IDENTIFIER));
                allUsers.add(userObject);
            } else {
                User_Global userObject = new User_Global((String) user.getProperty(User_Global.PROPERTY_NAME),
                        user.getKey().getName(),

                        addressData,
                        (String) user.getProperty(User_Global.PROPERTY_ROLE)
                );
                allUsers.add(userObject);
            }
        }
        return Response.ok(g.toJson(allUsers)).build();
    }

    @POST
    @Path("/lel")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response lel(String jsonString) {
        this.databaseSetup(jsonString);

        Entity user;
        Random generate = new Random();
        System.out.println("Customer: " + nomes[generate.nextInt(12)]);

        System.out.println("Customer: " + apelidos[generate.nextInt(600)]);


        for (int i = 0; i < 31; i++) {
            Transaction txn = datastore.beginTransaction();
            int smallRandom = generate.nextInt(2) + 1;
            System.out.println(smallRandom);

            if (smallRandom == 1) {
                user = buildUser_Trial();
            } else {
                user = buildUser_Basic();
            }
            datastore.put(txn, user);
            txn.commit();
        }

        RegisterData_Administration administrationObject1
                = new RegisterData_Administration("Core Almada", "almada@almada.pt", "almada@almada.pt", "pass", "pass",
                new AddressData("Setúbal", "Almada"), "32324");
        RegisterData_Administration administrationObject2
                = new RegisterData_Administration("Core Moita", "moita@moita.pt", "moita@moita.pt", "pass", "pass",
                new AddressData("Setúbal", "Moita"), "324");
        RegisterData_Administration administrationObject3
                = new RegisterData_Administration("Core Cascais", "cascais@cascais.pt", "cascais@cascais.pt", "pass", "pass",
                new AddressData("Lisboa", "Cascais"), "21212");

        ArrayList<RegisterData_Administration> list = new ArrayList<>();
        list.add(administrationObject1);
        list.add(administrationObject2);
        list.add(administrationObject3);
        Transaction txn;
        TransactionOptions options = TransactionOptions.Builder.withXG(true);

        for (int i = 0; i < 3; i++) {
            txn = datastore.beginTransaction(options);
            RegisterData_Administration administrationObject = list.get(i);
            user = buildUser_Administration(administrationObject);

            Utils.buildDefaultTypesForCore(user, txn, datastore, g, LOG);

            Areas area = new Areas(administrationObject.email, administrationObject.address.county);
            Entity areaEntity = new Entity(Areas.KIND, area.county);
            areaEntity.setIndexedProperty(Areas.PROPERTY_RESPONSIBLE, area.responsible);
            areaEntity.setIndexedProperty(Areas.PROPERTY_COUNTY, area.county);
            areaEntity.setIndexedProperty(Areas.PROPERTY_CREATION_DATE, area.creationDate);
            areaEntity.setIndexedProperty(Areas.PROPERTY_IS_AVAILABLE, area.isAvailable);
            datastore.put(txn, areaEntity);

            Graph graph = new Graph(area.county, true, System.currentTimeMillis(), false);
            Entity graphEntity = new Entity(Graph.KIND);
            graphEntity.setIndexedProperty(Graph.PROPERTY_ACTIVE, graph.active);
            graphEntity.setIndexedProperty(Graph.PROPERTY_COUNTY, graph.county);
            graphEntity.setIndexedProperty(Graph.PROPERTY_DATE_DAY, graph.date);
            graphEntity.setIndexedProperty(Graph.PROPERTY_TOTAL_EVER, graph.isTotal);

            // Sets the entity's properties.
            for (TypeEnum typeEnum : TypeEnum.values()) {
                graphEntity.setIndexedProperty(typeEnum.getTypeDescription(), 0);

            }
            for (StatusEnum statusEnum : StatusEnum.values()) {
                graphEntity.setIndexedProperty(statusEnum.getStatusDescription(), 0);
            }

            datastore.put(txn, graphEntity);

            graph = new Graph(area.county, false, System.currentTimeMillis(), true);
            graphEntity = new Entity(Graph.KIND);
            graphEntity.setIndexedProperty(Graph.PROPERTY_ACTIVE, graph.active);
            graphEntity.setIndexedProperty(Graph.PROPERTY_COUNTY, graph.county);
            graphEntity.setIndexedProperty(Graph.PROPERTY_DATE_DAY, graph.date);
            graphEntity.setIndexedProperty(Graph.PROPERTY_TOTAL_EVER, graph.isTotal);
            // Sets the entity's properties.
            for (TypeEnum typeEnum : TypeEnum.values()) {
                graphEntity.setIndexedProperty(typeEnum.getTypeDescription(), 0);

            }
            for (StatusEnum statusEnum : StatusEnum.values()) {
                graphEntity.setIndexedProperty(statusEnum.getStatusDescription(), 0);
            }
            datastore.put(txn, graphEntity);

            datastore.put(txn, user);
            txn.commit();
        }
        RegisterData_BackOffice backOfficeObject1
                = new RegisterData_BackOffice("Empresa Faz Tudo ", "eft@eft.pt", "eft@eft.pt", "pass", "pass",
                new AddressData("Setúbal", "Almada"), "123456789",
                new ArrayList<String>(Arrays.asList("Almada", "Lisboa", "Barreiro")));
        RegisterData_BackOffice backOfficeObject2
                = new RegisterData_BackOffice("Empresa Polivalente", "ep@ep.pt", "ep@ep.pt", "pass", "pass",
                new AddressData("Setúbal", "Moita"), "987654321",
                new ArrayList<String>(Arrays.asList("Almada", "Lisboa", "Barreiro", "Moita")));
        RegisterData_BackOffice backOfficeObject3
                = new RegisterData_BackOffice("Jose Vai a Todas", "josetodas@josetodas.pt", "josetodas@josetodas.pt", "pass", "pass",
                new AddressData("Lisboa", "Cascais"), "123654987",
                new ArrayList<String>(Arrays.asList("Almada", "Lisboa", "Barreiro", "Moita","Cascais")));

        ArrayList<RegisterData_BackOffice> list1 = new ArrayList<>();
        list1.add(backOfficeObject1);
        list1.add(backOfficeObject2);
        list1.add(backOfficeObject3);


        for (int i = 0; i < 3; i++) {
            txn = datastore.beginTransaction(options);
            RegisterData_BackOffice backOfficeObject = list1.get(i);
            user = buildUser_Worker(backOfficeObject);
            datastore.put(txn, user);
            txn.commit();
        }
        ArrayList<String> counties = new ArrayList<>(Arrays.asList("Almada", "Moita", "Cascais"));
        for (int i = 0; i < 3; i++) {
            String county = counties.get(i);
            txn = datastore.beginTransaction();
            Key areasKey = KeyFactory.createKey(Areas.KIND, county);
            try {
                Entity area = datastore.get(areasKey);
                area.setProperty(Areas.PROPERTY_IS_AVAILABLE, !(boolean) area.getProperty(Areas.PROPERTY_IS_AVAILABLE));
                datastore.put(txn, area);
                txn.commit();
            } catch (EntityNotFoundException e) {
                txn.rollback();
                LOG.severe("area not found");
            } finally {
                if (txn.isActive()) {
                    txn.rollback();
                    LOG.severe(Utils.OUTPUT_TRANSACTION_FAILED);
                }
            }
        }

        ArrayList<String> typeEnumList = new ArrayList<>();
        for (TypeEnum type : TypeEnum.values()) {
            typeEnumList.add(type.getTypeDescription());
        }

//   ArrayList<String> counties = new ArrayList<>(Arrays.asList("Almada", "Moita", "Cascais"));
        for (int i = 0; i < 2; i++) {
            String county = counties.get(i);
            Collections.shuffle(typeEnumList);
            for (int j = 0; j < 3; j++) {
                String type = typeEnumList.get(j);

                Query.Filter typeFilter = new Query.FilterPredicate(ReportType.PROPERTY_NAME, Query.FilterOperator.EQUAL, type);
                Query.Filter countyFilter = new Query.FilterPredicate(ReportType.PROPERTY_COUNTY, Query.FilterOperator.EQUAL, county);

                Query query = new Query(ReportType.KIND)
                        .setFilter(Query.CompositeFilterOperator.and(typeFilter, countyFilter));

                Entity result = datastore.prepare(query).asSingleEntity();
                result.setProperty(ReportType.PROPERTY_RESPONSIBLE, list1.get(i));
            }
            String type=typeEnumList.get(3);

        }

        return Response.ok().build();
    }


    @POST
    @Path("/users")
    @Produces(MediaType.APPLICATION_JSON)
    public Response addUsers(String jsonString) {

        JsonElement jelement = new JsonParser().parse(jsonString);
        JsonObject jobject = jelement.getAsJsonObject();
        JsonArray jsonArrayUsers = jobject.getAsJsonArray("users");
        //JsonArray jsonArray = jelement.getAsJsonArray();

        RegisterData_FrontOffice frontOfficeObject = null;
        RegisterData_BackOffice backOfficeObject = null;
        RegisterData_Administration administrationObject = null;
        Entity user = null;

        TransactionOptions options = TransactionOptions.Builder.withXG(true);
        Transaction txn = datastore.beginTransaction(options);

        for (int i = 0; i < jsonArrayUsers.size(); i++) {
            JsonElement jsonElement = jsonArrayUsers.get(i);
            JsonObject jsonObject = jsonElement.getAsJsonObject();
            JsonElement element = jsonObject.getAsJsonPrimitive("type");
            String type = g.fromJson(element.toString(), String.class);
            JsonObject object = jsonObject.getAsJsonObject("data");
            switch (type) {
                case "trial":
                    frontOfficeObject = g.fromJson(object.toString(), RegisterData_FrontOffice.class);
                    user = Insecure.buildEntityUser_Trial(frontOfficeObject);
                    break;
                case "basic":
                    frontOfficeObject = g.fromJson(object.toString(), RegisterData_FrontOffice.class);
                    user = Insecure.buildEntityUser_Basic(frontOfficeObject);
                    break;
            /*    case "end":
                    administrationObject = g.fromJson(object.toString(), RegisterData_Administration.class);
                    user = Utils.buildEntityUser_Administration(administrationObject);
                    break;*/
                case "core":
                    administrationObject = g.fromJson(object.toString(), RegisterData_Administration.class);
                    user = buildUser_Administration(administrationObject);
                    Utils.buildDefaultTypesForCore(user, null, datastore, g, LOG);
                    Areas area = new Areas(administrationObject.email, administrationObject.address.county);
                    Entity areaEntity = new Entity(Areas.KIND, area.county);
                    areaEntity.setIndexedProperty(Areas.PROPERTY_RESPONSIBLE, area.responsible);
                    areaEntity.setIndexedProperty(Areas.PROPERTY_COUNTY, area.county);
                    areaEntity.setIndexedProperty(Areas.PROPERTY_CREATION_DATE, area.creationDate);
                    areaEntity.setIndexedProperty(Areas.PROPERTY_IS_AVAILABLE, area.isAvailable);
                    datastore.put(txn, areaEntity);


                    Graph graph = new Graph(area.county, true, System.currentTimeMillis(), false);
                    Entity graphEntity = new Entity(Graph.KIND);
                    graphEntity.setIndexedProperty(Graph.PROPERTY_ACTIVE, graph.active);
                    graphEntity.setIndexedProperty(Graph.PROPERTY_COUNTY, graph.county);
                    graphEntity.setIndexedProperty(Graph.PROPERTY_DATE_DAY, graph.date);
                    graphEntity.setIndexedProperty(Graph.PROPERTY_TOTAL_EVER, graph.isTotal);

                    // Sets the entity's properties.
                    for (TypeEnum typeEnum : TypeEnum.values()) {
                        graphEntity.setIndexedProperty(typeEnum.getTypeDescription(), 0);

                    }
                    for (StatusEnum statusEnum : StatusEnum.values()) {
                        graphEntity.setIndexedProperty(statusEnum.getStatusDescription(), 0);
                    }

                    datastore.put(txn, graphEntity);

                    graph = new Graph(area.county, false, System.currentTimeMillis(), true);
                    graphEntity = new Entity(Graph.KIND);
                    graphEntity.setIndexedProperty(Graph.PROPERTY_ACTIVE, graph.active);
                    graphEntity.setIndexedProperty(Graph.PROPERTY_COUNTY, graph.county);
                    graphEntity.setIndexedProperty(Graph.PROPERTY_DATE_DAY, graph.date);
                    graphEntity.setIndexedProperty(Graph.PROPERTY_TOTAL_EVER, graph.isTotal);
                    // Sets the entity's properties.
                    for (TypeEnum typeEnum : TypeEnum.values()) {
                        graphEntity.setIndexedProperty(typeEnum.getTypeDescription(), 0);

                    }
                    for (StatusEnum statusEnum : StatusEnum.values()) {
                        graphEntity.setIndexedProperty(statusEnum.getStatusDescription(), 0);
                    }
                    datastore.put(txn, graphEntity);

                    break;
                case "work":
                    backOfficeObject = g.fromJson(object.toString(), RegisterData_BackOffice.class);
                    user = buildUser_Worker(backOfficeObject);
                    break;
            }
            datastore.put(txn, user);

        }

        txn.commit();
        JsonObject jsonObject = jobject.getAsJsonObject("defineAreaStatus");
        JsonPrimitive jsonPrimitive = jsonObject.getAsJsonPrimitive("area");
        String county = jsonPrimitive.getAsString();



      /*  Key areasKey = KeyFactory.createKey(Areas.KIND, county);
        try {
            Entity area = datastore.get(areasKey);
            area.setProperty(Areas.PROPERTY_IS_AVAILABLE, !(boolean) area.getProperty(Areas.PROPERTY_IS_AVAILABLE));
            datastore.put(txn, area);
            txn.commit();
        } catch (EntityNotFoundException e) {
            txn.rollback();
            LOG.severe("area not found");
        } finally {
            if (txn.isActive()) {
                txn.rollback();
                LOG.severe(Utils.OUTPUT_TRANSACTION_FAILED);
            }
        }*/
        options = TransactionOptions.Builder.withXG(true);
        txn = datastore.beginTransaction(options);

        options = TransactionOptions.Builder.withXG(true);
        txn = datastore.beginTransaction(options);


        JsonArray jsonArrayDefineWorker = jobject.getAsJsonArray("defineWorker");
        for (int i = 0; i < jsonArrayDefineWorker.size(); i++) {
            JsonElement jsonElement = jsonArrayDefineWorker.get(i);
            jsonObject = jsonElement.getAsJsonObject();
            JsonElement element = jsonObject.getAsJsonPrimitive("county");
            county = g.fromJson(element.toString(), String.class);
            element = jsonObject.getAsJsonPrimitive("email");
            String email = g.fromJson(element.toString(), String.class);
            JsonArray arr = jsonObject.getAsJsonArray("type");
            ArrayList<String> list = new ArrayList<String>();
            String type = null;
            for (int j = 0; j < arr.size(); j++) {
                type = arr.get(j).getAsString();
                Query.Filter typeFilter = new Query.FilterPredicate(ReportType.PROPERTY_NAME, Query.FilterOperator.EQUAL, type);
                Query.Filter countyFilter = new Query.FilterPredicate(ReportType.PROPERTY_COUNTY, Query.FilterOperator.EQUAL, county);

                Query query = new Query(ReportType.KIND)
                        .setFilter(Query.CompositeFilterOperator.and(typeFilter, countyFilter));

                Entity result = datastore.prepare(query).asSingleEntity();
                result.setProperty(ReportType.PROPERTY_RESPONSIBLE, email);
                datastore.put(txn, result);
            }


        }
        txn.commit();


        options = TransactionOptions.Builder.withXG(true);
        txn = datastore.beginTransaction(options);


        Query.Filter trialFilter = new Query.FilterPredicate(User_Global.PROPERTY_ROLE, Query.FilterOperator.EQUAL, RolesEnum.TRIAL_USER.getRoleDescription());
        Query.Filter basicFilter = new Query.FilterPredicate(User_Global.PROPERTY_ROLE, Query.FilterOperator.EQUAL, RolesEnum.BASIC_USER.getRoleDescription());


        Query query = new Query(User_Global.KIND)
                .setFilter(Query.CompositeFilterOperator.or(trialFilter, basicFilter));

        ArrayList<String> users = new ArrayList<>();

        Iterator<Entity> results = datastore.prepare(query).asIterator();
        while (results.hasNext()) {
            Entity entity = results.next();
            Key key = entity.getKey();

            //key.getName()
            users.add(key.getName());
        }
        txn.commit();
        final Queue queue = QueueFactory.getDefaultQueue();

        JsonArray jsonArrayReports = jobject.getAsJsonArray("reports");
        for (int i = 0; i < jsonArrayReports.size(); i++) {
            JsonElement jsonElement = jsonArrayReports.get(i);
            jsonObject = jsonElement.getAsJsonObject();
            JsonElement element = jsonObject.getAsJsonPrimitive("county");
            county = g.fromJson(element.toString(), String.class);
            element = jsonObject.getAsJsonPrimitive("district");
            String district = g.fromJson(element.toString(), String.class);
            JsonObject jlimits = jsonObject.getAsJsonObject("limits");
            JsonElement minLatString = jlimits.get("minLat");
            JsonElement maxLatString = jlimits.get("maxLat");
            JsonElement minLngString = jlimits.get("minLng");
            JsonElement maxLngString = jlimits.get("maxLng");
            double minLat = Double.parseDouble(minLatString.getAsString());
            double maxLat = Double.parseDouble(maxLatString.getAsString());
            double minLng = Double.parseDouble(minLngString.getAsString());
            double maxLng = Double.parseDouble(maxLngString.getAsString());


            AddressData addressData = new AddressData(district, county);

/*
            for (int y = 0; y < 2; y++) {
                options = TransactionOptions.Builder.withXG(true);
                txn = datastore.beginTransaction(options);
                double foundLatitude = randomInRange(minLat, maxLat);
                double foundLongitude = randomInRange(minLng, maxLng);
                String type = randomType();


                Query.Filter filter = Query.CompositeFilterOperator.and(
                        new Query.FilterPredicate(ReportType.PROPERTY_COUNTY, Query.FilterOperator.EQUAL, county),
                        new Query.FilterPredicate(ReportType.PROPERTY_NAME, Query.FilterOperator.EQUAL, type));

                Query query2 = new Query(ReportType.KIND).setFilter(filter);
                Entity entity = datastore.prepare(query2).asSingleEntity();
                String responsible = (String) entity.getProperty(ReportType.PROPERTY_RESPONSIBLE);

                int randomPriority = randomPriority();
                ReportData reportData = new ReportData(type, randomPriority, randomDescription(),
                        "/img/missing.png", randomDescription(), foundLatitude, foundLongitude, addressData);

                Entity reportEntity = new Entity(Report.KIND);
                long registerTime = System.currentTimeMillis();
                String randomUser = randomUser(users);

                reportEntity.setIndexedProperty(Report.PROPERTY_USER, randomUser);
                reportEntity.setIndexedProperty(Report.PROPERTY_TYPE, reportData.type);
                reportEntity.setIndexedProperty(Report.PROPERTY_PRIORITY, reportData.priority);
                reportEntity.setIndexedProperty(Report.PROPERTY_STATUS_DESCRIPTION, StatusEnum.SUBMITED.getStatusDescription());
                reportEntity.setProperty(Report.PROPERTY_DESCRIPTION, reportData.description);
                reportEntity.setProperty(Report.PROPERTY_IMAGE, reportData.imageUrl);
                reportEntity.setIndexedProperty(Report.PROPERTY_ADDRESS_AS_STREET, reportData.addressAsStreet);
                reportEntity.setIndexedProperty(Report.PROPERTY_ADDRESS, Utils.buildAddress(reportData.address));

                reportEntity.setIndexedProperty(Report.PROPERTY_LATITUDE, foundLatitude);
                reportEntity.setIndexedProperty(Report.PROPERTY_LONGITUDE, foundLongitude);
                reportEntity.setProperty(Report.PROPERTY_FOLLOWERS, 1);
                reportEntity.setProperty(Report.PROPERTY_CREATION_DATE, registerTime);
                reportEntity.setIndexedProperty(Report.PROPERTY_WORKER_RESPONSIBLE, responsible);
                reportEntity.setIndexedProperty(Report.PROPERTY_SUM_PRIORITY, reportData.priority);


                int points = 0;
                if (!reportData.imageUrl.trim().equalsIgnoreCase(Utils.DEFAULT_IMAGE.trim())) {
                    points += Utils.IMAGE_POINTS;
                }
                points += reportData.priority * 1;

                reportEntity.setIndexedProperty(Report.PROPERTY_POINTS, points);

                Key reportKey = datastore.put(txn, reportEntity);
                String id = String.valueOf(reportKey.getId());

                Entity statusLogEntity = (Entity) (Utils.newStatusLogObject(StatusEnum.SUBMITED.getStatusDescription(),
                        Utils.DATABASE_USER_REPORT_LOG, reportKey, randomUser))[0];
                datastore.put(txn, statusLogEntity);

                Entity followerEntity = new Entity(Follower.KIND);
                followerEntity.setProperty(Follower.PROPERTY_EMAIL, randomUser);
                followerEntity.setProperty(Follower.PROPERTY_REPORT, String.valueOf(reportEntity.getKey().getId()));
                followerEntity.setProperty(Follower.PROPERTY_PRIORITY, randomPriority);
                followerEntity.setProperty(Follower.PROPERTY_FOLLOW_DATE, Utils.generateCustomDate());
                followerEntity.setProperty(Follower.PROPERTY_FOLLOW_TIME, registerTime);

                datastore.put(txn, followerEntity);

                queue.add(TaskOptions.Builder.withUrl("/rest/report/changeGraphInfoOfCounty").etaMillis(System.currentTimeMillis() + 10000)
                        .param("county", reportData.address.county)
                        .param("typeReport", reportData.type)
                        .header("Content-Type", "application/html; charset=utf8"));
                txn.commit();

                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }*/

        }


        // TODO: INSERT REPORTS: :)

        return Response.ok().build();
    }

    @POST
    @Path("/newGraph")
    @Produces(MediaType.APPLICATION_JSON)
    public void newGraph() {
        LOG.info("Starting the GraphNewDay routine...");

        Transaction txn = datastore.beginTransaction();

        Query query = new Query(Areas.KIND);
        Iterator<Entity> areaEntityIterator = datastore.prepare(query).asIterator();

        txn.rollback();

        while (areaEntityIterator.hasNext()) {
            Entity areaEntity = areaEntityIterator.next();
            String county = (String) areaEntity.getProperty(Areas.PROPERTY_COUNTY);
            ArrayList<String> typesOfArea = new ArrayList<>();

            txn = datastore.beginTransaction();

            Query.Filter filter = new Query.FilterPredicate(ReportType.PROPERTY_COUNTY, Query.FilterOperator.EQUAL, county);
            query = new Query(ReportType.KIND).setFilter(filter);
            Iterator<Entity> results = datastore.prepare(query).asIterator();

            txn.rollback();

            while (results.hasNext()) {
                Entity type = results.next();
                typesOfArea.add((String) type.getProperty(ReportType.PROPERTY_NAME));
            }

            Calendar cal = Calendar.getInstance(); //current date and time
            cal.add(Calendar.DAY_OF_MONTH, 1);
            cal.setTimeZone(TimeZone.getTimeZone("Europe/Lisbon"));
            long nextDayMillis = cal.getTimeInMillis();

            txn = datastore.beginTransaction();

            Query.Filter filterByCounty = new Query.FilterPredicate(Graph.PROPERTY_COUNTY, Query.FilterOperator.EQUAL, county);
            Query.Filter filterByActive = new Query.FilterPredicate(Graph.PROPERTY_ACTIVE, Query.FilterOperator.EQUAL, true);
            query = new Query(Graph.KIND).setFilter(
                    Query.CompositeFilterOperator.and(filterByActive, filterByCounty));

            Entity entityOfGraph = datastore.prepare(query).asSingleEntity();

            entityOfGraph.setIndexedProperty(Graph.PROPERTY_ACTIVE, false);
            datastore.put(txn, entityOfGraph);

            txn.commit();
            txn = datastore.beginTransaction();

            Graph graph = new Graph(county, true, nextDayMillis, false);
            Entity graphEntity = new Entity(Graph.KIND);
            graphEntity.setIndexedProperty(Graph.PROPERTY_ACTIVE, graph.active);
            graphEntity.setIndexedProperty(Graph.PROPERTY_COUNTY, graph.county);
            graphEntity.setIndexedProperty(Graph.PROPERTY_DATE_DAY, graph.date);
            graphEntity.setIndexedProperty(Graph.PROPERTY_TOTAL_EVER, graph.isTotal);

            // Sets the entity's properties.
            for (String type : typesOfArea) {
                graphEntity.setIndexedProperty(type, 0);

            }
            for (StatusEnum statusEnum : StatusEnum.values()) {
                graphEntity.setIndexedProperty(statusEnum.getStatusDescription(), 0);
            }

            datastore.put(txn, graphEntity);
            txn.commit();
        }

      /*  Transaction txn = datastore.beginTransaction();

        Query query = new Query(Areas.KIND);
        Iterator<Entity> entityIterator = datastore.prepare(query).asIterator();
        txn.rollback();
        TransactionOptions options = TransactionOptions.Builder.withXG(true);

        while (entityIterator.hasNext()) {
            Entity entity = entityIterator.next();
            String county = (String) entity.getProperty(Areas.PROPERTY_COUNTY);
            Calendar cal = Calendar.getInstance(); //current date and time
            cal.setTimeZone(TimeZone.getTimeZone("Europe/Lisbon"));
            cal.add(Calendar.DAY_OF_MONTH, 1);
            long nextDayMillis = cal.getTimeInMillis();

            txn = datastore.beginTransaction();

            Query.Filter filterByCounty = new Query.FilterPredicate(Graph.PROPERTY_COUNTY, Query.FilterOperator.EQUAL, county);
            Query.Filter filterByActive = new Query.FilterPredicate(Graph.PROPERTY_ACTIVE, Query.FilterOperator.EQUAL, true);
            query = new Query(Graph.KIND).setFilter(
                    Query.CompositeFilterOperator.and(filterByActive, filterByCounty));

            Entity entityOfGraph = datastore.prepare(query).asSingleEntity();

            entityOfGraph.setProperty(Graph.PROPERTY_ACTIVE, false);
            datastore.put(txn, entityOfGraph);
            txn.commit();
            txn = datastore.beginTransaction();

            Graph graph = new Graph(county, true, nextDayMillis,false);
            Entity graphEntity = new Entity(Graph.KIND);
            graphEntity.setProperty(Graph.PROPERTY_ACTIVE, graph.active);
            graphEntity.setProperty(Graph.PROPERTY_COUNTY, graph.county);
            graphEntity.setProperty(Graph.PROPERTY_DATE_DAY, graph.date);
            graphEntity.setProperty(Graph.PROPERTY_TOTAL_EVER, graph.isTotal);

            // Sets the entity's properties.
            for (TypeEnum typeEnum : TypeEnum.values()) {
                graphEntity.setProperty(typeEnum.getTypeDescription(), 0);

            }
            for (StatusEnum statusEnum : StatusEnum.values()) {
                graphEntity.setProperty(statusEnum.getStatusDescription(), 0);
            }

            datastore.put(txn, graphEntity);

            txn.commit();
        }*/

    }


    @POST
    @Path("/newGraphSum")
    @Produces(MediaType.APPLICATION_JSON)
    public void newGraphSum() {
        //Transaction txn = datastore.beginTransaction();

        Query query = new Query(Areas.KIND);
        Iterator<Entity> entityIterator = datastore.prepare(query).asIterator();
       // txn.rollback();

        while (entityIterator.hasNext()) {

            Entity entity = entityIterator.next();
            String county = (String) entity.getProperty(Areas.PROPERTY_COUNTY);


            ArrayList<String> typesOfArea = new ArrayList<>();


            Query.Filter filter = new Query.FilterPredicate(ReportType.PROPERTY_COUNTY, Query.FilterOperator.EQUAL, county);
            query = new Query(ReportType.KIND).setFilter(filter);
            Iterator<Entity> results = datastore.prepare(query).asIterator();



            while (results.hasNext()) {
                Entity type = results.next();
                typesOfArea.add((String) type.getProperty(ReportType.PROPERTY_NAME));
            }


            Query.Filter filterByCounty = new Query.FilterPredicate(Graph.PROPERTY_COUNTY, Query.FilterOperator.EQUAL, county);
            Query.Filter filterByActive = new Query.FilterPredicate(Graph.PROPERTY_ACTIVE, Query.FilterOperator.EQUAL, true);
           // Query.Filter filterByDate = new Query.FilterPredicate(Graph.PROPERTY_DATE_DAY, Query.FilterOperator.EQUAL, creationTime);
            Query.Filter filterByIsTotal = new Query.FilterPredicate(Graph.PROPERTY_TOTAL_EVER, Query.FilterOperator.EQUAL, false);

            query = new Query(Graph.KIND).setFilter(
                    Query.CompositeFilterOperator.and(filterByActive, filterByCounty, filterByIsTotal));

            Entity entityOfGraph = datastore.prepare(query).asSingleEntity();


           Transaction txn = datastore.beginTransaction();

           filterByCounty = new Query.FilterPredicate(Graph.PROPERTY_COUNTY, Query.FilterOperator.EQUAL, county);
        filterByIsTotal = new Query.FilterPredicate(Graph.PROPERTY_TOTAL_EVER, Query.FilterOperator.EQUAL, true);

            query = new Query(Graph.KIND).setFilter(
                    Query.CompositeFilterOperator.and(filterByCounty, filterByIsTotal));

            Entity entityOfGraphTotal = datastore.prepare(query).asSingleEntity();

            for (String type : typesOfArea) {

                int newValue = (int) ((long) entityOfGraph.getProperty(type));
                int before = (int) ((long) entityOfGraphTotal.getProperty(type));
                entityOfGraphTotal.setIndexedProperty(type, before + newValue);
                datastore.put(txn, entityOfGraphTotal);
            }
            for (StatusEnum statusEnum : StatusEnum.values()) {


                int newValue = (int) ((long) entityOfGraph.getProperty(statusEnum.getStatusDescription()));
                int before = (int) ((long) entityOfGraphTotal.getProperty(statusEnum.getStatusDescription()));
                entityOfGraphTotal.setIndexedProperty(statusEnum.getStatusDescription(), before + newValue);
                datastore.put(txn, entityOfGraphTotal);
            }

            txn.commit();



        /*    for(int i=0; i< ;i++){

            }*/


            //txn = datastore.beginTransaction();

            /*Query.Filter filterByCounty = new Query.FilterPredicate(Graph.PROPERTY_COUNTY, Query.FilterOperator.EQUAL, county);
            Query.Filter filterByActive = new Query.FilterPredicate(Graph.PROPERTY_ACTIVE, Query.FilterOperator.EQUAL, false);
            Query.Filter filterByIsTotal = new Query.FilterPredicate(Graph.PROPERTY_TOTAL_EVER, Query.FilterOperator.EQUAL, true);
            query = new Query(Graph.KIND).setFilter(
                    Query.CompositeFilterOperator.and(filterByActive, filterByCounty, filterByIsTotal));

            Entity entityOfGraph = datastore.prepare(query).asSingleEntity();*/


        }
    }


    private String randomType() {
        ArrayList<String> arr = new ArrayList<>();

        for (TypeEnum typeEnum : TypeEnum.values())
            arr.add(typeEnum.getTypeDescription());

        Random random = new Random();

        // randomly selects an index from the arr
        int select = random.nextInt(arr.size());

        // prints out the value at the randomly selected index
        System.out.println("Random String selected: " + arr.get(select));
        return arr.get(select);
    }

    private String randomUser(ArrayList<String> users) {
        Random random = new Random();

        // randomly selects an index from the arr
        int select = random.nextInt(users.size());

        // prints out the value at the randomly selected index
        System.out.println("Random String selected: " + users.get(select));
        return users.get(select);
    }


    private int randomPriority() {
        Random r = new Random();
        int Low = 1;
        int High = 6;
        return r.nextInt(High - Low) + Low;
    }

    private String randomDescription() {
        char[] chars = "abcdefghijklmnopqrstuvwxyz ".toCharArray();
        StringBuilder sb = new StringBuilder();
        Random random = new Random();
        for (int i = 0; i < 20; i++) {
            char c = chars[random.nextInt(chars.length)];
            sb.append(c);
        }
        String output = sb.toString();
        return output;
    }


    @SuppressWarnings("incomplete-switch")
    private final Response registerUser(Object dataIn, RolesEnum role) {

        String email = ((RegisterData_Global) dataIn).email;
        RegisterData_FrontOffice frontOfficeObject = null;
        RegisterData_BackOffice backOfficeObject = null;
        RegisterData_Administration administrationObject = null;

        // Cast the object to the correct type &&
        // Checks whether the registration data is valid.
        // If not, terminates execution and replies to the client with
        // BAD_REQUEST.
        boolean failed = false;

        if (dataIn instanceof RegisterData_FrontOffice) {
            frontOfficeObject = (RegisterData_FrontOffice) dataIn;
            if (!frontOfficeObject.validRegistrationForFrontOffice())
                failed = true;

        } else if (dataIn instanceof RegisterData_BackOffice) {
            backOfficeObject = (RegisterData_BackOffice) dataIn;
            if (!backOfficeObject.validRegistration())
                failed = true;

        } else if (dataIn instanceof RegisterData_Administration) {
            administrationObject = (RegisterData_Administration) dataIn;
            if (!administrationObject.validRegistration())
                failed = true;
        }

        if (failed) {
            // TODO: change message
            LOG.warning("Did not register user " + email + " because the submitted data was invalid.");
            return Response.status(Response.Status.BAD_REQUEST).entity(g.toJson(new StringUtil(Utils.OUTPUT_WRONG_FIELDS))).build();
        }

        // Begins a transaction.
        //TransactionOptions options = TransactionOptions.Builder.withXG(true);
        //Transaction txn = datastore.beginTransaction(options);

        try {
            // Builds the key using the provided email addressAsStreet.
            // Key userKey = KeyFactory.createKey(User.KIND, data.email);
            Key userKey = KeyFactory.createKey(User_Global.KIND, email);
            // Throws EntityNotFoundException if the provided email addressAsStreet is
            // not in use.
            @SuppressWarnings("unused")
            Entity user = datastore.get(userKey);

            // If the provided email addressAsStreet is in use, terminates the
            // transaction.
            //  txn.rollback();
            LOG.warning(Utils.OUTPUT_USER_ALREADY_EXISTS + " " + email);
            return Response.status(Response.Status.CONFLICT).entity(g.toJson(new StringUtil(Utils.OUTPUT_USER_ALREADY_EXISTS))).build();

        } catch (EntityNotFoundException e) {
            Entity user = null;
            switch (role) {
                case TRIAL_USER: {
                    //user = Utils.buildEntityUser_Trial(frontOfficeObject);
                    for (int i = 0; i < 501; i++) {
                        user = Insecure.buildEntityUser_Trial(frontOfficeObject);
                        datastore.put(user);
                    }

                }
                break;
                case WORKER_USER: {
                    //user = Utils.buildEntityUser_Worker(backOfficeObject);
                }
                break;
                case CORE_USER: {
                    //  user = Utils.buildEntityUser_Administration(administrationObject);
                    //  Utils.buildDefaultTypesForCore(user, null, datastore, g, LOG);
                }
                break;
                case END_USER: {
                    //   user = Utils.buildEntityUser_End(administrationObject);
                    //Utils.buildDefaultTypesForCore(user, txn, datastore, g, LOG);
                }
                break;
            }
            //datastore.put(txn, user);
            // txn.commit();
            // sendConfirmationEmail(user, email);
            // Informs the client that the operation finished successfully.
            LOG.info(Utils.OUTPUT_USER_REGISTERED + " " + email);
            return Response.ok(g.toJson(new StringUtil(Utils.OUTPUT_USER_REGISTERED))).build();
        } finally {
            // Checks if something went wrong and the transaction is still
            // active.
            //    if (txn.isActive()) {
            //         txn.rollback();
            //         LOG.severe(Utils.OUTPUT_TRANSACTION_STILL_ACTIVE);
            //         return Response.status(Status.INTERNAL_SERVER_ERROR)
            //                 .entity(g.toJson(new StringUtil(Utils.OUTPUT_TRANSACTION_STILL_ACTIVE))).build();
            //     }
        }
    }


    @GET
    @Path("/test")
    @Produces(MediaType.APPLICATION_JSON)
    public Response testegil() {

        Query.Filter filterByCounty = new Query.FilterPredicate("address.county",
                Query.FilterOperator.EQUAL, "Barreiro");
        Query query = new Query(Report.KIND).setFilter(filterByCounty);

        Iterator<Entity> it = datastore.prepare(query).asIterator();
        //List<String> allUsers = new ArrayList<>();
        while (it.hasNext()) {
            Entity report = it.next();
            System.out.println(report.getProperty(Report.PROPERTY_ADDRESS));

        }
        return
                Response.ok().build();
    }


    @POST
    @Path("/reports")
    @Produces(MediaType.APPLICATION_JSON)
    public Response registerNewReport(String jsonString) {

/*Extracts data from custom JSON input string and creates objects from it*/
        JsonElement jelement = new JsonParser().parse(jsonString);
        JsonObject jobject = jelement.getAsJsonObject();
        JsonObject jreport = jobject.getAsJsonObject("report");
        JsonObject jtoken = jobject.getAsJsonObject("token");
        JsonObject jlimits = jobject.getAsJsonObject("limits");
        JsonElement minLatString = jlimits.get("minLat");
        JsonElement maxLatString = jlimits.get("maxLat");
        JsonElement minLngString = jlimits.get("minLng");
        JsonElement maxLngString = jlimits.get("maxLng");
        double minLat = Double.parseDouble(minLatString.getAsString());
        double maxLat = Double.parseDouble(maxLatString.getAsString());
        double minLng = Double.parseDouble(minLngString.getAsString());
        double maxLng = Double.parseDouble(maxLngString.getAsString());
        ReportData data = g.fromJson(jreport.toString(), ReportData.class);
        AuthToken token = g.fromJson(jtoken.toString(), AuthToken.class);
        LOG.info("Attempting to register a new report from user: " + token.user);

        /* Security Validations */
        Entity user = Utils.getUser(token.user, datastore, LOG);
        if (user == null) {
            LOG.warning(Utils.OUTPUT_MISSING_USER);
            return Response.status(Response.Status.UNAUTHORIZED).entity(new StringUtil(Utils.OUTPUT_MISSING_USER)).build();
        }
        Response rsp = Utils.securityValidations(user, token, ResourcesEnum.FRONTOFFICE_REGISTER_NEW_REPORT, datastore,
                LOG);
        if (rsp.getStatus() != Response.Status.ACCEPTED.getStatusCode())
            return rsp;
        /* Security Validations */

        // Validates the report fields
        if (!data.validReport()) {
            // If missing or wrong fields were introduced, it returns HTTP 400
            // code
            LOG.warning(Utils.OUTPUT_WRONG_FIELDS);
            return Response.status(Response.Status.BAD_REQUEST).entity(g.toJson(new StringUtil(Utils.OUTPUT_WRONG_FIELDS))).build();
        }

        String responsible = "ninguem";

        // In case token is valid and report fields are valid, begin transaction

        // Creates a new entity for the report and saves it on database

        for (int i = 0; i < 101; i++) {
            Entity reportEntity = new Entity(Report.KIND);
            long registerTime = System.currentTimeMillis();

            reportEntity.setIndexedProperty(Report.PROPERTY_USER, token.user);
            reportEntity.setIndexedProperty(Report.PROPERTY_TYPE, data.type);
            reportEntity.setIndexedProperty(Report.PROPERTY_PRIORITY, data.priority);
            reportEntity.setIndexedProperty(Report.PROPERTY_STATUS_DESCRIPTION, StatusEnum.SUBMITED.getStatusDescription());
            reportEntity.setIndexedProperty(Report.PROPERTY_DESCRIPTION, data.description);
            reportEntity.setIndexedProperty(Report.PROPERTY_IMAGE, data.imageUrl);
            reportEntity.setIndexedProperty(Report.PROPERTY_ADDRESS_AS_STREET, data.addressAsStreet);
            reportEntity.setIndexedProperty(Report.PROPERTY_ADDRESS, Utils.buildAddress(data.address));

          /*  Random random = new Random();

            // Convert radius from meters to degrees
            double radiusInDegrees = 2000 / 111000f;

            double u = random.nextDouble();
            double v = random.nextDouble();
            double w = radiusInDegrees * Math.sqrt(u);
            double t = 2 * Math.PI * v;
            double x = w * Math.cos(t);
            double y = w * Math.sin(t);
            double x0=-9.194373;
            double y0=38.655926;
            // Adjust the x-coordinate for the shrinking of the east-west distances
            double new_x = x / Math.cos(Math.toRadians(y0));

            double foundLongitude = new_x + x0;
            double foundLatitude = y + y0;*/


           /* 38.603569, -9.199595
            38.667230, -9.201192

            38.669386, -9.153033
            38.605046, -9.152533*/
/*
// 6378000 Size of the Earth (in meters)
            double longitudeD = (Math.asin(1000 / (6378000 * Math.cos(Math.PI*latitude/180))))*180/Math.PI;
            double latitudeD = (Math.asin((double)1000 / (double)6378000))*180/Math.PI;

            double latitudeMax = latitude+(latitudeD);
            double latitudeMin = latitude-(latitudeD);
            double longitudeMax = longitude+(longitudeD);
            double longitudeMin = longitude-(longitudeD);*/
            double foundLatitude = randomInRange(minLat, maxLat);
            double foundLongitude = randomInRange(minLng, maxLng);

            reportEntity.setIndexedProperty(Report.PROPERTY_LATITUDE, foundLatitude);
            reportEntity.setIndexedProperty(Report.PROPERTY_LONGITUDE, foundLongitude);
            reportEntity.setIndexedProperty(Report.PROPERTY_FOLLOWERS, 1);
            reportEntity.setProperty(Report.PROPERTY_CREATION_DATE, registerTime);
            reportEntity.setProperty(Report.PROPERTY_WORKER_RESPONSIBLE, responsible);

            Key reportKey = datastore.put(reportEntity);
            String id = String.valueOf(reportKey.getId());

            Entity statusLogEntity = (Entity) (Utils.newStatusLogObject(StatusEnum.SUBMITED.getStatusDescription(),
                    Utils.DATABASE_USER_REPORT_LOG, reportKey, token.user))[0];
            datastore.put(statusLogEntity);

            Entity followerEntity = new Entity(Follower.KIND);
            followerEntity.setProperty(Follower.PROPERTY_EMAIL, token.user);
            followerEntity.setProperty(Follower.PROPERTY_REPORT, String.valueOf(reportEntity.getKey().getId()));
            followerEntity.setProperty(Follower.PROPERTY_PRIORITY, data.priority);
            followerEntity.setProperty(Follower.PROPERTY_FOLLOW_DATE, Utils.generateCustomDate());
            followerEntity.setProperty(Follower.PROPERTY_FOLLOW_TIME, registerTime);

            datastore.put(followerEntity);
        }
        return Response.ok().build();
    }


    public static double randomInRange(double min, double max) {
        Random random = new Random();
        double range = max - min;
        double scaled = random.nextDouble() * range;
        double shifted = scaled + min;
        return shifted; // == (rand.nextDouble() * (max-min)) + min;
    }

    @POST
    @Path("/points")
    public Response reportsUpdatePointsRoutine() {
        LOG.info("Starting the ReportsUpdatePoints routine...");
        Transaction txn = datastore.beginTransaction();
        Query.Filter filterByStatus1 = new Query.FilterPredicate(Report.PROPERTY_STATUS_DESCRIPTION,
                Query.FilterOperator.EQUAL, StatusEnum.SUBMITED.getStatusDescription());
        Query.Filter filterByStatus2 = new Query.FilterPredicate(Report.PROPERTY_STATUS_DESCRIPTION,
                Query.FilterOperator.EQUAL, StatusEnum.IN_RESOLUTION.getStatusDescription());
        Query query = new Query(Report.KIND).setFilter(Query.CompositeFilterOperator.or(filterByStatus1, filterByStatus2));
        List<Entity> results = datastore.prepare(query).asList(FetchOptions.Builder.withDefaults());
        txn.rollback();

        if (results.size() == 0) {
            return Response.ok().build();
        }

        for (Entity report : results) {
            txn = datastore.beginTransaction();
            long oldPoints = (long) report.getProperty(Report.PROPERTY_POINTS);
            report.setIndexedProperty(Report.PROPERTY_POINTS, (int) oldPoints + 50);
            datastore.put(txn, report);
            txn.commit();
        }
        return Response.ok().build();
    }


    @POST
    @Path("/try")
    @Produces(MediaType.APPLICATION_JSON)
    public void getLocation() {
        Random random = new Random();

        // Convert radius from meters to degrees
        double radiusInDegrees = 2000 / 111000f;

        double u = random.nextDouble();
        double v = random.nextDouble();
        double w = radiusInDegrees * Math.sqrt(u);
        double t = 2 * Math.PI * v;
        double x = w * Math.cos(t);
        double y = w * Math.sin(t);
        double x0 = -9.194373;
        double y0 = 38.655926;
        // Adjust the x-coordinate for the shrinking of the east-west distances
        double new_x = x / Math.cos(Math.toRadians(y0));

        double foundLongitude = new_x + x0;
        double foundLatitude = y + y0;
        System.out.println("Longitude: " + foundLongitude + "  Latitude: " + foundLatitude);
    }

    public static Entity buildEntityUser_Trial(RegisterData_FrontOffice data) {
        // Creates the User entity.
        //String email = generateEmail("gmail.com", 8);
        Entity user = new Entity(User_Global.KIND, data.email);
        //Entity user = new Entity(User_Global.KIND, data.email);

        // Sets the entity's properties.

        user.setProperty(User_Global.PROPERTY_NAME, data.name);
        user.setProperty(User_Global.PROPERTY_PWD, DigestUtils.sha512Hex(data.password));
        user.setIndexedProperty(User_Global.PROPERTY_ADDR, Utils.buildAddress(data.address));

        user.setProperty(User_Global.PROPERTY_ROLE, RolesEnum.TRIAL_USER.getRoleDescription());
        user.setProperty(User_Global.PROPERTY_ACCOUNT_BLOCKED, false);
        // TODO: o random nao deveria existir
        Random random = new Random();
        int randomNumber = random.nextInt(1000 + 1 - 1) + 1;
        user.setProperty(User_FrontOffice.PROPERTY_IDENTIFIER, randomNumber);
        //TODO: CHANGE ACCOUNT VERIFIED
        user.setProperty(User_Global.PROPERTY_ACCOUNT_VERIFIED, false);
        user.setProperty(User_FrontOffice.PROPERTY_NUMBER_REPORTS, 0);
        return user;
    }

    public static Entity buildEntityUser_Basic(RegisterData_FrontOffice data) {
        // Creates the User entity.
        //String email = generateEmail("gmail.com", 8);
        Entity user = new Entity(User_Global.KIND, data.email);
        //Entity user = new Entity(User_Global.KIND, data.email);

        // Sets the entity's properties.

        user.setProperty(User_Global.PROPERTY_NAME, data.name);
        user.setProperty(User_Global.PROPERTY_PWD, DigestUtils.sha512Hex(data.password));
        user.setIndexedProperty(User_Global.PROPERTY_ADDR, Utils.buildAddress(data.address));

        user.setProperty(User_Global.PROPERTY_ROLE, RolesEnum.BASIC_USER.getRoleDescription());
        user.setProperty(User_Global.PROPERTY_ACCOUNT_BLOCKED, false);
        // TODO: o random nao deveria existir
        Random random = new Random();
        int randomNumber = random.nextInt(1000 + 1 - 1) + 1;
        user.setProperty(User_FrontOffice.PROPERTY_IDENTIFIER, randomNumber);
        //TODO: CHANGE ACCOUNT VERIFIED
        user.setProperty(User_Global.PROPERTY_ACCOUNT_VERIFIED, true);
        user.setProperty(User_FrontOffice.PROPERTY_NUMBER_REPORTS, 0);
        return user;
    }

    public static String generateEmail(String domain, int length) {
        return RandomStringUtils.random(length, "abcdefghijklmnopqrstuvwxyz") + "@" + domain;
    }

    public static Entity buildUser_Trial() {
        // Creates the User entity.
        Random generate = new Random();
        String firstName = nomes[generate.nextInt(12)];
        String lastName = apelidos[generate.nextInt(600)];
        String email = firstName + lastName + "@trial.com";

        Entity user = new Entity(User_Global.KIND, email);

        // Sets the entity's properties.

        user.setProperty(User_Global.PROPERTY_NAME, firstName + " " + lastName);
        user.setProperty(User_Global.PROPERTY_PWD, DigestUtils.sha512Hex("pass"));
        AddressData addressData = new AddressData("Lisboa", "Lisboa");
        user.setIndexedProperty(User_Global.PROPERTY_ADDR, Utils.buildAddress(addressData));
        user.setProperty(User_Global.PROPERTY_ROLE, RolesEnum.TRIAL_USER.getRoleDescription());
        user.setProperty(User_Global.PROPERTY_ACCOUNT_BLOCKED, false);
        // TODO: o random nao deveria existir
        Random rn = new Random();
        int randomNumber = generate.nextInt(1000 + 1 - 1) + 1;
        user.setProperty(User_FrontOffice.PROPERTY_IDENTIFIER, randomNumber);
        //TODO: CHANGE ACCOUNT VERIFIED
        user.setProperty(User_Global.PROPERTY_ACCOUNT_VERIFIED, false);
        user.setProperty(User_FrontOffice.PROPERTY_NUMBER_REPORTS, 0);
        return user;
    }

    public static Entity buildUser_Basic() {
        // Creates the User entity.
        Random generate = new Random();
        String firstName = nomes[generate.nextInt(12)];
        String lastName = apelidos[generate.nextInt(600)];
        String email = firstName + lastName + "@basic.com";

        Entity user = new Entity(User_Global.KIND, email);

        // Sets the entity's properties.

        user.setProperty(User_Global.PROPERTY_NAME, firstName + " " + lastName);
        user.setProperty(User_Global.PROPERTY_PWD, DigestUtils.sha512Hex("pass"));
        AddressData addressData = new AddressData("Lisboa", "Lisboa");
        user.setIndexedProperty(User_Global.PROPERTY_ADDR, Utils.buildAddress(addressData));
        user.setProperty(User_Global.PROPERTY_ROLE, RolesEnum.BASIC_USER.getRoleDescription());
        user.setProperty(User_Global.PROPERTY_ACCOUNT_BLOCKED, false);
        // TODO: o random nao deveria existir
        Random rn = new Random();
        int randomNumber = generate.nextInt(1000 + 1 - 1) + 1;
        user.setProperty(User_FrontOffice.PROPERTY_IDENTIFIER, randomNumber);
        //TODO: CHANGE ACCOUNT VERIFIED
        user.setProperty(User_Global.PROPERTY_ACCOUNT_VERIFIED, true);
        user.setProperty(User_FrontOffice.PROPERTY_NUMBER_REPORTS, 0);
        return user;
    }

    public static Entity buildUser_Administration(RegisterData_Administration data) {
        // Creates the User entity.
        Entity user = new Entity(User_Global.KIND, data.email);

        // Sets the entity's properties.

        user.setProperty(User_Global.PROPERTY_NAME, data.name);
        user.setProperty(User_Global.PROPERTY_PWD, DigestUtils.sha512Hex(data.password));
        user.setIndexedProperty(User_Global.PROPERTY_ADDR, Utils.buildAddress(data.address));
        user.setProperty(User_Administration.PROPERTY_INTERNAL_ID, data.internalId);

        user.setProperty(User_Global.PROPERTY_ROLE, RolesEnum.CORE_USER.getRoleDescription());
        user.setProperty(User_Global.PROPERTY_ACCOUNT_BLOCKED, false);


        //TODO: CHANGE ACCOUNT VERIFIED
        user.setProperty(User_Global.PROPERTY_ACCOUNT_VERIFIED, true);
        return user;
    }


    public static Entity buildUser_Worker(RegisterData_BackOffice data) {
        // Creates the User entity.
        // String email = generateEmail("gmail.com", 6);

        Entity user = new Entity(User_Global.KIND, data.email);

        // Sets the entity's properties.

        user.setProperty(User_Global.PROPERTY_NAME, data.name);
        user.setProperty(User_Global.PROPERTY_PWD, DigestUtils.sha512Hex(data.password));
        user.setIndexedProperty(User_Global.PROPERTY_ADDR, Utils.buildAddress(data.address));
        user.setProperty(User_BackOffice.PROPERTY_NIF, data.nif);
        user.setProperty(User_BackOffice.PROPERTY_WORKING_AREA, data.workingArea);
        user.setProperty(User_Global.PROPERTY_ROLE, RolesEnum.WORKER_USER.getRoleDescription());


        user.setProperty(User_Global.PROPERTY_ACCOUNT_BLOCKED, false);
        //TODO: CHANGE ACCOUNT VERIFIED
        user.setProperty(User_Global.PROPERTY_ACCOUNT_VERIFIED, true);

        user.setProperty(User_BackOffice.PROPERTY_WORKER_INFO, "");
        return user;
    }
}
