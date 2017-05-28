package es.csic.iiia.planes.liam;

import es.csic.iiia.planes.Block;

/**
 * Definition of a block distance heuristic, used by LIAM planes to compute whether
 * the next possible search block is too close to a nearby neighbor agent.
 *
 * Created by Guillermo on 8/31/2016.
 * @author Guillermo <gbau@mit.edu>
 */
public interface DistanceHeuristic {
    public boolean checkNearby(Block searchBlock, Block nearbyBlock, double crowdDistance);
}
