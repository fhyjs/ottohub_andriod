package org.eu.hanana.reimu.ottohub_andriod.util.ft;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import org.eu.hanana.reimu.lib.ottohub.util.ProgressedRequestBody;
import org.eu.hanana.reimu.ottohub_andriod.MyApp;
import org.eu.hanana.reimu.ottohub_andriod.util.ApiUtil;
import org.eu.hanana.reimu.ottohub_andriod.util.BiliPlaybackData;
import org.eu.hanana.reimu.ottohub_andriod.util.BiliPlaybackUtil;
import org.eu.hanana.reimu.ottohub_andriod.util.TimerLoopThread;

import kotlin.Pair;
import okhttp3.*;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;
import java.util.stream.Collectors;


public class DownloadVideoHandler {
    public interface OutCtrl{
        void sendString(String string);
        void sendClose();
    }
    public static class Runner implements Runnable{
        private final OutCtrl outCtrl;
        private JsonObject args = null;
        public Throwable error = null;
        public Runner(OutCtrl outCtrl){
            this.outCtrl=outCtrl;
            outCtrl.sendString("{\"op\":\"status\",\"data\":\"waiting\"}");
            outCtrl.sendString(String.format("{\"op\":\"msg\",\"data\":\"%s\"}","server:等待客户端发送数据"));
            sendOpString("start_pre","");
        }

        public void input(JsonElement jsonElement) {
            this.input(jsonElement,true);
        }
        public void input(JsonElement jsonElement,boolean run) {
            var jo = jsonElement.getAsJsonObject();
            var op = jo.get("op").getAsString();
            if (op.equals("start")){
                if (args!=null) return;
                args=jo.get("data").getAsJsonObject();
                if (run) {
                    new Thread(this).start();
                }
            }
        }
        protected void sendOpString(String op,String data){
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("op",op);
            jsonObject.addProperty("data",data);
            outCtrl.sendString(jsonObject.toString());
        }

        @Override
        public void run() {
            var tmpFiles = new ArrayList<File>();
            try {
                sendOpString("status","准备中");
                sendOpString("progress","99%");
                sendOpString("msg","数据已接收");
                sendOpString("msg","avid: "+args.get("aid").getAsString());
                sendOpString("msg","cid: "+args.get("cid").getAsString());
                sendOpString("msg","清晰度: "+args.get("qn").getAsString());

                String picData = args.get("pic").getAsString();
                String base64Image = picData.split(",")[1]; // 去掉 "data:image/jpeg;base64," 前缀
                byte[] imageBytes = Base64.getDecoder().decode(base64Image);
                Bitmap bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length);

                var title = args.get("title").getAsString();
                var desc = args.get("desc").getAsString();
                var qn = args.get("qn").getAsString();
                var sessData = "";
                var tags = new HashSet<String>();
                for (JsonElement jsonElement : args.get("tags").getAsJsonArray()) {
                    tags.add(jsonElement.getAsString());
                }
                var punctuationMarks = new char[]{
                        '!', '"', '#', '$', '%', '&', '\'', '(', ')', '*', '+', ',', '-', '.', '/', ':', ';', '<', '=', '>', '?', '@', '[', '\\', ']', '^', '_', '`', '{', '|', '}', '~', '—',
                        '！', '“', '”', '＃', '￥', '％', '＆', '’', '（', '）', '＊', '＋', '，', '－', '．', '／', '：', '；', '＜', '＝', '＞', '？', '＠', '［', '＼', '］', '＾', '＿', '｀', '｛', '｜', '｝', '～',
                        '。', '，', '！', '；', '：', '（', '）', '［', '］', '｛', '｝', '⋯', '﹐', '﹑', '。', '、', '〃', '〝', '〞', '〟', '﹔', '﹕', '﹖', '﹗', '「', '」', '『', '』', '【', '】', '〝', '〞',
                        '\u2000', '\u2001', '\u2002', '\u2003', '\u2004', '\u2005', '\u2006', '\u2007', '\u2008', '\u2009', '\u200A', '\u200B', '\u2028', '\u2029', '\u202F', '\u205F', '\u2060'
                };
                if (tags.size()>10){
                    throw new IllegalArgumentException("标签数量限制笑传之超超标");
                }
                for (String tag : tags) {
                    if (tag.isBlank()) throw new IllegalArgumentException("有空标签");
                    if (tag.length()>20) throw new IllegalArgumentException("那我希望你的退役时间和你的标签一样长");
                    for (char c : punctuationMarks) {
                        if (tag.indexOf(c) != -1) {
                            throw new IllegalArgumentException("标签有非法字符\\n牢内");
                        }
                    }
                }
                if (title.length()>=44)throw new IllegalArgumentException("标题太长了");
                if (desc.length()>=222)throw new IllegalArgumentException("简介太长了");
                ByteArrayOutputStream picImage = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.JPEG, 80, picImage); // 100 表示不压缩

