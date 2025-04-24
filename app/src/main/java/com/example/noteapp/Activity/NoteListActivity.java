package com.example.noteapp.Activity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import com.example.noteapp.R;
import com.example.noteapp.Adapter.NoteAdapter;
import com.example.noteapp.RoomDataBase.NotesDatabase;
import com.example.noteapp.databinding.ActivityAddNoteBinding;
import com.example.noteapp.databinding.ActivityNoteDetailsBinding;
import com.example.noteapp.databinding.ActivityNoteListBinding;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;

public class NoteListActivity extends AppCompatActivity {

    private ActivityNoteListBinding binding;
    private NoteAdapter noteAdapter;
    private NotesDatabase database;
    private final CompositeDisposable compositeDisposable = new CompositeDisposable();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityNoteListBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        initViews();
        setupListeners();
        database = NotesDatabase.getInstance(getApplicationContext());

        binding.noteListRv.setLayoutManager(new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL));
    }

    private void initViews() {
        binding.noteListRv.setLayoutManager(
                new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL)
        );
    }

    private void setupListeners() {
        binding.addNoteBtn.setOnClickListener(v -> {
            startActivity(new Intent(this, AddNoteActivity.class));
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadNotes();
    }

    private void loadNotes() {
        compositeDisposable.add(
                database.getNoteDao().getNotes()
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(notes -> {
                            if (noteAdapter == null) {
                                noteAdapter = new NoteAdapter(this, notes);
                                binding.noteListRv.setAdapter(noteAdapter);
                            } else {
                                noteAdapter.updateNotes(notes);
                            }
                        }, throwable -> {
                            Toast.makeText(this, "Failed to load notes", Toast.LENGTH_SHORT).show();
                            Log.e("NoteListActivity", "Error loading notes", throwable);
                        })
        );
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        compositeDisposable.clear();
    }
}