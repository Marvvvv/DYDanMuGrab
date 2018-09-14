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

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class MsgServiceImpl implements MsgService {

    @Autowired
    UEnterMapper uEnterMapper;
    @Autowired
    AnchorOnlineMapper anchorOnlineMapper;
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
    public void anchorOnlineMsgHandle(Map<String, Object> msg) {
        AnchorOnline anchorOnline = new AnchorOnline();
        anchorOnline.setRoom_id(Integer.parseInt(msg.get("rid").toString()));
        anchorOnline.setOnline_status(Integer.parseInt(msg.get("ss").toString()));
        anchorOnline.setDate(anchorOnline.getOnline_status() == 1?sdf.format(System.currentTimeMillis()):
                sdf.format(Long.valueOf(msg.get("endtime").toString()) * 1000));
        // 修改开播状态
        GlobalCache.getGlobalCache().setOnline(anchorOnline.getOnline_status() == 1);
        anchorOnlineMapper.insert(anchorOnline);
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
            radio.setDate(System.currentTimeMillis()+"");
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
                radio.setDate(System.currentTimeMillis()+"");
                radio.setRoomId(nobleHistory.getRoom_id());
                radio.setAchorName(nobleHistory.getAnchor_name());
                radio.setGiveName(nobleHistory.getUname());
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


}
