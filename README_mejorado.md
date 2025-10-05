# 🗺️ GeoNotesTeaching (Java 21)

Proyecto docente desarrollado como parte de la práctica **GeoNotesTeaching**.  
Implementa una pequeña aplicación de consola (CLI) para gestionar notas geográficas con soporte de exportación a distintos formatos.

---

## ✅ Ejercicios realizados

El proyecto se ha completado **hasta el bloque C (inclusive C1 y C2)** según la hoja de ejercicios del documento *GeonotesTarea (3).pdf*.

### 🔹 Bloque A — Fundamentos y calentamiento
**A1. Validación y excepciones**
- En `Note`, se añadieron validaciones:
  - `title`: mínimo 3 caracteres, no nulo ni vacío.
  - `content`: se limpia con `trim()` y, si queda vacío, se sustituye por "`-`".
  - `location` y `createdAt`: se validan como no nulos.
- En `GeoNotes` (CLI), se maneja la excepción con mensajes claros al usuario.

**A2. Equals/HashCode vs. Records**
- Se creó `LegacyPoint`, clase clásica con `equals`, `hashCode` y `toString` manuales.
- Se comparó con `GeoPoint`, que es un *record* inmutable.
  - Ventajas de `record`: menos código, inmutabilidad, `equals`/`hashCode` automáticos.
  - Cuándo no usar `record`: cuando se necesita mutabilidad o herencia.

---

### 🔹 Bloque B — Jerarquía *sealed* y *switch* moderno
**B1. Nuevo subtipo: `Video`**
- Se añadió `public record Video(String url, int width, int height, int seconds) implements Attachment`.
- `Attachment` se amplió para permitir `Video`.
- En `Describe.describeAttachment`, se añadieron los casos:
  ```java
  case Video v when v.seconds() > 120 -> "🎥 Video largo";
  case Video v -> "🎥 Video";
  ```

**B2. Uso de `yield` con bloques**
- En `Describe`, el caso de `Audio` largo usa un *switch expression* con `yield`:
  ```java
  case Audio audio when audio.duration() > 300 -> {
      var mins = audio.duration() / 60;
      yield "🎵 Audio (" + mins + " min)";
  }
  ```
- Se comprobó que el *switch* es exhaustivo para todos los subtipos de `Attachment`.

---

### 🔹 Bloque C — *Text Blocks* y exportación
**C1. Export JSON pretty**
- En `Timeline.Render.export()`:
  - Se mejoró la legibilidad del *JSON* usando *text blocks* (Java 15+).
  - Se mantiene indentación uniforme.
  - Se formatean correctamente los campos, generando salida como:
    ```json
    { "notes": [
      {
        "id": 1,
        "title": "Cádiz",
        "content": "Playita",
        "location": { "lat": 36.529700, "lon": -6.292700 },
        "createdAt": "2025-09-25T12:00:00Z"
      }
    ] }
    ```

**C2. Export Markdown**
- Se creó la clase `MarkdownExporter` (implementa `Exporter`):
  - Usa `Streams` y `Comparator` para ordenar las notas por fecha descendente.
  - Formato de salida:
    ```markdown
    - [ID 1] "Cádiz" — (36.529700, -6.292700) — 2025-09-25
    - [ID 2] "Sevilla" — (37.382600, -5.996300) — 2025-09-25
    ```
- Se añadió una opción en la CLI para exportar Markdown desde el menú principal.

---

### 🔹 Bloque D — Listar últimas N notas (Streams y Comparator)

**D1. Orden por fecha y límite**
- En `Timeline` se añadió el método:
  public java.util.List<Note> latest(int n) { ... }
  que devuelve las `n` notas más recientes según `createdAt` descendente.
- En la CLI (`GeoNotes`) se añadió la opción **7. Listar últimas N notas**:
  - Solicita al usuario cuántas notas quiere ver.
  - Muestra la información básica de cada nota (`ID`, `title`, `content`, `createdAt`).
- Internamente se usan **Streams**, **Comparator** y **limit()** para obtener las notas más recientes de forma concisa y eficiente.
