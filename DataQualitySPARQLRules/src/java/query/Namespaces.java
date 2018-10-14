/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package query;

/**
 *
 * @author karsten
 */
public class Namespaces {
    // Prefix
    static final String nm = "PREFIX nm: <http://nomisma.org/id/> ";
    static final String nmo = "PREFIX nmo: <http://nomisma.org/ontology#> ";
    static final String voi = "PREFIX void: <http://rdfs.org/ns/void#> ";
    static final String rdf = "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> ";
    static final String bio = "PREFIX bio: <http://purl.org/vocab/bio/0.1/> ";
    static final String crm = "PREFIX crm: <http://www.cidoc-crm.org/cidoc-crm/> ";
    static final String dcmitype = "PREFIX dcmitype: <http://purl.org/dc/dcmitype/> ";
    static final String dcterms = "PREFIX dcterms: <http://purl.org/dc/terms/> ";
    static final String foaf = "PREFIX foaf: <http://xmlns.com/foaf/0.1/> ";
    static final String geo = "PREFIX geo: <http://www.w3.org/2003/01/geo/wgs84_pos#> ";
    static final String org = "PREFIX org: <http://www.w3.org/ns/org#> ";
    static final String osgeo = "PREFIX osgeo: <http://data.ordnancesurvey.co.uk/ontology/geometry/> ";
    static final String rdac = "PREFIX rdac: <http://www.rdaregistry.info/Elements/c/> ";
    static final String skos = "PREFIX skos: <http://www.w3.org/2004/02/skos/core#> ";
    static final String xsd = "PREFIX xsd: <http://www.w3.org/2001/XMLSchema#> ";
    static final String spatial = "PREFIX spatial: <http://jena.apache.org/spatial#> ";

    static String prefix = nm + nmo + voi + rdf + bio + crm + dcmitype + dcterms + foaf + geo + org + osgeo
            + rdac + skos + xsd + spatial;

    public static String getPrefix() {
        return prefix;
    }
       
}
