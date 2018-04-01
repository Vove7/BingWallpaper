package cn.vove7.bingwallpaper.fragments;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import cn.vove7.bingwallpaper.R;
import cn.vove7.bingwallpaper.activities.ViewImageActivity;
import cn.vove7.bingwallpaper.adapters.GalleryRecyclerViewAdapter;
import cn.vove7.bingwallpaper.adapters.ViewPageAdapter;
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
      LogHelper.d("gall onCreateView");
      View view = inflater.inflate(R.layout.fragment_gallery_list, container, false);
      swipeRefreshLayout = view.findViewById(R.id.swipe_refresh_gallery);
      swipeRefreshLayout.setOnRefreshListener(() ->
              new Handler().postDelayed(() -> {
                 refreshView(0);
                 swipeRefreshLayout.setRefreshing(false);
              }, 300));
      setHasOptionsMenu(true);
      // Set the adapter

      Context context = view.getContext();
      recyclerView = view.findViewById(R.id.list);

      manager=new GridLayoutManager(context, mColumnCount);
      recyclerView.setLayoutManager(manager);

      refreshView(0);

      return view;
   }
   private GridLayoutManager manager;

   private GalleryRecyclerViewAdapter.OnItemClickListener listener = p -> {

      Intent viewIntent = new Intent(getContext(), ViewImageActivity.class);
      viewIntent.putExtra("from", ViewPageAdapter.IMAGE_FROM_LOCAL);
      viewIntent.putExtra("startdates", Utils.List2Array(imagePaths));
      viewIntent.putExtra("pos", p);
      startActivityForResult(viewIntent, 0);
   };

   public void refreshView(int pos) {
      getPaths();
      if (adapter == null) {
         adapter = new GalleryRecyclerViewAdapter
                 (imagePaths, getScreenWidth(this.getContext()), mColumnCount);
         recyclerView.setAdapter(adapter);
         adapter.setListener(listener);
      }
      //((SimpleItemAnimator) recyclerView.getItemAnimator())
      //        .setSupportsChangeAnimations(false); //取消RecyclerView的动画效果

      //moveToPosition(pos);
      adapter.notifyDataSetChanged();
      manager.scrollToPositionWithOffset(pos*adapter.itemHeight,0);
      //recyclerView.scrollToPosition(adapter.getItemCount()-1);

   }
   private void moveToPosition(int n) {

      int firstItem = manager.findFirstVisibleItemPosition();
      int lastItem = manager.findLastVisibleItemPosition();
      if (n <= firstItem ){
         recyclerView.scrollToPosition(n);
      }else if ( n <= lastItem ){
         int top = recyclerView.getChildAt(n - firstItem).getTop();
         recyclerView.scrollBy(0, top);
      }else{
         recyclerView.scrollToPosition(n);
      }

   }
   @Override
   public void onActivityResult(int requestCode, int resultCode, Intent data) {
      if (requestCode == 0 && data != null) {
         int pos = data.getIntExtra("pos", 0);
         LogHelper.d(pos);
         getPaths();
         if (pos < imagePaths.size())
            refreshView(pos);
      }
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
         Utils.shareTo(getContext());
      }
      return false;
   }

   @Override
   public void onDetach() {
      super.onDetach();
   }


}
