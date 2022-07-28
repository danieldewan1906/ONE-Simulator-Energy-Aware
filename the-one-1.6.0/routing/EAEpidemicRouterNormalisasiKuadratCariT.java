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
public class EAEpidemicRouterNormalisasiKuadratCariT implements RoutingDecisionEngine, Estimasi {

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
    ArrayList<Double> energi;
    ArrayList<Double> kecepatan;
    ArrayList<Double> percepatan;
    Map<DTNHost, ArrayList<Double>> energiNode;
    Map<DTNHost, ArrayList<Double>> kecepatanNode;
    Map<DTNHost, ArrayList<Double>> percepatanNode;
    protected Map<DTNHost, Double> startTimestamps;
    protected Map<DTNHost, List<Duration>> connHistory;

    public EAEpidemicRouterNormalisasiKuadratCariT(Settings s) {
    }

    protected EAEpidemicRouterNormalisasiKuadratCariT(EAEpidemicRouterNormalisasiKuadratCariT proto) {
        energiNode = new HashMap<>();
        this.thisNode = proto.thisNode;
        this.otherHost = proto.otherHost;
        energi = new ArrayList<>();
        kecepatan = new ArrayList<>();
        percepatan = new ArrayList<>();
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
//        Collection<Message> messageCollection = thisNode.getMessageCollection();
//        for (Message message : messageCollection) {
//            if (m.getId().equals(message.getId())) {
//                return false;
//            }
//        }
        return true;
    }

    @Override
    public boolean shouldSendMessageToHost(Message m, DTNHost otherHost) {
        Double thisHostFreeBuffer = thisNode.freeBufferSize();
        Double otherHostFreeBuffer = otherHost.freeBufferSize();
//        Collection<Message> thisHostMessage = thisNode.getMessageCollection();
        Collection<Message> otherHostMessage = otherHost.getMessageCollection();
//        List<Message> msgBundle = new ArrayList<Message>();

//        for (Message message : thisHostMessage) {
//            if (!otherHostMessage.contains(message)) {
//                msgBundle.add(message);
//            }
//        }
        if (getEnergy(thisNode) < (getInitialEnergy(thisNode) * 50 / 100)) {
            cariT = hitT(thisNode);
            if (cariT < thresshold) {
                if (!otherHostMessage.contains(m)) {
//                a = hitungKecepatan(otherHost);
//                v = hitungPercepatan(otherHost);
//                thressholdKec = normKec(otherHost);
//                thressholdPerc = normPerc(otherHost);
                    if ((hitungKecepatan(otherHost) > normKec(otherHost) && hitungPercepatan(otherHost) > normPerc(otherHost)) && (otherHostFreeBuffer > thisHostFreeBuffer)) {
                        if (otherHostFreeBuffer > m.getSize()) {
                            otherHostFreeBuffer = otherHostFreeBuffer - m.getSize();
                            // hitung sisa buffer otherHost dikurangi dengan m.getSize()
                            return true;
                            // pesan dikirim                        
                        }
                        return false;
                        //buffer otherHost lebih kecil dari m.getSize();
                    }
                    return false;
                    // pesan tidak dikirim karena kecepatan atau percepatan ataupun buffer tidak memenuhi syarat
                }
                return false;
                // pesan tidak diteruskan. menunggu pesan dari otherHost 
            }
            return false;
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
        return new routing.EAEpidemicRouterNormalisasiKuadratCariT(this);
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
            kecepatan.add(this.hitungKecepatan(host));
            percepatan.add(this.hitungPercepatan(host));
            energi.add(this.getEnergy(host));
            energiNode.put(host, energi);
            kecepatanNode.put(host, kecepatan);
            percepatanNode.put(host, percepatan);
            this.lastRecord = simTime - simTime % interval;
        }
    }

    public Double hitungKecepatan(DTNHost h) {
        double temp = 0.0;
        Double x1, x2, t1, t2;
        if (energi.size() >= 0) { //jika indeks nilai energi lebih besar sama dengan 0 maka lanjut ke while
            if (energi.size() == 0) {
                x1 = getEnergy(h);
                x2 = getInitialEnergy(h);
                t1 = lastRecord;
                t2 = SimClock.getTime();
                v = (x2 - x1) / (t2 - t1);
                return v;
            }
            while (energi.iterator().hasNext()) { //mengecek apakah nilai energi selanjut ada atau tidak
                temp = energi.get(energi.size() - 1); //mengambil nilai energi sebelumnya dari energi sekarang
                x1 = getEnergy(h);
                x2 = temp;
                t1 = lastRecord;
                t2 = SimClock.getTime();
                v = (x2 - x1) / (t2 - t1);
                return v;
            }
        }
        return temp;
    }

