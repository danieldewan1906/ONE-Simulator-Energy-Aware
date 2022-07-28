/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package report;

import core.DTNHost;
import core.Message;
import core.MessageListener;
import core.SimClock;
import core.UpdateListener;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author DANIEL
 */
public class DeliveryProbabilityReport extends Report implements MessageListener, UpdateListener {

    private int terkirim;
    private int terbuat;
    public double lastRecord = Double.MIN_VALUE;
    public int interval = 600;
    public Map<Double, Double> probability;

    public DeliveryProbabilityReport() {
        super();
        terkirim = 0;
        terbuat = 0;
        probability = new HashMap<>();
    }

    @Override
    public void newMessage(Message m) {
        terbuat++;
    }

    @Override
    public void messageTransferStarted(Message m, DTNHost from, DTNHost to) {
    }

    @Override
    public void messageDeleted(Message m, DTNHost where, boolean dropped) {
    }

    @Override
    public void messageTransferAborted(Message m, DTNHost from, DTNHost to) {
    }

    @Override
    public void messageTransferred(Message m, DTNHost from, DTNHost to, boolean finalTarget) {
        if (finalTarget) {
            terkirim++;
        }
    }

    @Override
    public void updated(List<DTNHost> hosts) {
        double simTime = SimClock.getTime();
        if (simTime - lastRecord >= interval) {
            double probabilitynya = ((1.0 * this.terkirim) / this.terbuat);//hitung probability pesan terkirim
            this.lastRecord = simTime - simTime % interval;
            probability.put(lastRecord, probabilitynya);
        }
    }
    
    @Override
    public void done(){
        String printLn = "Time\tProbabilityMessageDelivered\n";
        for (Map.Entry<Double, Double> entry : probability.entrySet()) {
            Double key = entry.getKey();
            Double value = entry.getValue();
            printLn += key + "\t" + value + "\n";
        }
        write(printLn);
        super.done();
    }
}
