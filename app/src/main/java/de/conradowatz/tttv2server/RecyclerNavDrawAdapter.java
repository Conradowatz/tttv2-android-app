package de.conradowatz.tttv2server;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;


public class RecyclerNavDrawAdapter extends RecyclerView.Adapter<RecyclerNavDrawAdapter.ProfileHolder> {

    private String[] titleArray;
    private int[] iconArray;
    private int selectedItem;
    private Context context;

    public RecyclerNavDrawAdapter(String[] titles, int[] icons) {
        titleArray = titles;
        iconArray = icons;
    }

    @Override
    public ProfileHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        context = parent.getContext();

        // If normal Item
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.nav_draw_item, parent, false);
        if (viewType==0) { //If header item
            itemView = LayoutInflater.from((parent.getContext())).inflate(R.layout.nav_draw_header, parent, false);
        }

        return new ProfileHolder(itemView, viewType);
    }

    @Override
    public int getItemViewType(int position) {
        int viewType = 1; //normal clickable item
        if (iconArray[position]==0) {
            viewType = 0; //Header item
        }

        return viewType;
    }

    @Override
    public void onBindViewHolder(ProfileHolder holder, final int position) {

        holder.vTitle.setText(titleArray[position]);
        if (iconArray[position]!=0) {
            holder.vIcon.setImageResource(iconArray[position]);

            if (position==selectedItem) {
                holder.vLayout.setBackgroundColor(context.getResources().getColor(R.color.light_grey));
            } else {
                holder.vLayout.setBackgroundResource(R.drawable.nav_drawer_selector_bg);
            }

        } else { if (position==0){
            holder.vDivider.setVisibility(View.INVISIBLE);
        }}
    }

    @Override
    public int getItemCount() {
        return titleArray.length;
    }

    public void selectItem(int position) {
        int prevSelected = selectedItem;
        selectedItem = position;
        notifyItemChanged(position);
        notifyItemChanged(prevSelected);
    }


    public class ProfileHolder extends RecyclerView.ViewHolder {
        protected RelativeLayout vLayout;
        protected TextView vTitle;
        protected ImageView vIcon;
        protected View vDivider;


        public ProfileHolder(View itemView, int viewType) {
            super(itemView);
            if (viewType==0) {
                vTitle = (TextView) itemView.findViewById(R.id.nav_draw_header);
                vDivider = itemView.findViewById(R.id.nav_draw_item_divider);
            } else {
                vTitle = (TextView) itemView.findViewById(R.id.nav_draw_title);
                vIcon = (ImageView) itemView.findViewById(R.id.nav_draw_icon);
                vLayout = (RelativeLayout) itemView.findViewById(R.id.nav_draw_item_layout);
            }

        }

    }
}