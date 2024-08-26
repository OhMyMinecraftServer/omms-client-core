package icu.takeneko.omms.client.session.callback;

import icu.takeneko.omms.client.data.announcement.Announcement;
import icu.takeneko.omms.client.session.SessionContext;
import icu.takeneko.omms.client.session.handler.CallbackHandle;
import icu.takeneko.omms.client.session.request.Request;
import icu.takeneko.omms.client.util.Result;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.LockSupport;

import icu.takeneko.omms.client.util.Util;

public class AnnouncementListCallbackHandle extends CallbackHandle1<Map<String, Announcement>, SessionContext> {

    public AnnouncementListCallbackHandle(Callback<Map<String, Announcement>> fn) {
        super("announcements", fn);
    }

    @Override
    protected Map<String, Announcement> parse(SessionContext context) {
        String controllerListString = context.getContent(key);
        if (controllerListString == null) return null;
        List<String> controllerNames = Arrays.asList(Util.getGson().fromJson(controllerListString, String[].class));
        context.getSession().getAnnouncementMap().clear();
        List<String> a = new ArrayList<>(controllerNames);
        String id = Long.toString(System.nanoTime());
        CallbackHandle<SessionContext> handle = new CallbackHandle1<Announcement, SessionContext>("", ann -> {
            context.getSession().getAnnouncementMap().put(ann.getId(), ann);
            a.remove(ann.getId());
        }) {
            @Override
            protected Announcement parse(SessionContext context) {
                return new Announcement(context.getContent("id"),
                        Long.parseLong(context.getContent("time")),
                        context.getContent("title"),
                        Util.getGson().fromJson(context.getContent("content"), String[].class));
            }
        };
        CallbackHandle<SessionContext> notExist = new StringCallbackHandle("announcement", a::remove);
        handle.setAssociateGroupId(id);
        notExist.setAssociateGroupId(id);
        context.getSession().getDelegate().register(Result.ANNOUNCEMENT_GOT, handle, false);
        context.getSession().getDelegate().register(Result.ANNOUNCEMENT_NOT_EXIST, notExist, false);
        for (String name : controllerNames) {
            Request request = new Request("ANNOUNCEMENT_GET").withContentKeyPair("id", name);
            context.getSession().send(request);
        }
        while (!a.isEmpty()) {
            LockSupport.parkNanos(1);
        }
        context.getSession().getDelegate().removeAssocGroup(id);
        return context.getSession().getAnnouncementMap();
    }


}
