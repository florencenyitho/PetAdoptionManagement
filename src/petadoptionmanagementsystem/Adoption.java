package petadoptionmanagementsystem;

/**
 * Model class representing an adoption record.
 * Includes fee and payment status fields.
 */
public class Adoption {

    private int    adoptionId;
    private int    petId;
    private int    adopterId;
    private String adoptionDate;
    private String notes;
    private double adoptionFee;
    private String paymentStatus;   // "Paid" | "Pending" | "Waived"

    // Joined display fields
    private String petName;
    private String adopterName;
    private String adopterPhone;
    private String petSpecies;

    public Adoption() {}

    public Adoption(int adoptionId, int petId, int adopterId,
                    String adoptionDate, String notes,
                    double adoptionFee, String paymentStatus) {
        this.adoptionId    = adoptionId;
        this.petId         = petId;
        this.adopterId     = adopterId;
        this.adoptionDate  = adoptionDate;
        this.notes         = notes;
        this.adoptionFee   = adoptionFee;
        this.paymentStatus = paymentStatus;
    }

    // ── Getters & Setters ─────────────────────────────────────────────────────

    public int    getAdoptionId()                        { return adoptionId; }
    public void   setAdoptionId(int adoptionId)          { this.adoptionId = adoptionId; }

    public int    getPetId()                             { return petId; }
    public void   setPetId(int petId)                    { this.petId = petId; }

    public int    getAdopterId()                         { return adopterId; }
    public void   setAdopterId(int adopterId)            { this.adopterId = adopterId; }

    public String getAdoptionDate()                      { return adoptionDate; }
    public void   setAdoptionDate(String adoptionDate)   { this.adoptionDate = adoptionDate; }

    public String getNotes()                             { return notes; }
    public void   setNotes(String notes)                 { this.notes = notes; }

    public double getAdoptionFee()                       { return adoptionFee; }
    public void   setAdoptionFee(double adoptionFee)     { this.adoptionFee = adoptionFee; }

    public String getPaymentStatus()                          { return paymentStatus; }
    public void   setPaymentStatus(String paymentStatus)      { this.paymentStatus = paymentStatus; }

    public String getPetName()                           { return petName; }
    public void   setPetName(String petName)             { this.petName = petName; }

    public String getAdopterName()                       { return adopterName; }
    public void   setAdopterName(String adopterName)     { this.adopterName = adopterName; }

    public String getAdopterPhone()                      { return adopterPhone; }
    public void   setAdopterPhone(String adopterPhone)   { this.adopterPhone = adopterPhone; }

    public String getPetSpecies()                        { return petSpecies; }
    public void   setPetSpecies(String petSpecies)       { this.petSpecies = petSpecies; }
}
