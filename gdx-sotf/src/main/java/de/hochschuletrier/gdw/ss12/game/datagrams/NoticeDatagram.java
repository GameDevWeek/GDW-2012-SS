package de.hochschuletrier.gdw.ss12.game.datagrams;

import de.hochschuletrier.gdw.commons.netcode.core.NetDatagram;
import de.hochschuletrier.gdw.commons.netcode.core.NetMessageIn;
import de.hochschuletrier.gdw.commons.netcode.core.NetMessageOut;
import de.hochschuletrier.gdw.commons.netcode.core.NetMessageType;
import de.hochschuletrier.gdw.ss12.game.data.NoticeType;

/**
 * send from server only
 */
public class NoticeDatagram extends NetDatagram {

    private NoticeType noticeType;
    private float delay;
    private float timeLeft;

    public static NoticeDatagram create(NoticeType type, float delay, float timeLeft) {
        NoticeDatagram datagram = DatagramFactory.create(NoticeDatagram.class);
        datagram.noticeType = type;
        datagram.delay = delay;
        datagram.timeLeft = timeLeft;
        return datagram;
    }

    public NoticeType getNoticeType() {
        return noticeType;
    }

    public float getDelay() {
        return delay;
    }

    public float getTimeLeft() {
        return timeLeft;
    }

    @Override
    public NetMessageType getMessageType() {
        return NetMessageType.NORMAL;
    }

    @Override
    public void writeToMessage(NetMessageOut message) {
        message.putEnum(noticeType);
        message.putFloat(delay);
    }

    public @Override
    void readFromMessage(NetMessageIn message) {
        noticeType = message.getEnum(NoticeType.class);
        delay = message.getFloat();
    }
}
