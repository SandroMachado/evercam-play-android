package io.evercam.android.dto;

public class AppUser
{
	private int id;
	private String email;
	private String username;
	private String password;
	private String country;
	private boolean isDefault;
	private String apiKey;
	private String apiId;

	public AppUser()
	{

	}

	public AppUser(int id, String email, String username, String password, String apiKey, String apiId,String country,
			 boolean isDefault)
	{
		this.id = id;
		this.email = email;
		this.username = username;
		this.password = password;
		this.country = country;
		this.apiKey = apiKey;
		this.apiId = apiId;
		this.isDefault = isDefault;
	}

	public AppUser(int id, String email,String username, String password, String apiKey, String apiId,String country,
			int isDefaultInteger)
	{
		this.id = id;
		this.email = email;
		this.username = username;
		this.password = password;
		this.country = country;
		this.apiKey = apiKey;
		this.apiId = apiId;
		this.isDefault = (isDefaultInteger == 1);
	}

	public String getUsername()
	{
		return username;
	}

	public String getCountry()
	{
		return country;
	}

	public String getApiId()
	{
		return apiId;
	}

	public void setUsername(String username)
	{
		this.username = username;
	}

	public void setCountry(String country)
	{
		this.country = country;
	}

	public void setApiId(String apiId)
	{
		this.apiId = apiId;
	}

	public int getId()
	{
		return id;
	}

	public String getEmail()
	{
		return email;
	}

	public String getPassword()
	{
		return password;
	}

	public String getApiKey()
	{
		return apiKey;
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

	public void setEmail(String email)
	{
		this.email = email;
	}

	public void setPassword(String password)
	{
		this.password = password;
	}

	public void setApiKey(String apiKey)
	{
		this.apiKey = apiKey;
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
		return "id[" + id + "], email [" + email + "], password [" + password
				+ "], isDefault [" + isDefault + "]";
	}

	@Override
	public String toString()
	{
		return "id[" + id + "], email [" + email + "], password [" + password
				+ "], isDefault [" + isDefault + "]";
	}

}
