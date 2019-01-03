package info.mis.motorequipment.helper;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import java.util.List;

import info.mis.motorequipment.Entity.PendingSrevice;
import info.mis.motorequipment.R;

public class PendingServiceActivityAdapter extends RecyclerView.Adapter<PendingServiceActivityAdapter.ViewHolder> {
    Context context;


    private List<PendingSrevice> mDataset;
    public class ViewHolder extends RecyclerView.ViewHolder {

        public TextView textCompany,textContact,textSL,textDateTime;
        public ImageView imageView;

        public RecyclerView lv;

        View listView;


        public ViewHolder(View v) {
            super(v);
            lv = (RecyclerView) v.findViewById(R.id.main_list);
            textSL = (TextView) v.findViewById(R.id.SLPending);
            textCompany = (TextView) v.findViewById(R.id.CompanyNamePending);
            textContact = (TextView) v.findViewById(R.id.ContactPersonPending);
            textDateTime = (TextView) v.findViewById(R.id.DatePending);

            listView = v;
            // imageView = (ImageView) v.findViewById(R.id.icon);

        }
    }
    public void add(int position, PendingSrevice item) {
        mDataset.add(position, item);
        notifyItemInserted(position);
    }
    public void remove(PendingSrevice item) {
        int position = mDataset.indexOf(item);
        mDataset.remove(position);
        notifyItemRemoved(position);
    }
    public PendingServiceActivityAdapter(List<PendingSrevice> myDataset) {
        mDataset = myDataset;
    }
    @Override
    public PendingServiceActivityAdapter.ViewHolder onCreateViewHolder(ViewGroup parent,
                                                   int viewType) {
        // create a new view
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.activity_pending_listview, parent, false);

        ViewHolder vh = new ViewHolder(v);
        return vh;
    }
    @SuppressLint("ResourceAsColor")
    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {

        Log.d("ColorCode", String.valueOf(mDataset.get(position).getServiceColor()));
        if(mDataset.get(position).getServiceColor() == 1 ){
            holder.listView.setBackgroundColor(0xFFFF0000);
        }

        if(mDataset.get(position).getServiceColor() == 2){
            holder.listView.setBackgroundColor(Color.parseColor("#ffcf00"));
        }

        if(mDataset.get(position).getServiceColor() == 3){
            holder.listView.setBackgroundColor(Color.parseColor("#00ff00"));
        }
        //holder.lv.setBackgroundColor(Integer.parseInt(mDataset.get(position).getServiceColor()));
        holder.textSL.setText(String.valueOf(mDataset.get(position).getServiceId()));
        holder.textCompany.setText(mDataset.get(position).getCompanyName());
        holder.textContact.setText(mDataset.get(position).getContactPerson());
        holder.textDateTime.setText(mDataset.get(position).getServiceTime());

    }
    @Override
    public int getItemCount() {
        return mDataset.size();
    }



}
