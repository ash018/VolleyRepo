package info.mis.motorequipment.helper;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import info.mis.motorequipment.R;

import static info.mis.motorequipment.R.layout.activity_listview;

public class ForwardActivityAdapter extends ArrayAdapter {
    private final Context context;
    private final String[] values;
    private final String[] valueRole;
    private final String[] valueStaffId;
    public ArrayList<String> stuffIdList = new ArrayList<>();
    public ArrayList<String> roleList = new ArrayList<>();

    public List _select;




    private int chkArray[];
    public ForwardActivityAdapter(Context context, String[] values, String[] valueRole, String[] valueStaffId) {
        super(context, R.layout.activity_listview, values);
        this.context = context;
        this.values = values;
        this.valueRole = valueRole;
        this.valueStaffId = valueStaffId;
        this.chkArray = new int[valueStaffId.length];
    }

    public ArrayList<String> MyActionCallback(){
        return this.stuffIdList;
    }

    public ArrayList<String> getRoleList(){
        return this.roleList;
    }

    public int getRoleListSize(){
        return this.valueStaffId.length;
    }

    @Override
    public View getView(final int position, final View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);



        final View rowView = inflater.inflate(activity_listview, parent, false);
        TextView tvName = (TextView) rowView.findViewById(R.id.tvName);
        TextView tvStuffID = (TextView) rowView.findViewById(R.id.tvStuffID);
        TextView tvDesignation = (TextView) rowView.findViewById(R.id.tvDesignation);

//        ImageView imageView = (ImageView) rowView.findViewById(R.id.icon);
        tvName.setText(values[position]);
        tvStuffID.setText(valueStaffId[position]);
        tvDesignation.setText(valueRole[position]);

        //final ArrayList<String> stuffIdList = new ArrayList<>();

        Log.d("Position","Pos"+String.valueOf(position));

        final CheckBox cBox=(CheckBox) rowView.findViewById(R.id.checkAssign);


        cBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener()
        {

            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked)
            {


                if ( isChecked )
                {

                    stuffIdList.add(valueStaffId[position]);
                    roleList.add(valueRole[position]);
                    Log.d("check","Checked Box"+String.valueOf(position));
                    //chkArray[position]=1;

                    chkArray[position]=1;
                    Log.d("array","Checked Box"+String.valueOf(chkArray.length));
                }
                else{
                    for(int i=0;i<stuffIdList.size();i++){
                        if(stuffIdList.get(i) == valueStaffId[position]){
                            stuffIdList.remove(i);
                            roleList.remove(i);
                            Log.d("Remove","Removed"+String.valueOf(chkArray.length));
                            chkArray[position]=0;
                        }
                    }

                    Log.d("uncheck","UNChecked Box"+String.valueOf(position));
                }



            }
        });

        if(chkArray.length>0)
        {
            for (int i = 0; i < chkArray.length; i++)
            {
                if(chkArray[position] == 1)
                {
                    Log.d("check","Loop Box"+String.valueOf(position));
                    cBox.setChecked(true);
                }
            }
        }



        return rowView;
    }

    public ArrayList<String> getData(){
        int i=stuffIdList.size();
        return stuffIdList;
    }

}
