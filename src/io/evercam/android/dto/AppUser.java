package io.evercam.android.dto;

public class AppUser
{

	static String TAG = "AppUser";

	// private variables
	private int ID;
	private String UserEmail;
	private String UserPassword;
	private String ApiKey;
	private boolean IsActive;
	private boolean IsDefault;

	// Empty constructor
	public AppUser()
	{

	}

	// constructor
	public AppUser(int _ID, String _UserEmail, String _UserPassword, String _ApiKey,
			boolean _IsActive, boolean _IsDefault)
	{
		this.ID = _ID;
		this.UserEmail = _UserEmail;
		this.UserPassword = _UserPassword;
		ApiKey = _ApiKey;
		this.IsActive = _IsActive;
		this.IsDefault = _IsDefault;
	}

	public AppUser(int _ID, String _UserEmail, String _UserPassword, String _ApiKey,
			int _IsActiveInteger, int _IsDefaultInteger)
	{
		this.ID = _ID;
		this.UserEmail = _UserEmail;
		this.UserPassword = _UserPassword;
		this.ApiKey = _ApiKey;
		this.IsActive = (_IsActiveInteger == 1);
		this.IsDefault = (_IsDefaultInteger == 1);
	}

	public int getID()
	{
		return ID;
	}

	public String getUserEmail()
	{
		return UserEmail;
	}

	public String getUserPassword()
	{
		return UserPassword;
	}

	public String getApiKey()
	{
		return ApiKey;
	}

	public boolean getIsActive()
	{
		return IsActive;
	}

	public int getIsActiveInteger()
	{
		return (IsActive ? 1 : 0);
	}

	public boolean getIsDefault()
	{
		return IsDefault;
	}

	public int getIsDefaultInteger()
	{
		return (IsDefault ? 1 : 0);
	}

	public void setID(int _ID)
	{
		ID = _ID;
	}

	public void setUserEmail(String _UserEmail)
	{
		UserEmail = _UserEmail;
	}

	public void setUserPassword(String _UserPassword)
	{
		UserPassword = _UserPassword;
	}

	public void setApiKey(String _ApiKey)
	{
		ApiKey = _ApiKey;
	}

	public void setIsActive(boolean _IsActive)
	{
		IsActive = _IsActive;
	}

	public void setIsActiveInteger(int _IsActiveInteger)
	{
		IsActive = (_IsActiveInteger == 1);
	}

	public void setIsDefault(boolean _IsDefault)
	{
		IsDefault = _IsDefault;
	}

	public void setIsDefaultInteger(int _IsDefaultInteger)
	{
		IsDefault = (_IsDefaultInteger == 1);
	}

	public String toStringAll()
	{
		return "ID[" + ID + "], UserEmail [" + UserEmail + "], UserPassword [" + UserPassword
				+ "], IsActive [" + IsActive + "], IsDefault [" + IsDefault + "]";
	}

	@Override
	public String toString()
	{
		// return UserEmail + (IsDefault? " - Default" : ""); // for
		// arrayadapter
		return "ID[" + ID + "], UserEmail [" + UserEmail + "], UserPassword [" + UserPassword
				+ "], IsActive [" + IsActive + "], IsDefault [" + IsDefault + "]";
	}

}
