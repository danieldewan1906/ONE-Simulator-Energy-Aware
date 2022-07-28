/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package report;

import core.DTNHost;
import core.Message;
import core.MessageListener;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author DANIEL
 */
public class MessageDeliveredReport extends Report implements MessageListener {

    public Map<DTNHost, Integer> mDelivered;

    public MessageDeliveredReport() {
        init();
        this.mDelivered = new HashMap<>();
    }

    public void messageTransferred(Message m, DTNHost from, DTNHost to, boolean firstDelivery) {

    }

    public void newMessage(Message m) {
    }

    public void messageDeleted(Message m, DTNHost where, boolean dropped) {
    }

    public void messageTransferAborted(Message m, DTNHost from, DTNHost to) {
    }

    public void messageTransferStarted(Message m, DTNHost from, DTNHost to) {
        if (this.mDelivered.containsKey(to)) {
            this.mDelivered.put(to, mDelivered.get(to) + 1);
        } else {
            this.mDelivered.put(to, 1);
        }
    }

    @Override
    public void done() {
        for (Map.Entry<DTNHost, Integer> entry : mDelivered.entrySet()) {
            DTNHost key = entry.getKey();
            Integer value = entry.getValue();
            write("Node " + key + " \t" + value);
        }
        super.done();
    }

}
