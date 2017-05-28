/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package es.csic.iiia.planes.BMUTA;

import es.csic.iiia.planes.BMUTAPlane;
import es.csic.iiia.planes.behaviors.AbstractBehavior;
import es.csic.iiia.planes.cli.Configuration;
/**
 *
 * @author noura 
 */

public class BMUTABehavior extends AbstractBehavior {



    /**
     * Builds an auctioning behavior for the given agent.
     *
     * @param agent that will display this behavior.
     */
    public BMUTABehavior(BMUTAPlane agent) {
        super(agent);
    }

    @Override
    public Class[] getDependencies() {
        return new Class[]{};
    }

    @Override
    public void initialize() {
        super.initialize();
        final Configuration config = getAgent().getWorld().getFactory().getConfiguration();

      /**  getAgent().setEagleCrowdDistance(config.getEagleCrowdDistance());
        getAgent().setEagleJumpDistance(config.getEagleJumpDistance());
        getAgent().setScoutJumpDistance(config.getScoutJumpDistance());
        getAgent().setScoutSpeedPercentage(config.getScoutSpeed());
        getAgent().setEagleSpeedPercentage(config.getEagleSpeed());
        * 
        */
    }

   
    @Override
    public BMUTAPlane getAgent() {
        return (BMUTAPlane)super.getAgent();
    }


    
}
