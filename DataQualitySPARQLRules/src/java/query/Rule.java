/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package query;

import com.hp.hpl.jena.query.QuerySolution;
import java.util.ArrayList;
import java.util.Iterator;

/**
 *
 * @author karsten
 */
public class Rule {

    public static final int INCONSISTENT = 1;
    public static final int MISSING = 2;
    public static final int OUTLIER = 3;
    
    private static int int_PK_AI = 1;

    int id;
    String name;

    String description;
    ArrayList<Subrule> subrules = new ArrayList();
    private String query_result; // the part after select, might be equal to the attributes
    private String attributes; // attributes to be retrieved

    String query_pattern; // the where-clause part
    Query reference_count_query;
    // Inconsistent = 1, missing = 2, outlier = 3 
    int type = Rule.INCONSISTENT; // default 

    public int getType() {
        return type;
    }

    public String getTypeString() {
        switch (this.type) {
                case 1: return "Inconsistent";
                case 2: return "Missing";
                case 3: return "Outlier";
        }
        return "tpye not defined";
    }
    
    public void setType(int type) {
        this.type = type;
    }

    public static ArrayList<Rule> RULES = new ArrayList();

    public Rule() {
    }

    /**
     * Constructor for rules without subrules.
     *
     * @param name
     * @param description
     * @param query_result
     * @param query_pattern
     * @param reference_count_query
     */
    public Rule(String name, String description, String query_result, String query_pattern, Query reference_count_query) {
        this(name, description, query_result, reference_count_query);
        this.query_pattern = query_pattern;
    }

    /**
     * Constructor fur subrules.
     *
     * @param name
     * @param description
     * @param query_result
     * @param reference_count_query
     */
    public Rule(String name, String description, String query_result, Query reference_count_query) {
        this.id = Rule.int_PK_AI++;
        this.name = name;
        this.description = description;
        this.query_result = query_result;
        this.reference_count_query = reference_count_query;
    }

