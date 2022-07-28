/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package report;

import core.DTNHost;
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
public class KecepatanReport extends Report implements UpdateListener{
    public static int interval = 600;
    public Double lastRecord = Double.MIN_VALUE;

    @Override
    public void done() {
        List<DTNHost> nodes = SimScenario.getInstance().getHosts();

        for (DTNHost h : nodes) {
            MessageRouter r = h.getRouter();
            if (!(r instanceof DecisionEngineRouter)) {
                continue;
            }
            RoutingDecisionEngine de = ((DecisionEngineRouter) r).getDecisionEngine();
            if (!(de instanceof Estimasi)) {
                continue;
            }
            Estimasi fe = (Estimasi) de;

            Map<DTNHost, ArrayList<Double>> getT1 = fe.getNilaiKecepatan();
            for (Map.Entry<DTNHost, ArrayList<Double>> entry : getT1.entrySet()) {
//                    if (host.getAddress() == 0) {
                write("Node " + entry.getKey() + "\t" + entry.getValue());
//                    }
            }
        }
        super.done();
    }

    @Override
    public void updated(List<DTNHost> hosts) {
//        double simTime = SimClock.getTime();
//        if (simTime - lastRecord >= interval) {
//
//            for (DTNHost host : hosts) {
//                MessageRouter r = host.getRouter();
//                if (!(r instanceof DecisionEngineRouter)) {
//                    continue;
//                }
//                RoutingDecisionEngine de = ((DecisionEngineRouter) r).getDecisionEngine();
//                if (!(de instanceof estimasi)) {
//                    continue;
//                }
//                estimasi fe = (estimasi) de;
//
//                Map<DTNHost, ArrayList<Double>> getT1 = fe.getT1();
//                for (Map.Entry<DTNHost, ArrayList<Double>> entry : getT1.entrySet()) {
////                    if (host.getAddress() == 0) {
//                        write("Node " + entry.getKey() + "\t" + entry.getValue());
////                    }
//                }
//            }
//            lastRecord = simTime - simTime % interval;
//        }
    }
}
