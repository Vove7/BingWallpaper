package cn.vove7.bingwallpaper.fragments;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import cn.vove7.bingwallpaper.R;

/**
 * A simple {@link Fragment} subclass.
 */
public class BaseFragment extends Fragment {


   public BaseFragment() {
      // Required empty public constructor
   }


   @Override
   public View onCreateView(LayoutInflater inflater, ViewGroup container,
                            Bundle savedInstanceState) {
      TextView textView = new TextView(getActivity());
      textView.setText(R.string.hello_blank_fragment);
      return textView;
   }

}