    public Double hitungPercepatan(DTNHost h) {
        double temp = 0.0;
        if (kecepatan.size() > 1) { //jika indeks nilai kecepatan lebih besar dari 2 maka lanjut ke while (memulai dari indeks ke 3 karena indeks 0 pada kecepatan bernilai null maka tidak bisa dihitung)
            while (kecepatan.iterator().hasNext()) { //mengecek apakah nilai kecepatan selanjutnya ada atau tidak
                temp = kecepatan.get(kecepatan.size() - 1); //mengambil nilai kecepatan dengan indeks dikurangi 1 (kecepatan sekarang)
                Double v1 = temp; //mengambil nilai kecepatan dengan indeks dikurangi 2 (kecepatan sebelumnya)
                Double v2 = kecepatan.get(kecepatan.size() - 2);
                Double t1 = lastRecord;
                Double t2 = SimClock.getTime();
                a = (v2 - v1) / (t2 - t1);
                return a;
            }
        }
        return temp;
    }

    public Double hitT(DTNHost h) {//menghitung nilai(waktu) prediksi
        Double Vt;
        Double Vo = getKecepatan(h).get(kecepatan.size() - 1); //kecepatan terbaru
//        Vo = Math.abs(Vo); //nilai kecepatan jadi mutlak(karena nilainya -)
        Double a = getPercepatan(h).get(percepatan.size() - 1); //percepatan terbaru
        a = Math.abs(a);
        Double s = getEnergy(h); //sisa energi
        Vt = (Math.pow(Vo, 2) + (2 * a * s));//hitung kecepatan akhir
        cariT = ((Math.sqrt(Vt)) - Vo) / a;//hitung waktu prediksi
        return cariT;
    }

//    private Double getMinData(DTNHost h) {
//        if (percepatan == null || percepatan.size() == 0) {
//            return Double.MAX_VALUE;
//        }
//        Collections.sort(percepatan);
//
//        return percepatan.get(0);
//    }
////
//    private Double getMaxData(DTNHost h) {
//        if (percepatan == null || percepatan.size() == 0) {
//            return Double.MIN_VALUE;
//        }
//        Collections.sort(percepatan);
//
//        return percepatan.get(percepatan.size() - 1);
//    }
//
//    private Double normalisasiD(DTNHost h) {
//        Double min = getMinData(h);
//        Double max = getMaxData(h);
//        Double range = max - min;
//        Double normalisasi = 0.0;
//        Double m01 = 0.0;
//        if (percepatan.size() > 1) {
//            while (percepatan.iterator().hasNext()) {
//                m01 = (percepatan.get(percepatan.size() - 1) - min) / range;
//                normalisasi = 2 * m01 - 1;
//                return normalisasi;
//            }
//        }
////        for (double norm : percepatan) {
////            m01 = (percepatan.get(percepatan.size() - 1) - min) / range;
////            normalisasi = 2 * m01 - 1;
////            return max;
////        }
//        return normalisasi;
//    }
//    private Double getMean(DTNHost hosts) {
//        Message m = null;
//        DTNHost dest = m.getTo();
//        double mean;
//
//        if (connHistory.containsKey(dest)) {
//            List<Duration> v = connHistory.get(dest);
//            Iterator<Duration> i = v.iterator();
//
//            int frek = 0;
//            double sum = 0;
//            double temp = 0;
//
//            if (v.size() > 1) {
//                while (i.hasNext()) {
//                    Duration d = i.next();
//                    //Double a = d.end - d.start;
//                    sum += d.start - temp;
//                    temp = d.end;
//
//                    frek++;
//                }
//            }
//            //double mean = sum / frek;
//            if (frek == 0) {
//                mean = 0;
//            } else {
//                mean = sum / (frek - 1);
//            }
//        } else {
//            return 0.0;
//        }
//        System.out.println(mean);
//        return mean;
//    }
//    public List<Duration> getList(DTNHost h) {
//        if (connHistory.containsKey(h)) {
//            return connHistory.get(h);
//        } else {
//            List<Duration> d = new LinkedList<>();
//            return d;
//        }
//    }
//
//    private Double getMean(DTNHost h) {
//        List<Duration> list = getList(h);
//        Iterator<Duration> duration = list.iterator();
//        double hasil = 0;
//        while (duration.hasNext()) {
//            Duration d = duration.next();
//            hasil += (d.end - d.start);
//        }
//        return hasil / list.size();
//    }
//
//    public Double getVariansiNode(DTNHost h) {
//        List<Duration> list = getList(h);
//        Iterator<Duration> duration = list.iterator();
//        double temp = 0;
//        double mean = getMean(h);
//        while (duration.hasNext()) {
//            Duration d = duration.next();
//            temp += Math.pow((d.end - d.start) - mean, 2);
//        }
//        return temp / list.size();
//    }
//
//    private Double normalisasiKecepatan(DTNHost h) {
//        double kecepatan = hitungKecepatan(h);
//        double variansi = getVariansiNode(h);
//        Double normKec = Math.exp(-(Math.pow(kecepatan, 2) / (2 * variansi)));
//        return normKec;
//    }
//
//    private Double normalisasiPercepatan(DTNHost h) {
//        double percepatan = hitungPercepatan(h);
//        double variansi = getVariansiNode(h);
//        normPerc = Math.exp(-(Math.pow(percepatan, 2) / (2 * variansi)));
//        return normPerc;
//    }
    public Double rataRataKec(DTNHost h) {
        Iterator<Double> kec = kecepatan.iterator();
        while (kec.hasNext()) {
            hasil = hasil + kecepatan.get(kecepatan.size() - 1);
            return hasil / kecepatan.size();
        }
        return hasil;
    }

