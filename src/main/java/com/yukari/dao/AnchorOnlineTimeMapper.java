package com.yukari.dao;

import com.yukari.entity.AnchorOnlineTime;

public interface AnchorOnlineTimeMapper {

    AnchorOnlineTime getLast ();

    void updateOnlineTime (AnchorOnlineTime anchorOnlineTime);

    void insert (AnchorOnlineTime anchorOnlineTime);


}
