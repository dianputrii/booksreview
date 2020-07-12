package com.example.BooksReview;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;


import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.StorageReference;

import static com.google.firebase.storage.FirebaseStorage.getInstance;

public class MainActivity extends AppCompatActivity {

    RecyclerView mRecyclerView;
    FirebaseDatabase mFirebaseDatabase;
    DatabaseReference mRef;

    FirebaseRecyclerAdapter<Model,ViewHolder> firebaseRecyclerAdapter;
    FirebaseRecyclerOptions<Model> options;

    private void showData(){
            options = new FirebaseRecyclerOptions.Builder<Model>().setQuery(mRef, Model.class).build();

            firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<Model, ViewHolder>(options) {
                @Override
                protected void onBindViewHolder(@NonNull ViewHolder holder, int position, @NonNull Model model) {

                    holder.setDetails(getApplicationContext(), model.getJudul(), model.getDeskripsi(), model.getGambar());
                }

                @NonNull
                @Override
                public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

                    View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.baris,parent, false);

                    ViewHolder viewHolder = new ViewHolder(itemView);

                    viewHolder.setOnClickListener(new ViewHolder.ClickListener() {
                        @Override
                        public void onItemClick(View view, int position) {
                            String mTitle = getItem(position).getJudul();
                            String mDesc = getItem(position).getDeskripsi();
                            String mImage = getItem(position).getGambar();

                            Intent intent = new Intent(view.getContext(), PostDetailActivity.class);


                            intent.putExtra("gambar", mImage);
                            intent.putExtra("judul", mTitle);
                            intent.putExtra("deskripsi", mDesc);
                            startActivity(intent);
                        }

                        @Override
                        public void onItemLongClick(View view, int position) {

                            final String cTitle = getItem(position).getJudul();
                            final String cImage = getItem(position).getGambar();
                            final String cDesc = getItem(position).getDeskripsi();

                            AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                            String[] options = {"Update", "Hapus"};
                            builder.setItems(options, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int which) {
                                    if (which  == 0){

                                        Intent intent = new Intent(MainActivity.this, AddPostActivity.class);
                                        intent.putExtra("cTitle", cTitle);
                                        intent.putExtra("cDesc", cDesc);
                                        intent.putExtra("cImage", cImage);
                                        startActivity(intent);

                                    }

                                    if (which == 1){
                                        showDeleteDataDialog(cTitle, cImage,cDesc);
                                    }
                                }
                            });
                            builder.create().show();


                        }
                    });

                    return viewHolder;
                }
            };

        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        firebaseRecyclerAdapter.startListening();
        mRecyclerView.setAdapter(firebaseRecyclerAdapter);

    }

    private void showDeleteDataDialog(final String cTitle, final String cImage, String cDesc) {
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setTitle("Delete");
        builder.setMessage("Anda yakin ingin menghapus Post ini?");
        builder.setPositiveButton("Ya", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                Query mQuery = mRef.orderByChild("judul").equalTo(cTitle);
                mQuery.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        for (DataSnapshot ds: dataSnapshot.getChildren()){
                            ds.getRef().removeValue();
                        };
                        Toast.makeText(MainActivity.this, "Berhasil Dihapus", Toast.LENGTH_SHORT).show();

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        Toast.makeText(MainActivity.this, databaseError.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });

                StorageReference mPictureRefe = getInstance().getReferenceFromUrl(cImage);
                mPictureRefe.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Toast.makeText(MainActivity.this, "Gambar berhasil dihapus", Toast.LENGTH_SHORT).show();
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {

                        Toast.makeText(MainActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
        builder.setNegativeButton("Tidak", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        });
        builder.create().show();

    }

    private void firebaseSearch(String searchText){
        Query firebaseSearchQuery = mRef.orderByChild("judul").startAt(searchText).endAt(searchText + "\uf8ff");

        options = new FirebaseRecyclerOptions.Builder<Model>().setQuery(firebaseSearchQuery, Model.class).build();

        firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<Model, ViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull ViewHolder holder, int position, @NonNull Model model) {

                holder.setDetails(getApplicationContext(), model.getJudul(), model.getDeskripsi(), model.getGambar());
            }

            @NonNull
            @Override
            public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

                View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.baris,parent, false);

                ViewHolder viewHolder = new ViewHolder(itemView);

                viewHolder.setOnClickListener(new ViewHolder.ClickListener() {
                    @Override
                    public void onItemClick(View view, int position) {
                        String mTitle = getItem(position).getJudul();
                        String mDesc = getItem(position).getDeskripsi();
                        String mImage = getItem(position).getGambar();

                        Intent intent = new Intent(view.getContext(), PostDetailActivity.class);


                        intent.putExtra("gambar", mImage);
                        intent.putExtra("judul", mTitle);
                        intent.putExtra("deskripsi", mDesc);
                        startActivity(intent);
                    }

                    @Override
                    public void onItemLongClick(View view, int position) {

                        final String cTitle = getItem(position).getJudul();
                        final String cImage = getItem(position).getGambar();
                        final String cDesc = getItem(position).getDeskripsi();

                        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                        String[] options = {"Update", "Hapus"};
                        builder.setItems(options, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int which) {
                                if (which  == 0){

                                    Intent intent = new Intent(MainActivity.this, AddPostActivity.class);
                                    intent.putExtra("cTitle", cTitle);
                                    intent.putExtra("cDesc", cDesc);
                                    intent.putExtra("cImage", cImage);
                                    startActivity(intent);

                                }

                                if (which == 1){
                                    showDeleteDataDialog(cTitle, cImage, cDesc);
                                }
                            }
                        });
                        builder.create().show();
                    }
                });

                return viewHolder;
            }
        };

        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        firebaseRecyclerAdapter.startListening();
        mRecyclerView.setAdapter(firebaseRecyclerAdapter);
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ActionBar actionBar = getSupportActionBar();

        actionBar.setTitle("Book List");

        mRecyclerView = findViewById(R.id.recyclerView);
        mRecyclerView.setHasFixedSize(true);



        mFirebaseDatabase = FirebaseDatabase.getInstance();
        mRef = mFirebaseDatabase.getReference("Data");

        showData();

    }

    protected void onStart(){
        super.onStart();
       if (firebaseRecyclerAdapter != null){
           firebaseRecyclerAdapter.startListening();
       }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        MenuItem item = menu.findItem(R.id.action_search);
        SearchView searchView = (SearchView) MenuItemCompat.getActionView(item);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                firebaseSearch(query);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                firebaseSearch(newText);
                return false;
            }
        });
        return super.onCreateOptionsMenu(menu);

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_settings){
            //TODO
            return true;
        }

        if (id == R.id.action_add){
            startActivity(new Intent(MainActivity.this, AddPostActivity.class));

            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
