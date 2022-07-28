/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package report;

import core.DTNHost;
import core.SimClock;
import core.SimScenario;
import core.UpdateListener;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import routing.DecisionEngineRouter;
import routing.MessageRouter;
import routing.RoutingDecisionEngine;

/**
 *
 * @author DANIEL
 */
public class Energy extends Report implements UpdateListener {
    
    public static int interval = 600;
    public Double lastRecord = Double.MIN_VALUE;

    @Override
    public void updated(List<DTNHost> hosts) {
    }
    
    @Override
    public void done() {
        List<DTNHost> nodes = SimScenario.getInstance().getHosts();
        double simTime = SimClock.getTime();
//        if (simTime - lastRecord >= interval) {
            for (DTNHost host : nodes) {
                MessageRouter r = host.getRouter();
                if (!(r instanceof DecisionEngineRouter)) {
                    continue;
                }
                RoutingDecisionEngine de = ((DecisionEngineRouter) r).getDecisionEngine();
                if (!(de instanceof Estimasi)) {
                    continue;
                }
                Estimasi fe = (Estimasi) de;

                Map<DTNHost, ArrayList<Double>> getEnergi = fe.getEnergi();
                for (Map.Entry<DTNHost, ArrayList<Double>> entry : getEnergi.entrySet()) {
//                write(printLn);
//                    if (host.getAddress() == 0) {
                    write("Node " + entry.getKey() + "\t" + entry.getValue());
//                    }
                }
            }
//            lastRecord = simTime - simTime % interval;
//        }
        super.done();
    }
    
}
