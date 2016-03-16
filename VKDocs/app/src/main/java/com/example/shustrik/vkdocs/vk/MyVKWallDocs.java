package com.example.shustrik.vkdocs.vk;


import java.util.List;

public class MyVKWallDocs {
    private int count;
    private List<MyVKApiDocument> results;

    public MyVKWallDocs(int count, List<MyVKApiDocument> results) {
        this.count = count;
        this.results = results;
    }

    public int getCount() {
        return count;
    }

    public List<MyVKApiDocument> getResults() {
        return results;
    }
}
