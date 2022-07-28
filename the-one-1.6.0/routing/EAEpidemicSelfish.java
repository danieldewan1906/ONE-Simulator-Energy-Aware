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
public class EAEpidemicSelfish implements RoutingDecisionEngine, Estimasi {

    public static int interval = 600;
    public double lastRecord = Double.MIN_VALUE;
    DTNHost thisNode;
    DTNHost otherHost;
    int thresshold = 80000;
    Double thressholdKec;
    Double thressholdPerc;
    Double v;
    Double a;
    Double normKec;
    Double normPerc;
    Double hasil = 0.0;
    Double hasilPerc = 0.0;
    Double hasilVar = 0.0;
    Double hasilVarPerc = 0.0;
    Double cariT;
    ArrayList<Double> Listenergi;
    ArrayList<Double> Listkecepatan;
    ArrayList<Double> Listpercepatan;
    Map<DTNHost, ArrayList<Double>> energiNode;
    Map<DTNHost, ArrayList<Double>> kecepatanNode;
    Map<DTNHost, ArrayList<Double>> percepatanNode;
    protected Map<DTNHost, Double> startTimestamps;
    protected Map<DTNHost, List<Duration>> connHistory;

    public EAEpidemicSelfish(Settings s) {
    }

    protected EAEpidemicSelfish(EAEpidemicSelfish proto) {
        energiNode = new HashMap<>();
        this.thisNode = proto.thisNode;
        this.otherHost = proto.otherHost;
        Listenergi = new ArrayList<>();
        Listkecepatan = new ArrayList<>();
        Listpercepatan = new ArrayList<>();
        kecepatanNode = new HashMap<>();
        percepatanNode = new HashMap<>();
        startTimestamps = new HashMap<DTNHost, Double>();
        connHistory = new HashMap<DTNHost, List<Duration>>();
    }

    @Override
    public void connectionUp(DTNHost thisHost, DTNHost peer) {
        this.thisNode = thisHost;
        this.otherHost = peer;
        // Find or create the connection history list
//        double getLastDisconnect = 0;
//        if (startTimestamps.containsKey(peer)) {
//            getLastDisconnect = startTimestamps.get(peer);
//        }
//        double currentTime = SimClock.getTime();
//
//        List<Duration> history;
//        if (!connHistory.containsKey(peer)) {
//            history = new LinkedList<Duration>();
////            connHistory.put(peer, history);
//
//        } else {
//            history = connHistory.get(peer);
//
//        }
//
////         add this connection to the list
//        if (currentTime - getLastDisconnect > 0) {
//            history.add(new Duration(getLastDisconnect, currentTime));
//
//        }
//        connHistory.put(peer, history);
//        this.startTimestamps.remove(peer);
    }

