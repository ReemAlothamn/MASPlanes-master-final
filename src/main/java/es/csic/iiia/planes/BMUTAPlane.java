/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package es.csic.iiia.planes;
import es.csic.iiia.planes.gui.GUIWorld;
import es.csic.iiia.planes.Plane.Type;
import es.csic.iiia.planes.BMUTA.BMUTABehavior;
import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.logging.Level;

/**
 *
 * @author Noura
 */
public class BMUTAPlane extends AbstractPlane {
    
    /**
     * Current plane type
     * By default, all planes start as SEARCHER.
     */
    
     private Type type = Type.SEARCHER;
     
     /**
     * Penalty incurred on waiting time for searching a block.
     */
    private long searchTimePenalty;
    
    /**
     * Penalty incurred on waiting time for rescuing a survivor.
     */
    private double rescueTimePenalty;

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
     * 
     * 
     */
    private Region nextRegion = null;
    

    /**
     * Default constructor
     *
     * @param location initial location of the plane
     */
    public BMUTAPlane(Location location) {
        super(location);
       addBehavior(new BMUTABehavior(this));
       
    }
     @Override
    public void initialize() {
        super.initialize();
   
        searchTimePenalty = getWorld().getConfig().getSearchTimePenalty();
        rescueTimePenalty = getWorld().getConfig().getRescueTimePenalty();
       

        if(initialized) {
            if (type == Type.SEARCHER) {
                setNextRegion();
                setNextBlock(nextRegion);
            }
            else {
                setNextRegionRescue();
                setNextBlockRescue(nextRegion);
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
   
    }
    
    @Override
    public List<Location> getPlannedLocations() {
        List<Location> plannedLocations = new ArrayList<Location>();

        plannedLocations.add(getCurrentDestination().destination);
        return plannedLocations;
    }

     @Override
    public void step() {
        
        super.BmutaStep();
       

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
        
 if (getWaitingTime() > 0) {
                
             double newAngle = getAngle() + 0.01;
             setAngle(newAngle);
                tick();
                return;
            } 
    
        //Switch to Searcher if the idel time greater than or equals to threshold
    
             if( getIdleTime()>=1600&& type != Type.SEARCHER && !getWorld().getUnassignedBlocks().isEmpty()&&getNumOfbeacones()>0){
              setType(Type.SEARCHER);
              setIdleTime(0);
              //System.out.println("Rescuer to Searcher "+getId());
              nextRegion = null;
              nextBlock = null;
            } 
                
                if(nextBlock != null) {
                
                    setDestination(nextBlock.getCenter());
                }
                else { // if there is no assign block 
               
                    if (type == Type.SEARCHER) {
                        
                        if(nextRegion==null){    
                            
                        if(setNextRegion()) {
               
                            setNextBlock(nextRegion);
                        }
                        else {// if there is no Unexplored Region  
                            
                           if (!setNextRegionPending()){// if there is no pending region change to Rescuer
                               
                               setType(Type.RESCUER);
                               setIdleTime(0);
                               
                //               System.out.println(" Searcher to Rescuer 1");
                               
                               if(setNextRegionRescue())
                               setNextBlockRescue(nextRegion);
                               else{
                                   nextBlock=null;
                                   nextRegion=null;
                                   
                               }
                           
                           }
                    else
                            setNextBlock(nextRegion);
                        }
                    
                    } else {
                        
                        // setNextBlock(nextRegion);
                         if(!setNextBlock(nextRegion)) {
                //System.out.print("Scout:\nCurrent region's blocks are fully assigned, searching for new region\n");
                if(setNextRegion()) {
                    //System.out.print("Scout:\nFound unassigned region, assigning new block:\n");
                    setNextBlock(nextRegion);
                }
                else {
                         if(!setNextRegionPending()){
                         
                               setType(Type.RESCUER);
                               setIdleTime(0);
                              // System.out.println(" Searcher to Rescuer 3");
                               
                               if(setNextRegionRescue())
                               setNextBlockRescue(nextRegion);
                               else{
                                   nextBlock=null;
                                   nextRegion=null;
                                   
                               }
                             // setNextBlockRescue();
                      }
                    else{
                        setNextBlock(nextRegion);
                    }
            }
           
        }
                         
                         
                         
                         
                        }
                    
                    }
                        else 
                            if (type == Type.RESCUER){
                           
                                if(nextRegion==null){
                               if (setNextRegionRescue())
                               setNextBlockRescue(nextRegion);
                                }
                                else{
                                 // setNextBlockRescue(nextRegion); 
                                if(!setNextBlockRescue(nextRegion)) {
                //System.out.print("Scout:\nCurrent region's blocks are fully assigned, searching for new region\n");
                       if(setNextRegionRescue()) {
                    //System.out.print("Scout:\nFound unassigned region, assigning new block:\n");
                        setNextBlockRescue(nextRegion);
                       }
                else {
                         nextBlock=null;
                       nextRegion=null;
                     
                }
           
        }  
                                }
                                
                            }
                
                }

      
   
       
 

       
        if (nextBlock != null) {
              
             if (move()) { 
                 
                final Block completed = nextBlock;

                if(type == Type.SEARCHER) {
          
                    stepSearcher(completed);
                    
                }
                else if(type == Type.RESCUER) {
                    setIdleTime(0);
                    stepRescuer(completed);
                  
                    
               //nextBlock=null;
              // nextRegion=null;
              // setNextBlockRescue();
    
                }
        
            }
            return;
        }

        // If we reach this point, it means that the plane is idle, so let it
        // do some "idle action"
        idleAction();
        
       
    }
    
     /**
     * Actions performed by the plane whenever it is in Searcher mode.
     */
    private void stepSearcher(Block completed) {
            
       waitFor(searchTimePenalty);
      
  //  if(completed.hasSurvivor())
    //       System.out.println(completed.hasSurvivor()+" "+completed.getId());
    
        if(completed.hasSurvivor() && completed.getSurvivor().isAlive()) {
           // System.out.println("TT plane"+id+" type "+type+ " beacons" +getNumOfbeacones()+" idel time"+getIdelTime());

           nextRegion.taskFound();
           nextRegion.setNumOfbeacones(nextRegion.getNumOfbeacones()+1);
           getWorld().foundTask(completed.getSurvivor());   
           completed.setState(Block.blockState.PENDING);
           completed.setBeaconOrNot(true);
           setNumOfbeacones(getNumOfbeacones()-1);
           completed.getSurvivor().setFound(true);
           
           // Switch to Rescuer if number of beacons smaller than or equals to threshold
           
           if (getNumOfbeacones()<= getthresholdB() ) {
            
            setType(Type.RESCUER);
            setIdleTime(0);
      //      System.out.println("Searcher To Rescuer ");
           
        }
           
        }
        else
        { 
                
                        if(!completed.hasSurvivor()||!(completed.getSurvivor().isAlive())){
                        nextBlock.setState(Block.blockState.EMPTY);
      //               System.out.println("Block Empty ");
                             }
                    }
        
           checkRegionExplored();

          if ( type == Type.RESCUER ) {
//        
             nextRegion = null;
             nextBlock = null;
       //   setNextBlockRescue();
            return;
        }
         
      else if(nextRegion.getState() == Region.regionState.EXPLORED) {
               
              /*if(setNextRegion()) {
                    //System.out.print("Scout:\nFound unassigned region, assigning new block:\n");
                    setNextBlock(nextRegion);
                }
                else {
                       
                         if (!setNextRegionPending()){
                               setType(Type.RESCUER);
                               setIdleTime(0);
                               System.out.println(" Searcher to Rescuer 2");
                               nextRegion = null;
                               nextBlock = null;
                      //  setNextBlockRescue();
                       
                       }
                    else{
                             setNextBlock(nextRegion);
                    }
                }*/
              nextRegion=null;
              nextBlock=null;
            }
        
        else {
          
          nextBlock=null;
          
           /*  if(!setNextBlock(nextRegion)) {
                //System.out.print("Scout:\nCurrent region's blocks are fully assigned, searching for new region\n");
                if(setNextRegion()) {
                    //System.out.print("Scout:\nFound unassigned region, assigning new block:\n");
                    setNextBlock(nextRegion);
                }
                else {
                         if(!setNextRegionPending()){
                         
                               setType(Type.RESCUER);
                               setIdleTime(0);
                               System.out.println(" Searcher to Rescuer 3");   
                                nextRegion = null;
                                nextBlock = null;
                             // setNextBlockRescue();
                      }
                    else{
                        setNextBlock(nextRegion);
                    }
            }
           
        }*/
   
    }
    }
    
    private void checkRegionExplored() {
        boolean remainingAssignment = false;
        boolean explored = true;
        for (Block b:getWorld().getBlocks()[nextRegion.getID()]) {
            if (b.getState() == Block.blockState.PENDING||b.getState() == Block.blockState.ASSIGNED) {
                remainingAssignment = true;
                explored = false;
            }
            if (b.getState() == Block.blockState.UNASSIGNED) {
                explored = false;
            }
        }
           if( !explored && !remainingAssignment){
            nextRegion.setState(Region.regionState.PENDING);
            return;
        }
        if(explored) {
            nextRegion.setState(Region.regionState.EXPLORED);
        //   System.out.println("Region Explored");
        }
    }
    
     protected void idleAction() {
      
        if (!getIdleStrategy().idleAction(this)) {
            double newAngle = getAngle() + 0.01;
          //  setAngle(newAngle);
            setIdleTime(getIdleTime()+1);
           
        }
    }
     
      /**
     * Sets the next region that this plane is going to fulfill.
     *
     * This plane will fly in a straight line towards that location, until one
     * of the following happens:
     *   - It reaches (and thus completes) the given task.
     *   - A new "next region" is set by calling this method again.
     *
     */ 
 private boolean setNextRegion() {
        List<Region> regions = new ArrayList<Region>();
 
        for (Region r:getWorld().getRegions()) {
       
            if (r.getState()== Region.regionState.UNASSIGNED) { 
                    regions.add(r);
                        
                }
            }
        

        if (!regions.isEmpty()) {
         
            Region t=regions.get(0);
         
            for(Region r:regions){
            if(this.getLocation().getDistance(r.getCenter())<= this.getLocation().getDistance(t.getCenter())){
            
                t=regions.get(regions.indexOf(r));
                
            }
            
            }
            nextRegion = regions.get(regions.indexOf(t));
            nextRegion.setState(Region.regionState.PENDING);
            return true;
        }
      
        else {
            return false;
        }
    }
 
 private boolean setNextRegionPending() {
        List<Region> regions = new ArrayList<Region>();

        for (Region r:getWorld().getRegions()) {
            if (r.getState()== Region.regionState.PENDING) { 
                    regions.add(r);
                      
                }
            }
        

        if (!regions.isEmpty()) {
         
            Region t=null;
            for(Region r:regions){
                int id = r.getID();
                for(Block s:getWorld().getBlocks()[id]){
            if (s.getState()== Block.blockState.UNASSIGNED){  
                t=regions.get(regions.indexOf(r));
                break;
                }}
            }
            
            if(t!=null){
       
            for(Region p:regions){
                 boolean n=false;
                 int id = p.getID();
                for(Block s:getWorld().getBlocks()[id]){
            if (s.getState()== Block.blockState.UNASSIGNED)  
                n=true;
                }        
            if(n&&this.getLocation().getDistance(p.getCenter())<= this.getLocation().getDistance(t.getCenter())){
           
                t=regions.get(regions.indexOf(p));
                
            }
                
                }
            
            nextRegion = regions.get(regions.indexOf(t));
            return true;
            }
            else 
                return false;
            }
        
        return false;
        
 }
      
        
    
 
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
            nextBlock=null;
            nextRegion=null;
            return false;
        }
        else {
     
            Block t=availableBlocks.get(0);
          //  availableBlocks.add(t);
            for(Block s:availableBlocks){
            if(this.getLocation().getDistance(s.getCenter())<= this.getLocation().getDistance(t.getCenter()))
                t=availableBlocks.get(availableBlocks.indexOf(s));
            }
 
            nextBlock = availableBlocks.get(availableBlocks.indexOf(t));
            availableBlocks.remove(availableBlocks.indexOf(nextBlock));
            getWorld().getUnassignedBlocks().remove(nextBlock);

            //System.out.print("Assigned block:\n");
            //System.out.print("Region ID: "+id+", Block ID: "+nextBlock.getId()+"\n");
            //System.out.print("Location: "+nextBlock.getCenter().getX()+", "+nextBlock.getCenter().getY()+"\n");
            
            // may we use it !(ASSIGNED block) 
           nextBlock.setState(Block.blockState.ASSIGNED);
            setDestination(nextBlock.getCenter());
            return true;
        }
    }
  
