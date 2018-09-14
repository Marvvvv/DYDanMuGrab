package com.yukari.utils;


import com.yukari.client.DyBulletScreenClient;

/**
 * @Summary: 服务器心跳保持线程
 * @author: FerroD     
 * @date:   2016-3-12   
 * @version V1.0
 */

public class KeepAlive extends Thread {

    @Override
    public void run() {
        DyBulletScreenClient client = DyBulletScreenClient.getInstance();
        while (client.getReadyFlag()) {
            client.keepAlive();
            try {
                Thread.sleep(45000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

}
