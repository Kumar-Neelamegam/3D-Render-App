package com.asoss.a3drender.app.Adapters;

import android.content.Context;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import com.asoss.a3drender.app.GlobalObjects.DataObjects;
import com.asoss.a3drender.app.GlobalObjects.ItemClickListener;
import com.asoss.a3drender.app.R;
import com.asoss.a3drender.app.Utilities.CircularTextView;

import java.util.List;

public class RecyclerViewHorizontalListAdapter extends RecyclerView.Adapter<RecyclerViewHorizontalListAdapter.StlViewHolder> {


    private List<DataObjects> horizontalStlList;
    Context context;

    private ItemClickListener clickListener;

    public void setClickListener(ItemClickListener itemClickListener) {
        this.clickListener = itemClickListener;
    }

    public RecyclerViewHorizontalListAdapter(List<DataObjects> horizontalStlList, Context context) {
        this.horizontalStlList = horizontalStlList;
        this.context = context;

    }


    @Override
    public StlViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        //inflate the layout file
        View groceryProductView = LayoutInflater.from(parent.getContext()).inflate(R.layout.activity_stllist_rowitem, parent, false);
        StlViewHolder holder = new StlViewHolder(groceryProductView);
        return holder;
    }


    @Override
    public void onBindViewHolder(StlViewHolder holder, final int position) {

        holder.txtTitle.setText(horizontalStlList.get(position).getTitle());

        holder.circularTextView.setText(String.valueOf(position+1));

    }


    @Override
    public int getItemCount() {
        return horizontalStlList.size();
    }


    public class StlViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        ImageView imgLogo;
        TextView txtTitle;
        CardView cardView;
        CircularTextView circularTextView;

        public StlViewHolder(View view) {
            super(view);
            cardView = view.findViewById(R.id.cardvw);
            imgLogo = view.findViewById(R.id.imgvw_thumb);
            txtTitle = view.findViewById(R.id.txtvw_filename);
            circularTextView = view.findViewById(R.id.circularTextView);
            circularTextView.setStrokeWidth(1);
            circularTextView.setStrokeColor("#ffffff");
            circularTextView.setSolidColor("#99CA3C");

            itemView.setTag(itemView);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {

            if (clickListener != null) clickListener.onClick(v, getAdapterPosition(), horizontalStlList);

        }

    }


}//END
