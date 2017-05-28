package es.csic.iiia.planes;

import es.csic.iiia.planes.Plane.Type;
import es.csic.iiia.planes.liam.LIAMBehavior;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.logging.Level;

/**
 * Created on 12/11/2015.
 * Implementation of a Search and Rescue (SAR) Plane, that implements very basic lower-level
 * behavior.
 *
 * @author Guillermo Bautista <gbau at mit.edu>
 */
public class SARPlane extends AbstractPlane {

    /**
     * Current plane state
     */
    private State state = State.NORMAL;

    /**
     * Current plane type
     * By default, all planes start as SCOUTS.
     */
    private Type type = Type.SCOUT;

    /**
     * Percentage power level at which plane switches to EAGLE type.
     */
    private long eagleEnergy;

    /**
     * Percentage power level at which plane switches to STANDBY type.
     */
    private long standbyEnergy;

    /**
     * Number of blocks that a Plane tries to stay away from other
     * active UAV's while in Eagle or Scout mode.
     */
    private int eagleCrowdDistance;

    /**
     * Preferred range that an Eagle-type plane will try to stay within
     * for the next available block to search.
     */
    private double eagleJumpDistance;

    /**
     * Preferred range that a Scout-type plane will try to stay within
     * for the next available block to search.
     */
    private double scoutJumpDistance;

    /**
     * Penalty incurred on battery power for searching a block.
     */
    private long searchPowerPenalty;

    /**
     * Penalty incurred on waiting time for searching a block.
     */
    private long searchTimePenalty;

    /**
     * Penalty incurred on battery power for rescuing a survivor.
     */
    private double rescuePowerPenalty;

    /**
     * Penalty incurred on waiting time for rescuing a survivor.
     */
    private double rescueTimePenalty;

    /**
     * Agent maximum speed in meters per tenth of second.
     * This is the speed at which Standby planes travel.
     */
    private double maxSpeed = -1;

    /**
     * Eagle-type speed as a percentage of maximum speed.
     */
    private double eagleSpeedPercentage;

    /**
     * Scout-type speed as a percentage of maximum speed.
     */
    private double scoutSpeedPercentage;

    /**
     * Tells the system whether the agent's initial destination has been initialized.
     */
    private boolean initialized = false;

    /**
     * Next block to be completed by the plane
     */
    private Block nextBlock = null;

    /**
     * Next region to be completed by the plane
     * Only used for Scouts
     */
    private Region nextRegion = null;

    /**
     * Time in tenthes of a second before plane switches
     * to Rescue Type.
     */
    private long rescueTime;

    /**
     * Default constructor
     *
     * @param location initial location of the plane
     */
    public SARPlane(Location location) {
        super(location);
        addBehavior(new LIAMBehavior(this));
    }

    @Override
    public void initialize() {
        super.initialize();
        searchPowerPenalty = getWorld().getConfig().getSearchPowerPenalty();
        searchTimePenalty = getWorld().getConfig().getSearchTimePenalty();
        rescuePowerPenalty = getWorld().getConfig().getRescuePowerPenalty();
        rescueTimePenalty = getWorld().getConfig().getRescueTimePenalty();
        rescueTime = getWorld().getConfig().getRescueTime();

        if(initialized) {
            if (type == Type.SCOUT) {
                setNextRegion();
                setNextBlock(nextRegion);
            }
            else {
                setNextBlockBasic();
            }
            setDestination(nextBlock.getCenter());
        }
        else{
            initialized = true;
        }
    }


    /**
     * Get the type of this plane.
     *
     * @see Type
     * @return type of this plane.
     */
    public Type getType() { return type; }

    /**
     * Set the type of this plane.
     *
     * @see Type
     */
    public void setType(Type t) {
        this.type = t;
        if (t == Type.SCOUT) {
            setSpeed(maxSpeed*scoutSpeedPercentage);
        } else if (t == Type.EAGLE) {
            setSpeed(maxSpeed*eagleSpeedPercentage);
        } else {
            setSpeed(maxSpeed);
        }
    }

    private int getEagleCrowdDistance() { return eagleCrowdDistance; }

