package io.evercam.androidapp.account;

import android.net.Uri;

import java.util.ArrayList;
import java.util.List;

public class UserProfile
{
    /**
     * Adds an email address to the list of possible email addresses for the
     * user
     *
     * @param email the possible email address
     */
    public void addPossibleEmail(String email)
    {
        addPossibleEmail(email, false);
    }

    /**
     * Adds an email address to the list of possible email addresses for the
     * user. Retains information about whether this email address is the primary
     * email address of the user.
     *
     * @param email      the possible email address
     * @param is_primary whether the email address is the primary email address
     */
    public void addPossibleEmail(String email, boolean is_primary)
    {
        if(email == null) return;
        if(is_primary)
        {
            _primary_email = email;
            _possible_emails.add(email);
        }
        else _possible_emails.add(email);
    }

    /**
     * Adds a name to the list of possible names for the user.
     *
     * @param name the possible name
     */
    public void addPossibleName(String name)
    {
        if(name != null) _possible_names.add(name);
    }

    /**
     * Adds a phone number to the list of possible phone numbers for the user.
     *
     * @param phone_number the possible phone number
     */
    public void addPossiblePhoneNumber(String phone_number)
    {
        if(phone_number != null) _possible_phone_numbers.add(phone_number);
    }

    /**
     * Adds a phone number to the list of possible phone numbers for the user.
     * Retains information about whether this phone number is the primary phone
     * number of the user.
     *
     * @param phone_number the possible phone number
     * @param is_primary   whether the phone number is teh primary phone number
     */
    public void addPossiblePhoneNumber(String phone_number, boolean is_primary)
    {
        if(phone_number == null) return;
        if(is_primary)
        {
            _primary_phone_number = phone_number;
            _possible_phone_numbers.add(phone_number);
        }
        else _possible_phone_numbers.add(phone_number);
    }

    /**
     * Sets the possible photo for the user.
     *
     * @param photo the possible photo
     */
    public void addPossiblePhoto(Uri photo)
    {
        if(photo != null) _possible_photo = photo;
    }

    /**
     * Retrieves the list of possible email addresses.
     *
     * @return the list of possible email addresses
     */
    public List<String> possibleEmails()
    {
        return _possible_emails;
    }

    /**
     * Retrieves the list of possible names.
     *
     * @return the list of possible names
     */
    public List<String> possibleNames()
    {
        return _possible_names;
    }

    /**
     * Retrieves the list of possible phone numbers
     *
     * @return the list of possible phone numbers
     */
    public List<String> possiblePhoneNumbers()
    {
        return _possible_phone_numbers;
    }

    /**
     * Retrieves the possible photo.
     *
     * @return the possible photo
     */
    public Uri possiblePhoto()
    {
        return _possible_photo;
    }

    /**
     * Retrieves the primary email address.
     *
     * @return the primary email address
     */
    public String primaryEmail()
    {
        return _primary_email;
    }

    /**
     * Retrieves the primary phone number
     *
     * @return the primary phone number
     */
    public String primaryPhoneNumber()
    {
        return _primary_phone_number;
    }

    /**
     * The primary email address
     */
    private String _primary_email;
    /**
     * The primary name
     */
    private String _primary_name;
    /**
     * The primary phone number
     */
    private String _primary_phone_number;
    /**
     * A list of possible email addresses for the user
     */
    private List<String> _possible_emails = new ArrayList<String>();
    /**
     * A list of possible names for the user
     */
    private List<String> _possible_names = new ArrayList<String>();
    /**
     * A list of possible phone numbers for the user
     */
    private List<String> _possible_phone_numbers = new ArrayList<String>();
    /**
     * A possible photo for the user
     */
    private Uri _possible_photo;
}