package com.yukari;

import com.yukari.client.DyBulletScreenClient;
import com.yukari.config.PropProperties;
import com.yukari.utils.KeepAlive;
import com.yukari.utils.KeepGetMsg;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

@SpringBootApplication
@MapperScan("com.yukari.dao")
public class ApplicationRun {

	public static void main(String[] args) {
        ConfigurableApplicationContext ctx = SpringApplication.run(ApplicationRun.class, args);

        PropProperties p = (PropProperties)ctx.getBean("propProperties");
		int roomId = p.getRoomId();

        DyBulletScreenClient client = DyBulletScreenClient.getInstance();
        client.init(roomId, -9999);

        // 发送心跳包线程
        KeepAlive alive = new KeepAlive();
        alive.start();


        // 接收弹幕服务器的消息
        KeepGetMsg getMsg = new KeepGetMsg();
        getMsg.start();
	}
}
