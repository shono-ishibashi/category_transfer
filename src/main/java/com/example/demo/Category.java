package com.example.demo;

public class Category {
    private Integer id;
    private String parentName;
    private String grandParentName;
    private String categoryName;
    private String nameAll;
    private Integer depth;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }

    public String getNameAll() {
        return nameAll;
    }

    public void setNameAll(String nameAll) {
        this.nameAll = nameAll;
    }

    public String getParentName() {
        return parentName;
    }

    public void setParentName(String parentName) {
        this.parentName = parentName;
    }

    public String getGrandParentName() {
        return grandParentName;
    }

    public void setGrandParentName(String grandParentName) {
        this.grandParentName = grandParentName;
    }

    public Integer getDepth() {
        return depth;
    }

    public void setDepth(Integer depth) {
        this.depth = depth;
    }

    @Override
    public String toString() {
        return "Category{" +
                "id=" + id +
                ", parentName='" + parentName + '\'' +
                ", grandParentName='" + grandParentName + '\'' +
                ", categoryName='" + categoryName + '\'' +
                ", nameAll='" + nameAll + '\'' +
                '}';
    }
}
