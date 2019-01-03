package info.mis.motorequipment.helper;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.util.ArrayList;
import java.util.List;
import java.util.jar.Attributes;

import info.mis.motorequipment.Entity.PendingSrevice;

public class JSONParse {
    //Declare the arrays of fields you require
    public static String[] names;
    public static String[] Persons;
    public static String[] sl;
    public static String[] DateTime;
    public static Integer[] ServiceColor;
    private JSONArray pendingServices = null;


    List<PendingSrevice> PendingServices ;


    private String json;

    public JSONParse(String json){

        this.json = json;
    }

    public void parseJSON(){
        JSONObject jsonObject=null;

        try {
            JSONObject jObj = new JSONObject(json);

            String StatusMessage = jObj.getString("StatusMessage");

            Log.d("StatusMessage-->", StatusMessage);

            /* Previous Code
            pendingServices = new JSONArray(StatusMessage);
            Log.d("pendingservices", pendingServices.toString());
            */

            pendingServices = new JSONArray(StatusMessage);
            for(int n = 0; n < pendingServices.length(); n++)
            {
                JSONObject object = pendingServices.getJSONObject(n);
                Log.d("Heelo",String.valueOf(n));
                // do some stuff....
            }




            sl = new String[pendingServices.length()];
            names = new String[pendingServices.length()];
            Persons = new String[pendingServices.length()];
            DateTime = new String[pendingServices.length()];
            ServiceColor = new Integer[pendingServices.length()];
            PendingServices = new ArrayList<PendingSrevice>();



            for(int i=0;i< pendingServices.length();i++){
                PendingSrevice pendingservice_object =  new PendingSrevice();

                jsonObject = pendingServices.getJSONObject(i);

                sl[i] = jsonObject.getString("ServiceId");
                DateTime[i] = jsonObject.getString("ServiceId__ServiceTime");
                names[i] = jsonObject.getString("ServiceId__CompanyName");
                Persons[i] = jsonObject.getString("To__UserName");
                ServiceColor[i]= jsonObject.getInt("color");

                pendingservice_object.setServiceId(Integer.parseInt(sl[i]));
                pendingservice_object.setServiceTime(DateTime[i]);
                pendingservice_object.setCompanyName(names[i]);
                pendingservice_object.setContactPerson(Persons[i]);
                pendingservice_object.setServiceColor(ServiceColor[i]);
                PendingServices.add(pendingservice_object);
                Log.d("SERVICE",names[i]);


            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
    public List<PendingSrevice> getPendingServices()
    {
        //function to return the final populated list
        return PendingServices;
    }
}
