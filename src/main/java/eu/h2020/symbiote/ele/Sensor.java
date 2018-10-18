package eu.h2020.symbiote.ele;

import eu.h2020.symbiote.model.cim.UnitOfMeasurement;

public class Sensor {
    Double latitude;
    Double longitude;
    String value;
    UnitOfMeasurement uom;

    public Double getLatitude() {
        return latitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public Double getLongitude() {
        return longitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }

    public String getValue() {
        if (value == null) {
            System.out.println("latitude:" + latitude);
        }
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public UnitOfMeasurement getUom() {
        return uom;
    }

    public void setUom(UnitOfMeasurement value) {
        this.uom= value;
    }
}
