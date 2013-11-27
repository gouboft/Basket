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

public class MyApplication extends Application {
    private EventHandler mHandler;

    public void setHandler(EventHandler handler) {
        mHandler = handler;
    }
    public  EventHandler getHandler() {
        return mHandler;
    }
}
