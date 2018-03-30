package cn.vove7.bingwallpaper.fragments;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SimpleItemAnimator;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import cn.vove7.bingwallpaper.R;
import cn.vove7.bingwallpaper.adapters.GalleryRecyclerViewAdapter;
import cn.vove7.bingwallpaper.utils.LogHelper;
import cn.vove7.bingwallpaper.utils.MyApplication;
import cn.vove7.bingwallpaper.utils.Utils;

import static cn.vove7.bingwallpaper.services.DownloadService.IMAGE_DIRECTORY;
import static cn.vove7.bingwallpaper.utils.Utils.getScreenWidth;

public class GalleryFragment extends Fragment {

   private final static int mColumnCount = 2;
   private GalleryRecyclerViewAdapter adapter;
   private ArrayList<String> imagePaths = new ArrayList<>();
   private SwipeRefreshLayout swipeRefreshLayout;
   private RecyclerView recyclerView;

   public GalleryFragment() {
   }


   @Override
   public void onCreate(Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);
      MyApplication.getApplication().setGalleryFragment(this);
   }

   @Override
   public View onCreateView(LayoutInflater inflater, ViewGroup container,
                            Bundle savedInstanceState) {
      LogHelper.logD("gall onCreateView");
      View view = inflater.inflate(R.layout.fragment_gallery_list, container, false);
      swipeRefreshLayout = view.findViewById(R.id.swipe_refresh_gallery);
      swipeRefreshLayout.setOnRefreshListener(() -> new Handler().postDelayed(() -> {
         refreshView();
         swipeRefreshLayout.setRefreshing(false);
      }, 300));
      setHasOptionsMenu(true);
      // Set the adapter

      Context context = view.getContext();
      recyclerView = view.findViewById(R.id.list);

      recyclerView.setLayoutManager(mColumnCount <= 1 ?
              new LinearLayoutManager(context) :
              new GridLayoutManager(context, mColumnCount));

      getPaths();
      if (adapter == null) {
         adapter = new GalleryRecyclerViewAdapter
                 (this, imagePaths, getScreenWidth(this.getContext()), mColumnCount);
         recyclerView.setAdapter(adapter);
         adapter.notifyItemRangeChanged(0, imagePaths.size());
      }
      return view;
   }

   public void refreshView() {
      getPaths();
      ((SimpleItemAnimator) recyclerView.getItemAnimator())
              .setSupportsChangeAnimations(false); //取消RecyclerView的动画效果
      adapter.notifyDataSetChanged();
   }

   private void getPaths() {
      File file = new File(IMAGE_DIRECTORY);
      List<String> list = Arrays.asList(file.list());
      Collections.sort(list);
      Collections.sort(list, Collections.reverseOrder());
      imagePaths.clear();
      imagePaths.addAll(list);
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
