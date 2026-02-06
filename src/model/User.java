package model;

/**
 *
 * @author miwa
 */
public class User {
    private int id;
    private String firstName;
    private String lastName;
    private String email;
    private String password;
    private String role;
    private String contact;
    private String status;
    private String gender;
    private String accountID;
    private String schoolLevel;
    private String address;
    private String birthdate;
    
    public User() {
    }
    
    public User(String firstName, String lastName, String email, String password) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.password = password;
        this.role = "student";
    }
    
    public int getId() {
        return id;
    }
    
    public void setId(int id) {
        this.id = id;
    }
    
    public String getFirstName() {
        return firstName;
    }
    
    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }
    
    public String getLastName() {
        return lastName;
    }
    
    public void setLastName(String lastName) {
        this.lastName = lastName;
    }
    
    public String getEmail() {
        return email;
    }
    
    public void setEmail(String email) {
        this.email = email;
    }
    
    public String getPassword() {
        return password;
    }
    
    public void setPassword(String password) {
        this.password = password;
    }
    
    public String getRole() {
        return role;
    }
    
    public void setRole(String role) {
        this.role = role;
    }
    
    public String getFullName() {
        return firstName + " " + lastName;
    }
    
    public String getName() {
        return firstName;
    }
    
    public String getContact() {
        return contact;
    }
    
    public void setContact(String contact) {
        this.contact = contact;
    }
    
    public String getStatus() {
        return status;
    }
    
    public void setStatus(String status) {
        this.status = status;
    }
    
    public String getGender() {
        return gender;
    }
    
    public void setGender(String gender) {
        this.gender = gender;
    }
    
    public String getAccountID() {
        return accountID;
    }
    
    public void setAccountID(String accountID) {
        this.accountID = accountID;
    }
    
    public String getSchoolLevel() {
        return schoolLevel;
    }
    
    public void setSchoolLevel(String schoolLevel) {
        this.schoolLevel = schoolLevel;
    }
    
    public String getAddress() {
        return address;
    }
    
    public void setAddress(String address) {
        this.address = address;
    }
    
    public String getBirthdate() {
        return birthdate;
    }
    
    public void setBirthdate(String birthdate) {
        this.birthdate = birthdate;
    }
}
