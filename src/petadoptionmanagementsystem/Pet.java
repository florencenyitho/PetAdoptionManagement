package petadoptionmanagementsystem;

/**
 * Model class representing a pet.
 * Includes an optional photo_path — an absolute path to an image file on disk.
 */
public class Pet {

    private int     petId;
    private String  name;
    private String  species;
    private String  breed;
    private int     age;
    private String  gender;
    private String  description;
    private boolean available;
    private String  dateAdded;
    private String  photoPath;   // nullable — full path to image file

    public Pet() {}

    public Pet(int petId, String name, String species, String breed,
               int age, String gender, String description,
               boolean available, String dateAdded, String photoPath) {
        this.petId       = petId;
        this.name        = name;
        this.species     = species;
        this.breed       = breed;
        this.age         = age;
        this.gender      = gender;
        this.description = description;
        this.available   = available;
        this.dateAdded   = dateAdded;
        this.photoPath   = photoPath;
    }

    // ── Getters & Setters ─────────────────────────────────────────────────────

    public int     getPetId()                        { return petId; }
    public void    setPetId(int petId)               { this.petId = petId; }

    public String  getName()                         { return name; }
    public void    setName(String name)              { this.name = name; }

    public String  getSpecies()                      { return species; }
    public void    setSpecies(String species)        { this.species = species; }

    public String  getBreed()                        { return breed; }
    public void    setBreed(String breed)            { this.breed = breed; }

    public int     getAge()                          { return age; }
    public void    setAge(int age)                   { this.age = age; }

    public String  getGender()                       { return gender; }
    public void    setGender(String gender)          { this.gender = gender; }

    public String  getDescription()                  { return description; }
    public void    setDescription(String d)          { this.description = d; }

    public boolean isAvailable()                     { return available; }
    public void    setAvailable(boolean available)   { this.available = available; }

    public String  getDateAdded()                    { return dateAdded; }
    public void    setDateAdded(String dateAdded)    { this.dateAdded = dateAdded; }

    public String  getPhotoPath()                    { return photoPath; }
    public void    setPhotoPath(String photoPath)    { this.photoPath = photoPath; }

    @Override
    public String toString() { return name + " (" + species + ")"; }
}
