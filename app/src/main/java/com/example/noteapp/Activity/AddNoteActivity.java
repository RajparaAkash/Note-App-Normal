package com.example.noteapp.Activity;

import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.room.Room;

import com.example.noteapp.R;
import com.example.noteapp.databinding.ActivityAddNoteBinding;
import com.example.noteapp.Model.Note;
import com.example.noteapp.RoomDataBase.NotesDatabase;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Random;

import io.noties.markwon.Markwon;
import io.noties.markwon.editor.MarkwonEditor;
import io.noties.markwon.editor.MarkwonEditorTextWatcher;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;

public class AddNoteActivity extends AppCompatActivity {

    private ActivityAddNoteBinding binding;
    private NotesDatabase database;
    private final CompositeDisposable compositeDisposable = new CompositeDisposable();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAddNoteBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        initViews();
        initDatabase();
        setupMarkdownEditor();
        setupListeners();
        handleBackPress();
    }

    private void initViews() {
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    private void initDatabase() {
        database = NotesDatabase.getInstance(getApplicationContext());
    }

    private void setupMarkdownEditor() {
        Markwon markwon = Markwon.create(this);
        binding.contentTxt.addTextChangedListener(MarkwonEditorTextWatcher.withProcess(
                MarkwonEditor.create(markwon)
        ));
    }

    private void setupListeners() {
        binding.saveTxt.setOnClickListener(v -> saveNote());
    }

    private void saveNote() {
        String title = binding.titleEt.getText().toString().trim();
        String content = binding.contentTxt.getText().toString().trim();

        if (title.isEmpty() || content.isEmpty()) {
            Toast.makeText(this, "Title and content cannot be empty", Toast.LENGTH_SHORT).show();
            return;
        }

        String createdAt = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date());
        int[] colors = getResources().getIntArray(R.array.colors);
        int color = colors[new Random().nextInt(colors.length)];

        Note note = new Note(0, title, content, createdAt, color);

        compositeDisposable.add(
                database.getNoteDao().addNote(note)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(() -> {
                            Toast.makeText(this, "Note saved", Toast.LENGTH_SHORT).show();
                            finish();
                        }, throwable -> {
                            Toast.makeText(this, "Failed to save note", Toast.LENGTH_SHORT).show();
                            Log.e("AddNoteActivity", "Save failed", throwable);
                        })
        );
    }

    private void handleBackPress() {
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

    @Override
    protected void onDestroy() {
        super.onDestroy();
        compositeDisposable.clear();
    }
}