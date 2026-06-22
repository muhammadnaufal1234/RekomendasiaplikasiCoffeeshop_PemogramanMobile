package com.example.monokromcoffee;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

/**
 * RecyclerView Adapter untuk menampilkan daftar review yang pernah ditulis user.
 */
public class ReviewAdapter extends RecyclerView.Adapter<ReviewAdapter.ViewHolder> {

    private final List<Review> items;
    private final ReviewManager reviewManager;
    private final OnReviewDeletedListener deleteListener;

    /** Callback saat user menghapus review */
    public interface OnReviewDeletedListener {
        void onDeleted(Review review, int position);
    }

    public ReviewAdapter(List<Review> items,
                         ReviewManager reviewManager,
                         OnReviewDeletedListener deleteListener) {
        this.items         = items;
        this.reviewManager = reviewManager;
        this.deleteListener = deleteListener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_review, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Review review = items.get(position);

        holder.imgLogo.setImageResource(review.getLogoResId());
        // Nama user (bold, primer)
        holder.tvUsername.setText(
                review.getUserName() != null && !review.getUserName().isEmpty()
                        ? review.getUserName() : "Pengguna");
        // Nama coffee shop (subtitle abu-abu)
        holder.tvShopName.setText(review.getShopName());
        holder.tvComment.setText(review.getComment());
        holder.tvDate.setText(review.getDate());

        // Render bintang
        renderStars(holder, review.getRating());

        // Klik hapus
        holder.btnDelete.setOnClickListener(v -> {
            int pos = holder.getAdapterPosition();
            if (pos == RecyclerView.NO_ID) return;

            Review deleted = items.get(pos);
            reviewManager.removeReviewForShop(deleted.getShopName());
            items.remove(pos);
            notifyItemRemoved(pos);
            notifyItemRangeChanged(pos, items.size());

            if (deleteListener != null) deleteListener.onDeleted(deleted, pos);
        });
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    // ─── Helper: render bintang aktif/nonaktif ──────────────────────────────

    private void renderStars(ViewHolder holder, int rating) {
        ImageView[] stars = {holder.star1, holder.star2, holder.star3, holder.star4, holder.star5};
        int activeColor   = Color.parseColor("#8B4513");  // cokelat (brown)
        int inactiveColor = Color.parseColor("#E0E0E0");  // abu-abu

        for (int i = 0; i < stars.length; i++) {
            stars[i].setColorFilter(i < rating ? activeColor : inactiveColor);
        }
    }

    // ─── ViewHolder ────────────────────────────────────────────────────────────

    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imgLogo, btnDelete;
        ImageView star1, star2, star3, star4, star5;
        TextView  tvUsername, tvShopName, tvComment, tvDate;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            imgLogo    = itemView.findViewById(R.id.img_review_logo);
            btnDelete  = itemView.findViewById(R.id.btn_delete_review);
            tvUsername = itemView.findViewById(R.id.tv_review_username);
            tvShopName = itemView.findViewById(R.id.tv_review_shop_name);
            tvComment  = itemView.findViewById(R.id.tv_review_comment);
            tvDate     = itemView.findViewById(R.id.tv_review_date);
            star1      = itemView.findViewById(R.id.star_1);
            star2      = itemView.findViewById(R.id.star_2);
            star3      = itemView.findViewById(R.id.star_3);
            star4      = itemView.findViewById(R.id.star_4);
            star5      = itemView.findViewById(R.id.star_5);
        }
    }
}
