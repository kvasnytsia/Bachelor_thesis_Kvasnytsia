/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package query;

import com.hp.hpl.jena.query.QuerySolution;

/**
 *
 * @author karsten
 */
public class Query {
    
 

    public static final Query count_coins_type = new Query(Namespaces.getPrefix()+" SELECT (count(*) as ?count)  \n" +
"WHERE {\n" +
"  ?coin nmo:hasTypeSeriesItem ?type .\n" +
"}", "coins_type");

       public static final Query count_coins = new Query(Namespaces.getPrefix()+" SELECT (count(*) as ?count)  \n" +
"WHERE {\n" +
"  ?coin nmo:hasObjectType nm:coin .\n" +
"}", "coins");
       
       public static final Query count_coinsandtypes = new Query(Namespaces.getPrefix()+" SELECT (count(*) as ?count)  \n" +
"WHERE {\n" +
" { ?coin nmo:hasObjectType nm:coin } UNION { ?coin rdf:type nmo:TypeSeriesItem . }\n" +
"}", "coins AND types");

    
    private final String name;
    private final String query;
 
    
    public Query(String query, String name) {
        this.name = name;
        this.query = query;
    }
    
    
    public String getName() {
        return name;
    }

    public int getCount(String service) {
        Request r = new Request();
        QuerySolution qs = r.queryRS(service, this.query).nextSolution();
        return Integer.parseInt(qs.get("?count").asLiteral().getString());
    }
    
}
