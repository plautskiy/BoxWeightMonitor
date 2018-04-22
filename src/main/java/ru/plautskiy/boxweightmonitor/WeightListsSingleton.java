/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ru.plautskiy.boxweightmonitor;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 *
 * @author plautskii
 */
public enum WeightListsSingleton {
    INSTANCE;
    
    private List<WeightEvent> wEventsCommonLog = new ArrayList<>();
    private List<WeightEvent> wEventsArchCommonLog = new ArrayList<>();
    private static final int NUM_BOXES = 4;
    private double[] LastWeight={0.0,0.0};
    private double[] LastAvrWeight={0.0,0.0};
    
    private double[] ArchLastAvrWeight={0.0,0.0};
          
    private WeightListsSingleton(){
    }
    
    public List<WeightEvent> getwEventsCommonLog() {
        return wEventsCommonLog;
    }

    public void clearwEventsCommonLog() {
        this.wEventsCommonLog.clear();
    }
    
    public void addEventCommonLog(WeightEvent wEvent) {
        this.wEventsCommonLog.add(wEvent);
    }

    public List<WeightEvent> getwEventsArchCommonLog() {
        return wEventsArchCommonLog;
    }

    public void clearwEventsArchCommonLog() {
        this.wEventsArchCommonLog.clear();
    }
    
    public void addEventArchCommonLog(WeightEvent wEvent) {
        this.wEventsArchCommonLog.add(wEvent);
    }

    public double[] getLastAvrWeight() {
        return LastAvrWeight;
    }

    public double[] getArchLastAvrWeight() {
        return ArchLastAvrWeight;
    }

    public double[] getLastWeight() {
        return LastWeight;
    }
    
    
  
    public void calculate(){
        if(!wEventsCommonLog.isEmpty()){
            List<WeightEvent> lastA1Events;
            List<WeightEvent> lastA2Events;
            List<WeightEvent> listA1Events = wEventsCommonLog.stream().filter(t->"A1".equals(t.getBoxType())).collect(Collectors.toList());
            List<WeightEvent> listA2Events = wEventsCommonLog.stream().filter(t->"A2".equals(t.getBoxType())).collect(Collectors.toList());
            int countA1=listA1Events.size();
            int countA2=listA2Events.size();
            
            if(countA1<NUM_BOXES && countA1>0){
                lastA1Events = new ArrayList<>(listA1Events);
                LastAvrWeight[0] = getAverage(lastA1Events);
                LastWeight[0] = lastA1Events.stream().reduce((first, second) -> second).get().getWeight();
            }else if(countA1==0){
                LastAvrWeight[0]=0.0;
                LastWeight[0]=0.0;
            }else{
                lastA1Events = listA1Events.stream().skip(countA1-4).collect(Collectors.toList());
                LastAvrWeight[0] = getAverage(lastA1Events);
                LastWeight[0] = lastA1Events.stream().reduce((first, second) -> second).get().getWeight();
            }
            if(countA2<NUM_BOXES && countA2>0){
                lastA2Events = new ArrayList<>(listA2Events);
                LastAvrWeight[1] = getAverage(lastA2Events);
                LastWeight[1] = lastA2Events.stream().reduce((first, second) -> second).get().getWeight();
            }else if(countA2==0){
                LastAvrWeight[1]=0.0;
                LastWeight[1]=0.0;
            }else{
                lastA2Events = listA2Events.stream().skip(countA2-4).collect(Collectors.toList());
                LastAvrWeight[1] = getAverage(lastA2Events);
                LastWeight[1] = lastA2Events.stream().reduce((first, second) -> second).get().getWeight();
            }
        }
    }
    
    private double getAverage(List<WeightEvent> list){
        double result=0.0;
        int k = list.size();
        result = (list.stream().mapToDouble(f->f.getWeight()).sum())/k;
        return result;
    }
}
