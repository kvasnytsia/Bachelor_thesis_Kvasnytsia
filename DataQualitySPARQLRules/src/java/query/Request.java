package query;

import com.hp.hpl.jena.query.ResultSetFormatter;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.StringReader;
import org.xml.sax.*;
import org.xml.sax.helpers.*;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.sparql.core.DatasetImpl;
import com.hp.hpl.jena.rdf.model.Model;
import excel.WriteSheet;
import java.util.Iterator;

/**
 *
 * @author of the origional file: karsten the functions queryAsConstruct and
 * formatFederatedQuery were written by Lea The function queryAsText was edited
 * by Lea to query 2 endpoints in one Query
 */
public class Request {

    // Services
    public static final String fuseki_local_cnt = "http://localhost:3030/dump/sparql";
//    public static final String fuseki_local_cnt_notPublic = "http://localhost:3030/CNT_notreadytopublish_inMem/query";
//    public static final String fuseki_local_afe = "http://localhost:3030/ocre_nomisma_afe/query";
//    public static final String rgk_endpoint = "http://afe.dainst.org/rdf/sparql";
//    public static final String pl_endpoint = "http://frcpl.uw.edu.pl/rdf/sparql";
    
    // Stored queries
    String query_ask = "ASK { }";

    //checking whether the selected endpoints are alive

    public boolean alive(String service) {
        QueryExecution qe_ask = QueryExecutionFactory.sparqlService(service, query_ask);
        return qe_ask.execAsk();
    }

    //querying the Select querys to the given endpoints

    public String queryAsText(String service, String query, int numEndpoints) {
        if (numEndpoints == 1) {
            //preparing the statement with the given service and query
            QueryExecution qe_select = QueryExecutionFactory.sparqlService(service, query);
            try {
                //executing the Query
                return ResultSetFormatter.asXMLString(qe_select.execSelect());
                
            } catch (Exception e) {
                System.err.println(e.getLocalizedMessage());
                System.out.println(service + " is DOWN");
                return service + " not reachable";
            }
        }
        if (numEndpoints == 2) {
            //prepare the statement with the federated Query. 
            //the following line was taken from https://github.com/ncbo/sparql-code-examples/blob/master/java/src/org/ncbo/stanford/sparql/examples/JenaARQFederationExample.java
            QueryExecution qe_select = QueryExecutionFactory.create(QueryFactory.create(query), new DatasetImpl(ModelFactory.createDefaultModel()));
            try {
                //sending the federated query to an endpoint. Since it contains the SERVICE keyword, the endpoint its send to, doesn´t matter
                return ResultSetFormatter.asXMLString(qe_select.execSelect());

            } catch (Exception e) {
                System.err.println(e.getLocalizedMessage());
                System.out.println("At least one endpoint is DOWN");
                return "one or more endpoints not reachable";
            }
        }
        return null;
    }

    //Generating and ecexuting the CONSTRUCT-Querys for the Graph Search

    public String queryAsConstruct(String service, String query, int numEndpoints) {
        //query = prefix + query;
        if (numEndpoints == 1) {
            //prepare the statement for the query and the given endpoint
            QueryExecution qe_construct = QueryExecutionFactory.sparqlService(service, query);
            try {
                //execute the query and start cutting the result string
                Model graph_model = qe_construct.execConstruct();
                String[] triples1;
                triples1 = (graph_model.toString()).split(" \\|  \\[");
                String[] triples_afe = (triples1[1]).split("]>");
                String graph_modele = triples_afe[0];
                //send it to GenerateGraphPattern to get the right structure for plotting the Graph
                String result = GenerateGraphPattern.generateNodesEdges(graph_modele);
                return (result);

            } catch (Exception e) {
                return null;
            }
        }
        if (numEndpoints == 2) {
            //get the results for both endpoints
            QueryExecution qe_construct_afe = QueryExecutionFactory.sparqlService("http://afe.dainst.org/rdf/sparql", query);
            QueryExecution qe_construct_pl = QueryExecutionFactory.sparqlService("http://frcpl.uw.edu.pl/rdf/sparql", query);
            try {
                Model graph_model_afe = qe_construct_afe.execConstruct();
                Model graph_model_pl = qe_construct_pl.execConstruct();
                //after ececuting both querys, cut the strings to fit the right pattern
                String[] triples1;
                triples1 = (graph_model_afe.toString()).split(" \\|  \\[");
                String[] triples_afe = (triples1[1]).split("]>");
                String[] triples2 = (graph_model_pl.toString()).split(" \\|  \\[");
                String[] triples_pl = (triples2[1]).split("\\]>");
                //append the two results and send it to Generate Graph pattern
                String graph_modele = triples_pl[0] + "] [" + triples_afe[0];
                String result = GenerateGraphPattern.generateNodesEdges(graph_modele);
                return (result);

            } catch (Exception e) {
                return null;
            }
        }
        return null;
    }

