package com.campus.lostfound.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class ItemForm {

    @NotBlank(message = "Please enter an item title.")
    @Size(max = 120, message = "Title must be 120 characters or fewer.")
    private String title;

    @NotBlank(message = "Please describe the item.")
    @Size(max = 2000, message = "Description must be 2,000 characters or fewer.")
    private String description;

    @NotBlank(message = "Please enter a category.")
    @Size(max = 60, message = "Category must be 60 characters or fewer.")
    private String category;

    @NotBlank(message = "Please enter where the item was lost or found.")
    @Size(max = 160, message = "Location must be 160 characters or fewer.")
    private String location;

    @NotNull(message = "Choose whether this item was lost or found.")
    private ReportType reportType;

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public ReportType getReportType() {
        return reportType;
    }

    public void setReportType(ReportType reportType) {
        this.reportType = reportType;
    }

    public static ItemForm from(Item item) {
        ItemForm form = new ItemForm();
        form.setTitle(item.getTitle());
        form.setDescription(item.getDescription());
        form.setCategory(item.getCategory());
        form.setLocation(item.getLocation());
        form.setReportType(item.getReportType());
        return form;
    }
}
