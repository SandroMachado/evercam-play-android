package io.evercam.android.dto;

public class AppUser
{

	private static String TAG = "AppUser";

	private int id;
	private String userEmail;
	private String userPassword;
	private String apiKey;
	private boolean isActive;
	private boolean isDefault;

	public AppUser()
	{

	}

	// constructor
	public AppUser(int id, String userEmail, String userPassword, String apiKey,
			boolean isActive, boolean isDefault)
	{
		this.id = id;
		this.userEmail = userEmail;
		this.userPassword = userPassword;
		this.apiKey = apiKey;
		this.isActive = isActive;
		this.isDefault = isDefault;
	}

	public AppUser(int _id, String _userEmail, String _userPassword, String _apiKey,
			int _isActiveInteger, int _isDefaultInteger)
	{
		this.id = _id;
		this.userEmail = _userEmail;
		this.userPassword = _userPassword;
		this.apiKey = _apiKey;
		this.isActive = (_isActiveInteger == 1);
		this.isDefault = (_isDefaultInteger == 1);
	}

	public int getId()
	{
		return id;
	}

	public String getUserEmail()
	{
		return userEmail;
	}

	public String getUserPassword()
	{
		return userPassword;
	}

	public String getApiKey()
	{
		return apiKey;
	}

	public boolean getIsActive()
	{
		return isActive;
	}

	public int getIsActiveInteger()
	{
		return (isActive ? 1 : 0);
	}

	public boolean getIsDefault()
	{
		return isDefault;
	}

	public int getIsDefaultInteger()
	{
		return (isDefault ? 1 : 0);
	}

	public void setId(int id)
	{
		this.id = id;
	}

	public void setUserEmail(String userEmail)
	{
		this.userEmail = userEmail;
	}

	public void setUserPassword(String userPassword)
	{
		this.userPassword = userPassword;
	}

	public void setApiKey(String apiKey)
	{
		this.apiKey = apiKey;
	}

	public void setIsActive(boolean isActive)
	{
		this.isActive = isActive;
	}

	public void setIsActiveInteger(int isActiveInteger)
	{
		this.isActive = (isActiveInteger == 1);
	}

	public void setIsDefault(boolean isDefault)
	{
		this.isDefault = isDefault;
	}

	public void setisDefaultInteger(int isDefaultInteger)
	{
		this.isDefault = (isDefaultInteger == 1);
	}

	public String toStringAll()
	{
		return "id[" + id + "], userEmail [" + userEmail + "], userPassword [" + userPassword
				+ "], isActive [" + isActive + "], isDefault [" + isDefault + "]";
	}

	@Override
	public String toString()
	{
		// return userEmail + (isDefault? " - Default" : ""); // for
		// arrayadapter
		return "id[" + id + "], userEmail [" + userEmail + "], userPassword [" + userPassword
				+ "], isActive [" + isActive + "], isDefault [" + isDefault + "]";
	}

}
