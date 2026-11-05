package com.print.kiosk.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class RenameInventoryItemRequest {

    @NotBlank(message = "Item name is required")
    @Size(max = 120, message = "Item name must not exceed 120 characters")
    private String name;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}