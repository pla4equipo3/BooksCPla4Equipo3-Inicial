package edu.uoc.plagrupo3.bookscpla4equipo3.ui;

import android.app.Activity;
import com.google.android.material.appbar.CollapsingToolbarLayout;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.squareup.picasso.Picasso;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import edu.uoc.plagrupo3.bookscpla4equipo3.R;
import edu.uoc.plagrupo3.bookscpla4equipo3.modeloDatos.Libro;
import edu.uoc.plagrupo3.bookscpla4equipo3.modeloDatos.LibroDatos;

/**
 * A fragment representing a single Item detail screen.
 * This fragment is either contained in a {@link ItemListActivity}
 * in two-pane mode (on tablets) or a {@link ItemDetailActivity}
 * on handsets.
 */
public class ItemDetailFragment extends Fragment {
    public static final String ARG_ITEM_ID = "item_id";
    private Libro mItem;
    private TextView descripcion,autor,fechaP,titulo,disponible;
    private ImageView fotoLibro;
    private FloatingActionButton buttonFavoritos;
    private Button buttonLiberar,buttonReservar;
    private ImageView imageGPS;

   public ItemDetailFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments().containsKey(ARG_ITEM_ID)) {
          //  mItem = LibroDatos.listalibros.get(Integer.parseInt(getArguments().getString(ARG_ITEM_ID)));
            int p = getArguments().getInt(ARG_ITEM_ID);
            mItem = LibroDatos.getBooks().get(getArguments().getInt(ARG_ITEM_ID));

            Activity activity = this.getActivity();
            CollapsingToolbarLayout appBarLayout = (CollapsingToolbarLayout) activity.findViewById(R.id.toolbar_layout);

            if (appBarLayout != null) appBarLayout.setTitle(mItem.getTitulo());
             else activity.setTitle(mItem.getTitulo());

        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.item_detail, container, false);

        descripcion = (TextView) rootView.findViewById(R.id.textViewDescripcion);
        titulo = (TextView)rootView.findViewById(R.id.textViewTitulo);
        autor = (TextView)rootView.findViewById(R.id.textViewAutor);
        fechaP = (TextView)rootView.findViewById(R.id.textViewFecha);
        fotoLibro = (ImageView)rootView.findViewById(R.id.imageViewFotoLibro);
        disponible = (TextView)rootView.findViewById(R.id.textViewDisponible);
        buttonFavoritos = (FloatingActionButton)rootView.findViewById(R.id.buttonFavorito);
        buttonLiberar = (Button)rootView.findViewById(R.id.buttonLiberar);
        buttonReservar = (Button)rootView.findViewById(R.id.buttonReservar);
        imageGPS = (ImageView) rootView.findViewById(R.id.imageButtonGPS);

        // Show the dummy content as text in a TextView.

        if (mItem != null) {
            autor.setText("By "+mItem.getAutor());
            titulo.setText(mItem.getTitulo());
            fechaP.setText(mItem.getFechapub());
            descripcion.setText(mItem.getDescripcion());
            if (mItem.getDisponible().equalsIgnoreCase("true"))
            {
                disponible.setText(R.string.Disponible);
                disponible.setTextColor(Color.GREEN);

            }else{
                disponible.setText(R.string.NoDisponible);
                disponible.setTextColor(Color.RED);

            }
            Picasso.get().load(mItem.getUrlimage()).error(R.drawable.no_photo).placeholder(R.drawable.logo).fit().centerCrop().into(fotoLibro);


           }

           buttonFavoritos.setOnClickListener(new View.OnClickListener() {
               @Override
               public void onClick(View v) {
                   dialogCloseSesion();

               }
           });
        imageGPS.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cargarMapa();

            }
        });
        return rootView;
    }


    private void dialogCloseSesion() {
        AlertDialog alertDialog = new AlertDialog.Builder(getContext()).create();
        alertDialog.setTitle(getResources().getString(R.string.MenuMisFavoritos));
        alertDialog.setMessage(getResources().getString(R.string.DialogAñadirFavorito)+" "+mItem.getTitulo()+"?");

        alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, getResources().getString(R.string.SI),
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                      LibroDatos.addFavourite(mItem.getId()); // método añadir a favorito
                      dialog.dismiss();

                    }
                });
        alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, getResources().getString(R.string.NO),
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        LibroDatos.deleteFavourite(mItem.getId()); // método borrar de favoritos
                        dialog.dismiss();
                    }
                });

        alertDialog.show();
    }

    private void addFavourite(){

    }
    private void cargarMapa(){

        Intent intent = new Intent(this.getContext(), MapsActivity.class)
                .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        intent.putExtra(ItemDetailFragment.ARG_ITEM_ID, mItem.getId());
        startActivity(intent);
    }

}
