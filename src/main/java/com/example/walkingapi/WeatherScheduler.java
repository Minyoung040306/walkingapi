package com.example.walkingapi;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import javax.sql.DataSource;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.HashMap;
import java.util.Map;

@Component
public class WeatherScheduler {

    private final DataSource dataSource;


    private final String SERVICE_KEY = "";

    public WeatherScheduler(DataSource dataSource) {
        this.dataSource = dataSource;
    }


    @Scheduled(fixedRate = 3600000)
    public void updateWeatherAndDust() {
        System.out.println("실시간 날씨 및 미세먼지 동기화 ");


        Map<String, int[]> regionCoords = new HashMap<>();
        regionCoords.put("서울", new int[]{60, 127});
        regionCoords.put("대전", new int[]{67, 104});
        regionCoords.put("대구", new int[]{89, 90});
        regionCoords.put("경기", new int[]{60, 120});
        regionCoords.put("강원", new int[]{73, 134});
        regionCoords.put("충남", new int[]{68, 100});
        regionCoords.put("충북", new int[]{69, 107});
        regionCoords.put("전남", new int[]{51, 67});
        regionCoords.put("전북", new int[]{63, 89});
        regionCoords.put("경남", new int[]{91, 77});

        for (String region : regionCoords.keySet()) {
            int nx = regionCoords.get(region)[0];
            int ny = regionCoords.get(region)[1];

            double temp = fetchTemperature(nx, ny);
            String rainType = fetchRainType(nx, ny);
            String fineDust = fetchFineDust(region);


        }
        System.out.println("=== 실시간 기상 데이터 DB 적재 완료 ===");
    }


    private double fetchTemperature(int nx, int ny) {
        try {

            String urlStr = "http://apis.data.go.kr/1360000/VilageFcstInfoService_2.0/getUltraSrtNcst"
                    + "?serviceKey=" + URLEncoder.encode(SERVICE_KEY, "UTF-8")
                    + "&pageNo=1&numOfRows=10&dataType=JSON"
                    + "&base_date=20260604&base_time=0600"
                    + "&nx=" + nx + "&ny=" + ny;

            String json = callApi(urlStr);


            if (json.contains("\"category\":\"T1H\"")) {
                int index = json.indexOf("\"category\":\"T1H\"");
                int valStart = json.indexOf("\"obsrValue\":\"", index) + 13;
                int valEnd = json.indexOf("\"", valStart);
                return Double.parseDouble(json.substring(valStart, valEnd));
            }
        } catch (Exception e) {
            System.out.println("기상청 API 호출 실패 (격자 " + nx + "," + ny + "): " + e.getMessage());
        }
        return 22.0;
    }


    private String fetchRainType(int nx, int ny) {
        try {
            String urlStr = "http://apis.data.go.kr/1360000/VilageFcstInfoService_2.0/getUltraSrtNcst"
                    + "?serviceKey=" + URLEncoder.encode(SERVICE_KEY, "UTF-8")
                    + "&pageNo=1&numOfRows=10&dataType=JSON"
                    + "&base_date=20260604&base_time=0600"
                    + "&nx=" + nx + "&ny=" + ny;

            String json = callApi(urlStr);
            if (json.contains("\"category\":\"PTY\"")) {
                int index = json.indexOf("\"category\":\"PTY\"");
                int valStart = json.indexOf("\"obsrValue\":\"", index) + 13;
                int valEnd = json.indexOf("\"", valStart);
                String ptyCode = json.substring(valStart, valEnd);

                if (ptyCode.equals("1")) return "비";
                if (ptyCode.equals("2")) return "비/눈";
                if (ptyCode.equals("3")) return "눈";
                if (ptyCode.equals("4")) return "소나기";
            }
        } catch (Exception e) {}
        return "없음";
    }


    private String fetchFineDust(String region) {
        try {

            String urlStr = "http://apis.data.go.kr/B552584/ArpltnInforInqireService/getCtprvnRltmMesureDnsty"
                    + "?serviceKey=" + URLEncoder.encode(SERVICE_KEY, "UTF-8")
                    + "&returnType=json&numOfRows=1&pageNo=1"
                    + "&sidoName=" + URLEncoder.encode(region, "UTF-8")
                    + "&ver=1.0";

            String json = callApi(urlStr);
            if (json.contains("\"pm10Grade\":\"")) {
                int valStart = json.indexOf("\"pm10Grade\":\"") + 13;
                int valEnd = json.indexOf("\"", valStart);
                String grade = json.substring(valStart, valEnd);

                if (grade.equals("1")) return "좋음";
                if (grade.equals("2")) return "보통";
                if (grade.equals("3")) return "나쁨";
                if (grade.equals("4")) return "매우나쁨";
            }
        } catch (Exception e) {
            System.out.println("미세먼지 API 호출 실패 (" + region + "): " + e.getMessage());
        }
        return "보통";
    }


    private String callApi(String urlStr) throws Exception {
        URL url = new URL(urlStr);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        conn.setRequestProperty("Content-type", "application/json");

        BufferedReader rd;
        if (conn.getResponseCode() >= 200 && conn.getResponseCode() <= 300) {
            rd = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"));
        } else {
            rd = new BufferedReader(new InputStreamReader(conn.getErrorStream(), "UTF-8"));
        }
        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = rd.readLine()) != null) {
            sb.append(line);
        }
        rd.close();
        conn.disconnect();
        return sb.toString();
    }


    private void saveToDatabase(String region, double temp, String rainType, String fineDust) {
        String sql = "INSERT INTO weather (region_name, temperature, rain_type, fine_dust) VALUES (?, ?, ?, ?) "
                + "ON DUPLICATE KEY UPDATE temperature = ?, rain_type = ?, fine_dust = ?";

        try (
                Connection conn = dataSource.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)
        ) {
            stmt.setString(1, region);
            stmt.setDouble(2, temp);
            stmt.setString(3, rainType);
            stmt.setString(4, fineDust);


            stmt.setDouble(5, temp);
            stmt.setString(6, rainType);
            stmt.setString(7, fineDust);

            stmt.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}