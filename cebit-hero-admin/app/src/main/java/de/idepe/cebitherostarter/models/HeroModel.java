package de.idepe.cebitherostarter.models;

import java.util.List;
import java.util.concurrent.TimeUnit;

public class HeroModel {
    boolean active;

    int id;
    int score;
    String name;
    String code;
    String email;
    String company;
    String contact;

    double lat;
    double lng;
    long created = -1;
    long next = -1;

    public long getNext() {
        if(next == -1){
            return System.currentTimeMillis() + TimeUnit.HOURS.toMillis(1);
        }
        return next;
    }

    public void setNext(long next) {
        this.next = next;
    }


    List<HeroModelTupel> scorehistory;

    class HeroModelTupel{
        int score;
        double timestamp;

        public int getScore() {
            return score;
        }

        public void setScore(int score) {
            this.score = score;
        }

        public double getTimestamp() {
            return timestamp;
        }

        public void setTimestamp(double timestamp) {
            this.timestamp = timestamp;
        }
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getCompany() {
        return company;
    }

    public void setCompany(String company) {
        this.company = company;
    }

    public String getContact() {
        return contact;
    }

    public void setContact(String contact) {
        this.contact = contact;
    }

    public double getLat() {
        return lat;
    }

    public void setLat(double lat) {
        this.lat = lat;
    }

    public double getLng() {
        return lng;
    }

    public void setLng(double lng) {
        this.lng = lng;
    }

    public long getCreated() {
        return created;
    }

    public void setCreated(long created) {
        this.created = created;
    }

    public List<HeroModelTupel> getScorehistory() {
        return scorehistory;
    }

    public void setScorehistory(List<HeroModelTupel> scorehistory) {
        this.scorehistory = scorehistory;
    }
}
