
#include <stdio.h> 
#include <stdlib.h>
#include <fcntl.h>
#include <errno.h>
#include <unistd.h>
#include "com_jpjy_basket_Linuxc.h"
#include <sys/types.h>
#include <sys/stat.h>
#include <string.h>
#include <stdint.h>
#include <termios.h>
#include <android/log.h>
#include <jni.h>
#include <sys/ioctl.h>

#include <sys/time.h> 

#include <android/log.h>
#define LOGI(...) ((void)__android_log_print(ANDROID_LOG_INFO, "", __VA_ARGS__))

#undef  TCSAFLUSH
#define TCSAFLUSH  TCSETSF
#ifndef _TERMIOS_H_
#define _TERMIOS_H_
#endif

//定义全局变量
int fd; 
int ttyS6gpioFd;
int ttyS7gpioFd;
//---------------------------
//当需要从ttyS6读一维码扫描数据的时候需要把em13_gpio_state写低电平，一旦退出设备的时候设置为高电平
//当需要从ttyS7写数据的时候需要把uart485_gpio_state写高电平，一旦退出设备的时候设置为低电平,或者写完也是设置低
const char *uart485 = "/sys/class/power_supply/battery/device/uart485_gpio_state";
const char *em13 = "/sys/class/power_supply/battery/device/em13_gpio_state";
const char *ttyS6buf = "/dev/ttyS6";
const char *ttyS7buf = "/dev/ttyS7";
int ttyS6andS7=0; //如果是6 怎是设置ttyS6,如果是7则是设置ttyS7
const char dat0 = '0';
const char dat1 = '1';
//实现本地方法 openUart
//str=> "/dev/ttyS6","/dev/ttyS7", "/dev/rfid_rc522_dev"
JNIEXPORT jint JNICALL Java_com_jpjy_basket_Linuxc_openUart(JNIEnv *env,jobject mc, jstring str)
{
    const char *bufchar;
    bufchar=(*env)->GetStringUTFChars(env,str,NULL);
    if(!strcmp(ttyS6buf,bufchar))
    {
        ttyS6gpioFd = open(em13, O_RDWR);
        if(ttyS6gpioFd > 0)
            ttyS6andS7 = 6;
        else
            ttyS6andS7 = 0;

    }
    else if(!strcmp(ttyS7buf,bufchar))
    {
        ttyS7gpioFd = open(uart485, O_RDWR);
        if(ttyS7gpioFd > 0)
            ttyS6andS7 = 7;
        else
            ttyS6andS7 = 0;
    }
    else
    {
        ttyS6andS7 = 0;
    }

    LOGI("--jni ttyS6andS7 = %d--",ttyS6andS7) ;
    fd=open(bufchar,O_RDWR|O_NOCTTY|O_NDELAY); 
    LOGI("jni opne %s device ",bufchar) ;
    (*env)->ReleaseStringUTFChars(env, str, bufchar);
    return fd;
}


JNIEXPORT void JNICALL Java_com_jpjy_basket_Linuxc_closeUart(JNIEnv *env,jobject mc)
{
        if(ttyS6andS7 == 6)
        {
            //--------------------关闭ttyS6的前，设置em13_gpio_state=1
            write(ttyS6gpioFd, &dat1, sizeof(dat1));
            LOGI("jni receive ttyS6, write to %c data to em13_gpio_state !",dat1) ;
            close(ttyS6gpioFd);
        }
        if(ttyS6andS7 == 7)
            close(ttyS7gpioFd);
        
        close(fd);
        LOGI("jni close devices !") ;
}


JNIEXPORT jint JNICALL Java_com_jpjy_basket_Linuxc_setUart(JNIEnv *env,jobject mc,jint baudrate)
{
    struct termios newtio,oldtio;
    int  speed_arr[]={B1200,B2400,B4800,B9600,B19200,B38400,B57600,B115200,B230400,B921600};
    int  speed_int[]={ 1200, 2400, 4800, 9600, 19200, 38400, 57600, 115200, 230400, 921600};
    int  i;

        LOGI("jni setUart devices baud rate !") ;
        for(i =0 ; i < sizeof(speed_int)/sizeof(int); i++)
        {
            if(baudrate == speed_int[i])
            {
                LOGI("jni setUart devices %d baud rate !",speed_int[i]) ;
                tcgetattr(fd,&oldtio);
                tcgetattr(fd,&newtio);
                cfsetispeed(&newtio,speed_arr[i]);
                cfsetospeed(&newtio,speed_arr[i]);

                newtio.c_lflag=0; 
                newtio.c_cflag = speed_arr[i] | CS8 | CREAD | CLOCAL;
                newtio.c_iflag= BRKINT | IGNPAR | IGNCR | IXON | IXOFF | IXANY ;
                newtio.c_oflag=02;
                newtio.c_line=0;
                newtio.c_cc[7]=255;
                newtio.c_cc[4]=0;
                newtio.c_cc[5]=0; 

                if(tcsetattr(fd,TCSANOW,&newtio)<0)
                {
                    LOGI("ttySx tcsetattr fail !");
                    //exit(1);
                    close(fd); //关闭
                    goto  setend;
                }

                return fd;


            }
        }

setend:
    LOGI("jni not setUart devices baud rate end!") ;
    return -1;
}