    public void setEagleCrowdDistance(int eagleCrowdDistance) { this.eagleCrowdDistance = eagleCrowdDistance; }

    public void setEagleJumpDistance(double eagleJumpDistance) { this.eagleJumpDistance = eagleJumpDistance; }

    public void setScoutJumpDistance(double scoutJumpDistance) {
        this.scoutJumpDistance = scoutJumpDistance;
    }

    private void setEagleEnergy (long eagleEnergy) { this.eagleEnergy = eagleEnergy; }

    private void setStandbyEnergy(long standbyEnergy) { this.standbyEnergy = standbyEnergy; }

    public void setEagleSpeedPercentage(double eagleSpeedPercentage) {
        this.eagleSpeedPercentage = eagleSpeedPercentage;
    }

    public void setScoutSpeedPercentage(double scoutSpeedPercentage) {
        this.scoutSpeedPercentage = scoutSpeedPercentage;
    }

    @Override
    public void setBattery(Battery battery) {
        super.setBattery(battery);
        /**
         * @author Ebtesam
         * battery.getCapacity() is always zero
         * It is Hard coded (9000), will fix it later
         */
        //TODO: Getting configuration from factory without hard coded
        setEagleEnergy((long)(getWorld().getConfig().getEaglePower() * 9000));
        setStandbyEnergy((long)(getWorld().getConfig().getStandbyPower() * 9000));
    }

    @Override
    public List<Location> getPlannedLocations() {
        List<Location> plannedLocations = new ArrayList<Location>();

        plannedLocations.add(getCurrentDestination().destination);
        return plannedLocations;
    }


    @Override
    public void step() {
        //TODO: Make this time a set percentage of time in the configuration
        //Switch to rescuer at 75% of duration
        if (getWorld().getTime()%getWorld().getDuration() >= rescueTime && type != Type.RESCUER && type != Type.BASIC) {
        	/** @author Ebtesam 
        	 * If it is StandBy, remove it from StandBy available list
        	 * */
            if(type == Type.STANDBY)
                getWorld().getStandbyAvailable().remove(this);
            setType(Type.RESCUER);
        }
        if (state == State.CHARGING) {
            if (type == Type.EAGLE || type == Type.STANDBY) {
            	/** @author Ebtesam 
            	 * If it is StandBy, remove it from StandBy available list
            	 * */
            	if(type == Type.STANDBY)
                    getWorld().getStandbyAvailable().remove(this);
                setType(Type.SCOUT);
            }
            getBattery().recharge(getRechargeRatio());
            if (getBattery().isFull()) {
                state = State.NORMAL;
                //TODO: Code to continue finishing up tasks the plane was doing before it had to charge
                if(nextBlock != null) {
                    setDestination(nextBlock.getCenter());
                }
                else { // if there is no assign block 
                    if (type == Type.SCOUT) {
                        if(setNextRegion()) {
                            setNextBlock(nextRegion);
                        }
                        else {
                            setType(Type.EAGLE);
                            if (!setNextBlockEagle()) {
                                setType(Type.STANDBY);
                                //Since Standby Planes don't search, they only need to wait for a rescue request.
                                //Otherwise, they idle in place.
                                nextRegion = null;
                                nextBlock = null;
                                /** @author Ebtesam 
                                 * Add standBy to the list of available standBy
                                 * */
                                getWorld().getStandbyAvailable().add(this);
                            }
                        }
                    }
                    else {
                        setNextBlockRescue();
                    }
                }
            }
            super.step();
            return;
        }

        Station st = getWorld().getNearestStation(getLocation());
        if (state == State.TO_CHARGE) {
            goCharge(st);
            super.step();
            return;
        }else if (this.getBattery().getEnergy() <= getLocation().getDistance(st.getLocation())/getSpeed()) {
            setDestination(st.getLocation());
            goCharge(st);
            super.step();
            return;
        }

        // Handle this iteration's messages
        super.step();

        // Iterate through all of the plane's list of survivors it's trying
        // to find, and see if they have died at this point.
        // TODO: Uncomment when survivors can expire
        if (!tasksToRemove.isEmpty()) {
            for (Task t:tasksToRemove) {
                t.expire();
                getWorld().removeExpired(t);
            }
        }

        tasksToRemove.clear();


        // Move the plane if it has some task to fulfill and is not charging
        // or going to charge
        if (nextBlock != null) {
            if (getWaitingTime() > 0) {
                idleAction();
                tick();
                return;
            }
            else if (move()) {
                final Block completed = nextBlock;

                if(type == Type.SCOUT) {
                    stepScout(completed);
                }
                else if(type == Type.EAGLE) {
                    stepEagle(completed);
                }
                else if(type == Type.STANDBY) {
                    	if (completed.getSurvivor().isAlive()) {
                		    triggerTaskCompleted(completed);
                		    nextBlock = null;
                		    nextRegion = null;
                    	
                    }
                }
                else if(type == Type.RESCUER) {
                    getBattery().consume(searchPowerPenalty);
                    waitFor(searchTimePenalty);
                    nextBlock.setState(Block.blockState.EXPLORED);
                    checkRegionExplored();
                    if(completed.hasSurvivor() && completed.getSurvivor().isAlive()) {
                        triggerTaskCompleted(completed);
                    }
                    setNextBlockRescue();
                }
                else if(type == Type.BASIC) {
                    getBattery().consume(searchPowerPenalty);
                    waitFor(searchTimePenalty);
                    if(completed.hasSurvivor() && completed.getSurvivor().isAlive()) {
                        triggerTaskCompleted(completed);
                    }
                    setNextBlockBasic();
                }

            }
            return;
        }

        // If we reach this point, it means that the plane is idle, so let it
        // do some "idle action"
        idleAction();
    }


