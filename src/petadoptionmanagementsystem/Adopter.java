package petadoptionmanagementsystem;

/**
 * Model class representing an adopter.
 */
public class Adopter {

    private int    adopterId;
    private String name;
    private String phone;
    private String email;
    private String address;
    private String dateRegistered;

    public Adopter() {}

    public Adopter(int adopterId, String name, String phone,
                   String email, String address, String dateRegistered) {
        this.adopterId      = adopterId;
        this.name           = name;
        this.phone          = phone;
        this.email          = email;
        this.address        = address;
        this.dateRegistered = dateRegistered;
    }

    // ---------- Getters & Setters ----------

    public int getAdopterId()                    { return adopterId; }
    public void setAdopterId(int adopterId)      { this.adopterId = adopterId; }

    public String getName()                      { return name; }
    public void setName(String name)             { this.name = name; }

    public String getPhone()                     { return phone; }
    public void setPhone(String phone)           { this.phone = phone; }

    public String getEmail()                     { return email; }
    public void setEmail(String email)           { this.email = email; }

    public String getAddress()                   { return address; }
    public void setAddress(String address)       { this.address = address; }

    public String getDateRegistered()                      { return dateRegistered; }
    public void setDateRegistered(String dateRegistered)   { this.dateRegistered = dateRegistered; }

    @Override
    public String toString() {
        return name + " - " + phone;
    }
}
