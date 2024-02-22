import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.reflect.TypeToken;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import com.google.gson.*;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Objects;

public class Main {
    enum JsonLib {GSON, JACKSON}

    private static final String NASA_API_KEY = "6419oH4x4ljVWBY5qwtedZ5rtDkrghtlMVJDpJbI";
    private static final String NASA_URL = "https://api.nasa.gov/planetary/apod?api_key=" + NASA_API_KEY;

    public static void main(String[] args) throws IOException {
        CloseableHttpClient cli = HttpClients.createDefault();
        CloseableHttpResponse res = cli.execute(new HttpGet(NASA_URL));
        NasaMediaRecord rec = jsonToNasaMediaRecord(
                new String(res.getEntity().getContent().readAllBytes()),
                JsonLib.GSON /*JsonLib.JACKSON*/
        );

        String url = Objects.requireNonNull(rec).getUrl();
        res = cli.execute(new HttpGet(url));
        HttpEntity media = res.getEntity();
        System.out.println("==> DOWNLOADED: NASA MEDIA BINARY BY URL\n" + url);

        String[] chunks = url.split("/");
        String fileName = chunks[chunks.length - 1];
        FileOutputStream fos = new FileOutputStream(fileName);
        media.writeTo(fos);
        fos.close();
        System.out.println("==> SAVED: NASA MEDIA FILE\n./" + fileName);
    }

    private static NasaMediaRecord jsonToNasaMediaRecord(String content, JsonLib jsonLib) throws JsonProcessingException {
        NasaMediaRecord rec = switch (jsonLib) {
            case GSON -> new GsonBuilder().create().fromJson(content, NasaMediaRecord.class);
            case JACKSON -> new ObjectMapper().readValue(content, NasaMediaRecord.class);
        };
        String pretty = new GsonBuilder()
                .setPrettyPrinting()
                .create()
                .toJson(rec, new TypeToken<NasaMediaRecord>() {
                }.getType());
        System.out.println("==> RECEIVED: NASA MEDIA RECORD\n" + pretty);
        return rec;
    }
}
