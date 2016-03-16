package com.example.shustrik.vkdocs.vk;

import java.util.List;

public class MyVKDocsAttachments {
    private List<MyVKApiDocument> documents;
    private String next;

    public MyVKDocsAttachments(List<MyVKApiDocument> documents, String next) {
        this.documents = documents;
        this.next = next;
    }

    public List<MyVKApiDocument> getDocuments() {
        return documents;
    }

    public String getNext() {
        return next;
    }

    public boolean isNext() {
        return next != null && !next.isEmpty();
    }
}
