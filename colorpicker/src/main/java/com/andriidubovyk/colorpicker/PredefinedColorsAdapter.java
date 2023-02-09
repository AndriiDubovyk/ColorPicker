package com.andriidubovyk.colorpicker;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;


import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

class PredefinedColorsAdapter extends RecyclerView.Adapter<PredefinedColorsAdapter.ViewHolder> {
    private ArrayList<Integer> colorList;

    public PredefinedColorsAdapter(ArrayList<Integer>colorList) {
        this.colorList = colorList;
    }

    public int getColor(int position) {
        return colorList.get(position);
    }

    @Override
    public PredefinedColorsAdapter.ViewHolder onCreateViewHolder( ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.predefined_color_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(PredefinedColorsAdapter.ViewHolder holder, int position) {;
        holder.getColorHolder().setBackgroundColor(colorList.get(position));
    }



    @Override
    public int getItemCount() {
        return colorList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private final View colorHolder;

        public ViewHolder(View view) {
            super(view);
            this.colorHolder = view.findViewById(R.id.color_holder);
        }

        public View getColorHolder() {
            return this.colorHolder;
        }
    }
}
