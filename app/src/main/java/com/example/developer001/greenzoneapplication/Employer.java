package com.example.developer001.greenzoneapplication;


import java.util.ArrayList;
import java.util.List;

/**
 * Created by Developer001 on 2/26/2018.
 */

public class Employer {

    private String name;
    private int ID;
    private String state;
    private double salary;
    private String received_date;
    private int salary_month;


    public Employer() {

    }

    public Employer(String name, int ID) {
        this.name = name;
        this.ID = ID;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getID() {
        return ID;
    }

    public void setID(int ID) {
        this.ID = ID;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public double getSalary() {
        return salary;
    }

    public void setSalary(double salary) {
        this.salary = salary;
    }

    public String getReceived_date() {
        return received_date;
    }

    public void setReceived_date(String received_date) {
        this.received_date = received_date;
    }

    public int getSalary_month() {
        return salary_month;
    }

    public void setSalary_month(int salary_month) {
        this.salary_month = salary_month;
    }
}