    // existing Rule 
    public static void init() {
        // Portrait
        Rule portrait = new Rule("Portrait",
                "Prüft auf Unstimmigkeiten von Portraitmerkmalen zwischen Münzen und ihren Typen.",
                "?reason ?coin ?c_portrait ?type ?t_portrait ?cmint",
                Query.count_coins_type);
        portrait.subrules.add(new Subrule(1, "Typ hat Porträt, Münze nicht.",
                "?type rdf:type nmo:TypeSeriesItem. ?type nmo:hasPortrait ?t_portrait.\n"
                + "  ?coin nmo:hasTypeSeriesItem ?type. ?coin nmo:hasMint ?cmint .\n"
                + "  MINUS { ?coin nmo:hasPortrait ?c_portrait. }."));

        portrait.subrules.add(new Subrule(2, "Münze hat Porträt, Typ nicht.",
                "?coin nmo:hasTypeSeriesItem ?type. ?coin nmo:hasPortrait ?c_portrait.  ?coin nmo:hasMint ?cmint . "
                + "FILTER NOT EXISTS {\n"
                + "      ?type skos:broader ?x}\n"
                + "  MINUS {?type nmo:hasPortrait ?t_portrait.}."));

        portrait.subrules.add(new Subrule(3, "Münzen und Typ haben unterschiedliche Porträts.",
                "?coin nmo:hasTypeSeriesItem ?type. ?coin nmo:hasPortrait ?c_portrait.\n"
                + "   ?type nmo:hasPortrait ?t_portrait.  ?coin nmo:hasMint ?cmint .\n"
                + "   FILTER NOT EXISTS {?type nmo:hasPortrait ?c_portrait.}"));

        // Start - End Date
        Rule start_end = new Rule("Start - End Date - missing",
                "Prüft welche Start und End-Dates bei Münzen und Typen fehlen.",
                "DISTINCT ?reason ?coin ?c_startdate ?c_enddate ?type ?t_startdate ?t_enddate ?cmint",
                Query.count_coins_type);
        start_end.setType(Rule.MISSING);
        start_end.subrules.add(new Subrule(1, "Münze hat kein Startdate, Type schon",
                "?coin nmo:hasTypeSeriesItem ?type.  ?coin nmo:hasMint ?cmint .\n"
                + "         FILTER NOT EXISTS {?coin nmo:hasStartDate ?c_startdate}.\n"
                + "         ?type nmo:hasStartDate ?t_startdate."));
        start_end.subrules.add(new Subrule(2, "Münze hat kein Enddate, Type schon",
                "?coin nmo:hasTypeSeriesItem ?type.  ?coin nmo:hasMint ?cmint . "
                + "FILTER NOT EXISTS {?coin nmo:hasEndDate ?c_enddate}.\n"
                + "         ?type nmo:hasEnddateDate ?t_enddate."));
        start_end.subrules.add(new Subrule(3, "Typ hat kein Startdate, Münze schon",
                "?coin nmo:hasTypeSeriesItem ?type.  ?coin nmo:hasMint ?cmint . "
                + "FILTER NOT EXISTS {\n"
                + "         ?type skos:broader ?subtype}. "
                + "FILTER NOT EXISTS {?type nmo:hasStartDate ?t_startdate}.\n"
                + "         ?coin nmo:hasStartDate ?c_startdate."));
        start_end.subrules.add(new Subrule(4, "Typ hat kein Enddate, Münze schon",
                "?coin nmo:hasTypeSeriesItem ?type.  ?coin nmo:hasMint ?cmint . "
                + "FILTER NOT EXISTS {\n"
                + "         ?type skos:broader ?subtype}. "
                + "FILTER NOT EXISTS {?type nmo:hasEndDate ?t_enddate}.\n"
                + "         ?coin nmo:hasEndDate ?c_enddate."));
        start_end.subrules.add(new Subrule(5, "Typ und Münze haben kein Startdate",
                "?coin nmo:hasTypeSeriesItem ?type.  ?coin nmo:hasMint ?cmint .\n"
                + "         ?coin nmo:hasEndDate ?c_enddate.\n"
                + "         ?type nmo:hasEnddateDate ?t_enddate.\n"
                + "         FILTER NOT EXISTS {?coin nmo:hasStartDate ?c_startdate}.\n"
                + "         FILTER NOT EXISTS {?type nmo:hasStartDate ?t_startdate}"));
        start_end.subrules.add(new Subrule(6, "Typ und Münze haben kein Enddate",
                "?coin nmo:hasTypeSeriesItem ?type.  ?coin nmo:hasMint ?cmint .\n"
                + "         ?coin nmo:hasStartDate ?c_startdate.\n"
                + "         ?type nmo:hasStartDate ?t_startdate.\n"
                + "         FILTER NOT EXISTS {?coin nmo:hasEndDate ?c_enddate}.\n"
                + "         FILTER NOT EXISTS {?type nmo:hasEndDate ?t_enddate}"));
        start_end.subrules.add(new Subrule(7, "Weder Typ noch Münze haben Startdate oder Enddate",
                "?coin nmo:hasTypeSeriesItem ?type.  ?coin nmo:hasMint ?cmint .\n"
                + "         FILTER NOT EXISTS {?coin nmo:hasEndDate ?c_enddate}.\n"
                + "         FILTER NOT EXISTS {?coin nmo:hasStartDate ?c_startdate}.\n"
                + "         FILTER NOT EXISTS {?type nmo:hasEndDate ?t_enddate}\n"
                + "         FILTER NOT EXISTS {?type nmo:hasStartDate ?t_startdate}"));

        Rule start_end_fitting = new Rule("Start Date and End Date fitting",
                "Prüft, ob die Münz-Datierung außerhalb der Type-Datierung liegt.",
                "DISTINCT ?reason ?coin ?c_startdate ?c_enddate ?type ?t_startdate ?t_enddate ?cmint",
                Query.count_coins_type);
        start_end_fitting.subrules.add(new Subrule(1, "Startdate liegt außerhalb der Typendatierung",
                "?coin nmo:hasStartDate ?c_startdate. ?coin nmo:hasTypeSeriesItem ?type.  "
                + "?coin nmo:hasMint ?cmint . "
                + "FILTER NOT EXISTS {\n"
                + "      ?type skos:broader ?subtype}. ?coin nmo:hasEndDate ?c_enddate.\n"
                + "  ?type nmo:hasStartDate ?t_startdate. ?type nmo:hasEndDate ?t_enddate.\n"
                + "  FILTER (?c_startdate < ?t_startdate || ?c_startdate > ?t_enddate ) "));
        start_end_fitting.subrules.add(new Subrule(2, "Enddate liegt außerhalb der Typendatierung",
                " ?coin nmo:hasStartDate ?c_startdate. ?coin nmo:hasTypeSeriesItem ?type.  "
                + "?coin nmo:hasMint ?cmint . "
                + "FILTER NOT EXISTS {\n"
                + "      ?type skos:broader ?subtype}. "
                + "?coin nmo:hasEndDate ?c_enddate.\n"
                + "  ?type nmo:hasStartDate ?t_startdate. ?type nmo:hasEndDate ?t_enddate.\n"
                + "  FILTER (?c_enddate > ?t_enddate || ?c_enddate < ?t_startdate) "));

        // rule without subrule
        Rule start_end_wrong = new Rule("Start Date after End Date",
                "Prüft, ob das Start Date logisch vor dem End Date liegt",
                "?reason ?coin ?c_startdate ?c_enddate ?cmint",
                "  ?coin nmo:hasStartDate ?c_startdate.\n"
                + "  ?coin nmo:hasEndDate ?c_enddate.  ?coin nmo:hasMint ?cmint .\n"
                + "  FILTER (?c_startdate > ?c_enddate )\n"
                + "    VALUES ?reason { \"Startdate liegt nach Enddate!\"}. ",
                Query.count_coinsandtypes);

        // Denomination      
        Rule denomination = new Rule("Denomination",
                "Prüft, ob die Münzen das gleiche Nominal haben wie der Typ (bei Zuordung zu einem Subtype wird der Wert vom entsprechenden Typ verglichen).",
                "?reason ?coin ?type ?st ?coin_value ?type_value ?cmint",
                Query.count_coins_type);
        denomination.subrules.add(new Subrule(1, "Denomination der Münze passt nicht zum Typ",
                "?coin nmo:hasTypeSeriesItem ?type .\n"
                + "   ?coin nmo:hasDenomination ?coin_value ."
                + " ?coin nmo:hasMint ?cmint . \n"
                + "   ?type nmo:hasDenomination ?type_value .\n"
                + "    FILTER (?coin_value != ?type_value)"));

        denomination.subrules.add(new Subrule(2, "Denomination der Münze passt nicht zum Typ (subtype)",
                "?coin nmo:hasTypeSeriesItem ?st .\n"
                + "    ?st skos:broader ?type .  ?coin nmo:hasMint ?cmint .\n"
                + "   ?coin nmo:hasDenomination ?coin_value .\n"
                + "   ?type nmo:hasDenomination ?type_value .\n"
                + "    FILTER (?coin_value != ?type_value)"));

        // Mints
        Rule mint = new Rule("Mint",
                "Prüft, ob die Münzen die gleiche Mint hat wie der Typ (bei Zuordung zu einem Subtype wird der Wert vom entsprechenden Typ verglichen).",
                "?reason ?coin ?type ?st ?coin_value ?type_value",
                Query.count_coins_type);
        mint.subrules.add(new Subrule(1, "Münzstätte der Münze passt nicht zum Typ",
                "?coin nmo:hasTypeSeriesItem ?type .\n"
                + "   ?coin nmo:hasMint ?coin_value .\n"
                + "   ?type nmo:hasMint ?type_value .\n"
                + "    FILTER (?coin_value != ?type_value)"));

        mint.subrules.add(new Subrule(2, "Münzstätte der Münze passt nicht zum Typ (subtype)",
                "?coin nmo:hasTypeSeriesItem ?st .\n"
                + "    ?st skos:broader ?type .\n"
                + "   ?coin nmo:hasMint ?coin_value .\n"
                + "   ?type nmo:hasMint ?type_value .\n"
                + "    FILTER (?coin_value != ?type_value)"));

        // Material
        Rule material = new Rule("Material",
                "Prüft, ob die Münzen das gleiche Material hat wie der Typ (bei Zuordung zu einem Subtype wird der Wert vom entsprechenden Typ verglichen).",
                "?reason ?coin ?type ?st ?coin_value ?type_value ?cmint",
                Query.count_coins_type);
        material.subrules.add(new Subrule(1, "Material der Münze passt nicht zum Typ",
                "?coin nmo:hasTypeSeriesItem ?type .\n"
                + "   ?coin nmo:hasMaterial ?coin_value .  ?coin nmo:hasMint ?cmint .\n"
                + "   ?type nmo:hasMaterial ?type_value .\n"
                + "    FILTER (?coin_value != ?type_value)"));

        material.subrules.add(new Subrule(2, "Material der Münze passt nicht zum Typ (subtype)",
                "?coin nmo:hasTypeSeriesItem ?st .\n"
                + "    ?st skos:broader ?type .\n"
                + "   ?coin nmo:hasMaterial ?coin_value .  ?coin nmo:hasMint ?cmint .\n"
                + "   ?type nmo:hasMaterial ?type_value .\n"
                + "    FILTER (?coin_value != ?type_value)"));

        // Diameter
        Rule diameter = new Rule("Diameter",
                "Prüft MinDiameter und MaxDiameter Angaben.",
                "?reason ?coin1 ?diamin ?diamax ?cmint",
                Query.count_coins);
        diameter.subrules.add(new Subrule(1, "MinDiameter größer als Maxdiameter",
                "    ?coin1 nmo:hasMaxDiameter ?diamax .\n"
                + "  ?coin1 nmo:hasMinDiameter ?diamin . ?coin1 nmo:hasMint ?cmint .\n"
                + "  FILTER (?diamin > ?diamax) "));
        diameter.subrules.add(new Subrule(2, "Starke Abweichung zwischen Min-Maxdiameter",
                "    ?coin1 nmo:hasMaxDiameter ?diamax .\n"
                + "  ?coin1 nmo:hasMinDiameter ?diamin . ?coin1 nmo:hasMint ?cmint .\n"
                + "  FILTER ((?diamax - ?diamin) > 10) .   FILTER (?diamin > 0) . "));
        diameter.subrules.add(new Subrule(3, "Außergewöhnlich große Münze.",
                "   ?coin1 nmo:hasMaxDiameter ?diamax . ?coin1 nmo:hasMint ?cmint .\n"
                + "  FILTER (?diamax > 100) . "));
        diameter.subrules.add(new Subrule(4, "MinDiameter ohne MaxDiameter.",
                "   ?coin1 nmo:hasMinDiameter ?diamin . ?coin1 nmo:hasMint ?cmint .\n"
                + "  FILTER NOT EXISTS {?coin1 nmo:hasMaxDiameter ?diamax} "));

        // Diameter Weight
        Rule diameter_weight = new Rule("Diameter Weight",
                "Prüft auf Extremfälle bei Diameter und Weight relationen.",
                "?reason ?coin1 ?diamax ?weight (?diamax/?weight as ?relation) ?cmint",
                Query.count_coins);
        diameter_weight.setType(Rule.OUTLIER);
        diameter_weight.attributes = "?reason ?coin1 ?diamax ?weight ?relation";
        diameter_weight.subrules.add(new Subrule(1, "Starke Abweichung - zu leicht - Grenzrelation: 20",
                "   ?coin1 nmo:hasMaxDiameter ?diamax . ?coin1 nmo:hasMint ?cmint .\n"
                + "  ?coin1 nmo:hasWeight ?weight .\n"
                + "  FILTER (?diamax/?weight > 20) ."));
        diameter_weight.subrules.add(new Subrule(2, "Starke Abweichung - zu schwer - Grenzrelation: 0.8",
                "   ?coin1 nmo:hasMaxDiameter ?diamax . ?coin1 nmo:hasMint ?cmint .\n"
                + "  ?coin1 nmo:hasWeight ?weight .\n"
                + "  FILTER (?diamax > 0) .\n"
                + "  FILTER (?weight > 0) .\n"
                + "  FILTER (?diamax/?weight < 0.8) ."));

        // Diameter Weight
        Rule existing_weight_diameterMinMax = new Rule("Diameter Weight existing",
                "Prüft auf Vollständigkeit der Daten bezogen auf Diameter (Min, Max) und Weight.",
                "DISTINCT ?coin ?weight ?dia_max ?dia_min ?reason ?cmint",
                Query.count_coins);
        existing_weight_diameterMinMax.setType(Rule.MISSING);
        existing_weight_diameterMinMax.subrules.add(new Subrule(1, "Gewicht nicht eingetragen",
                "   ?coin nmo:hasMint ?cmint .\n"
                + " ?coin nmo:hasMaxDiameter ?dia_max.\n"
                + "  ?coin nmo:hasMinDiameter ?dia_min.\n"
                + "    FILTER NOT EXISTS {?coin nmo:hasWeight ?weight.}. "));

        existing_weight_diameterMinMax.subrules.add(new Subrule(2, "Maximaler Durchmesser nicht eingetragen",
                "   ?coin nmo:hasMint ?cmint .\n"
                + " ?coin nmo:hasWeight ?weight.\n"
                + "  ?coin nmo:hasMinDiameter ?dia_min.\n"
                + "    FILTER NOT EXISTS {?coin nmo:hasMaxDiameter ?dia_max.}. "));

        existing_weight_diameterMinMax.subrules.add(new Subrule(3, "Minimaler Durchmesser nicht eingetragen",
                "   ?coin nmo:hasMint ?cmint .\n"
                + " ?coin nmo:hasWeight ?weight.\n"
                + "  ?coin nmo:hasMaxDiameter ?dia_max.\n"
                + "    FILTER NOT EXISTS {?coin nmo:hasMinDiameter ?dia_min.}. "));

        existing_weight_diameterMinMax.subrules.add(new Subrule(4, "Maximaler und minimaler Durchmesser nicht eingetragen",
                "   ?coin nmo:hasMint ?cmint .\n"
                + " ?coin nmo:hasWeight ?weight.\n"
                + "    FILTER NOT EXISTS {?coin nmo:hasMaxDiameter ?dia_max.}.\n"
                + "    FILTER NOT EXISTS {?coin nmo:hasMinDiameter ?dia_min.}."));

        existing_weight_diameterMinMax.subrules.add(new Subrule(5, "Gewicht und minimaler Durchmesser nicht eingetragen",
                "   ?coin nmo:hasMint ?cmint .\n"
                + " ?coin nmo:hasMaxDiameter ?dia_max.\n"
                + "    FILTER NOT EXISTS {?coin nmo:hasWeight ?weight.}.\n"
                + "    FILTER NOT EXISTS {?coin nmo:hasMinDiameter ?dia_min.}."));

        existing_weight_diameterMinMax.subrules.add(new Subrule(6, "Gewicht und maximaler Durchmesser nicht eingetragen",
                "   ?coin nmo:hasMint ?cmint .\n"
                + " ?coin nmo:hasMinDiameter ?dia_min.\n"
                + "    FILTER NOT EXISTS {?coin nmo:hasWeight ?weight.}.\n"
                + "    FILTER NOT EXISTS {?coin nmo:hasMaxDiameter ?dia_max.}."));

        existing_weight_diameterMinMax.subrules.add(new Subrule(6, "Gewicht, maximaler und minimaler Durchmesser nicht eingetragen",
                "   ?coin nmo:hasMint ?cmint .\n"
                + " ?coin nmo:hasObjectType nm:coin.\n"
                + "    FILTER NOT EXISTS {?coin nmo:hasWeight ?weight.}.\n"
                + "    FILTER NOT EXISTS {?coin nmo:hasMaxDiameter ?dia_max.}.\n"
                + "    FILTER NOT EXISTS {?coin nmo:hasMinDiameter ?dia_min.}."));

        // 0-Entries
        Rule zero_entries = new Rule("Tests Diameter Weight for 0 or negative values",
                "Prüft ob 0 oder negative Werte bezogen auf Diameter (Min, Max) und Weight vorliegen.",
                "DISTINCT ?coin ?weight ?dia_max ?dia_min ?reason ?cmint",
                Query.count_coins);
        zero_entries.setType(Rule.MISSING);
        zero_entries.subrules.add(new Subrule(1, "Angabe 0 oder negativ.",
                "   ?coin nmo:hasMint ?cmint .\n"
                + " ?coin nmo:hasWeight ?weight.\n"
                + "  ?coin nmo:hasMaxDiameter ?dia_max.\n"
                + "  ?coin nmo:hasMinDiameter ?dia_min.\n"
                + "    FILTER ((?weight <= 0) || (?dia_max <= 0) || (?dia_min <= 0)) "));

        zero_entries.subrules.add(new Subrule(2, "Angabe 0 oder negativ.",
                "   ?coin nmo:hasMint ?cmint .\n"
                + " ?coin nmo:hasMaxDiameter ?dia_max.\n"
                + "  ?coin nmo:hasMinDiameter ?dia_min.\n"
                + "    FILTER NOT EXISTS {?coin nmo:hasWeight ?weight.}.\n"
                + "    FILTER ((?dia_max <= 0) || (?dia_min <= 0))  "));

        zero_entries.subrules.add(new Subrule(3, "Angabe 0 oder negativ.",
                "   ?coin nmo:hasMint ?cmint .\n"
                + " ?coin nmo:hasWeight ?weight.\n"
                + "  ?coin nmo:hasMinDiameter ?dia_min.\n"
                + "    FILTER NOT EXISTS {?coin nmo:hasMaxDiameter ?dia_max.}.\n"
                + "    FILTER ((?weight = 0) || (?dia_min = 0))  "));

        zero_entries.subrules.add(new Subrule(4, "Angabe 0 oder negativ.",
                "   ?coin nmo:hasMint ?cmint .\n"
                + " ?coin nmo:hasWeight ?weight.\n"
                + "  ?coin nmo:hasMaxDiameter ?dia_max.\n"
                + "    FILTER NOT EXISTS {?coin nmo:hasMinDiameter ?dia_min.}.\n"
                + "    FILTER ((?weight = 0) || (?dia_max = 0))   "));

        zero_entries.subrules.add(new Subrule(5, "Angabe 0 oder negativ.",
                "   ?coin nmo:hasMint ?cmint .\n"
                + "  ?coin nmo:hasWeight ?weight.\n"
                + "    FILTER NOT EXISTS {?coin nmo:hasMaxDiameter ?dia_max.}.\n"
                + "    FILTER NOT EXISTS {?coin nmo:hasMinDiameter ?dia_min.}.\n"
                + "    FILTER (?weight = 0)  "));

        zero_entries.subrules.add(new Subrule(6, "Angabe 0 oder negativ.",
                "   ?coin nmo:hasMint ?cmint .\n"
                + "  ?coin nmo:hasMaxDiameter ?dia_max.\n"
                + "    FILTER NOT EXISTS {?coin nmo:hasWeight ?weight.}.\n"
                + "    FILTER NOT EXISTS {?coin nmo:hasMinDiameter ?dia_min.}.\n"
                + "    FILTER (?dia_max = 0) "));

        zero_entries.subrules.add(new Subrule(7, "Angabe 0 oder negativ.",
                "   ?coin nmo:hasMint ?cmint .\n"
                + "  ?coin nmo:hasMinDiameter ?dia_min.\n"
                + "    FILTER NOT EXISTS {?coin nmo:hasWeight ?weight.}.\n"
                + "    FILTER NOT EXISTS {?coin nmo:hasMaxDiameter ?dia_max.}.\n"
                + "    FILTER (?dia_min = 0) "));

        // Inconsistent
        Rule.RULES.add(portrait);
        Rule.RULES.add(start_end_fitting);
        Rule.RULES.add(start_end_wrong);
        Rule.RULES.add(denomination);
        Rule.RULES.add(mint);
        Rule.RULES.add(material);
        Rule.RULES.add(diameter);
       
        // missing
        Rule.RULES.add(existing_weight_diameterMinMax);
        Rule.RULES.add(start_end);
        Rule.RULES.add(zero_entries);
        
        // Outlier
        Rule.RULES.add(diameter_weight);
        
    }

