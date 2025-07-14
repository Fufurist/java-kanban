package server;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class GsonDateTimeCustomParse extends TypeAdapter<LocalDateTime> {
    DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy.MM.dd-HH:mm");

    @Override
    public void write(JsonWriter jsonWriter, LocalDateTime localDateTime) throws IOException {
        if (localDateTime == null){
            jsonWriter.nullValue();
            return;
        }
        jsonWriter.value(localDateTime.format(dtf));
    }

    @Override
    public LocalDateTime read(JsonReader jsonReader) throws IOException {
        if (jsonReader.peek() == JsonToken.NULL) {
            jsonReader.nextNull();
            return null;
        }
        return LocalDateTime.parse(jsonReader.nextString(), dtf);
    }
}
