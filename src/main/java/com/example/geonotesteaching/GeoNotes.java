package com.example.geonotesteaching;

import java.time.Instant;
import java.util.Scanner;

/*
 * GeoNotes — Clase principal con una CLI sencilla.
 *
 * COSAS A FIJARSE (Java 11 → 21):
 * - Java 11: API estándar consolidada; aquí usamos Instant (java.time) para fechas.
 * - Java 14: "switch expressions" con flechas (->) y posibilidad de yield en bloques (en este archivo usamos la forma simple; ver Describe para más).
 * - Java 15: "Text Blocks" (""" ... """) — se usan en Timeline.Render para generar JSON multilínea.
 * - Java 16: "records" (GeoPoint, Note, etc.) — clases inmutables concisas con constructor canónico, equals/hashCode/toString.
 * - Java 17: "sealed classes/interfaces" (Attachment) — jerarquías cerradas que el compilador puede verificar exhaustivamente.
 * - Java 17: pattern matching para instanceof (lo verás en Describe).
 * - Java 21: "record patterns" (lo verás en Match donde se desestructura un record directamente en un switch/if).
 * - Java 21: "Sequenced Collections" (Timeline podría usar SequencedMap/LinkedHashMap.reversed(); aquí mostramos el enfoque clásico, pero coméntalo en clase).
 * - Java 21: "Virtual Threads" (demo aparte en el proyecto moderno; no se usan aquí).
 */
public class GeoNotes {

    /*
     * timeline es el "modelo" de la aplicación: guarda las notas en memoria.
     * Timeline tiene una inner class no estática (Render) que sabe exportar el contenido a JSON con Text Blocks.
     * -> OJO: inner class no estática = necesita una instancia externa para crearse (ver exportNotesToJson()).
     */
    private static final Timeline timeline = new Timeline();

    /*
     * Scanner para leer del stdin. Mantener uno único y reutilizarlo es buena práctica para la CLI.
     */
    private static final Scanner scanner = new Scanner(System.in);

    /*
     * Generador simple de IDs. En un proyecto real, probablemente usarías UUID o una secuencia persistente.
     */
    private static long noteCounter = 1;

    // La clase principal del programa. Contiene el menú interactivo para la CLI.
    public static void main(String[] args) {
        /*
         * Modo "examples":
         * Gradle define una tarea 'examples' que invoca main con el argumento "examples".
         * Útil para mostrar rápidamente la salida JSON sin teclear en la CLI.
         */
        if (args != null && args.length > 0 && "examples".equalsIgnoreCase(args[0])) {
            seedExamples();
            exportNotesToJson();
            return;
        }
        System.out.println("--------------------------------------");
        System.out.println("  📝 Bienvenid@ a la aplicación GeoNotes");
        System.out.println("--------------------------------------");
        boolean running = true;
        while (running) {
            printMenu();
            try {
                int choice = Integer.parseInt(scanner.nextLine().trim());
                switch (choice) {
                    case 1 -> createNote();
                    case 2 -> listNotes();
                    case 3 -> filterNotes();
                    case 4 -> exportNotesToJson();
                    case 5 -> exportNotesToMarkdown();
                    case 6 -> running = false;
                    case 7 -> listLatestNotes(); // D1
                    case 8 -> advancedSearch();  // ✅ D2
                    default -> System.out.println("❌ Opción no válida. Inténtalo de nuevo.");
                }
            } catch (NumberFormatException e) {
                System.out.println("❌ Entrada no válida. Por favor, ingresa un número.");
            }
        }
        System.out.println("¡Gracias por usar GeoNotes! 👋");
    }

    private static void printMenu() {
        System.out.println("\n--- Menú ---");
        System.out.println("1. Crear una nueva nota");
        System.out.println("2. Listar todas las notas");
        System.out.println("3. Filtrar notas por palabra clave");
        System.out.println("4. Exportar notas a JSON (Text Blocks)");
        System.out.println("5. Exportar notas a Markdown");
        System.out.println("6. Salir");
        System.out.println("7. Listar últimas N notas"); // D1
        System.out.println("8. Búsqueda avanzada");       // ✅ D2
        System.out.print("Elige una opción: ");
    }

    private static void createNote() {
        System.out.println("\n--- Crear una nueva nota ---");
        System.out.print("Título: ");
        var title = scanner.nextLine();
        System.out.print("Contenido: ");
        var content = scanner.nextLine();
        System.out.print("Latitud: ");
        var lat = Double.parseDouble(scanner.nextLine());
        System.out.print("Longitud: ");
        var lon = Double.parseDouble(scanner.nextLine());
        try {
            var geoPoint = new GeoPoint(lat, lon);
            var note = new Note(noteCounter++, title, content, geoPoint, Instant.now(), null);
            timeline.addNote(note);
            System.out.println("✅ Nota creada con éxito.");
        } catch (IllegalArgumentException e) {
            System.out.println("❌ Error: " + e.getMessage());
        }
    }

