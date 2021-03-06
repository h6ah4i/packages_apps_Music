package com.dominionos.music.utils.adapters;

import android.content.Context;
import android.content.Intent;
import android.os.CountDownTimer;
import android.support.annotation.NonNull;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.dominionos.music.R;
import com.dominionos.music.service.MusicService;
import com.dominionos.music.ui.layouts.activity.PlaylistActivity;
import com.dominionos.music.utils.MySQLiteHelper;
import com.dominionos.music.utils.items.Playlist;

import java.util.List;

public class PlaylistAdapter extends RecyclerView.Adapter<PlaylistAdapter.SimpleItemViewHolder> {

    private final List<Playlist> items;
    private Context context;

    final static class SimpleItemViewHolder extends RecyclerView.ViewHolder {
        TextView gridName;
        ImageView overflow;
        View mainView;

        SimpleItemViewHolder(View view) {
            super(view);

            gridName = (TextView) view.findViewById(R.id.playlist_name);
            overflow = (ImageView) view.findViewById(R.id.playlist_menu);
            mainView = view;
        }
    }

    public PlaylistAdapter(Context context, List<Playlist> items) {
        this.context = context;
        this.items = items;
    }

    @Override
    public PlaylistAdapter.SimpleItemViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).
                inflate(R.layout.playlist_list_item, parent, false);


        return new SimpleItemViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(final SimpleItemViewHolder holder, final int position) {
        holder.gridName.setText(items.get(position).getName());
        if (items.get(position).getId() == -1) {
            holder.overflow.setVisibility(View.GONE);
            holder.mainView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    showNewPlaylistPrompt();
                }
            });
        } else {
            holder.overflow.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    PopupMenu popupMenu = new PopupMenu(context, v);
                    popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                        @Override
                        public boolean onMenuItemClick(MenuItem item) {
                            switch (item.getItemId()) {
                                case R.id.menu_playlist_delete:
                                    MySQLiteHelper helper = new MySQLiteHelper(context);
                                    helper.removePlayList(items.get(position).getId());
                                    items.remove(position);
                                    notifyItemRemoved(position);
                                    new CountDownTimer(400, 1000) {

                                        public void onTick(long millisUntilFinished) {
                                        }

                                        public void onFinish() {
                                            notifyDataSetChanged();
                                        }

                                    }.start();
                                    return true;
                                case R.id.menu_playlist_play:
                                    Intent i = new Intent();
                                    i.putExtra("playlistId", items.get(position).getId());
                                    i.setAction(MusicService.ACTION_PLAY_PLAYLIST);
                                    context.sendBroadcast(i);
                                    return true;
                                case R.id.menu_playlist_rename:
                                    showRenamePlaylistPrompt(position);
                                    return true;
                            }
                            return false;
                        }
                    });
                    popupMenu.inflate(R.menu.playlist_popup_menu);
                    popupMenu.show();
                }
            });
            holder.mainView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent i = new Intent(context, PlaylistActivity.class);
                    i.putExtra("playlistId", items.get(position).getId());
                    i.putExtra("title", items.get(position).getName());
                    context.startActivity(i);
                }
            });
        }
    }


    private void showNewPlaylistPrompt() {

        new MaterialDialog.Builder(context)
                .title("Create playlist")
                .inputType(InputType.TYPE_CLASS_TEXT)
                .input("e.g. Favourites", null, new MaterialDialog.InputCallback() {

                    @Override
                    public void onInput(@NonNull MaterialDialog dialog, CharSequence input) {
                        if(!input.toString().equals("")) {
                            MySQLiteHelper helper = new MySQLiteHelper(context);
                            items.add(items.size() - 1, new Playlist(helper.createNewPlayList(
                                    input.toString()), input.toString()));
                            notifyItemInserted(items.size() - 2);
                        } else {
                            Toast.makeText(context, "Playlist name cannot be empty", Toast.LENGTH_SHORT).show();
                        }
                    }
                }).show();
    }

    private void showRenamePlaylistPrompt(final int pos) {
        new MaterialDialog.Builder(context)
                .title("Rename playlist")
                .inputType(InputType.TYPE_CLASS_TEXT)
                .input("e.g. Favourites", null, new MaterialDialog.InputCallback() {

                    @Override
                    public void onInput(@NonNull MaterialDialog dialog, CharSequence input) {
                        if(!input.toString().equals("")) {
                            MySQLiteHelper helper = new MySQLiteHelper(context);
                            helper.renamePlaylist(input.toString(), items.get(pos).getId());
                            items.get(pos).setName(input.toString());
                            notifyItemChanged(pos);
                        } else {
                            Toast.makeText(context, "Playlist name cannot be empty", Toast.LENGTH_SHORT).show();
                        }
                    }
                }).show();
    }


    @Override
    public int getItemCount() {
        return this.items.size();
    }
}