                if (picImage.size()> 1048576) throw new IllegalArgumentException("封面过大");
                sendOpString("msg","封面(bytes): "+picImage.size());
                if (!args.has("right")) throw new IllegalArgumentException("未选择版权");
                var right = String.valueOf(args.get("right").getAsInt());
                if (!args.has("type")) throw new IllegalArgumentException("未选择类型");
                var type = String.valueOf(args.get("type").getAsInt());
                sendOpString("msg","获取视频元数据...");
                String vurl;
                String aurl;
                long size;
                BiliPlaybackData playbackData;
                var login=!sessData.isEmpty();
                try{
//                    HttpRequest request = HttpRequest.newBuilder()
//                            .uri(URI.create("https://api.bilibili.com/x/player/playurl?cid="+args.get("cid").getAsString()+"&avid="+args.get("aid").getAsString()+"&fnval=1&qn="+qn))
//                            .method("GET", HttpRequest.BodyPublishers.noBody())
//                            .build();
//                    HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
//                    JsonObject asJsonObject = JsonParser.parseString(response.body()).getAsJsonObject();
//                    vurl = asJsonObject.get("data").getAsJsonObject().get("durl").getAsJsonArray().get(0).getAsJsonObject().get("url").getAsString();
//                    size = asJsonObject.get("data").getAsJsonObject().get("durl").getAsJsonArray().get(0).getAsJsonObject().get("size").getAsBigInteger().longValue();
                    Pair<String, List<BiliPlaybackData>> data;
                    if (login){
                        data= BiliPlaybackUtil.getDashPlaybackData(sessData,"avid",args.get("aid").getAsString(),args.get("cid").getAsString());
                    }else {
                        data=BiliPlaybackUtil.getMp4PlaybackData("avid",args.get("aid").getAsString(),args.get("cid").getAsString());
                    }
                    Map<String,BiliPlaybackData> playbackDataMap = data.getSecond().stream()
                            .collect(Collectors.collectingAndThen(
                                Collectors.toMap(u -> u.qn, u -> u, (a, b) -> a), // 如果重复取第一个
                                m -> m.values().stream()
                             )).collect(Collectors.toMap(biliPlaybackData -> String.valueOf(biliPlaybackData.qn),biliPlaybackData -> biliPlaybackData));
                    if (playbackDataMap.containsKey(qn)){
                        playbackData=playbackDataMap.get(qn);
                    }else {
                        var lQn = 0;
                        for (String s : playbackDataMap.keySet()) {
                            lQn=Math.max(Integer.parseInt(s),lQn);
                        }
                        playbackData=playbackDataMap.get(String.valueOf(lQn));
                    }
                    sendOpString("msg","最终命中清晰度: "+playbackData.qn);
                    aurl= playbackData.audioUrl;
                    vurl= playbackData.videoUrl;
                    size= playbackData.size;
                }catch (Exception e){
                    throw e;
                }

                Pair<String, List<BiliPlaybackData>> data;

