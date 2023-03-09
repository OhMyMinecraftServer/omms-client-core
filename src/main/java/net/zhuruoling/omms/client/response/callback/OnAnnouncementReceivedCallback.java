package net.zhuruoling.omms.client.response.callback;

import net.zhuruoling.omms.client.announcement.Announcement;

import java.util.List;

@FunctionalInterface
public interface OnAnnouncementReceivedCallback extends Callback<List<Announcement>> {
}
