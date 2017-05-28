package es.csic.iiia.planes.liam;

import es.csic.iiia.planes.Block;

/**
 * Block distance heuristic for crowd check is set to use the Manhattan Distance.
 *
 * Created by Guillermo on 8/31/2016.
 * @author Guillermo <gbau@mit.edu> */
public class ManhattanBlockDistance implements DistanceHeuristic{
    @Override
    public boolean checkNearby(Block searchBlock, Block nearbyBlock, double crowdDistance) {
        double xDistance = Math.abs(nearbyBlock.getxLoc() - searchBlock.getxLoc());
        double yDistance = Math.abs(nearbyBlock.getyLoc() - searchBlock.getyLoc());
        boolean agentNearby = nearbyBlock.getState() == Block.blockState.ASSIGNED;
        if (Math.abs(xDistance)+Math.abs(yDistance) < crowdDistance && agentNearby) {
            return false;
        }
        else {
            return true;
        }
    }
}
