package info.mis.motorequipment.Entity;

public class LocationX {
    public String latitude,longitude;

    public LocationX(){

    }

    public LocationX(String lat,String lng){
        this.latitude = lat;
        this.longitude = lng;
    }

    public String getLatitude(){
        return latitude;
    }

    public String getLongitude(){
        return longitude;
    }

    public void setLatitude(String lat){
        this.latitude = lat;
    }

    public void setLongitude(String lng){
        this.longitude = lng;
    }
}
