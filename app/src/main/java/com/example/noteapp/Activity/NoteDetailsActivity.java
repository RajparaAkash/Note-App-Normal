package com.example.noteapp.Activity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.noteapp.R;
import com.example.noteapp.databinding.ActivityNoteDetailsBinding;
import com.example.noteapp.Model.Note;
import com.example.noteapp.RoomDataBase.NotesDatabase;
import com.google.android.material.snackbar.Snackbar;

import io.reactivex.Completable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

public class NoteDetailsActivity extends AppCompatActivity {

    private ActivityNoteDetailsBinding binding;
    private NotesDatabase database;
    private Note note;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityNoteDetailsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        handleBackPressBehavior();

        database = NotesDatabase.getInstance(this);
        int noteId = getIntent().getIntExtra("note_id", -1);

        if (noteId == -1) {
            Toast.makeText(this, "Invalid note ID", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        loadNoteDetails(noteId);
        setupActionListeners();
    }

    private void loadNoteDetails(int noteId) {
        database.getNoteDao().getNoteById(noteId)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(fetchedNote -> {
                    this.note = fetchedNote;
                    binding.titleTxt.setText(note.getNote_title());
                    binding.dateTxt.setText(note.getCreated_at());
                    binding.contentTxt.setText(note.getNote_content());
                }, throwable -> {
                    Toast.makeText(this, "Error loading note", Toast.LENGTH_SHORT).show();
                    finish();
                });
    }

    private void setupActionListeners() {
        binding.deleteNoteLay.setOnClickListener(view ->
                Snackbar.make(view, getString(R.string.msg_confirm_delete), Snackbar.LENGTH_LONG)
                        .setAction(getString(R.string.btn_yes), v -> deleteNote())
                        .show()
        );

        binding.editNoteLay.setOnClickListener(v -> {
            if (note != null) {
                Intent intent = new Intent(this, EditNoteActivity.class);
                intent.putExtra("note", note);
                startActivity(intent);
            }
        });
    }

    private void deleteNote() {
        if (note != null) {
            Completable.fromAction(() -> database.getNoteDao().deleteNoteById(note.getNote_id()))
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(() -> {
                                Toast.makeText(this, "Note Deleted", Toast.LENGTH_SHORT).show();
                                finish();
                            }, throwable ->
                                    Toast.makeText(this, "Failed to delete note", Toast.LENGTH_SHORT).show()
                    );
        }
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