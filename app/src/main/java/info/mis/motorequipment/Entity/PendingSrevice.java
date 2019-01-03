package info.mis.motorequipment.Entity;

public class PendingSrevice {

    public int ServiceId;
    public String CompanyName;
    public String ContactPerson;
    public String ServiceTime;
    public Integer ServiceColor;

    public PendingSrevice(){}

    public PendingSrevice(int ServiceId, String CompanyName, String ContactPerson,String ServiceTime,Integer ServiceColor) {
        this.ServiceId = ServiceId;
        this.CompanyName = CompanyName;
        this.ContactPerson = ContactPerson;
        this.ServiceTime = ServiceTime;
        this.ServiceColor = ServiceColor;
    }

    public int getServiceId() {
        return ServiceId;
    }

    public void setServiceId(int ServiceId) {
        this.ServiceId = ServiceId;
    }

    public String getCompanyName() {
        return CompanyName;
    }

    public void setCompanyName(String CompanyName) {
        this.CompanyName = CompanyName;
    }

    public String getContactPerson() {
        return ContactPerson;
    }

    public void setContactPerson(String ContactPerson) {
        this.ContactPerson = ContactPerson;
    }

    public String getServiceTime() {
        return ServiceTime;
    }

    public void setServiceTime(String ServiceTime) {
        this.ServiceTime = ServiceTime;
    }

    public Integer getServiceColor(){
        return ServiceColor;
    }

    public void setServiceColor(Integer serviceColor){
        this.ServiceColor = serviceColor;
    }

}
