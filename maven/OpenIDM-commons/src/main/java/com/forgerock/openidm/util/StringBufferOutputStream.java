package com.forgerock.openidm.util;

/*
 * Created on Dec 25, 2004
 *
 * Copyright 2005 CafeSip.org
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

import java.io.IOException;
import java.io.OutputStream;

/**
 * @author amit
 *
 */
public class StringBufferOutputStream extends OutputStream
{
    private StringBuffer textBuffer = new StringBuffer();

    /**
     *
     */
    public StringBufferOutputStream()
    {
        super();
    }

    /*
     * @see java.io.OutputStream#write(int)
     */
    public void write(int b) throws IOException
    {
        char a = (char)b;
        textBuffer.append(a);
    }

    public String toString()
    {
        return textBuffer.toString();
    }

    public void clear()
    {
        textBuffer.delete(0, textBuffer.length());
    }
}