package io.evercam.androidapp.dto;

import android.content.Context;

public class MailInputDto
{
	public Context cont;
	public String Message;
	public Throwable ex;

	public MailInputDto(Context _c, String _message, Throwable _ex)
	{
		cont = _c;
		Message = _message;
		ex = _ex;
	}
}
