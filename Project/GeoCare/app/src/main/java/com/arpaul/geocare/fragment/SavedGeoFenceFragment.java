package com.arpaul.geocare.fragment;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import com.arpaul.geocare.FenceActivity;
import com.arpaul.geocare.GeoFenceActivity;
import com.arpaul.geocare.LocationSearchActivity;
import com.arpaul.geocare.R;
import com.arpaul.geocare.adapter.GeoLocationsAdapter;
import com.arpaul.geocare.common.ApplicationInstance;
import com.arpaul.geocare.dataAccess.GCCPConstants;
import com.arpaul.geocare.dataObject.PrefLocationDO;
import com.arpaul.utilitieslib.StringUtils;

import java.util.ArrayList;
import java.util.Calendar;

/**
 * Created by Aritra on 03-11-2016.
 */

public class SavedGeoFenceFragment extends Fragment implements LoaderManager.LoaderCallbacks {

    private Toolbar toolbar;
    private FloatingActionButton fabAddLocation;
    private TextView tvNoLocations;
    private RecyclerView rvGeoLocations;

    private ArrayList<PrefLocationDO> arrPrefLocationDO = new ArrayList<>();
    private GeoLocationsAdapter adapter;

    public static SavedGeoFenceFragment newInstance() {
        SavedGeoFenceFragment fragment = new SavedGeoFenceFragment();
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_savefence, container, false);

        initialiseFragment(view);

        bindControls();

        return view;
    }

    private void loadData(){
        getActivity().getSupportLoaderManager().initLoader(ApplicationInstance.LOADER_FETCH_ALL_LOCATION, null, this);
    }

    private void bindControls(){
        fabAddLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getActivity(), LocationSearchActivity.class));
            }
        });

    }

    @Override
    public void onResume() {
        super.onResume();

        if(getActivity().getSupportLoaderManager().getLoader(ApplicationInstance.LOADER_FETCH_ALL_LOCATION) != null)
            getActivity().getSupportLoaderManager().restartLoader(ApplicationInstance.LOADER_FETCH_ALL_LOCATION, null, this);
        else
            loadData();
    }

    @Override
    public Loader onCreateLoader(int id, Bundle args) {
        switch (id){
            case ApplicationInstance.LOADER_FETCH_ALL_LOCATION :
                return new CursorLoader(getActivity(), GCCPConstants.CONTENT_URI_SAVED_LOC,
                        new String[]{PrefLocationDO.LOCATIONID, PrefLocationDO.LOCATIONNAME, PrefLocationDO.ADDRESS,
                                PrefLocationDO.LATITUDE, PrefLocationDO.LONGITUDE},
                        null,
                        null,
                        null);
            default:
                return null;
        }
    }

    @Override
    public void onLoadFinished(Loader loader, Object data) {
        switch (loader.getId()){
            case ApplicationInstance.LOADER_FETCH_ALL_LOCATION :
                if(data instanceof Cursor) {
                    Cursor cursor = (Cursor) data;
                    if(cursor != null && cursor.moveToFirst()){
                        PrefLocationDO objPrefLocationDO = null;
                        arrPrefLocationDO.clear();
                        do {
                            objPrefLocationDO = new PrefLocationDO();
                            objPrefLocationDO.LocationId = StringUtils.getInt(cursor.getString(cursor.getColumnIndex(PrefLocationDO.LOCATIONID)));
                            objPrefLocationDO.LocationName = cursor.getString(cursor.getColumnIndex(PrefLocationDO.LOCATIONNAME));
                            objPrefLocationDO.Address = cursor.getString(cursor.getColumnIndex(PrefLocationDO.ADDRESS));
                            objPrefLocationDO.Latitude = StringUtils.getDouble(cursor.getString(cursor.getColumnIndex(PrefLocationDO.LATITUDE)));
                            objPrefLocationDO.Longitude = StringUtils.getDouble(cursor.getString(cursor.getColumnIndex(PrefLocationDO.LONGITUDE)));

                            arrPrefLocationDO.add(objPrefLocationDO);
                        } while (cursor.moveToNext());

                        if(arrPrefLocationDO != null && arrPrefLocationDO.size() > 0){
                            tvNoLocations.setVisibility(View.GONE);
                            rvGeoLocations.setVisibility(View.VISIBLE);

                            adapter.refresh(arrPrefLocationDO);
                        } else {
                            tvNoLocations.setVisibility(View.VISIBLE);
                            rvGeoLocations.setVisibility(View.GONE);
                        }

                    }
                }
                break;
        }
    }

    @Override
    public void onLoaderReset(Loader loader) {

    }

    private void initialiseFragment(View view){
//        toolbar = (Toolbar) view.findViewById(R.id.toolbar);
//        setSupportActionBar(toolbar);

        fabAddLocation = (FloatingActionButton) view.findViewById(R.id.fabAddLocation);

        tvNoLocations = (TextView) view.findViewById(R.id.tvNoLocations);
        rvGeoLocations = (RecyclerView) view.findViewById(R.id.rvGeoLocations);

        adapter = new GeoLocationsAdapter(getActivity(), new ArrayList<PrefLocationDO>());
        rvGeoLocations.setAdapter(adapter);
    }
}
