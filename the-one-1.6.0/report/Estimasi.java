/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package report;

import core.DTNHost;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Map;

/**
 *
 * @author DANIEL
 */
public interface Estimasi {
    
    public ArrayList<Double> getKecepatan(DTNHost h);
    public ArrayList<Double> getPercepatan(DTNHost h);
    public Map<DTNHost, ArrayList<Double>> getEnergi();
    public Map<DTNHost, ArrayList<Double>> getNilaiKecepatan();
    public Map<DTNHost, ArrayList<Double>> getNilaiPercepatan();
}
