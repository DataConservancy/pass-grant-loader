package org.dataconservancy.pass.grant.data;

import org.dataconservancy.pass.model.Grant;


import java.net.URI;
import java.util.HashSet;

public class CoeusPassInitEntityUtil extends CoeusPassEntityUtil {

    @Override
    public Grant update(Grant system, Grant stored) {
        if (this.grantNeedsUpdate(system, stored)) {
            return this.updateGrant(system, stored);
        }
        return null;
    }


    /**
     * Compare two Grant objects. Note that the Lists of Co-Pis are compared as Sets
     * @param system the version of the Grant as seen in the COEUS system pull
     * @param stored the version of the Grant as read from Pass
     * @return a boolean which asserts whether the stored grant needs to be updated
     */

    private boolean grantNeedsUpdate(Grant system, Grant stored) {
     if (system.getAwardNumber() != null ? !system.getAwardNumber().equals(stored.getAwardNumber()) : stored.getAwardNumber() != null) return true;
     if (system.getAwardStatus() != null? !system.getAwardStatus().equals(stored.getAwardStatus()) : stored.getAwardStatus() != null) return true;
     if (system.getLocalKey() != null? !system.getLocalKey().equals(stored.getLocalKey()) : stored.getLocalKey() != null) return true;
     if (system.getProjectName() != null? !system.getProjectName().equals(stored.getProjectName()) : stored.getProjectName() != null) return true;
     if (system.getPrimaryFunder() != null? !system.getPrimaryFunder().equals(stored.getPrimaryFunder()) : stored.getPrimaryFunder() != null) return true;
     if (system.getDirectFunder() != null? !system.getDirectFunder().equals(stored.getDirectFunder()) : stored.getDirectFunder() != null) return true;
     if (system.getPi() != null? !system.getPi().equals(stored.getPi()) : stored.getPi() != null) return true;
     if (system.getCoPis() != null? !new HashSet(system.getCoPis()).equals(new HashSet(stored.getCoPis())): stored.getCoPis() != null) return true;
     if (system.getAwardDate() != null? !system.getAwardDate().equals(stored.getAwardDate()) : stored.getAwardDate() != null) return true;
     if (system.getStartDate() != null? !system.getStartDate().equals(stored.getStartDate()) : stored.getStartDate() != null) return true;
     if (system.getEndDate() != null? !system.getEndDate().equals(stored.getEndDate()) : stored.getEndDate() != null) return true;
     return false;
    }

    /**
     * Update a Pass Grant object with new information from COEUS
     *
     * @param system the version of the Grant as seen in the COEUS system pull
     * @param stored the version of the Grant as read from Pass
     * @return the Grant object which represents the Pass object, with any new information from COEUS merged in
     */
    private Grant updateGrant(Grant system, Grant stored) {
        stored.setAwardNumber(system.getAwardNumber());
        stored.setAwardStatus(system.getAwardStatus());
        stored.setLocalKey(system.getLocalKey());
        stored.setProjectName(system.getProjectName());
        stored.setPrimaryFunder(system.getPrimaryFunder());
        stored.setDirectFunder(system.getDirectFunder());

        //adjust the system view of co-pis  by merging in the stored view
        for( URI uri : stored.getCoPis() ) {
            if ( !system.getCoPis().contains(uri) ) {
                system.getCoPis().add(uri);
            }
        }
        URI storedPi = stored.getPi();
        if ( !system.getPi().equals( storedPi )) {
            if ( !system.getCoPis().contains( storedPi )) {
                system.getCoPis().add ( storedPi );
            }
        }

        stored.setPi( system.getPi() );
        stored.setCoPis( system.getCoPis() );
        stored.setAwardDate(system.getAwardDate());
        stored.setStartDate(system.getStartDate());
        stored.setEndDate(system.getEndDate());
        return stored;
    }

}