    /**
     * Actions performed by the plane whenever it is in Scout mode.
     */
    private void stepScout(Block completed) {
        getBattery().consume(searchPowerPenalty);
        waitFor(searchTimePenalty);
        completed.setState(Block.blockState.EXPLORED);
       
        checkRegionExplored();
        if(completed.hasSurvivor() && completed.getSurvivor().isAlive()) {
            nextRegion.taskFound();
            triggerTaskCompleted(completed);
        }

        if(nextRegion.getState() == Region.regionState.EXPLORED) {
            //System.out.print("Scout:\nCurrent region explored:\n");
            if(getBattery().getEnergy() <= eagleEnergy) {
                //System.out.print("Scout:\nPower level too low, switching to Eagle:\n");
                setType(Type.EAGLE);
                if (!setNextBlockEagle()) {
                    //System.out.print("Eagle:\nNo unassigned blocks, switching to Standby:\n");
                    setType(Type.STANDBY);
                    //Since Standby Planes don't search, they only need to wait for a rescue request.
                    //Otherwise, they idle in place.
                    nextRegion = null;
                    nextBlock = null;
                    /** @author Ebtesam 
                     * Add Standby to the list of available StandBy
                     * */
                    getWorld().getStandbyAvailable().add(this);
                }
            }
            else {
                if(setNextRegion()) {
                    //System.out.print("Scout:\nFound unassigned region, assigning new block:\n");
                    setNextBlock(nextRegion);
                }
                else {
                    //System.out.print("Scout:\nNo unassigned region, changing to Eagle:\n");
                    setType(Type.EAGLE);
                    if (!setNextBlockEagle()) {
                        //System.out.print("Eagle:\nNo unassigned blocks, switching to Standby:\n");
                        setType(Type.STANDBY);
                        //Since Standby Planes don't search, they only need to wait for a rescue request.
                        //Otherwise, they idle in place.
                        nextRegion = null;
                        nextBlock = null;
                        /** @author Ebtesam 
                         * Add standBy to the list of available standBy
                         * */
                        getWorld().getStandbyAvailable().add(this);
                    }
                }
            }
        }
        else {
            //System.out.print("Scout:\nCurrent region still not fully explored:\n");
            if(!setNextBlock(nextRegion)) {
                //System.out.print("Scout:\nCurrent region's blocks are fully assigned, searching for new region\n");
                if(setNextRegion()) {
                    //System.out.print("Scout:\nFound unassigned region, assigning new block:\n");
                    setNextBlock(nextRegion);
                }
                else {
                    //System.out.print("Scout:\nNo unassigned region, changing to Eagle:\n");
                    setType(Type.EAGLE);
                    if (!setNextBlockEagle()) {
                        //System.out.print("Eagle:\nNo unassigned blocks, switching to Standby:\n");
                        setType(Type.STANDBY);
                        //Since Standby Planes don't search, they only need to wait for a rescue request.
                        //Otherwise, they idle in place.
                        nextRegion = null;
                        nextBlock = null;
                        /** @author Ebtesam 
                         * Add standBy to the list of available standBy
                         * */
                        getWorld().getStandbyAvailable().add(this);
                    }
                }
            }
            //System.out.print("Scout:\nAssigned new block within current region:\n");
        }
    }

