
public class Contact{
	private int clientID;
	private String firstName;
	private String lastName;
	private String phoneNumber;

	public Contact(int clientID, String firstName, String lastName, String phoneNumber){
		this.clientID = clientID;
		this.firstName = firstName;
		this.lastName = lastName;
		this.phoneNumber = phoneNumber;
	}

	//Getters for contact information
	public int getClientID(){
		return clientID;
	}
	public String getFirstName(){
		return firstName;
	}
	
	public String getLastName(){
		return lastName;
	}
	
	public String getPhoneNumber(){
		return phoneNumber;
	}
	
	//Setters for contact information
	public void setClientID(int clientID){
		this.clientID = clientID;
	}
	public void setFirstName(String firstName){
		this.firstName = firstName;
	}
	
	public void setLastName(String lastName){
		this.lastName = lastName;
	}
	
	public void setPhoneNumber(String phoneNumber){
		this.phoneNumber = phoneNumber;
	}
	
	public String toString(){
		return "\n" + "Client ID: " + clientID + "\n"
				+ "First Name: " + firstName + "\n"
				+ "Last Name: " + lastName + "\n"
				+ "Phone Number: " + phoneNumber + "\n\n";
	}
}
