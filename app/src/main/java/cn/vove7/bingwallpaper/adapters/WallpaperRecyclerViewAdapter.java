package cn.vove7.bingwallpaper.adapters;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import cn.vove7.bingwallpaper.R;
import cn.vove7.bingwallpaper.fragments.dummy.DummyContent.DummyItem;

import java.util.List;

public class WallpaperRecyclerViewAdapter extends RecyclerView.Adapter<WallpaperRecyclerViewAdapter.ViewHolder> {

   private final List<DummyItem> mValues;

   public WallpaperRecyclerViewAdapter(List<DummyItem> items) {
      mValues = items;
   }

   @Override
   public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
      View view = LayoutInflater.from(parent.getContext())
              .inflate(R.layout.fragment_wallpaper, parent, false);
      return new ViewHolder(view);
   }

   @Override
   public void onBindViewHolder(final ViewHolder holder, int position) {
      holder.mItem = mValues.get(position);
      holder.mIdView.setText(mValues.get(position).id);
      holder.mContentView.setText(mValues.get(position).content);

   }

   @Override
   public int getItemCount() {
      return mValues.size();
   }

   public class ViewHolder extends RecyclerView.ViewHolder {
      public final View mView;
      public final TextView mIdView;
      public final TextView mContentView;
      public DummyItem mItem;

      public ViewHolder(View view) {
         super(view);
         mView = view;
         mIdView = view.findViewById(R.id.id);
         mContentView = view.findViewById(R.id.content);
      }

      @Override
      public String toString() {
         return super.toString() + " '" + mContentView.getText() + "'";
      }
   }
}