    //the format of the wished result is given. Not fully implemented yet. 

    public String queryAs(String service, String query, int format, int numEndpoints) {

        switch (format) {
            case 0:
                return queryAsHTML(service, query, numEndpoints);
            case 1:
                return this.queryAsText(service, query, numEndpoints);// xml
            case 2:
                ResultSetFormatter.outputAsCSV(true);
                return ResultSetFormatter.asText(QueryExecutionFactory.sparqlService(service, query).execSelect()); // csv
        }
        return null;
    }

    
    public ResultSet queryRS(String service, String query) {
        return QueryExecutionFactory.sparqlService(service, query).execSelect();
        
    }
    
    //        ArrayList<ArrayList> data = new ArrayList();
    
    //this function is called for the select querys, and redirecting to other functions
    public String queryAsHTML(String service, String query, int numEndpoints) {
        return this.asHTML(queryAsText(service, query, numEndpoints));
    }

    
    //calling the SPARQLCMLHandler class to format the output table properly

    private String asHTML(String result) {
        SPARQLXMLHandler handler = new SPARQLXMLHandler();
        try {
            // generating a XMLReader
            XMLReader xmlReader = XMLReaderFactory.createXMLReader();
            // InputSource for XML Data
            InputSource inputSource = new InputSource(new StringReader(result));
            // ContentHandler 
            xmlReader.setContentHandler(handler);
            // start parsing
            xmlReader.parse(inputSource);
        } catch (FileNotFoundException e) {
        } catch (IOException | SAXException e) {
        }
        return handler.getSb().toString();
    }

    //The SELECT Query is given and formatted to a Federated Query

    public String formatFederatedQuery(String query) {
        //if the query contains the keyword SERVICE, it already is a Federated Query no changes needed
        if (query.contains("SERVICE")) {
            return query;
        } //the query get doubled and for each service its added as a subquery
        else {
            String[] queryList = query.split("(?i)SELECT", 2);
            query = queryList[0] + "SELECT * { {SERVICE <http://frcpl.uw.edu.pl/rdf/sparql>{ SELECT "
                    + queryList[1] + "}} UNION{SERVICE <http://afe.dainst.org/rdf/sparql> { SELECT "
                    + queryList[1] + "}}}";
            return query;
        }
    }

    public void duRequest(String filePath) {
        Request req = new Request();
        //System.out.println("Fuseki: "+req.alive(Request.fuseki_local_cnt));
        //System.out.println(req.alive(Request.rgk_endpoint));
        StringBuilder sb = new StringBuilder();

        Rule.init();
        sb.append("Folgende Regeln sind hinterlegt:\n");
        Rule r;
        /*Iterator<Rule> it = Rule.RULES.iterator();
        while (it.hasNext()) {
            r = it.next();
            sb.append("\t").append(r.getId()).append(" ").append(r.getName()).append("\n");
        }
        sb.append("Auffälligkeiten pro Regel:").append("\n");
        it = Rule.RULES.iterator();
        while (it.hasNext()) {
            r = it.next();
            sb.append("\t").append(r.getId()).append(" ").append(req.queryAs(Request.fuseki_local_cnt, r.getCountQuery(), 2, 1));
            sb.append("\n");
        }*/
        // Ausgabe des Ergebnisses
        //System.out.println(sb.toString());
        System.out.println("Ausgabe in Excel ...");
        WriteSheet ws = new WriteSheet(filePath);
        String service = Request.fuseki_local_cnt;
        ws.writeOverview("Übersicht", Rule.getOverview(service));
        Rule rule = new Rule();
        Iterator<Rule> it = rule.RULES.iterator();
        int i = 1;
        while (it.hasNext()) {
            r = it.next();
            ws.write(r.getName(), req.queryRS(service, r.getQuery()), r.getAttributes());
            i++;
        }
        rule.RULES.clear();
        ws.closeWorkbook();
        

        // System.out.println(Rule.RULES.get(2).getQuery(0));
        // System.out.println(req.queryAs(Request.fuseki_local_cnt, Namespaces.getPrefix() + Rule.RULES.get(2).getQuery(0), 2, 1));
        // System.out.println("End");
    }

}