JNIEXPORT jint JNICALL Java_com_jpjy_basket_Linuxc_sendMsgUart(JNIEnv *env,jobject mc,jstring str)
{
    int len;
    const char *buf;
    buf=(*env)->GetStringUTFChars(env,str,NULL);
    len= (*env)->GetStringLength(env,str );

    if(ttyS6andS7 == 7)
    {
        //--------------------写ttyS7前，设置uart485_gpio_state=1
        write(ttyS7gpioFd, &dat1, sizeof(dat1));
        LOGI("jni write to %c data to uart485_gpio_state !",dat1) ;
        write(fd,buf,len);
        //--------------------写ttyS7后，设置uart485_gpio_state=0
        write(ttyS7gpioFd, &dat0, sizeof(dat0));
        LOGI("jni write to %c data to uart485_gpio_state !",dat0) ;
    }
    else
    {
        write(fd,buf,len);
    }

    LOGI("jni write to %s data to devices !",buf) ;
    (*env)->ReleaseStringUTFChars(env, str, buf);
}


JNIEXPORT jstring JNICALL Java_com_jpjy_basket_Linuxc_receiveMsgUart(JNIEnv *env,jobject mc)
{
    int len=0,ret;
    char buffer[128]="";
    char card[6];
    fd_set rdfd; 
    memset(buffer,0,sizeof(buffer));

    //--------------------接收ttyS6的一维码扫描前，设置em13_gpio_state=0
    if(ttyS6andS7 == 6)
    {
        write(ttyS6gpioFd, &dat0, sizeof(dat0));
        //LOGI("jni receive ttyS6, write to %c data to em13_gpio_state !",dat0) ;

    }
    FD_SET(fd, &rdfd);/* 把句柄加入读监视集合 */

    struct timeval timeout={1,0}; //select等待1秒，1秒轮询，要非阻塞就置0

    ret = select(fd + 1, &rdfd, NULL, NULL, &timeout); /* 注意是最大值加1 */

    if(ret < 0)
    {
        LOGI("call select fail");  /* select函数出错 */
        return NULL ;
    }
    else if(ret == 0)
    {
        LOGI("selet timeout "); /* 在设定的timeval 时间内，socket的状态没有发生变化 */
        return NULL ;
    }
    else
    {
        if (ttyS6andS7  != 0) //如果这个变量都不等于O的，那么肯定是ttyS6跟S7啦！
        {

            if(FD_ISSET(fd, &rdfd)) /* 先判断一下 fd 这个被监视的句柄是否真的变成可读的了 */
            {
                FD_ZERO(&rdfd); //清空集合
                /* 读取ttySxfd 句柄里的数据 */
                len=read(fd, buffer, sizeof(buffer));
                //LOGI("len = %d, buffer[0]=%c, buffer[1]=%c",len,buffer[0],buffer[1]);
                if (len > 0)
                {
                    LOGI("jni read  data from ttySx !") ;
                    return (*env)->NewStringUTF(env,buffer);
                }
                else
                    return NULL;

            }
            return NULL ;

        }
        else 
        {
            if(FD_ISSET(fd, &rdfd)) 
            {
                FD_ZERO(&rdfd); //清空集合
                /* 读取 rc522fd 句柄里的数据 */
                len=read(fd, buffer, sizeof(buffer));
                if (len > 0)
                {
                    sprintf(card,"%2.2X%2.2X%2.2X%2.2X",buffer[0],buffer[1],buffer[2],buffer[3]);
                    LOGI("jni read data from rc522_dev !") ;
                    return (*env)->NewStringUTF(env,card);
                }
                else
                    return NULL;

            }
            return NULL ;

        }

    }
}




