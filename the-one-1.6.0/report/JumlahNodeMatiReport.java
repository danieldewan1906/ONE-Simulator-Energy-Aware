/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package report;

import core.DTNHost;
import core.SimClock;
import core.UpdateListener;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author DANIEL
 */
public class JumlahNodeMatiReport extends Report implements UpdateListener{

    public static int interval = 600;
    public Double lastRecord = Double.MIN_VALUE;
    public Map<Double, Integer> mati;

    public JumlahNodeMatiReport() {
        super();
        mati = new HashMap<Double, Integer>();
    }


    @Override
    public void updated(List<DTNHost> hosts) {
        double simTime = SimClock.getTime();
        
        if (simTime - lastRecord >= interval) {
            int va = 0;
            for (DTNHost h : hosts) {
                if (getEnergy(h) <= 0) {
                    va++;
                }
            }
            lastRecord = simTime - simTime % interval;
            mati.put(lastRecord, va);
        }
    }
    
    @Override
    public void done() {
        for (Map.Entry<Double, Integer> entry : mati.entrySet()) {
            Double key = entry.getKey();
            Integer value = entry.getValue();
            write(key + " \t" + value);
        }
        super.done();
    }
    
    private Double getEnergy(DTNHost h){
        return (Double) h.getComBus().getProperty(routing.util.EnergyModel.ENERGY_VALUE_ID);
    }
    
}