    private static void listNotes() {
        System.out.println("\n--- Notas disponibles ---");
        if (timeline.getNotes().isEmpty()) {
            System.out.println("No hay notas creadas.");
            return;
        }
        timeline.getNotes().forEach((id, note) -> {
            var gp = note.location();
            var region = Match.where(gp);
            var attachmentInfo = (note.attachment() == null)
                    ? "—"
                    : Describe.describeAttachment(note.attachment());
            System.out.printf("ID: %d | %s | %s | loc=%s | adj=%s%n",
                    id, note.title(), note.content(), region, attachmentInfo);
        });
    }

    private static void filterNotes() {
        System.out.print("\nIntroduce la palabra clave para filtrar: ");
        var keyword = scanner.nextLine();
        System.out.println("\n--- Resultados de búsqueda ---");
        var filtered = timeline.getNotes().values().stream()
                .filter(n -> n.title().contains(keyword) || n.content().contains(keyword))
                .toList();
        if (filtered.isEmpty()) {
            System.out.println("No se encontraron notas con: " + keyword);
            return;
        }
        filtered.forEach(n -> System.out.printf("ID: %d | %s | %s%n",
                n.id(), n.title(), n.content()));
    }

    private static void exportNotesToJson() {
        var renderer = timeline.new Render();
        String json = renderer.export();
        System.out.println("\n--- Exportando notas a JSON ---");
        System.out.println(json);
    }

    public static void exportNotesToMarkdown() {
        MarkdownExporter exporter = new MarkdownExporter();
        timeline.getNotes().values().forEach(exporter::addNote);
        String markdown = exporter.export();
        System.out.println("\n--- Exportando notas a Markdown ---");
        System.out.println(markdown);
    }

    private static void listLatestNotes() {
        System.out.print("\n¿Cuántas notas recientes quieres ver? ");
        try {
            int n = Integer.parseInt(scanner.nextLine());
            var latestNotes = timeline.latest(n);
            if (latestNotes.isEmpty()) {
                System.out.println("No hay notas en la línea temporal.");
                return;
            }
            System.out.println("\n--- Últimas " + n + " notas ---");
            latestNotes.forEach(note -> System.out.printf(
                    "ID: %d | %s | %s | Fecha: %s%n",
                    note.id(), note.title(), note.content(), note.createdAt().toString()));
        } catch (NumberFormatException e) {
            System.out.println("❌ Entrada no válida. Por favor, ingresa un número.");
        }
    }

    // ✅ D2: Búsqueda avanzada
    private static void advancedSearch() {
        try {
            System.out.print("\nLatitud mínima: ");
            double latMin = Double.parseDouble(scanner.nextLine());
            System.out.print("Latitud máxima: ");
            double latMax = Double.parseDouble(scanner.nextLine());
            System.out.print("Longitud mínima: ");
            double lonMin = Double.parseDouble(scanner.nextLine());
            System.out.print("Longitud máxima: ");
            double lonMax = Double.parseDouble(scanner.nextLine());
            System.out.print("Palabra clave (title/content, opcional): ");
            String keyword = scanner.nextLine().trim();

            GeoArea area = new GeoArea(new GeoPoint(latMin, lonMin), new GeoPoint(latMax, lonMax));

            var results = timeline.getNotes().values().stream()
                    .filter(n -> Match.isInArea(n.location(), area))
                    .filter(n -> keyword.isEmpty() || n.title().contains(keyword) || n.content().contains(keyword))
                    .toList();

            System.out.println("\n--- Resultados de búsqueda avanzada ---");
            if (results.isEmpty()) {
                System.out.println("No se encontraron notas que cumplan los criterios.");
                return;
            }
            results.forEach(note -> System.out.printf("ID: %d | %s | %s | loc=(%.6f, %.6f) | Fecha: %s%n",
                    note.id(), note.title(), note.content(),
                    note.location().lat(), note.location().lon(),
                    note.createdAt().toString()));
        } catch (NumberFormatException e) {
            System.out.println("❌ Entrada no válida. Por favor, ingresa números correctos para lat/lon.");
        } catch (IllegalArgumentException e) {
            System.out.println("❌ Error en coordenadas: " + e.getMessage());
        }
    }

    private static void seedExamples() {
        timeline.addNote(new Note(noteCounter++, "Cádiz", "Playita",
                new GeoPoint(36.5297, -6.2927),
                Instant.now(),
                new Photo("u", 2000, 1000)));

        timeline.addNote(new Note(noteCounter++, "Sevilla", "Triana",
                new GeoPoint(37.3826, -5.9963),
                Instant.now(),
                new Audio("a", 320)));

        timeline.addNote(new Note(noteCounter++, "Córdoba", "Mezquita",
                new GeoPoint(37.8790, -4.7794),
                Instant.now(),
                new Link("http://cordoba", "Oficial")));        
    }
}
