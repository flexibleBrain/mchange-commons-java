/*
 * Distributed as part of mchange-commons-java 0.2.8
 *
 * Copyright (C) 2014 Machinery For Change, Inc.
 *
 * Author: Steve Waldman <swaldman@mchange.com>
 *
 * This library is free software; you can redistribute it and/or modify
 * it under the terms of EITHER:
 *
 *     1) The GNU Lesser General Public License (LGPL), version 2.1, as 
 *        published by the Free Software Foundation
 *
 * OR
 *
 *     2) The Eclipse Public License (EPL), version 1.0
 *
 * You may choose which license to accept if you wish to redistribute
 * or modify this work. You may offer derivatives of this work
 * under the license you have chosen, or you may provide the same
 * choice of license which you have been offered here.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 *
 * You should have received copies of both LGPL v2.1 and EPL v1.0
 * along with this software; see the files LICENSE-EPL and LICENSE-LGPL.
 * If not, the text of these licenses are currently available at
 *
 * LGPL v2.1: http://www.gnu.org/licenses/old-licenses/lgpl-2.1.html
 *  EPL v1.0: http://www.eclipse.org/org/documents/epl-v10.php 
 * 
 */

package com.mchange.util.impl;

import java.io.*;
import java.rmi.*;
import java.security.*;
import java.util.*;
import com.mchange.lang.*;
import com.mchange.util.*;

public class HexAsciiMD5PropertiesPasswordManager implements PasswordManager
{
  private final static String DIGEST_ALGORITHM  = "MD5";
  private final static String PASSWORD_ENCODING = "8859_1";

  private final static String DEF_PASSWORD_PROP_PFX = "password";
  private final static String DEF_HEADER = "com.mchange.util.impl.HexAsciiMD5PropertiesPasswordManager data";

  private final static boolean DEBUG = true;

  SyncedProperties props;
  String           pfx;
  MessageDigest    md;

  public HexAsciiMD5PropertiesPasswordManager(File propsFile, String pfx, String[] header) throws IOException
    {this(new SyncedProperties(propsFile, header), pfx);}

  public HexAsciiMD5PropertiesPasswordManager(File propsFile, String pfx, String header) throws IOException
    {this(new SyncedProperties(propsFile, header), pfx);}

  public HexAsciiMD5PropertiesPasswordManager(File propsFile) throws IOException
    {this(propsFile, DEF_PASSWORD_PROP_PFX, DEF_HEADER);}

  private HexAsciiMD5PropertiesPasswordManager(SyncedProperties sp, String pfx) throws IOException
    {
      try
	{
	  this.props = sp;
	  this.pfx   = pfx;
	  this.md    = MessageDigest.getInstance(DIGEST_ALGORITHM);
	}
      catch (NoSuchAlgorithmException e)
	{throw new InternalError(DIGEST_ALGORITHM + " is not supported???");}
    }

  public synchronized boolean validate(String username, String password) throws IOException
    {
      try
	{
	  String hStr = props.getProperty(pfx != null ? pfx + '.' + username : username);
	  byte[] fileAuth     = ByteUtils.fromHexAscii(hStr);
	  byte[] incomingAuth = md.digest(password.getBytes(PASSWORD_ENCODING));
	  return Arrays.equals(fileAuth, incomingAuth);
	}
      catch (NumberFormatException e)
	{throw new IOException("Password file corrupted! [contains invalid hex ascii string]");}
      catch (UnsupportedEncodingException e)
	{
	  if (DEBUG) e.printStackTrace();
	  throw new InternalError(PASSWORD_ENCODING + "is an unsupported encoding???");
	}
    }

  public synchronized boolean updatePassword(String username, String oldPassword, String newPassword) throws IOException
    {
      if (!validate(username, oldPassword)) return false;
      props.put(pfx + '.' + username, ByteUtils.toHexAscii(md.digest(newPassword.getBytes(PASSWORD_ENCODING))));
      return true;
    }
}





