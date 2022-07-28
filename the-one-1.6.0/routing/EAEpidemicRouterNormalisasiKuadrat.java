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
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import report.Estimasi;

/**
 *
 * @author DANIEL
 */
public class EAEpidemicRouterNormalisasiKuadrat implements RoutingDecisionEngine, Estimasi {

    public static int interval = 600;
    public double lastRecord = Double.MIN_VALUE;
    DTNHost thisNode;
    DTNHost otherHost;
    Double hasil = 0.0;
    Double hasilPerc = 0.0;
    Double hasilVar = 0.0;
    Double hasilVarPerc = 0.0;
    private ArrayList<Double> listEnergi;
    private ArrayList<Double> listKecepatan;
    private ArrayList<Double> listPercepatan;
    private Map<DTNHost, ArrayList<Double>> energiNode;
    private Map<DTNHost, ArrayList<Double>> kecepatanNode;
    private Map<DTNHost, ArrayList<Double>> percepatanNode;

    public EAEpidemicRouterNormalisasiKuadrat(Settings s) {
    }

    protected EAEpidemicRouterNormalisasiKuadrat(EAEpidemicRouterNormalisasiKuadrat proto) {
        energiNode = new HashMap<>();
        this.thisNode = proto.thisNode;
        this.otherHost = proto.otherHost;
        listEnergi = new ArrayList<>();
        listKecepatan = new ArrayList<>();
        listPercepatan = new ArrayList<>();
        kecepatanNode = new HashMap<>();
        percepatanNode = new HashMap<>();
    }

    @Override
    public void connectionUp(DTNHost thisHost, DTNHost peer) {
        this.thisNode = thisHost;
        this.otherHost = peer;
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
        Double thisHostFreeBuffer = thisHost.freeBufferSize();
        Double otherHostFreeBuffer = otherHost.freeBufferSize();
        if (getEnergy(thisHost) < (getInitialEnergy(thisHost) * 50 / 100)) {
            if ((hitungKecepatan(thisHost) > normalisasiKecepatan(thisHost) && hitungPercepatan(thisHost) > normalisasiPercepatan(thisHost)) 
                    || (thisHostFreeBuffer > otherHostFreeBuffer)) {
                if (thisHostFreeBuffer > m.getSize()) {
                    return true;
                }
            }
            return false;
        }
        return true;
    }

    @Override
    public boolean shouldSendMessageToHost(Message m, DTNHost otherHost) {
        Double thisHostFreeBuffer = thisNode.freeBufferSize();
        Double otherHostFreeBuffer = otherHost.freeBufferSize();
        Collection<Message> otherHostMessage = otherHost.getMessageCollection();
        
        if (getEnergy(thisNode) < (getInitialEnergy(thisNode) * 50 / 100)) {
            if (!otherHostMessage.contains(m)) {
                if ((hitungKecepatan(otherHost) > normalisasiKecepatan(otherHost) && hitungPercepatan(otherHost) > normalisasiPercepatan(otherHost)) 
                        || (otherHostFreeBuffer > thisHostFreeBuffer)) {
                    if (otherHostFreeBuffer > m.getSize()) {
                        return true;
                        // pesan dikirim                        
                    }
                } else {
                    return false;
                    // pesan tidak dikirim karena kecepatan atau percepatan ataupun buffer tidak memenuhi syarat
                }
            }
        }
        return true;
        //pesan diteruskan jika energi node masih diatas 50%
    }

    @Override
    public boolean shouldDeleteSentMessage(Message m, DTNHost otherHost) {
        return false;
    }

    @Override
    public boolean shouldDeleteOldMessage(Message m, DTNHost hostReportingOld) {
        return true;
    }

    @Override
    public RoutingDecisionEngine replicate() {
        return new routing.EAEpidemicRouterNormalisasiKuadrat(this);
    }

    public Double getHasil(DTNHost h) {
        return hasil / getKecepatan(h).size();
    }

    public Double getHasilPerc(DTNHost h) {
        return hasilPerc / getPercepatan(h).size();
    }

    private double getLastRecord() {
        return this.lastRecord;
    }

    private ArrayList<Double> getListEnergi(DTNHost h) { //ambil list energi yang disimpan
        return listEnergi;
    }

    private Double getEnergy(DTNHost h) { //ambil energi saat ini untuk node h
        return (Double) h.getComBus().getProperty(routing.util.EnergyModel.ENERGY_VALUE_ID);
    }