    /**
     * Actions performed by the plane whenever it is in Eagle mode.
     */
    private void stepEagle(Block completed) {
        getBattery().consume(searchPowerPenalty);
        waitFor(searchTimePenalty);
        completed.setState(Block.blockState.EXPLORED);
        checkRegionExplored();
    
        if(completed.hasSurvivor() && completed.getSurvivor().isAlive()) {
            /** @author Ebtesam
             * Check if there is an available Standby for rescue
             * if not, Eagle do the rescue 
             **/
            nextRegion.taskFound();
            getWorld().foundTask(completed.getSurvivor());
            getWorld().sendStandby(completed);
            triggerTaskCompleted(completed);
        }

        if (getBattery().getEnergy() <= standbyEnergy && type == Type.EAGLE) {
            setType(Type.STANDBY);
            //Since Standby Planes don't search, they only need to wait for a rescue request.
            //Otherwise, they idle in place, so we set the next destination to be null.
            nextRegion = null;
            nextBlock = null;
            /** @author Ebtesam 
             * Add standBy to the list of available standBy
             * */
            getWorld().getStandbyAvailable().add(this);
        }
        else if (!setNextBlockEagle()){
            setType(Type.STANDBY);
            //Since Standby Planes don't search, they only need to wait for a rescue request.
            //Otherwise, they idle in place, so we set the next destination to be null.
            nextRegion = null;
            nextBlock = null;
            /** @author Ebtesam 
             * Add standBy to the list of available standBy
             * */
            getWorld().getStandbyAvailable().add(this);
        }
    }

    private void checkRegionExplored() {
        boolean remainingAssignment = false;
        boolean explored = true;
        for (Block b:getWorld().getBlocks()[nextRegion.getID()]) {
            if (b.getState() == Block.blockState.ASSIGNED) {
                remainingAssignment = true;
                explored = false;
            }
            if (b.getState() == Block.blockState.UNASSIGNED) {
                explored = false;
            }
        }
        if((type == Type.EAGLE || type == Type.RESCUER) && !explored && !remainingAssignment){
            nextRegion.setState(Region.regionState.UNASSIGNED);
            return;
        }
        if(explored) {
            nextRegion.setState(Region.regionState.EXPLORED);
        }
    }

    /**
     * Action done by the plane whenever it is ready to handle tasks but no
     * task has been assigned to it.
     */
    protected void idleAction() {
        if (!getIdleStrategy().idleAction(this)) {
            double newAngle = getAngle() + 0.01;
            setAngle(newAngle);
            if(getWorld().getTime()%3 == 0) {
                getBattery().consume(1);
            }
        }
    }

    @Override
    public void setSpeed(double speed) {
        super.setSpeed(speed);
        // Section below only runs upon initialization to set the maximum speed
        if (this.maxSpeed < 0) {
            this.maxSpeed = speed;
            super.setSpeed(maxSpeed*scoutSpeedPercentage);
        }
    }

    /**
     * Method executed when a plane has just enough battery to go recharge
     * itself
     *
     * @param st charging station where to recharge
     */
    protected void goCharge(Station st) {
        state = State.TO_CHARGE;
        if (move()) {
            state = State.CHARGING;
            getCompletedLocations().add(st.getLocation());
        }
    }

    /**TODO: Make sure this is coded well.
     * Signals that a task has been completed.
     *
     * @param b block in which task has been completed.
     */
    private void taskCompleted(Block b) {
        if(type == Type.EAGLE || type == Type.SCOUT) {
            for (Block[] blocks : getWorld().getBlocks()) {
                for (Block block : blocks) {
                    if (block.getId() == b.getId()) {
                        block.setState(Block.blockState.EXPLORED);
                    }
                }
            }
        }
    }

