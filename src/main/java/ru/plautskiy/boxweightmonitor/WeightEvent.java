/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ru.plautskiy.boxweightmonitor;

/**
 *
 * @author plautskii
 */
public class WeightEvent {
    static int AllEventsCounter = 1;
    static int AllArchEventsCounter = 1;
    private int counter = 1;
    private int archCounter = 1;
    private String date;
    private String time;
    private String boxType;
    private double weight;

    public WeightEvent(String date, String time, String boxtype, double weight) {
        this.date=date;
        this.time=time;
        this.boxType=boxtype;
        this.weight=weight;
        counter=AllEventsCounter;
        AllEventsCounter++;
    }
    
    public WeightEvent(String date, String time, String boxtype, double weight, boolean isArchive) {
        this.date=date;
        this.time=time;
        this.boxType=boxtype;
        this.weight=weight;
        archCounter=AllArchEventsCounter;
        AllArchEventsCounter++;
    }

    public int getCounter() {
        return counter;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getBoxType() {
        return boxType;
    }

    public void setBoxType(String boxType) {
        this.boxType = boxType;
    }

    public double getWeight() {
        return weight;
    }

    public void setWeight(double weight) {
        this.weight = weight;
    }

    @Override
    public String toString() {
        return  counter + "\t\t" + date + "\t\t\t\t\t" + time + "\t\t\t\t\t" + boxType + "\t\t\t\t\t" + weight;
    }

    public String toString(boolean isArchive) {
        return  archCounter + "\t\t" + date + "\t\t\t\t\t" + time + "\t\t\t\t\t" + boxType + "\t\t\t\t\t" + weight;
    }
    
        
}
