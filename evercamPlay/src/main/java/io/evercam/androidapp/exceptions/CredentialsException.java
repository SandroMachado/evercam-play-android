//package io.evercam.androidapp.exceptions;
//
//import io.evercam.androidapp.utils.Constants;
//
//public class CredentialsException extends Exception
//{
//
//	private String ServerHtml;
//
//	public CredentialsException(String message)
//	{
//		super((message == null ? Constants.ErrorMessageInvalidCredentials : message));
//	}
//
//	public CredentialsException()
//	{
//		super(Constants.ErrorMessageInvalidCredentials);
//	}
//
//	public CredentialsException(String message, String _serverHtml)
//	{
//		super((message == null ? Constants.ErrorMessageInvalidCredentials : message));
//		ServerHtml = _serverHtml;
//	}
//
//	private static final long serialVersionUID = 1L;
//
//	public String getServerHtml()
//	{
//		return ServerHtml;
//	}
//
// }
