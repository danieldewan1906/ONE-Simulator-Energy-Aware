/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package routing;

import core.Connection;
import core.DTNHost;
import core.Message;
import core.Settings;
import core.SimClock;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import report.Estimasi;

/**
 *
 * @author DANIEL
 */
public class EpidemicRouterEnergy implements RoutingDecisionEngine, Estimasi {
    
    public static int interval = 600;
    public double lastRecord = Double.MIN_VALUE;
    private ArrayList<Double> listEnergi;
    private ArrayList<Double> listKecepatan;
    private ArrayList<Double> listPercepatan;
    private Map<DTNHost, ArrayList<Double>> energiNode;
    private Map<DTNHost, ArrayList<Double>> kecepatanNode;
    private Map<DTNHost, ArrayList<Double>> percepatanNode;

    public EpidemicRouterEnergy(Settings s) {
    }

    protected EpidemicRouterEnergy(EpidemicRouterEnergy prototype) {
        energiNode = new HashMap<>();
        listEnergi = new ArrayList<>();
        listKecepatan = new ArrayList<>();
        listPercepatan = new ArrayList<>();
        kecepatanNode = new HashMap<>();
        percepatanNode = new HashMap<>();
    }

    @Override
    public void connectionUp(DTNHost thisHost, DTNHost peer) {
    }

    @Override
    public void connectionDown(DTNHost thisHost, DTNHost peer) {
    }

    @Override
    public void doExchangeForNewConnection(Connection con, DTNHost peer) {
    }

    @Override
    public boolean newMessage(Message m) {
        return true;
    }

    @Override
    public boolean isFinalDest(Message m, DTNHost aHost) {
        return m.getTo() == aHost;
    }

    @Override
    public boolean shouldSaveReceivedMessage(Message m, DTNHost thisHost) {
        Collection<Message> messageCollection = thisHost.getMessageCollection();
        for (Message message : messageCollection) {
            if (m.getId().equals(message.getId())) {
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean shouldSendMessageToHost(Message m, DTNHost otherHost) {
        return true;
    }

    @Override
    public boolean shouldDeleteSentMessage(Message m, DTNHost otherHost) {
        return false;
    }

    @Override
    public boolean shouldDeleteOldMessage(Message m, DTNHost hostReportingOld) {
        return true;
    }
    
    private Double getEnergy(DTNHost h) { //ambil energi saat ini untuk node h
        return (Double) h.getComBus().getProperty(routing.util.EnergyModel.ENERGY_VALUE_ID);
    }
    
    private Double getInitialEnergy(DTNHost h) { //ambil initial energy (energy awal)
        return (Double) h.getComBus().getProperty(routing.util.EnergyModel.INIT_ENERGY_S);
    }

    @Override
    public RoutingDecisionEngine replicate() {
        return new routing.EpidemicRouterEnergy(this);
    }

    private EpidemicRouterEnergy getOtherDecisionEngine(DTNHost h) {
        MessageRouter otherRouter = h.getRouter();
        assert otherRouter instanceof DecisionEngineRouter : "This router only works "
                + " with other routers of same type";

        return (EpidemicRouterEnergy) ((DecisionEngineRouter) otherRouter).getDecisionEngine();
    }

    @Override
    public void update(DTNHost host) {
        //ambil data setiap interval waktu yang diinginkan
        double simTime = SimClock.getTime();
        if (simTime - lastRecord >= interval) {
            listEnergi.add(this.getEnergy(host));
            energiNode.put(host, listEnergi);
            this.lastRecord = simTime - simTime % interval;
        }
    }

    @Override
    public ArrayList<Double> getKecepatan(DTNHost h) {
        return this.listKecepatan;
    }

    @Override
    public ArrayList<Double> getPercepatan(DTNHost h) {
        return this.listPercepatan;
    }

    @Override
    public Map<DTNHost, ArrayList<Double>> getEnergi() {
        return this.energiNode;
    }

    @Override
    public Map<DTNHost, ArrayList<Double>> getNilaiKecepatan() {
        return this.kecepatanNode;
    }

    @Override
    public Map<DTNHost, ArrayList<Double>> getNilaiPercepatan() {
        return this.percepatanNode;
    }
    
}
