package com.example.smartpantry.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.smartpantry.R;
import com.example.smartpantry.logic.UnitConverter;
import com.example.smartpantry.model.PantryItem;
import com.example.smartpantry.util.ExpiryUtils;
import com.example.smartpantry.util.IngredientIcons;

import java.util.ArrayList;
import java.util.List;

/**
 * Custom RecyclerView adapter that binds pantry rows from the database to item_pantry.xml.
 * onCreateViewHolder inflates a row, onBindViewHolder fills it with one PantryItem.
 */
public class PantryAdapter extends RecyclerView.Adapter<PantryAdapter.ViewHolder> {

    public interface Listener {
        void onEdit(PantryItem item);

        void onDelete(PantryItem item);
    }

    private final List<PantryItem> items = new ArrayList<>();
    private final Listener listener;
    private boolean alertsEnabled = true;

    public PantryAdapter(Listener listener) {
        this.listener = listener;
    }

    /** Replaces the displayed data (called after every database change). */
    public void setItems(List<PantryItem> newItems, boolean alertsEnabled) {
        items.clear();
        items.addAll(newItems);
        this.alertsEnabled = alertsEnabled;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_pantry, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        final PantryItem item = items.get(position);
        Context ctx = holder.itemView.getContext();

        holder.icon.setText(IngredientIcons.forName(item.getName()));
        holder.name.setText(item.getName());
        holder.qty.setText(UnitConverter.formatQuantity(item.getQuantity()) + " " + item.getUnit());

        Long days = ExpiryUtils.daysUntilExpiry(item.getExpiryDate());
        if (days == null) {
            holder.expiry.setVisibility(View.GONE);
        } else {
            holder.expiry.setVisibility(View.VISIBLE);
            String shown = ExpiryUtils.toDisplay(item.getExpiryDate());
            int color = R.color.text_secondary;
            String text = ctx.getString(R.string.item_expires, shown);
            if (alertsEnabled && days < 0) {
                text = ctx.getString(R.string.item_expired, shown);
                color = R.color.danger;
            } else if (alertsEnabled && days <= ExpiryUtils.SOON_DAYS) {
                text = ctx.getString(R.string.item_expiring_soon, shown);
                color = R.color.warning;
            }
            holder.expiry.setText(text);
            holder.expiry.setTextColor(ContextCompat.getColor(ctx, color));
        }

        View.OnClickListener edit = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.onEdit(item);
            }
        };
        holder.itemView.setOnClickListener(edit);
        holder.btnEdit.setOnClickListener(edit);
        holder.btnDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.onDelete(item);
            }
        });
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        final TextView icon;
        final TextView name;
        final TextView qty;
        final TextView expiry;
        final ImageButton btnEdit;
        final ImageButton btnDelete;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            icon = itemView.findViewById(R.id.txtIcon);
            name = itemView.findViewById(R.id.txtName);
            qty = itemView.findViewById(R.id.txtQty);
            expiry = itemView.findViewById(R.id.txtExpiry);
            btnEdit = itemView.findViewById(R.id.btnEdit);
            btnDelete = itemView.findViewById(R.id.btnDelete);
        }
    }
}
