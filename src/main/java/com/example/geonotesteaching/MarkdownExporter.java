package com.example.geonotesteaching;

import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

public final class MarkdownExporter implements Exporter {

    private final Map<Long, Note> notes = new LinkedHashMap<>();

    public Map<Long, Note> getNotes() {
        return notes;
    }

    public void addNote(Note note) {
        notes.put(note.id(), note);
    }

    public Note getNote(long id) {
        return notes.get(id);
    }


    @Override
    public String export() {
        // - [ID 1] Título — (lat, lon) — YYYY-MM-DD
        return notes.values().stream()
            .sorted(Comparator.comparing(Note::createdAt).reversed())
            .map(note -> String.format(
                "- [ID %d] \"%s\" — (%.6f, %.6f) — %s",
                note.id(),
                note.title(),
                note.location().lat(),
                note.location().lon(),
                note.createdAt().toString().substring(0, 10) // YYYY-MM-DD
            ))
            .collect(Collectors.joining("\n"));
    }
}
