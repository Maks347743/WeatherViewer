package com.geek.maksim.potapov.weatherviewer;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;


public class FavoriteFragment extends Fragment implements FavoriteRecyclerItemTouchHelper.RecyclerItemTouchHelperListener {
    private ArrayList<String> mFavoriteList;
    private View mView;
    private RecyclerView mFavoriteRecyclerView;
    private FavoriteCityAdapter mFavoriteCityAdapter;
    private MenuItem mButtonMenuAdd;
    private String mCity;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mView = inflater.inflate(R.layout.favorite_fragment, container, false);
        SharedPreferences preferences = getContext().getSharedPreferences(FragmentActivity.CITY_PREFERENCES, Context.MODE_PRIVATE);
        if (preferences != null) {
            mFavoriteList = PreferencesHelper.loadFavoriteCities(preferences);
        }
        mFavoriteRecyclerView = mView.findViewById(R.id.favorite_recycler_view);
        mFavoriteRecyclerView.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));
        mFavoriteRecyclerView.setItemAnimator(new DefaultItemAnimator());
        mFavoriteRecyclerView.addItemDecoration(new DividerItemDecoration(getActivity(), DividerItemDecoration.VERTICAL));
        mFavoriteCityAdapter = new FavoriteCityAdapter(getContext(), mFavoriteList);
        mFavoriteRecyclerView.setAdapter(mFavoriteCityAdapter);
        ItemTouchHelper.SimpleCallback itemTouchHelperCallback = new FavoriteRecyclerItemTouchHelper(0, ItemTouchHelper.LEFT, this);
        new ItemTouchHelper(itemTouchHelperCallback).attachToRecyclerView(mFavoriteRecyclerView);
        Toolbar toolbar = mView.findViewById(R.id.toolbar);
        ((FragmentActivity) getActivity()).setSupportActionBar(toolbar);
        setHasOptionsMenu(true);
        return mView;
    }


    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_favorite, menu);
        mButtonMenuAdd = menu.findItem(R.id.add_to_favorite_item);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.add_to_favorite_item:
                AlertDialog.Builder dialog = new AlertDialog.Builder(getActivity());
                dialog.setTitle(R.string.add_to_favorite_text);
                EditText cityEditText = new EditText(getActivity());
                dialog.setView(cityEditText);
                dialog.setPositiveButton(android.R.string.ok, (dialogInterface, itemId) -> {
                    mCity = Utilities.getFormatCity(cityEditText.getText().toString());
                    if (mCity == null) {
                        Snackbar.make(getView(), R.string.enter_city_message, Snackbar.LENGTH_SHORT).show();
                        return;
                    }
                    if (mFavoriteList.contains(mCity)) {
                        Snackbar.make(getView(), R.string.city_already_added, Snackbar.LENGTH_SHORT).show();
                        return;
                    }
                    new CheckCityTask().execute(mCity);
                });
                dialog.setNegativeButton(android.R.string.cancel, null);
                dialog.show();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public class FavoriteCityAdapter extends RecyclerView.Adapter<FavoriteCityAdapter.FavoriteCityViewHolder> {
        private List<String> mFavoriteList;
        private Context mContext;

        public FavoriteCityAdapter(Context context, List<String> cities) {
            mFavoriteList = cities;
            mContext = context;
        }

        @Override
        public FavoriteCityViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            LayoutInflater inflater = LayoutInflater.from(mContext);
            View itemView = inflater.inflate(R.layout.favorite_recycler_view_item, parent, false);
            return new FavoriteCityViewHolder(itemView, mContext);
        }

        @Override
        public void onBindViewHolder(FavoriteCityViewHolder holder, final int position) {
            holder.bind(mFavoriteList.get(position));
        }

        @Override
        public int getItemCount() {
            return mFavoriteList.size();
        }

        public void removeItem(int position) {
            mFavoriteList.remove(position);
            // notify the item removed by position
            // to perform recycler view delete animations
            // NOTE: don't call notifyDataSetChanged()
            notifyItemRemoved(position);
        }

        public void restoreItem(String item, int position) {
            mFavoriteList.add(position, item);
            // notify item added by position
            notifyItemInserted(position);
        }

        public class FavoriteCityViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
            private Context mContext;
            private TextView mCityTextView;
            private RelativeLayout mViewBackground;
            RelativeLayout mViewForeground;


            public FavoriteCityViewHolder(View itemView, Context context) {
                super(itemView);
                mContext = context;
                mCityTextView = itemView.findViewById(R.id.favorite_city_text_view);
                mViewBackground = itemView.findViewById(R.id.view_background);
                mViewForeground = itemView.findViewById(R.id.view_foreground);
                itemView.setOnClickListener(this);
            }

            public void bind(String city) {
                mCityTextView.setText(city);
            }

            @Override
            public void onClick(View view) {
                SharedPreferences preferences = getActivity().getSharedPreferences(FragmentActivity.CITY_PREFERENCES, Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = preferences.edit();
                editor.putInt("position", this.getAdapterPosition());
                editor.apply();
                WeatherFragment weatherFragment = new WeatherFragment();
                FragmentManager manager = ((FragmentActivity) mContext).getSupportFragmentManager();
                manager.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
                FragmentTransaction transaction = manager.beginTransaction();
                transaction.replace(R.id.fragment_weather_container, weatherFragment);
                transaction.commit();
            }
        }
    }

    private class CheckCityTask extends AsyncTask<String, Void, HttpURLConnection> {

        @Override
        protected HttpURLConnection doInBackground(String... cities) {
            HttpURLConnection connection = null;
            try {
                String apiKey = getString(R.string.api_key);
                URL checkCityUrl = new URL(String.format(getActivity().getString(R.string.current_web_service_url), cities[0], apiKey));
                connection = (HttpURLConnection) checkCityUrl.openConnection();
                if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
                    Snackbar.make(getView(), R.string.city_not_found, Snackbar.LENGTH_SHORT).show();
                    return null;
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            return connection;
        }

        @Override
        protected void onPostExecute(HttpURLConnection connection) {
            if (connection != null) {
                SharedPreferences preferences = getContext().getSharedPreferences(FragmentActivity.CITY_PREFERENCES, Context.MODE_PRIVATE);
                if (preferences != null) {
                    mFavoriteList.add(mCity);
                    PreferencesHelper.saveFavoriteCities(mFavoriteList, getActivity());
                    mFavoriteCityAdapter.notifyDataSetChanged();
                    Snackbar.make(mView, getString(R.string.city_addition_message), Toast.LENGTH_SHORT).show();
                }
                connection.disconnect();
            }
        }
    }

    @Override
    public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction, int position) {
        if (viewHolder instanceof FavoriteCityAdapter.FavoriteCityViewHolder){
            //сохранение индекса и наименования города для восстановления через Snackbar
            String cityName = ((FavoriteCityAdapter.FavoriteCityViewHolder) viewHolder).mCityTextView.getText().toString();
            final int deletedIndex = viewHolder.getAdapterPosition();
            //удаление города из recyclerView
            mFavoriteCityAdapter.removeItem(viewHolder.getAdapterPosition());
            //удаление города из preference
            SharedPreferences preferences = getActivity().getSharedPreferences(FragmentActivity.CITY_PREFERENCES, Context.MODE_PRIVATE);
            if (mFavoriteList.size() == 0) {
                preferences.edit().remove("position").apply();
            }
            if (preferences!= null) {
                PreferencesHelper.saveFavoriteCities(mFavoriteList, getActivity());
            }
            //отображение snackbar с опцией восстановления
            Snackbar snackbar = Snackbar
                    .make(getView(), cityName + " removed from favorite!", Snackbar.LENGTH_LONG);
            snackbar.setAction("UNDO", view -> {
                // выбрана опция восстановления
                mFavoriteCityAdapter.restoreItem(cityName, deletedIndex);
                PreferencesHelper.saveFavoriteCities(mFavoriteList, getActivity());
            });
            snackbar.setActionTextColor(Color.YELLOW);
            snackbar.show();

        }
    }
}
