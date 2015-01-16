package io.evercam.androidapp.exceptions;

import io.evercam.androidapp.utils.Constants;

public class ConnectivityException extends Exception
{

    private String ServerHtml;

    public ConnectivityException(String message)
    {
        super(message);
    }

    public ConnectivityException()
    {
        super(Constants.ErrorMessageNoConnectivity);
    }

    public ConnectivityException(String message, String _serverHtml)
    {
        super((message != null ? message : Constants.ErrorMessageNoConnectivity));
        ServerHtml = _serverHtml;
    }

    private static final long serialVersionUID = 1L;

    public String getServerHtml()
    {
        return ServerHtml;
    }

    public void setServerHtml(String _ServerHtml)
    {
        ServerHtml = _ServerHtml;
    }

}