    private Double getInitialEnergy(DTNHost h) { //ambil initial energy (energy awal)
        return (Double) h.getComBus().getProperty(routing.util.EnergyModel.INIT_ENERGY_S);
    }
    
//    private Double getTransmitEnergy(DTNHost h) { //ambil initial energy (energy awal)
//        return (Double) h.getComBus().getProperty(routing.util.EnergyModel.TRANSMIT_ENERGY_S);
//    }

    private EAEpidemicRouterNormalisasiKuadrat getOtherDecicionEngine(DTNHost h) {
        MessageRouter otherRouter = h.getRouter();
        assert otherRouter instanceof DecisionEngineRouter : "This router only works "
                + " with other routers of same type";

        return (EAEpidemicRouterNormalisasiKuadrat) ((DecisionEngineRouter) otherRouter).getDecisionEngine();
    }

    @Override
    public void update(DTNHost host) {
        //ambil data setiap interval waktu yang diinginkan
        double simTime = SimClock.getTime();
        if (simTime - lastRecord >= interval) {
            listKecepatan.add(this.hitungKecepatan(host));
            listPercepatan.add(this.hitungPercepatan(host));
            listEnergi.add(this.getEnergy(host));
            energiNode.put(host, listEnergi);
            kecepatanNode.put(host, listKecepatan);
            percepatanNode.put(host, listPercepatan);
            this.lastRecord = simTime - simTime % interval;
        }
    }

    private Double hitungKecepatan(DTNHost h) {
        Double temp = 0.0;
        Double x1, x2, t1, t2, v;
        ActiveRouter act = (ActiveRouter) h.getRouter();
        DecisionEngineRouter deAct = (DecisionEngineRouter) act;
        EAEpidemicRouterNormalisasiKuadrat epDeAct = (EAEpidemicRouterNormalisasiKuadrat) deAct.getDecisionEngine();
        ArrayList<Double> energi = epDeAct.getListEnergi(h);
        if (energi.size() >= 0) { //jika indeks nilai energi lebih besar sama dengan 0 maka lanjut ke while
            if (energi.size() == 0) {
                x1 = getInitialEnergy(h); //energi awal
                x2 = getEnergy(h); //energi sekarang
                t1 = getLastRecord(); //waktu awal
                t2 = SimClock.getTime(); //waktu sekarang
                v = (x2 - x1) / (t2 - t1);
                v = Math.abs(v); //karena hasil kecepatan minus, maka harga mutlak untuk menjadi plus
                return v;
            }
            while (energi.iterator().hasNext()) { //mengecek apakah nilai energi selanjut ada atau tidak
                temp = energi.get(energi.size() - 1); //mengambil nilai energi sebelumnya dari energi sekarang
                x1 = temp;
                x2 = getEnergy(h); //energi sekarang
                t1 = getLastRecord(); //waktu sebelumnya
                t2 = SimClock.getTime(); //waktu sekarang
                v = (x2 - x1) / (t2 - t1);
                v = Math.abs(v);
                return v;
            }
        }
        return temp;
    }

    private Double hitungPercepatan(DTNHost h) {
        Double temp = 0.0;
        Double v1, v2, t1, t2, a;
        ActiveRouter act = (ActiveRouter) h.getRouter();
        DecisionEngineRouter deAct = (DecisionEngineRouter) act;
        EAEpidemicRouterNormalisasiKuadrat epDeAct = (EAEpidemicRouterNormalisasiKuadrat) deAct.getDecisionEngine();
        ArrayList<Double> kecepatan = epDeAct.getKecepatan(h);
        if (kecepatan.size() > 1) { //jika indeks nilai kecepatan lebih besar dari 1 maka lanjut ke while (memulai dari indeks ke 2 karena indeks 0 pada kecepatan bernilai null maka tidak bisa dihitung)
            while (kecepatan.iterator().hasNext()) { //mengecek apakah nilai kecepatan selanjutnya ada atau tidak
                temp = kecepatan.get(kecepatan.size() - 1); //mengambil nilai kecepatan dengan indeks dikurangi 1 (kecepatan sekarang)
                v1 = kecepatan.get(kecepatan.size() - 2); //mengambil nilai kecepatan dengan indeks dikurangi 2 (kecepatan sebelumnya) 
                v2 = temp;
                t1 = getLastRecord(); //waktu sebelumnya
                t2 = SimClock.getTime(); //waktu sekarang
                a = (v2 - v1) / (t2 - t1);
                return a;
            }
        }
        return temp;
    }

