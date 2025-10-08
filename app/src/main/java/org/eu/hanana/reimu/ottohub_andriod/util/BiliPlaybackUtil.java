package org.eu.hanana.reimu.ottohub_andriod.util;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.jetbrains.annotations.Nullable;

import kotlin.Pair;
import kotlin.TuplesKt;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class BiliPlaybackUtil {

    private static final OkHttpClient client = new OkHttpClient();

    /**
     * 获取Dash播放数据
     */
    public static Pair<String, List<BiliPlaybackData>> getDashPlaybackData(@Nullable String sessdata, String type, String vid, String cid) throws IOException {
        String sessData = sessdata != null ? sessdata : "";
        boolean login = !sessData.isEmpty();
        String imgKey = "";
        String subKey = "";

        // 获取 wbi imgKey/subKey
        {
            Request request = new Request.Builder()
                    .url("https://api.bilibili.com/x/web-interface/nav")
                    .header("Cookie", "SESSDATA=" + sessData)
                    .get()
                    .build();

            try (Response response = client.newCall(request).execute()) {
                if (!response.isSuccessful()) throw new IOException("HTTP " + response.code());
                JsonObject asJsonObject = JsonParser.parseString(response.body().string()).getAsJsonObject();
                JsonObject wbi_img = asJsonObject.get("data").getAsJsonObject().get("wbi_img").getAsJsonObject();
                imgKey = wbi_img.get("img_url").getAsString();
                subKey = wbi_img.get("sub_url").getAsString();

                Pattern pattern = Pattern.compile("/([a-f0-9]{32})\\.png$");
                Matcher matcherImgKey = pattern.matcher(imgKey);
                Matcher matcherSubKey = pattern.matcher(subKey);
                if (matcherImgKey.find()) imgKey = matcherImgKey.group(1);
                if (matcherSubKey.find()) subKey = matcherSubKey.group(1);
            }
        }

        // 构建播放请求参数
        String param = WbiUtil.getParam(Map.of(
                "cid", cid,
                type, vid,
                "fnval", 4048,
                "qn", "127"
        ), imgKey, subKey);

        Request playRequest = new Request.Builder()
                .url("https://api.bilibili.com/x/player/wbi/playurl?" + param)
                .header("Cookie", "SESSDATA=" + sessData)
                .get()
                .build();

        JsonObject asJsonObject;
        try (Response response = client.newCall(playRequest).execute()) {
            if (!response.isSuccessful()) throw new IOException("HTTP " + response.code());
            asJsonObject = JsonParser.parseString(response.body().string()).getAsJsonObject();
        }

        JsonArray accept_quality = asJsonObject.get("data").getAsJsonObject().get("accept_quality").getAsJsonArray();
        JsonObject sizes = new JsonObject();

        JsonArray videos = asJsonObject.get("data").getAsJsonObject().get("dash").getAsJsonObject().get("video").getAsJsonArray();
        JsonArray audios = asJsonObject.get("data").getAsJsonObject().get("dash").getAsJsonObject().get("audio").getAsJsonArray();
        long duration = asJsonObject.get("data").getAsJsonObject().get("dash").getAsJsonObject().get("duration").getAsLong();

        JsonObject audio = null;
        for (JsonElement jsonElement : audios) {
            JsonObject ao = jsonElement.getAsJsonObject();
            int id = ao.get("id").getAsInt();
            int oid = 0;
            if (audio != null) oid = audio.get("id").getAsInt();
            if (id > oid) audio = ao;
        }

        for (JsonElement jsonElement : accept_quality) {
            int qn = jsonElement.getAsInt();
            JsonObject vn = null;
            for (JsonElement video : videos) {
                JsonObject vo = video.getAsJsonObject();
                if (vo.get("id").getAsInt() == qn) {
                    vn = vo;
                    break;
                }
            }
            if (vn == null) {
                sizes.addProperty(String.valueOf(qn), -1);
                continue;
            }
            long bandwidth = vn.get("bandwidth").getAsLong();
            sizes.addProperty(String.valueOf(qn), duration * bandwidth / 8f + audio.get("bandwidth").getAsLong() * duration / 8f);
        }

        asJsonObject.add("sizes", sizes);

        List<BiliPlaybackData> result = new ArrayList<>();
        for (JsonElement video : videos) {
            JsonObject vo = video.getAsJsonObject();
            BiliPlaybackData biliPlaybackData = new BiliPlaybackData();
            biliPlaybackData.isDash = true;
            biliPlaybackData.videoUrl = vo.get("baseUrl").getAsString();
            biliPlaybackData.audioUrl = audio.get("baseUrl").getAsString();
            int qn = vo.get("id").getAsInt();
            biliPlaybackData.size = sizes.get(String.valueOf(qn)).getAsLong();
            biliPlaybackData.qn = qn;
            biliPlaybackData.sizeA = (long) (audio.get("bandwidth").getAsLong() * duration / 8f);
            result.add(biliPlaybackData);
        }
        return TuplesKt.to(asJsonObject.toString(), result);
    }

    /**
     * 获取 Mp4 播放数据
     */
    public static Pair<String, List<BiliPlaybackData>> getMp4PlaybackData(String type, String vid, String cid) throws IOException {
        String imgKey = "";
        String subKey = "";

        // 获取 wbi imgKey/subKey
        {
            Request request = new Request.Builder()
                    .url("https://api.bilibili.com/x/web-interface/nav")
                    .get()
                    .build();

            try (Response response = client.newCall(request).execute()) {
                if (!response.isSuccessful()) throw new IOException("HTTP " + response.code());
                JsonObject asJsonObject = JsonParser.parseString(response.body().string()).getAsJsonObject();
                JsonObject wbi_img = asJsonObject.get("data").getAsJsonObject().get("wbi_img").getAsJsonObject();
                imgKey = wbi_img.get("img_url").getAsString();
                subKey = wbi_img.get("sub_url").getAsString();

                Pattern pattern = Pattern.compile("/([a-f0-9]{32})\\.png$");
                Matcher matcherImgKey = pattern.matcher(imgKey);
                Matcher matcherSubKey = pattern.matcher(subKey);
                if (matcherImgKey.find()) imgKey = matcherImgKey.group(1);
                if (matcherSubKey.find()) subKey = matcherSubKey.group(1);
            }
        }

        String paramBase = WbiUtil.getParam(Map.of(
                "cid", cid,
                type, vid,
                "fnval", 1,
                "qn", "127"
        ), imgKey, subKey);

        List<BiliPlaybackData> result = new ArrayList<>();
        JsonObject sizes = new JsonObject();
        JsonObject asJsonObjectFinal = JsonParser.parseString(
                client.newCall(new Request.Builder()
                                .url("https://api.bilibili.com/x/player/playurl?" + paramBase)
                                .get()
                                .build())
                        .execute().body().string()
        ).getAsJsonObject();

        JsonArray accept_quality = asJsonObjectFinal.get("data").getAsJsonObject().get("accept_quality").getAsJsonArray();

        for (JsonElement q : accept_quality) {
            int qn = q.getAsInt();
            String param1 = WbiUtil.getParam(Map.of(
                    "cid", cid,
                    type, vid,
                    "fnval", 1,
                    "qn", qn
            ), imgKey, subKey);

            Request req = new Request.Builder()
                    .url("https://api.bilibili.com/x/player/playurl?" + param1)
                    .get()
                    .build();

            try (Response resp = client.newCall(req).execute()) {
                if (!resp.isSuccessful()) throw new IOException("HTTP " + resp.code());
                JsonObject asJsonObject1 = JsonParser.parseString(resp.body().string())
                        .getAsJsonObject().get("data").getAsJsonObject();
                if (asJsonObjectFinal == null) asJsonObjectFinal = asJsonObject1;

                JsonObject vo = asJsonObject1.get("durl").getAsJsonArray().get(0).getAsJsonObject();
                sizes.addProperty(String.valueOf(asJsonObject1.get("quality").getAsInt()), vo.get("size").getAsLong());

                BiliPlaybackData bpd = new BiliPlaybackData();
                bpd.isDash = false;
                bpd.qn = asJsonObject1.get("quality").getAsInt();
                bpd.size = vo.get("size").getAsLong();
                bpd.videoUrl = vo.get("url").getAsString();
                result.add(bpd);
            }
        }

        if (asJsonObjectFinal != null)
            asJsonObjectFinal.add("sizes", sizes);

        return TuplesKt.to(asJsonObjectFinal != null ? asJsonObjectFinal.toString() : "{}", result);
    }
}
