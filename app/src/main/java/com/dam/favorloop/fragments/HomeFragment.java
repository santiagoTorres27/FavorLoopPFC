package com.dam.favorloop.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.dam.favorloop.LoopDetail;
import com.dam.favorloop.R;
import com.dam.favorloop.adapters.LoopAdapter;
import com.dam.favorloop.model.Loop;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class HomeFragment extends Fragment {

    public static final String CLAVE_LOOP = "LOOP";

    RecyclerView rvLoop;
    LoopAdapter loopAdapter;
    List<Loop> loopList;
    LinearLayout alertaHome;
    private List<String> followingList;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        alertaHome = view.findViewById(R.id.alertaHome);
        rvLoop = view.findViewById(R.id.rvLoop);
        rvLoop.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);
        rvLoop.setLayoutManager(linearLayoutManager);
        loopList = new ArrayList<>();
        loopAdapter = new LoopAdapter(getContext(), loopList);
        rvLoop.setAdapter(loopAdapter);

        checkFollowing();
        return view;
    }

    private void checkFollowing() {
        followingList = new ArrayList<>();

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Conexiones")
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                .child("siguiendo");

        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                followingList.clear();
                for (DataSnapshot snap : snapshot.getChildren()) {
                    followingList.add(snap.getKey());
                }

                readPosts();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void readPosts() {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Loops");

        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                loopList.clear();
                for (DataSnapshot snap : snapshot.getChildren()) {
                    Loop loop = snap.getValue(Loop.class);
                    for (String id : followingList) {
                        if (loop.getPublisher().equals(id)) {
                            loopList.add(loop);
                        }
                    }
                }

                if (!loopList.isEmpty()) {
                    alertaHome.setVisibility(View.INVISIBLE);
                }

                loopAdapter.setListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        int i = rvLoop.getChildAdapterPosition(v);
                        Loop loop = loopList.get(i);

                        Intent intentDatos = new Intent(getContext(), LoopDetail.class);
                        intentDatos.putExtra(CLAVE_LOOP, loop);
                        intentDatos.putExtra("USER", loop.getPublisher());
                        startActivity(intentDatos);

                    }
                });
                loopAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
    }

}