package cn.vove7.bingwallpaper.fragments;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import cn.vove7.bingwallpaper.R;
import cn.vove7.bingwallpaper.adapters.WallpaperRecyclerViewAdapter;
import cn.vove7.bingwallpaper.fragments.dummy.DummyContent;
import cn.vove7.bingwallpaper.utils.Utils;

public class WallpaperFragment extends Fragment {

   // TODO: Customize parameter argument names
   private static final String ARG_COLUMN_COUNT = "column-count";
   // TODO: Customize parameters
   private int mColumnCount = 2;

   /**
    * Mandatory empty constructor for the fragment manager to instantiate the
    * fragment (e.g. upon screen orientation changes).
    */
   public WallpaperFragment() {
   }

   // TODO: Customize parameter initialization
   @SuppressWarnings("unused")
   public static WallpaperFragment newInstance(int columnCount) {
      WallpaperFragment fragment = new WallpaperFragment();
      Bundle args = new Bundle();
      args.putInt(ARG_COLUMN_COUNT, columnCount);
      fragment.setArguments(args);
      return fragment;
   }

   @Override
   public void onCreate(Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);

      if (getArguments() != null) {
         mColumnCount = getArguments().getInt(ARG_COLUMN_COUNT);
      }
   }

   @Override
   public View onCreateView(LayoutInflater inflater, ViewGroup container,
                            Bundle savedInstanceState) {
      View view = inflater.inflate(R.layout.fragment_wallpaper_list, container, false);
      setHasOptionsMenu(true);
      // Set the adapter
      if (view instanceof RecyclerView) {
         Context context = view.getContext();
         RecyclerView recyclerView = (RecyclerView) view;
         if (mColumnCount <= 1) {
            recyclerView.setLayoutManager(new LinearLayoutManager(context));
         } else {
            recyclerView.setLayoutManager(new GridLayoutManager(context, mColumnCount));
         }
         recyclerView.setAdapter(new WallpaperRecyclerViewAdapter(DummyContent.ITEMS));
      }
      return view;
   }

   @Override
   public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
      inflater.inflate(R.menu.wallpaper_menu, menu);
      super.onCreateOptionsMenu(menu, inflater);
   }

   @Override
   public boolean onOptionsItemSelected(MenuItem item) {
      if (item.getItemId() == R.id.menu_share) {
         Utils.shareTo(this.getContext());
      }
      return false;
   }

   @Override
   public void onDetach() {
      super.onDetach();
   }

}