    public String getCountQuery() {
        return Namespaces.getPrefix() + this.getQuery(1);
    }

    public int getCount(String service) {
        Request r = new Request();
        QuerySolution qs = r.queryRS(service, this.getCountQuery()).nextSolution();
        return Integer.parseInt(qs.get("?count").asLiteral().getString());
    }

    private int getCount(String query, String service) {
        Request r = new Request();
        QuerySolution qs = r.queryRS(service, query).nextSolution();
        return Integer.parseInt(qs.get("?count").asLiteral().getString());
    }

    public String getQuery() {
        return Namespaces.getPrefix() + this.getQuery(0);
    }

    public String[] getAttributes() {
        if (this.attributes == null) {
            if (this.query_result.toUpperCase().contains("DISTINCT ")) {
                return this.query_result.replaceAll("DISTINCT ", "").split(" ");
            }
            return this.query_result.split(" ");
        }
        return this.attributes.split(" ");

    }

    /**
     *
     * @param i 0: normal Query; 1: count(*)
     * @return
     */
    private String getQuery(int i) {
        //
        StringBuilder sb = new StringBuilder();
        sb.append("select ");
        switch (i) {
            case 0:
                sb.append(this.query_result);
                break;
            case 1:
                sb.append(" (count(*) as ?count) ");
                break;
        }
        sb.append(" WHERE { ");
        if (this.subrules != null && !this.subrules.isEmpty()) {
            Iterator<Subrule> it = this.subrules.iterator();
            while (it.hasNext()) {
                sb.append(it.next().getSubQuery());
                sb.append(" UNION "); // TODO genauer
            }
            // remove last UNION
            sb.delete(sb.length() - 7, sb.length());

        } else {
            sb.append(this.query_pattern);

        }
        sb.append(" }");
        //System.out.println("sb: "+sb.toString());
        return sb.toString();
    }

