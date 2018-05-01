package com.docwei.cameraphotodemo.album;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.docwei.cameraphotodemo.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by git on 2018/4/29.
 */

public class ImageChooseAdapter extends RecyclerView.Adapter<ImageChooseAdapter.ViewHolder> implements View.OnClickListener{
    private List<ImageBean>       dataList=new ArrayList<>();
    private OnSelectImageListener listener;
    private int                   maxCount;
    private List<ImageBean> selectImages=new ArrayList<>();
    private RequestOptions mRequestOptions;

    public ImageChooseAdapter(int maxImageCount, OnSelectImageListener listener) {
        this.listener = listener;
        this.maxCount=maxImageCount;
    }
    public void updateData(List<ImageBean> list){
        dataList.clear();
        dataList.addAll(list);
        notifyDataSetChanged();
    }
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                                  .inflate(R.layout.recycler_item, parent, false);
        mRequestOptions = new RequestOptions().placeholder(R.drawable.img_default)
                                              .error(R.drawable.img_fail);
        final ViewHolder holder = new ViewHolder(view);
        holder.mFr_container.setOnClickListener(this);
        holder.mCbImg.setOnClickListener(this);
        return holder;
    }


    @SuppressWarnings("deprecation")
    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {
       ImageBean bean = dataList.get(position);
       holder.mCbImg.setSelected(bean.isSelect);
       holder.mOverlay.setVisibility(bean.isSelect?View.VISIBLE:View.GONE);
        if (!(bean.imagePath.startsWith("http") || bean.imagePath.startsWith("file") || bean.imagePath.startsWith("content"))) {
            bean.imagePath = "file:///" + bean.imagePath;
        }

        Glide.with(holder.itemView.getContext())
             .load(bean.imagePath)
             .apply(mRequestOptions)
             .into(holder.mIvImage);
        holder.mCbImg.setTag(position);
        holder.mCbImg.setTag(R.id.overlay,holder.mOverlay);
        holder.mFr_container.setTag(position);
    }

    @Override
    public int getItemCount() {
        return dataList == null
               ? 0
               : dataList.size();
    }

    @Override
    public void onClick(View view) {
        int position= (int) view.getTag();
        if(view.getId()==R.id.cb_image) {
            View            overlay = (View) view.getTag(R.id.overlay);
            final ImageBean bean    = dataList.get(position);
            if (selectImages.size() < maxCount) {
                bean.setSelect(!bean.isSelect);
                if (bean.isSelect) {
                    selectImages.add(bean);
                    view.setSelected(true);
                    overlay.setVisibility(View.VISIBLE);
                } else {
                    selectImages.remove(bean);
                    view.setSelected(false);
                    overlay.setVisibility(View.GONE);
                }
            } else {
                if (selectImages.contains(bean)) {
                    bean.setSelect(!bean.isSelect);
                    if (bean.isSelect) {
                        selectImages.add(bean);
                        view.setSelected(true);
                        overlay.setVisibility(View.VISIBLE);
                    } else {
                        selectImages.remove(bean);
                        view.setSelected(false);
                        overlay.setVisibility(View.GONE);
                    }
                } else {
                    if (listener != null) {
                        listener.selectIsFull();
                    }
                }
            }
            if (listener != null) {
                listener.selectImages(selectImages);
            }
        }else if(view.getId()==R.id.fr_container){
            if(mOnImagePreviewListener!=null){
                mOnImagePreviewListener.previewImage(dataList.get(position).getImagePath(),view);
            }
        }

    }


    class ViewHolder extends RecyclerView.ViewHolder {

        private final ImageView mIvImage;
        private final ImageView   mCbImg;
        private final View mOverlay;
        private final FrameLayout mFr_container;

        public ViewHolder(View itemView) {
            super(itemView);
            mFr_container = itemView.findViewById(R.id.fr_container);
            mIvImage = itemView.findViewById(R.id.iv_image);
            mCbImg = itemView.findViewById(R.id.cb_image);
            mOverlay = itemView.findViewById(R.id.overlay);
        }
    }

    public interface OnSelectImageListener {
        void selectIsFull();
        void selectImages(List<ImageBean> bean);

    }
    public OnImagePreviewListener mOnImagePreviewListener;

    public void setOnImagePreviewListener(OnImagePreviewListener onImagePreviewListener) {
        mOnImagePreviewListener = onImagePreviewListener;
    }

    public interface OnImagePreviewListener{
        void previewImage(String imagePath,View iv);
    }
}

