package com.example.walkingapi;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
public class WalkController {

    private final DataSource dataSource;

    public WalkController(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    // 전체 산책로 및 정렬 조회
    @GetMapping("/api/walks")
    public List<Map<String, Object>> getWalks(@RequestParam(value = "sort", defaultValue = "none") String sort) {
        List<Map<String, Object>> walkList = new ArrayList<>();

        String sql = "SELECT w1.id, w1.trail_name, w1.course_name, w1.course_desc, w1.district_name, "
                + "w2.difficulty, w2.course_km, w2.water, w2.toilet, w3.address, "
                + "we.temperature, we.rain_type, we.fine_dust "
                + "FROM walk1 w1 "
                + "JOIN walk2 w2 ON w1.id = w2.id "
                + "JOIN walk3 w3 ON w1.id = w3.id "
                + "LEFT JOIN weather we ON we.region_name = SUBSTRING(w1.district_name, 1, 2) ";

        if (sort.equals("difficulty")) sql += "ORDER BY CASE w2.difficulty WHEN '쉬움' THEN 1 WHEN '보통' THEN 2 WHEN '어려움' THEN 3 END ASC";
        else if (sort.equals("difficulty_desc")) sql += "ORDER BY CASE w2.difficulty WHEN '어려움' THEN 1 WHEN '보통' THEN 2 WHEN '쉬움' THEN 3 END ASC";
        else if (sort.equals("distance")) sql += "ORDER BY w2.course_km ASC";
        else if (sort.equals("distance_desc")) sql += "ORDER BY w2.course_km DESC";
        else if (sort.equals("name")) sql += "ORDER BY w1.trail_name ASC";

        try (
                Connection conn = dataSource.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql);
                ResultSet rs = stmt.executeQuery()
        ) {
            while (rs.next()) {
                Map<String, Object> walk = new HashMap<>();
                walk.put("id", rs.getString("id"));
                walk.put("trailName", rs.getString("trail_name"));
                walk.put("courseName", rs.getString("course_name"));
                walk.put("courseDesc", rs.getString("course_desc"));
                walk.put("districtName", rs.getString("district_name"));
                walk.put("difficulty", rs.getString("difficulty"));
                walk.put("courseKm", rs.getDouble("course_km"));
                walk.put("water", rs.getString("water"));
                walk.put("toilet", rs.getString("toilet"));
                walk.put("address", rs.getString("address"));


                walk.put("temperature", rs.getDouble("temperature"));
                walk.put("rainType", rs.getString("rain_type"));
                walk.put("fineDust", rs.getString("fine_dust"));

                walkList.add(walk);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return walkList;
    }

    // 드롭다운으로 지역 선택 및 텍스트 키워드 검색
    @GetMapping("/api/walks/search")
    public List<Map<String, Object>> searchWalks(
            @RequestParam(value = "district", required = false) String district,
            @RequestParam(value = "sort", defaultValue = "none") String sort) {

        List<Map<String, Object>> walkList = new ArrayList<>();

        String sql = "SELECT w1.id, w1.trail_name, w1.course_name, w1.course_desc, w1.district_name, "
                + "w2.difficulty, w2.course_km, w2.water, w2.toilet, w3.address, "
                + "we.temperature, we.rain_type, we.fine_dust "
                + "FROM walk1 w1 "
                + "JOIN walk2 w2 ON w1.id = w2.id "
                + "JOIN walk3 w3 ON w1.id = w3.id "
                + "LEFT JOIN weather we ON we.region_name = SUBSTRING(w1.district_name, 1, 2) "
                + "WHERE w1.district_name LIKE ? OR w1.trail_name LIKE ? OR w1.course_name LIKE ? ";

        if (sort.equals("difficulty")) sql += "ORDER BY CASE w2.difficulty WHEN '쉬움' THEN 1 WHEN '보통' THEN 2 WHEN '어려움' THEN 3 END ASC";
        else if (sort.equals("difficulty_desc")) sql += "ORDER BY CASE w2.difficulty WHEN '어려움' THEN 1 WHEN '보통' THEN 2 WHEN '쉬움' THEN 3 END ASC";
        else if (sort.equals("distance")) sql += "ORDER BY w2.course_km ASC";
        else if (sort.equals("distance_desc")) sql += "ORDER BY w2.course_km DESC";
        else if (sort.equals("name")) sql += "ORDER BY w1.trail_name ASC";

        try (
                Connection conn = dataSource.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)
        ) {
            String searchKeyword = "%" + (district != null ? district : "") + "%";
            stmt.setString(1, searchKeyword);
            stmt.setString(2, searchKeyword);
            stmt.setString(3, searchKeyword);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Map<String, Object> walk = new HashMap<>();
                    walk.put("id", rs.getString("id"));
                    walk.put("trailName", rs.getString("trail_name"));
                    walk.put("courseName", rs.getString("course_name"));
                    walk.put("courseDesc", rs.getString("course_desc"));
                    walk.put("districtName", rs.getString("district_name"));
                    walk.put("difficulty", rs.getString("difficulty"));
                    walk.put("courseKm", rs.getDouble("course_km"));
                    walk.put("water", rs.getString("water"));
                    walk.put("toilet", rs.getString("toilet"));
                    walk.put("address", rs.getString("address"));


                    walk.put("temperature", rs.getDouble("temperature"));
                    walk.put("rainType", rs.getString("rain_type"));
                    walk.put("fineDust", rs.getString("fine_dust"));

                    walkList.add(walk);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return walkList;
    }
}