  private void stepRescuer(Block completed){
      
    if(completed.getBeaconOrNot() && completed.getSurvivor().isAlive()) {
                   
                       triggerTaskCompleted(completed);
                       System.out.println("Survivor Rescued");
                       completed.setState(Block.blockState.OK);
                       completed.setBeaconOrNot(false);
                       setNumOfbeacones(getNumOfbeacones()+1);  
                       nextRegion.setNomOFBlocksassignToPlane(nextRegion.getNomOFBlocksassignToPlane()-1);
                       nextRegion.setNumOfbeacones(nextRegion.getNumOfbeacones()-1);
                      
                        
                    }
                    else{ 
                        
                        if(completed.getBeaconOrNot()&&!(completed.getSurvivor().isAlive())){
                        completed.setState(Block.blockState.EMPTY);
                        completed.setBeaconOrNot(false);
                        setNumOfbeacones(getNumOfbeacones()+1);
                       // completed.getSurvivor().expire();
                        //getWorld().removeExpired(completed.getSurvivor());
                        nextRegion.setNumOfbeacones(nextRegion.getNumOfbeacones()-1);
                         
                         
                        }
                    }
                    
                    checkRegionExplored();
         
          if(nextRegion.getState() == Region.regionState.EXPLORED) {
               
             /* if(setNextRegionRescue()) {
                    //System.out.print("Scout:\nFound unassigned region, assigning new block:\n");
                    setNextBlockRescue(nextRegion);
                }
                else {
                       nextBlock=null;
                       nextRegion=null;
                         
                }*/
                       nextBlock=null;
                       nextRegion=null;
            }
        
        else {
             
              nextBlock=null;
             
              /*if(!setNextBlockRescue(nextRegion)) {
                //System.out.print("Scout:\nCurrent region's blocks are fully assigned, searching for new region\n");
                if(setNextRegionRescue()) {
                    //System.out.print("Scout:\nFound unassigned region, assigning new block:\n");
                    setNextBlockRescue(nextRegion);
                }
                else {
                         nextBlock=null;
                       nextRegion=null;
                     
                }
           
        }      */     
  }
  }
  
