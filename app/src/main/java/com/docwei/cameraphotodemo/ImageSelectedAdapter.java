package com.docwei.cameraphotodemo;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.docwei.imageupload_lib.GlideApp;
import com.docwei.imageupload_lib.view.RectImageView;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by git on 2018/4/30.
 * 附件显示图片
 */

public class ImageSelectedAdapter extends RecyclerView.Adapter<ImageSelectedAdapter.ViewHolder> implements View.OnClickListener {
    private Context mContext;
    private int TYPE_ADD = 0;
    private int TYPE_NORMAL = 1;
    private int maxCount;
    private List<String> list;

    public ImageSelectedAdapter(Context context, int count) {
        mContext = context;
        maxCount = count;

        list = new ArrayList<>();
        list.add("add");
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == TYPE_NORMAL) {
            View view = LayoutInflater.from(mContext)
                    .inflate(R.layout.recycler_image_select_item, parent, false);
            NormalVH normalVH = new NormalVH(view);
            normalVH.mIv_selected.setOnClickListener(this);
            normalVH.mIv_deleted.setOnClickListener(this);
            return normalVH;
        } else {
            View view = LayoutInflater.from(mContext)
                    .inflate(R.layout.recycler_image_add_item, parent, false);
            AddVH addVH = new AddVH(view);
            addVH.mIv_selected.setOnClickListener(this);
            return addVH;

        }

    }

    public void updateDataFromAlbum(List<String> images) {
        if (images.size() > 0) {
            int lastIndex = (list.size() - 1) < 0 ? 0 : list.size() - 1;
            list.addAll(lastIndex, images);
            notifyItemRangeChanged(lastIndex, images.size() + 1);
        }

    }

    public void updateDataFromCamera(String path) {
        if (!TextUtils.isEmpty(path)) {
            int lastIndex = (list.size() - 1) < 0 ? 0 : list.size() - 1;
            list.add(lastIndex, path);
            notifyItemRangeChanged(lastIndex, 2);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, final int position) {
        if (getItemViewType(position) == TYPE_ADD) {
            holder.mIv_selected.setImageResource(R.mipmap.icon_add_img);
            holder.mIv_selected.setVisibility(list.size() > maxCount ? View.GONE : View.VISIBLE);
            holder.mIv_selected.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //去添加图片
                    if (mOnImageHandleListener != null) {
                        mOnImageHandleListener.addImages(maxCount - list.size() + 1);
                    }
                }
            });
        } else {
            GlideApp.with(mContext)
                    .load(list.get(position)).placeholder(R.drawable.img_default)
                    .error(R.drawable.img_fail)
                    .into(holder.mIv_selected);
            if (holder instanceof NormalVH) {
                NormalVH vh = (NormalVH) holder;
                vh.mIv_deleted.setTag(position);
            }
            holder.mIv_selected.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //预览图片
                    if (mOnImageHandleListener != null) {
                        mOnImageHandleListener.previewImage(list.get(position), holder.mIv_selected);
                    }
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return list.size();//1是add用
    }

    @Override
    public void onClick(View v) {
        int position = (int) v.getTag();
        //删除图片
        list.remove(position);
        notifyItemRangeChanged(position, list.size() - position + 1);
    }

    @Override
    public int getItemViewType(int position) {
        if (list.size() == 1 && position == 0) {
            return TYPE_ADD;
        } else {
            if (position < list.size() - 1) {
                return TYPE_NORMAL;
            } else {
                return TYPE_ADD;
            }
        }

    }

    // 仅仅在选择图片后上传前使用
    public List<String> getSelectImages() {
        List<String> newPaths = new ArrayList<>();
        for (String str : list) {
            //相册
            if (str.startsWith("file:///")) {
                newPaths.add(str.substring(8));
                //拍照
            } else if (str.startsWith("content://com.docwei.cameraphotodemo.fileprovider")) {
                newPaths.add(mContext.getExternalCacheDir().getAbsolutePath() + str.substring(str.lastIndexOf("/")));
            }

        }
        return newPaths;
    }

    static class ViewHolder extends RecyclerView.ViewHolder {

        public final RectImageView mIv_selected;


        public ViewHolder(View itemView) {
            super(itemView);
            mIv_selected = itemView.findViewById(R.id.iv_selected);

        }

    }

    static class AddVH extends ViewHolder {
        public AddVH(View itemView) {
            super(itemView);
        }
    }

    static class NormalVH extends ViewHolder {
        public final ImageView mIv_deleted;

        public NormalVH(View itemView) {
            super(itemView);
            mIv_deleted = itemView.findViewById(R.id.iv_deleted);
        }
    }

    public OnImageHandleListener mOnImageHandleListener;

    public void setOnImageHandleListener(OnImageHandleListener onImageHandleListener) {
        mOnImageHandleListener = onImageHandleListener;
    }

    public interface OnImageHandleListener {
        void previewImage(String imagePath, RectImageView iv);

        void addImages(int count);

    }
}
