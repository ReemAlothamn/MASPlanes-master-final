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

import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Communication adapter that delivers messages by rounds.
 * <p/>
 * This adapter buffers all the messages being sent, and only delivers them
 * when it is ticked. This makes it very easy to implement lock-stepped max-sum,
 * provided that you do *not* need to send the messages through a simulated
 * network and/or tamper with them in any way.
 *
 * @author Marc Pujol <mpujol@iiia.csic.es>
 */
public class TickCommunicationAdapter implements CommunicationAdapter<Factor<Factor<?>>> {
    private static final Logger LOG = Logger.getLogger(TickCommunicationAdapter.class.getName());

    private final ArrayList<Message> buffer = new ArrayList<Message>();

    @Override
    public void send(double message, Factor<Factor<?>> sender, Factor<Factor<?>> recipient) {
        LOG.log(Level.FINEST, "Message from {0} to {1} : {2}", new Object[]{sender, recipient, message});
        buffer.add(new Message(message, sender, recipient));
    }

    /**
     * Messages are buffered until the channel is ticked, when it delivers all
     * of the messages sent since the last tick.
     */
    public void tick() {
        for (Message m : buffer) {
            m.recipient.receive(m.value, m.sender);
        }
        buffer.clear();
    }

    /**
     * This is just a holder of typed values. Nothing special about it.
     */
    private class Message {
        public final double value;
        public final Factor<Factor<?>> sender;
        public final Factor<Factor<?>> recipient;
        public Message(double value, Factor<Factor<?>> sender, Factor<Factor<?>> recipient) {
            this.value = value;
            this.sender = sender;
            this.recipient = recipient;
        }
    }

}
