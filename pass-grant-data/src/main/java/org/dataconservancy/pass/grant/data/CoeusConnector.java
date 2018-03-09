/*
 * Copyright 2018 Johns Hopkins University
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.dataconservancy.pass.grant.data;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Map;
import java.util.StringJoiner;

/**
 * This class connects to a COEUS database via the Oracle JDBC driver
 */
public class CoeusConnector {
    private static Logger LOG = LoggerFactory.getLogger(CoeusConnector.class);
    //property names
    private static final String COEUS_URL = "coeus.url";
    private static final String COEUS_USER = "coeus.user";
    private static final String COEUS_PASS = "coeus.pass";

    private String coeusUrl;
    private String coeusUser;
    private String coeusPassword;

    public CoeusConnector(Map<String, String> connectionProperties) {
        if (connectionProperties != null) {

            if (connectionProperties.get(COEUS_URL) != null) {
                this.coeusUrl = connectionProperties.get(COEUS_URL);
            }
            if (connectionProperties.get(COEUS_USER) != null) {
                this.coeusUser = connectionProperties.get(COEUS_USER);
            }
            if (connectionProperties.get(COEUS_PASS) != null) {
                this.coeusPassword = connectionProperties.get(COEUS_PASS);
            }
        }
    }

    /**
     * This method returns a {@code ResultSet} for a query for a specific set of fields in several views in COEUS.
     *
     * @param queryString the query string to the COEUS database needed to update the information
     * @return the {@code ResultSet} from the query
     */
    public ResultSet retrieveCoeusUpdates(String queryString) throws ClassNotFoundException, SQLException {

        ResultSet rs;

        Class.forName("oracle.jdbc.driver.OracleDriver");
        Connection con = DriverManager.getConnection(coeusUrl, coeusUser, coeusPassword);
        Statement stmt = con.createStatement();
        rs = stmt.executeQuery(queryString);

        LOG.info("Retrieved result set from COEUS");

        return rs;
    }

    /**
     * Method for building the query string against the COEUS database. We draw from three views, with the PRSN view
     * (aliased to B) serving just to contain keys for joining the PROP (alias A) and DETAIL (alias C) views.
     *
     * Since dates are stored in the views as strings, we need to process these strings using the TO_DATE function
     * String literals are protected by single quotes. This explains some of the strangeness in the query string
     * construction.
     *
     * Normally, we will pull for just one date - in this case, startDate and endDate will be the same. Dates must be
     * specified as mm/dd/yyyy. Dates are expected to be valid as supplied.
     *
     * If no dates are specified, we assume that this is part of a daily run to get the updates from the previous day,
     * so start date and end date will be yesterday.
     * If only the start date is specified, we take the end date as being yesterday.
     * If only the end date is specified, we throw an exception, as there is no natural default for start date.
     *
     * @param startDate - the date we want to start the query against LAST_MODIFIED
     * @param endDate - the date we want to end the query against LAST_MODIFIED
     * @return the SQL query string
     */
    public String buildQueryString(String startDate, String endDate){

        //TODO handle dates to agree with javadoc on method

        String[] propViewFields = { "TITLE", "GRANT_NUMBER", "AWARD_DATE", "AWARD_END",
                "AWARD_NUMBER", "AWARD_START", "AWARD_STATUS", "PRIME_SPONSOR_CODE", "SPONSOR", "SPOSNOR_CODE", "DEPARTMENT",
                "DIVISION", "TITLE", "UNIT_NAME", "UNIT_NUMBER", "UPDATE_TIMESTAMP" }; //TODO add LAST_MODIFIED

        StringJoiner propViewQuery = new StringJoiner(", A.", "A.", ", ");
        for (String field : propViewFields){
            propViewQuery.add(field);
        }

        String[] personDetailViewFields = {"JHED_ID", "FIRST_NAME", "LAST_NAME", "EMAIL_ADDRESS"}; //TODO add AFFILIATION

        StringJoiner personDetailViewQuery = new StringJoiner(", C.", "C.", ", ");
        for (String field : personDetailViewFields){
            personDetailViewQuery.add(field);
        }

        String[] sponsorViewFields = { "SPONSOR_NAME" };
        StringJoiner sponsorViewQuery = new StringJoiner(", D.", "D.", "");
        for (String field : sponsorViewFields) {
            sponsorViewQuery.add(field);
        }

        StringBuilder sb = new StringBuilder();
        sb.append("SELECT ");
        sb.append(propViewQuery.toString());
        sb.append(personDetailViewQuery.toString());
        sb.append(sponsorViewQuery.toString());
        sb.append(" FROM COEUS.JHU_FACULTY_FORCE_PROP A ");
        sb.append("INNER JOIN COEUS.JHU_FACULTY_FORCE_PRSN B ON A.INST_PROPOSAL = B.INST_PROPOSAL ");
        sb.append("INNER JOIN COEUS.JHU_FACULTY_FORCE_PRSN_DETAIL C ON B.JHED_ID = C.JHED_ID ");
        sb.append("INNER JOIN COEUS.SWIFT_SPONSOR D ON A.SPOSNOR_CODE = D.SPONSOR_CODE ");
        sb.append("WHERE (TRUNC(A.UPDATE_TIMESTAMP) BETWEEN TO_DATE('");
       // if(startDate.length() == 0) {
        //    sb.append("TRUNC(s/")
       // }
        sb.append(startDate);
        sb.append("', 'mm/dd/yyyy') AND TO_DATE('");
        sb.append(endDate);
        sb.append("', 'mm/dd/yyyy')) AND (A.PROPOSAL_STATUS = 'Funded') ");
        sb.append("AND (A.AWARD_STATUS = 'Active' OR A.AWARD_STATUS = 'Terminated')");

        String queryString = sb.toString();

        LOG.debug("Query string is: " + queryString);
        return queryString;
    }

}
