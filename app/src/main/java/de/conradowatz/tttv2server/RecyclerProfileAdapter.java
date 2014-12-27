package de.conradowatz.tttv2server;

import android.graphics.Bitmap;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;


public class RecyclerProfileAdapter extends RecyclerView.Adapter<RecyclerProfileAdapter.ProfileHolder> {

    private String[] nameArray;
    private Bitmap[] imageArray;

    public RecyclerProfileAdapter(String[] names, Bitmap[] avatars ) {
        nameArray = names;
        imageArray = avatars;
    }

    @Override
    public ProfileHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.cardview_profile_search_item, parent, false);

        return new ProfileHolder(itemView);
    }

    @Override
    public void onBindViewHolder(ProfileHolder holder, final int position) {

        holder.vName.setText(nameArray[position]);
        if (imageArray[position]!=null) {
            holder.vAvatar.setImageBitmap(imageArray[position]);
            holder.vLoadingPic.setVisibility(View.INVISIBLE);
        }
    }

    @Override
    public int getItemCount() {
        return nameArray.length;
    }


    public class ProfileHolder extends RecyclerView.ViewHolder {
        protected TextView vName;
        protected ImageView vAvatar;
        protected TextView vLoadingPic;


        public ProfileHolder(View itemView) {
            super(itemView);
            vName = (TextView) itemView.findViewById(R.id.profile_search_name);
            vAvatar = (ImageView) itemView.findViewById(R.id.profile_search_avatar);
            vLoadingPic = (TextView) itemView.findViewById(R.id.profile_search_loading_pic);

        }

    }
}