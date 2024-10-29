package com.bossymr.rapid.robot.api.client.response;

import com.bossymr.rapid.robot.api.GenericType;
import com.bossymr.rapid.robot.api.NetworkManager;
import com.bossymr.rapid.robot.api.ResponseConverter;
import com.bossymr.rapid.robot.api.ResponseConverterFactory;
import com.bossymr.rapid.robot.api.client.entity.EntityModel;
import com.bossymr.rapid.robot.api.client.entity.ResponseModel;
import org.apache.tika.utils.XMLReaderUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ResponseModelConverter implements ResponseConverter<ResponseModel> {

    public static final ResponseConverterFactory FACTORY = new ResponseConverterFactory() {
        @SuppressWarnings("unchecked")
        @Override
        public <T> ResponseConverter<T> create(@NotNull NetworkManager manager, @NotNull GenericType<T> type) {
            if (type.getRawType().equals(ResponseModel.class)) {
                return (ResponseConverter<T>) new ResponseModelConverter();
            }
            return null;
        }
    };

    @Override
    public @Nullable ResponseModel convert(@NotNull HttpResponse<byte[]> response) throws IOException {
        byte[] body = response.body();
        return convert(body);
    }

    public @NotNull ResponseModel convert(byte @NotNull [] body) {
        String input = new String(body, StandardCharsets.UTF_8);
        Pattern pattern = Pattern.compile("\"([^\"]*)\"");
        Matcher matcher = pattern.matcher(input);
        input = matcher.replaceAll(result -> result.group()
                .replaceAll("<", "&lt;")
                .replaceAll(">", "&gt;"));
        return ResponseModel.fromXML(input);
    }
}
