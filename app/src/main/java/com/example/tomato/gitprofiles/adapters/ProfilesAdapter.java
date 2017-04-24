package com.example.tomato.gitprofiles.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.tomato.gitprofiles.R;
import com.example.tomato.gitprofiles.model.GitProfile;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class ProfilesAdapter extends RecyclerView.Adapter<ProfilesAdapter.ViewHolder> {

    private ArrayList<GitProfile> profiles;
    private Context context;
    private LayoutInflater layoutInflater;

    static class ViewHolder extends RecyclerView.ViewHolder{
        View view;
        TextView login;
        TextView profileUrl;
        ImageView avatar;

        ViewHolder(View itemView) {
            super(itemView);
            view = itemView;
            login = (TextView) itemView.findViewById(R.id.login);
            profileUrl = (TextView) itemView.findViewById(R.id.profile_url);
            avatar = (ImageView) itemView.findViewById(R.id.avatar_image);
        }
    }

    public ProfilesAdapter(ArrayList<GitProfile> profiles, Context context) {
        layoutInflater = LayoutInflater.from(context);
        this.profiles = profiles;
        this.context = context;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ViewHolder(layoutInflater.inflate(R.layout.card_item, parent, false));
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        GitProfile profile = profiles.get(position);
        holder.profileUrl.setText(profile.getHtmlUrl());
        holder.login.setText(profile.getLogin());
        Picasso.with(context).load(profile.getAvatarUrl())
                .error(R.drawable.placeholder)
                .placeholder(R.drawable.placeholder)
                .into(holder.avatar);
    }

    @Override
    public int getItemCount() {
//        return profiles.size();
        return (null != profiles ? profiles.size() : 0);
    }

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
    }

    public void updateProfiles(ArrayList<GitProfile> profiles){
        this.profiles = profiles;
        notifyDataSetChanged();
    }

    public void clearProfiles(ArrayList<GitProfile> profiles){
        profiles.clear();
        notifyDataSetChanged();
    }

}
