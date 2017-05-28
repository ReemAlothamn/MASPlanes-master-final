package es.csic.iiia.planes.liam;

import es.csic.iiia.planes.SARPlane;
import es.csic.iiia.planes.behaviors.AbstractBehavior;
import es.csic.iiia.planes.cli.Configuration;

/**
 * Implements the dynamic task allocation behavior for AuctionPlanes.
 *
 * @author Guillermo Bautista <gbau@mit.edu>
 *
 * Created by Guillermo on 9/1/2016.
 */
public class LIAMBehavior extends AbstractBehavior{

    /**
     * Builds an auctioning behavior for the given agent.
     *
     * @param agent that will display this behavior.
     */
    public LIAMBehavior(SARPlane agent) {
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

        getAgent().setEagleCrowdDistance(config.getEagleCrowdDistance());
        getAgent().setEagleJumpDistance(config.getEagleJumpDistance());
        getAgent().setScoutJumpDistance(config.getScoutJumpDistance());
        getAgent().setScoutSpeedPercentage(config.getScoutSpeed());
        getAgent().setEagleSpeedPercentage(config.getEagleSpeed());
    }

    @Override
    public SARPlane getAgent() {
        return (SARPlane)super.getAgent();
    }

}