    protected void taskCompleted(Task t) {}

    private void setNextBlockBasic() {
        Random rnd = new Random();
        if (getWorld().getUnassignedBlocks().size() < 1){
            idleAction();
            return;
        }
        nextBlock = getWorld().getUnassignedBlocks().remove(rnd.nextInt(getWorld().getUnassignedBlocks().size()));
        setDestination(nextBlock.getCenter());
    }
    /**
     * Sets the next region that this plane is going to fulfill.
     *
     * This plane will fly in a straight line towards that location, until one
     * of the following happens:
     *   - It reaches (and thus completes) the given task.
     *   - It runs out of battery (and therefore goes to recharse itself).
     *   - A new "next region" is set by calling this method again.
     *
     */
    private boolean setNextRegion() {
        List<Region> regionsNear = new ArrayList<Region>();
        List<Region> regionsFar = new ArrayList<Region>();

        Random rnd = new Random();


        for (Region r:getWorld().getRegions()) {
            if (r.getState()== Region.regionState.UNASSIGNED) {
                if (this.getLocation().getDistance(r.getCenter()) < scoutJumpDistance) {
                    regionsNear.add(r);
                }
                else {
                    regionsFar.add(r);
                }
            }
        }

        if (!regionsNear.isEmpty()) {
            nextRegion = regionsNear.remove(rnd.nextInt(regionsNear.size()));
            nextRegion.setState(Region.regionState.ASSIGNED);
            return true;
        }
        else if (!regionsFar.isEmpty()) {
            nextRegion = regionsFar.remove(rnd.nextInt(regionsFar.size()));
            nextRegion.setState(Region.regionState.ASSIGNED);
            return true;
        }
        else {
            return false;
        }
    }

    /**
     * Sets the next region that this plane is going to fulfill.
     *
     * This plane will fly in a straight line towards that location, until one
     * of the following happens:
     *   - It reaches (and thus completes) the given task.
     *   - It runs out of battery (and therefore goes to recharse itself).
     *   - A new "next region" is set by calling this method again.
     *
     * @param r region to select block from.
     */
    private boolean setNextBlock(Region r) {

        //First check if region we are attempting to get a block from is fully assigned:
        List<Block> availableBlocks = new ArrayList<Block>();
        int id = r.getID();
        for (Block block:getWorld().getBlocks()[id]) {
            if (block.getState()== Block.blockState.UNASSIGNED) {
                availableBlocks.add(block);
            }
        }
        if (availableBlocks.isEmpty()) {
            return false;
        }
        else {
            Random rand = new Random();
            nextBlock = availableBlocks.get(rand.nextInt(availableBlocks.size()));
            getWorld().getUnassignedBlocks().remove(nextBlock);

            //System.out.print("Assigned block:\n");
            //System.out.print("Region ID: "+id+", Block ID: "+nextBlock.getId()+"\n");
            //System.out.print("Location: "+nextBlock.getCenter().getX()+", "+nextBlock.getCenter().getY()+"\n");

            nextBlock.setState(Block.blockState.ASSIGNED);
            setDestination(nextBlock.getCenter());
            return true;
        }
    }

