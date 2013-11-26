/*
 * Copyright 2009 Cedric Priscal
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License. 
 */

package com.jpjy.basket;

import android.app.Application;

import com.jpjy.basket.MainActivity.EventHandler;

import java.io.File;
import java.io.IOException;
import java.security.InvalidParameterException;

public class MyApplication extends Application {

    private SerialPort mRfidSerialPort = null;
    private SerialPort mElecLockSerialPort = null;
    private StringBuffer mBuffer;
    private long updateBuffer = 0;
    private EventHandler mHandler;


    public SerialPort getBarcodeSerialPort() throws SecurityException, IOException, InvalidParameterException {
        if (mRfidSerialPort == null) {
            String path = "/dev/ttyS5";
            int baudrate = 57600;

            /* Check parameters */
            if ((path.length() == 0) || (baudrate == -1)) {
                throw new InvalidParameterException();
            }

			/* Open the serial port */
            mRfidSerialPort = new SerialPort(new File(path), baudrate, 0);
        }
        return mRfidSerialPort;
    }

    public void closeRfidSerialPort() {
        if (mRfidSerialPort != null) {
            mRfidSerialPort.close();
            mRfidSerialPort = null;
        }
    }

    public SerialPort getElecLockSerialPort() throws SecurityException, IOException, InvalidParameterException {
        if (mElecLockSerialPort == null) {
            String path = "/dev/ttyS5";;
            int baudrate = 57600;

            /* Check parameters */
            if ((path.length() == 0) || (baudrate == -1)) {
                throw new InvalidParameterException();
            }

			/* Open the serial port */
            mElecLockSerialPort = new SerialPort(new File(path), baudrate, 0);
        }
        return mElecLockSerialPort;
    }

    public void closeEleclockSerialPort() {
        if (mElecLockSerialPort != null) {
            mElecLockSerialPort.close();
            mElecLockSerialPort = null;
        }
    }


    public void setHandler(EventHandler handler) {
        mHandler = handler;
    }
    public  EventHandler getHandler() {
        return mHandler;
    }
}
