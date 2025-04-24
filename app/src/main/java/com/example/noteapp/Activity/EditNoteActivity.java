package com.example.noteapp.Activity;

import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.noteapp.R;
import com.example.noteapp.databinding.ActivityEditNoteBinding;
import com.example.noteapp.Model.Note;
import com.example.noteapp.RoomDataBase.NotesDatabase;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

public class EditNoteActivity extends AppCompatActivity {

    private ActivityEditNoteBinding binding;
    private Note currentNote;
    private NotesDatabase database;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityEditNoteBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        handleBackPressBehavior();

        database = NotesDatabase.getInstance(this);
        currentNote = (Note) getIntent().getSerializableExtra("note");

        if (currentNote == null) {
            Toast.makeText(this, "Note not found", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        populateNoteFields();

        binding.updateTxt.setOnClickListener(v -> updateNote());
    }

    private void populateNoteFields() {
        binding.titleEt.setText(currentNote.getNote_title());
        binding.contentTxt.setText(currentNote.getNote_content());
    }

    private void updateNote() {
        String title = binding.titleEt.getText().toString().trim();
        String content = binding.contentTxt.getText().toString().trim();

        if (title.isEmpty() || content.isEmpty()) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        currentNote.setNote_title(title);
        currentNote.setNote_content(content);

        database.getNoteDao().updateNote(currentNote)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(() -> {
                    Toast.makeText(this, "Note updated", Toast.LENGTH_SHORT).show();
                    finish();
                }, throwable -> {
                    Toast.makeText(this, "Error updating note", Toast.LENGTH_SHORT).show();
                    throwable.printStackTrace();
                });
    }

    private void handleBackPressBehavior() {
        OnBackPressedCallback onBackPressedCallback = new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                finish();
            }
        };
        getOnBackPressedDispatcher().addCallback(this, onBackPressedCallback);

        binding.backLayout.setOnClickListener(view -> {
            onBackPressedCallback.handleOnBackPressed();
        });
    }
}