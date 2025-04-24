package com.example.noteapp.RoomDataBase;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.example.noteapp.Model.Note;

import java.util.List;

import io.reactivex.Completable;
import io.reactivex.Single;

@Dao
public interface NoteDao {

    @Query("SELECT * FROM notes ORDER BY note_id DESC")
    Single<List<Note>> getNotes();

    @Query("SELECT * FROM notes WHERE note_id = :note_id")
    Single<Note> getNoteDetails(int note_id);

    @Query("SELECT * FROM notes WHERE note_id = :noteId LIMIT 1")
    Single<Note> getNoteById(int noteId);

    @Insert
    Completable addNote(Note note);

    @Update
    Completable updateNote(Note note);

    @Delete
    Completable deleteNote(Note note);

    @Query("DELETE FROM notes WHERE note_id = :noteId")
    void deleteNoteById(int noteId);
}
