package com.example.barberbookingapp.models;

public class Appointments {
    private String serviceType;
    private String clientId;
    private String status;
    private String dateTime;

    public Appointments() {}


    /*
    מחלקה הנועדה לייצוג של תור בפיירבייס
    השדות הרלוונטים בה הם: המזהה של הלקוח, סוג השירות, התאריך והשעה
    המזהה של הלקוח נוצר מיצירת לקוח והמפתח שהתקבל מהפיירבייס
    * */

    public Appointments(String serviceType, String clientId, String status, String dateTime) {
        this.serviceType = serviceType;
        this.clientId = clientId;
        this.status = status;
        this.dateTime = dateTime;
    }


    public String getServiceType() {
        return serviceType;
    }

    public void setServiceType(String serviceType) {
        this.serviceType = serviceType;
    }

    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getDateTime() {
        return dateTime;
    }

    public void setDateTime(String dateTime) {
        this.dateTime = dateTime;
    }
}

