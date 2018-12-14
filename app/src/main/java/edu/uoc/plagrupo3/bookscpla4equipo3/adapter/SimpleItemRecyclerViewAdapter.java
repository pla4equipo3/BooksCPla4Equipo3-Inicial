package edu.uoc.plagrupo3.bookscpla4equipo3.adapter;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import edu.uoc.plagrupo3.bookscpla4equipo3.R;
import edu.uoc.plagrupo3.bookscpla4equipo3.modeloDatos.Libro;
import edu.uoc.plagrupo3.bookscpla4equipo3.ui.ItemDetailActivity;
import edu.uoc.plagrupo3.bookscpla4equipo3.ui.ItemDetailFragment;
import edu.uoc.plagrupo3.bookscpla4equipo3.ui.ItemListActivity;

public class SimpleItemRecyclerViewAdapter extends RecyclerView.Adapter<SimpleItemRecyclerViewAdapter.ViewHolder> {

    private final ItemListActivity mParentActivity;
    private List<Libro> mValues;
    private final boolean mTwoPane;
    private final View.OnClickListener mOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            //Control cuando seleccionan un libro
            Libro item = (Libro) view.getTag();
            if (mTwoPane) {
                Bundle arguments = new Bundle();
                arguments.putInt(ItemDetailFragment.ARG_ITEM_ID, item.getId());
                ItemDetailFragment fragment = new ItemDetailFragment();
                fragment.setArguments(arguments);
                mParentActivity.getSupportFragmentManager().beginTransaction()
                        .replace(R.id.item_detail_container, fragment)
                        .commit();
            } else {
                Context context = view.getContext();
                Intent intent = new Intent(context, ItemDetailActivity.class);
               // intent.putExtra(ItemDetailFragment.ARG_ITEM_ID, String.valueOf(item.getId()));
                  intent.putExtra(ItemDetailFragment.ARG_ITEM_ID, item.getId());
                context.startActivity(intent);
            }
        }
    };

    public SimpleItemRecyclerViewAdapter(ItemListActivity parent,
                                  List<Libro> items,
                                  boolean twoPane) {
        mValues = items;
        mParentActivity = parent;
        mTwoPane = twoPane;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_list_content, parent, false);
        return new ViewHolder(view);
    }

   @Override
   public int getItemViewType(int position){
        //Cómo queremos un layout distinto para pares e impares, aquí utilizamos
        //position que nos indica la posición del elemento de la lista para
        //gestionar el viewType de la función onCreateViewHolder
        return position % 2;
    }

    @NonNull


    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        holder.titulolista.setText(mValues.get(position).getTitulo());
        holder.autorlista.setText(mValues.get(position).getAutor());
        holder.fechalista.setText(mValues.get(position).getFechapub());
        holder.disponiblelista.setText(mValues.get(position).getDisponible());

        if (mValues.get(position).getDisponible().equalsIgnoreCase("true")) {
            holder.disponiblelista.setText(R.string.Disponible);
            holder.disponiblelista.setTextColor(Color.GREEN);
        }
        else {
            holder.disponiblelista.setText(R.string.NoDisponible);
            holder.disponiblelista.setTextColor(Color.RED);


        }

     //   if (mValues.get(position).estaDisponible())
     //       holder.disponiblelista.setTextColor(Color.GREEN);
     //   else
     //       holder.disponiblelista.setTextColor(Color.RED);
       // new LibroDatos.cargaImagendeURL(holder.imagenlista).execute(mValues.get(position).getUrlimage());

        Picasso.get().load(mValues.get(position).getUrlimage()).error(R.drawable.no_photo).placeholder(R.drawable.logo).fit().centerCrop().into(holder.imagenlista);

        holder.itemView.setTag(mValues.get(position));
        holder.itemView.setOnClickListener(mOnClickListener);
    }

    @Override
    public int getItemCount() {
        return  mValues == null ? 0 : mValues.size();
       // if (mValues != null) {
       //     return mValues.size() > 0 ? mValues.size() : 0;
      //  } else {
      //      return 0;
       // }

    }

    class ViewHolder extends RecyclerView.ViewHolder {
        final TextView titulolista;
        final TextView autorlista;
        final TextView fechalista;
        final TextView disponiblelista;
        final ImageView imagenlista;

        ViewHolder(View view) {
            super(view);
            titulolista = (TextView) view.findViewById(R.id.id_titulo);
            autorlista = (TextView) view.findViewById(R.id.autor);
            fechalista = (TextView) view.findViewById(R.id.fechaP);
            disponiblelista = (TextView) view.findViewById(R.id.id_disponible);
            imagenlista = (ImageView) view.findViewById(R.id.libro_imagen_detail);
        }
    }
    //Método que actuliza los datos de lista.
    public void setItems(List<Libro> items) {
        Log.d("TAG", "actualizando");
        mValues = items;
        notifyDataSetChanged();
        //Indicamos que se ha actualizado la lista y que se tiene que refrescar
    }
}