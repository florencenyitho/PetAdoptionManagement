# Pet Adoption Management System

A Java Swing desktop application backed by **SQLite** — no database server required.

## Requirements
- Java 17+
- NetBeans IDE 12+
- `sqlite-jdbc-*.jar` (download once, no server install needed)

## Project Setup in NetBeans

### 1. Open the project
File → Open Project → select the `PetAdoptionManagementSystem` folder.

### 2. Add the SQLite JDBC driver
1. Download the latest **sqlite-jdbc** JAR from https://github.com/xerial/sqlite-jdbc/releases
   (e.g. `sqlite-jdbc-3.45.3.0.jar`)
2. Copy the JAR into the `lib/` folder inside the project.
3. In NetBeans: right-click **Libraries** → **Add JAR/Folder** → select the JAR from `lib/`.

### 3. Run
Press **F6** (or Run → Run Project).
- No configuration needed — there is no username, password, or server to configure.
- The database file `pet_adoption.db` is created automatically in the working directory
  (same folder where you run the app from NetBeans).
- On first run, all three tables are created automatically.

---

## Features

| Tab | What you can do |
|-----|----------------|
| 🐶 Pets | Register pets (name, species, breed, age, gender, description), view all, search, edit, delete |
| 👤 Adopters | Register adopters (name, phone, email, address), view all, search, edit, delete |
| 🤝 Adoptions | Record an adoption (links a pet to an adopter with a date), view full history, filter by date range |
| 📊 Reports | Summary statistics, pets grouped by species, monthly adoption counts (last 12 months) |

---

## Database Schema (SQLite)

```sql
pets (
    pet_id       INTEGER PRIMARY KEY AUTOINCREMENT,
    name         TEXT    NOT NULL,
    species      TEXT    NOT NULL,
    breed        TEXT,
    age          INTEGER NOT NULL,
    gender       TEXT,
    description  TEXT,
    available    INTEGER NOT NULL DEFAULT 1,   -- 1=available, 0=adopted
    date_added   TEXT    DEFAULT (datetime('now','localtime'))
)

adopters (
    adopter_id      INTEGER PRIMARY KEY AUTOINCREMENT,
    name            TEXT NOT NULL,
    phone           TEXT NOT NULL,
    email           TEXT,
    address         TEXT,
    date_registered TEXT DEFAULT (datetime('now','localtime'))
)

adoptions (
    adoption_id   INTEGER PRIMARY KEY AUTOINCREMENT,
    pet_id        INTEGER NOT NULL REFERENCES pets(pet_id),
    adopter_id    INTEGER NOT NULL REFERENCES adopters(adopter_id),
    adoption_date TEXT    NOT NULL,
    notes         TEXT
)
```

Adoption recording is transactional: the adoption row is inserted **and** the pet's
`available` flag is set to `0` atomically (rolled back on error).

---

## File Structure

```
PetAdoptionManagementSystem/
├── src/petadoptionmanagementsystem/
│   ├── PetAdoptionManagementSystem.java   ← main / entry point
│   ├── MainFrame.java                     ← JFrame with tabbed panes
│   ├── PetsPanel.java                     ← Pets tab UI
│   ├── AdoptersPanel.java                 ← Adopters tab UI
│   ├── AdoptionsPanel.java                ← Adoptions tab UI
│   ├── ReportsPanel.java                  ← Reports tab UI
│   ├── Pet.java                           ← Pet model
│   ├── Adopter.java                       ← Adopter model
│   ├── Adoption.java                      ← Adoption model
│   ├── PetDAO.java                        ← Pet DB operations
│   ├── AdopterDAO.java                    ← Adopter DB operations
│   ├── AdoptionDAO.java                   ← Adoption DB operations
│   ├── DatabaseConnection.java            ← SQLite JDBC connection singleton
│   └── DatabaseSetup.java                 ← Creates schema on first run
├── lib/                                   ← Place sqlite-jdbc-*.jar here
├── nbproject/                             ← NetBeans project files
├── build.xml                              ← Ant build script
└── manifest.mf
```

## No MySQL Needed

SQLite stores everything in a single file (`pet_adoption.db`) — no server to install,
start, or configure. The file is portable: copy it anywhere and the data comes with it.
