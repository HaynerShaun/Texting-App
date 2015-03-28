
public class Contact{
	private String firstName;
	private String middleName;
	private String lastName;
	private String phoneNumber;

	public Contact(String firstName, String middleName, String lastName, String phoneNumber){
		this.firstName = firstName;
		this.middleName = middleName;
		this.lastName = lastName;
		this.phoneNumber = phoneNumber;
	}

	//Getters for contact information
	public String getFirstName(){
		return firstName;
	}
	
	public String getMiddleName(){
		return middleName;
	}
	
	public String getLastName(){
		return lastName;
	}
	
	public String getPhoneNumber(){
		return phoneNumber;
	}
	
	//Setters for contact information
	public void setFirstName(String firstName){
		this.firstName = firstName;
	}
	
	public void setMiddleName(String middleName){
		this.middleName = middleName;
	}
	
	public void setLastName(String lastName){
		this.lastName = lastName;
	}
	
	public void setPhoneNumber(String phoneNumber){
		this.phoneNumber = phoneNumber;
	}
	
	public String toString(){
		return "\n" + "First Name: " + firstName + "\n"
				+ "Middle Name: " + middleName + "\n"
				+ "Last Name: " + lastName + "\n"
				+ "Phone Number: " + phoneNumber + "\n\n";
	}
}