    private Double rataRataKecepatan(DTNHost h) {
        ActiveRouter act = (ActiveRouter) h.getRouter();
        DecisionEngineRouter deAct = (DecisionEngineRouter) act;
        EAEpidemicRouterNormalisasiKuadrat epDeAct = (EAEpidemicRouterNormalisasiKuadrat) deAct.getDecisionEngine();
        ArrayList<Double> kecepatan = epDeAct.getKecepatan(h);
        hasil = hasil + kecepatan.get(kecepatan.size() - 1);
        return hasil / kecepatan.size();
    }

    private Double variansKecepatan(DTNHost h) {
        ActiveRouter act = (ActiveRouter) h.getRouter();
        DecisionEngineRouter deAct = (DecisionEngineRouter) act;
        EAEpidemicRouterNormalisasiKuadrat epDeAct = (EAEpidemicRouterNormalisasiKuadrat) deAct.getDecisionEngine();
        ArrayList<Double> kecepatan = epDeAct.getKecepatan(h);
        Double rata = getHasil(h);
        hasilVar = hasilVar + Math.pow(kecepatan.get(kecepatan.size() - 1) - rata, 2);
        return hasilVar / kecepatan.size();
    }

    private Double normalisasiKecepatan(DTNHost h) {
        ActiveRouter act = (ActiveRouter) h.getRouter();
        DecisionEngineRouter deAct = (DecisionEngineRouter) act;
        EAEpidemicRouterNormalisasiKuadrat epDeAct = (EAEpidemicRouterNormalisasiKuadrat) deAct.getDecisionEngine();
        ArrayList<Double> kecepatan = epDeAct.getKecepatan(h);
        Double ratarata = rataRataKecepatan(h);
        Double var = variansKecepatan(h);
        Double normKec;
        normKec = Math.exp(-(Math.pow(kecepatan.get(kecepatan.size() - 1) - ratarata, 2)) / (2 * Math.pow(var, 2)));
        return normKec;
    }

    private Double rataRataPercepatan(DTNHost h) {
        ActiveRouter act = (ActiveRouter) h.getRouter();
        DecisionEngineRouter deAct = (DecisionEngineRouter) act;
        EAEpidemicRouterNormalisasiKuadrat epDeAct = (EAEpidemicRouterNormalisasiKuadrat) deAct.getDecisionEngine();
        ArrayList<Double> percepatan = epDeAct.getPercepatan(h);
        hasilPerc = hasilPerc + percepatan.get(percepatan.size() - 1);
        return hasilPerc / percepatan.size();
    }

    private Double variansPercepatan(DTNHost h) {
        ActiveRouter act = (ActiveRouter) h.getRouter();
        DecisionEngineRouter deAct = (DecisionEngineRouter) act;
        EAEpidemicRouterNormalisasiKuadrat epDeAct = (EAEpidemicRouterNormalisasiKuadrat) deAct.getDecisionEngine();
        ArrayList<Double> percepatan = epDeAct.getPercepatan(h);
        Double rata = getHasilPerc(h);
        hasilVarPerc = hasilVarPerc + Math.pow(percepatan.get(percepatan.size() - 1) - rata, 2);
        return hasilVarPerc / percepatan.size();
    }

    private Double normalisasiPercepatan(DTNHost h) {
        ActiveRouter act = (ActiveRouter) h.getRouter();
        DecisionEngineRouter deAct = (DecisionEngineRouter) act;
        EAEpidemicRouterNormalisasiKuadrat epDeAct = (EAEpidemicRouterNormalisasiKuadrat) deAct.getDecisionEngine();
        ArrayList<Double> percepatan = epDeAct.getPercepatan(h);
        Double rataRataPerc = rataRataPercepatan(h);
        Double var = variansPercepatan(h);
        Double normPerc = 0.0;
        normPerc = Math.exp(-(Math.pow(percepatan.get(percepatan.size() - 1) - rataRataPerc, 2)) / (2 * Math.pow(var, 2)));
        return normPerc;
    }

    @Override
    public Map<DTNHost, ArrayList<Double>> getEnergi() {
        return this.energiNode;
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
    public Map<DTNHost, ArrayList<Double>> getNilaiKecepatan() {
        return this.kecepatanNode;
    }

    @Override
    public Map<DTNHost, ArrayList<Double>> getNilaiPercepatan() {
        return this.percepatanNode;
    }
}
