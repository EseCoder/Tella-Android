package rs.readahead.washington.mobile.views.adapters;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import androidx.vectordrawable.graphics.drawable.VectorDrawableCompat;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.hzontal.tella_vault.VaultFile;
import com.hzontal.utils.MediaFile;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import rs.readahead.washington.mobile.R;
import rs.readahead.washington.mobile.media.MediaFileHandler;
import rs.readahead.washington.mobile.media.VaultFileUrlLoader;
import rs.readahead.washington.mobile.presentation.entity.MediaFilesData;
import rs.readahead.washington.mobile.presentation.entity.VaultFileLoaderModel;
import rs.readahead.washington.mobile.presentation.entity.ViewType;
import rs.readahead.washington.mobile.util.Util;
import rs.readahead.washington.mobile.views.interfaces.IAttachmentsMediaHandler;


public class AttachmentsRecycleViewAdapter extends RecyclerView.Adapter<AttachmentsRecycleViewAdapter.ViewHolder> {
    private List<VaultFile> attachments = new ArrayList<>();
    private VaultFileUrlLoader glideLoader;
    private IAttachmentsMediaHandler attachmentsMediaHandler;
    protected ViewType type;


    public AttachmentsRecycleViewAdapter(Context context, IAttachmentsMediaHandler attachmentsMediaHandler,
                                         MediaFileHandler mediaFileHandler, ViewType type) {
        this.glideLoader = new VaultFileUrlLoader(context.getApplicationContext(), mediaFileHandler);
        this.attachmentsMediaHandler = attachmentsMediaHandler;
        this.type = type;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.card_attachment_media_file, parent, false);
        return new ViewHolder(v, type);
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, final int position) {
        final VaultFile vaultFile = attachments.get(position);

        holder.setRemoveButton();

        holder.setMetadataIcon(vaultFile);

        if (MediaFile.INSTANCE.isImageFileType(vaultFile.mimeType)) {
            holder.showImageInfo();
            Glide.with(holder.mediaView.getContext())
                    .using(glideLoader)
                    .load(new VaultFileLoaderModel(vaultFile, VaultFileLoaderModel.LoadType.THUMBNAIL))
                    .diskCacheStrategy(DiskCacheStrategy.NONE)
                    .skipMemoryCache(true)
                    .into(holder.mediaView);
        } else if (MediaFile.INSTANCE.isAudioFileType(vaultFile.mimeType)) {
            holder.showAudioInfo(vaultFile);
            Drawable drawable = VectorDrawableCompat.create(holder.itemView.getContext().getResources(),
                    R.drawable.ic_mic_gray, null);
            holder.mediaView.setImageDrawable(drawable);
        } else if (MediaFile.INSTANCE.isVideoFileType(vaultFile.mimeType)) {
            holder.showVideoInfo(vaultFile);
            Glide.with(holder.mediaView.getContext())
                    .using(glideLoader)
                    .load(new VaultFileLoaderModel(vaultFile, VaultFileLoaderModel.LoadType.THUMBNAIL))
                    .diskCacheStrategy(DiskCacheStrategy.NONE)
                    .skipMemoryCache(true)
                    .into(holder.mediaView);
        }

        holder.mediaView.setOnClickListener(v -> attachmentsMediaHandler.playMedia(vaultFile));

        holder.removeFile.setOnClickListener(view -> {
            removeAttachment(vaultFile);
            attachmentsMediaHandler.onRemoveAttachment(vaultFile);
        });
    }

    @Override
    public int getItemCount() {
        return attachments.size();
    }

    public void setAttachments(@NonNull List<VaultFile> attachments) {
        this.attachments = attachments;
        notifyDataSetChanged();
    }

    public void prependAttachment(@NonNull VaultFile vaultFile) {
        if (attachments.contains(vaultFile)) {
            return;
        }

        attachments.add(0, vaultFile);
        notifyItemInserted(0);
    }

    public void appendAttachment(@NonNull VaultFile vaultFile) {
        if (attachments.contains(vaultFile)) {
            return;
        }

        attachments.add(vaultFile);
        notifyItemInserted(attachments.size() - 1);
    }

    public void removeAttachment(@NonNull VaultFile vaultFile) {
        int position = attachments.indexOf(vaultFile);

        if (position == -1) {
            return;
        }

        attachments.remove(vaultFile);
        notifyItemRemoved(position);
    }

    public MediaFilesData getAttachments() {
        return new MediaFilesData(attachments);
    }

    public void clearAttachments() {
        attachments.clear();
        notifyDataSetChanged();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.mediaView)
        ImageView mediaView;
        @BindView(R.id.videoInfo)
        ViewGroup videoInfo;
        @BindView(R.id.videoDuration)
        TextView videoDuration;
        @BindView(R.id.audioInfo)
        ViewGroup audioInfo;
        @BindView(R.id.audioDuration)
        TextView audioDuration;
        @BindView(R.id.remove_file)
        ImageView removeFile;
        @BindView(R.id.metadata_icon)
        ImageView metadataIcon;

        private ViewType type;

        public ViewHolder(View itemView, ViewType type) {
            super(itemView);
            this.type = type;
            ButterKnife.bind(this, itemView);
        }

        void showVideoInfo(VaultFile vaultFile) {
            audioInfo.setVisibility(View.GONE);
            videoInfo.setVisibility(View.VISIBLE);
            if (vaultFile.duration > 0) {
                videoDuration.setText(getDuration(vaultFile));
                videoDuration.setVisibility(View.VISIBLE);
            } else {
                videoDuration.setVisibility(View.INVISIBLE);
            }
        }

        void showAudioInfo(VaultFile vaultFile) {
            videoInfo.setVisibility(View.GONE);
            audioInfo.setVisibility(View.VISIBLE);
            if (vaultFile.duration > 0) {
                audioDuration.setText(getDuration(vaultFile));
                audioDuration.setVisibility(View.VISIBLE);
            } else {
                audioDuration.setVisibility(View.INVISIBLE);
            }
        }

        void showImageInfo() {
            videoInfo.setVisibility(View.GONE);
            audioInfo.setVisibility(View.GONE);
        }

        void setRemoveButton() {
            removeFile.setVisibility(type == ViewType.PREVIEW ? View.GONE : View.VISIBLE);
        }

        private String getDuration(VaultFile vaultFile) {
            return Util.getShortVideoDuration((int) (vaultFile.duration / 1000));
        }

        void setMetadataIcon(VaultFile vaultFile) {
            if (vaultFile.metadata != null) {
                metadataIcon.setVisibility(View.VISIBLE);
            } else {
                metadataIcon.setVisibility(View.GONE);
            }
        }
    }
}