    /**
     * TODO: Finish coding this section.
     * Sets the next block that this plane is going to fulfill.
     * This method should only be called by Eagle Planes.
     *
     * This plane will fly in a straight line towards that location, until one
     * of the following happens:
     *   - It reaches (and thus completes) the given task.
     *   - It runs out of battery (and therefore goes to recharges itself).
     *   - A new "next region" is set by calling this method again.
     *
     */
    private boolean setNextBlockEagle() {

//        boolean blockChanged = false;
//        double avgDist = 0;
//        double mDist = Double.MAX_VALUE;
//        Block isolated = null;

        List<Block> blocksTried = new ArrayList<Block>();
        List<Block> blocksNear = new ArrayList<Block>();
        List<Block> blocksFar = new ArrayList<Block>();
        Random rnd = new Random();

        while (!getWorld().getUnassignedBlocks().isEmpty() && blocksNear.size() < 1 && blocksFar.size() < 10) {
            Block b = getWorld().getUnassignedBlocks().remove(rnd.nextInt(getWorld().getUnassignedBlocks().size()));
            if(crowdCheck(b)) {
                //TODO: Set jump distance using Configuration
                if(this.getLocation().getDistance(b.getCenter()) < eagleJumpDistance) {
                    blocksNear.add(b);
                }
                else {
                    blocksFar.add(b);
                }
            }
            else {
                blocksTried.add(b);
            }
        }

        if (!blocksNear.isEmpty()) {
            nextBlock = blocksNear.remove(rnd.nextInt(blocksNear.size()));
            nextBlock.setState(Block.blockState.ASSIGNED);
            setDestination(nextBlock.getCenter());
            for (Block near: blocksNear) {
                getWorld().getUnassignedBlocks().add(near);
            }
            for (Block far: blocksFar) {
                getWorld().getUnassignedBlocks().add(far);
            }
            for (Block tried: blocksTried) {
                getWorld().getUnassignedBlocks().add(tried);
            }
            nextRegion = getWorld().getRegions().get(nextBlock.getRegion());
            nextRegion.setState(Region.regionState.ASSIGNED);
            return true;
        }
        else if (!blocksFar.isEmpty()) {
            nextBlock = blocksFar.remove(rnd.nextInt(blocksFar.size()));
            nextBlock.setState(Block.blockState.ASSIGNED);
            setDestination(nextBlock.getCenter());
            for (Block far: blocksFar) {
                getWorld().getUnassignedBlocks().add(far);
            }
            for (Block tried: blocksTried) {
                getWorld().getUnassignedBlocks().add(tried);
            }
            nextRegion = getWorld().getRegions().get(nextBlock.getRegion());
            nextRegion.setState(Region.regionState.ASSIGNED);
            return true;
        }
        else {
            for (Block tried: blocksTried) {
                getWorld().getUnassignedBlocks().add(tried);
            }
            return false;
        }

//        for (Block[] blocks: getWorld().getBlocks()) {
//            for (Block b: blocks) {
//                if (b.getState() == Block.blockState.UNASSIGNED && crowdCheck(b)) {
//                    blockChanged = true;
//                    avgDist = 0;
//                    for (Plane p : getWorld().getPlanes()) {
//                        avgDist += b.getCenter().getDistance(p.getLocation());
//                    }
//                    avgDist = avgDist / getWorld().getPlanes().size();
//                    if (avgDist < mDist) {
//                        mDist = avgDist;
//                        isolated = b;
//                    }
//                }
//            }
//        }
//        if(blockChanged) {
//            nextBlock = isolated;
//            setDestination(nextBlock.getCenter());
//        }
//        return blockChanged;
    }

    //TODO: Double check if this is how Heba wants crowd distance control.
    private boolean crowdCheck(Block b) {

        int leftBound = b.getxLoc() - getEagleCrowdDistance();
        int rightBound = b.getxLoc() + getEagleCrowdDistance() + 1;
        int lowerBound = b.getyLoc() - getEagleCrowdDistance();
        int upperBound = b.getyLoc() + getEagleCrowdDistance() + 1;

        if(leftBound < 0) {
            leftBound = 0;
        }
        if(rightBound >= getWorld().getBlockGrid().length) {
            rightBound = getWorld().getBlockGrid().length - 1;
        }
        if(lowerBound < 0) {
            lowerBound = 0;
        }
        if(upperBound >= getWorld().getBlockGrid().length) {
            upperBound = getWorld().getBlockGrid().length - 1;
        }

        for (int i = leftBound; i < rightBound; i++) {
            for (int j = lowerBound; j < upperBound; j++) {
                Block a = getWorld().getBlockGrid()[i][j];
                if (Math.abs(a.getxLoc()-b.getxLoc())+Math.abs(a.getyLoc()-b.getyLoc()) < getEagleCrowdDistance() &&
                        a.getState() == Block.blockState.ASSIGNED) {
                    return false;
                }
            }
        }
//        for (Block[] blocks: getWorld().getBlocks()) {
//            for (Block a: blocks) {
//                if (Math.abs(a.getxLoc()-b.getxLoc())+Math.abs(a.getyLoc()-b.getyLoc()) < getEagleCrowdDistance() &&
//                        a.getState() == Block.blockState.ASSIGNED) {
//                    return false;
//                }
//            }
//        }
        return true;
    }

