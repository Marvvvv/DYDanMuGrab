package com.yukari;

import com.yukari.dao.AnchorOnlineTimeMapper;
import com.yukari.entity.AnchorOnlineTime;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.text.ParseException;
import java.text.SimpleDateFormat;

@RunWith(SpringRunner.class)
@SpringBootTest
public class DyDanmuGrabApplicationTests {

	@Autowired
	AnchorOnlineTimeMapper anchorOnlineTimeMapper;

	@Test
	public void contextLoads() {
	}



	@Test
	public void test () throws ParseException {
		/*SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		String time1 = "2018-09-13 18:42:56";
		String time2 = "2018-09-13 18:58:34";

		long mss = sdf.parse(time2).getTime() - sdf.parse(time1).getTime();
		String re = formatDuring(mss);
		System.out.println(re);*/

		AnchorOnlineTime act = anchorOnlineTimeMapper.getLast();
		String onlineTime = act.getOnlineTime().substring(0,act.getOnlineTime().indexOf(".0"));
		System.out.println(1);

	}


	// 毫秒转换为XX小时XX分钟
	private String formatDuring (long mss){
		long hours = mss / (1000 * 60 * 60);
		long minutes = (mss % (1000 * 60 * 60)) / (1000 * 60);
		return hours + "小时" + minutes +"分钟";
	}


}
