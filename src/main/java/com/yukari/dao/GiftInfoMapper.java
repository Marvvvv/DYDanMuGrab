package com.yukari.dao;

import com.yukari.entity.Gift;
import com.yukari.entity.GiftInfo;

import java.util.List;

public interface GiftInfoMapper {

    List<GiftInfo> getAllGift();

    void emptyData();

    void insert(GiftInfo giftInfo);

    void batchInsert(List<GiftInfo> giftInfos);



}