    protected void setNextBlockStandby(Block b) {
        nextBlock = b;
        nextRegion = getWorld().getRegions().get(b.getRegion());
        setDestination(b.getCenter());
    }

    /**
     * Sets the next region that this plane is going to fulfill.
     *
     * This plane will fly in a straight line towards that location, until one
     * of the following happens:
     *   - It reaches (and thus completes) the given task.
     *   - It runs out of battery (and therefore goes to recharges itself).
     *   - A new "next region" is set by calling this method again.
     *
     */
    private void setNextBlockRescue() {

        //boolean allExplored = true;
        List<Region> regionsUnexplored = new ArrayList<Region>();
        int maxCrowded = -1;

        for (Region r: getWorld().getRegions()) {
            if (r.getState() != Region.regionState.EXPLORED) {
                regionsUnexplored.add(r);
                if(r.getTasksFound() > maxCrowded) {
                    nextRegion = r;
                    maxCrowded = r.getTasksFound();
                }
            }
        }

        if (regionsUnexplored.isEmpty()) {
            nextRegion = null;
            nextBlock = null;
            return;
        }

        List<Block> searchList = new ArrayList<Block>();
        for (Block b: getWorld().getBlocks()[nextRegion.getID()]) {
            if (b.getState() != Block.blockState.EXPLORED) {
                searchList.add(b);
            }
        }

        Random rnd = new Random();
        nextBlock = searchList.get(rnd.nextInt(searchList.size()));
        getWorld().getUnassignedBlocks().remove(nextBlock);

//        for (Block[] blocks: getWorld().getBlocks()) {
//            for (Block b: blocks) {
//                if (b.getState() != Block.blockState.EXPLORED) {
//                    allExplored = false;
//                }
//            }
//        }
//        if (allExplored) {
//            return false;
//        }
//        else {
//
//            for (Block[] blocks: getWorld().getBlocks()) {
//
//                for (Block b: blocks) {
//                    if (b.getState() == Block.blockState.EXPLORED && b.hasSurvivor()) {
//                        nextBlock = b;
//                        setDestination(nextBlock.getCenter());
//                        return true;
//                    }
//                }
//
//            }
//            return false;
//        }
    }

    /**
     * Record a task completion trigger any post-completion effects
     *
     * @param b block in which task has been completed
     */
    private void triggerTaskCompleted(Block b) {
        Task t = b.getSurvivor();
        getLog().log(Level.FINE, "{0} completes {1}", new Object[]{this, t});
        getCompletedLocations().add(t.getLocation());
        if (getType() != Type.STANDBY && getType() != Type.EAGLE) {
            getWorld().foundTask(t);
        }
        getWorld().removeTask(t);
        removeTask(t);
        taskCompleted(t);
        final long timeLeft = getWorld().getDuration() - getWorld().getTime()%getWorld().getDuration();
        waitFor((long)(timeLeft*rescueTimePenalty));
        getBattery().consume((long)(getBattery().getEnergy()*rescuePowerPenalty));
//        if (nextBlock == null) {
//            Operator o = getWorld().getNearestOperator(getLocation());
//            setDestination(o.getLocation());
//        }
    }

    /**TODO: Change to instead of setNextTask, setNextBlock or setNextRegion
     * Signals that a new task has been added.
     *
     * @param t task that has been added.
     */
    protected void taskAdded(Task t) {}

    /**TODO
     * Signals that a task has been removed.
     *
     * @param t task that has been removed.
     */
    protected void taskRemoved(Task t) {}

    @Override
    public Task removeTask(Task task) {
        for (Plane p : getWorld().getPlanes()) {
            p.getSearchForTasks().remove(task);
            p.getTasks().remove(task);
        }
        // TODO: Remove next line?
        taskRemoved(task);
        return task;
    }

}