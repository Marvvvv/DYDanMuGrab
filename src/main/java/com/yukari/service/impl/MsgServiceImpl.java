package com.yukari.service.impl;

import com.yukari.cache.GlobalCache;
import com.yukari.config.PropProperties;
import com.yukari.dao.*;
import com.yukari.producer.RadioMQSender;
import com.yukari.service.MsgService;
import com.yukari.utils.DYSerializeUtil;
import com.yukari.entity.*;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class MsgServiceImpl implements MsgService {

    @Autowired
    UEnterMapper uEnterMapper;
    @Autowired
    AnchorOnlineTimeMapper anchorOnlineTimeMapper;
    @Autowired
    GiftRadioMapper giftRadioMapper;
    @Autowired
    ShutUpMapper shutUpMapper;
    @Autowired
    GiftHistoryMapper giftHistoryMapper;
    @Autowired
    BulletHistoryMapper bulletHistoryMapper;
    @Autowired
    NobleHistoryMapper nobleHistoryMapper;
    @Autowired
    GiftInfoMapper giftInfoMapper;

    private SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    private List<UEnter> uEnters = new ArrayList<>(); // 节省资源，用来批量插入
//    private List<GiftHistory> generalGifts = new ArrayList<>(); // 节省资源，小礼物批量插入
    private List<BulletHistory> bullets = new ArrayList<>(); // 节省资源，弹幕批量插入


    @Autowired
    PropProperties properties;

    @Autowired
    RadioMQSender radioMQSender;


    @Override
    // 弹幕消息
    public void bulletMsgHandle(Map<String, Object> msg) {
        // 直播时500条弹幕插入一次，下播后100条弹幕插入一次
        if (StringUtils.isNotBlank(msg.get("uid").toString()) && msg.containsKey("txt")) {
            BulletHistory bullet = new BulletHistory();
            bullet.setRoom_id(Integer.parseInt(msg.get("rid").toString()));
            bullet.setUid(Integer.parseInt(msg.get("uid").toString()));
            bullet.setUname(msg.get("nn").toString());
            bullet.setUlevel(Integer.parseInt(msg.get("level").toString()));
            bullet.setHeadIcon_url(DYSerializeUtil.headIconUrlEscape(msg.get("ic").toString()));

            // 粉丝牌可能为空
            String fansCardName = msg.get("bnn").toString();
            Integer fansCardLevel = Integer.parseInt(msg.get("bl").toString());
            Integer fansCardRoomId = Integer.parseInt(msg.get("brid").toString());
            if (StringUtils.isNotBlank(fansCardName)) {
                bullet.setFans_card_name(fansCardName);
                bullet.setFans_card_level(fansCardLevel);
                bullet.setFans_card_room_id(fansCardRoomId);
            }

            bullet.setContent(msg.get("txt").toString());
            bullet.setDate(sdf.format(System.currentTimeMillis()));

            bullets.add(bullet);

            int insertSize = GlobalCache.getGlobalCache().isOnline()?properties.getBulletOpenplayMaxSize():properties.getBulletCloseplayMaxSize();
            if (!bullets.isEmpty() && bullets.size() >= insertSize) {
                bulletHistoryMapper.insertBatch(bullets);
                bullets.clear();
            }
        }
    }

    @Override
    // 赠送礼物消息
    public void giftMsgHandle(Map<String, Object> msg) {
        // 不记录小礼物了，大礼物可通过礼物广播获取
        /*if (StringUtils.isNotBlank(msg.get("gfid").toString())) {
            GiftHistory gift = new GiftHistory();
            gift.setRoom_id(Integer.parseInt(msg.get("rid").toString()));
            gift.setUid(Integer.parseInt(msg.get("uid").toString()));
            gift.setUname(msg.get("nn").toString());
            gift.setUlevel(Integer.parseInt(msg.get("level").toString()));
            gift.setHeadIcon_url(DYSerializeUtil.headIconUrlEscape(msg.get("ic").toString()));
            gift.setGift_id(Integer.parseInt(msg.get("gfid").toString()));
            gift.setGift_amount(Integer.parseInt(msg.get("gfcnt").toString()));

            // 粉丝牌可能为空
            String fansCardName = msg.get("bnn").toString();
            Integer fansCardLevel = Integer.parseInt(msg.get("bl").toString());
            Integer fansCardRoomId = Integer.parseInt(msg.get("brid").toString());
            if (StringUtils.isNotBlank(fansCardName)) {
                gift.setFans_card_name(fansCardName);
                gift.setFans_card_level(fansCardLevel);
                gift.setFans_card_room_id(fansCardRoomId);
            }

            gift.setDate(sdf.format(System.currentTimeMillis()));

            generalGifts.add(gift);
            if (!generalGifts.isEmpty() && generalGifts.size() >= giftMaxSize) {
                giftHistoryMapper.insertBatch(generalGifts);
                generalGifts.clear();
            }

        }*/
   }

    @Override
    // 用户进房消息
    public void enterMsgHandle(Map<String, Object> msg) {
        UEnter uEnter = new UEnter();
        uEnter.setRoom_id(Integer.parseInt(msg.get("rid").toString()));
        uEnter.setUid(Integer.parseInt(msg.get("uid").toString()));
        uEnter.setUname(msg.get("nn").toString());
        uEnter.setLevel(Integer.parseInt(msg.get("level").toString()));
        uEnter.setHeadIcon_url(DYSerializeUtil.headIconUrlEscape(msg.get("ic").toString()));
        uEnter.setDate(sdf.format(System.currentTimeMillis()));
        uEnters.add(uEnter);

        if (!uEnters.isEmpty() && uEnters.size() >= properties.getUenterMaxSize()) {
            // 100条插入一次
            uEnterMapper.insertBatch(uEnters);
            uEnters.clear();
        }
    }

    @Override
    // 开关播消息
    public void anchorOnlineMsgHandle(Map<String, Object> msg) throws ParseException {

        // 修改开播状态
        GlobalCache.getGlobalCache().setOnline("1".equals(msg.get("ss").toString()));
        if ("1".equals(msg.get("ss").toString())) {
            // 开播信息 ，插入新纪录，房间id、开播时间、开播状态
            AnchorOnlineTime act = new AnchorOnlineTime();
            act.setRoom_id(Integer.parseInt(msg.get("rid").toString()));
            act.setOnline_status(Integer.parseInt(msg.get("ss").toString()));
            act.setOnlineTime(sdf.format(System.currentTimeMillis()));
            anchorOnlineTimeMapper.insert(act);
        } else {
            // 关播信息 , update ： 关播时间、直播时长、开播状态
            AnchorOnlineTime act = anchorOnlineTimeMapper.getLast();
            act.setOfflineTime(sdf.format(Long.valueOf(msg.get("endtime").toString()) * 1000));
            act.setOnline_status(Integer.parseInt(msg.get("ss").toString()));
            // 计算直播时长
            String onlineTime = act.getOnlineTime().substring(0,act.getOnlineTime().indexOf(".0"));
            long mss = (Long.valueOf(msg.get("endtime").toString()) * 1000) - sdf.parse(onlineTime).getTime();
            act.setOnline_length(formatDuring(mss));
            anchorOnlineTimeMapper.updateOnlineTime(act);
        }


    }

    @Override
    // 超级弹幕消息
    public void bigBulletMsgHandle(Map<String, Object> msg) {

    }

    @Override
    // 礼物广播消息
    public void giftRadioMsgHandle(Map<String, Object> msg) {
        if (StringUtils.isNotBlank(msg.get("gn").toString())) {
            GiftRadio giftRadio = new GiftRadio();
            giftRadio.setRoom_id(Integer.parseInt(msg.get("drid").toString()));
            giftRadio.setGiver(msg.get("sn").toString());
            giftRadio.setAnchor_name(msg.get("dn").toString());
            giftRadio.setGift_id(Integer.parseInt(msg.get("gfid").toString()));
            giftRadio.setGift_name(msg.get("gn").toString());
            giftRadio.setAmount(Integer.parseInt(msg.get("gc").toString()));
            giftRadio.setGift_style(Integer.parseInt(msg.get("es").toString()));
            giftRadio.setDate(sdf.format(System.currentTimeMillis()));
            giftRadioMapper.insert(giftRadio);

            // 发送到rabbitMQ
            RadioMQModel radio = new RadioMQModel();
            radio.setRoomId(giftRadio.getRoom_id());
            radio.setGift_name(giftRadio.getGift_name());
            radio.setGiveName(giftRadio.getGiver());
            radio.setAchorName(giftRadio.getAnchor_name());
            radio.setRadioType(1);
            radio.setDate(giftRadio.getDate());
            radio.setGift_src(getGiftSrc(giftRadio.getGift_id()));
            radioMQSender.send(radio);
        }
    }

    @Override
    // 禁言消息
    public void shutUpMsgHandle(Map<String, Object> msg) {
        if (StringUtils.isNotBlank(msg.get("did").toString())) {
            ShutUp shutUp = new ShutUp();
            shutUp.setRoom_id(Integer.parseInt(msg.get("rid").toString()));
            shutUp.setExecuter_type(Integer.parseInt(msg.get("otype").toString()));
            shutUp.setExecuter_id(Integer.parseInt(msg.get("sid").toString()));
            shutUp.setExecuter_name(msg.get("snic").toString());
            shutUp.setShutUp_id(Integer.parseInt(msg.get("did").toString()));
            shutUp.setShutUp_name(msg.get("dnic").toString());
            long endTime = Long.valueOf(msg.get("endtime").toString()) * 1000L;
            shutUp.setEnd_time(sdf.format(endTime));
            // 计算禁言时间
            long time = endTime - System.currentTimeMillis();
            long oneMin = 1000*60;
            long tenMin = oneMin*10;
            long oneDay = oneMin*60*24;
            String banTime = "";
            if (time > oneDay) {
                banTime = "30天";
            } else if (time > tenMin) {
                banTime = "1天";
            } else if (time > oneMin) {
                banTime = "10分";
            } else {
                banTime = "1分";
            }
            shutUp.setBan_time(banTime);
            shutUpMapper.insert(shutUp);
        }
    }


    @Override
    // 开通/续费贵族消息
    public void openOrRenewNobleMsgHandle(Map<String, Object> msg) {
        if (StringUtils.isNotBlank(msg.get("uid").toString())) {
            NobleHistory nobleHistory = new NobleHistory();
            nobleHistory.setType("anbc".equals(msg.get("type").toString())?1:2);
            nobleHistory.setUid(Integer.parseInt(msg.get("uid").toString()));
            nobleHistory.setUname(msg.get("unk").toString());
            nobleHistory.setHeadIcon_url(DYSerializeUtil.headIconUrlEscape(msg.get("uic").toString()));
            nobleHistory.setRoom_id(Integer.parseInt(msg.get("drid").toString()));
            nobleHistory.setAnchor_name(msg.get("donk").toString());
            int nobleType = Integer.parseInt(msg.get("nl").toString());
            nobleHistory.setNoble_type(nobleType);
            nobleHistory.setNoble_name(getNobleName(nobleType));
            nobleHistory.setDate(sdf.format(System.currentTimeMillis()));
            nobleHistoryMapper.insert(nobleHistory);

            // 发送到rabbitMQ
            if (nobleHistory.getType() == 1) {
                RadioMQModel radio = new RadioMQModel();
                radio.setRadioType(2);
                radio.setGift_name(nobleHistory.getNoble_name());
                radio.setDate(nobleHistory.getDate());
                radio.setRoomId(nobleHistory.getRoom_id());
                radio.setAchorName(nobleHistory.getAnchor_name());
                radio.setGiveName(nobleHistory.getUname());
                radio.setGift_src(rtnNobleSrc(radio.getGift_name()));
                radioMQSender.send(radio);
            }

        }
    }

    @Override
    // 礼物连击广播
    public void giftHitRadioMsgHandle(Map<String, Object> msg) {
        if (StringUtils.isNotBlank(msg.get("sid").toString())) {
            GiftRadio gift = new GiftRadio();
            gift.setRoom_id(Integer.parseInt(msg.get("drid").toString()));
            gift.setGiver(msg.get("sn").toString());
            gift.setAnchor_name(msg.get("dn").toString());
            gift.setGift_id(Integer.parseInt(msg.get("gfid").toString()));
            gift.setGift_name(msg.get("gn").toString());
            gift.setAmount(Integer.parseInt(msg.get("gc").toString()));
            gift.setDate(sdf.format(System.currentTimeMillis()));
            giftRadioMapper.insert(gift);
        }
    }


    private String getNobleName (int type) {
        switch (type) {
            case 1:
                return "骑士";
            case 2:
                return "子爵";
            case 3:
                return "伯爵";
            case 4:
                return "公爵";
            case 5:
                return "国王";
            case 6:
                return "皇帝";
            default:
                return "游侠";
        }
    }


    private String getGiftSrc (Integer giftId) {
        Map<Integer,GiftInfo> giftInfoMap = GlobalCache.getGlobalCache().getGiftInfoCache();
        if (giftInfoMap != null && !giftInfoMap.isEmpty()) {
            // 从缓存中拿礼物url
            if (giftInfoMap.containsKey(giftId)) {
                return giftInfoMap.get(giftId).getGift_gif_url();
            }
            return "";
        } else {
            // 从数据库中拿
            Map<Integer,GiftInfo> map = new HashMap<>();
            List<GiftInfo> giftInfos = giftInfoMapper.getAllGift();
            for (GiftInfo info : giftInfos) {
                map.put(info.getGift_id(),info);
            }
            GlobalCache.getGlobalCache().setGiftInfoCache(map);
            if (map.containsKey(giftId)) {
                return map.get(giftId).getGift_gif_url();
            }
            return "";
        }
    }


    private String rtnNobleSrc (String nobleName) {
        switch (nobleName) {
            case "游侠":
                return "https://res.douyucdn.cn/resource/2017/09/16/common/6b12f00d675b1bf85f03c67c9c5f1b24.png";
            case "骑士":
                return "https://res.douyucdn.cn/resource/2017/09/16/common/2804bf974a63ddf64f77942a56392a32.png";
            case "子爵":
                return "https://res.douyucdn.cn/resource/2017/09/16/common/06db8debe9fa16787586998b2498701a.png";
            case "伯爵":
                return "https://res.douyucdn.cn/resource/2017/09/16/common/17392efa63400c410947c5a69ff1cc35.gif";
            case "公爵":
                return "https://res.douyucdn.cn/resource/2017/09/16/common/534e34cbcfab5f62744e7ee89d946410.gif";
            case "国王":
                return "https://res.douyucdn.cn/resource/2017/09/16/common/0f0cecdc2a8b42727f59d0f07a571712.gif";
            case "皇帝":
                return "https://res.douyucdn.cn/resource/2017/09/16/common/59853156e3274457ac3bc9f837c287c7.gif";
            default:
                return "";
        }
    }


    // 毫秒转换为XX小时XX分钟
    private String formatDuring (long mss){
        long hours = mss / (1000 * 60 * 60);
        long minutes = (mss % (1000 * 60 * 60)) / (1000 * 60);
        return hours + "小时" + minutes +"分钟";
    }


}