    public Double variansKec(DTNHost h) {
        Iterator<Double> kec = kecepatan.iterator();
        Double rata = rataRataKec(h);
        while (kec.hasNext()) {
            hasilVar = hasilVar + Math.pow(kecepatan.get(kecepatan.size() - 1) - rata, 2);
            return hasilVar / kecepatan.size();
        }
        return hasilVar;
    }

    public Double normKec(DTNHost h) {
        Double ratarata = rataRataKec(h);
        Double var = variansKec(h);
        Iterator<Double> kec = kecepatan.iterator();
        while (kec.hasNext()) {
            normKec = Math.exp(-((Math.pow((kecepatan.get(kecepatan.size() - 1)) - ratarata, 2)) / (2 * (Math.pow(var, 2)))));
            return normKec;
        }
        return normKec;
    }

    public Double rataRataPerc(DTNHost h) {
        Iterator<Double> perc = percepatan.iterator();
        while (perc.hasNext()) {
            hasilPerc = hasilPerc + percepatan.get(percepatan.size() - 1);
            return hasilPerc / percepatan.size();
        }
        return hasilPerc;
    }

    public Double variansPerc(DTNHost h) {
        Iterator<Double> perc = percepatan.iterator();
        Double rata = rataRataPerc(h);
        while (perc.hasNext()) {
            hasilVarPerc = hasilVarPerc + Math.pow(percepatan.get(percepatan.size() - 1) - rata, 2);
            return hasilVarPerc / percepatan.size();
        }
        return hasilVarPerc;
    }

    public Double normPerc(DTNHost h) {
        Double ratarata = rataRataPerc(h);
        Double var = variansPerc(h);
        Iterator<Double> perc = percepatan.iterator();
        while (perc.hasNext()) {
            normPerc = Math.exp(-((Math.pow((percepatan.get(percepatan.size() - 1)) - ratarata, 2)) / (2 * (Math.pow(var, 2)))));
            return normPerc;
        }
        return normPerc;
    }

    @Override
    public Map<DTNHost, ArrayList<Double>> getEnergi() {
        return this.energiNode;
    }

    @Override
    public ArrayList<Double> getKecepatan(DTNHost h) {
        return this.kecepatan;
    }

    @Override
    public ArrayList<Double> getPercepatan(DTNHost h) {
        return this.percepatan;
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