    @Override
    public void connectionDown(DTNHost thisHost, DTNHost peer) {
//        this.startTimestamps.put(peer, SimClock.getTime());
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
        if (getEnergy(thisHost) < (getInitialEnergy(thisHost) * 50 / 100)) {
            cariT = hitT(thisHost);
            if (cariT < thresshold) {
                return false;
            } else {
                return true;
            }
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
                cariT = hitT(thisNode);
                if (cariT < thresshold) {
                    if (m.getFrom() == thisNode && m.getTo() == otherHost) {
                        return true;//kirim pesan
                    }
                } else {
                    //cek apakah peer adalah destinasi pesan atau jumlah copy pesan lebih dari satu
                    if (m.getTo() == otherHost) {
                        return true;
                    }
                }
                return false;
            }
            return false;
        }
        return true;
        //pesan diteruskan jika energi node masih diatas 50%
    }

    @Override
    public boolean shouldDeleteSentMessage(Message m, DTNHost otherHost
    ) {
        return false;
    }

    @Override
    public boolean shouldDeleteOldMessage(Message m, DTNHost hostReportingOld
    ) {
        return true;
    }

    @Override
    public RoutingDecisionEngine replicate() {
        return new routing.EAEpidemicSelfish(this);
    }

    public double getLastRecord() {
        return lastRecord;
    }

    public ArrayList<Double> getListenergi(DTNHost h) {
        return Listenergi;
    }
    
    private Double getEnergy(DTNHost h) {
        return (Double) h.getComBus().getProperty(routing.util.EnergyModel.ENERGY_VALUE_ID);
    }

    private Double getInitialEnergy(DTNHost h) {
        return (Double) h.getComBus().getProperty(routing.util.EnergyModel.INIT_ENERGY_S);
    }

    private EAEpidemicRouter getOtherDecicionEngine(DTNHost h) {
        MessageRouter otherRouter = h.getRouter();
        assert otherRouter instanceof DecisionEngineRouter : "This router only works "
                + " with other routers of same type";

        return (EAEpidemicRouter) ((DecisionEngineRouter) otherRouter).getDecisionEngine();
    }

    @Override
    public void update(DTNHost host) {
        //ambil data setiap interval waktu yang diinginkan
        double simTime = SimClock.getTime();
        if (simTime - lastRecord >= interval) {
            Listkecepatan.add(this.hitungKecepatan(host));
            Listpercepatan.add(this.hitungPercepatan(host));
            Listenergi.add(this.getEnergy(host));
            energiNode.put(host, Listenergi);
            kecepatanNode.put(host, Listkecepatan);
            percepatanNode.put(host, Listpercepatan);
            this.lastRecord = simTime - simTime % interval;
        }
    }

    public Double hitungKecepatan(DTNHost h) {
        double temp = 0.0;
        Double x1, x2, t1, t2, v;
        ActiveRouter act = (ActiveRouter) h.getRouter();
        DecisionEngineRouter deAct = (DecisionEngineRouter) act;
        EAEpidemicSelfish epDeAct = (EAEpidemicSelfish) deAct.getDecisionEngine();
        ArrayList<Double> energi = epDeAct.getListenergi(h);
        if (energi.size() >= 0) { //jika indeks nilai energi lebih besar sama dengan 0 maka lanjut ke while
            if (energi.size() == 0) {
                x1 = getEnergy(h);
                x2 = getInitialEnergy(h);
                t1 = getLastRecord();
                t2 = SimClock.getTime();
                v = (x2 - x1) / (t2 - t1);
                return v;
            }
            while (energi.iterator().hasNext()) { //mengecek apakah nilai energi selanjut ada atau tidak
                temp = energi.get(energi.size() - 1); //mengambil nilai energi sebelumnya dari energi sekarang
                x1 = getEnergy(h);
                x2 = temp;
                t1 = getLastRecord();
                t2 = SimClock.getTime();
                v = (x2 - x1) / (t2 - t1);
                return v;
            }
        }
        return temp;
    }

    public Double hitungPercepatan(DTNHost h) {
        double temp = 0.0;
        ActiveRouter act = (ActiveRouter) h.getRouter();
        DecisionEngineRouter deAct = (DecisionEngineRouter) act;
        EAEpidemicSelfish epDeAct = (EAEpidemicSelfish) deAct.getDecisionEngine();
        ArrayList<Double> kecepatan = epDeAct.getKecepatan(h);
        if (kecepatan.size() > 1) { //jika indeks nilai kecepatan lebih besar dari 2 maka lanjut ke while (memulai dari indeks ke 3 karena indeks 0 pada kecepatan bernilai null maka tidak bisa dihitung)
            while (kecepatan.iterator().hasNext()) { //mengecek apakah nilai kecepatan selanjutnya ada atau tidak
                temp = kecepatan.get(kecepatan.size() - 1); //mengambil nilai kecepatan dengan indeks dikurangi 1 (kecepatan sekarang)
                Double v1 = temp; //mengambil nilai kecepatan dengan indeks dikurangi 2 (kecepatan sebelumnya)
                Double v2 = kecepatan.get(kecepatan.size() - 2);
                Double t1 = getLastRecord();
                Double t2 = SimClock.getTime();
                Double a = (v2 - v1) / (t2 - t1);
                return a;
            }
        }
        return temp;
    }

    public Double hitT(DTNHost h) {//menghitung nilai(waktu) prediksi
        Double Vt;
        Double Vo = getKecepatan(h).get(Listkecepatan.size() - 1); //kecepatan terbaru
//        Vo = Math.abs(Vo); //nilai kecepatan jadi mutlak(karena nilainya -)
        Double a = getPercepatan(h).get(Listpercepatan.size() - 1); //percepatan terbaru
        a = Math.abs(a);
        Double s = getEnergy(h); //sisa energi
        Vt = (Math.pow(Vo, 2) + (2 * a * s));//hitung kecepatan akhir
        cariT = ((Math.sqrt(Vt)) - Vo) / a;//hitung waktu prediksi
        return cariT;
    }
    
    @Override
    public Map<DTNHost, ArrayList<Double>> getEnergi() {
        return this.energiNode;
    }

    @Override
    public ArrayList<Double> getKecepatan(DTNHost h) {
        return this.Listkecepatan;
    }

    @Override
    public ArrayList<Double> getPercepatan(DTNHost h) {
        return this.Listpercepatan;
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
