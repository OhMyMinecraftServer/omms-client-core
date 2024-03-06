package icu.takeneko.omms.client.session.callback;

import icu.takeneko.omms.client.announcement.Announcement;
import icu.takeneko.omms.client.request.Request;
import icu.takeneko.omms.client.response.Response;
import icu.takeneko.omms.client.session.SessionContext;
import icu.takeneko.omms.client.util.Result;
import jdk.vm.ci.code.site.Call;

import java.util.Map;
import java.util.function.Consumer;

import static icu.takeneko.omms.client.util.Util.gson;

public class AnnouncementListCallbackHandle extends CallbackHandle1<Map<String, Announcement>, SessionContext> {

    public AnnouncementListCallbackHandle(Callback<Map<String, Announcement>> fn) {
        super("announcements", fn);
    }

    @Override
    protected Map<String, Announcement> parse(SessionContext context) {

        String announcements = context.getContent("announcements");
        if (announcements == null) return null;
        String[] names = gson.fromJson(announcements, String[].class);
        context.getSession().getAnnouncementMap().clear();
        boolean hasError = false;
        RuntimeException re = new RuntimeException();
        for (String name : names) {
            try {
                Response r = context.getSession().sendBlocking(new Request().setRequest("ANNOUNCEMENT_GET").withContentKeyPair("id", name));
                if (r.getResponseCode() == Result.ANNOUNCEMENT_GOT) {
                    Announcement announcement = new Announcement(r.getContent("id"),
                            Long.parseLong(r.getContent("time")),
                            r.getContent("title"),
                            gson.fromJson(r.getContent("content"), String[].class)
                    );
                    context.getSession().getAnnouncementMap().put(name, announcement);
                }
            } catch (Throwable e) {
                re.addSuppressed(e);
                hasError = true;
            }
        }
        if (hasError) throw re;
        return context.getSession().getAnnouncementMap();
    }
}