    public static Object[][] getOverview(String service) {
        Object[][] data = new Object[(Rule.RULES.size() + 1)][9];
        data[0][0] = "Regel";
        data[0][1] = "Name";
        data[0][2] = "Beschreibung";
        data[0][3] = "Anzahl Fälle";
        data[0][4] = "Reference Query";
        data[0][5] = "Reference Size";
        data[0][6] = "Fallquotient";
        data[0][7] = "Query Type";
        data[0][8] = "Anfrage";

        int rulenr = 1;

        for (Rule r : Rule.RULES) {
            int count_rule = r.getCount(service);
            int count_ref = r.reference_count_query.getCount(service);
            //System.out.println("count_rule: "+count_rule+" count_ref: "+count_ref);
            //System.out.println("Res.: "+(double)count_rule/(double)count_ref);
            data[rulenr][0] = rulenr;
            data[rulenr][1] = r.name;
            data[rulenr][2] = r.description;
            data[rulenr][3] = count_rule;
            data[rulenr][4] = r.reference_count_query.getName();
            data[rulenr][5] = count_ref;
            data[rulenr][6] = (double) count_rule / (double) count_ref;
            data[rulenr][7] = r.getTypeString();
            data[rulenr][8] = r.getQuery();
            rulenr++;
        }
        return data;
    }

    // generated Getter and Setter
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public ArrayList<Subrule> getSubrules() {
        return subrules;
    }

    public void setSubrules(ArrayList<Subrule> subrules) {
        this.subrules = subrules;
    }

}
