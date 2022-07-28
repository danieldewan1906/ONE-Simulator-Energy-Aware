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
public class EASprayAndWaitRouter implements RoutingDecisionEngine, Estimasi {

    public static final String NROF_COPIES = "nrofCopies";
    public static final String BINARY_MODE = "binaryMode";
    public static final String SPRAYANDWAIT_NS = "DecisionSprayAndWaitRouter";
    public static final String MSG_COUNT_PROPERTY = SPRAYANDWAIT_NS + "." + "copies";

    protected int initialNrofCopies;
    protected boolean isBinary;

    public static int interval = 300;
    public double lastRecord = Double.MIN_VALUE;
    DTNHost thisNode;
    DTNHost otherHost;
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
    private ArrayList<Double> Listenergi;
    private ArrayList<Double> Listkecepatan;
    private ArrayList<Double> Listpercepatan;
    Map<DTNHost, ArrayList<Double>> energiNode;
    Map<DTNHost, ArrayList<Double>> kecepatanNode;
    Map<DTNHost, ArrayList<Double>> percepatanNode;
    protected Map<DTNHost, Double> startTimestamps;
    protected Map<DTNHost, List<Duration>> connHistory;

    public EASprayAndWaitRouter(Settings s) {
        s = new Settings(SPRAYANDWAIT_NS);
        initialNrofCopies = s.getInt(NROF_COPIES);

        if (isBinary) {
            isBinary = s.getBoolean(BINARY_MODE);
        }
    }

    protected EASprayAndWaitRouter(EASprayAndWaitRouter proto) {
        this.initialNrofCopies = proto.initialNrofCopies;
        this.isBinary = proto.isBinary;
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
        m.addProperty(MSG_COUNT_PROPERTY, initialNrofCopies);
        return true;
    }

    @Override
    public boolean isFinalDest(Message m, DTNHost aHost) {
        Integer nrofCopies = (Integer) m.getProperty(MSG_COUNT_PROPERTY);

        assert nrofCopies != null : "Not a SnW message: " + m;

        if (isBinary) {
            /* in binary S'n'W the receiving node gets ceil(n/2) copies */
            nrofCopies = (int) Math.ceil(nrofCopies / 2.0);
        } else {
            /* in standard S'n'W the receiving node gets only single copy */
            nrofCopies = 1;
        }

        m.updateProperty(MSG_COUNT_PROPERTY, nrofCopies);
        return m.getTo() == aHost;
    }

    @Override
    public boolean shouldSaveReceivedMessage(Message m, DTNHost thisHost) {
        Double thisHostFreeBuffer = thisHost.freeBufferSize();
        Double otherHostFreeBuffer = otherHost.freeBufferSize();
        if (getEnergy(thisHost) < (getInitialEnergy(thisHost) * 50 / 100)) { //cek energi kita 
            if ((hitungKecepatan(thisHost) < normKec(thisHost) && hitungPercepatan(thisHost) < normPerc(thisHost)) && (thisHostFreeBuffer < otherHostFreeBuffer)) { //bandingkan nilai T dengan threshold
                return false; //jangan simpan pesan
            } else {
                return m.getTo() != thisHost;//hanya menyimpan pesan jika destinasi bukan untuk kita
            }
        } else {
            return m.getTo() != thisHost;
        }
    }

