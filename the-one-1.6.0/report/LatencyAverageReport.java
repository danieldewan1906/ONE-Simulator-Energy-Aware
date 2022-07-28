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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author DANIEL
 */
public class LatencyAverageReport extends Report implements MessageListener, UpdateListener {
    
    private Map<Double, String> latencies;
    private Map<String, Double> waktuPembuatanPesan;
    private List<Double> lat;
    public int interval = 600;
    public double lastRecord = Double.MIN_VALUE;

    public LatencyAverageReport() {
        super();
        latencies = new HashMap<>();
        waktuPembuatanPesan = new HashMap<>();
        lat = new ArrayList<Double>();
    }

    @Override
    public void newMessage(Message m) {
        this.waktuPembuatanPesan.put(m.getId(), getSimTime());
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
    public void messageTransferred(Message m, DTNHost from, DTNHost to, boolean firstDelivery) {
        if (firstDelivery) {
            double latenciesValue = getSimTime() - this.waktuPembuatanPesan.get(m.getId());
            this.lat.add(latenciesValue);
        }
    }

    @Override
    public void updated(List<DTNHost> hosts) {
        double simTime = SimClock.getTime();
        if (simTime - lastRecord >= interval) {
            this.lastRecord = simTime - simTime % interval;
            latencies.put(lastRecord, getAverage(this.lat));
        }
    }
    
    @Override
    public void done() {
        String printLn = "Time\tLatencyAverage\n";
        for (Map.Entry<Double, String> entry : latencies.entrySet()) {
            Double key = entry.getKey();
            String value = entry.getValue();
            printLn += key + "\t" + value + "\n";
        }
        write(printLn);
        super.done();
    }
    
}
