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
public class Subrule extends Rule {

       /**
     * Constructor fur subrules.
     *
     * @param id
     * @param query
     * @param description
     */
    public Subrule(int id, String description, String query) {
        super.id = id;
        super.name = name;
        super.description = description;
        super.query_pattern = query;
    }
    
    String getSubQuery() {
        StringBuilder sb = new StringBuilder();
        sb.append("{").append(this.query_pattern).append(" VALUES ?reason { \"").append(this.description).append("\"}.}");
        return sb.toString();
    }

}
