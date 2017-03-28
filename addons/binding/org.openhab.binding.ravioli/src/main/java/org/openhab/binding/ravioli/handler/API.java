package org.openhab.binding.ravioli.handler;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

import org.json.JSONException;
import org.json.JSONObject;

public class API {
    public static final int DEFAULT_CONNECTION_TIMEOUT = 15000;
    public static final int DEFAULT_READ_TIMEOUT = 60000;

    public enum HttpMethod {
        POST,
        GET
    }

    public enum Error {
        NO_ERROR(0),
        UNKNOWN_ERROR(1) // responseCode != 200
        ,
        INVALID_TOKEN(401) // токен пользователя неверный
        ,
        SERVICE_ERROR(500); // сервис недоступен

        private int code;

        public int getCode() {
            return code;
        }

        private Error(int code) {
            this.code = code;
        }

        private static Error fromCode(int code) {
            for (Error er : Error.values()) {
                if (er.getCode() == code) {
                    return er;
                }
            }
            return Error.UNKNOWN_ERROR;
        }
    }

    public static final class ApiResponse {
        private boolean success;
        private int statusCode;
        private Error error;
        private String errorString;
        private JSONObject json;

        public ApiResponse(Error error) {
            this.error = error;
            success = error == Error.NO_ERROR;
        }

        public ApiResponse(int statusCode, String responseString) throws IOException, JSONException {
            this.statusCode = statusCode;
            this.json = new JSONObject(responseString);
            this.success = statusCode == 200;
            if (!success) {
                this.errorString = responseString;
                int errorCode = 401;
                this.error = Error.fromCode(errorCode);

            } else {
                error = Error.NO_ERROR;
            }
        }

        public JSONObject getJson() {
            return json;
        }

    }

    public static final class Header {
        private String key;
        private String value;

        public Header(String key, String value) {
            this.key = key;
            this.value = value;
        }

        public String getKey() {
            return key;
        }

        public String getValue() {
            return value;
        }
    }

    private static String getParamsString(String... args) {
        StringBuilder builder = new StringBuilder();
        if (args.length % 2 != 0) {
            throw new IllegalArgumentException("Request parameters should come in pairs");
        }
        for (int i = 0; i < args.length; i += 2) {
            if (i != 0) {
                builder.append("&");
            }
            builder.append(args[i]);
            builder.append("=");
            try {

                builder.append(URLEncoder.encode(args[i + 1], "UTF-8"));
            } catch (UnsupportedEncodingException e) {
            }
        }
        return builder.toString();
    }

    public static ApiResponse execute(String path, HttpMethod method, Header[] headers, String... args) {
        ApiResponse response;
        if (method == HttpMethod.GET) {
            response = executeGet(path, headers, args);
        } else if (method == HttpMethod.POST) {
            response = executePost(path, headers, getParamsString(args));
        } else {
            throw new IllegalArgumentException("Unknown http method " + method);
        }
        return response;
    }

    public static String getRequestPath(String path, String... params) {
        String query = getParamsString(params);
        if (params.length == 0) {
            return path;
        } else {
            return String.format("%s?%s", path, query);
        }
    }

    public static ApiResponse executeGet(String path, Header[] headers, String... params) {
        return executeGet(getRequestPath(path, params), headers);
    }

    private static ApiResponse executeGet(String url, Header[] headers) {

        InputStream input = null;
        try {
            HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
            connection.setRequestMethod(HttpMethod.GET.toString());
            connection.setConnectTimeout(DEFAULT_CONNECTION_TIMEOUT);
            connection.setReadTimeout(DEFAULT_READ_TIMEOUT);
            connection.setDoInput(true);
            connection.setDoOutput(false);
            connection.setUseCaches(false);
            if (headers != null) {
                for (Header x : headers) {
                    connection.setRequestProperty(x.getKey(), x.getValue());
                }
            }
            int responseCode = connection.getResponseCode();

            String response = "";
            if (responseCode == 200) {
                response = InputStreamUtils.toString(connection.getInputStream());
            } else {
                response = InputStreamUtils.toString(connection.getErrorStream());
            }

            switch (responseCode) {
                case 200:
                    return new ApiResponse(responseCode, response);
                case 401:
                    return new ApiResponse(Error.INVALID_TOKEN.getCode(), response);
                case 500:
                    return new ApiResponse(Error.SERVICE_ERROR.getCode(), response);
                default:
                    return new ApiResponse(Error.UNKNOWN_ERROR);
            }
        } catch (Exception e) {
            return new ApiResponse(Error.UNKNOWN_ERROR);
        } finally {
            try {
                if (input != null) {
                    input.close();
                }
            } catch (Exception ignore) {
            }
        }
    }

    private static ApiResponse executePost(String url, Header[] headers, String entity) {

        InputStream input = null;
        OutputStream ostream = null;
        try {
            HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
            connection.setConnectTimeout(DEFAULT_CONNECTION_TIMEOUT);
            connection.setReadTimeout(DEFAULT_READ_TIMEOUT);
            connection.setDoInput(true);
            connection.setDoOutput(true);
            connection.setRequestMethod(HttpMethod.POST.toString());
            if (headers != null) {
                for (Header x : headers) {
                    connection.setRequestProperty(x.getKey(), x.getValue());
                }
            }
            ostream = new DataOutputStream(connection.getOutputStream());
            ostream.write(entity.getBytes("UTF-8"));

            int responseCode = connection.getResponseCode();
            String response = "";
            if (responseCode == 200) {
                response = InputStreamUtils.toString(connection.getInputStream());
            } else {
                response = InputStreamUtils.toString(connection.getErrorStream());
            }

            switch (responseCode) {
                case 200:
                    return new ApiResponse(responseCode, response);
                case 401:
                    return new ApiResponse(Error.INVALID_TOKEN.getCode(), response);
                case 500:
                    return new ApiResponse(Error.SERVICE_ERROR.getCode(), response);
                default:
                    return new ApiResponse(Error.UNKNOWN_ERROR);
            }

        } catch (Exception e) {
            return new ApiResponse(Error.UNKNOWN_ERROR);
        } finally {
            try {
                if (ostream != null) {
                    ostream.close();
                }
                if (input != null) {
                    input.close();
                }
            } catch (Exception ignore) {
            }
        }
    }
}