  private boolean setNextRegionRescue(){
  
   
        List<Region> regionsUnexplored = new ArrayList<Region>();
        int maxBeacones = -1;

        for (Region r: getWorld().getRegions()) {
            if (r.getState() != Region.regionState.EXPLORED&&r.getNumOfbeacones()>r.getNomOFBlocksassignToPlane() ) {
                regionsUnexplored.add(r);
                if(r.getNumOfbeacones() > maxBeacones) {
                    nextRegion = r;
                    maxBeacones = r.getNumOfbeacones();
                }
                else if(r.getNumOfbeacones() == maxBeacones&&this.getLocation().getDistance(r.getCenter())<= this.getLocation().getDistance(nextRegion.getCenter()) ) {
                    nextRegion = r;
         //           maxBeacones = r.getNumOfbeacones();
                } 
            }
        }

        if (regionsUnexplored.isEmpty()) {
          //  System.out.println("regionsUnexplored is empty");
            nextRegion = null;
            nextBlock = null;
            return false;
        }
  return true ;
  }
  
    private boolean setNextBlockRescue(Region r) {
           
       

        List<Block> searchList = new ArrayList<Block>();
        for (Block b: getWorld().getBlocks()[r.getID()]) {
            if (b.getState() == Block.blockState.PENDING&&!b.getassignToPlane()) {
                searchList.add(b);
            }
        }
        if(!searchList.isEmpty()){
           Block t=searchList.get(0);
          //  searchList.add(t);
    
            for(Block s:searchList){
            if(this.getLocation().getDistance(s.getCenter())<= this.getLocation().getDistance(t.getCenter()))
                t=searchList.get(searchList.indexOf(s));
            }
     
        nextBlock = searchList.get(searchList.indexOf(t));
        nextBlock.setassignToPlane(true);
        nextRegion.setNomOFBlocksassignToPlane(nextRegion.getNomOFBlocksassignToPlane()+1);
        setDestination(nextBlock.getCenter());

          return true;
        }
             //  System.out.println("searchList is empty");
        return false;

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
       
        getWorld().removeTask(t);
        removeTask(t);
        taskCompleted(t);
        final long timeLeft = getWorld().getDuration() - getWorld().getTime()%getWorld().getDuration();
        waitFor((long)(timeLeft*rescueTimePenalty));

    }
   protected void taskRemoved(Task t) {} 
   
   protected void taskAdded(Task t) {}
    
   protected void taskCompleted(Task t) {}
    
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
