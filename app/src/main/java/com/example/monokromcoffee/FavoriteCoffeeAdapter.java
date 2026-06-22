package com.example.monokromcoffee;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

/**
 * RecyclerView Adapter untuk menampilkan daftar coffee shop favorit.
 */
public class FavoriteCoffeeAdapter extends RecyclerView.Adapter<FavoriteCoffeeAdapter.ViewHolder> {

    private final List<CoffeeShop> items;
    private final FavoriteManager favoriteManager;
    private final OnFavoriteRemovedListener removeListener;

    /** Callback yang dipanggil ketika user menghapus item dari favorit */
    public interface OnFavoriteRemovedListener {
        void onRemoved(CoffeeShop shop);
    }

    public FavoriteCoffeeAdapter(List<CoffeeShop> items,
                                 FavoriteManager favoriteManager,
                                 OnFavoriteRemovedListener removeListener) {
        this.items = items;
        this.favoriteManager = favoriteManager;
        this.removeListener = removeListener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_favorite_coffee, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        CoffeeShop shop = items.get(position);

        holder.imgLogo.setImageResource(shop.getLogoResId());
        holder.tvName.setText(shop.getName());
        holder.tvRating.setText(shop.getRating());
        holder.tvAddress.setText(shop.getAddress());
        holder.tvPrice.setText(shop.getAvgPrice());

        // Klik tombol hati merah → hapus dari favorit
        holder.btnRemove.setOnClickListener(v -> {
            int pos = holder.getAdapterPosition();
            if (pos == RecyclerView.NO_ID) return;

            CoffeeShop removedShop = items.get(pos);
            favoriteManager.removeFavorite(removedShop.getName());
            items.remove(pos);
            notifyItemRemoved(pos);
            notifyItemRangeChanged(pos, items.size());

            if (removeListener != null) removeListener.onRemoved(removedShop);
        });

        // Klik card → buka CoffeeDetailActivity
        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(v.getContext(), CoffeeDetailActivity.class);
            intent.putExtra("shop_name", shop.getName());
            v.getContext().startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    // ─── ViewHolder ────────────────────────────────────────────────────────────

    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imgLogo;
        TextView  tvName, tvRating, tvAddress, tvPrice;
        ImageView btnRemove;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            imgLogo   = itemView.findViewById(R.id.img_fav_logo);
            tvName    = itemView.findViewById(R.id.tv_fav_name);
            tvRating  = itemView.findViewById(R.id.tv_fav_rating);
            tvAddress = itemView.findViewById(R.id.tv_fav_address);
            tvPrice   = itemView.findViewById(R.id.tv_fav_price);
            btnRemove = itemView.findViewById(R.id.btn_remove_favorite);
        }
    }
}
