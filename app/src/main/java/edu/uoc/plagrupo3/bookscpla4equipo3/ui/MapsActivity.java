package edu.uoc.plagrupo3.bookscpla4equipo3.ui;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.FragmentActivity;
import edu.uoc.plagrupo3.bookscpla4equipo3.R;
import edu.uoc.plagrupo3.bookscpla4equipo3.modeloDatos.Libro;
import edu.uoc.plagrupo3.bookscpla4equipo3.modeloDatos.LibroDatos;

import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import static edu.uoc.plagrupo3.bookscpla4equipo3.ui.ItemDetailFragment.ARG_ITEM_ID;

public class MapsActivity extends AppCompatActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private Libro mItem;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        Bundle parametros = this.getIntent().getExtras();
        if (parametros.containsKey(ARG_ITEM_ID)) {
            //  mItem = LibroDatos.listalibros.get(Integer.parseInt(getArguments().getString(ARG_ITEM_ID)));
            int p = parametros.getInt(ARG_ITEM_ID);
            mItem = LibroDatos.getBooks().get(parametros.getInt(ARG_ITEM_ID));
        }

        mapFragment.getMapAsync(this);
    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        Log.d("TAG","MAPA " + mItem.getTitulo());
        // Add a marker in Sydney and move the camera
        LatLng localLibro = new LatLng(Double.parseDouble(mItem.getLatitud()), Double.parseDouble(mItem.getLongitud()));
        mMap.addMarker(new MarkerOptions().position(localLibro).title("Marker in Sydney"));
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(localLibro,15));

    }
}
