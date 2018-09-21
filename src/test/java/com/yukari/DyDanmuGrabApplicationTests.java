package com.yukari;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.yukari.dao.AnchorOnlineTimeMapper;
import com.yukari.dao.GiftInfoMapper;
import com.yukari.entity.AnchorOnlineTime;
import com.yukari.entity.Gift;
import com.yukari.entity.GiftInfo;
import org.jsoup.Connection;
import org.jsoup.HttpStatusException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

@RunWith(SpringRunner.class)
@SpringBootTest
public class DyDanmuGrabApplicationTests {

	@Autowired
	GiftInfoMapper giftInfoMapper;


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

		/*AnchorOnlineTime act = anchorOnlineTimeMapper.getLast();
		String onlineTime = act.getOnlineTime().substring(0,act.getOnlineTime().indexOf(".0"));
		System.out.println(1);*/

	}


	// 毫秒转换为XX小时XX分钟
	private String formatDuring (long mss){
		long hours = mss / (1000 * 60 * 60);
		long minutes = (mss % (1000 * 60 * 60)) / (1000 * 60);
		return hours + "小时" + minutes +"分钟";
	}




	/*@Test
	public void outputJSONRun () {
		FileOutputStream fot = null;
		try {
			fot = new FileOutputStream("D:/custoMap.json");
			Map<Integer,Object> custoMap = customGiftJSON();	//	定制礼物JSONArray
			custoMap.putAll(roomGift());
			String a = JSON.toJSONString(custoMap);
			fot.write(a.getBytes());
			fot.flush();
		}catch (FileNotFoundException e){
			e.printStackTrace();
		}catch (IOException e) {
			e.printStackTrace();
		}catch (InterruptedException e) {
			e.printStackTrace();
		} finally {
			try {
				if (fot != null) {
					fot.close();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}*/

	@Test
	public void newestGiftTask () throws IOException, InterruptedException {
		/*giftInfoMapper.emptyData();
		customGift();
		roomGift();*/
	}


	public void customGift () throws IOException{
		// 获取json格式文件
		Connection conn = Jsoup.connect("https://webconf.douyucdn.cn/resource/common/prop_gift_list/prop_gift_config.json").timeout(1000 * 60).userAgent("Mozilla").ignoreContentType(true);
		Document doc = conn.get();
		String content = doc.text();

		List<GiftInfo> giftInfos = new ArrayList<>();

		// 去除jsonp标识
		content = content.replace("DYConfigCallback(","");
		content = content.substring(0,content.indexOf(");"));
		JSONObject obj = JSON.parseObject(content);

		Map<String, Object> map = obj.getJSONObject("data").getInnerMap();
		for(Map.Entry<String,Object> giftObj : map.entrySet()){
			JSONObject giftProp = (JSONObject) giftObj.getValue();

			GiftInfo info = new GiftInfo();
			info.setGift_id(Integer.parseInt(giftObj.getKey()));
			info.setGift_name(giftProp.getString("name"));
			info.setGift_devote(giftProp.getInteger("devote"));
			info.setGift_exp(giftProp.getInteger("exp"));
			info.setGift_pic_url(giftProp.getString("bimg"));
			info.setGift_gif_url(giftProp.getString("himg"));
			giftInfos.add(info);

			if (giftInfos.size() >= 200) {
				giftInfoMapper.batchInsert(giftInfos);
				giftInfos.clear();
			}
		}
		if (giftInfos != null && !giftInfos.isEmpty()) {
			giftInfoMapper.batchInsert(giftInfos);
		}
	}


	public void roomGift () throws IOException, InterruptedException {
		Set<GiftInfo> uniq = new HashSet<>();
		int maxSize = 700;
		for (int i = 1; i <= maxSize; i++) {
			System.out.println(i);

			if (i==310){continue;}

			String href = "https://webconf.douyucdn.cn/resource/common/gift/gift_template/"+ i +".json";
			Connection conn = Jsoup.connect(href).userAgent("Mozilla").timeout(1000 * 60).ignoreContentType(true);
			Document doc = null;
			try {
				doc = conn.get();
			} catch (HttpStatusException e) {
				continue;
			}

			String content = doc.text();
			// 去除jsonp标识
			content = content.replace("DYConfigCallback(","");
			content = content.substring(0,content.indexOf(");"));
			JSONObject obj = JSON.parseObject(content);

			JSONArray giftArray = obj.getJSONArray("data");

			for (int j = 0; j < giftArray.size(); j++) {
				JSONObject giftObj = giftArray.getJSONObject(j);

				GiftInfo info = new GiftInfo();
				info.setGift_id(giftObj.getInteger("id"));
				info.setGift_name(giftObj.getString("name"));
				info.setGift_devote(giftObj.getInteger("devote"));
				info.setGift_exp(giftObj.getInteger("exp"));
				info.setGift_pic_url(giftObj.getString("gift_pic"));
				info.setGift_gif_url(giftObj.getString("himg"));
				uniq.add(info);
			}
			Thread.sleep(100);
		}

		List<GiftInfo> giftInfos = new ArrayList<>(uniq);
		giftInfoMapper.batchInsert(giftInfos);

	}


	/*public Map<Integer,Object> customGiftJSON () throws IOException {
		Map<Integer,Object> m = new HashMap<>();

		// 获取json格式文件
		Connection conn = Jsoup.connect("https://webconf.douyucdn.cn/resource/common/prop_gift_list/prop_gift_config.json").timeout(1000 * 60).userAgent("Mozilla").ignoreContentType(true);
		Document doc = conn.get();
		String content = doc.text();

		// 去除jsonp标识
		content = content.replace("DYConfigCallback(","");
		content = content.substring(0,content.indexOf(");"));
		JSONObject obj = JSON.parseObject(content);

		Map<String, Object> map = obj.getJSONObject("data").getInnerMap();
		for(Map.Entry<String,Object> giftObj : map.entrySet()){

			JSONObject giftProp = (JSONObject) giftObj.getValue();
			String gift_name = giftProp.getString("name");
			String gift_url = giftProp.getString("himg");
			int gift_devote = giftProp.getInteger("devote");
			int gift_exp = giftProp.getInteger("exp");

			GiftInfo gi = new GiftInfo(Integer.parseInt(giftObj.getKey()),gift_name,gift_url,gift_exp,gift_devote);
			m.put(Integer.parseInt(giftObj.getKey()),gi);

		}
		return m;
	}*/


	/*@Test
	public void run2() throws IOException, InterruptedException {roomGift();}


	public Map<Integer,Object> roomGift () throws IOException, InterruptedException {
		Map<Integer,Object> rtnMap = new HashMap<>();
		int maxSize = 700;
		for (int i = 1; i <= maxSize; i++) {
			System.out.println(i);

			if (i==310){continue;}

			String href = "https://webconf.douyucdn.cn/resource/common/gift/gift_template/"+ i +".json";
			Connection conn = Jsoup.connect(href).userAgent("Mozilla").timeout(1000 * 60).ignoreContentType(true);
//			Document doc = Jsoup.parse(new URL(href).openStream(),"utf-8",href);
			Document doc = null;
			try {
				doc = conn.get();
			} catch (HttpStatusException e) {
				continue;
			}

			String content = doc.text();
			// 去除jsonp标识
			content = content.replace("DYConfigCallback(","");
			content = content.substring(0,content.indexOf(");"));
			JSONObject obj = JSON.parseObject(content);

			JSONArray giftArray = obj.getJSONArray("data");

			for (int j = 0; j < giftArray.size(); j++) {
				JSONObject giftObj = giftArray.getJSONObject(j);
				GiftInfo gi = new GiftInfo(
						giftObj.getInteger("id"),
						giftObj.getString("name"),
						giftObj.getString("himg"),
						giftObj.getInteger("exp"),
						giftObj.getInteger("devote")
				);
				rtnMap.put(giftObj.getInteger("id"),gi);
			}
			Thread.sleep(100);
		}
		return rtnMap;
	}*/


	/*class GiftInfo implements Serializable{

		private int gift_id;
		private String gift_name;
		private String gift_url;
		private int gift_exp;
		private int gift_devote;

		public GiftInfo(){}

		public GiftInfo(int gift_id, String gift_name, String gift_url, int gift_exp, int gift_devote) {
			this.gift_id = gift_id;
			this.gift_name = gift_name;
			this.gift_url = gift_url;
			this.gift_exp = gift_exp;
			this.gift_devote = gift_devote;
		}

		public int getGift_id() {
			return gift_id;
		}

		public void setGift_id(int gift_id) {
			this.gift_id = gift_id;
		}

		public String getGift_name() {
			return gift_name;
		}

		public void setGift_name(String gift_name) {
			this.gift_name = gift_name;
		}

		public String getGift_url() {
			return gift_url;
		}

		public void setGift_url(String gift_url) {
			this.gift_url = gift_url;
		}

		public int getGift_exp() {
			return gift_exp;
		}

		public void setGift_exp(int gift_exp) {
			this.gift_exp = gift_exp;
		}

		public int getGift_devote() {
			return gift_devote;
		}

		public void setGift_devote(int gift_devote) {
			this.gift_devote = gift_devote;
		}

		@Override
		public String toString() {
			return "{" +
					"gift_id=" + gift_id +
					", gift_name='" + gift_name + '\'' +
					", gift_url='" + gift_url + '\'' +
					", gift_exp=" + gift_exp +
					", gift_devote=" + gift_devote +
					'}';
		}
	}*/

	@Test
	public void run222(){

		Map<Integer,GiftInfo> map = new HashMap<>();
		List<GiftInfo> giftInfos = giftInfoMapper.getAllGift();
		for (GiftInfo info : giftInfos) {
			map.put(info.getGift_id(),info);
		}
		System.out.println(1);

	}

}
