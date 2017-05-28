/*
 * Software License Agreement (BSD License)
 *
 * Copyright 2013 Marc Pujol <mpujol@iiia.csic.es>.
 *
 * Redistribution and use of this software in source and binary forms, with or
 * without modification, are permitted provided that the following conditions
 * are met:
 *
 *   Redistributions of source code must retain the above
 *   copyright notice, this list of conditions and the
 *   following disclaimer.
 *
 *   Redistributions in binary form must reproduce the above
 *   copyright notice, this list of conditions and the
 *   following disclaimer in the documentation and/or other
 *   materials provided with the distribution.
 *
 *   Neither the name of IIIA-CSIC, Artificial Intelligence Research Institute
 *   nor the names of its contributors may be used to
 *   endorse or promote products derived from this
 *   software without specific prior written permission of
 *   IIIA-CSIC, Artificial Intelligence Research Institute
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */
package es.csic.iiia.bms;

import java.util.List;
import java.util.Map;

/**
 * Basic definition of a MaxSum factor.
 *
 * @param <T> Type of the factor's identity.
 * @author Marc Pujol <mpujol@iiia.csic.es>
 */

public interface Factor<T> {

    /**
     * Sets the identity of this factor.
     *
     * @param identity object represented by this factor.
     */
    public void setIdentity(T identity);

    /**
     * Get the identity of this factor.
     *
     * @return identity of this factor.
     */
    public T getIdentity();

    /**
     * Get the <em>max</em> operator used by this factor.
     *
     * @return <em>max</em> operator used by this factor.
     */
    public MaxOperator getMaxOperator();

    /**
     * Set the maximization operator used by this factor.
     * @param maxOperator maximization operator to employ.
     */
    public void setMaxOperator(MaxOperator maxOperator);

    /**
     * Set the communication adapter used by this factor.
     *
     * @param manager communication adapter to use.
     */
    public void setCommunicationAdapter(CommunicationAdapter<T> manager);

    /**
     * Get the communication adapter used by this factor.
     *
     * @return communication adapter used by this factor.
     */
    public CommunicationAdapter<T> getCommunicationAdapter();

    /**
     * Adds a new neighbor of this factor (graph link).
     *
     * @param factor new neighbor.
     */
    public void addNeighbor(T factor);

    /**
     * Removes the specified neighbor from this factor.
     *
     * @param factor neighbor to remove.
     * @return <code>true</code> if this factor contained the specified neighbor
     */
    public boolean removeNeighbor(T factor);

    /**
     * Get the neighbors of this factor.
     *
     * @return neighbors of this factor
     */
    public List<T> getNeighbors();

    /**
     * Removes all neighbors of this factor.
     */
    public void clearNeighbors();

    /**
     * Receive a message.
     *
     * @param message message to receive
     * @param sender sender of the message
     */
    public void receive(double message, T sender);

    /**
     * Send a message to a neighboring factor.
     *
     * @param message message to send
     * @param recipient intended recipient
     */
    public void send(double message, T recipient);

    /**
     * Get the last message received from the given neighbor.
     *
     * @param neighbor neighbor whose message to get.
     * @return message received from the given neighbor.
     */
    public double getMessage(T neighbor);

    /**
     * Evaluate this factor given the neighbor's values.
     *
     * @param values map of value for each neighbor
     * @return cost/utility of this factor given the neighbor's values.
     */
    public double evaluate(Map<T, Boolean> values);

    /**
     * Run the this factor (process incoming messages and send out new ones).
     *
     * @return number of Constraint Checks performed by this node.
     */
    public long run();

}