    @Override
    public boolean shouldSendMessageToHost(Message m, DTNHost otherHost) {
        Double thisHostFreeBuffer = thisNode.freeBufferSize();
        Double otherHostFreeBuffer = otherHost.freeBufferSize();
        Collection<Message> otherHostMessage = otherHost.getMessageCollection();
        Integer nrofCopies = (Integer) m.getProperty(MSG_COUNT_PROPERTY);
        
        if (getEnergy(thisNode) < (getInitialEnergy(thisNode) * 50 / 100)) {
            if ((hitungKecepatan(otherHost) > normKec(otherHost) && hitungPercepatan(otherHost) > normPerc(otherHost)) && (otherHostFreeBuffer > thisHostFreeBuffer)) {
                if (otherHostFreeBuffer > m.getSize()) {
                    if (m.getFrom() == thisNode && m.getTo() == otherHost) {
                        return true;//kirim pesan
                    }
                    otherHostFreeBuffer = otherHostFreeBuffer - m.getSize();
                }
                return false;
            } else {
                //cek apakah peer adalah destinasi pesan atau jumlah copy pesan lebih dari satu
                if (m.getTo() == otherHost || nrofCopies > 1) {
                    return true;
                }
            }
            return false;
        } else {
            if (m.getTo() == otherHost || nrofCopies > 1) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean shouldDeleteSentMessage(Message m, DTNHost otherHost) {
        Integer nrofCopies = (Integer) m.getProperty(MSG_COUNT_PROPERTY);

        if (isBinary) {
            nrofCopies /= 2;
        } else {
            nrofCopies--;
        }
        m.updateProperty(MSG_COUNT_PROPERTY, nrofCopies);

        return false;
    }

    @Override
    public boolean shouldDeleteOldMessage(Message m, DTNHost hostReportingOld) {
        return true;
    }

    @Override
    public RoutingDecisionEngine replicate() {
        return new routing.EASprayAndWaitRouter(this);
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

    private Double hitungKecepatan(DTNHost h) {
        double temp = 0.0;
        Double x1, x2, t1, t2, v;
        ActiveRouter act = (ActiveRouter) h.getRouter();
        DecisionEngineRouter deAct = (DecisionEngineRouter) act;
        EASprayAndWaitRouter epDeAct = (EASprayAndWaitRouter) deAct.getDecisionEngine();
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

    private Double hitungPercepatan(DTNHost h) {
        double temp = 0.0;
        ActiveRouter act = (ActiveRouter) h.getRouter();
        DecisionEngineRouter deAct = (DecisionEngineRouter) act;
        EASprayAndWaitRouter epDeAct = (EASprayAndWaitRouter) deAct.getDecisionEngine();
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

    private Double rataRataKec(DTNHost h) {
        ActiveRouter act = (ActiveRouter) h.getRouter();
        DecisionEngineRouter deAct = (DecisionEngineRouter) act;
        EASprayAndWaitRouter epDeAct = (EASprayAndWaitRouter) deAct.getDecisionEngine();
        ArrayList<Double> kecepatan = epDeAct.getKecepatan(h);
        Iterator<Double> kec = kecepatan.iterator();
        hasil = hasil + kecepatan.get(kecepatan.size() - 1);
        return hasil / kecepatan.size();
    }

    private Double variansKec(DTNHost h) {
        ActiveRouter act = (ActiveRouter) h.getRouter();
        DecisionEngineRouter deAct = (DecisionEngineRouter) act;
        EASprayAndWaitRouter epDeAct = (EASprayAndWaitRouter) deAct.getDecisionEngine();
        ArrayList<Double> kecepatan = epDeAct.getKecepatan(h);
        Iterator<Double> kec = kecepatan.iterator();
        double rata = rataRataKec(h);
        hasilVar = hasilVar + Math.pow(kecepatan.get(kecepatan.size() - 1) - rata, 2);
        return hasilVar / kecepatan.size();
    }

    private Double normKec(DTNHost h) {
        ActiveRouter act = (ActiveRouter) h.getRouter();
        DecisionEngineRouter deAct = (DecisionEngineRouter) act;
        EASprayAndWaitRouter epDeAct = (EASprayAndWaitRouter) deAct.getDecisionEngine();
        ArrayList<Double> kecepatan = epDeAct.getKecepatan(h);
        double normKec = 0;
        double ratarata = rataRataKec(h);
        double var = variansKec(h);
        normKec = Math.exp(-((Math.pow((kecepatan.get(kecepatan.size() - 1)) - ratarata, 2)) / (2 * (Math.pow(var, 2)))));
//        while (kec.hasNext()) {
//            normKec = Math.exp(-((Math.pow((kecepatan.get(kecepatan.size() - 1)) - ratarata, 2)) / (2 * (Math.pow(var, 2)))));
////            return normKec;
//        }
        return normKec;
    }

    private Double rataRataPerc(DTNHost h) {
        ActiveRouter act = (ActiveRouter) h.getRouter();
        DecisionEngineRouter deAct = (DecisionEngineRouter) act;
        EASprayAndWaitRouter epDeAct = (EASprayAndWaitRouter) deAct.getDecisionEngine();
        ArrayList<Double> percepatan = epDeAct.getPercepatan(h);
        Iterator<Double> perc = percepatan.iterator();
        hasilPerc = hasilPerc + percepatan.get(percepatan.size() - 1);
        return hasilPerc / percepatan.size();
    }

    private Double variansPerc(DTNHost h) {
        ActiveRouter act = (ActiveRouter) h.getRouter();
        DecisionEngineRouter deAct = (DecisionEngineRouter) act;
        EASprayAndWaitRouter epDeAct = (EASprayAndWaitRouter) deAct.getDecisionEngine();
        ArrayList<Double> percepatan = epDeAct.getPercepatan(h);
        Iterator<Double> perc = percepatan.iterator();
        double rata = rataRataPerc(h);
        hasilVarPerc = hasilVarPerc + Math.pow(percepatan.get(percepatan.size() - 1) - rata, 2);
        return hasilVarPerc / percepatan.size();

    }

    private Double normPerc(DTNHost h) {
        ActiveRouter act = (ActiveRouter) h.getRouter();
        DecisionEngineRouter deAct = (DecisionEngineRouter) act;
        EASprayAndWaitRouter epDeAct = (EASprayAndWaitRouter) deAct.getDecisionEngine();
        ArrayList<Double> percepatan = epDeAct.getPercepatan(h);
        double normPerc = 0;
        double ratarata = rataRataPerc(h);
        double var = variansPerc(h);
        normPerc = Math.exp(-((Math.pow((percepatan.get(percepatan.size() - 1)) - ratarata, 2)) / (2 * (Math.pow(var, 2)))));
//        while (perc.hasNext()) {
//            normPerc = Math.exp(-((Math.pow((percepatan.get(percepatan.size() - 1)) - ratarata, 2)) / (2 * (Math.pow(var, 2)))));
////            return normPerc;
//        }
        return normPerc;
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
