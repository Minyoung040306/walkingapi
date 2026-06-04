package com.example.walkingapi;

public class Walk {
    private String id;
    private String courseId;
    private String trailName;
    private String courseName;
    private String courseDesc;
    private String districtName;
    private String difficulty;
    private double courseKm;
    private String address;
    private String water;
    private String toilet;

    public Walk(String id, String courseId, String trailName, String courseName,
                String courseDesc, String districtName, String difficulty, double courseKm,
                String address, String water, String toilet)
    {
        this.id = id;
        this.courseId = courseId;
        this.trailName = trailName;
        this.courseName = courseName;
        this.courseDesc = courseDesc;
        this.districtName = districtName;
        this.difficulty = difficulty;
        this.courseKm = courseKm;
        this.address = address;
        this.water = water;
        this.toilet = toilet;
    }


    public String getId() { return id; }
    public String getCourseId() { return courseId; }
    public String getTrailName() { return trailName; }
    public String getCourseName() { return courseName; }
    public String getCourseDesc() { return courseDesc; }
    public String getDistrictName() { return districtName; }
    public String getDifficulty() { return difficulty; }
    public double getCourseKm() { return courseKm; }
    public String getAddress() { return address; }
    public String getWater() { return water; }
    public String getToilet() { return toilet; }
}
