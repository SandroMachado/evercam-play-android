package io.evercam.androidapp.dto;

import io.evercam.EvercamException;
import io.evercam.User;

public class AppUser
{
    private int id;
    private String email = "";
    private String username = "";
    private String country = "";
    private String firstName = "";
    private String lastName = "";
    private boolean isDefault = false;
    private String apiKey = "";
    private String apiId = "";

    public AppUser()
    {

    }

    public AppUser(User user) throws EvercamException
    {
        setUsername(user.getUsername());
        setEmail(user.getEmail());
        setFirstName(user.getFirstName());
        setLastName(user.getLastName());
        setCountry(user.getCountry());
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

    public int getId()
    {
        return id;
    }

    public String getEmail()
    {
        return email;
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

    public void setApiKeyPair(String apiKey, String apiId)
    {
        this.apiKey = apiKey;
        this.apiId = apiId;
    }

    public void setIsDefault(boolean isDefault)
    {
        this.isDefault = isDefault;
    }

    public String getFirstName()
    {
        return firstName;
    }

    public void setFirstName(String firstName)
    {
        this.firstName = firstName;
    }

    public String getLastName()
    {
        return lastName;
    }

    public void setLastName(String lastName)
    {
        this.lastName = lastName;
    }

    @Override
    public String toString()
    {
        return "AppUser{" +
                "id=" + id +
                ", email='" + email + '\'' +
                ", username='" + username + '\'' +
                ", country='" + country + '\'' +
                ", firstName='" + firstName + '\'' +
                ", lastName='" + lastName + '\'' +
                ", isDefault=" + isDefault +
                ", apiKey='" + apiKey + '\'' +
                ", apiId='" + apiId + '\'' +
                '}';
    }
}