                sendOpString("msg",vurl);
                sendOpString("status", "下载视频");
                if (size> 104857600 * 2) throw new IllegalStateException("视频大于200MB,请降低清晰度或手动上传");
                try{
                    var dir= new File(MyApp.getInstance().getCacheDir(),"tmp/ohupd");
                    if (!dir.exists()) dir.mkdirs();

                    var fileV = new File(dir, ApiUtil.generateRandomString(10)+(playbackData.isDash?".m4s":".mp4"));
                    fileV.createNewFile();
                    tmpFiles.add(fileV);
                    dlFile(vurl,fileV,size);
                    var outFile = ApiUtil.generateRandomString(10)+".mp4";
                    tmpFiles.add(new File(dir,outFile));
                    if (playbackData.isDash) {
                        sendOpString("msg","哇袄！这是DASH格式视频！！需要进行神秘操作！先来ccb吧");
                        var fileA = new File(dir, ApiUtil.generateRandomString(10) + ".m4s");
                        tmpFiles.add(fileA);
                        dlFile(aurl,fileA,playbackData.sizeA);

                        // 构建命令
                        String[] command = {
                                "ffmpeg",
                                "-i", fileA.getName(),
                                "-i", fileV.getName(),
                                "-c:v", "copy",
                                "-c:a", "copy",
                                "-f", "mp4",
                                outFile
                        };

                        ProcessBuilder pb = new ProcessBuilder(command);
                        pb.directory(dir); // 设置运行目录
                        pb.redirectErrorStream(true); // 合并 stderr 到 stdout

                        try {
                            Process process = pb.start();
                            // 异步线程读取输出
                            new Thread(() -> {
                                try (BufferedReader reader = new BufferedReader(
                                        new InputStreamReader(process.getInputStream()))) {
                                    String line;
                                    while ((line = reader.readLine()) != null) {
                                        sendOpString("msg","[ffmpeg] " + line);
                                    }
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            }).start();
                            int exitCode = process.waitFor(); // 等待执行完成
                            sendOpString("msg","ffmpeg 执行结束，退出码: " + exitCode);
                        } catch (Exception e) {
                            throw new RuntimeException(e);
                        }
                    }

                    sendOpString("status", "上传中");
                    sendOpString("msg", "开始上传");
                    sendOpString("msg", "远程地址: "+"Android Hanana2.link");
                    OkHttpClient client = new OkHttpClient.Builder()
                            .readTimeout(1, TimeUnit.MINUTES)
                            .build();
                    var tagstr = new StringBuilder();
                    for (String tag : tags) {
                        tagstr.append("#").append(tag);
                    }
                    // 构造 Multipart 表单
                    MultipartBody.Builder multipartBuilder = new MultipartBody.Builder()
                            .setType(MultipartBody.FORM)
                            .addFormDataPart("title", title)
                            .addFormDataPart("intro", desc)
                            .addFormDataPart("token", ApiUtil.getAppApi().getLoginToken())
                            .addFormDataPart("type", right)
                            .addFormDataPart("action", "submit_video")
                            .addFormDataPart("category", type)
                            .addFormDataPart("tag", tagstr.toString());
                    multipartBuilder.addFormDataPart("file_jpg", ApiUtil.generateRandomString(10)+".jpg", RequestBody.create(picImage.toByteArray(), MediaType.get("image/jpeg")));
                    multipartBuilder.addFormDataPart("file_mp4", ApiUtil.generateRandomString(10)+".mp4", RequestBody.create((playbackData.isDash?new File(dir,outFile):fileV), MediaType.get("video/mp4")));

                    RequestBody requestBody = multipartBuilder.build();
                    ProgressedRequestBody requestBody1 = null;
                    // 构造请求
                    Request okHrequest = new Request.Builder()
                            .url("https://api.ottohub.cn/module/creator/submit_video.php")
                            .post(requestBody1=new ProgressedRequestBody(requestBody,(written, length, progress) -> {}))

                            //.addHeader("Cookie", String.format("login_token=%s; PHPSESSID=%s",asJsonObject.get("token").getAsString(),asJsonObject.get("PHPSESSID").getAsString())) // 替换为实际 token
                            .build();
                    ProgressedRequestBody finalRequestBody = requestBody1;
                    Thread listener = new TimerLoopThread<>(new AtomicReference<>(finalRequestBody), progressedRequestBody -> {
                        double progress = progressedRequestBody.getProgress();
                        sendOpString("progress", String.valueOf(progress*100));
                        return progress >= 1;
                    }, 100);
                    listener.start();
                    // 发送请求
                    try (Response okHresponse = client.newCall(okHrequest).execute()) {
                        listener.interrupt();
                        if (okHresponse.isSuccessful()) {
                            String string = okHresponse.body().string();
                            sendOpString("msg","<font color='red'>"+string+"</font>");
                            if (!string.contains("success")){
                                throw new IllegalStateException("远程返回: "+string);
                            }else {
                                sendOpString("success","ok");
                            }
                        } else {
                            System.out.println("Request failed: " + okHresponse.message());
                        }
                    }finally {
                        listener.interrupt();
                    }
                }catch (Exception e){
                    throw e;
                }

            }catch (Throwable e){
                sendOpString("error", Base64.getEncoder().encodeToString(e.toString().getBytes(StandardCharsets.UTF_8)));
                sendOpString("status", "发生错误,按返回上一步调整设置");
                e.printStackTrace();
                error=e;
            }finally {
                outCtrl.sendClose();
                for (File tmpFile : tmpFiles) {
                    if (tmpFile.exists()){
                        tmpFile.delete();
                    }
                }
            }
        }

        private void dlFile(String vurl, File file, long size) throws IOException {
            sendOpString("msg", file + " 文件大小: " + size);

            OkHttpClient client = new OkHttpClient.Builder()
                    .readTimeout(1, TimeUnit.MINUTES)
                    .build();

            Request request = new Request.Builder()
                    .url(vurl)
                    .get()
                    .header("Referer", "https://www.bilibili.com/")
                    .header("Origin", "https://www.bilibili.com")
                    .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/133.0.0.0 Safari/537.36")
                    .build();

            Call call = client.newCall(request);
            Response response = call.execute();

            if (!response.isSuccessful()) {
                throw new IOException("请求失败: " + response);
            }

            InputStream inputStream = response.body().byteStream();
            FileOutputStream outputStream = new FileOutputStream(file);

            byte[] buffer = new byte[512 * 1024]; // 512KB 缓冲
            long downloaded = 0;
            int read;

            long startTime = System.currentTimeMillis();

            while ((read = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, read);
                downloaded += read;

                // 进度
                if (size > 0) {
                    double progress = (double) downloaded / size;
                    sendOpString("progress", String.valueOf(progress * 100));
                }
            }

            outputStream.flush();
            outputStream.close();
            inputStream.close();

            long totalTime = System.currentTimeMillis() - startTime;
            sendOpString("msg", "下载完成, 用时(ms): " + totalTime);
        }

    }
}
